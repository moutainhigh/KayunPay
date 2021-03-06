//UTF-8编码
document.write("<script type=\"text/javascript\" src=\"/portal/js/sign.js\"></script>");

//HTML5兼容ie定义代码
document.createElement('article');
document.createElement('aside');
document.createElement('footer');
document.createElement('header');
document.createElement('hgroup');
document.createElement('main');
document.createElement('nav');
document.createElement('section');
document.createElement('time');

$(function() {
	if ($.datepicker) {
		$.datepicker.regional["zh-CN"] = {
			closeText : "关闭",
			prevText : "&#x3c;上月",
			nextText : "下月&#x3e;",
			currentText : "今天",
			monthNames : [ "一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月",
					"九月", "十月", "十一月", "十二月" ],
			monthNamesShort : [ "一", "二", "三", "四", "五", "六", "七", "八", "九",
					"十", "十一", "十二" ],
			dayNames : [ "星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六" ],
			dayNamesShort : [ "周日", "周一", "周二", "周三", "周四", "周五", "周六" ],
			dayNamesMin : [ "日", "一", "二", "三", "四", "五", "六" ],
			weekHeader : "周",
			dateFormat : "yy-mm-dd",
			firstDay : 1,
			isRTL : !1,
			showMonthAfterYear : !0,
			yearSuffix : "年"
		}
		$.datepicker.setDefaults($.datepicker.regional["zh-CN"]);
	}

	$("head")
			.append(
					'<link rel="icon" href="/portal/images/icons/favicon.ico" type="image/x-icon" /> ');
	init_header();// 初始化头部
	init_footer();// 初始化底部
	init_headerIndex(); //首页初始化头部

	$('.select').hover(function() {
		$(this).find('.options').show();
	}, function() {
		$(this).find('.options').hide();
	});

	$('.select .options a').click(function() {
		var html = $(this).html()
		var value = html;
		var selectElement = $(this).parent().parent();
		if ($(this).attr('value')) {
			value = $(this).attr('value');
		}
		selectElement.children('span').html(html);
		selectElement.children('input').val(value);
		$(this).parent().hide();
	});

	if ($.datepicker) {
		$('.time_input').datepicker({
			changeMonth : true,
			changeYear : true
		});
	}
	$('.clear_text').click(function() {
		$(this).prev().find('input').val('');
	});

	$('.list_table0').children('ul').children('li').click(function() {
		$(this).parent().children('.active').removeClass('active');
		$(this).addClass('active');
	});
	init_m_sidebar();


	$('.app_list').hover(function() {
		$('.app_warp').show();
	}, function() {
		$('.app_warp').hide();
	});
	
	$("#headerIndex .top_headWarp .main nav li").eq(0).addClass("tophead_fontWeight");
	$(".top_headWarp .top_right .top_mobile .top_up").css("background","url(/portal/images/index/ico.png)");
	$(".top_headWarp .top_right .top_mobile .top_up").css("background-position","0px -378px");
	//头部导航移上后展示的效果
	$(".top_headWarp").hover(function(){
		$(".top_headWarp").css("background-color","#fff");
		$(".top_headMain").css("background-color","#fff");
		$(".top_headWarp .main nav li a").css("color","#222");
		$("#headerIndex .top_headWarp .main nav li").eq(0).removeClass("tophead_fontWeight");
		$(".top_headWarp .main nav li").eq(0).addClass("selected");
		$(".top_head").css("color","#666");
		$(".top_left").css("color","#222");
		$(".top_right a").css("color","#666");
		$(".top_headWarp .main .myCount").css("border","1px solid #E6E6E6");
		$(".top_headWarp .main .indexMyCount").css("background-color","#fff");
		$(".top_headWarp .main .indexMyCount").css("color","#222");
		$(".top_headWarp .top_right .topLoginArea li").css("color","#666");
		$(".top_headWarp .head_menu .logo .img1").attr("src","/portal/images/new_logohead_hover.png");
		$(".top_headWarp .head_menu .logo .img2").attr("src","/portal/images/five_years_hover.png");
		$(".top_headWarp .main .indexMyCount i").css("background-position","-98px 0px");
		$(".top_headWarp .top_right .top_mobile .top_up").css("background","url(/portal/images/index/ico2_hover.png)");
		$(".top_headWarp .top_right .top_mobile .top_up").css("background-position","0px -378px");
		$(".top_headWarp .sign-gift").attr("src","/portal/images/sign-gift2.png");
	},function(){
		$(".top_headWarp").css("background-color","inherit");
		$(".top_headMain").css("background-color","inherit");
		$("#headerIndex .top_headWarp .main nav li").eq(0).addClass("tophead_fontWeight");
		$(".top_headWarp .main nav li a").css("color","#fff");
		$("#suspension .main nav li a").css("color","#222");
		$(".top_headWarp .main nav li").eq(0).removeClass("selected");
		$(".top_head").css("color","#fff");
		$(".top_left").css("color","#fff");
		$(".top_right a").css("color","#fff");
		$(".top_headWarp .top_right .topLoginArea li").css("color","#fff");
		$(".top_headWarp .main .myCount").css("border","none");
		$(".top_headWarp .main .indexMyCount").css("background-color","inherit");
		$(".top_headWarp .main .indexMyCount").css("color","#fff");
		$(".top_headWarp .head_menu .logo .img1").attr("src","/portal/images/new_logohead.png");
		$(".top_headWarp .head_menu .logo .img2").attr("src","/portal/images/five_years.png");
		$(".top_headWarp .main .indexMyCount i").css("background-position","-98px -16px");
		$(".top_headWarp .top_right .top_mobile .top_up").css("background","url(/portal/images/index/ico.png)");
		$(".top_headWarp .top_right .top_mobile .top_up").css("background-position","0px -378px");
		$(".top_headWarp .sign-gift").attr("src","/portal/images/sign-gift.png");
	});
	
	
});
// 右侧导航
function init_m_sidebar() {
	var mSidebarElement = $('<div>').attr('id', 'mSidebar').addClass(
			'm-sidebar');
	var mSidebarHTML = "<div class='left'>";
	mSidebarHTML += "<a class='side_1 sidebar'><div class='sweep_app'></div></a>";
	mSidebarHTML += "<a class='side_2 sidebar' href='/aiServer'></a>";
//	mSidebarHTML += "<a class='side_2 sidebar' href='http://crm2.qq.com/page/portalpage/wpa.php?uin=4000270707&f=1&ty=1&aty=0&a=&from=6'></a>";
	mSidebarHTML += "<a class='side_3 sidebar' href='/calculator'></a>";
	mSidebarHTML += "<a class='side_4 sidebar'></a>";
	mSidebarHTML += "</div>";
	
	mSidebarElement.append(mSidebarHTML);
	mSidebarElement.appendTo('body');


}

function close_login_dialog() {
	if (window.closeLoginDialog) {
		window.closeLoginDialog();
	}else {
		$("#loginDialog").hide();
		$("#yinying").hide();
		$('body,html').css('overflow', 'auto');
		
	}
}

// 登录模态框
function show_login_dialog() {
	if ($("div").hasClass("login_dialog")) {
		// 重复登录
		$("#loginDialog").show();
		$("#yinying").show();
	} else {

		var dislogElement = $('<div>').addClass('login_dialog').attr('id',
				'loginDialog');
		var closeElement = $('<div>').addClass('del').appendTo(dislogElement);
		var h2Element = $('<h2>').html('用户登录');
		var formElement = $('<div>').attr('id', 'login_form');
		var accountElement = null;
		accountElement = $('<input>')
				.attr('name', 'account')
				.attr('type', 'text')
				.attr('maxlength', '30')
				.attr('placeholder', '输入11位手机号码')
				.blur(
						function() {
							if ($(this).val() == '') {
								$('#login_account_err').show();
								$('#login_account_err').find('span').remove();
								$('#login_account_err').append(
										$('<span>').html('请输入用户名'));
							} else if (!/^1\d{10}$/.test($.trim($(this).val()))) {
								$('#login_account_err').show();
								$('#login_account_err').find('span').remove();
								$('#login_account_err').append(
										$('<span>').html('用户名不合法'));
							} else {
								$('#login_account_err').hide();
							}
						});

		if (typeof (autologin) != 'undefined') {
			accountElement.val(autologin);
		}

		$('<div>').addClass('item').append(
				$('<i>').addClass('iconUser').css('marginTop', '17px')).append(
				accountElement).append(
				$('<p>').attr('id', 'login_account_ok').addClass('tips-right')
						.addClass('u-tips')).append(
				$('<p>').attr('id', 'login_account_err').addClass('tips-error')
						.addClass('u-tips')).appendTo(formElement);

		$('<div>').addClass('item').append(
				$('<i>').addClass('iconPwd').css('marginTop', '17px')).append(
				$('<input>').attr('type', 'password').attr('name', 'password')
						.addClass('inp').attr('maxlength', '30').val('').attr(
								'placeholder', '输入密码'))
				.append(
						$('<p>').attr('id', 'login_password_ok').addClass(
								'tips-right').addClass('u-tips').html(
								$('<i>').addClass('iconfont'))).append(
						$('<p>').attr('id', 'login_password_err').addClass(
								'tips-error').addClass('u-tips').html(
								$('<i>').addClass('iconfont'))).appendTo(
						formElement);

		$('<div>').addClass('button_panel').append(
				$('<div>').addClass('loginBtn').attr('id', 'login_btn').html(
						'登录').click(function() {

					loginFnPop();

				})).appendTo(formElement);

		$('<div>').addClass('other')
				.append('赶快加入易融恒信吧&nbsp;&nbsp;&nbsp;&nbsp;').append(
						$('<a>').attr('href', '/register').html('立即注册'))
				.append('！').appendTo(formElement);

		closeElement.appendTo(dislogElement);
		closeElement.click(close_login_dialog);
		h2Element.appendTo(dislogElement);
		formElement.appendTo(dislogElement);
		init_yinying();

		$('body,html').css('overflow', 'hidden');
		to_center($('#yinying').show());
		to_center(dislogElement.appendTo('body').show());

		$("input[name='password']").focus(function() {
			$(window).on("keydown", function(event) {
				if (event.keyCode == 13) {
					loginFnPop();
				}
			});
		})

	}

}
function loginFnPop() {
	var loginName = $('#login_form').find('input[name=account]').val();
	var loginPwd = $('#login_form').find('input[name=password]').val();
	YRHX.Cookie().clear();
	YRHX.ajax({
		url : CONFIG.getRequestURI("login"),
		data : {
			loginName : loginName,
			loginPwd : loginPwd
		},
		success : function(sucData) {
			setTimeout(function() {
				YRHX.Cookie("loginName", loginName).set();
				YRHX.Cookie("userCode", sucData.userCode).set();
				YRHX.Cookie("userName", sucData.userName).set();
				YRHX.Cookie("loginId", sucData.loginId).set();
				YRHX.Cookie("verifyPwd",sucData.verifyPwd).set();
				YRHX.Cookie("verifyAuth",sucData.verifyAuth).set();
				close_login_dialog();
				if (window.loginSuccessCallback) {
					window.loginSuccessCallback();
				} else {
					window.location.href=window.location.href;
				}
				
			}, 300);
		},
		error : function(errData) {
			alert('用户名或密码错误');
		}
	});

}
function init_yinying() {
	if ($('#yinying').length == 0) {
		$('<div>').attr('id', 'yinying').addClass('yinying').appendTo('body');
	}
}

// 居中
function to_center(obj) {
	var width = $(window).width();
	var height = $(window).height();
	var top = $(window).scrollTop();
	var eleWidth = obj.width();
	var eleHeight = obj.height();
	obj.css({
		left : (width - eleWidth) / 2,
		top : top + (height - eleHeight) * 2 / 5
	});
}

/**
 * 拼接格式化时间 startDate 20150827 startTime 121212
 */
function formatDateTime(startDate, startTime) {

	if (startDate && typeof (startTime) == "undefined") {
		return startDate.substr(0, 4) + '-' + startDate.substr(4, 2) + '-'
				+ startDate.substr(6, 2) + ' ' + startDate.substr(8, 2) + ':'
				+ startDate.substr(10, 2) + ':' + startDate.substr(12, 2);
	} else if (startDate && startTime) {
		var formateStartDate = startDate.substr(0, 4) + '-'
				+ startDate.substr(4, 2) + '-' + startDate.substr(6, 2);
		var formateStartTime = startTime.substr(0, 2) + ':'
				+ startTime.substr(2, 2) + ':' + startTime.substr(4, 2);
		return formateStartDate + ' ' + formateStartTime;
	}

}

/**
 * 计算距离N天后倒计时 startDate 20150827 startTime 121212 days 3
 */
function countLeftTime(getStartStampTime, days) {

	var getStartStamp = getStartStampTime;
	var endTimeStamp = getStartStamp + 60 * 60 * 24 * days * 1000;
	var curTimeStamp = new Date().getTime();
	var restTimeStamp = endTimeStamp - curTimeStamp;
	var restDays = parseInt(restTimeStamp / 86400000);
	var remain = restTimeStamp % 86400000;
	var restHour = parseInt(remain / 3600000);
	var remain = restTimeStamp % 3600000;
	var restMinute = parseInt(remain / 60000);

	if (restDays + restHour + restMinute < 0) {
		return false;
	} else {
		return restDays + '天 ' + restHour + '小时 ' + restMinute + '分';
	}

}

function strDate2Date(strDate) {
	var ft = "";
	if (strDate.length >= 8) {
		y = strDate.substr(0, 4);
		M = strDate.substr(4, 2);
		d = strDate.substr(6, 2);
		ft += y + "-" + M + "-" + d
	}
	if (strDate.length == 14) {
		H = strDate.substr(8, 2);
		m = strDate.substr(10, 2);
		s = strDate.substr(12, 2);
		ft += " " + H + ":" + m + ":" + s
	}
	return NewDate(ft);
}

function NewDate(str) {
	str = str.substring(0, 19);
	str = str.replace(/-/g, '/');
	var timestamp = new Date(str).getTime();
	return timestamp;
}

// 定时标倒计时
function showLastTime(mv) {
	var showText = "";
	if (mv <= 0) {
		window.location.reload()
	}
	if (mv % 60 > 0) {
		showText += (mv % 60) + "秒";
	}
	var tmpv = mv / 3600;
	if (Math.floor(mv / 60) > 0) {
		showText = Math.floor(mv / 60 % 60) + "分" + showText;
	}
	if (tmpv >= 1) {
		showText = Math.floor(tmpv) + "小时" + showText;
	}
	return showText;
}

//初始化头部
function init_header(){
	 /*TopHtml = "<div class='top_headWarp'>";*/
	var	TopHtml = "<div class='top_head'>";
		//TopHtml += "<div class='top_left'>欢迎致电：400-027-0707</div>";
		TopHtml += "<div class='top_right'>";
		/*TopHtml += "<div class='is_login'><a href='/login'>登录</a><span>|</span><a href='/register'>注册</a></div>";*/
		TopHtml += "<ul class='topLoginArea'>";
		TopHtml += "<li><a href='/login'>登录</a></li>";
		TopHtml += "<span>|</span><li class='last'><a href='/register'>注册</a></li>";
		TopHtml += "<a href='/LendersEducation' class='lender'>出借人教育";
		TopHtml += "<a href='/K01'>帮助中心</a>";
		TopHtml += "<a href='/X06'>活动专题</a>";
//		TopHtml += "<span>|</span><a href='/Y01'>新手指南</a>";
//		TopHtml += "<span>|</span><a href='/K01'>帮助中心</a>";
		TopHtml += "<li class=\"integral-sel\" id=\"sign\" style=\"cursor:pointer\"><a id=\"integralBox\"><img src=\"/portal/images/sign-gift2.png\" class=\"sign-gift\" />签到领积分</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div></li>";
		TopHtml += "</ul>";
		TopHtml += "<a href='/K08' class='jfMall'>积分商城</a>";
		TopHtml += "<div class='top_mobile'><a  class='top_up'><div class='top_down'></div></a></div>";//href='http://topic.yrhx.com/zt/2016/20160205/'
//		TopHtml +="<div class='top_wx'><div class='top_wx_code'></div></div><a href='http://weibo.com/rxmoney' class='top_wb'></a>";
		TopHtml += "</div></div></div>";
	var HeadHtml = "<section class='main'><div class='head_menu'>";
		HeadHtml +="<a href='javascript:;' class='logo'><img class='img1' src='/portal/images/new_logohead_hover.png' title='易融恒信' alt='易融恒信'><i></i><img class='img2' src='/portal/images/five_years_hover.png' title='易融恒信5周年' alt='易融恒信5周年'></a>";
		HeadHtml +="<nav><ul class='nav_menu_list'>";
		HeadHtml +="<li><a href='/index'>首页</a></li>";
		HeadHtml +="<li class='loanHead'><i></i><a href='/Z02?navTab=1' class='first'>我要出借</a><dl class='loan_nav'><dd><a href='/Z02?navTab=0'>新手专享</a></dd><dd><a href='/Z02?navTab=1'>投资项目</a></dd><dd><a href='/Z02?navTab=2'>债权转让</a></dd></dl></li>";
		HeadHtml +="<li><a href='/Z03'>实时数据</a></li>";
		HeadHtml +="<li class='loanHead'><i></i><div class='loanHead_pic'></div><a href='/Message01' class='first'>信息披露</a><dl class='loan_nav'><dd><a href='/Message01'>合规之路</a></dd><dd><a href='http://topic.yrhx.com/zt/2018/20180521/' target='_blank'>银行存管</a></dd><dd><a href='/Message03'>资质证书</a></dd><dd><a href='/Message04'>组织信息</a></dd><dd><a href='/X09'>运营报告</a></dd><dd><a href='/Message06'>审计信息</a></dd><dd><a href='/Message07' target='_blank'>风险控制</a></dd><dd><a href='/Message08'>监管法规</a></dd><dd><a href='/Message09'>重大事项</a></dd><dd><a href='/Message10'>承诺书</a></dd></dl></li>";
		HeadHtml +="<li class='loanHead'><i></i><a href='/Y06_01' class='first'>关于我们</a><dl class='loan_nav'><dd><a href='/Y06_01'>公司简介</a></dd><dd><a href='/Y06_03'>管理团队</a></dd><dd><a href='/Y06_05'>平台成长记录</a></dd><dd><a href='/Y06_06'>合作伙伴</a></dd></dl></li>";
		HeadHtml +="</ul>";
		HeadHtml +="<div class='myCount'><a href='/A00' class='indexMyCount'><i></i>我的账户<b></b></a></div>";
		HeadHtml +="</nav></div></section>";
		//HeadHtml +="</div>";

		
	var topRight = CONFIG.header.topRight;
	var topRightLength = topRight.length;
	$('#header').append(TopHtml);
	$('#header').append(HeadHtml);
	isSelect('.nav_menu_list li');
	
	/*-------------------华丽分割线----------------*/
	if($("#header").length > 0) {
		var oCalendar = new Calendar('sign');
		// 签到领积分
		if (YRHX.Cookie("userCode").get()) {	// 用户是否登录
			YRHX.ajax({
				url : CONFIG.getRequestURI("isSign"),
				success : function(sucData) {
					$(".integral-sel").html("<a id=\"integralBox\"><img src=\"/portal/images/sign-gift2.png\" class=\"sign-gift\" />已签到</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
					var oCalendar = new Calendar('sign');
				},
				error : function(errData) {
					$(".integral-sel").html("<a id=\"integralBox\"><img src=\"/portal/images/sign-gift2.png\" class=\"sign-gift\" />签到领积分</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
					var oCalendar = new Calendar('sign');
					$("#integralBox").bind("click", function() {
						var boo = true;
						if (boo) {
							YRHX.ajax({
								url : CONFIG.getRequestURI("signIn"),
								success : function(sucData) {
									$("#sign_points").text(sucData["points"]);
									$("#sustain_day").text(sucData["sustain_day"]);
									$('body,html').css('overflow', 'hidden');
									to_center($('#yinying').show());
									to_center($('.bid_show2').show());
									$(".integral-sel").html("<a id=\"integralBox\">已签到<img src=\"/portal/images/sign-gift2.png\" class=\"sign-gift\" /></a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
									boo = true;
								},
								error : function(errData) {
									$.popTips("popTipErr", errData.return_info || "签到异常");
									boo = true;
								}
							});
							boo = false;
						}
						$(".integral-con").css("display", "none");
							
						$('.bid_show2 .pop_close').click(function(){
							$('body,html').css('overflow', 'auto');
							$('#yinying').hide();
							$('.bid_show2').hide();
						});
					});
				}
			});
		} else {
			$("#integralBox").bind("click", function() {
				show_login_dialog();
			});
		}
	}
	
}







//首页初始化头部		
function init_headerIndex(){
	var IndexTopHtml = "<div class='top_headWarp'><div class='top_head'>";
		//IndexTopHtml += "<div class='top_left'>欢迎致电：400-027-0707</div>";
		IndexTopHtml += "<div class='top_right'>";
		/*IndexTopHtml += "<div class='is_login'><a href='/login'>登录</a><span>|</span><a href='/register'>注册</a></div>";*/
		IndexTopHtml += "<ul class='topLoginArea'>";
		IndexTopHtml += "<li><a href='/login'>登录</a></li>";
		IndexTopHtml += "<span>|</span><li class='last'><a href='/register'>注册</a></li>";
		IndexTopHtml += "<a href='/LendersEducation' class='lender'>出借人教育</a>";
		IndexTopHtml += "<a href='/K01'>帮助中心</a>";
		IndexTopHtml += "<a href='/X06'>活动专题</a>";
//		IndexTopHtml += "<span>|</span><a href='/Y01'>新手指南</a>";
//		IndexTopHtml += "<span>|</span><a href='/K01'>帮助中心</a>";
		IndexTopHtml += "<li class=\"integral-sel\" id=\"sign\" style=\"cursor:pointer\"><a id=\"integralBox\"><img src=\"/portal/images/sign-gift.png\" class=\"sign-gift\" />签到领积分</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div></li>";
		IndexTopHtml += "</ul>";
		IndexTopHtml += "<a href='/K08' class='jfMall'>积分商城</a>";
		IndexTopHtml += "<div class='top_mobile'><a  class='top_up'><div class='top_down'></div></a></div>";//href='http://topic.yrhx.com/zt/2016/20160205/'
//		IndexTopHtml +="<div class='top_wx'><div class='top_wx_code'></div></div><a href='http://weibo.com/rxmoney' class='top_wb'></a>";
		IndexTopHtml += "</div></div></div></div>";
	var IndexHeadHtml = "<div class='top_headWarp'><section class='main'><div class='head_menu'>";
		IndexHeadHtml +="<a href='javascript:;' class='logo'><img class='img1' src='/portal/images/new_logohead.png' title='易融恒信' alt='易融恒信'><i></i><img class='img2' src='/portal/images/five_years.png' title='易融恒信5周年' alt='易融恒信5周年'></a>";
		IndexHeadHtml +="<nav><ul class='nav_menu_list'>";
		IndexHeadHtml +="<li><a href='/index'>首页</a></li>";
		IndexHeadHtml +="<li class='loanHead'><i></i><a href='/Z02?navTab=1' class='first'>我要出借</a><dl class='loan_nav'><dd><a href='/Z02?navTab=0'>新手专享</a></dd><dd><a href='/Z02?navTab=1'>投资项目</a></dd><dd><a href='/Z02?navTab=2'>债权转让</a></dd></dl></li>";
		IndexHeadHtml +="<li><a href='/Z03'>实时数据</a></li>";
		IndexHeadHtml +="<li class='loanHead'><i></i><div class='loanHead_pic'></div><a href='/Message01' class='first'>信息披露</a><dl class='loan_nav'><dd><a href='/Message01'>合规之路</a></dd><dd><a href='http://topic.yrhx.com/zt/2018/20180521/' target='_blank'>银行存管</a></dd><dd><a href='/Message03'>资质证书</a></dd><dd><a href='/Message04'>组织信息</a></dd><dd><a href='/X09'>运营报告</a></dd><dd><a href='/Message06'>审计信息</a></dd><dd><a href='/Message07' target='_blank'>风险控制</a></dd><dd><a href='/Message08'>监管法规</a></dd><dd><a href='/Message09'>重大事项</a></dd><dd><a href='/Message10'>承诺书</a></dd></dl></li>";
		IndexHeadHtml +="<li class='loanHead'><i></i><a href='/Y06_01' class='first'>关于我们</a><dl class='loan_nav'><dd><a href='/Y06_01'>公司简介</a></dd><dd><a href='/Y06_03'>管理团队</a></dd><dd><a href='/Y06_05'>平台成长记录</a></dd><dd><a href='/Y06_06'>合作伙伴</a></dd></dl></li>";
		IndexHeadHtml +="</ul>";
		IndexHeadHtml +="<div class='myCount'><a href='/A00' class='indexMyCount'><i></i>我的账户<b></b></a></div>";
		IndexHeadHtml +="</nav></div></section></div>";
								
	$('#headerIndex').append(IndexTopHtml);
	$('#headerIndex').append(IndexHeadHtml);	
				
				

				/*-------------------首页华丽分割线----------------*/
				if($("#headerIndex").length > 0) {
					var oCalendar = new Calendar('sign');
					// 签到领积分
					if (YRHX.Cookie("userCode").get()) {	// 用户是否登录
						YRHX.ajax({
							url : CONFIG.getRequestURI("isSign"),
							success : function(sucData) {
								$(".integral-sel").html("<a id=\"integralBox\"><img src=\"/portal/images/sign-gift.png\" class=\"sign-gift\" />已签到</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
								var oCalendar = new Calendar('sign');
							},
							error : function(errData) {
								$(".integral-sel").html("<a id=\"integralBox\"><img src=\"/portal/images/sign-gift.png\" class=\"sign-gift\" />签到领积分</a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
								var oCalendar = new Calendar('sign');
								$("#integralBox").bind("click", function() {
									var boo = true;
									if (boo) {
										YRHX.ajax({
											url : CONFIG.getRequestURI("signIn"),
											success : function(sucData) {
												$("#sign_points").text(sucData["points"]);
												$("#sustain_day").text(sucData["sustain_day"]);
												$('body,html').css('overflow', 'hidden');
												to_center($('#yinying').show());
												to_center($('.bid_show2').show());
												$(".integral-sel").html("<a id=\"integralBox\">已签到<img src=\"/portal/images/sign-gift.png\" class=\"sign-gift\" /></a><div class=\"sellist integral-con hide\"><i class=\"lineinteg\"></i><div class=\"boxinter\"></div>");
												boo = true;
											},
											error : function(errData) {
												$.popTips("popTipErr", errData.return_info || "签到异常");
												boo = true;
											}
										});
										boo = false;
									}
									$(".integral-con").css("display", "none");
										
									$('.bid_show2 .pop_close').click(function(){
										$('body,html').css('overflow', 'auto');
										$('#yinying').hide();
										$('.bid_show2').hide();
									});
								});
							}
						});
					} else {
						$("#integralBox").bind("click", function() {
							show_login_dialog();
						});
					}
				}

}






$('.top_mobile').delegate(".top_up","hover",function(){
  $('.top_down').animate({'top':'32px'},500);
});
// 初始化底部
function init_footer() {
	var href4="http://whgswj.whhd.gov.cn:8089/whwjww/indexquery/indexqueryAction!dizview.dhtml?chr_id=69039c17c4594b9b8b8d89cf63eb4286&bus_ent_id=420000000008829404&bus_ent_chr_id=f76636a166c342619c9dbeecf32f0464";
	var mainElement = $('<section>').addClass('main').appendTo('#footer');
	var bottomElement = $('<section>').addClass('bottom').appendTo('#footer');
	var mainLayoutCenterElement = $('<div>').addClass('layout_center')
			.appendTo(mainElement);
	
	var bottomMenu = CONFIG.footer.bottomMenu;
	var bottomMenuLength = bottomMenu.length;
	var aboutMeElement = "<div class='about_me'><ul>";
		aboutMeElement +="<li><div class='foot_code code2'></div><p>微信服务号</p></li>";
		aboutMeElement +="<li><div class='foot_code code3'></div><p>微信订阅号</p></li>";
		aboutMeElement +="<li class='last'><div class='foot_code code1'></div><p>手机客户端</p></li>";
		aboutMeElement +="</ul></div>";
	var contactUsElement="<div class='contact_us'>";
		contactUsElement+="<div class='contact_us_text1'>400 027 0707</div>";
		contactUsElement+="<div class='contact_us_text2'>客服工作时间：9:00-22:00</div>";
		contactUsElement+="<div class='contact_us_text4'>投诉联系电话：027-83356151</div>";
		contactUsElement+="<div class='contact_us_text3'>";
		contactUsElement+="<a href='javascript:;'>官方QQ群</a>";
		contactUsElement+="<div class='contact_qq01'><div class='contact_qqBox'><p>官方①群: 148177083（已满）</p><p>官方②群: 98588949（已满）</p><p class='contact_last'>官方③群: 246071745（可加入）</p></div></div>";
		contactUsElement+="</div></div>";
	var CopyrightElement="<div class='layout_center'>";
		CopyrightElement+="<div class='text'>Copyright © 2014 YRHX,All rights reserved.  |  鄂ICP备13012903号-2<p style='margin-top:11px;'><a href='http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=42010302000470' target='_blank' style='background:url(/portal/images/beianbgs.png) no-repeat; padding-left:30px; color:#999; display:block;'>鄂公网安备&nbsp;42010302000470</a></p></div>";
		CopyrightElement+="<div class='g'>";
		CopyrightElement+="<a href='http://si.trustutn.org/info?sn=483150130000081064353' target='_blank' class='g0'></a>";
		CopyrightElement+="<a href='http://www.cyberpolice.cn/wfjb/' target='_blank' class='g2'></a>";
		CopyrightElement+="<a href='https://trustsealinfo.verisign.com/splash?form_file=fdf/splash.fdf&amp;dn=www.yrhx.com&amp;lang=zh_cn' target='_blank' class='g3'></a>";
		CopyrightElement+="<a href='http://whgswj.whhd.gov.cn:8089/whwjww/indexquery/indexqueryAction!dizview.dhtml?chr_id=69039c17c4594b9b8b8d89cf63eb4286&amp;bus_ent_id=420000000008829404&amp;bus_ent_chr_id=f76636a166c342619c9dbeecf32f0464' target='_blank' class='g4'></a>";
		CopyrightElement+='<a href="http://webscan.360.cn/index/checkwebsite/url/www.yrhx.com"><img border="0" src="http://webscan.360.cn/status/pai/hash/88f815840742ffa158c082b864ca3ce3"/></a>';
		CopyrightElement+="</div></div>";
	
	bottomElement.append(CopyrightElement)
	for (var i = 0; i < bottomMenuLength; i++) {
		if(i<bottomMenuLength-1){
			var divElement = $('<div>').addClass('list').appendTo(
					mainLayoutCenterElement);
			
		}else{
			var divElement = $('<div>').addClass('list last').appendTo(
					mainLayoutCenterElement);
		}
		
		var ulElement = $('<ul>');
		var menu = bottomMenu[i].menu;
		var menuLength = menu.length;
		divElement.append($('<label>').html(bottomMenu[i].label));
		divElement.append(ulElement);
		for (var j = 0; j < menuLength; j++) {
			$('<li>').append(
					$('<a>').attr('href', menu[j].url).html(menu[j].text))
					.appendTo(ulElement);
		}
	}
	mainLayoutCenterElement.append(aboutMeElement);
	mainLayoutCenterElement.append(contactUsElement);
	

	// 统计代码
	// $("body").append('<script type="text/javascript"
	// src="http://pingjs.qq.com/h5/stats.js" name="MTAH5" sid="500002247"
	// ></script>');
	
	var _hmt = _hmt || [];
	(function() {
		var hm = document.createElement("script");
		hm.src = "//hm.baidu.com/hm.js?466bbed2607a2b7a987635d0100b0cdb";
		var s = document.getElementsByTagName("script")[0];
		s.parentNode.insertBefore(hm, s);
	})();
}
/**
 * 验证码
 * 
 * 
 */
function captchaApiV2(callback) {
	if (!$("#sendMsgBtn").hasClass("disabled")) {
		function initCaptcha() {
			$("#txtYZM").val("");
			$("#captchaImg").attr("src", "/captcha4sys?v=" + Math.random());
		}

		if (!$("div").hasClass("captchaLayer")) {
			$("body")
					.append(
							'<div class="confirm_pay captchaLayer">'
									+ '<div class="pay_del"><div class="del" id="captchaClose"></div></div>'
									+ '<div class="pay_row">'
									+ '<label>验证码：</label><input id="txtYZM" type="text" maxlength=4><img src="" title="验证码" id="captchaImg">'
									+ '</div>'
									+ '<div class="pay_row"><button id="confirmCap">确认</button></div>'
									+ '</div>'
									+ '<div id="yinyingTop" class="yinyingTop" style="zindex:10"></div>');
			$("#captchaClose").click(function() {
				$("#yinyingTop").hide();
				$(".confirm_pay").hide();
				$('body,html').css('overflow', 'auto');
			});
			$("#confirmCap").click(function() {
				if ($("#txtYZM").val().length == 4) {
					callback($("#txtYZM").val());
				}

			});
			$("#captchaImg").click(function() {
				$(this).attr("src", "/captcha4sys?v=" + Math.random());
			});
		} else {
			$(".captchaLayer").show();
		}
		initCaptcha();
		to_center($('#yinyingTop').show());
		to_center($('.confirm_pay').show());
		$('body,html').css('overflow', 'hidden');
	}
}
/**
 * 标书title去掉区域显示
 */
function dealTitle(title) {
	if (title != "【周年庆专享标】") {
		var startSort = title.indexOf("】") + 1;
		return title.substring(startSort);
	} else {
		return title;
	}
}
/**
 * 登录后状态显示
 * shiqingsong
 */
$(document).ready(function(){
	loginState();
}); 
$('body').delegate('.side_4','click',function(){
	
	if ($(window).scrollTop() > 600) {
		
		$('html,body').animate({
			scrollTop : '0px'
		});
	}
	
})
function loginState() {
	var topLiArr = $(".topLoginArea > li");
	if (YRHX.Cookie("userName").get()) {
		$(topLiArr).each(
			function(i) {
				if ($(topLiArr[i]).text() == "注册") {
					
				
					$(topLiArr[i]).html("<a>退出</a>");
					$(topLiArr[i]).css({
						"cursor" : "pointer"
					});
					$(topLiArr[i]).on('click',function() {
						YRHX.ajax({
							url : CONFIG.getRequestURI("logout"),
							success : function(sucData) {
								var loginName = YRHX.Cookie("loginName").get();
								YRHX.Cookie().clear();
								YRHX.Cookie("loginName", loginName).set();
								window.location.href = "/login";
							},
							error : function(errData) {
								$.popTips("popTipErr",errData.return_info || "退出异常");
							}
						});
					})
				
				
				}
				if ($(topLiArr[i]).text() == "登录") {
					$(topLiArr[i]).html("您好, <a href='/A00' id='curUserName' style='padding-left:5px'> "+ YRHX.Cookie("userName").get()+ "</a>");
				}
			});
		/*我的账户*/
		YRHX.ajax({
			url : CONFIG.getRequestURI("queryMainPage"),
			success : function(sucData) {
				var indexCount = "<div class='indexCount_list'>";
				indexCount += "<div class='count_pro'><div class='user_icon'></div><div class='name_level'>#{time}，<span> #{userName}</span><br /><a href='/grade'>等级&nbsp;:&nbsp;#{vipLevelName}</a></div></div>"; 
				indexCount += "<div class='indexCount_item'><a href='/A00'>资产总额：<span>#{all_amount}元</span></a></div>";
				indexCount += "<div class='indexCount_item'><a href='/A00'>可用余额：<span>#{avBalance}元</span></a></div>";
				indexCount += "<div class='indexCount_item'>加息额度：<span>#{rewardRateAmount}元</span></div>";
				indexCount += "<div class='indexCount_item'><a href='/B05'>投资券：<span>#{countTickets} 张</span></a></div>";
				//indexCount += "<div class='indexCount_item'><a href='/A05'>智投盈：<span>已加入</span></a></div>";
				indexCount += "<div class='indexCount_item'><a href='/A04'>自动投标：<span>#{state}</span></a></div>";
				indexCount += "<div class='indexCount_item last'><a class='single_link red' href='/B02'>充值</a><a class='single_link' href='/B03'>提现</a></div>";
				indexCount += "</div>";
				var lable_welcome = "";
				var nowDate = new Date();
				var hour = nowDate.getHours();
				if(hour < 4)
					lable_welcome = "晚上好";
				else if (hour < 9)
					lable_welcome = "早上好";
				else if (hour < 12)
					lable_welcome = "上午好"; 
				else if (hour < 14)
					lable_welcome = "中午好";
				else if (hour < 17)
					lable_welcome = "下午好";
				else
					lable_welcome = "晚上好";
					
				
				sucData['all_amount'] = YRHX.toDecimal2((sucData["avBalance"]+sucData["frozeBalance"]+sucData["beRecyPrincipal"])/10.0/10.0);
				sucData['avBalance'] = YRHX.toDecimal2(sucData["avBalance"]/10.0/10.0);
				sucData['rewardRateAmount'] = YRHX.toDecimal2(sucData["rewardRateAmount"]/10.0/10.0);
				if(sucData["isAvailable"] && sucData["isAvailable"]==1){
					sucData['state'] ='已开启'
				}else{
					sucData['state'] ='未开启'
				}
				var tmpHtml = indexCount.makeHtml({time:lable_welcome, userName:sucData['userName'],vipLevelName:sucData['vipLevelName'],all_amount:sucData['all_amount'],avBalance:sucData['avBalance'],countTickets:sucData['countTickets'],state:sucData['state'],rewardRateAmount:sucData['rewardRateAmount']});
				$('.myCount').append(tmpHtml);
			},
			error : function(errData) {
				$.popTips("popTipErr",errData.return_info || "链接服务器失败");
			}
		});
		
	}
}
$('body').delegate('.indexMyCount','mouseover',function(){
	if (YRHX.Cookie("userName").get()) {
		$('.indexMyCount').css('height','32px');
	}
})
$('body').delegate('.indexMyCount','mouseleave',function(){
	if (YRHX.Cookie("userName").get()) {
		$('.indexMyCount').css('height','30px');
	}
})

/*头部导航滚动*/
function scroll_head(){
	var HeadHtml = "<section class='main scroll_up' id='suspension'><div class='head_menu'>";
	HeadHtml +="<a href='javascript:;' class='logo'><img class='img1' src='/portal/images/new_logohead_hover.png' title='易融恒信' alt='易融恒信'><i></i><img class='img2' src='/portal/images/five_years_hover.png' title='易融恒信5周年' alt='易融恒信5周年'></a>";
	HeadHtml +="<nav><ul class='nav_menu_list1'>";
	HeadHtml +="<li><a href='/index'>首页</a></li>";
	HeadHtml +="<li class='loanHead'><i></i><a href='/Z02?navTab=1' class='first'>我要出借</a><dl class='loan_nav'><dd><a href='/Z02?navTab=0'>新手专享</a></dd><dd><a href='/Z02?navTab=1'>投资项目</a></dd><dd><a href='/Z02?navTab=2'>债权转让</a></dd></dl></li>";
	HeadHtml +="<li><a href='/Z03'>实时数据</a></li>";
	HeadHtml +="<li class='loanHead'><i></i><div class='loanHead_pic'></div><a href='/Message01' class='first'>信息披露</a><dl class='loan_nav'><dd><a href='/Message01'>合规证书</a></dd><dd><a href='http://topic.yrhx.com/zt/2018/20180521/' target='_blank'>银行存管</a></dd><dd><a href='/Message03'>资质证书</a></dd><dd><a href='/Message04'>组织信息</a></dd><dd><a href='/X09'>运营报告</a></dd><dd><a href='/Message06'>审计信息</a></dd><dd><a href='/Message07' target='_blank'>风险控制</a></dd><dd><a href='/Message08'>监管法规</a></dd><dd><a href='/Message09'>重大事项</a></dd><dd><a href='/Message10'>承诺书</a></dd></dl></li>";
	HeadHtml +="<li class='loanHead'><i></i><a href='/Y06_01' class='first'>关于我们</a><dl class='loan_nav'><dd><a href='/Y06_01'>公司简介</a></dd><dd><a href='/Y06_03'>管理团队</a></dd><dd><a href='/Y06_05'>平台成长记录</a></dd><dd><a href='/Y06_06'>合作伙伴</a></dd></dl></li>";
	HeadHtml +="</ul>";
	HeadHtml +="<div class='myCount'><a href='/A00' class='indexMyCount'><i></i>我的账户<b></b></a></div>";
	HeadHtml +="</nav></div></section>";
	$('#header').append(HeadHtml);
	$('#headerIndex').append(HeadHtml);
}

scroll_head();

$(window).scroll(function() {
	if ($(window).scrollTop() > 500) {
		$('#suspension').removeClass('scroll_up').addClass('scroll_down');
	} else {
		$('#suspension').removeClass('scroll_down').addClass('scroll_up');
	}
});
window.onload = function(){
}
function isSelect(target){
	if( topNavSelect < 3 ){
		$(target).eq( topNavSelect ).addClass("selected").siblings().removeClass("selected");
	}else if( topNavSelect == 3){
		topNavSelect++;
		$(target).eq( topNavSelect ).addClass("selected").siblings().removeClass("selected");
	}
}
isSelect('.nav_menu_list1 li');



/*---------------------华丽分割线-------------------------*/
document.write("<div class=\"bid_show2\" style=\"display:none; position: absolute; z-index: 15; color:#FFE8AE; font-size:14px;\">" +
		"<span class=\"pop_close\" style=\"background: url(/portal/images/box_bg_close.png);width: 35px;height: 35px;position: absolute;top: -17px;right: -17px;cursor: pointer;\"></span>" +
		"<span id=\"sign_points\" style=\"width:100%; display:block; position: absolute; padding-left:200px; margin-top: 25px; color:#FFF; font-size:30px\"></span>" +
		"<span id=\"sustain_day\" style=\"width:100%; display:block; position: absolute; padding-left:225px; margin-top: 58px; color:#FFF; font-size:20px\"></span>" +
		"<a href=\"/Z02?navTab=1\"><img src=\"portal/images/sign.png\" style=\"width:100%\"></a></div>");

