<?php
    /**
     * @class club 
     * @author KUvH (kuvh@live.co.kr)
     * @brief  동아리 모듈의 package
     **/

    class club extends ModuleObject {

        /**
         * @brief 설치시 추가 작업이 필요할시 구현
         **/
        function moduleInstall() {
            $oModuleController = &getController('module');

            $oModuleController->insertTrigger('display', 'club', 'controller', 'triggerMemberMenu', 'before');
			$oModuleController->insertTrigger('moduleHandler.proc', 'club', 'controller', 'triggerApplyLayout', 'after');
			$oModuleController->insertTrigger('moduleHandler.init', 'club', 'controller', 'triggerApplyMLayout', 'after');

            return new Object();
        }


        /**
         * @brief 설치가 이상이 없는지 체크하는 method
         **/
        function checkUpdate() {
            $oModuleController = &getController('module');
            $oModuleModel = &getModel('module');
            $oDB = &DB::getInstance();

            // 2009. 02. 11 가상 사이트의 로그인 정보 영역에 관리 기능이 추가되어 표시되도록 트리거 등록
            if(!$oModuleModel->getTrigger('display', 'club', 'controller', 'triggerMemberMenu', 'before')) return true;

            // 2009. 04. 23 카페의 설명
            if(!$oDB->isColumnExists("clubs","description")) return true;

            if(!$oModuleModel->getTrigger('moduleHandler.proc', 'club', 'controller', 'triggerApplyLayout', 'after')) return true;

			//2012. 08. 30 모바일 레이아웃 지원
			if(!$oDB->isColumnExists("clubs","mlayout_srl")) return true;
            if(!$oModuleModel->getTrigger('moduleHandler.init', 'club', 'controller', 'triggerApplyMLayout', 'after')) return true;

            return false;
        }

        /**
         * @brief 업데이트 실행
         **/
        function moduleUpdate() {
            $oModuleController = &getController('module');
            $oModuleModel = &getModel('module');
            $oDB = &DB::getInstance();

            // 2009. 02. 11 가상 사이트의 로그인 정보 영역에 관리 기능이 추가되어 표시되도록 트리거 등록
            if(!$oModuleModel->getTrigger('display', 'club', 'controller', 'triggerMemberMenu', 'before')) 
                $oModuleController->insertTrigger('display', 'club', 'controller', 'triggerMemberMenu', 'before');

            // 2009. 04. 23 카페의 설명
            if(!$oDB->isColumnExists("clubs","description")) 
                $oDB->addColumn("clubs","description","text");

            if(!$oModuleModel->getTrigger('moduleHandler.proc', 'club', 'controller', 'triggerApplyLayout', 'after') )
                $oModuleController->insertTrigger('moduleHandler.proc', 'club', 'controller', 'triggerApplyLayout', 'after');

			//2012. 08. 30 모바일 레이아웃
			if(!$oDB->isColumnExists("clubs","mlayout_srl")) {
				$oDB->addColumn('clubs',"mlayout_srl","number",11,0);
			}
            if(!$oModuleModel->getTrigger('moduleHandler.init', 'club', 'controller', 'triggerApplyMLayout', 'after') )
                $oModuleController->insertTrigger('moduleHandler.init', 'club', 'controller', 'triggerApplyMLayout', 'after');
			

            return new Object(0, 'success_updated');
        }

        /**
         * @brief 캐시 파일 재생성
         **/
        function recompileCache() {
        }
    }
?>
