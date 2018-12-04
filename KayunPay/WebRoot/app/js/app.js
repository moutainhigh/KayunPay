(function($) {

	// 后台全局设置
	var config = {} ;
	
	config["title"] = window.RES["title"] ;
	
	// 使用配置
	$("title").text( config["title"] );
	$(".am-topbar-brand").find("strong").text( config["title"] ) ;
	
	// 使用配置---End
	$(function() {
		var $fullText = $('.admin-fullText');
		$('#admin-fullscreen').on('click', function() {
			$.AMUI.fullscreen.toggle();
		});

		$(document).on($.AMUI.fullscreen.raw.fullscreenchange, function() {
			$fullText.text($.AMUI.fullscreen.isFullscreen ? '退出全屏' : '开启全屏');
		});
	});

	nav.regist({
		"root" : ".nav-root"
	});
	
	YRHX.ajax({
		url : "/getMenusV2" ,
		success : function(menus){
			RES["menu"] = menus ;
			//Got menu's info.
			for(var mIndex in RES["menu"]){
				var menu = RES["menu"][mIndex] ;
				if( menu["menu_type"] == "A" ){
					if( menu["menu_id"] == menu["menu_id_p"]){
						nav.genParentNavs([ {
							"navCode" : "t_" + menu["menu_id"],
							"navTitle" : menu["menu_name"]
						} ]);
					}else{
						nav.appendChild({
							"parentCode" : "t_" + menu["menu_id_p"] ,
							"navCode" : "t_" + menu["menu_id"],
							"navTitle" : menu["menu_name"] ,
							"navUrl" : menu["menu_url"] ,
							"navClass" : menu["menu_icon1"]||"am-icon-puzzle-piece"
						});
					}
				}
			}
		}
	});

	nav.ready();

	$(".notice").find("span.title").text("今天公告");
	$(".notice").find("p.content").html("自动还款时间为每日上午10:30至10:45之间完成，<br>请勿在此时间区间内做操作以免影响数据。");
	
	$(".wiki").find("span.title").text("Wiki");
	$(".wiki").find("p.content").text("在柏林墙推倒的前两年，东德一个名叫亨里奇的守墙卫兵，开枪射杀了攀爬柏林墙企图逃向西德的青年克利斯。 在墙倒后对他的审判中，他的律师辩称，他仅仅是施行命令的人，基本没有挑选的权力，罪不在己。 而法官则指出：“作为警察，不施行上级命令是有罪的，然而打不准是无罪的。 作为一个心智健全的人，此时此刻，你有把枪口抬高一厘米的权力，这是你应自动承担的良心义务。 这个世界，在法律之外还有良心。当法律和良心抵触之时，良心是最高的行动原则，而不是法律。尊崇性命，是一个放之四海而皆准的准绳。”");
	
	$(".copy-right").text(window.RES["copyright"]);
	
})(jQuery);
