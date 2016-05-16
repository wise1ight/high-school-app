<?php
class pushnotify extends ModuleObject
{
	// @@@@@@@@@@ 사용자 커스텀 시작
	// 쪽지를 열 mid 지정
	// 쪽지를 열 때 해당 mid에서 열리도록 합니다
	// 비워두면 접속한 페이지에서 열림(기본 동작)
	var $message_mid = '';

	// 노티바(알림바)를 감출 mid - array('mid1', 'mid2', 'mid3')
	// 지정한 mid에서는 노티바를 출력하지 않습니다
	var $disable_notify_bar_mid = array();

	// 노티바(알림바)를 감출 act - array('act1', 'act2', 'act3')
	// 지정한 act에서는 노티바를 출력하지 않습니다
	var $disable_notify_bar_act = array();

	// 알림을 보내지 않을 게시판 mid - array('mid1', 'mid2', 'mid3')
	// 지정한 mid에서는 댓글 알림을 보내지 않습니다
	var $disable_notify = array();
	// @@@@@@@@@@ 사용자 커스텀 끝


	var $_TYPE_DOCUMENT = 'D'; // 게시글
	var $_TYPE_COMMENT = 'C'; // 댓글의 댓글
	var $_TYPE_ADMIN_COMMENT = 'A'; // 어드민 댓글 알림
	var $_TYPE_MENTION = 'M'; // 멘션
	var $_TYPE_MESSAGE = 'E'; // 쪽지 mEssage
	var $_TYPE_DOCUMENTS = 'P'; // 글 작성 알림
	var $_TYPE_VOTED = 'V'; // 추천글 안내 알림
	var $_TYPE_TEST = 'T';

	var $triggers = array(
		array('comment.insertComment', 'pushnotify', 'controller', 'triggerAfterInsertComment', 'after'),
		array('comment.deleteComment', 'pushnotify', 'controller', 'triggerAfterDeleteComment', 'after'),
		array('document.insertDocument', 'pushnotify', 'controller', 'triggerAfterInsertDocument', 'after'),
		array('document.deleteDocument', 'pushnotify', 'controller', 'triggerAfterDeleteDocument', 'after'),
		array('moduleHandler.proc', 'pushnotify', 'controller', 'triggerAfterModuleHandlerProc', 'after'),
		array('moduleObject.proc', 'pushnotify', 'controller', 'triggerBeforeModuleObjectProc', 'before'),
		array('member.deleteMember', 'pushnotify', 'controller', 'triggerAfterDeleteMember', 'after'),
		array('communication.sendMessage', 'pushnotify', 'controller', 'triggerAfterSendMessage', 'after'),
		array('document.updateVotedCount', 'pushnotify', 'controller', 'triggerAfterVotedupdate', 'after'),
		array('moduleHandler.init', 'pushnotify', 'controller', 'triggerAddMemberMenu', 'after'),
		array('member.doLogin', 'pushnotify', 'controller', 'triggerAfterLogin', 'after'),
		array('member.doLogout', 'pushnotify', 'controller', 'triggerBeforeLogout', 'before'),
	);

	function _isDisable()
	{
		$result = FALSE;
		if(count($this->disable_notify))
		{
			$module_info = Context::get('module_info');
			if(in_array($module_info->mid, $this->disable_notify)) $result = TRUE;
		}

		return $result;
	}

	function moduleInstall()
	{
		return new Object();
	}

	function checkUpdate()
	{
		$oModuleModel = &getModel('module');
		$oDB = &DB::getInstance();

		if(!$oDB->isColumnExists("member", "obid"))    return true;
		
		foreach($this->triggers as $trigger)
		{
			if(!$oModuleModel->getTrigger($trigger[0], $trigger[1], $trigger[2], $trigger[3], $trigger[4])) return true;
		}

		if(!$oDB->isColumnExists('pushnotify', 'readed'))
		{
			return true;
		}

		if(!$oDB->isColumnExists('pushnotify', 'target_browser'))
		{
			return true;
		}

		if(!$oDB->isColumnExists('pushnotify', 'target_p_srl'))
		{
			return true;
		}

		return false;
	}

	function moduleUpdate()
	{
		$oModuleModel = &getModel('module');
		$oModuleController = &getController('module');
		$oDB = &DB::getInstance();
		
		if(!$oDB->isColumnExists("member", "obid")) $oDB->addColumn("member", "obid", "varchar","128");

		foreach($this->triggers as $trigger)
		{
			if(!$oModuleModel->getTrigger($trigger[0], $trigger[1], $trigger[2], $trigger[3], $trigger[4]))
			{
				$oModuleController->insertTrigger($trigger[0], $trigger[1], $trigger[2], $trigger[3], $trigger[4]);
			}
		}

		if(!$oDB->isColumnExists('pushnotify','readed'))
		{
			$oDB->addColumn('pushnotify', 'readed', 'char', 1, 'N', true);
			$oDB->addIndex('pushnotify', 'idx_readed', array('readed'));
			$oDB->addIndex('pushnotify', 'idx_member_srl', array('member_srl'));
			$oDB->addIndex('pushnotify', 'idx_regdate', array('regdate'));
		}

		if(!$oDB->isColumnExists('pushnotify','target_browser'))
		{
			$oDB->addColumn('pushnotify', 'target_browser', 'varchar', 50, true);
		}

		if(!$oDB->isColumnExists('pushnotify','target_p_srl'))
		{
			$oDB->addColumn('pushnotify', 'target_p_srl', 'number', 10, true);
		}

		return new Object(0, 'success_updated');
	}

	function recompileCache()
	{
		return new Object();
	}

	function moduleUninstall()
	{
		$oModuleController = &getController('module');

		foreach($this->triggers as $trigger)
		{
			$oModuleController->deleteTrigger($trigger[0], $trigger[1], $trigger[2], $trigger[3], $trigger[4]);
		}
		return new Object();
	}
}
