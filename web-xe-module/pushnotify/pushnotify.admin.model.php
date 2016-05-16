<?php

class pushnotifyAdminModel extends pushnotify
{

	function getAdminNotifyList($member_srl=null, $page=1, $readed='N')
	{
		$oPushnotifyModel = getModel('pushnotify');
		$config = $oPushnotifyModel->getConfig();

		global $lang;

		$act = Context::get('act');
		$output = $oPushnotifyModel->getPushnotifyAdminList();

		$oMemberModel = getModel('member');
		$list = $output->data;

		foreach($list as $k => $v)
		{
			$target_member = $v->target_nick_name;

			switch($v->type)
			{
				case 'D':
					$type = $lang->pushnotify_document; //$type = '글';
				break;
				case 'C':
					$type = $lang->pushnotify_comment; //$type = '댓글';
				break;
				case 'E':
					$type = $lang->pushnotify_type_message; //$type = '쪽지';
				break;
			}

			switch($v->target_type)
			{
				case 'D':
					$str = sprintf($lang->pushnotify_documented, $v->target_browser, $v->target_summary);
				break;
				case 'C':
					$str = sprintf($lang->pushnotify_commented, $target_member, $type, $v->target_summary);
				break;
				case 'A':
					$str = sprintf('<strong>%s</strong>님이 <strong>"%s"</strong>게시판에 <strong>"%s"</strong>댓글을 남겼습니다. ', $target_member, $v->target_browser, $v->target_summary);
				break;
				case 'M':
					$str = sprintf($lang->pushnotify_mentioned, $target_member,  $v->target_summary, $type);
				break;
				case 'E':
					if(version_compare(__XE_VERSION__, '1.7.4', '>='))
					{
						$str = sprintf('<strong>%s</strong>님께서 <strong>%s</strong> 메세지를 보내셨습니다.',$target_member, $v->target_summary);
					}
					else
					{
						$str = sprintf($lang->pushnotify_message_string, $v->target_summary);
					}
				break;
				case 'T':
					$str = sprintf('<strong>%s</strong>님! 스킨 테스트 알림을 완료했습니다.', $target_member);
				break;
				case 'P':
					$str = sprintf('<strong>%s</strong>님이 <strong>"%s"</strong>게시판에 <strong>%s</strong>글을 남겼습니다.', $target_member, $v->target_browser, $v->target_summary);
				break;
				case 'S':
					if($v->target_browser)
					{
						$str = sprintf('<strong>%s</strong>님이 <strong>"%s"</strong>게시판에 <strong>"%s"</strong>글을 남겼습니다.', $target_member, $v->target_browser, $v->target_summary);
					}
					else
					{
						$str = sprintf('<strong>%s</strong>님이 <strong>"%s"</strong>글을 남겼습니다.', $target_member, $v->target_summary);
					}
				break;
				case 'V':
					$str = sprintf('<strong>%s</strong>님이 <strong>"%s"</strong>글을 추천하였습니다.', $target_member, $v->target_summary);
				break;
			}

			$v->text = $str;
			$v->ago = $oPushnotifyModel->getAgo($v->regdate);
			$v->url = getUrl('','act','procPushnotifyRedirect', 'notify', $v->notify, 'url', $v->target_url);
			if($v->target_member_srl)
			{
				$profileImage = $oMemberModel->getProfileImage($v->target_member_srl);
				$v->profileImage = $profileImage->src;
			}

			$list[$k] = $v;
		}

		$output->data = $list;
		return $output;
	}

}