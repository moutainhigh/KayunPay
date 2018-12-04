
var NAV_HTML = {
    "ROOT": "<div class='user-nav-item'><i style='top: 50px;'></i><ul id='ROOT_#{navCode}'>#{childHtml}</ul></div>",
    "BUTTON": "<li class='childButton' id='child_#{navCode}'><a href='#{navUrl}'>#{navName}</a></li>"
};

$.fn.nav = function (data) {
    var that = this;

    for (var navIndex in data) {

        var navRow = data[navIndex];
        var rootHtml = NAV_HTML["ROOT"];
        rootHtml = formatHtml( navRow , rootHtml ) ;
        var childHtml = "" ;
        if(  navRow.list ){
            var childList = navRow.list ;
            childHtml = childNav( childList ) ;
        }
        rootHtml = rootHtml.replaceAll("#{childHtml}" , childHtml , true) ;
        that.append(rootHtml);
    }   

    return that;
};

function formatHtml( data , html ){

    for( var key in data ){
        var kValue = data[ key ] ;
        html = html.replaceAll("#{" + key +"}" , kValue );
    }
    return html ;
}

function childNav( childData ){
    var echoHtml = "" ;
    for( var childIndex in childData ){
        var child = childData[ childIndex ] ;
        var buttonHtml =  NAV_HTML["BUTTON"] ;
        buttonHtml = formatHtml( child , buttonHtml ) ;
        echoHtml += buttonHtml ;
    }
    return echoHtml ;
}
/*用户中心导航*/
function NavMenu(index) {
	var NavHTML = "<div class='user_nav'>";
	NavHTML += "<ul>";
	NavHTML += "<li><i class='nav_i1'></i><a href='/A00'>账户总览</a></li>";
	//NavHTML += "<li><i class='nav_i2'></i><a href='/A01'>投资管理</a><dl><dd><a href='/A01'>我的投资</a></dd><dd><a href='/A02'>债权转让</a></dd><dd><a href='/A05'>智投盈</a></dd><dd><a href='/A04'>自动投标</a></dd></dl></li>";
	NavHTML += "<li><i class='nav_i2'></i><a href='/A01'>投资管理</a><dl><dd><a href='/A01'>我的投资</a></dd><dd><a href='/A02'>债权转让</a></dd><dd><a href='/A04'>自动投标</a></dd></dl></li>";
	NavHTML += "<li><i class='nav_i3'></i><a href='/B01'>资金管理</a><dl><dd><a href='/B01'>资金详情</a></dd><dd><a href='/B05'>投资券</a></dd><dd><a href='/B02'>充值</a></dd><dd><a href='/B03' id='putForward'>提现</a></dd></dl></li>";
	NavHTML += "<li><i class='nav_i4'></i><a href='/C01'>账户管理</a><dl><dd><a href='/C01'>安全中心</a></dd><dd><a href='/K08' target='view_window'>积分商城</a></dd><dd><a href='/C04'>个人资料</a></dd></dl></li>";
	NavHTML += "<li><i class='nav_i5'></i><a href='/C03'>邀请奖励</a></li>";
	NavHTML += "</ul></div>";
	$('.user_index').append(NavHTML);
	$('.user_index .user_nav li').eq(index).addClass('selected');
}
$('body').delegate('.user_nav li','mouseover',function(){
	$(this).find('dl').show();
})
$('body').delegate('.user_nav li','mouseout',function(){
	$(this).find('dl').hide();
})	
//初始化用户中心菜单
$('body').delegate('.user-nav li','mouseover',function(){
	var index = $(this).index();
	$('.user-nav .user-nav-item i').css('top',50*index + 'px');
})
$('body').delegate('.user-nav li','mouseout',function(){
	$('.user-nav .user-nav-item i').css('top',50*window.userNavActive + 'px');
})

//切换卡
$.fn.tabChange = function( selector ){
    var that = $(this);
    that.click(function(){
        $(this).addClass('active').siblings().removeClass('active');
        $(this).addClass('red').siblings().removeClass('red');
        var index = $(this).index();
        $('.'+ selector+'-list').hide();
        $('.'+ selector+'-list:eq('+index+')').show();
        if(index==1 || index==2){
        	$("#tab-content2").next(".add_money").hide();
        }
        else{
        	$("#tab-content2").next(".add_money").show();
        }
        
        

    })
};

$.fn.tabChange2 = function( selector ){
    var that = $(this);
    that.click(function(){
        $(this).addClass('active').siblings().removeClass('active');
        var index = $(this).index();
        $('.tabNav i').animate({left:130*index + 'px'},300);
        $('.'+ selector+'-list').hide();
        $('.'+ selector+'-list:eq('+index+')').show();

    })
};

$(".tab-content2-list").eq(0).parents("#tab-content2").next(".add_money").show();
$(".tabNav li").tabChange( "tab-content");
$(".menu li").tabChange2( "tab-content");
$(".menu2 li").tabChange("tab-content2");
$(".transferNav li").tabChange( "Nav-content");//承接的债权

//NEW
$("#child_userTicket").append( '<i class="iconNew"></i>' );

$('#delCardBank').delegate('.card_del','click',function(){
	$('#yinying').hide();
	$('body,html').css('overflow', 'initial');
	$('#delCardBank').hide();
});

$('#addBankDialog .del').click(function(){
	$('#yinying').hide();
	$('body,html').css('overflow', 'initial');
	$('#addBankDialog').hide();
});

$('.list2 .tq').click(function(){
	to_center($('#yinying').show());
	$('body,html').css('overflow', 'hidden');
	to_center($('#tqhk').show());
});

$('#tqhk .del').click(function(){
	$('#yinying').hide();
	$('body,html').css('overflow', 'initial');
	$('#tqhk').hide();
});

$('.list2 .hk').click(function(){
	to_center($('#yinying').show());
	$('body,html').css('overflow', 'hidden');
	to_center($('#hk').show());
});

$('#hk .del').click(function(){
	$('#yinying').hide();
	$('body,html').css('overflow', 'initial');
	$('#hk').hide();
});
window.onload = function(){
	//用户中心左右齐平
	//我的帐户置红
	if( topNavSelect == "4"){
		$(".indexMyCount").addClass("login");	
	}
}

window.closeLoginDialog = function(){
	window.location.href = "/"; 
};
/*页面参数配置*/
function setting(item1,index){
	var uNav = window.uNav = [];
	if(item1 == 1){
		var root = {navName: "投资管理", navCode: "a1",isShow:false, list: []};
		root.list.push({navName: "我的投资", navCode: "a11", navUrl: CONFIG["ROOT"]+"/A01"});
		root.list.push({navName: "债权转让", navCode: "a12", navUrl: CONFIG["ROOT"]+"/A02"});
		//root.list.push({navName: "智投盈", navCode: "a14", navUrl: CONFIG["ROOT"]+"/A05"});
		root.list.push({navName: "自动投标", navCode: "a13", navUrl: CONFIG["ROOT"]+"/A04"});
	}else if(item1 == 2){
		var root = {navName: "资金管理", navCode: "a1",isShow:false, list: []};
		root.list.push({navName: "资金详情", navCode: "a12", navUrl: CONFIG["ROOT"]+"/B01"});
		root.list.push({navName: "投资券", navCode: "a11", navUrl: CONFIG["ROOT"]+"/B05"});
	    root.list.push({navName: "充值", navCode: "a13", navUrl: CONFIG["ROOT"]+"/B02"});
	    root.list.push({navName: "提现", navCode: "a14", navUrl: CONFIG["ROOT"]+"/B03"});
	   // root.list.push({navName: "银行卡管理", navCode: "a14", navUrl: CONFIG["ROOT"]+"/B04"});
	}else{
		var root = {navName: "账户管理", navCode: "a1",isShow:false, list: []};
	    root.list.push({navName: "安全中心", navCode: "a11", navUrl: CONFIG["ROOT"]+"/C01"});
//	    root.list.push({navName: "积分商城", navCode: "a12", navUrl: CONFIG["ROOT"]+"/C02"});
	    root.list.push({navName: "个人资料", navCode: "a13", navUrl: CONFIG["ROOT"]+"/C04"});
	}
	
	uNav.push(root);
	$(".user-nav").nav(uNav);
	$(".user-nav li").eq( index ).addClass("active");
	$('.user-nav .user-nav-item i').css('top',index*50 + 'px');
}
//首页日历
var date=new Date();
var now_y=date.getFullYear();
var now_m=date.getMonth() + 1;
var now_d=date.getDate();
var today_date=parseInt(format_time(now_y,now_m,now_d));


//日期格式化
function format_time(first,second,third){
	if(second < 10){
		second='0' + second;
	}else{
		second=second.toString();
	}
	if(third < 10){
		third='0' +third;
	}else{
		third=third.toString();
	}
	var time=first + second + third;
	return time;
}

/*用户中心头部滚动条*/
$(window).scroll(function() {
	var window_height=$(window).height();
	var rel_height = window_height - 378;
	if ($(window).scrollTop() > 400) {
		$('#suspension').css('display','none');
		$('.user_index').addClass('userFixed');
		$('.user-main-left').addClass('leftFixed').css('height',rel_height+'px');
	} else {
		$('#user_nav').removeClass('scroll_down').addClass('scroll_up').fadeOut();
		$('.user_index').removeClass('userFixed');
		$('.user-main-left').removeClass('leftFixed').css('height','705px');
	}
});
