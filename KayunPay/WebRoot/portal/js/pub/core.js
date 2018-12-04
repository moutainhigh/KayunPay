//                            _ooOoo_  
//                           o8888888o  
//                           88" . "88  
//                           (| -_- |)  
//                            O\ = /O  
//                        ____/`---'\____  
//                      .   ' \\| |// `.  
//                       / \\||| : |||// \  
//                     / _||||| -:- |||||- \  
//                       | | \\\ - /// | |  
//                     | \_| ''\---/'' | |  
//                      \ .-\__ `-` ___/-. /  
//                   ___`. .' /--.--\ `. . __  
//                ."" '< `.___\_<|>_/___.' >'"".  
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |  
//                 \ \ `-. \_ __\ /__ _/ .-` / /  
//         ======`-.____`-.___\_____/___.-`____.-'======  
//                            `=---='  
//         .............................................  
//                  佛祖保佑             永无BUG 
/**
 * 	业务类
 * 
 */
(function(){


	/**
	 * 	实际使用时，需要重写该函数
	 * 
	 */
	window.reAuth = function(){
		YRHX.Cookie().clear();
		
		if( !$("body").hasClass("indexPage") ){
			show_login_dialog();	
		}
		/*$.popTips("popTipErr","未登录或者登录超时,重新登录!",function(){
			window.location.href = CONFIG["ROOT"] + "/login" ;
		});*/
	};
	
		var YRHX = window.YRHX = {} ;
	 
	/**
	 * 	控制台输出接口
	 */
	YRHX.console = function( str , level ){
		if( window["ISDEBUG"] == true ){
			level = level?level:"info" ;
			console[level]( str );
		}
	};
	
	YRHX.alert = function(str ){
		if( typeof(str) == "object"){
			str = JSON.stringify( str );
		}
		alert(str) ;
	}
	
	//init js
	/**
	 * 	初始化服务
	 * 		jsArr Array	需要动态加在的js集合，仅包含摇TV部分		
	 * 		cb			回调函数
	 */
	YRHX.init = function( jsArr , cb ){
		//$.getScript( url , cb );
		var urlStr = window["YAOTV-CONCAT-URL"] ;
		var pType = typeof( jsArr );
		if( pType == "string")
			urlStr += window["CONCAT-ARRAY"][ jsArr ] ;
		else{
			for(var kIndex in jsArr ){
				var reUrl = window["CONCAT-ARRAY"][ jsArr[kIndex] ] ;
				if( reUrl ){
					reUrl = reUrl.replace( window["YAOTV-CONCAT_PRE"] ,"") ;
					urlStr += reUrl + ",";
				}
			}
		}
		
		//get script 
		var script = document.createElement("script");
		script.setAttribute("type","text/javascript");
		script.onload = cb ;
		script.setAttribute( "src" , urlStr.substring( 0 , urlStr.length -1 ) ); 
		document.getElementsByTagName("head").item(0).appendChild( script );
	};
	
	/**
	 * 	注册服务参数
	 * 		true  - 注册成功
	 * 		false - 注册失败
	 */
	YRHX.regist = function( obj ){
		if( !obj )
			return false ;
		for(var key in obj ){
			YRHX[ key ] = obj[ key ] ;
		}
		return true ;
	};
	
	/**
	 * start--$.util.queryString 从地址栏根据key获取value
	 * 
	 * */
	YRHX.queryString = function(item){
		var svalue = location.search.match(new RegExp("[\?\&]" + item + "=([^\&]*)(\&?)","i"));
		return svalue ? svalue[1] : svalue;
	};
	
	/**
	 * 	Cookie
	 * 
	 */
	YRHX.Cookie = function( key , value , expireMin ){
		this.key = key + "_" + ( window.cookieMark||Math.floor(new Date().getTime()/1000/60/60/24) );
		this.expireMin = expireMin||-1 ;
		if( typeof value == "object"  )
			this.value = JSON.stringify( value );
		else
			this.value = value ;

		/*
		YRHX.console("Cookie-Key：" + this.key );
		YRHX.console("Cookie-Value：" + this.value );
		YRHX.console("Cookie-expireMin：" + this.expireMin );
		*/
		
		//覆盖原来的值
		this.set = function(){
			YRHX.console("Cookie->Set: " + this.key + "  " + this.value );
			this.del();	//delete
			var cookieString = this.key + "=" + encodeURIComponent(this.value);
	        if (expireMin > 0) {
	            var date = new Date();
	            date.setTime(date.getTime() + expireMin * 60 * 1000);
	            cookieString = cookieString + "; expire=" + date.toGMTString();
	        }
	        document.cookie = cookieString;
		};
		
		this.del = function(){
			YRHX.console("Cookie->Del: " + this.key );
			if( this.get() != null ){
				//this.set(this.key, "", -1);
				document.cookie = this.key + '=0;expires=' + new Date(0).toUTCString()
			}
		};
		
		this.get = function(){
	        var strCookie = document.cookie;
	        var arrCookie = strCookie.split(";");
	        var cookieValue = null  ;
	        for (var i = 0; i < arrCookie.length; i++) {
	            var arr = arrCookie[i].split("=");
	            if ( $.trim( arr[0] ) == $.trim( this.key )){
	            	cookieValue =  decodeURIComponent(arr[1]);
	            	if(!isNaN(cookieValue)){
	            		break;
	            	}
	            	try{
	            		cookieValue = JSON.parse( cookieValue ) ;
	            	}catch(e){
	            		// not object
	            	}
	            	break ;
	            }
	        }
			YRHX.console("Cookie->Get : "+ cookieValue );
	        return cookieValue ;
		} ;
		
		this.clear = function(){
			var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
	        if ( keys ) {
	        	//console.log(keys);
	            for (var i = keys.length; i--;)
	                document.cookie = keys[i] + '=0;expires=' + new Date(0).toUTCString()
	        }
		};
		
		return this ;
	};
	
	var tokenCookieName = "aiisxx01" ;
	function saveToken( token ){
		var cookieObj = YRHX.Cookie( tokenCookieName , token , 0) ;
		cookieObj.del();
		cookieObj.set();
	}

	function getToken(){
		var tokenCookie = YRHX.Cookie(tokenCookieName );
		return tokenCookie.get() ;
	}
	
	/**
	 * 	重写Ajax，支持jsonp
	 */
	YRHX.ajax = function( opt ){
		
		var tokenName = "token" ;
		var returnCodeKey = "return_code" ;
		var returnCodeValue = "00" ;
		var returnDataKey  = "return_data" ;
		var returnInfoKey = "return_info" ;
		
		YRHX.console("Request-URL:" + opt.url ); 
		//opt.async = opt.async || true ;
		opt.timeout = opt.timeout || (10*1000) ;

//		if( opt.url.indexOf("?") < 0 )
//			opt.url += "?" ;
		
		//TODO Get Token From Cookie
		//if( window[tokenName] )
		//	opt.url += (tokenName + "=" + window[tokenName] + "&");
		/*
		if( opt.type.toUpperCase == "GET" ){ 
			if( typeof( opt.data ) == "object" ){
				var reqData = opt.data ;
				for( var key in reqData ){
					YRHX.console("Key=" + key +"  value=" + reqData[ key ]);
					if( reqData[ key ] && (reqData[ key ]+"").length >  0  ){
						opt.url += key + "=" + encodeURIComponent(encodeURIComponent(reqData[key])) + "&";
						YRHX.console("Request-Data : [ " + key + " = " + reqData[key] + "]" ); 
					}
				}
				opt.url = opt.url.substr(0 , opt.url.length -1 );
			}
		}*/
		$.ajax({ 
			url : opt.url ,
			async : opt.async ,
			type : opt.type || "post" ,
			dataType : opt.dataType||"json" ,	//default jsonp
			scriptCharset : "UTF-8" ,
			data : opt.data ,
			timeout : opt.timeout , 
			headers : {
				//"token" : getToken() 
			},
			beforeSend : function(){
				if( typeof ( opt.beforeSend ) == "function" ){
					opt.beforeSend();
				}
				//before
			},
			success : function( sucData ){
				
//				if( sucData[ tokenName ] ){
//					
//					window[ tokenName ] = sucData[ tokenName ] ;
//					saveToken( sucData[ tokenName ] );
//				}
				
				//check return_code
				if( sucData[ returnCodeKey  ] == returnCodeValue ){
					//suc
					//var returnData = JSON.parse( sucData[ $.util["RETURNDATA"] ] );
					var returnData = sucData[ returnDataKey ];
					if( opt.success )
						opt.success.call( this , returnData ) ;
				}else{
					
					if( sucData[ returnCodeKey ] == "AA" || sucData[ returnCodeKey ] == "AB"){
						window.reAuth();
						
					}else{
						
						//error
						if( opt.error ){
							opt.error.call( this , sucData );
							//opt.error();
						}else{
							//alert("Return_Code:" + sucData[ $.util["RETURNCODE"] ]+ "\n" + "Return_Info:" + sucData[ $.util["RETURNINFO"] ]);
						}
					}
					
				}
			},
			error : function( errData ){
				//error 
				if( opt.error ){
					opt.error.call( this , errData ) ;
					//opt.error() ;
				}else{
					
				}
			},
			complete : function(data){
				if( typeof ( opt.complete ) == "function" ){
					opt.complete();
				}
			}
		}) ;
		
	};
	
	/*运算工具*/
	YRHX.toDecimal2 = function(s,p) {    
		if( s < 0 ){
			return (p||"")+"0.00";
		}else{
			var n = 2 ;
			s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + ""; 
			var l = s.split(".")[0].split("").reverse(), 
			r = s.split(".")[1]; 
			t = ""; 
			for(i = 0; i < l.length; i ++ ) 
			{ 
			t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : ""); 
			} 
			return (p||"")+t.split("").reverse().join("") + "." + r;   
		}
    }
	
	YRHX.toDecimal3 = function(s,p) {    
		if( s < 0 ){
			return (p||"")+"0.0";
		}else{
			var n = 1 ;
			s = parseFloat((s + "").replace(/[^\d\.-]/g, "")).toFixed(n) + ""; 
			var l = s.split(".")[0].split("").reverse(), 
			r = s.split(".")[1]; 
			t = ""; 
			for(i = 0; i < l.length; i ++ ) 
			{ 
			t += l[i] + ((i + 1) % 3 == 0 && (i + 1) != l.length ? "," : ""); 
			} 
			return (p||"")+t.split("").reverse().join("") + "." + r;   
		}
    }
	

    $.fn.ajaxLoading = function( ){
        var self = $(this);
        if( !$("div").hasClass("ajaxLoading") ){
        	self.append('<div class="ajaxLoading"><div class="rotateBox"><img src="/portal/images/loadingPic.gif" alt="加载中..." width="20"></div></div>');	
        }
        
    };

    $.fn.ajaxDone = function(){
        var self = $(this);
        $(".ajaxLoading").remove();
    };
    
	String.prototype.replaceAll = function(s1,s2,notSafe){
		if( !notSafe ){
			if( typeof(s2) == "string" ){
				s2 = s2.replace(new RegExp("<","gm"),"&lt;");
				s2 = s2.replace(new RegExp(">","gm"),"&gt;");
				s2 = s2.replace(new RegExp("\"","gm"),"&quot;");
			}
		}
		return this.replace(new RegExp(s1,"gm"),s2);
	};
    
    String.prototype.makeHtml = function( obj ){
    	if( !obj )
    		obj = {} ;
    	var temp = this ;
    	for( var key in obj ){
    		
    		temp = temp.replaceAll( "#{" + key +"}" , typeof( obj[key] ) != 'undefined'? obj[key]:"" );
    	}
    	return temp ;
    };
    
    String.prototype.dateformat = function(){
    	var temp = this ;
    	var returnVal = "" ;
    	if( this.length == 6 ){
    		//temp = "00000000" + this ;
    		returnVal += temp.substring(0,2);
    		returnVal += ":" ;
    		returnVal += temp.substring(2,4);
    		returnVal += ":" ;
    		returnVal += temp.substring(4,6);
    	}
    	if( this.length == 8){
    		//temp = this + "000000";
    		returnVal = temp.substring(0,4);
    		returnVal += "-" ;
    		returnVal += temp.substring(4,6);
    		returnVal += "-" ;
    		returnVal += temp.substring(6,8);
    	}
    	if( this.length == 14){
    		returnVal = temp.substring(0,4);
    		returnVal += "-" ;
    		returnVal += temp.substring(4,6);
    		returnVal += "-" ;
    		returnVal += temp.substring(6,8);
    		returnVal += " " ;
    		returnVal += temp.substring(8,10);
    		returnVal += ":" ;
    		returnVal += temp.substring(10,12);
    		returnVal += ":" ;
    		returnVal += temp.substring(12,14);
    	}
    	return returnVal ;
    };
    
    YRHX.checkLogin = function(){
    	var token = getToken();
    	if( token == null || token == "" ){
    		window.reAuth() ;
    	}
    };
    
    
	//获取两时间相差月份
    YRHX.getMonthNumber = function(date1, date2) {
	   //默认格式为"20030303",根据自己需要改格式和方法
	   var year1 = date1.substr(0, 4);
	   var year2 = date2.substr(0, 4);
	   var month1 = date1.substr(5, 2);
	   var month2 = date2.substr(5, 2);
	   var len = (year2 - year1) * 12 + (month2 - month1);
	
	   var day = date2.substr(8, 2) - date1.substr(8, 2);
	   if (day > 0) {
	       len += 1;
	   } 
	   else if (day < 0) 
	   {
	       len -= 1;
	   }
	   return len;
	 }
    
    $.popTips = function( type, text, callBack , delay ,obj){
    	if(!$("div").hasClass("popTips")){
    		$("body").append("<div class='popTips'><section class='popTips-section "+type+"'><p></p></section></div>");
    		$(".popTips p ").text(text);
    		$(".popTips").stop().show();
    		setTimeout(function() {
    			$(".popTips").fadeOut();
    			if( typeof( callBack ) == "function"){
    				callBack(obj);
    			}
    			setTimeout(function() {
    				$(".popTips").remove();
    			}, delay-1000 || 500 )
    		},delay || 1500);
    	}
    };
    
    $.fn.popUp = function( type, text, callBack, delay ){
    	var self = $(this);
    	self.parent().css("position","relative");
    	if( !$("div").hasClass("popOver") ){
	    	var outPut = '<div class="popOver">'+
		    				 '<div class="popArrow"></div>'+
		    				 '<div class="popContent">'+ text +'</div>'+
	    				 '</div>';
	    	self.before( outPut );
	    	$(".popOver").show();
	    	var marginWidth = ( self.width() - $(".popOver").width() ) / 2 + 5 ;
	    	$(".popOver").css({
	    		"left":  marginWidth +"px"
	    	});
	
	    	setTimeout(function(){
	    		$(".popOver").remove();
				if( typeof callBack == "function"){
					callBack();
				}
	    	},delay||2000)
    	}
    	
    };
    
    
    /**
	 * 发送短信
	 *  obj.msgCounter //倒计时显示容器
	 *  obj.msgTips    //发送短信手机号显示容器
	 *  obj.type       //发送短信为2中类型 
	 *  obj.urPhone    //电话号码 
	 *  obj.url	    //请求接口 
	 */
	$.fn.sendMsg = function( obj, callBack ){
		var $msgCounter = obj["msgCounter"]; 
		var $msgTips    = obj["msgTips"];
		var type        = obj["type"];
		var urPhone     = obj["urPhone"];
		var url         = obj["url"];
		var cv         = obj["cv"];
		
		var that = $(this);
		if( !that.hasClass("disabled") ){
		
			YRHX.ajax({
				url : CONFIG.getRequestURI( url ),
				data:{
					type: type,
					mobile: urPhone,
					cv   : cv
				},
				success : function( sucData ) {
					//that.attr("sendToggle","1");
					that.addClass("disabled");
					if( sucData ){ //TODO
						var sendPhone = sucData.substr("0","3")+"****"+sucData.substr("7","4");
						var interval = setInterval(function(){
							var time = $($msgCounter).text() || 60;
							
							if(time <= 0){
								$( $msgCounter ).text("");
								clearInterval(interval);
								that.removeClass("disabled");
								$( $msgTips ).text("");
							}else{
								$( $msgCounter ).css({
									"text-align":"left",
									"color":"red"
								})
								$( $msgCounter ).text(time - 1);
							}
						},1000);
					} 
					$( $msgTips ).addClass("red").text("短信已经发送到手机：" + sendPhone);
					if( typeof callBack == "function"){
						callBack();
					}
					$("#yinyingTop").hide();
					$(".confirm_pay").hide();
					$('body,html').css('overflow','auto');
				},
				error : function( errorSendMsgData ){
					if( errorSendMsgData["return_code"] == "11" ){
						$.popTips("popTipErr",errorSendMsgData["return_info"]);
						$("#captchaImg").attr("src","/captcha4sys?v="+Math.random());
						$("#txtYZM").val("");
					}else{
						$.popTips("popTipErr",errorSendMsgData.return_info || "发送失败");
						$("#yinyingTop").hide();
						$(".confirm_pay").hide();
						$('body,html').css('overflow','auto');
					}
				}
			});
		
		}
		
			
	}
    
})();