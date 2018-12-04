(function(){
	
	/**	
	 * 		rowHtml	-->html 变量用#{xx}代代替
	 * 			Demo: <td>#{name}</td><td><a href='aaa?id=#{id}'>delete</a></td>
	 */
	$.fn.table = function( config ){
		
		var that = $(this);
		that.find("table").addClass("am-table-hover");
		var rowHtml = "" ;
		var rowMax = config["max"]||10 ;
		/**
		 * 	[{
		 * 		name:"",
		 * 		text:""	
		 * 	}]
		 * 
		 */
		function makeHeader( headers ){
			that.find("thead").html("");
			var tmpHtml = "<tr>" ;
			rowHtml = "<tr>";
			for(var index in headers){
				var header = headers[ index ];
				var headerType = header["type"]||"text";
				
				if( headerType == "checkbox"){
					if(config["thtdcls"]){
						tmpHtml = tmpHtml + "<th style='"+config["thtdcls"]+"'><input type='checkbox' id='allBtn' ></th>";
						rowHtml = rowHtml + "<td style='"+config["thtdcls"]+"'><input type='checkbox' name='rowCheckBox' value='#{rowIndex}' ";
					}else{
						tmpHtml = tmpHtml + "<th><input type='checkbox' id='allBtn' ></th>";
						rowHtml = rowHtml + "<td><input type='checkbox' name='rowCheckBox' value='#{rowIndex}' ";
					}
					if( header["key"] ){
						rowHtml += "key='#{"+ header["key"] +"}'";
					}
					rowHtml += "/></td>";
				}else if( headerType == "text"){
					if(config["thtdcls"]){
						tmpHtml = tmpHtml + "<th style='"+config["thtdcls"]+"' name='"+header["name"]+"'>";
						tmpHtml += header["text"];
						tmpHtml += "</th>";
						rowHtml = rowHtml + "<td style='"+config["thtdcls"]+"'>"+ header["html"]+"</td>";
					}else{
						tmpHtml += "<th name='"+header["name"]+"'>";
						tmpHtml += header["text"];
						tmpHtml += "</th>";
						rowHtml += "<td>"+ header["html"]+"</td>";
					}
				}
			}
			tmpHtml += "</tr>";
			rowHtml += "</tr>";
			that.find("thead").html(tmpHtml);
			//bind checkbox btn
			$("#allBtn").change(function(){
				if( this.checked == true )
					$("[name=rowCheckBox]").prop("checked",true);
				else
					$("[name=rowCheckBox]").removeAttr("checked");
			});
		}
		
		function makeToTalHtml( countInfo ){
			var ct_info = "<font style='color: red;font-weight: bold;margin-left:20px;'>总计：</font>" +
					"</br><style>.totalTable tr td{padding:0 20px;}</style>";
			ct_info += "<table class='totalTable'>";
			var trCount = 0 ;
			for(var cikey in countInfo ){
				if( trCount == 0 ){
					ct_info += "<tr>";
				}
				ct_info += "<td>" + cikey + "：</td>";
				ct_info += "<td>" + countInfo[cikey] + "</td>";
				trCount ++ ;
				if( trCount == 3 ){
					trCount = 0 ;
					ct_info += "</tr>";
				}
			}
			ct_info += "</table>";
			return ct_info ;
		}
		
		function loadData( tableData , dataFormat){
			if( !tableData )
				return false ;
			that.find("tbody").html("");
			for(var rowIndex in tableData ){
				var rowData = tableData[ rowIndex ] ;
				//append row
				var tempRowHtml = rowHtml ;
				if( dataFormat ){
					rowData = dataFormat(rowData);
				}
				rowData["rowIndex"] = rowIndex ;
				var rowCount = 0 ;
				for( var key in rowData ){
					var kValue = rowData[ key ] ;
					if( kValue ){
						tempRowHtml = tempRowHtml.replaceAll("#{" + key +"}" , kValue );
					}else {
						tempRowHtml = tempRowHtml.replaceAll("#{" + key + "}","");
					}
					rowCount ++ ;
				}
				
				that.find("tbody").append( "<tr>" +  tempRowHtml + "</tr>");
			}
			if(config["count_info"]){
				if(config["thtdcls"]){
					that.find("tbody").append( "<tr><td style='"+config["thtdcls"]+"' colspan='"+config["header"].length+"'>" 
							+ makeToTalHtml(config["count_info"])  + "</td></tr>");
				}else{
					that.find("tbody").append( "<tr><td colspan='"+config["header"].length+"'>" + makeToTalHtml(config["count_info"])  + "</td></tr>");
				}
			}
		}
		
		function makePag( index , max , size){
			var tmpPag = new pag( index , max , size  );
			var pagEle = that.find("div.pad-div") ;
			pagEle.find("span").text("共 #{total} 条记录，页数：#{index}/#{max}"
					.replace("#{total}",size)
					.replace("#{index}",index)
					.replace("#{max}",tmpPag.getMaxPageNum()));	//total
			
			var lis = pagEle.find("ul.am-pagination").find("li");

			makePagLi( tmpPag.getMain() , lis[0]);
			makePagLi( tmpPag.getDown() , lis[1]);
			makePagLi( tmpPag.getUp() , lis[2]);
			makePagLi( tmpPag.getLast() , lis[3]);
			
		}
		
		function makePagLi( index , ele ){
			if( index == 0 ){
				$(ele).addClass("am-disabled");
			}else{
				$(ele).removeClass("am-disabled");
				$(ele).attr("index" , index );
			}
		}
		
		//make header
		if( config["header"] ){
			makeHeader( config["header"] );
		}
		
		//load data 
		loadData(config["data"] , config["dataFormat"]);
		makePag(config["pag"]["index"]||1 ,config["pag"]["max"]||20 , config["pag"]["size"]||0);
		$(".am-table-hover").unbind("click");
		$(".am-table-hover").click(function( row ){
//			var row = this;
			var nodes = $(row.target).siblings();
			var chk = nodes.find("[type=checkbox]") ;
			if( chk ){
				if( chk.attr("id") == "allBtn")
					return ;
				try{
					var chkd = chk[0].checked ;
					if( chkd )
						chk.removeAttr("checked");
					else
						chk.prop("checked",true);
				}catch(e){
					
				}
			}
		});
		
		/**
		 * 	index 从1开始
		 */
		function pag( index , max , size){
			this.getMain = function(){
				if( index == 1 ){
					return 0 ;
				}else{
					return 1 ;	
				}
			}
			this.getMaxPageNum = function(){
				return Math.ceil(size/max) ;
			}
			this.getLast = function(){
				var max = this.getMaxPageNum() ;
				if( index == max ){
					return 0 ;
				}else{
					return max ;
				}
			}
			this.getDown = function(){
				var max = this.getMaxPageNum() ;
				if( index > 1 ){
					return index -1 ;
				}else{
					return 0 ;
				}
			}
			this.getUp = function(){
				var maxPageNum = this.getMaxPageNum() ;	//max page
				if( index < maxPageNum ){
					return (Number(index) + 1);
				}else{
					return 0 ;
				}
			}
		}
		
	} ;
	
	//init
	$.fn.initPagEvent = function( callback ){
		$(this).find("ul.am-pagination").find("li").not("#not").click(function(){
			var that = $(this);
			if( that.hasClass("am-disabled") == false ){
				var pageNum = that.attr("index");
				$("#goValue").val(pageNum);//更改GO栏页码
				callback(pageNum , maxQueryNum );
			}
		});
	}
	
	//tools 
	window.getCheckedRow = function(){
		 return $("input:checkbox[name=rowCheckBox]:checked") ;
	}
	
})();
