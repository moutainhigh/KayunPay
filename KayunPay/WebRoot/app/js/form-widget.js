(function(){
	
	var fieldSelector = ".field" ;
	
	function addEle( arr , eles){
		var len = eles.length ;
		for( var index = 0 ; index < len ; index ++  ){
			var ele = eles[ index ];
			arr.push($(ele));
		}
	}
	
	function getFormElements( that ){
		var tmp = [] ;
		addEle( tmp , that.find("input"));
		addEle( tmp , that.find("select"));
		addEle( tmp , that.find(".am-btn-group"));
		return tmp ;
	}
	
	$.fn.formUnSerialize = function( formData ){
		//扫描所有元素
		var that = $(this);
		var eles = that.find( fieldSelector );
		eles.each(function(){
			var ele = $(this).field();
			var val = formData[ ele.attr("name")] ;
			if( val )
				ele.setValue( val );
		});
	};
	
	$.fn.formSerialize = function( className ){
		//扫描所有元素
		var that = $(this);
		var eles = that.find( fieldSelector );
		var formObj = {} ;
		eles.each(function(){
			var ele = $(this).field();
			if( ele.is(":hidden") == false ){
				var keyName = ele.attr("name");
				if( className )
					keyName = className + "." + keyName ;
				formObj[ keyName ] = ele.getValue()||"" ;
			}
		});
		
		return formObj ;
	}
	
	/********* Form Field  **********/
	
	$.fn.field = function( config ){
		var that = $(this) ;
		if( !config ){
			config = {} ;
		}
		
		config["do"] = config["do"] || "make" ;
		config["type"] = that.attr("type") || "text" ;
		config["optype"] = config["optype"]  || "view";	//设置
		config["etype"] = that.attr(config["optype"])|| "show";	//add=show
		config["name"] = that.attr("name") || "defaultName";
		config["text"] = that.attr("text") || "defaultText";
		config["defaultValue"] = that.attr("defaultValue") || "";
		config["map"] = window["DATA_MAP"][that.attr("map")] ||RES["DATA_MAP"][that.attr("map")]|| [];
		config["placeholder"] = that.attr("placeholder") ||"" ;
		config["element"] = that ;
		var ele = this[ config["type"] +"Field"]( config ) ;	//[config["do"]]()
		
		return ele ;
	}
	
	$.fn.radioField = function( config ){
		var that = $(this);
		var dataMap = config["map"] ;
		config["ele"] = that.find("input");
		
		this.make = function( attr ){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-8 am-u-md-10'>";
			demoHtml += "<div class='am-btn-group' data-am-button>";
			for(var index in dataMap){
				var data = dataMap[ index ];
				var tmpHtml = "" ;
				if( attr.indexOf("disabled") >= 0 ){
					if( config["defaultValue"] == data["value"] ){
						tmpHtml += "<label class='am-btn am-btn-default am-btn-xs ";
						tmpHtml += "am-active";
						tmpHtml += "'>";
						tmpHtml += "<input type='radio' name='"+config["name"]+"' value='"+data["value"]+"'>"+data["text"]+"</label> " ;
					}else{
						tmpHtml += "<label class='am-btn am-btn-default am-btn-xs ";
						tmpHtml += "'>";
						tmpHtml += "<input type='radio' name='"+config["name"]+"' value='"+data["value"]+"'>"+data["text"]+"</label> " ;
					}
				}else{
					tmpHtml += "<label class='am-btn am-btn-default am-btn-xs ";
					if(config["defaultValue"] == data["value"])
						tmpHtml += "am-active";
					tmpHtml += "'>";
					tmpHtml += "<input type='radio' name='"+config["name"]+"' value='"+data["value"]+"'>"+data["text"]+"</label> " ;
				}
				demoHtml += tmpHtml ;
			}
			demoHtml += "</div></div>";
			that.html( demoHtml );
			//this.setValue(config["defaultValue"]);
		}
		
		this.setValue = function(value){
			/*
			if( ele.val() == dataValue ){
				ele.parent().addClass("am-active");
			}else{
				ele.parent().removeClass("am-active");
			}*/
			if( value ){
				var len = config["ele"].length ;
				for(var index = 0 ; index < len ; index ++ ){
					var ele = config["ele"][index] ;
					if( ele.value == value ){
						$(ele).parent().addClass("am-active");
					}else{
						$(ele).parent().removeClass("am-active");
					}
				}
			}
		}
		
		this.getValue = function(){
			var checked = that.find(".am-active").find("input") ;
			return checked.val() ;
		}
		
		return this ;
	}
	
	$.fn.selectField = function( config ){
		var that = $(this);
		var dataMap = config["map"] ;
		config["ele"] = that.find("select");
		this.make = function( attr ){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-8 am-u-md-10'>";
			demoHtml += "<select name='#{name}' onchange='selectchange()' data-am-selected=\"{btnSize: 'sm'}\">".replace("#{name}", config["name"] );
			for(var index in dataMap){
				var data = dataMap[ index ];
				var tmpHtml = "<option value='"+data["value"]+"' #{attr} ".replace("#{attr}" , attr);
				if( config["defaultValue"] == data["value"] ){
					tmpHtml += " selected" ;
					tmpHtml = tmpHtml.replace(attr,"");
				}
				tmpHtml += ">"+data["text"]+"</option>";
				demoHtml += tmpHtml ;
			}
			demoHtml += "</select></div>";
			that.html( demoHtml );	
		}
		
		this.getValue = function(){
			return config["ele"].val() ;
		}
		
		this.setValue = function( value ){
			if( value ){
				config["ele"].find("option[value='"+value+"']").removeAttr("disabled");
				config["ele"].find("option[value='"+value+"']").attr("selected" , true );
			}
		}
		
		return  this ;
	}
	
	$.fn.checkboxField = function(config){
		var that = $(this);
		var dataMap = config["map"] ;
		config["ele"] = that.find("input");
		
		this.make = function(attr){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-8 am-u-md-10'>";
			demoHtml += "<div class='am-btn-group' data-am-button>";
			for(var index in dataMap){
				var data = dataMap[ index ];
				var tmpHtml = "" ;
				if( attr.indexOf("disabled") >=0 ){
					if(config["defaultValue"].indexOf(data["value"]) >= 0 ){
						tmpHtml += "<label class='am-btn am-btn-default am-btn-xs ";
						if(config["defaultValue"].indexOf(data["value"]) >= 0 )
							tmpHtml += "am-active";
						tmpHtml += "'>";
						tmpHtml += "<input type='checkbox' name='"+config["name"]+"' " +
								"value='"+data["value"]+"'>"+data["text"]+"</label> " ;
					}
				}else{
					tmpHtml += "<label class='am-btn am-btn-default am-btn-xs ";
					if(config["defaultValue"].indexOf(data["value"]) >= 0 )
						tmpHtml += "am-active";
					tmpHtml += "'>";
					tmpHtml += "<input type='checkbox' name='"+config["name"]+"' " +
							"value='"+data["value"]+"'>"+data["text"]+"</label> " ;
				}
				
				demoHtml += tmpHtml ;
			}
			demoHtml += "</div></div>";
			that.html( demoHtml );
		}
		
		this.setValue = function(value){
			/*
			if( ele.val() == dataValue ){
				ele.parent().addClass("am-active");
			}else{
				ele.parent().removeClass("am-active");
			}*/
			if( value ){
				var len = config["ele"].length ;
				for(var index = 0 ; index < len ; index ++ ){
					var ele = config["ele"][index] ;
					if( value.indexOf( ele.value ) >= 0 ){
						$(ele).parent().addClass("am-active");
					}else{
						$(ele).parent().removeClass("am-active");
					}
				}
			}
		}
		
		this.getValue = function(){
			var value = "" ;
			var checked = that.find(".am-active").find("input") ;
			for(var index = 0 ; index < checked.length ; index ++ ){
				var ele = checked[ index ];
				value += ele.value + ",";
			}
			return value.substring(0 , value.length -1 );
		}
		
		return this ;
	}
	
	$.fn.textareaField = function(config){
		var that = $(this);
		config["ele"] = that.find("textarea");
		config["row"] = that.attr("row");
		
		this.make = function(attr){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-12 am-u-md-10'>" ;
			demoHtml += "<textarea name='#{name}' rows='#{row}' placeholder='#{placeholder}' #{attr}></textarea>"
				.replace("#{name}",config["name"])
				.replace("#{row}",config["row"]||4)
				.replace("#{attr}" , attr)
				.replace("#{placeholder}",config["placeholder"]);
			
			that.html( demoHtml );
		}
		
		this.getValue = function(){
			return config["ele"].val();
		}
		
		this.setValue = function(value){
			config["ele"].val(value);
		}
		
		return this ;
	}
	
	$.fn.dateField = function( config ){
		var that = $(this);
		config["ele"] = that.find("input");
		config["format"] = that.attr("format");
		
		this.make = function(attr){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-8 am-u-md-10 '>";
			demoHtml += "<div class='am-form-group'>";
			demoHtml += ("<input name='#{name}' type='datetime' class='am-form-field am-input-sm' placeholder='#{placeholder}' " +
					"data-am-datepicker='{format: \"#{format}\"}' #{attr}>")
					.replace("#{name}",config["name"])
					.replace("#{attr}" , attr)
					.replace("#{placeholder}",config["placeholder"])
					.replace("#{format}",config["format"]);
			demoHtml += "</div></div>";
			that.html(demoHtml);
		}
		
		this.setValue = function(value){
			config["ele"].val( value );
		}
		
		this.getValue = function(){
			return config["ele"].val();
		}
		
		return this ;
	}
	
	$.fn.textField = function( config ){
		var that = $(this);
		config["ele"] = that.find("input");
		config["dtype"] = that.attr("dtype")||"text";	//数据类型	number/email/url/date
		config["min"] = that.attr("min");
		config["max"] = that.attr("max");
		config["minlength"] = that.attr("minlength")||0;
		config["maxlength"] = that.attr("maxlength")||100;
		config["desc"] = that.attr("desc")||"";
		config["error"] = that.attr("error")||"" ;
 		this.make = function(attr){
			var demoHtml = "<div class='am-u-sm-4 am-u-md-2 am-text-right'>#{text}</div>".replace("#{text}", config["text"] );
			demoHtml += "<div class='am-u-sm-8 am-u-md-4'>";
			demoHtml += ("<input name='#{name}' type='#{dtype}' class='am-input-sm' placeholder='#{placeholder}'" +
					" #{min} #{max} #{minlength} #{maxlength} #{errorMsg} #{attr} value='#{defaultValue}'>")
				.replace("#{name}",config["name"])
				.replace("#{errorMsg}",config["error"]?"data-validation-message='"+config["error"]+"'":"")
				.replace("#{placeholder}",config["placeholder"])
				.replace("#{min}",config["min"]?"min=" + config["min"]:"")
				.replace("#{max}",config["max"]?"max=" + config["max"]:"")
				.replace("#{minlength}",config["minlength"]?"minlength="+config["minlength"]:"")
				.replace("#{maxlength}",config["maxlength"]?"maxlength="+config["maxlength"]:"")
				.replace("#{attr}" , attr)
				.replace("#{defaultValue}" , config["defaultValue"])
				.replace("#{dtype}",config["dtype"]);
			demoHtml += "</div><div class='am-hide-sm-only am-u-md-6'>#{desc}</div>".replace("#{desc}", config["desc"]);
			that.html( demoHtml );
		}
		
		this.getValue = function(){
			var tmpValue = config["ele"].val()+"" ; 
			if( !tmpValue )
				tmpValue = "";
			if( config["dtype"] == "number"){
				if(tmpValue)
					tmpValue = tmpValue.replaceAll(",","");//.replaceAll("\\.","") ;
				else
					tmpValue=0;
			}
			return tmpValue ;
		}
		
		this.setValue = function( value ){
			value +="";
			if( config["dtype"] == "number"){
				value = value.replaceAll(",","") ;
			}
			config["ele"].val( value ) ;
		}
		
		return this ;
	}
	
	
	/********* Form Field  **********/
	/**
	 * 	config
	 * 		->formType		表单类型:add/edit/view	默认add
	 * 		
	 */
	$.fn.formInit = function( config ){
		if( !config)
			config = {} ;
		config["formType"] = config["formType"] || "add" ;
		var that = $(this);
		that.attr("action" , config["request"]);
		that.find( fieldSelector ).each(function(){
			var ele = $(this);
			var eleAttr1 = ele.attr( config["formType"] ) || "show";
			ele.show();
			if( eleAttr1 == "show" ){
				ele.field().make("") ;
			}
			
			if( eleAttr1 == "hide"){
				ele.hide();
			}
			
			if( eleAttr1 == "disabled" )
				ele.field().make("disabled") ;
			
			if( config["formType"] == "view"){
				ele.field().make("disabled");
			}
			
		});
		
		return this ;
	}
	
	$.fn.validateForm = function(){
		return this.data('amui.validator').isFormValid() ;
	}
	
	$.fn.validatorInit = function(){
		this.validator({
		    onValid: function(validity) {
			      $(validity.field).parent().find('.am-alert').hide();
			    },
		    onInValid: function(validity) {
		      var $field = $(validity.field);
		      var $group = $field.parent();
		      var $alert = $group.find('.am-alert');
		      // 使用自定义的提示信息 或 插件内置的提示信息
		      var msg = $field.data('validationMessage') || this.getValidationMessage(validity);
	
		      if (!$alert.length) {
		        $alert = $('<div class="am-alert am-alert-danger"></div>').hide().
		          appendTo($group);
		      }
	
		      $alert.html(msg).show();
		    }
		  });
	}
	
	$.fn.appendImg = function( imgurl ){
		var urldemo = "<li style='margin:10px;width: 22%;float: left;'><div class='am-gallery-item' style='position: relative;'><a href='#' class='am-close am-close-alt am-close-spin am-icon-times' style='position:absolute;right:5px;top:5px;'></a><img style='width:100%;height:240px;' src='#{yasuo}' class='.am-round' /></div></li>";
		urldemo = urldemo.replaceAll("#{yasuo}",imgurl);
		$(this).append(urldemo).delegate("a","click",function(){
			$(this).parent().parent().remove();
		});
	}
	
	$.fn.makeSelect4s = function( data ){
		var that = $(this);
		that.html("");
		that.append("<option value=''>所有选项</option>");
		for(var key in data ){
			that.append("<option value='"+key+"'>"+data[key]+"</option>");
		}
	}
	
	$.fn.makeSelectMap4s = function( data ){
		var that = $(this);
		that.html("");
		that.append("<option value=''>所有选项</option>");
		for(var key in data ){
			that.append("<option value='"+data[key]["activeCode"]+"'>"+data[key]["activeName"]+"</option>");
		}
	}
	
	/**
	 * 
	 * btn
	 * 	->text
	 * 	->url
	 * 	->ico
	 */
	$.makeButtons = function( btns ){
		var html = "<div class='am-btn-toolbar'><div class='am-btn-group am-btn-group-xs'>";
		var demo = "<a class='am-btn am-btn-default am-btn-xs am-text-secondary' href=\"#{url}\" " +
				"target='#{target}' style='color:#{color}'>" ;
		demo += "<span class='#{ico}'></span>#{text}</a>";
		for(var index in btns ){
			var btn = btns[ index ] ;
			btn["ico"] = btn["ico"]||"am-icon-pencil-square-o"; 
			btn["color"] = btn["color"]||"blue";
			btn["target"] = btn["target"]||"_self";
			html += demo.makeHtml( btn );
		}
		html += "</div></div>";
		return html ;
	}
	
	$.newFileBtn = function( obj ){
		var uploader = WebUploader.create({
		    // 选完文件后，是否自动上传。
		    auto: true,
		    // swf文件路径
		    swf: 'http://cdn.staticfile.org/webuploader/0.1.0/Uploader.swf',
		    // 文件接收服务端。
		    server: '/upload',
		    // 选择文件的按钮。可选。
		    // 内部根据当前运行是创建，可能是input元素，也可能是flash.
		    pick: obj.pick ,
		    // 只允许选择图片文件。
		    accept: {
		        title: 'Images',
		        extensions: 'gif,jpg,jpeg,bmp,png',
		        mimeTypes: 'image/*'
		    },
		    duplicate : true
		});
		
		uploader["listEle"] = obj.listEle ;
		
		uploader.on( 'fileQueued', function( file ) {
			var echoHtml = "<li fid='#{fid}'><div class='am-gallery-item am-position-relative loan-pic-item'><i class='am-close am-gallery-close'>×</i>"
				+ "<img src='../assets/css/loadingPic.gif' /><input class='am-gallery-title am-gallery-title' value='#{imgname}' ext='#{ext}'></input></div></li>";
			var fid = file.id ; //.replace("WU_FILE_","a_") ;
			var ext = file.ext ;
			echoHtml = echoHtml.replaceAll("#{fid}" , fid )
				.replaceAll("#{imgname}",file.name.replace("." + ext,"") )
				.replaceAll("#{ext}",ext );
			uploader["listEle"].append(echoHtml);
		});
		
		uploader.on("uploadSuccess",function( file ){
		});
		
		uploader.on("uploadAccept",function( result , ret){
			var urlPre = "http://image1.yrhx.com/";
			var imgli = uploader["listEle"].find("li[fid="+ret.id+"]");
			imgli.attr("imgcode" , ret.fileid );
			imgli.find("img").attr("src" , $.makeRequestImgUrl( ret.fileid ));
		});
	}
	
	$.makeRequestImgUrl = function( code , itype){
		itype = itype || "original";
		return "http://image1.yrhx.com/" + code +"/"+ itype;
	}
	
})();