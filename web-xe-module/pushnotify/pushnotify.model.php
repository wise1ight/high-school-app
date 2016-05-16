<?php
class pushnotifyModel extends pushnotify
{
	var $config;

	function getConfig()
	{
		if(!$this->config)
		{
			$oModuleModel = &getModel('module');
			$config = $oModuleModel->getModuleConfig('pushnotify');
			if(!$config->use) $config->use = 'Y';
			if(!$config->display_use) $config->display_use = 'Y';

			if(!$config->mention_names) $config->mention_names = 'nick_name';
			if(!$config->message_notify) $config->message_notify = 'Y';
			if(!$config->mention_format && !is_array($config->mention_format)) $config->mention_format = array('respect');
			if(!is_array($config->mention_format)) $config->mention_format = explode('|@|', $config->mention_format);
			if(!$config->document_notify) $config->document_notify = 'direct-comment';
			if(!$config->document_read) $config->document_read = 'N';
			if(!$config->voted_format) $config->voted_format = 'N';

			$this->config = $config;
		}

		return $this->config;
	}

	function _getMyNotifyList($member_srl=null, $page=1, $readed='N')
	{
		if(!$member_srl)
		{
			$logged_info = Context::get('logged_info');
			if(!$logged_info) return array();

			$member_srl = $logged_info->member_srl;
		}

		$args = new stdClass();
		$args->member_srl = $member_srl;
		$args->page = $page ? $page : 1;
		if($readed) $args->readed = $readed;
		$output = executeQueryArray('pushnotify.getNotifyList', $args);
		if(!$output->data) $output->data = array();

		return $output;
	}

	function getMyDispNotifyList($member_srl)
	{

		$logged_info = Context::get('logged_info');

		$member_srl = $logged_info->member_srl;

		$args = new stdClass();
		$args->page = Context::get('page');
		$args->list_count = '20';
		$args->page_count = '10';
		$args->member_srl = $member_srl;
		$output = executeQueryArray('pushnotify.getDispNotifyList', $args);
		if(!$output->data) $output->data = array();

		return $output;
	}

	function getPushnotifyAdminList($member_srl)
	{
		$logged_info = Context::get('logged_info');

		$member_srl = $logged_info->member_srl;

		$args = new stdClass();
		$args->page = Context::get('page');
		$args->list_count = '20';
		$args->page_count = '10';
		$output = executeQueryArray('pushnotify.getAdminNotifyList', $args);
		if(!$output->data) $output->data = array();

		return $output;
	}

	function getMemberAdmins()
	{
		$args->is_admin = 'Y';
		$output = executeQueryArray('pushnotify.getMemberAdmins', $args);
		if(!$output->data) $output->data = array();

		return $output;
	}

	function _getNewCount($member_srl=null)
	{
		if(!$member_srl)
		{
			$logged_info = Context::get('logged_info');
			if(!$logged_info) return 0;

			$member_srl = $logged_info->member_srl;
		}

		$args->member_srl = $member_srl;
		$output = executeQuery('pushnotify.getNotifyNewCount', $args);
		if(!$output->data) return 0;
		return $output->data->cnt;
	}

	/**
	 * @brief 주어진 시간이 얼마 전 인지 반환
	 * @param string YmdHis
	 * @return string
	 **/
	function getAgo($datetime)
	{
		global $lang;
		$lang_type = Context::getLangType();

		$display = $lang->pushnotify_date; // array('Year', 'Month', 'Day', 'Hour', 'Minute', 'Second')

		$ago = $lang->pushnotify_ago; // 'Ago'

		$date = getdate(strtotime(zdate($datetime, 'Y-m-d H:i:s')));

		$current = getdate();
		$p = array('year', 'mon', 'mday', 'hours', 'minutes', 'seconds');
		$factor = array(0, 12, 30, 24, 60, 60);

		for($i = 0; $i < 6; $i++)
		{
			if($i > 0)
			{
				$current[$p[$i]] += $current[$p[$i - 1]] * $factor[$i];
				$date[$p[$i]] += $date[$p[$i - 1]] * $factor[$i];
			}

			if($current[$p[$i]] - $date[$p[$i]] > 1)
			{
				$value = $current[$p[$i]] - $date[$p[$i]];
				if($lang_type == 'en')
				{
					return $value . ' ' . $display[$i] . (($value != 1) ? 's' : '') . ' ' . $ago;
				}
				return $value . $display[$i] . ' ' . $ago;
			}
		}

		return zdate($datetime, 'Y-m-d');
	}
	
	function getPushMessage($v,$obid) {	
		global $lang;
	
		switch($v->type)
		{
			case 'D':
			    $type = "글";
			break;
			case 'C':
			    $type = "댓글";
			break;
			case 'E':
			    $type = "쪽지";
			break;
		}
 
		switch($v->target_type)
		{
			case 'D':
				$str = sprintf($lang->pushnotify_documented, $v->target_browser, $v->target_summary);
			break;
			case 'C':
			    $str = sprintf($lang->pushnotify_commented, $v->target_nick_name, $type, $v->target_summary);
			break;
			case 'M':
				$str = sprintf($lang->pushnotify_mentioned, $v->target_nick_name,  $v->target_summary, $type);
		    break;
		    case 'E':
			    $str = sprintf($lang->pushnotify_message_string, $v->target_summary);
		    break;
		}
  
		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();
		$url = 'https://api.parse.com/1/push';
	    $appId = $config->app_id;
		$restKey = $config->rest_key;
 
	    $target_device = $obid;
		
		if ($v->target_type == "D")
		{
			$push_payload = json_encode(array(
			    "where" => array(
				        "deviceType" => "android",
				),
				"data" => array(
					    "alert" => $str,
						"url" => $v->target_url
	            )
			));
		} else {
			$push_payload = json_encode(array(
					"where" => array(
							"objectId" => $target_device,
					),
					"data" => array(
							"alert" => $str,
							"url" => $v->target_url
					)
			));
		}
 
	    $rest = curl_init();
		curl_setopt($rest,CURLOPT_URL,$url);
	    curl_setopt($rest, CURLOPT_RETURNTRANSFER, 1);
		curl_setopt($rest, CURLOPT_SSL_VERIFYPEER, 0);
	    curl_setopt($rest,CURLOPT_PORT,443);
		curl_setopt($rest,CURLOPT_POST,1);
	    curl_setopt($rest,CURLOPT_POSTFIELDS,$push_payload);
		curl_setopt($rest,CURLOPT_HTTPHEADER,
			    array("X-Parse-Application-Id: " . $appId,
				        "X-Parse-REST-API-Key: " . $restKey,
					    "Content-Type: application/json"));
	    curl_exec($rest);
	}
}
