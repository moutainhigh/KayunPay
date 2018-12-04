(function(){

    var usNav = [];
    usNav.push({navName: "公司简介", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_01"});
    usNav.push({navName: "经营范围", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_02"});
    usNav.push({navName: "企业理念", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_03"});
    //usNav.push({navName: "企业文化", navClass: "a121", navUrl: CONFIG["ROOT"]+"/Y06_03"});
    //usNav.push({navName: "社会责任", navClass: "a11", navUrl: "Z04_04"});
    usNav.push({navName: "高管团队", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_04"});
    usNav.push({navName: "董事长致辞", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_05"});
    usNav.push({navName: "平台成长记录", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_06"});
    usNav.push({navName: "战略合作", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_07"});
    //usNav.push({navName: "分公司介绍", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_08"});
	//usNav.push({navName: "公司培训", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_08_1"});
	//usNav.push({navName: "专家顾问", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_07"});
    usNav.push({navName: "招贤纳士", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_08"});
    usNav.push({navName: "联系我们", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Y06_09"});
    
    var newsNav = [];
    newsNav.push({navName: "最新公告", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X01"});
    newsNav.push({navName: "公司新闻", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X02"});
    newsNav.push({navName: "行业动态", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X03"});
    newsNav.push({navName: "媒体报道", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X04"});
    //newsNav.push({navName: "易融月刊", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X05"});
    newsNav.push({navName: "往期专题", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X06"});
    
    var helpNav = [];
    helpNav.push({navName: "注册登录", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K01"});
    helpNav.push({navName: "实名认证", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K02"});
    helpNav.push({navName: "充值提现", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K03"});
    helpNav.push({navName: "投资回款", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K04"});
    helpNav.push({navName: "会员积分", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K05"});
    helpNav.push({navName: "安全保障", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K06"});
    helpNav.push({navName: "名词解释", navClass: "a11", navUrl: CONFIG["ROOT"]+"/K07"});
    
    var infoNav = [];
    infoNav.push({navName: "合规之路", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message01"});
    infoNav.push({navName: "银行存管", navClass: "a11", navUrl: CONFIG["ROOT"]+"http://topic.yrhx.com/zt/2018/20180521/"});
    infoNav.push({navName: "资质证书", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message03"});
    infoNav.push({navName: "组织信息", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message04"});
    infoNav.push({navName: "运营报告", navClass: "a11", navUrl: CONFIG["ROOT"]+"/X09"});
    infoNav.push({navName: "审计信息", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message06"});
    infoNav.push({navName: "风险控制", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message07"});
    infoNav.push({navName: "监管法规", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message08"});
    infoNav.push({navName: "重大事项", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message09"});
    infoNav.push({navName: "承诺书", navClass: "a11", navUrl: CONFIG["ROOT"]+"/Message10"});
    
    var NAV_HTML = "<a href='#{navUrl}' class='#{navClass}'>#{navName}</a>";
    //var NAV_HTML2 = "<a href='#{navUrl}' class='#{navClass}' style='display:none;'>#{navName}</a>";
    
    $.fn.nav = function (data) {
        var that = this;
        for (var navIndex in data) {
            var navRow = data[navIndex];
            if(navRow["navClass"].length>3){
            	var navHtml = formatHtml( navRow , NAV_HTML2 ) ;
            }else{
            var navHtml = formatHtml( navRow , NAV_HTML ) ;}
           
			if(navRow.navName == $('.aboutUs-main-title span').html()){
				navHtml = $(navHtml).addClass('active');
			}
            that.append(navHtml);        
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
 
    $("#aboutUs-nav").nav( usNav );
    $("#news-nav").nav( newsNav );
    $("#help-nav").nav( helpNav );
    $("#info-nav").nav( infoNav );
}());

var now_index = location.pathname.substr(-1,1) -1;
$('.aboutUs-nav .ico').css('top',50*now_index + 'px');

$('body').delegate('.aboutUs-nav a','mouseover',function(){
	var index = $(this).index();
	$('.aboutUs-nav .ico').css('top',50*(index-1) + 'px');
})

$('body').delegate('.aboutUs-nav a','mouseout',function(){
	$('.aboutUs-nav .ico').css('top',50*now_index + 'px');
})

$("#info-nav a").eq(1).attr("target","_blank");
$("#info-nav a").eq(6).attr("target","_blank");



//合作伙伴公司信息
var data=[
	{province:'云南', cityList:[		
		{	name:'昆明', 
			address:'云南省昆明市西山区老海埂路云纺东南亚珠宝中心B座1504',
			img:'/portal/images/4now/partner13.jpg'
		},	   
		{	name:'楚雄',
			address:'云南省楚雄州楚雄市鹿城镇鹿城北路鑫茂时代雅居808',
			img:'/portal/images/4now/partner15.jpg'
		}	
	]},
	{
	province:'湖北',cityList:[
		{	name:'汉街',
			address:'汉街总部国际E座1907',
			img:'/portal/images/4now/partner17.jpg',
		}
	]},
	{
	province:'贵州',cityList:[
		{	name:'贵阳',
			address:'贵州省贵阳市南明区花果园金融大厦2403A',
			img:'/portal/images/4now/partner18.jpg'
		},
		{
			name:'遵义',
			address:'贵州省遵义市汇川区大连路航天大厦607',
			img:'/portal/images/4now/partner19.jpg'
		},
		{	name:'铜仁',
			address:'贵州省铜仁市碧江区南长城路金滩半岛豪苑C栋二单元19楼',
			img:'/portal/images/4now/partner20.jpg'
		},
	]},
	
	{
	province:'新疆',cityList:[
		{	name:'乌鲁木齐一店',
			address:'乌鲁木齐经济技术开发区万寿山街顺德创业大厦6楼',
			img:'/portal/images/4now/partner21.jpg'
		},
		{	name:'乌鲁木齐二店',
			address:'乌鲁木齐新市区北京南路大寨沟康源财富中心803',
			img:'/portal/images/4now/partner22.jpg'
		},
		{	name:'乌鲁木齐三店',
			address:'乌鲁木齐天山区人民路2号乌鲁木齐大厦12楼',
			img:'/portal/images/4now/partner26.jpg'
		},
		{	name:'伊犁',
			address:'伊宁市新华西路705号融和大厦B座1625室',
			img:'/portal/images/4now/partner23.jpg'
		},
		{	name:'克拉玛依',
			address:'克拉玛依市克拉玛依区恒隆广场B座-507室',
			img:'/portal/images/4now/partner25.jpg'
		}
	]},
	{
	province:'湖南',cityList:[
		{	name:'娄底',
			address:'娄底市娄星区乐坪大道锦洋大厦9楼',
			img:'/portal/images/4now/partner23.jpg'
		}
	]},
	{
	province:'安徽',cityList:[
	    {	name:'安庆',
		    address:'安徽省安庆市迎江区绿地紫峰大厦A座901-903',
		    img:'/portal/images/4now/partner24.jpg'
	    }
	]},
	{
		province:'江西',cityList:[
		    {	name:'九江',
			    address:'西省九江市九江经济技术开发区九瑞大道九龙世纪城小区1号楼龙德大厦9F',
			    img:'/portal/images/4now/partner23.jpg'
		    }
		]}

];

function load_data(data,index){
	var mes_list=data[index].cityList;
	city_list.html('');
	$('.filiale_con ul').html('');
	for(var i=0 ;i < mes_list.length; i++){
		var liEle=$('<li>').html(mes_list[i].name);
		liEle.appendTo(city_list);
		var rightHtml="<li><img src="+mes_list[i].img+">";
		rightHtml +="<div class='filiale_mes'><strong>"+mes_list[i].name+"</strong>" ;
		rightHtml +="<div class='filiale_detail'>";
		rightHtml +="<p>"+mes_list[i].address+"</p>";
		rightHtml +="</div>";
		rightHtml +="</div>";
		rightHtml +="</li>";
		$('.filiale_con ul').append(rightHtml);
		
	}
	city_list.find('li').eq(0).addClass('cur');
	init_yinying();
	$('body,html').css('overflow', 'hidden');
	to_center($('#yinying').show());
	to_center(filiale_dialog.show());
}

$("body").delegate(".filiale_city li","click",function () {
	var index=$(this).index();
	$(this).addClass('cur').siblings().removeClass('cur');
	$('.filiale_con li').eq(index).show().siblings().hide();
});

$('body').delegate('.list_q','click',function(){
	var that=$(this);
	if(that.hasClass('active')){
		$(this).removeClass('active');
	}else{
		that.addClass('active');
	}
	that.next('.list_a').toggle('slow')
})

$(window).scroll(function() {
	var window_height=$(window).height();
	var rel_height = window_height - 358;
	if ($(window).scrollTop() > 400) {
		$('.aboutUs-main-left').addClass('leftFixed').css('height',rel_height+'px');
	} else {
		$('.aboutUs-main-left').removeClass('leftFixed').css('height','700px');
	}
});

	
	