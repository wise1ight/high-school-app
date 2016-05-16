<?php
    /**
     * @class  clubSmartphone
     * @author KUvH (kuvh@live.co.kr)
     * @brief  동아리 모듈의 SmartPhone class
     **/

    class clubSPhone extends club {

        function procSmartPhone(&$oSmartPhone) {
            $oTemplate = new TemplateHandler();
            $content = $oTemplate->compile($this->module_path.'tpl', 'smartphone');
            $oSmartPhone->setContent($content);
        }
    }
?>
