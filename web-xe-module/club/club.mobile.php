<?php
    /**
     * @class  clubMobile
     * @author KUvH (kuvh@live.co.kr)
     * @brief  동아리 모듈의 mobile class
     **/

require_once(_XE_PATH_.'modules/club/club.view.php');

class clubMobile extends clubView {
	function init()
	{
			$template_path = sprintf("%sm.skins/%s/",$this->module_path, $this->module_info->mskin);
            if(!is_dir($template_path)||!$this->module_info->skin) {
                $this->module_info->mskin = 'blue_Fly';
                $template_path = sprintf("%sm.skins/%s/",$this->module_path, $this->module_info->mskin);
            }

			$site_srl = Context::get('site_srl');
			if($site_srl){
				$oClubModel = &getModel('club');
				$args->site_srl = $site_srl;
				$siteInfo = $oClubModel->getClubInfo($args->site_srl);
				Context::set('siteInfo', $siteInfo);
			}

            $this->setTemplatePath($template_path);   
	}

	function dispClubIndex() {
		parent::dispClubIndex();
		$club_list = Context::get('club_list');

		if($club_list){
			foreach($club_list as $key => $val){
					// get cafe site members count 
					$myargs->site_srl = $val->site_srl;
					$output = executeQuery('club.getSiteMemberCount', $myargs);
					$member_count = $output->data;
					$club_list[$key]->memberCount = $member_count->member_count;
			}
		}
		Context::get('club_list', $club_list);

		$my_cafes = array();
		$logged_info = Context::get('logged_info');
		if($logged_info->member_srl) {
			$myargs->member_srl = $logged_info->member_srl;
			$output = executeQueryArray('club.getMyCafeList', $myargs);
			$myCafes = $output->data;
			if($myCafes){
				foreach($myCafes as $key => $val){
						$my_cafes[] = $val->site_srl;
				}
			}
		}
		Context::set('my_cafes', $my_cafes);
	}

	function dispClubMyCafe() {
		$logged_info = Context::get('logged_info');
		
		if($logged_info->member_srl) {
			$myargs->member_srl = $logged_info->member_srl;
			$myargs->page = Context::get('page');
			$output = executeQueryArray('club.getMyCafeList', $myargs);

			if($output->data && count($output->data)) {
                foreach($output->data as $key => $val) {
                    $banner_src = 'files/attach/cafe_banner/'.$val->site_srl.'.jpg';
                    if(file_exists(_XE_PATH_.$banner_src)) $output->data[$key]->cafe_banner = $banner_src.'?rnd='.filemtime(_XE_PATH_.$banner_src);
                    else $output->data[$key]->cafe_banner = '';
                }
            }
			$my_cafes = $output->data;
			if($my_cafes){
				foreach($my_cafes as $key => $val){
					// get cafe site members count 
					$myargs->site_srl = $val->site_srl;
					$output = executeQuery('club.getSiteMemberCount', $myargs);
					$member_count = $output->data;
					$my_cafes[$key]->memberCount = $member_count->member_count;
				}
			}
			Context::set('my_cafes', $my_cafes);
			Context::set('mycafe_page_navigation',$output->page_navigation);
		}
		$this->setTemplateFile('myCafeList');
	}

	function dispClubNewestDoc() {
		$oDocumentModel = &getModel('document');
		$args->list_count = 10;
		$output = executeQueryArray('club.getNewestDocuments', $args);
		if($output->data) {
			foreach($output->data as $key => $attribute) {
				$document_srl = $attribute->document_srl;
				if(!$GLOBALS['XE_DOCUMENT_LIST'][$document_srl]) {
					unset($oDocument);
					$oDocument = new documentItem();
					$oDocument->setAttribute($attribute, false);
					$GLOBALS['XE_DOCUMENT_LIST'][$document_srl] = $oDocument;
				}
				$output->data[$key] = $GLOBALS['XE_DOCUMENT_LIST'][$document_srl];
			}
		}
		Context::set('newest_documents', $output->data);
		$this->setTemplateFile('allNewestDocs');
	}

	function dispMobileCafeSite() {
		$vars = Context::getRequestVars();
		$args->site_srl = $vars->site_srl;
		$args->list_count = 10;
		
		$output = executeQueryArray('club.getSiteNewestDocuments', $args);
		$siteDocuments = $output->data;
		Context::set('siteDocuments',$siteDocuments);
		$this->setTemplateFile('siteNewestDocs');
	}

	function dispSiteCafeItems() {
		$vars = Context::getRequestVars();
		$oClubModel = &getModel('club');

		$args->site_srl = $vars->site_srl;
		$siteInfo = $oClubModel->getClubInfo($args->site_srl);

		$menu_srl = $siteInfo->first_menu_srl;
		$php_file = sprintf('%sfiles/cache/menu/%d.php', _XE_PATH_, $menu_srl);
		@include($php_file);
		         
		if($args->start_depth == 2 && count($menu->list)) {
			$t_menu = null;
			foreach($menu->list as $key => $val) {
				if($val['selected']) {
					$t_menu->list = $val['list'];
					break;
				}
			}
			$menu = $t_menu;
		}

		$widget_info->menu = $menu->list;
		$this->_arrangeMenu($arranged_list, $menu->list, 0);
		$widget_info->arranged_menu = $arranged_list;

		$widget_info->xml_file = sprintf('%sfiles/cache/menu/%d.xml.php',Context::getRequestUri(), $args->menu_srl);
		$widget_info->menu_srl = $args->menu_srl;

		if($this->selected_node_srl) $widget_info->selected_node_srl = $this->selected_node_srl;
		Context::set('widget_info', $widget_info);

		$this->setTemplateFile('siteCafeItems');

	}

	function _arrangeMenu(&$menu, $list, $depth) {
		if(!count($list)) return;
		$idx = 0;
		$list_order = array();
		foreach($list as $key => $val) {
			if(!$val['text']) continue;
			$obj = null;
			$obj->href = $val['href'];
			$obj->url = $val['url'];
			$obj->node_srl = $val['node_srl'];
			$obj->parent_srl = $val['parent_srl'];
			$obj->title = $obj->text = $val['text'];
			$obj->expand = $val['expand']=='Y'?true:false;
			$obj->depth = $depth;
			$obj->selected = $val['selected'];
			$obj->open_window = $val['open_window'];

			$obj->normal_btn = $val['normal_btn'];
			$obj->hover_btn = $val['hover_btn'];
			$obj->active_btn = $val['active_btn'];
			$obj->child_count = 0;
			$obj->childs = array();

			if(Context::get('mid') == $obj->url){
				$selected = true;
				$this->selected_node_srl = $obj->node_srl;
				$obj->selected = true;
			}else{
				$selected = false;
			}
			$list_order[$idx++] = $obj->node_srl;
			if($obj->parent_srl) {

				$parent_srl = $obj->parent_srl;
				$expand = $obj->expand;
				if($selected) $expand = true;

				while($parent_srl) {
					$menu[$parent_srl]->childs[] = $obj->node_srl;
					$menu[$parent_srl]->child_count = count($menu[$parent_srl]->childs);
					if($expand) $menu[$parent_srl]->expand = $expand;

					$parent_srl = $menu[$parent_srl]->parent_srl;
				}
			}
			$menu[$key] = $obj;

			if(count($val['list'])) $this->_arrangeMenu($menu, $val['list'], $depth+1);
		}
		$menu[$list_order[0]]->first = true;
		$menu[$list_order[count($list_order)-1]]->last = true;
	}

	function dispCafeSiteInfo() {
		$oModuleModel = &getModel('module');
		$oClubModel = &getModel('club');

		$vars = Context::getRequestVars();
		$siteAminInfo = $oModuleModel->getSiteAdmin($vars->site_srl);
		$clubInfo = $oClubModel->getClubInfo($vars->site_srl);
		$myargs->site_srl = $vars->site_srl;

		// get cafe site members count 
		$output = executeQuery('club.getSiteMemberCount', $myargs);
		$member_count = $output->data;

		// get site total docs count 
		$output = executeQuery('club.getSiteTotalDocumentsCount', $myargs);
		$total_docs_count = $output->data;

		// get today's docs
		$myargs->s_regdate = date('Ymd');
		$output = executeQuery('club.getSiteTodayDocumentsCount', $myargs);
		$today_docs_count = $output->data;


		// set vaiables
		$cafeSiteInfo->admin_name = $siteAminInfo[0]->nick_name;
		$cafeSiteInfo->member_count = $member_count->member_count;
		$cafeSiteInfo->regdate = &getTimeGap($clubInfo->regdate);
		$cafeSiteInfo->total_articles_count = $total_docs_count->total_doc_count;
		$cafeSiteInfo->today_articles_count = $today_docs_count->today_doc_count;
		$cafeSiteInfo->domain = $clubInfo->domain;

		$logged_info = Context::get('logged_info');

		if($logged_info){
			$cafeSiteInfo->user_name = $logged_info->nick_name;
			$myargs->member_srl = $logged_info->member_srl;
			$output = executeQuery('club.getSiteMemberDocumentsCount', $myargs);
			$member_docs_count = $output->data;
			$cafeSiteInfo->member_articles_count = $member_docs_count->member_doc_count;

			$output = executeQuery('club.getSiteMemberCommentsCount', $myargs);
			$member_comments_count = $output->data;
			$cafeSiteInfo->member_comments_count = $member_comments_count->member_comment_count;
	
			$output = executeQuery('club.getSiteMemberGroupMemberInfo', $myargs);
			$memberInfo = $output->data;
			$cafeSiteInfo->member_join_date = zdate($memberInfo->regdate,'Y.m.d');
		}

		Context::set('cafeSiteInfo', $cafeSiteInfo);
		
		$this->setTemplateFile('siteCafeInfo');
	}

	function dispClubDocs() {
		$oDocumentModel = &getModel('document');
		$vars = Context::getRequestVars();
		$args->search_keyword = $vars->search_keyword;
		$output = executeQueryArray('club.getClubDocuments', $args);
		if($output->data) {
			foreach($output->data as $key => $attribute) {
				$document_srl = $attribute->document_srl;
				if(!$GLOBALS['XE_DOCUMENT_LIST'][$document_srl]) {
					unset($oDocument);
					$oDocument = new documentItem();
					$oDocument->setAttribute($attribute, false);
					$GLOBALS['XE_DOCUMENT_LIST'][$document_srl] = $oDocument;
				}
				$output->data[$key] = $GLOBALS['XE_DOCUMENT_LIST'][$document_srl];
			}
		}
		Context::set('newest_documents', $output->data);
		$this->setTemplateFile('allNewestDocs');
	}

}


?>
