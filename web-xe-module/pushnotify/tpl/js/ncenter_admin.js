(function($) {
	function debugPrint(msg){if(typeof console == 'object' && typeof console.log == 'function'){console.log(msg)}};

	$(function(){
		var $mentionOption = $('.mention_option');
		var $checkbox = $('input:checkbox', $mentionOption);
		var $preview = $('ul.preview', $mentionOption);
		var $previewItem = $('li', $preview);
		var mentionRegx = /(^|\s)@([^\s]+)/ig;

		$mentionOption.bind('prepare', function() {
			mentionRegx = ['(^|\\s)@([^\\s', '', ']+)'];
			var mentionSplit = [];
			$checkbox.each(function(){
				if($(this).is(':checked')) mentionSplit.push($(this).data('mention-split'));
			});
			mentionRegx[1] = mentionSplit.join('');
			mentionRegx = mentionRegx.join('');
			mentionRegx = new RegExp(mentionRegx, 'ig');
		});

		$preview.bind('prepare', function() {
			$previewItem.each(function() {
				var $this = $(this);
				var $strong = $('strong', $this);
				var names = [];
				$strong.contents().each(function() {
					names.push(this.nodeValue.replace('@', ''));
				});
				$this.data('mention-target', names);
			});
		});
		$preview.triggerHandler('prepare');

		$preview.bind('preview', function() {
			$mentionOption.triggerHandler('prepare');
			$previewItem.each(function() {
				var $this = $(this);
				var target = $this.data('mention-target');
				var defaultClass = $this.data('mention-default');
				var content = $this.text();
				var matched = content.match(mentionRegx);
				if(matched) $.map(matched, function(text, idx) { matched[idx] = $.trim(text.replace(['@'], ''))});
				$this.attr('class', (compare(target, matched)) ? 'enable' : (defaultClass && defaultClass == 'enable') ? 'disable' : '');
			});
		});
		$preview.triggerHandler('preview');
		$checkbox.click(function() {$preview.triggerHandler('preview');})

		function compare(arrayA, arrayB) {
			if(!arrayA || !arrayB) return false;
			if(arrayA.length != arrayB.length) return false;
			var a = $.extend(true, [], arrayA);
			var b = $.extend(true, [], arrayB);
			for(var i = 0, l = a.length; i < l; i++) {
				if(a[i] !== b[i]) return false;
			}
			return true;
		}

	});
}) (jQuery);


function doDummyDataInsert()
{
	jQuery.exec_json('pushnotify.procPushnotifyAdminInsertDummyData', {}, function completeGetDummyInfo(ret_obj){alert(ret_obj.message)});
}

function doDummyPushDataInsert()
{
	jQuery.exec_json('pushnotify.procPushnotifyAdminInsertPushData', {}, function completeGetDummyPushInfo(ret_obj){alert(ret_obj.message)});
}

