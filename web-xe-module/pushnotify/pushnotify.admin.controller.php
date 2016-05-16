<?php
class pushnotifyAdminController extends pushnotify
{
	function procPushnotifyAdminInsertConfig()
	{
		$oModuleController = &getController('module');

		$config->use = Context::get('use');

		$config->user_config_list = Context::get('user_config_list');
		$config->mention_format = Context::get('mention_format');
		$config->mention_names = Context::get('mention_names');
		$config->document_notify = Context::get('document_notify');
		$config->message_notify = Context::get('message_notify');
		$config->hide_module_srls = Context::get('hide_module_srls');
		if(!$config->mention_format && !is_array($config->mention_format))
		{
			$config->mention_format = array();
		}
		$config->admin_notice_doc_module_srls = Context::get('admin_notice_doc_module_srls');
		$config->admin_comment_module_srls = Context::get('admin_comment_module_srls');

		$config->app_id = Context::get('app_id');
		$config->rest_key = Context::get('rest_key');
		$config->anonymous_name = Context::get('anonymous_name');
		$config->document_read = Context::get('document_read');
		$config->voted_format = Context::get('voted_format');

		if(!$config->document_notify)
		{
			$config->document_notify = 'direct-comment';
		}

		$this->setMessage('success_updated');

		$oModuleController->updateModuleConfig('pushnotify', $config);

		if(!in_array(Context::getRequestMethod(),array('XMLRPC','JSON')))
		{
			$returnUrl = Context::get('success_return_url') ? Context::get('success_return_url') : getNotEncodedUrl('', 'module', 'admin', 'act', 'dispPushnotifyAdminConfig');
			header('location: ' . $returnUrl);
			return;
		}
	}

	/**
	 * @brief 스킨 테스트를 위한 더미 데이터 생성 5개 생성
	 **/
	function procPushnotifyAdminInsertDummyData()
	{
		$oPushnotifyController = &getController('pushnotify');
		$logged_info = Context::get('logged_info');

		for($i = 1; $i <= 5; $i++)
		{
			$args = new stdClass();
			$args->member_srl = $logged_info->member_srl;
			$args->srl = 1;
			$args->target_srl = 1;
			$args->type = $this->_TYPE_TEST;
			$args->target_type = $this->_TYPE_TEST;
			$args->target_url = getUrl('');
			$args->target_summary = '[*] 시험용 알림입니다' . rand();
			$args->target_nick_name = $logged_info->nick_name;
			$args->regdate = date('YmdHis');
			$args->notify = $oPushnotifyController->_getNotifyId($args);
			$output = $oPushnotifyController->_insertNotify($args);
		}
	}

	/**
	 * @brief 모듈 푸시 테스트를 위한 더미 데이터 생성 1개 생성
	 **/
	function procPushnotifyAdminInsertPushData()
	{
		$oPushnotifyController = getController('pushnotify');
		$logged_info = Context::get('logged_info');

		$args = new stdClass();
		$args->member_srl = $logged_info->member_srl;
		$args->srl = 1;
		$args->target_srl = 1;
		$args->type = $this->_TYPE_DOCUMENT;
		$args->target_type = $this->_TYPE_COMMENT;
		$args->target_url = getUrl('');
		$args->target_summary = '[*] 시험용 알림입니다' . rand();
		$args->target_nick_name = $logged_info->nick_name;
		$args->regdate = date('YmdHis');
		$args->notify = $oPushnotifyController->_getNotifyId($args);
		$output = $oPushnotifyController->_insertNotify($args);
	}

	function procPushnotifyAdminDeleteNofity()
	{
		$old_date = Context::get('old_date');
		$args = new stdClass;
		if($old_date)
		{
			$args->old_date = $old_date;
		}
		$output = executeQuery('pushnotify.deleteNotifyAll', $args);
		if(!$output->toBool())
		{
			$oDB->rollback();
			return $output;
		}

		if($old_date)
		{
			$this->setMessage('1달 이전 정보를 삭제하였습니다.');
		}
		else
		{
			$this->setMessage('모든 정보를 삭제하였습니다.');
		}
		
		if(!in_array(Context::getRequestMethod(),array('XMLRPC','JSON')))
		{
			$returnUrl = Context::get('success_return_url') ?  Context::get('success_return_url') : getNotEncodedUrl('', 'module', 'admin', 'act', 'dispPushnotifyAdminList');
			header('location: ' .$returnUrl);
			return;
		}
	}
}
