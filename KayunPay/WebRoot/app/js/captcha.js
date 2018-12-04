/**
* obj {}
* obj.buid
* obj.sid
**/

var CAPTACH_API = {} ;


function captchaApi( obj ){
	var self = this;
	self.loadJS = function( url , callback ){
		var oh = $("head")[0];
		var os = document.createElement("script");
		os.type="text/javascript" ;
		os.src = url;
		os.onload = function(){
			callback();
		};
		oh.appendChild(os);
	};
	self.closeCaptch = function(){
		$('.confirm_pay').remove();
		$('#yinyingTop').remove();
		$('body').css('overflow','auto');

	};
	self.ajaxRequest = function(){

		YRHX.ajax({
			url : "/cap/cjcu" ,
			data : {
				"buid" : obj.buid ,
				"sid" : obj.sid
			},
			success : function( capurl  ){
				self.loadJS( capurl , function(  ){
					$("body").append(
						'<div class="confirm_pay">'+
						'<div class="pay_del"><div class="del" id="captchaClose"></div></div>'+
						'<div class="pay_row">'+
						'<label>验证码：</label><input id="txtYZM" type="text"><img src="" title="验证码" onclick="TSOCapObj.refresh();" id="captchaImg">'+
						'</div>'+
						'<div class="pay_row"><button>确认</button></div>'+
						'</div>'+
						'<div id="yinyingTop" class="yinyingTop" style="zindex:10"></div>'
					);
					to_center($('#yinyingTop').show());
					to_center($('.confirm_pay').show());
					$('body,html').css('overflow','hidden');
					TSOCapObj.init( "captchaImg" );
					TSOCapObj.refresh();
				});

				$("body").delegate("#captchaClose","click",function(){
					self.closeCaptch();
				});

			},
			error : function( errData ){
				YRHX.alert("验证码服务异常");
			}
		});

	};
	self.OnVerifyVCode = function( jsonData ){
		var objInput = $( "#txtYZM" );
		if( jsonData.errorCode != 0 ){
			if( typeof obj.error == "function"){
				objInput.css( "border" , "1px solid red");
				objInput.val("");
				TSOCapObj.refresh();
			}
		}
		else{
			//验证码验证通过

			if( typeof obj.success == "function"){
				obj.success( jsonData.ticket );
				objInput.css( "border" , "1px solid #ddd");
				objInput.val("");
			}
			
		}


	};
	self.cap = function(){
		self.ajaxRequest();
		$("body").delegate(".pay_row button" ,"click",function(){
			var ans = $("#txtYZM" ).val();

			TSOCapObj.verify(ans, self.OnVerifyVCode);
		});
	};
	return self ;
}


/*(function($){

	$.captchaApi = function(){

		var defaults = {

		};
		var CAP = this;
		CAP.loadJS = function( url , callback ){
			var oh = $("head")[0];
			var os = document.createElement("script");
			os.type="text/javascript" ;
			os.src = url;
			os.onload = function(){
				callback();
			};
			oh.appendChild(os);
		};
		CAP.closeCaptch = function(){
			$('.confirm_pay').remove();
			$('.yinying').remove();
			$('body').css('overflow','auto');

		};
		CAP.ajaxRequest = function(){

			YRHX.ajax({
				url : "/cap/cjcu" ,
				data : {
					"buid" : obj.buid ,
					"sid" : obj.sid
				},
				success : function( capurl  ){
					CAP.loadJS( capurl , function(  ){
						$("body").append(
							'<div class="confirm_pay">'+
							'<div class="pay_del"><div class="del" id="captchaClose"></div></div>'+
							'<div class="pay_row">'+
							'<label>验证码：</label><input id="txtYZM" type="text"><img src="" title="验证码" onclick="TSOCapObj.refresh();" id="captchaImg">'+
							'</div>'+
							'<div class="pay_row"><button>确认承接</button></div>'+
							'</div>'+
							'<div id="yinying" class="yinying"></div>'
						);
						to_center($('.yinying').show());
						to_center($('.confirm_pay').show());
						$('body,html').css('overflow','hidden');
						TSOCapObj.init( "captchaImg" );
						TSOCapObj.refresh();
					});

					$("body").delegate("#captchaClose","click",function(){
						captchaApi.closeCaptch();
					});

				},
				error : function( errData ){
					YRHX.alert("验证码服务异常");
				}
			});

		};
		CAP.OnVerifyVCode = function( jsonData ){
			var objInput = $( "#txtYZM" );
			if( jsonData.errorCode != 0 ){
				if( typeof obj.error == "function"){
					objInput.css( "border" , "1px solid red");
					objInput.val("");
				}
			}
			else{
				//验证码验证通过

				if( typeof obj.success == "function"){
					obj.success( jsonData.ticket );
					objInput.css( "border" , "1px solid #ddd");
					objInput.val("");
				}

			}


		};
		CAP.init = function(){
			CAP.ajaxRequest();
			$("body").delegate("#txtYZM" ,"change",function(){
				var ans = $("#txtYZM" ).val();

				TSOCapObj.verify(ans, CAP.OnVerifyVCode);
			});
		}




	}



}(jQuery));*/
