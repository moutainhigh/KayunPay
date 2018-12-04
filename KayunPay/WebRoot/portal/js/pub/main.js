


require(["jquery-1.7.1.min","core","../config","../public","../user","../highcharts","widget-table","app"], function(jquery) {
    alert( $(window).height() );
    //This function is called when scripts/helper/util.js is loaded.
    //If util.js calls define(), then this function is not fired until
    //util's dependencies have loaded, and the util argument will hold
    //the module value for "helper/util".
    
    $(function () {
		YRHX.ajax({
			url : CONFIG.getRequestURI("queryRealtimeFinancial"),
			success : function( sucData ){
				
				$("#paytotal").text( YRHX.toDecimal2(sucData["payTotal"] / 100,"¥") );
				$("#reciedtotal").text( YRHX.toDecimal2(sucData["reciedTotal"] / 100,"¥") );				
				$("#risktotal").text( YRHX.toDecimal2(  sucData["riskTotal"] / 100,"¥") );
				$("#payamounttotal").text(  YRHX.toDecimal2(sucData["payAmountTotal"] / 100,"¥") );
				$("#baddebttotal").text(  sucData["badDebtTotal"] / 100 +"%" );
				$("#overduetotal").text(  sucData["overdueTotal"] / 100 +"%");
	
			},
			error : function( errData ){
				//alert(errData.return_info || "请求服务失败");
			}
		});
		
		$.queryweekLoan = function( pageNumber, queryDate ){
			var rowHtml1 = "<tr><td>#{userName}</td>";
		 	rowHtml1 += "<td>#{loanTitle}</td>";
			rowHtml1 += "<td>#{toBeBack}</td>";
			rowHtml1 += "<td>#{backDate}</td></tr>";
			
			YRHX.ajax({
				url:CONFIG.getRequestURI("queryLoanInfo4Week"),
				data:{
					"pageNumber" : pageNumber || 1,
					"pageSize" : 10,
					"date"		: queryDate || ''
				},
				success:function( sucData ){
					var tableEle = $("#weekReturn").find("tbody");
					tableEle.html("");
					
					var dataPath = sucData["list"];
					if( dataPath && dataPath.length > 0 ){
						for (var i = 0; i < dataPath.length; i++) {
							var row = dataPath[i];
							var nameLen=row["userName"].length;
							var temDay = row["effectDate"].substr(6,2);
							var curTimeStamp = new Date();
							var nextYear = curTimeStamp.getFullYear();
							var nextMonth = curTimeStamp.getMonth();
							nextMonth = nextMonth + 1;
							if( nextMonth < 10){
								nextMonth = '0'+nextMonth;
							}
							/*if(nameLen < 3){
								row["userName"]=row["userName"].substr(0,1) +"*";
							}else if(nameLen < 4){
								row["userName"]=row["userName"].substr(0,1) +"*";
							}else{
								row["userName"]=row["userName"].substr(0,2) +"**";
							}*/
							
							row["backDate"] = nextYear+"-"+nextMonth+"-"+temDay;
							row["userName"]=row["userName"].substr(0,1) + starNum(nameLen -1);
							function starNum(num){
								var show='';
								for(var i=0 ; i < num; i++){
									show+='*';
								}
								return $.trim(show);
								
							}
							
							//TODO
					
							var licai = new YRHX.licai(row["loanAmount"],row["rateByYear"], row["loanTimeLimit"]);
							/* var lixi_jl = new YRHX.licai(row["payamount"],row["rewardratebyyear"],row["loantimelimit"]).dengxi();  // 奖励利息 */
							//判断类型 计算已收本息
							if (row["refundType"] == "A") {
								//等额本息 
								var denge4year = licai.denge4year();
								row["toBeBack"] = YRHX.toDecimal2(denge4year[nextMonth-1].benxi / 100,"¥"); //未加奖励利息
								
							} else if (row["refundType"] == "B") {
								var jqdslx = licai.dengxi();
								//按月付息 到期还本
								if (row["reciedCount"] + 1 == row["loanTimeLimit"]) {
									
									row["toBeBack"] = YRHX.toDecimal2(( jqdslx + row["loanAmount"]) / 100, "¥"); //未加奖励利息
								} else {
									row["toBeBack"] = YRHX.toDecimal2(( jqdslx ) / 100, "¥"); //未加奖励利息
								}
							} else {
								//到期还本息 
								if (row["reciedCount"] + 1 == row["loanTimeLimit"]) {
									var lixi = licai.dengxi() * row["loanTimeLimit"];
									row["toBeBack"] = YRHX.toDecimal2( (lixi + row["loanAmount  "]) / 100, "¥");

								} else {
									row["toBeBack"] = "¥0.00";
								}
							}
							var tempHtml = rowHtml1.makeHtml( row );
							tableEle.append(tempHtml); 
						};
					}else{
						//暂无数据
						$("#weekReturn").noData();
					}
					//分页
					$(".pageOne").pag(sucData["pageNumber"], sucData["pageSize"],sucData["totalPage"], function() {
						var reqIndex = $(this).attr("index");
						$.queryweekLoan( reqIndex || 1);
					});
					
				},
				error:function( errData ){
					/* alert( errData.return_info || "请求服务失败"); */
				}
				
			});
		};
		//一周内应回收欠款列表
		$.queryweekLoan();
		
		
		
		
		//一周内应回收欠款列表 下拉框
		$.weekData = function(){
			var curTimeStamp = new Date().getTime();
			var nextDay = curTimeStamp + 60*60*24*3*1000;
			var nextYear = nextDay.getFullYear();
			var nextMonth = nextDay.getMonth();
			var nextDate = nextDay.getDate();
		};
		$.dateFormat = function( timeStamp ){
			var curtimeStamp = new Date( timeStamp );
			/* return curtimeStamp.getFullYear() +'-'+ curtimeStamp.getMonth() +'-'+ curtimeStamp.getDate();  */
			var timeMonth = curtimeStamp.getMonth();
			timeMonth = timeMonth + 1;
			if( timeMonth < 10){
				timeMonth = '0'+timeMonth;
			}
			var timeDay = curtimeStamp.getDate();
			if( timeDay < 10 ){
				timeDay = '0'+timeDay;
			}
			return curtimeStamp.getFullYear().toString() + timeMonth.toString() + timeDay.toString(); 
			
		};
		var curtimeStamp =  new Date().getTime();
		for( var i = 0;i<7;i++){
			if( i > 0){
				var newStamp = curtimeStamp + 60*60*24*i*1000;	
			}else{
				var newStamp = curtimeStamp;
			}			
			$(".options").append('<a href="javascript:">'+$.dateFormat( newStamp )+'</a>');
		}
		
		
		$('.select .options a').click(function(){
			var html = $(this).html()
			var value = html;
			var selectElement = $(this).parent().parent();
			if($(this).attr('value')){
				value = $(this).attr('value');
			}
			selectElement.children('span').html(html);
			$(this).parent().hide();

			if( value == '一周内'){
				$.queryweekLoan();
			}else{
				$.queryweekLoan(1, value );
			}
			
		});
		
		$.queryOverdueTrace30 = function( indexPage, type, selector ,pageSelector){
			YRHX.ajax({
				url:CONFIG.getRequestURI("queryOverdueTrace30"),
				data:{
					"pageNumber"	: indexPage || 1,
					"pageSize"	    : 10,
					"type"		    : type  //type 0 三十天内     1三十天以上
				},
				success:function( sucData ){
					var tableEle = $("#"+selector ).find("tbody");
					tableEle.html("");
					
					var rowHtml2 = "<tr><td>#{loanUserName}</td>";
				 	rowHtml2 += "<td>#{loanTitle}</td>";
					rowHtml2 += "<td>#{loanTimeInfo}</td>";
					rowHtml2 += "<td>#{loanMoney}</td>";
					rowHtml2 += "<td>#{overdueDate}</td></tr>";
					var dataPath = sucData["list"];
					if( dataPath && dataPath.length > 0 ){
						for (var i = 0; i < dataPath.length; i++) {
							var row = dataPath[i];
							if( row ){
								row["loanTimeInfo"] = row["repayIndex"] +"/"+ row["loanTimeLimit"];
								row["loanMoney"] = row["principal"] + row["interest"];
								row["overdueDate"] = row["overdueDate"].substr(0,4)+ '-'+ row["overdueDate"].substr(4,2) +'-'+row["overdueDate"].substr(6,2);
							}
				
							var tempHtml = rowHtml2.makeHtml( row );
							tableEle.append(tempHtml); 
						};
					}else{
						//暂无数据
						$("#"+selector).noData();
					}
					//分页
					$("."+ pageSelector ).pag(sucData["pageNumber"], sucData["pageSize"],sucData["totalPage"], function() {
						var reqIndex = $(this).attr("index");
						/* $.queryInvest4MyA( reqIndex || 1); */
					});
					
				},
				error:function( errData ){
					//alert( errData.return_info || "请求服务失败");
				}
				
			});
		};
		
		$.queryOverdueTrace4yes = function( pageNumber, indexPage ){
			YRHX.ajax({
				url:CONFIG.getRequestURI("queryOverdueTrace4yes"),
				data:{
					"pageNumber"	: indexPage || 1,
					"pageSize"	    : 10
				},
				success:function( sucData ){
				
					var rowHtml3 = "<tr><td>#{loanUserName}</td>";
				 	rowHtml3 += "<td>#{loanTitle}</td>";
					rowHtml3 += "<td>#{loanTimeinfo}</td>";
					rowHtml3 += "<td>#{loanMoney}</td>";
					rowHtml3 += "<td>#{overdueDate}</td></tr>";
					
					var tableEle = $("#delayReturnTable").find("tbody");
					tableEle.html("");
					
					var dataPath = sucData["list"];
					if( dataPath && dataPath.length > 0 ){
						for (var i = 0; i < dataPath.length; i++) {
							var row = dataPath[i];
							if( row ){
								row["loanTimeinfo"] = row["repayIndex"] +"/"+ row["loanTimeLimit"];
								row["loanMoney"] = row["principal"] + row["interest"];
								row["overdueDate"] = row["overdueDate"].substr(0,4)+ '-'+ row["overdueDate"].substr(4,2) +'-'+row["overdueDate"].substr(6,2);
							}
				
							var tempHtml = rowHtml3.makeHtml( row );
							tableEle.append(tempHtml); 
						};
					}else{
						//暂无数据
						$("#delayReturnTable").noData();
					}
					//分页
					$(".pageFour" ).pag(sucData["pageNumber"], sucData["pageSize"],sucData["totalPage"], function() {
						var reqIndex = $(this).attr("index");
						/* $.queryInvest4MyA( reqIndex || 1); */
					});
					
				},
				error:function( errData ){
					//alert( errData.return_info || "请求服务失败");
				}
				
			});
		};
		
		//逾期30天内未归还列表
		$("#delayMonth").click(function(){
			$.queryOverdueTrace30(1, 0, "delayMonthTable","pageTwo");
		});
		
		//逾期30天以上未归还列表
		$("#overDelayMonth").click(function(){
			$.queryOverdueTrace30(1, 1, "overDelayMonthTable","pageThree");
		});
		
		//逾期已归还列表
		$("#delayReturn").click(function(){
			$.queryOverdueTrace4yes(1);
		});
		
		var colors = Highcharts.getOptions().colors,
				categories = ['银行活期', '银行定期', '余额宝', '货币基金', '易融恒信'],
				data = [{
					y: 0.35,
					color: "#dcdcdc"
				}, {
					y: 3,
					color: "#dcdcdc"
				},{
					y: 4.2,
					color: "#dcdcdc"
				},{
					y: 4.54,
					color: "#dcdcdc"
				},{
					y:22.8,
					color: "#b4d4f8"
				}
				];

		var chart = $('#container').highcharts({
			chart: {
				type: 'column'
			},
			title: {
				text: '投资收益对比图',
				style: {
					fontSize: '24px'
				}
			},
			subtitle: {
				text: ''
			},
			xAxis: {
				categories: categories
			},
			yAxis: {
				title: {
					text: ''
				},
				minPadding: 0.1,
				labels:{
					enabled:false
				}
			},
			legend :{
				enabled : false,
			},
			plotOptions: {
				column: {
					cursor: 'pointer',
					dataLabels: {
						enabled: true,
						color: "#333",
						style: {
							fontSize: '14px',
						},
						formatter: function() {
							return this.y +'%';
						}
					}
				}
			},
			tooltip: {
				enabled:false
			},
			series: [{
				name: name,
				data: data,
				color: 'white'
			}],
			exporting: {
				enabled: false
			}
		})
				.highcharts(); // return chart
	});
	//获取两时间相差月份
	function getMonthNumber(date1, date2) {
	
		//默认格式为"20030303",根据自己需要改格式和方法
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
});