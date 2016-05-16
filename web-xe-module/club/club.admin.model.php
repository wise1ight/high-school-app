<?php
    /**
     * @class  clubAdminModel
     * @author KUvH (kuvh@live.co.kr)
     * @brief  동아리 모듈의 admin model class
     **/

    class clubAdminModel extends club {

        function init() {
        }

		function getClubList($args)
		{
			if(!$args->page) $args->page = 1;
			$this->_setSearchOption($args);
			$output = executeQueryArray('club.getClubList', $args);
			return $output;
		}

		function _setSearchOption(&$args)
		{
			switch($args->search_target)
			{
				case 'title':
				case 'domain':
					$args->{'s_'.$args->search_target} = $args->search_keyword;
					break;
			}
		}

    }

?>
