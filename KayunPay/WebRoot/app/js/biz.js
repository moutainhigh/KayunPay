(function(){

	/**
	 * 	实际使用时，需要重写该函数
	 * 
	 */
	window.reAuth = function(){
		alert("未登录或者登录超时,点击确认后重新登录!");
		//window.parent.location.href = (RES["ROOT"] + "/login.html") ;
		window.parent.location.href = "/login";
	};
	
	var YRHX = window.YRHX = {} ;
	
	YRHX.alert = function( title ,text ){
		
		if( !text ){
			text = title ;
			title = "警告";
		}
		
		var eleId = "yrhx-alert";
		var obj = $("#" + eleId) ;
		if( obj.length == 0 ){
			var demoHtml = "<div class='am-modal am-modal-alert' tabindex='-1' id='"+eleId+"'>";
			demoHtml += "<div class='am-modal-dialog'>";
			demoHtml += "<div class='am-modal-hd alert-title'>易融恒信</div>";
			demoHtml += "<div class='am-modal-bd alert-content'></div>";
			demoHtml += "<div class='am-modal-footer'><span class='am-modal-btn' onclick = 'javascript:window.location.reload()'>确定</span>";
			demoHtml += "</div></div></div>";
			$("body").append(demoHtml);
			obj = $("#" + eleId) ;
		}
		$("div.alert-title").text(title);
		$("div.alert-content").html("");
		$("div.alert-content").html( text);
		obj.modal();
	}
	
	YRHX.alert1 = function( title ,text ){
		
		if( !text ){
			text = title ;
			title = "警告";
		}
		
		var eleId = "yrhx-alert";
		var obj = $("#" + eleId) ;
		if( obj.length == 0 ){
			var demoHtml = "<div class='am-modal am-modal-alert' tabindex='-1' id='"+eleId+"'>";
			demoHtml += "<div class='am-modal-dialog'>";
			demoHtml += "<div class='am-modal-hd alert-title'>易融恒信</div>";
			demoHtml += "<div class='am-modal-bd alert-content'></div>";
			demoHtml += "<div class='am-modal-footer'><span class='am-modal-btn' >确定</span>";
			demoHtml += "</div></div></div>";
			$("body").append(demoHtml);
			obj = $("#" + eleId) ;
		}
		$("div.alert-title").text(title);
		$("div.alert-content").html("");
		$("div.alert-content").html( text);
		obj.modal();
	}
	
	YRHX.alert2 = function( title ,text ){
		
		if( !text ){
			text = title ;
			title = "警告";
		}
		
		var eleId = "yrhx-alert";
		var obj = $("#" + eleId) ;
		if( obj.length == 0 ){
			var demoHtml = "<div class='am-modal am-modal-alert' tabindex='-1' id='"+eleId+"'>";
			demoHtml += "<div class='am-modal-dialog'>";
			demoHtml += "<div class='am-modal-hd alert-title'>易融恒信</div>";
			demoHtml += "<div class='am-modal-bd alert-content'></div>";
			demoHtml += "<div class='am-modal-footer'><span class='am-modal-btn' onclick = 'javascript:window.history.go(-1)'>确定</span>";
			demoHtml += "</div></div></div>";
			$("body").append(demoHtml);
			obj = $("#" + eleId) ;
		}
		$("div.alert-title").text(title);
		$("div.alert-content").html("");
		$("div.alert-content").html( text);
		obj.modal();
		}

	//showPrompt
	YRHX["prompt_callback"] = function(){};
	YRHX.prompt = function(title , content , callback ){
		var pmpEle = $('#promptDiv');
		if( pmpEle.length == 0 ){
			var echoHtml = "<div class='am-modal am-modal-prompt' tabindex='-1' id='promptDiv'>" +
					"<div class='am-modal-dialog'><div class='am-modal-hd title'>标题</div>" +
					"<div class='am-modal-bd content'><span>请输入</span><input type='text' class='am-modal-prompt-input'>" +
					"</div><div class='am-modal-footer'><span class='am-modal-btn' data-am-modal-cancel>取消</span>" +
					"<span class='am-modal-btn' data-am-modal-confirm>提交</span></div></div></div>";
			$("body").append(echoHtml);
			pmpEle = $('#promptDiv');
		}
		YRHX["prompt_callback"] = callback ;
		pmpEle.find("div.title").text(title);
		pmpEle.find("div.content").find("span").text(content);
		pmpEle.find("input").val("");
		pmpEle.modal({
			relatedTarget: this,
			onConfirm: function(e){
				YRHX["prompt_callback"](e);
			} ,
			onCancel: function(e) {
			}
		});
	};
	
	YRHX.loading = function( opType , str){
		opType = opType || "open";
		var eleId = "yrhx-loading";
		var obj = $("#" + eleId);
		if( obj.length == 0 ){
			var demoHtml = "<div class='am-modal am-modal-loading am-modal-no-btn' tabindex='-1' id='"+eleId+"'>";
			demoHtml += "<div class='am-modal-dialog'><div class='am-modal-hd loading-content'></div>";
			demoHtml += "<div class='am-modal-bd'><span class='am-icon-spinner am-icon-spin'></span></div></div></div>";
			$("body").append(demoHtml);
			obj = $("#" + eleId) ;
		}
		$("div.loading-content").text( str||"正在载入...");
		obj.modal( opType );
	}
	
	String.prototype.replaceAll = function(s1,s2){
		if( typeof(s2) == "string" ){
			s2 = s2.replace(new RegExp("<","gm"),"&lt;");
			s2 = s2.replace(new RegExp(">","gm"),"&gt;");
			s2 = s2.replace(new RegExp("\"","gm"),"&quot;");
		}
		return this.replace(new RegExp(s1,"gm"),s2);
	};
	
	YRHX.queryString = function(item){
		var svalue = location.search.match(new RegExp("[\?\&]" + item + "=([^\&]*)(\&?)","i"));
		return svalue ? svalue[1] : svalue;
	};
	/**
	 * 	Cookie
	 * 
	 */
	YRHX.Cookie = function( key , value , expireMin ){
		//this.key = key + "_" + ( window.cookieMark||Math.floor(new Date().getTime()/1000/60/60/24) );
		this.key = key ;
		this.cfg = {
			path : "/"
		};
		this.expireMin = expireMin||-1 ;
		if( typeof value == "object"  )
			this.value = JSON.stringify( value );
		else
			this.value = value ;

		//覆盖原来的值
		this.set = function(){
			$.cookie( this.key , this.value , this.cfg);
		};
		
		this.del = function(){
			$.removeCookie(this.key , this.cfg );
		};
		
		this.get = function(){
			var cookieValue = $.cookie( this.key );
			
			try{
        		cookieValue = JSON.parse( cookieValue ) ;
			}catch(e){
			}
			
	        return cookieValue ;
		} ;
		
		this.clear = function(){
			var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
	        if ( keys ) {
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
		
		//YRHX.console("Request-URL:" + opt.url );  
		//opt.async = opt.async || true ;
		if(opt.timeout==-1){
			opt.timeout = 0;
		}else{
			opt.timeout = opt.timeout || (60*1000) ;
		}
		$.ajax({ 
			url : opt.url ,
			async : opt.async ,
			type : opt.type || "post" ,
			dataType : opt.dataType||"json" ,	//default jsonp
			scriptCharset : "UTF-8" ,
			data : opt.data ,
			timeout : opt.timeout , 
			headers : {
				"token" : getToken() 
			},
			beforeSend : function(){
				//before
				//loading
				YRHX.loading("open");
			},
			success : function( sucData ){
				
//				if( sucData[ tokenName ] ){
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
					
					if( sucData[ returnCodeKey ][0] == "A" ){
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
				setTimeout(function(){
					YRHX.loading("close");
				},50);
			}
		}) ;
		
	};
	
	YRHX.ajax1 = function( opt ){
		
		var tokenName = "token" ;
		var returnCodeKey = "return_code" ;
		var returnCodeValue = "00" ;
		var returnDataKey  = "return_data" ;
		var returnInfoKey = "return_info" ;
		
		//YRHX.console("Request-URL:" + opt.url );  
		//opt.async = opt.async || true ;
		if(opt.timeout==-1){
			opt.timeout = 0;
		}else{
			opt.timeout = opt.timeout || (60*1000) ;
		}
		$.ajax({ 
			url : opt.url ,
			async : opt.async ,
			type : opt.type || "post" ,
			dataType : opt.dataType||"json" ,	//default jsonp
			scriptCharset : "UTF-8" ,
			data : opt.data ,
			timeout : opt.timeout , 
			headers : {
				"token" : getToken() 
			},
			beforeSend : function(){
				//before
				//loading
			},
			success : function( sucData ){
				
//				if( sucData[ tokenName ] ){
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
					
					if( sucData[ returnCodeKey ][0] == "A" ){
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
				/*setTimeout(function(){
					YRHX.loading("close");
				},50);*/
			}
		}) ;
		
	};
	
	/*运算工具*/
	YRHX.toDecimal2 = function(s,p) {    
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
	

    $.fn.ajaxLoading = function( ){
        var that = $(this);
        that.css("position","relative");
        that.append('<div class="ajaxLoading"><div class="rotateBox"><img src="images/loadingPic.gif" alt="加载中..." width="20"></div></div>');
    };

    $.fn.ajaxDone = function(){
        var that = $(this);
        that.css("position","");
        $(".ajaxLoading").remove();
    };
    
    /*
    String.prototype.replaceAll = function (s1, s2) {
        return this.replace(new RegExp(s1, "gm"), s2);
    };*/
    
    String.prototype.makeHtml = function( obj ){
    	if( !obj )
    		obj = {} ;
    	var temp = this ;
    	for( var key in obj ){
    		temp = temp.replaceAll( "#{" + key +"}" , obj[key]||"" );
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
    }
	/*运算工具*/
	YRHX.toDecimal2 = function(s,p) {    
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
	
	YRHX.sortData = function( dataArr ){		
		var tmp = {};
		for(var index in dataArr ){
			var data = dataArr[index ] ;
			tmp[ data["value"] ] = data["text"] ;
		}
		return tmp ;
	}
    
	//获取两时间相差月份
	YRHX.getMonthNumber = function(date1, date2) {
		// 默认格式为"20030303",根据自己需要改格式和方法
		var year1 = date1.substr(0, 4);
		var year2 = date2.substr(0, 4);
		var month1 = date1.substr(5, 2);
		var month2 = date2.substr(5, 2);
		var len = (year2 - year1) * 12 + (month2 - month1);

		var day = date2.substr(8, 2) - date1.substr(8, 2);
		if (day > 0) {
			len += 1;
		} else if (day < 0) {
			len -= 1;
		}
		return len;
	}
    
    Date.prototype.Format = function(fmt) { // author: meizz
    	var o = {
    		"M+" : this.getMonth() + 1, // 月份
    		"d+" : this.getDate(), // 日
    		"h+" : this.getHours(), // 小时
    		"m+" : this.getMinutes(), // 分
    		"s+" : this.getSeconds(), // 秒
    		"q+" : Math.floor((this.getMonth() + 3) / 3), // 季度
    		"S" : this.getMilliseconds()
    	// 毫秒
    	};
    	if (/(y+)/.test(fmt))
    		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
    				.substr(4 - RegExp.$1.length));
    	for ( var k in o)
    		if (new RegExp("(" + k + ")").test(fmt))
    			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
    					: (("00" + o[k]).substr(("" + o[k]).length)));
    	return fmt;
    }
    
})();