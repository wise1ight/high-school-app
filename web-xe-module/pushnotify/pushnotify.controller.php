<?php
class pushnotifyController extends pushnotify
{
	function triggerAfterDeleteMember($obj)
	{
		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();

		$member_srl = $obj->member_srl;
		if(!$member_srl) return new Object();

		$args->member_srl = $member_srl;
		executeQuery('pushnotify.deleteNotifyByMemberSrl', $args);

		return new Object();
	}

	function triggerAfterInsertDocument(&$obj)
	{
		$oModuleModel = getModel('module');

		if($this->_isDisable()) return;

		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$content = strip_tags($obj->title . ' ' . $obj->content);

		$mention_targets = $this->_getMentionTarget($content);
		// 여기있던 $mention_targets 관련 주석문은 새글작동에 방해되므로 삭제했음.

		$oDocumentModel = getModel('document');
		$document_srl = $obj->document_srl;
		$oDocument = $oDocumentModel->getDocument($document_srl);
		$module_info = $oModuleModel->getModuleInfoByDocumentSrl($document_srl);

		$member_srl = $oDocument->get('member_srl');

		$oMemberModel = getModel('member');

		$group_list = $oMemberModel->getGroups();

		$is_anonymous = $this->_isAnonymous($this->_TYPE_DOCUMENT, $obj);
		// 맨션 알림일경우 맨션알림 시작.
		if($mention_targets)
		{
			// !TODO 공용 메소드로 분리
			foreach($mention_targets as $mention_member_srl)
			{
				$target_member_config = $oPushnotifyModel->getMemberConfig($mention_member_srl);
				$notify_member_config = $target_member_config->data;

				if($notify_member_config->mention_notify == 'N')
				{
					continue;
				}

				$args = new stdClass();
				$args->member_srl = $mention_member_srl;
				$args->srl = $obj->document_srl;
				$args->target_p_srl = $obj->document_srl;
				$args->target_srl = $obj->document_srl;
				$args->type = $this->_TYPE_DOCUMENT;
				$args->target_type = $this->_TYPE_MENTION;
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $obj->document_srl);
				$args->target_summary = cut_str(strip_tags($obj->title), 200);
				$args->target_nick_name = $obj->nick_name;
				$args->target_email_address = $obj->email_address;
				$args->regdate = date('YmdHis');
				$args->target_browser = $module_info->browser_title;
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args, $is_anonymous);
			}
		} else if (!$mention_targets) {
			if(in_array($module_info->module_srl, $config->admin_notice_doc_module_srls))
			{
				$args = new stdClass();
				$args->member_srl = $member_srl;
				$args->srl = $obj->document_srl;
				$args->target_p_srl = $obj->document_srl;
				$args->target_srl = $obj->document_srl;
				$args->type = $this->_TYPE_DOCUMENT;
				$args->target_type = $this->_TYPE_DOCUMENT;
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $obj->document_srl);
				$args->target_summary = cut_str(strip_tags($obj->title), 200);
				$args->target_nick_name = $obj->nick_name;
				$args->target_email_address = $obj->email_address;
				$args->regdate = date('YmdHis');
				$args->target_browser = $module_info->browser_title;
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args, $is_anonymous);
			}
		}

		return new Object();
	}

	function triggerAfterInsertComment(&$obj)
	{
		if($this->_isDisable()) return;

		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$logged_info = Context::get('logged_info');
		$notify_member_srl = array();

		$document_srl = $obj->document_srl;
		$oModuleModel = getModel('module');
		$module_info = $oModuleModel->getModuleInfoByDocumentSrl($document_srl);
		$comment_srl = $obj->comment_srl;
		$parent_srl = $obj->parent_srl;
		$content = $obj->content;
		$regdate = $obj->regdate;

		// 익명 노티 체크
		$is_anonymous = $this->_isAnonymous($this->_TYPE_COMMENT, $obj);

		// 멘션
		$mention_targets = $this->_getMentionTarget(strip_tags($obj->content));
		// !TODO 공용 메소드로 분리
		foreach($mention_targets as $mention_member_srl)
		{
			$args = new stdClass();
			$args->member_srl = $mention_member_srl;
			$args->target_p_srl = $obj->comment_srl;
			$args->srl = $obj->comment_srl;
			$args->target_srl = $obj->document_srl;
			$args->type = $this->_TYPE_COMMENT;
			$args->target_type = $this->_TYPE_MENTION;
			$args->target_url = getNotEncodedFullUrl('', 'document_srl', $document_srl, '_comment_srl', $comment_srl) . '#comment_'. $comment_srl;
			$args->target_summary = cut_str(strip_tags($content), 200);
			$args->target_nick_name = $obj->nick_name;
			$args->target_email_address = $obj->email_address;
			$args->regdate = date('YmdHis');
			$args->target_browser = $module_info->browser_title;
			$args->notify = $this->_getNotifyId($args);
			$output = $this->_insertNotify($args, $is_anonymous);
			$notify_member_srl[] = $mention_member_srl;
		}

		$admin_list = $oPushnotifyModel->getMemberAdmins();
		$admins_list = $admin_list->data;
		
		foreach($admins_list as $admins)
		{
			if(in_array($module_info->module_srl, $config->admin_comment_module_srls))
			{
				$args = new stdClass();
				$args->member_srl = $admins->member_srl;
				$args->target_p_srl = $obj->comment_srl;
				$args->srl = $obj->comment_srl;
				$args->target_srl = $obj->comment_srl;
				$args->type = $this->_TYPE_COMMENT;
				$args->target_type = $this->_TYPE_ADMIN_COMMENT;
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $document_srl, '_comment_srl', $comment_srl) . '#comment_'. $comment_srl;
				$args->target_summary = cut_str(strip_tags($content), 200);
				$args->target_nick_name = $obj->nick_name;
				$args->target_email_address = $obj->email_address;
				$args->regdate = date('YmdHis');
				$args->target_browser = $module_info->browser_title;
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args, $is_anonymous);
			}
		}
		// 대댓글
		if($parent_srl)
		{
			$oCommentModel = &getModel('comment');
			$oComment = $oCommentModel->getComment($parent_srl);
			$member_srl = $oComment->member_srl;
			// !TODO 공용 메소드로 분리
			if(!in_array(abs($member_srl), $notify_member_srl) && (!$logged_info || ($member_srl != 0 && abs($member_srl) != $logged_info->member_srl)))
			{
				$args = new stdClass();
				$args->member_srl = abs($member_srl);
				$args->srl = $comment_srl;
				$args->target_p_srl = $parent_srl;
				$args->target_srl = $obj->comment_srl;
				$args->type = $this->_TYPE_COMMENT;
				$args->target_type = $this->_TYPE_COMMENT;
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $document_srl, '_comment_srl', $comment_srl) . '#comment_'. $comment_srl;
				$args->target_summary = cut_str(strip_tags($content), 200);
				$args->target_nick_name = $obj->nick_name;
				$args->target_email_address = $obj->email_address;
				$args->regdate = $regdate;
				$args->target_browser = $module_info->browser_title;
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args, $is_anonymous);
				$notify_member_srl[] = abs($member_srl);
			}
		}
		// 대댓글이 아니고, 게시글의 댓글을 남길 경우
		if(!$parent_srl || ($parent_srl && $config->document_notify == 'all-comment'))
		{
			$oDocumentModel = &getModel('document');
			$oDocument = $oDocumentModel->getDocument($document_srl);

			$member_srl = $oDocument->get('member_srl');
			
			// !TODO 공용 메소드로 분리
			if(!in_array(abs($member_srl), $notify_member_srl) && (!$logged_info || ($member_srl != 0 && abs($member_srl) != $logged_info->member_srl)))
			{
				$args = new stdClass();
				$args->member_srl = abs($member_srl);
				$args->srl = $comment_srl;
				$args->target_p_srl = $comment_srl;
				$args->target_srl = $document_srl;
				$args->type = $this->_TYPE_DOCUMENT;
				$args->target_type = $this->_TYPE_COMMENT;
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $document_srl, '_comment_srl', $comment_srl) . '#comment_'. $comment_srl;
				$args->target_summary = cut_str(strip_tags($content), 200);
				$args->target_nick_name = $obj->nick_name;
				$args->target_email_address = $obj->email_address;
				$args->regdate = $regdate;
				$args->target_browser = $module_info->browser_title;
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args, $is_anonymous);
			}
		}

		return new Object();
	}

	function triggerBeforeModuleObjectProc(&$oModule)
	{
		if(version_compare(__XE_VERSION__, '1.7.4', '>='))
		{
			return new Object();
		}
		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$vars = Context::getRequestVars();
		$logged_info = Context::get('logged_info');

		$messages_member_config = $oPushnotifyModel->getMemberConfig($logged_info->member_srl);
		$message_member_config = $messages_member_config->data;

		// 쪽지 체크 및 유저 쪽지 알림 채크
		if($config->message_notify != 'N' && $message_member_config->message_notify != 'N')
		{
			$flag_path = './files/pushnotify/new_message_flags/';

			$need_update = false;
			// 쪽지 알림 메시지 체크
			if(strpos(Context::getHtmlFooter(), 'xeNotifyMessage') !== FALSE)
			{
				$need_update = true;
			}
			// 메시지 플래그 파일 체크
			else if(file_exists($flag_path . $logged_info->member_srl))
			{
				$need_update = true;
			}

			if($oModule->act == 'procCommunicationSendMessage')
			{
				FileHandler::makeDir($flag_path);
				$flag_file = sprintf('%s%s', $flag_path, $vars->receiver_srl);
				FileHandler::writeFile($flag_file, $vars->receiver_srl);
			}
			else if($need_update)
			{
				$oMemberModel = &getModel('member');
				$_sender_member_srl = trim(FileHandler::readFile($flag_path . $logged_info->member_srl));
				$sender_member_info = $oMemberModel->getMemberInfoByMemberSrl($_sender_member_srl);
				FileHandler::removeFile($flag_path . $logged_info->member_srl);

				// 새 쪽지 수
				$args->receiver_srl = $logged_info->member_srl;
				$output = executeQuery('pushnotify.getCountNewMessage', $args);
				$message_count = $output->data->count;

				// 기존 쪽지 알림을 읽은 것으로 변경
				$cond = new stdClass();
				$cond->type = $this->_TYPE_MESSAGE;
				$cond->member_srl = $logged_info->member_srl;
				$output = executeQuery('pushnotify.updateNotifyReadedByType', $cond);

				if(!$message_count) return;

				// 알림 추가
				$args = new stdClass();
				$args->member_srl = $logged_info->member_srl;
				$args->srl = $sender_member_info->member_srl;
				if(!$args->srl) $args->srl = 0;
				$args->target_p_srl = 1;
				$args->target_srl = $sender_member_info->member_srl;
				if(!$args->srl) $args->target_srl = 0;
				$args->type = $this->_TYPE_MESSAGE;
				$args->target_type = $this->_TYPE_MESSAGE;
				$args->target_url_params = $target_url_params;
				$args->target_summary = $message_count;
				$args->target_nick_name = $sender_member_info->nick_name;
				$args->target_member_srl = $sender_member_info->member_srl;
				$args->regdate = date('YmdHis');
				$args->notify = $this->_getNotifyId($args);
				$args->target_url = getNotEncodedFullUrl('', 'act', 'dispCommunicationMessages');

				$output = $this->_insertNotify($args, $is_anonymous);
			}
		}
	}

	function triggerAfterSendMessage(&$trigger_obj)
	{
		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();
		if($config->message_notify != 'N')
		{
			$messages_member_config = $oPushnotifyModel->getMemberConfig($trigger_obj->receiver_srl);
			$message_member_config = $messages_member_config->data;

			if(version_compare(__XE_VERSION__, '1.8', '>=') && $message_member_config->message_notify != 'N')
			{
				$args = new stdClass();
				$args->member_srl = $trigger_obj->receiver_srl;
				$args->srl = $trigger_obj->related_srl;
				$args->target_p_srl = '1';
				$args->target_srl = $trigger_obj->message_srl;
				$args->type = $this->_TYPE_MESSAGE;
				$args->target_type = $this->_TYPE_MESSAGE;
				$args->target_summary = $trigger_obj->title;
				$args->regdate = date('YmdHis');
				$args->notify = $this->_getNotifyId($args);
				$args->target_url = getNotEncodedFullUrl('', 'act', 'dispCommunicationMessages', 'message_srl', $trigger_obj->related_srl);
				$output = $this->_insertNotify($args, $is_anonymous);
			}
			elseif($message_member_config->message_notify != 'N')
			{
				$args = new stdClass();
				$args->member_srl = $trigger_obj->receiver_srl;
				$args->srl = $trigger_obj->receiver_srl;
				$args->target_p_srl = '1';
				$args->target_srl = $trigger_obj->sender_srl;
				$args->type = $this->_TYPE_MESSAGE;
				$args->target_type = $this->_TYPE_MESSAGE;
				$args->target_summary = $trigger_obj->title;
				$args->regdate = date('YmdHis');
				$args->notify = $this->_getNotifyId($args);
				$args->target_url = getNotEncodedFullUrl('', 'act', 'dispCommunicationMessages');
				$output = $this->_insertNotify($args, $is_anonymous);
			}
		}
	}

	function triggerAfterVotedupdate(&$obj)
	{
		$oDocumentModel = getModel('document');
		$oDocument = $oDocumentModel->getDocument($obj->document_srl, false, false);

		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();
		if($config->voted_format != 'Y') return new Object();

		$args = new stdClass();
		$args->member_srl = $obj->member_srl;
		$args->srl = $obj->document_srl;
		$args->target_p_srl = '1';
		$args->target_srl = $obj->document_srl;
		$args->type = $this->_TYPE_DOCUMENT;
		$args->target_type = $this->_TYPE_VOTED;
		$args->target_summary = $oDocument->get('title');
		$args->regdate = date('YmdHis');
		$args->notify = $this->_getNotifyId($args);
		$args->target_url = getNotEncodedFullUrl('', 'document_srl', $obj->document_srl);
		$output = $this->_insertNotify($args, $is_anonymous);

	}

	function triggerAfterDeleteComment(&$obj)
	{
		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$args->srl = $obj->comment_srl;
		$output = executeQuery('pushnotify.deleteNotifyBySrl', $args);
		return new Object();
	}

	function triggerAfterDeleteDocument(&$obj)
	{
		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$args->srl = $obj->document_srl;
		$output = executeQuery('pushnotify.deleteNotifyBySrl', $args);
		return new Object();
	}

	function triggerAfterModuleHandlerProc(&$oModule)
	{
		if(Context::get('obid')){
			$_SESSION["obid"] = Context::get('obid');
		}

		$vars = Context::getRequestVars();
		$logged_info = Context::get('logged_info');
		$args = new stdClass();

		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if($config->use != 'Y') return new Object();

		$this->_hide_pushnotify = false;
		if($oModule->module == 'beluxe' && Context::get('is_modal'))
		{
			$this->_hide_pushnotify = true;
		}
		if($oModule->module == 'bodex' && Context::get('is_iframe'))
		{
			$this->_hide_pushnotify = true;
		}
		
		if($oModule->act == 'dispBoardReplyComment')
		{
			$comment_srl = Context::get('comment_srl');
			$logged_info = Context::get('logged_info');
			if($comment_srl && $logged_info)
			{
				$args->target_srl = $comment_srl;
				$args->member_srl = $logged_info->member_srl;
				$output_update = executeQuery('pushnotify.updateNotifyReadedByTargetSrl', $args);
			}
		}
		else if($oModule->act == 'dispBoardContent')
		{
			$comment_srl = Context::get('_comment_srl');
			$document_srl = Context::get('document_srl');
			$oDocument = Context::get('oDocument');
			$logged_info = Context::get('logged_info');

			if($document_srl && $logged_info && $config->document_read=='Y')
			{
				$args->target_srl = $document_srl;
				$args->member_srl = $logged_info->member_srl;
				$outputs = executeQuery('pushnotify.updateNotifyReadedByTargetSrl', $args);
			}

			if($comment_srl && $document_srl && $oDocument)
			{
				$_comment_list = $oDocument->getComments();
				if($_comment_list)
				{
					if(array_key_exists($comment_srl, $_comment_list))
					{
						$url = getNotEncodedUrl('_comment_srl','') . '#comment_' . $comment_srl;
						$need_check_socialxe = true;
					}
					else
					{
						$cpage = $oDocument->comment_page_navigation->cur_page;
						if($cpage > 1)
						{
							$url = getNotEncodedUrl('cpage', $cpage-1) . '#comment_' . $comment_srl;
							$need_check_socialxe = true;
						}
						else
						{
							$url = getNotEncodedUrl('_comment_srl', '', 'cpage', '') . '#comment_' . $comment_srl;
						}
					}

					if($need_check_socialxe)
					{
						$oDB = &DB::getInstance();
						if($oDB->isTableExists('socialxe'))
						{
							unset($args);
							$oModuleModel = &getModel('module');
							$module_info = $oModuleModel->getModuleInfoByDocumentSrl($document_srl);
							$args->module_srl = $module_info->module_srl;
							$output = executeQuery('pushnotify.getSocialxeCount', $args);
							if($output->data->cnt)
							{
								$socialxe_comment_srl = $comment_srl;

								unset($args);
								$args->comment_srl = $comment_srl;
								$oCommentModel = &getModel('comment');
								$oComment = $oCommentModel->getComment($comment_srl);
								$parent_srl = $oComment->get('parent_srl');
								if($parent_srl)
								{
									$socialxe_comment_srl = $parent_srl;
								}

								$url = getNotEncodedUrl('_comment_srl', '', 'cpage', '', 'comment_srl', $socialxe_comment_srl) . '#comment_' . $comment_srl;
							}
						}
					}

					$url = str_replace('&amp;','&',$url);
					header('location: ' . $url);
					Context::close();
					exit;
				}
			}
		}
		elseif($oModule->act == 'dispCommunicationMessages')
		{
			$message_srl = Context::get('message_srl');
			$logged_info = Context::get('logged_info');
			if($message_srl)
			{
				$args = new stdClass();
				$args->target_srl = $message_srl;
				$args->member_srl = $logged_info->member_srl;
				executeQuery('pushnotify.updateNotifyReadedByTargetSrl', $args);
			}
		}

		// 지식인 모듈의 의견
		// TODO: 코드 분리
		if($oModule->act == 'procKinInsertComment')
		{
			// 글, 댓글 구분
			$parent_type = ($vars->document_srl == $vars->parent_srl) ? 'DOCUMENT' : 'COMMENT';
			if($parent_type == 'DOCUMENT')
			{
				$oDocumentModel = &getModel('document');
				$oDocument = $oDocumentModel->getDocument($vars->document_srl);
				$member_srl = $oDocument->get('member_srl');
				$type = $this->_TYPE_DOCUMENT;
			}
			else
			{
				$oCommentModel = &getModel('comment');
				$oComment = $oCommentModel->getComment($vars->parent_srl);
				$member_srl = $oComment->get('member_srl');
				$type = $this->_TYPE_COMMENT;
			}

			if($logged_info->member_srl != $member_srl)
			{
				$args = new stdClass();
				$args->member_srl = abs($member_srl);
				$args->srl = ($parent_type == 'DOCUMENT') ? $vars->document_srl : $vars->parent_srl;
				$args->type = $type;
				$args->target_type = $this->_TYPE_COMMENT;
				$args->target_srl = $vars->parent_srl;
				$args->target_p_srl = '1';
				$args->target_url = getNotEncodedFullUrl('', 'document_srl', $vars->document_srl, '_comment_srl', $vars->parent_srl) . '#comment_'. $vars->parent_srl;
				$args->target_summary = cut_str(strip_tags($vars->content), 200);
				$args->target_nick_name = $logged_info->nick_name;
				$args->target_email_address = $logged_info->email_address;
				$args->regdate = date('YmdHis');
				$args->notify = $this->_getNotifyId($args);
				$output = $this->_insertNotify($args);
			}
		}
		else if($oModule->act == 'dispKinView' || $oModule->act == 'dispKinIndex')
		{
			// 글을 볼 때 알림 제거
			$oDocumentModel = &getModel('document');
			$oDocument = $oDocumentModel->getDocument($vars->document_srl);
			$member_srl = $oDocument->get('member_srl');

			if($logged_info->member_srl == $member_srl)
			{
				$args = new stdClass;
				$args->member_srl = $logged_info->member_srl;
				$args->srl = $vars->document_srl;
				$args->type = $this->_TYPE_DOCUMENT;
				$output = executeQuery('pushnotify.updateNotifyReadedBySrl', $args);
			}
		}
		else if($oModule->act == 'getKinComments')
		{
			// 의견을 펼칠 때 알림 제거
			$args = new stdClass;
			$args->member_srl = $logged_info->member_srl;
			$args->target_srl = $vars->parent_srl;
			$output = executeQuery('pushnotify.updateNotifyReadedByTargetSrl', $args);
		}

		return new Object();
	}

	function _addFile()
	{
		$oModuleModel = &getModel('module');
		$module_info = $oModuleModel->getModuleInfoXml('pushnotify');
		if(file_exists(FileHandler::getRealPath($this->template_path . 'pushnotify.css')))
		{
			Context::addCssFile($this->template_path . 'pushnotify.css', true, 'all', '', 100);
		}

		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		if(!Mobile::isFromMobilePhone())
		{
			if($config->colorset && file_exists(FileHandler::getRealPath($this->template_path . 'pushnotify.' . $config->colorset . '.css')))
			{
				Context::addCssFile($this->template_path . 'pushnotify.'.$config->colorset.'.css', true, 'all', '', 100);
			}
		}
		elseif(Mobile::isFromMobilePhone())
		{
			if($config->mcolorset && file_exists(FileHandler::getRealPath($this->template_path . 'pushnotify.' . $config->mcolorset . '.css')))
			{
				Context::addCssFile($this->template_path . 'pushnotify.'.$config->mcolorset.'.css', true, 'all', '', 100);
			}

			Context::loadFile(array('./common/js/jquery.min.js', 'head', '', -100000), true);
			Context::loadFile(array('./common/js/xe.min.js', 'head', '', -100000), true);
			Context::addCssFile($this->template_path . 'pushnotify.mobile.css', true, 'all', '', 100);
		}
		if($config->zindex)
		{
			Context::set('pushnotify_zindex', ' style="z-index:' . $config->zindex . ';" ');
		}
	}

	function updateNotifyRead($notify, $member_srl)
	{
		$args->member_srl = $member_srl;
		$args->notify = $notify;
		$output = executeQuery('pushnotify.updateNotifyReaded', $args);
		//$output = executeQuery('pushnotify.deleteNotify', $args);

		return $output;
	}

	function updateNotifyReadiByTargetSrl($target_srl, $member_srl)
	{
		$args->member_srl = $member_srl;
		$args->target_srl = $target_srl;
		$output = executeQuery('pushnotify.updateNotifyReadedByTargetSrl', $args);
		//$output = executeQuery('pushnotify.deleteNotifyByTargetSrl', $args);

		return $output;
	}

	function updateNotifyReadAll($member_srl)
	{
		$args->member_srl = $member_srl;
		$output = executeQuery('pushnotify.updateNotifyReadedAll', $args);
		//$output = executeQuery('pushnotify.deleteNotifyByMemberSrl', $args);

		return $ouptut;
	}

	function procPushnotifyNotifyRead()
	{
		$logged_info = Context::get('logged_info');
		$target_srl = Context::get('target_srl');
		if(!$logged_info || !$target_srl) return new Object(-1, 'msg_invalid_request');

		$output = $this->updateNotifyRead($notify, $logged_info->member_srl);
		return $output;
	}

	function procPushnotifyNotifyReadAll()
	{
		$logged_info = Context::get('logged_info');
		if(!$logged_info) return new Object(-1, 'msg_invalid_request');

		$output = $this->updateNotifyReadAll($logged_info->member_srl);
		return $output;
	}

	function procPushnotifyRedirect()
	{
		$logged_info = Context::get('logged_info');
		$url = Context::get('url');
		$notify = Context::get('notify');
		if(!$logged_info || !$url || !$notify) return new Object(-1, 'msg_invalid_request');

		$output = $this->updateNotifyRead($notify, $logged_info->member_srl);
		if(!$output->toBool()) return $output;

		$url = str_replace('&amp;', '&', $url);
		header('location: ' . $url);
		Context::close();
		exit;
	}

	/**
	 * @brief 익명으로 노티해야 할지 체크하여 반환
	 * @return boolean
	 **/
	function _isAnonymous($source_type, $triggerObj)
	{
		// 회원번호가 음수
		if($triggerObj->member_srl < 0) return TRUE;

		$module_info = Context::get('module_info');

		// DX 익명 체크박스
		if($module_info->module == 'beluxe' && $triggerObj->anonymous == 'Y') return TRUE;

		if($source_type == $this->_TYPE_COMMENT)
		{
			// DX 익명 강제
			if($module_info->module == 'beluxe' && $module_info->use_anonymous == 'Y') return TRUE;
		}

		if($source_type == $this->_TYPE_DOCUMENT)
		{
			// DX 익명 강제
			if($module_info->module == 'beluxe' && $module_info->use_anonymous == 'Y') return TRUE;
		}

		return FALSE;
	}

	function _insertNotify($args, $anonymous = FALSE)
	{
		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		// 비회원 노티 제거
		if($args->member_srl <= 0) return new Object();

		$logged_info = Context::get('logged_info');

		if($anonymous == TRUE)
		{
			// 설정에서 익명 이름이 설정되어 있으면 익명 이름을 설정함. 없을 경우 Anonymous 를 사용한다.
			if(!$config->anonymous_name)
			{
				$anonymous_name = 'Anonymous';
			}
			else
			{
				$anonymous_name = $config->anonymous_name;
			}
			// 익명 노티 시 회원정보 제거
			$args->target_member_srl = 0;
			$args->target_nick_name = $anonymous_name;
			$args->target_user_id = $anonymous_name;
			$args->target_email_address = $anonymous_name;
			
			$oMemberModel = &getModel('member');
			$push_sender_info = $oMemberModel->getMemberInfoByMemberSrl($args->member_srl);
			$oPushnotifyModel->getPushMessage($args,$push_sender_info->obid);
		}
		else if($logged_info)
		{
			// 익명 노티가 아닐 때 로그인 세션의 회원정보 넣기
			$args->target_member_srl = $logged_info->member_srl;
			$args->target_nick_name = $logged_info->nick_name;
			$args->target_user_id = $logged_info->user_id;
			$args->target_email_address = $logged_info->email_address;
			
			$oMemberModel = &getModel('member');
			$push_sender_info = $oMemberModel->getMemberInfoByMemberSrl($args->member_srl);
			$oPushnotifyModel->getPushMessage($args,$push_sender_info->obid);
		}
		else
		{
			// 비회원
			$args->target_member_srl = 0;
			$args->target_user_id = '';
		}

		$output = executeQuery('pushnotify.insertNotify', $args);
		if(!$output->toBool())
		{
			return $output;
		}

		if($output->toBool())
		{
			$trigger_notify = ModuleHandler::triggerCall('pushnotify._insertNotify', 'after', $args);
			if(!$trigger_notify->toBool())
			{
				return $trigger_notify;
			}	
		}

		return $output;
	}

	/**
	 * @brief 노티 ID 반환
	 **/
	function _getNotifyId($args)
	{
		return md5(uniqid('') . $args->member_srl . $args->srl . $args->target_srl . $args->type . $args->target_type);
	}

	/**
	 * @brief 멘션 대상 member_srl 목록 반환
	 * @return array
	 **/
	function _getMentionTarget($content)
	{
		$oPushnotifyModel = &getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		$logged_info = Context::get('logged_info');

		$list = array();

		$content = strip_tags($content);
		$content = str_replace('&nbsp;', ' ', $content);

		// 정규표현식 정리
		$split = array();
		if(in_array('comma', $config->mention_format)) $split[] = ',';
		$regx = join('', array('/(^|\s)@([^@\s', join('', $split), ']+)/i'));

		preg_match_all($regx, $content, $matches);

		// '님'문자 이후 제거
		if(in_array('respect', $config->mention_format))
		{
			foreach($matches[2] as $idx => $item)
			{
				$pos = strpos($item, '님');
				if($pos !== false && $pos > 0)
				{
					$matches[2][$idx] = trim(substr($item, 0, $pos));
					if($logged_info && $logged_info->nick_name == $matches[2][$idx]) unset($matches[2][$idx]);
				}
			}
		}

		$nicks = array_unique($matches[2]);

		$oMemberModel = getModel('member');
		$member_config = $oMemberModel->getMemberConfig();

		if($config->mention_names == 'id' && $member_config->identifier != 'email_address')
		{
			foreach($nicks as $user_id)
			{
				$vars = null;
				$vars->user_id = $user_id;
				$output = executeQuery('pushnotify.getMemberSrlById', $vars);
				if($output->data && $output->data->member_srl) $list[] = $output->data->member_srl;
			}
		}
		else
		{
			foreach($nicks as $nick_name)
			{
				$vars = null;
				$vars->nick_name = $nick_name;
				$output = executeQuery('pushnotify.getMemberSrlByNickName', $vars);
				if($output->data && $output->data->member_srl) $list[] = $output->data->member_srl;
			}
		}

		return $list;
	}
	
	function triggerAfterLogin(&$obj)
	{
		if(Context::get('obid')){
			$_SESSION["obid"] = Context::get('obid');
		}
		
		$member_srl = $obj->member_srl;
		if(!$member_srl) return new Object();

		if(!$_SESSION["obid"]) return new Object();
   
		$oDB = &DB::getInstance();
		$query = $oDB->_query("UPDATE xe_member set `obid` = '".$_SESSION["obid"]."' where `member_srl` = ".$member_srl);
   
		if($query){
			unset($_SESSION["obid"]);
			return new Object();
		}else{
			return new Object(-1,"단말기 정보를 가져오는데에 실패했습니다.");
		}
	}
	
	function triggerBeforeLogout(&$obj)
	{
		if(Context::get('obid')){
			$_SESSION["obid"] = Context::get('obid');
		}
		
		$member_srl = $obj->member_srl;
		if(!$member_srl) return new Object();

		if(!$_SESSION["obid"]) return new Object();
   
		$oDB = &DB::getInstance();
		$query = $oDB->_query("UPDATE xe_member set `obid` = NULL where `member_srl` = ".$member_srl);

		unset($_SESSION["obid"]);
		return new Object();
	}

}
