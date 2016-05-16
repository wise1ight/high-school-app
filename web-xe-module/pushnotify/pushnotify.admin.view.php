<?php
class pushnotifyAdminView extends pushnotify
{
	function init()
	{
		$this->setTemplatePath($this->module_path.'tpl');
		$this->setTemplateFile(str_replace('dispPushnotifyAdmin', '', $this->act));
	}

	function dispPushnotifyAdminConfig()
	{
		$oModuleModel = &getModel('module');
		$oPushnotifyModel = &getModel('pushnotify');

		$config = $oPushnotifyModel->getConfig();
		Context::set('config', $config);

		$mid_list = $oModuleModel->getMidList(null, array('module_srl', 'mid', 'browser_title', 'module'));

		Context::set('mid_list', $mid_list);
	}
	
	function dispPushnotifyAdminAlert()
	{
		$oPushnotifyAdminModel = getAdminModel('pushnotify');
		
		$this->setTemplateFile('alert');
	}

	function dispPushnotifyAdminList()
	{
		$oPushnotifyAdminModel = getAdminModel('pushnotify');

		$output = $oPushnotifyAdminModel->getAdminNotifyList();

		Context::set('total_count', $output->page_navigation->total_count);
		Context::set('total_page', $output->page_navigation->total_page);
		Context::set('page', $output->page);
		Context::set('pushnotify_list', $output->data);
		Context::set('page_navigation', $output->page_navigation);

		$this->setTemplateFile('ncenter_list');
	}

}
