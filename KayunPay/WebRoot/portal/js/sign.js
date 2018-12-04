function Calendar(id)
{
	this.oSign = document.getElementById(id);
	this.oSignBtn = getByClassName(this.oSign, 'gain')[0];
	this.oCalendarBox = getByClassName(this.oSign, 'boxinter')[0];
	this.oList = document.getElementById('integralBox');
	this.iNow = 0;
	
	this.bClick = true;
	
	this.bSign = false;
	
	this.aSignDay = [];
	this.nSignDay = [];
	
	//创建日历
	this.createCalendar();
	
	//一进入页面检测签到
	this.checkSign();
	
	//签到
	this.sign();
}
//设置礼物title
Calendar.prototype.setTitle = function()
{
	this.aGiftList = getByClassName(this.oSign, 'date-signgift');
	for(var i = 0; i < this.aGiftList.length; i++)
	{
		this.aGiftList[i].title = '累计签到有惊喜！';
	}
}
//设置当天签到title
Calendar.prototype.setTodayTitle = function()
{
	this.oSignToday = getByClassName(this.oSign, 'sign-y')[0];
	if(this.oSignToday)
	{
		this.oSignToday.title = '立即签到！';
	}
}
//删除当天签到title
Calendar.prototype.removeTodayTitle = function(obj)
{
	obj.title = '';
}

//创建月历
Calendar.prototype.createCalendar = function()
{
	//当月时间
	var iDays = getDays(this.iNow);
	for(var i = 0; i < iDays; i++)
	{
		var oSpan = document.createElement('span');
		oSpan.className = 'normalDays';
		oSpan.innerHTML = i + 1;
		this.oCalendarBox.appendChild(oSpan);
	}
	
	//上月时间
	var iFirstDay = getFirst(this.iNow);  //0-6
	var iDaysLast = getDays(this.iNow - 1);
	for(var i = 0; i < iFirstDay; i++)
	{
		var oSpan = document.createElement('span');
		oSpan.innerHTML = iDaysLast--;
		oSpan.className = 'date-g';
		this.oCalendarBox.insertBefore(oSpan, this.oCalendarBox.children[0]);
	}
	
	//下月时间
	var iDaysNext = 35 - iFirstDay - iDays
	for(var i = 0; i < iDaysNext; i++)
	{
		var oSpan = document.createElement('span');
		oSpan.innerHTML = i + 1;
		oSpan.className = 'date-g';
		this.oCalendarBox.appendChild(oSpan);
	}
	
	//今天时间
	this.aDays = getByClassName(this.oCalendarBox, 'normalDays');
	var oDate = new Date();
	var iToday = oDate.getDate();
	
	for(var i = 0; i < this.aDays.length; i++)
	{
		if(this.aDays[i].innerHTML == iToday)
		{
			//今天
			addClass(this.aDays[i], 'sign-y');
		}
	}
}

//一进入页面检测签到
Calendar.prototype.checkSign = function()
{
	var This = this;
	YRHX.ajax({
		url: CONFIG.getRequestURI("signHistory"),
		data: {
			"userCode": YRHX.Cookie("userCode").get()
		},
		success: function(data){
			//设置已经签到的日子
			This.aSignDay = data["signDays"];
			This.setHasSignDay();
			// 设置未签到的日子
			This.nSignDay = data["notSignDays"];
			This.setNotSignDay();
		}	
	});
	this.oList.onmouseover = function()
	{
		//检查今天是否签到
		This.checkSignForToday();
		//连续签到10天显示礼物
//		This.setGiftForTenSign();
		//连续签到25天显示礼物
//		This.setGiftForTwentyFiveSign();
		//设置礼物title
//		This.setTitle();
		//设置今天没签到时title
		This.setTodayTitle();
		$(".integral-con").css("display", "table");
	}
	this.oList.onmouseout = function() {
		$(".integral-con").css("display", "none");
	}
}
//设置本月已经签到的日子
Calendar.prototype.setHasSignDay = function()
{
	for(var i = 0; i < this.aDays.length; i++)
	{
		for(var j = 0; j < this.aSignDay.length; j++) {
			if(this.aDays[i].innerHTML == this.aSignDay[j]) {
				addClass(this.aDays[i], 'date-lb');
				break;	
			} 
		}
	}
}
// 设置本月未签到的日子
Calendar.prototype.setNotSignDay = function() {
	for(var i = 0; i < this.aDays.length; i++) {
		for(var j = 0; j < this.nSignDay.length; j++) {
			if(this.aDays[i].innerHTML == this.nSignDay[j]) {
				addClass(this.aDays[i], 'date-g');
				break;
			}
		}
	}
}
//设置礼物title
Calendar.prototype.setTitle = function()
{
	this.aGiftList = getByClassName(this.oSign, 'date-signgift');
	for(var i = 0; i < this.aGiftList.length; i++)
	{
		this.aGiftList[i].title = '累计签到有惊喜！';
	}
}

//当天已经签到了，不能再签到
Calendar.prototype.checkSignForToday = function()
{
	if(this.bSign)
	{
		var oDate = new Date();
		var iToday = oDate.getDate();
		
		for(var i = 0; i < this.aDays.length; i++)
		{
			if(hasClass(this.aDays[i], 'sign-y'))
			{
				this.oList.innerHTML = '已签到';
				removeClass(this.aDays[i], 'sign-y');
				addClass(this.aDays[i], 'date-lb');
				this.bClick = false;
			}
		}	
	}
}

//十次签到后显示礼物
Calendar.prototype.setGiftForTenSign = function()
{
	var oDate = new Date();
	var iToday = oDate.getDate();
	
	if(this.aSignDay.length == 0)
	{
		var iCount = iToday + 9 - 1;
		//大于等于这个月的天数
		if(iCount >= getDays(this.iNow))
		{
			return;
		}
		this.aDays[iCount].innerHTML = '';
		addClass(this.aDays[iCount], 'date-signgift');	
	}
	else
	{
		if(this.aSignDay.length >= 10)
		{
			this.aDays[this.aSignDay[9] - 1].innerHTML = '';
			addClass(this.aDays[this.aSignDay[9] - 1], 'date-signgift');
		}
		else
		{
			if(!this.bSign)
			{
				var num = this.aSignDay[this.aSignDay.length - 1];
				var iCount = iToday - this.aSignDay.length + 8;
			}
			else
			{
				var num = this.aSignDay[this.aSignDay.length - 1];
				var iCount = (num - this.aSignDay.length) + 9;
				
			}
			//大于等于这个月的天数
			if(iCount >= getDays(this.iNow))
			{
				return;
			}
			this.aDays[iCount].innerHTML = '';
			addClass(this.aDays[iCount], 'date-signgift');
		}
	}
}

//25次签到后显示礼物
Calendar.prototype.setGiftForTwentyFiveSign = function()
{
	var oDate = new Date();
	var iToday = oDate.getDate();
	
	if(this.aSignDay.length == 0)
	{
		var iCount = iToday + 24 - 1;
		//大于等于这个月的天数
		if(iCount >= getDays(this.iNow))
		{
			return;
		}
		this.aDays[iCount].innerHTML = '';
		addClass(this.aDays[iCount], 'date-signgift');	
	}
	else
	{
		if(this.aSignDay.length >= 25)
		{
			this.aDays[this.aSignDay[24] - 1].innerHTML = '';
			addClass(this.aDays[this.aSignDay[24] - 1], 'date-signgift');
		}
		else
		{
			if(!this.bSign)
			{
				var num = this.aSignDay[this.aSignDay.length - 1];
				//var iCount = (num - this.aSignDay.length) + 24 + (iToday - num - 1);
				var iCount = iToday - this.aSignDay.length + 23;
			}
			else
			{
				var num = this.aSignDay[this.aSignDay.length - 1];
				var iCount = (num - this.aSignDay.length) + 24;
			}
			//大于等于这个月的天数
			if(iCount >= getDays(this.iNow))
			{
				return;
			}
			this.aDays[iCount].innerHTML = '';
			addClass(this.aDays[iCount], 'date-signgift');
			addClass(this.aDays[iCount], 'signgift25');
		}
	}
}
function getByClassName(oParent, sClass)
{
	if(document.getElementsByClassName)
	{
		return document.getElementsByClassName(sClass);
	}
	var aEle = document.getElementsByTagName('*');
	var aResult = [];
	var re = new RegExp('\\b' + sClass + '\\b', 'i');
	for(var i = 0; i < aEle.length; i++)
	{
		if(re.test(aEle[i].className))
		{
			aResult.push(aEle[i]);
		}
	}
	return aResult;
}
//本月有多少天
function getDays(iNow)
{
	var oDate = new Date();
  oDate.setDate(1);
	oDate.setMonth(oDate.getMonth() + iNow);
	oDate.setMonth(oDate.getMonth() + 1);
	oDate.setDate(0);
	return oDate.getDate();	
}
//本月第一天为星期几
function getFirst(iNow)
{
	var oDate = new Date();
	oDate.setMonth(oDate.getMonth() + iNow);
	oDate.setDate(1);
	return oDate.getDay();	//0-6
}
//添加class
function addClass(obj, sClass)
{ 
var aClass = obj.className.split(' ');
if (!obj.className)
	{
    obj.className = sClass;
    return;
}
for (var i = 0; i < aClass.length; i++)
	{
    if (aClass[i] === sClass) return;
}
obj.className += ' ' + sClass;
}

//签到
Calendar.prototype.sign = function()
{
//	var This = this;
//	for(var i = 0; i < this.aDays.length; i++)
//	{
//		if(hasClass(this.aDays[i], 'sign-y'))
//		{
//			this.aDays[i].onclick = function()
//			{
//				var That = this;
//				if(This.bClick)
//				{
//					var sCash;
//					if(hasClass(this, 'date-signgift'))
//					{
//						sCash = 30;
//					}
//					else
//					{
//						sCash = 15;
//					}
//					
//					//后台请求，成功后执行下面操作
//					$.ajax({
//			            type: "POST",
//			            async: false,
//			            url: basePath+"accessSign4Jsonp.act?paramMap.channel=1",
//			            dataType: "jsonp",
//			            jsonp: "callback",//传递给请求处理程序或页面的，用以获得jsonp回调函数名的参数名(一般默认为:callback)
//			            jsonpCallback:"accessSignJsonpCBF",//
//			            success: function(data){
////			            	accessSignJsonpCBF('{"code":"200","curExp":"26","curGrade":"普通会员","curPoint":"26","gift":"","point":"5","signNumOfMonth":"0"}')
//			            	var retInfo = $.parseJSON( data);
//			            	if(retInfo.code ==200){
//			            		sCash = retInfo.point ;
//			            		
//								var oEm = document.createElement('em');
//								oEm.className = 'praiseicon';
//								oEm.innerHTML = '+' + sCash;
//								That.appendChild(oEm);
//								startMove(oEm, {top: -40, fontSize:20, opacity:0}, {time:2000, fn:function(oEm){
//									That.removeChild(oEm);	
//								}});
//								removeClass(That, 'sign-y');
//								addClass(That, 'date-lb');
//								This.oList.innerHTML = '已签到';
//								//删除今天没签到时title
//								This.removeTodayTitle(That);
//								
//								if(hasClass(That, 'date-signgift') && hasClass(this, 'signgift25'))
//								{
//									oWindowSignGift25.showWindow();
//								}
//								
//								This.bClick = false;
//			            	}else if(retInfo.code ==300 ){
//			            		window.location.href = basePath + "login.html";
//			            	}
//			            }
//					})
//
//				}
//			}
//		}	
//	}
}

//判断是否有某个class
function hasClass(obj, sClass)
{
	var re = new RegExp('\\b' + sClass + '\\b');
	if(re.test(obj.className))
	{
		return true;
	}	
	return false;
}
