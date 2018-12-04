

//月刊模块banner
$(function(){
	timer=setTimeout(run,4000);
	$('.month_icon span').click(function(){
		
		if(timer!=null){
			clearTimeout(timer);
		}
		var i=$(this).index();
		$('.month_icon span').eq(i).addClass('selected').siblings().removeClass('selected');
		$(this).addClass('selected');
		$('.monthly_con').animate({scrollLeft:(i * 345) + "px"},500,function(){
				timer=setTimeout(run,4000);
		});
	});
	
	
	function run(){
		if(timer!=null){
			clearTimeout(timer);
		}
		var left=$('.monthly_con').scrollLeft();  
		var i=left/345;
		if( left==345){
			$('.monthly_con').animate({scrollLeft:"0px"},500,function(){
				$('.month_icon span').removeClass('selected');
				jQuery(".month_icon span").eq(0).addClass("selected");
					setTimeout(run,4000);	
			});	
		}
		else{
			    $('.monthly_con').animate({scrollLeft:(left + 345) + "px"},500,function(){
					var index = jQuery(".monthly_con").scrollLeft() / 345;
					$('.month_icon span').removeClass('selected');
					jQuery(".month_icon span").eq(index).addClass("selected");
				    setTimeout(run,4000);
			});	
		   
		}
	}
	$.fn.tabHoverChange = function( selector ){
		var that = $(this);

		that.hover(function(){
			$(this).addClass('active').siblings().removeClass('active');
			var index = $(this).index();
			$('.'+ selector+'-list').hide();
			$('.'+ selector+'-list:eq('+index+')').show();

		})
	};
	
	$(".downloadLogo i").tabHoverChange( "tab-content");
	
	
	if( YRHX.Cookie("userCode").get()){

		$("#registerBtn").attr("href","/A00");
		$("#registerBtn span").text("我的账户")
		//var ajaxUrl =  "queryFundsCount4user";
		YRHX.ajax({
			url : CONFIG.getRequestURI( "queryFundsCount4user" ),
			success : function(sucData){
				$(".a_panel").text("已为您赚取收益:" +YRHX.toDecimal2( sucData.reciedInterest / 100) + '元');
				
			},
			error : function( errData ){
				alert( errData.return_info || "链接服务失败");
			}
		});
	}else{
		//var ajaxUrl =  "queryRealtimeFinancial";
	}

	
	
	//最新公告
	YRHX.ajax({
		url : CONFIG.getRequestURI( "queryNewsByPage" ),
		data:{
			"pageNumber": 1,
			"pageSize": 4,
			"isContent": 1, //0为完整数据  1为简化数据
			"type": 0 //0最新公告 1公司新闻 2行业动态
		},
		success : function( sucData ){
			var html_tab = "<li><a href='X00?id=#{id}'  target='_blank' title=#{title}><div class='list_title'>#{title}</div><span>#{upDateTime}</span></a></li>";
			var tableEle = $("#indexNotice");
			tableEle.html("");
			var returnData = sucData.list ;
			if( returnData.length > 1 ){
				var Notice_len=returnData.length;
				for( var i = 0; i < Notice_len; i++ ){
						var row = returnData[i];
						row["upDateTime"] = row["upDateTime"].substr(0,4)+'-'+row["upDateTime"].substr(4,2)+'-'+row["upDateTime"].substr(6,2); ;
						var tmpHtml = html_tab.makeHtml( row );
						tableEle.append( tmpHtml );
				}
				var f_child=$('#indexNotic li').eq(0);	
				var s_child=$('#indexNotic li').eq(1);
				$('#indexNotic').append(f_child);
				$('#indexNotic').append(s_child);
				
			}
		},
		error: function( errData ){
			
		}
	});
	
	//公司新闻
	YRHX.ajax({
		url : CONFIG.getRequestURI( "queryNewsByPage" ),
		data:{
			"pageNumber": 1,
			"pageSize": 5,
			"isContent": 1, //0为完整数据  1为简化数据
			"type": 1 //0网站 1公司新闻 2行业动态
		},
		success : function( sucData ){
			var html_tab = "<li><a href='X00?id=#{id}' target='_blank' title=#{title}>•&nbsp;&nbsp;#{title}</a><span>#{upDateTime}</span></li>";
			var tableEle = $("#indexNews");
			tableEle.html("");
			var returnData = sucData.list ;
			if( returnData.length > 0 ){
				for( var i = 0; i < returnData.length; i++ ){
					var row = returnData[i];
					row['upDateTime'] =row['upDateTime'].substr(0,4)+"-"+ row['upDateTime'].substr(4,2) + '-' +row['upDateTime'].substr(6,2);
					var tmpHtml = html_tab.makeHtml( row );	
					tableEle.append( tmpHtml );
				}
			}
		},
		error: function( errData ){
			
		}
	});
	
	//新手专享标
	YRHX.ajax({
        url: CONFIG.getRequestURI("queryFinancialBid"),
        data: {
        	"pageNumber": 1,
        	"pageSize": 1,
        	"type":1
        },
        success: function (sucData) {
        	var html_li="<div class='i_giftTop'>";
 				html_li+="<div class='i_giftTitle'>新手福利标<i></i><div class='gift_font'>新手标累计投资上限为1万元</div></div>";
 				html_li+="<a href='/Y01' class='i_gift_help'>新手帮助></a></div>";
 				html_li+="<div class='i_giftList'><ul class='index_bid'>";
 				html_li+="<li><p><span class='index_rate'>#{rateByYear}<rate style='font-size:18px;'>%</rate></span><span class='add_lv' style='display:#{show_state}'>+#{benefits4new}<span class='pos' style='top:0'>%</span></span></p>预期年化利率</li>";
 				html_li+="<li><p><span class='index_month'>#{loanTimeLimit}</span>个月</p>期限</li>";	
 				//html_li+="<li class='index_bidLast'><p><span class='index_amount'>#{loanAmount}</span>万</p>最高可投</li></ul>";
 				html_li+="<li class='index_bidLast'><p><span class='index_amount'>100</span>元</p>起投金额</li></ul>";	
 				html_li+="<div class='index_bidBtn'><a href='Z02_1?loanCode=#{loanCode}' class='#{curStateCodes}'>#{curStateDesc}</a></div></div>";	
        	  var returnData = sucData["loanInfos"]["list"];
              if (returnData && returnData.length > 0) {
                  $(".table_panel table").find("tbody").html("");
                  for (var rowsIndex in returnData) {
                      var row = returnData[rowsIndex];
                      var progress = 0;

                      //操作
                      if(  row["loanBalance"] == 0 ){
                    	  row["curStateCode"] = "grayBtn";
                    	  row["curStateCodes"] = "new_grayBtn";
                    	  if( row["loanState"] == "J" || row["loanState"] == "M" ){
                    		  //row["curStateDesc"] = "满标待审";
                    		  row["curStateDesc"] = "";
                    		  row["curStateCode"] = "grayBtn";
                    		  row["curStateCodes"] = "new_grayBtn";
                    	  }else if( row["loanState"] == "N"){
                    		  row["curStateDesc"] = "";
                    		  row["curStateCodes"] = "new_grayBtn";
                    	  }else if( row["loanState"] == "O"){
                    		  row["curStateDesc"] = "已完成";
                    	  }else{
                    		  row["curStateDesc"] = "立即出借";
                    	  }  
                      }else{
                    	  row["curStateCode"] = "";
                    	  row["curStateDesc"] = "立即出借";
                      }
                      
                      //还款方式
                      /*if (row["refundType"] == "A") {
                          row["refundType"] = "按月等额本息";
                      } else if (row["refundType"] == "B") {
                          row["refundType"] = "先息后本";
                      } else if (row["refundType"] == "C") {
                          row["refundType"] = "到期还本息";
                      }*/

                      //年利率
                      row["rateByYear"] = row["rateByYear"] / 100 ;
                      row["benefits4new"] = row["rewardRateByYear"] / 100 + row["benefits4new"] / 100 ;
                      row["rateByYearPlus"] = row["rateByYear"] +  row["benefits4new"] ;
                      
                      //新手专享年利率
                      if ( row["benefits4new"] > 0) {
                      	row["isShow"] = "isShow";

                      }else{
                      	row["isShow"] = "";
                      }

                      //完成百分比
                     /* var tmpAmount = parseFloat(row["loanAmount"]);
                      var tmpBalance = parseFloat(row["loanBalance"]);
                      if (tmpBalance == 0) {
                          row["progress"] = 100;
                      } else {
                          if (tmpBalance == tmpAmount) {
                              row["progress"] = "0"
                          } else {
                              row["progress"] = tmpBalance == 0 ? 0 : (tmpBalance / tmpAmount) * 100;
                              row["progress"] = Math.floor(100 - row["progress"])
                          }
                      }
                      */
                      row["progress_p"] = (row["progress"] - 1) * 83.46;
                      row["loanAmount"] = row["loanAmount"] / 1000000;
                      
                      row["releaseTimeStamp"] = row["releaseDate"] + row["releaseTime"] ;
                      row["releaseLeftTime"] = countLeftTime( strDate2Date(row["releaseTimeStamp"]) ,3);
                      
                      var tmpHtml = html_li.makeHtml(row);
                      $("#new_bid").html('');
                      $("#new_bid").append(tmpHtml);
                      circleProgress.refresh();
                      //TODO 有满标时间
                      //if(row["loanState"] == "J" && row["loanBalance"] == 0 || row["loanState"] == "M" || row["loanState"] == "N" || row["loanState"] == "O"){
                    		  //$(".new_bid_leftTime").text("满标时间："+ row["lastPayLoanDateTime"].dateformat());
                      //}else {
                    	  //投标中状态 倒计时显示 or 正常投标显示
                    	  //if (sucData["serverTime"] < row["releaseDate"] + row["releaseTime"]) {
                            	//定时标
                      		  //$(".new_bid_leftTime").text("发布时间："+ (row["releaseDate"] + row["releaseTime"]).dateformat());
                      	 // }else{
                      		//$(".new_bid_leftTime").text("满标时间："+ row["lastPayLoanDateTime"].dateformat());
                      		//$(".new_bid_leftTime").text("剩余时间："+( row["releaseLeftTime"]?row["releaseLeftTime"]:"加载中"));
                      	 //}
                      //}
                      
                  }
              }
        },
        error: function(){
        	
        	
        	
        }
	});
	//监听倒计时
    function initInterval() {
    	setInterval(function () {
    	   $(".s_time[istime=1]").each(function () {
    		   $(this).show();
    		   var that = $(this);
               var isTime = that.attr("istime");
                   var startTime = parseInt(that.attr("starttime"));
                   var endTime = parseInt(that.attr("endtime"));
                   if($(this).display!="none"){
                	   $(this).parent(".reverse_href").prev().hide();
                	   that.text("剩余：" + showLastTime((endTime - startTime) / 1000));
                       that.attr("starttime", startTime + 1000);
                   }
                   
           });
        }, 1000)
    }
    initInterval();
    
    /*function getLoanType( option ) {
    	//A质押宝 B车稳盈 C房稳赚 D其它
    	var typePic = ["typePledge","typeCar", "typeHouse", "typeOther","typeNew", "typeRich"];
    	var typeClass = "";
    	switch(option){
    		case "A" : 
    			typeClass = typePic[0];
    			break;
    		case "B" : 
    			typeClass = typePic[1];
    			break;
    		case "C" : 
    			typeClass = typePic[2];
    			break;
    		case "D" : 
    			typeClass = typePic[3];
    			break;
    			
    	}
       
        return typeClass;
    }*/
    
	//首页投资项目
  //按投资产品类型查询
    $.index_loan = function(pageNumber,pageSize,index,productType){
    	YRHX.ajax({
    		url: CONFIG.getRequestURI("queryFinancialBid"),
            data: {
            	"pageNumber": pageNumber || 1,
            	"pageSize": pageSize,
            	"type":2,
            	"productType":productType||""
            },
            success: function (sucData) {
            	var html_tab="<li><a href='Z02_1?loanCode=#{loanCode}' title='#{loanTitle}' class='selected_btn'><div class='i_sele_name'>#{loanTitle}</div>";
            	    html_tab+="<div class='i_sele_num'><div class='i_lv_show'><span class='i_bid_rate'>#{rateByYearPlus}<span class='i_pos'>%</span></span><span class='i_add_lv' style='display:#{show_state}'>+#{rewardRateByYear}%</span></div><p>预期年化利率</p></div>";
            	    html_tab+="<div class='i_sele_money #{curStateCode}'><div class='i_sele_date1'>期限：<span>#{loanTimeLimit}月</span></div><div class='i_sele_date'>剩余：<span>#{loanBalance}</span>#{unit}</div></div>";	
            	    html_tab+="<div class='i_seleSubmit #{curStateCodes}'>#{curStateDesc}</div><div class='reverse_href'><div class='s_time' style='display:none' id='showTime_#{loanCode}' isTime='#{isTime}' startTime='#{startTime}' endTime='#{endTime}'>#{timeDesc} #{zfsj}</div></div></a></li>";		
					
                var returnData = sucData["loanInfos"]["list"];
                $( '.index_sele_ul' ).html("");
                var arrBid=[],arrInter=[],arrFull=[],arrBack=[];
                var arrList=new Array();    
                if (returnData && returnData.length > 0) {
                	for(var i=0 ;i<returnData.length;i++){
                		var row = returnData[i];
                		
                		if(  row["loanBalance"] == 0 ){
                        	row["curStateCode"] = "index_grayBtn";
                        	row["curStateCodes"] = "index_grayBtns";
                    	  if( row["loanState"] == "J" || row["loanState"] == "M" ){
                      		  row["curStateDesc"] = "";
                      		  row["curStateCode"] = "index_grayBtn";
                      		  row["curStateCodes"] = "index_grayBtns";
                      		  arrFull.push(row);
                      	  }else if( row["loanState"] == "N"){
                      		  row["curStateDesc"] = "";
                      		  arrBack.push(row);
                      	  }else if( row["loanState"] == "O"){
                      		  row["curStateDesc"] = "已完成";
                      		  arrBack.push(row);
                      	  }else{
                      		row["curStateCodes"] = "";
                      		  row["curStateDesc"] = "立即出借";
                      		 
                      	  }
                      	  
                        }else{
                      	  	if (sucData["serverTime"] < row["releaseDate"] + row["releaseTime"]) {
    	                        //定时标
    	                        row["loanName"] = "即将开始";
    	                        row["timeDesc"] = "等待发布";
    	                        row["isTime"] = 1;
    	                        row["startTime"] = strDate2Date(sucData["serverTime"]);
    	                        row["endTime"] = strDate2Date(row["releaseDate"] + row["releaseTime"]);
    	                        row["zfsj"] = "!";
    	                        row["curStateCode"] = "state_hide";
    	                        row["curStateDesc"]="";
    	                        arrInter.push(row);
    	                  	} else {
    	                    	row["curStateCodes"] = "";
    	                    	row["curStateDesc"] = "立即出借";
    	                    	arrBid.push(row);
    	                    }
                        }
                	}
                }
                concatArr(arrBid);concatArr(arrInter);concatArr(arrFull);concatArr(arrBack);
    			function concatArr(arr){
    				for(var i=0 ;i <arr.length; i++){
    					arrList.push(arr[i]);
    				}
    			}
                if (arrList && arrList.length > 0) {
                    for (var i=0;i<arrList.length;i++) {
    					var row = arrList[i];
                        var progress = 0;
                        
                      //操作
                        if(  row["loanBalance"] == 0 ){
                        	row["curStateCode"] = "index_grayBtn";
                        	row["curStateCodes"] = "index_grayBtns";
                    	  if( row["loanState"] == "J" || row["loanState"] == "M" ){
                      		  row["curStateCode"] = "index_grayBtn";
                      		  row["curStateCodes"] = "index_grayBtns";
                      	  }else if( row["loanState"] == "N"){
                      		  //row["curStateDesc"] = "";
                      	  }else if( row["loanState"] == "O"){
                      		  row["curStateDesc"] = "已完成";
                      		  row["curStateCode"] = "index_grayBtn";
                      		  row["curStateCodes"] = "index_grayBtns";
                      	  }else{
                      		row["curStateCodes"] = "";
                      		  row["curStateDesc"] = "立即出借";		 
                      	  }  
                        }else{
                      	  	if (sucData["serverTime"] < row["releaseDate"] + row["releaseTime"]) {
    	                        //定时标
    	                        row["loanName"] = "即将开始";
    	                        row["timeDesc"] = "等待发布";
    	                        row["isTime"] = 1;
    	                        row["startTime"] = strDate2Date(sucData["serverTime"]);
    	                        row["endTime"] = strDate2Date(row["releaseDate"] + row["releaseTime"]);
    	                        row["zfsj"] = "!";
    	                        row["curStateCode"] = "state_hide";
    	                        row["curStateDesc"]="";
    	                  	} else {
    	                    	row["curStateCode"] = "";
    	                    	row["curStateDesc"] = "立即出借";
    	                    }
                        }
                        
                        //状态
                        if (row["loanState"] == "J" && row["loanBalance"] == 0) {
                            row["loanState"] = "M"
                        }

                        if( row["loanState"] == "J"){
                        	
                        }else if( row["loanState"] == "M"){
                        	row["loanStateDesc"] = "finish";
                        }else if (row["loanState"] == "N") {
                        	row["loanStateCode"] = "finish";
                        	row["loanStateDesc"] = "出借完成";
                        } else if (row["loanState"] == "O" || row["loanState"] == "P" || row["loanState"] == "Q") {
                        	row["loanStateCode"] = "finish";
                        	row["loanStateDesc"] = "已完成";
                        }
                        //奖励年利率
    					if (row["rewardRateByYear"] > 0) {
    						row["rateByYearPlus"] = YRHX.toDecimal3(parseFloat(row["rateByYear"] / 100) + parseFloat(row["benefits4new"] / 100));
    						row["rewardRateByYear"] =parseFloat(row["rewardRateByYear"] / 100);
    						row["rateyear"] = row["rateByYear"] +row["benefits4new"]+row["rewardRateByYear"];//计算利息
    						if ("yes" == row["ifShow"]) {
    							row["display"] = "";
    						} else {
    							row["display"] = "none";
    						}
    					}else if(  row["benefits4new"] > 0){//兼容新手奖励年利率
    						row["rateByYearPlus"] = YRHX.toDecimal3((row["rateByYear"] +row["benefits4new"] + row["rewardRateByYear"])  / 100);
    						row["rateyear"] = row["rateByYear"] +row["benefits4new"]+row["rewardRateByYear"];//计算利息
    					}else{
    						row["rateByYearPlus"] = YRHX.toDecimal3(row["rateByYear"] / 100);
    						row["rateyear"]=row["rateByYear"];
    					}
                       
    					if(parseFloat(row['rewardRateByYear'])==0){
    						row["show_state"]='none'
    					}
    					
    					if(row["loanBalance"]>=1000000){
    						row["loanBalance"] = row["loanBalance"] / 1000000;
    						row["unit"]="万";
    					}else{
    						row["loanBalance"] = row["loanBalance"] / 100;
    						row["unit"]="元";
    					}
                       
                        var tmpHtml = html_tab.makeHtml(row);
                        $(".index_sele_ul").eq(index).append(tmpHtml);
                        circleProgress.refresh();
                    }//END for loop
                }else {
                    //无数据
                }
            },
            error:function(errData){
           
            }
    	})	
    }
    //精选项目 tab切换
    /*$(".loan_type li").on("click",function(){
        $(this).siblings().removeClass("selected");
        $(this).addClass("selected");
		var productType = $(this).attr("producttype");
		var index = $(this).index();
        $.index_loan(1, 4,index,productType);
    });*/
    $.index_loan(1,10,0);
    
    //债权转让
    YRHX.ajax({
        url: CONFIG.getRequestURI("queryLoanTransfer"),
        data: {"pageNumber": 1, "pageSize": 3,transState:'AB',orderParam:"transState",orderType:"desc"},
        success: function (sucData) {
        	 var debtTransHtml = '<tr>';
             debtTransHtml += "<td><span class='trans_title' title=#{title}>#{title}</span></td>";
            /* debtTransHtml += "<td><span class='red'><strong>#{rateByYear}</strong>%</span><span class='ratePlus #{isShow}'>奖励+#{rewardRateByYear}%</span></td>";*/
             debtTransHtml += "<td><span class='red'><strong>#{rateByYearAll}</strong>%</span></td>";
             debtTransHtml += '<td>#{transAmount}</td>';
             debtTransHtml += '<td>#{loanRecyCount}个月</td>';
             debtTransHtml += '<td>#{transFee}</td>';
             debtTransHtml += '<td><a href="/Z02?navTab=2" class="goBidBtn #{curStateCode}">#{curStateDesc}</span></td>';
             debtTransHtml += "</tr> ";
            var dataPath = sucData["list"];
            if (dataPath && dataPath.length > 0) {
            	$(".index_trans").find("tbody").html('');
                for (var i = 0; i < dataPath.length; i++) {
                    var row = dataPath[i];
                    if(row['productType'] =='A'){
                    	row['title']='质押宝'+row['loanNo'];
                    }else if(row['productType'] =='B'){
                    	row['title']='车稳盈'+row['loanNo'];
                    }else if(row['productType'] =='C'){
                    	row['title']='房稳赚'+row['loanNo'];
                    }else{
                    	row['title']=row["loanTitle"]+row['loanNo'];
                    }
                    if (row["transState"] == "A") {
                        row["curStateDesc"] = "转让中";
                        row["curStateCode"] = "";
                    } else if (row["transState"] == "B") {
                        row["curStateDesc"] = "转让成功";
                        row["curStateCode"] = "grayBtn";
                    }
                  //奖励年利率
                    /*row["rewardRateByYear"] =  row["rewardRateByYear"] / 100; 
                    if ( row["rewardRateByYear"] > 0) {
                    	row["isShow"] = "isShow";

                    }else{
                    	row["isShow"] = "";
                    }
                    row["rateByYear"] = row["rateByYear"] / 100;*/
                    row['rateByYearAll'] = (row["rateByYear"] + row['rewardRateByYear']) / 100;
                    row["transFee"] = YRHX.toDecimal2(row["transFee"] / 100)+'元';
                    row["transAmount"] = YRHX.toDecimal2(row["transAmount"] / 100) +'元';
                    var tempHtml = debtTransHtml.makeHtml(row);
                    $(".index_trans").find("tbody").append(tempHtml);

                }
            } else {
                $(".index_trans").find('tbody').html("<h1 class='noData'>暂无数据</h1>")
            }
        },
        error: function (errData) {

        }
    });
      
	//总排行
     YRHX.ajax({
    	url : CONFIG.getRequestURI("queryFundsOrderByBenJin"),
    	data: {"pageNumber": 1, "pageSize": 5},
        success: function (sucData) {
        	$('#rank_all').html('');
        	var rankHtml="<li><i></i><div class='name'>#{userName}</div><span>#{tzje}</span></li>"
        	var rankList=sucData["list"];
        	if(rankList && rankList.length > 0){
        		for(var i = 0; i < rankList.length; i++) {
        			var row = rankList[i];
        			var len=row["userName"].length;
        			if( len <2){
        				row["userName"]=row["userName"] + "***";
        			}else{
        				row["userName"]=row["userName"].substr(0,Math.floor(len / 2)) +"***";
        			}
        			
        			if(row){
        				row["tzje"]= YRHX.toDecimal2(row["tzje"] / 100) + '元';
        			}
        			var tmpHtml = rankHtml.makeHtml(row);
                    $('#rank_all').append(tmpHtml);
                }
        	}
        	else{
        		
        	}
        	$.rankICon('#rank_all li');
        	 
    	},
        error: function (errData) {

        }
    });
    $.rankICon=function( target ){
    	var target=$(target);
    	target.each(function(){
    		var i=$(this).index();
    		if(i == 0){
    			$(this).find('i').addClass('first');
    		}else if(i == 1){
    			$(this).find('i').addClass('second');
    		}else if(i == 2){
    			$(this).find('i').addClass('third');
    		}else{
    			$(this).find('i').addClass('four').text(i +1);
    		}
    	});
    }
  //月、日排行
    YRHX.ajax({
    	url : CONFIG.getRequestURI("duangToubiaoOrder2"),
        success: function (sucData) {
        	$('#rank_month').html('');
        	var rankHtml="<li><i></i><div class='name'>#{userName}</div><span>#{traceAmount}</span></li>"
        		//月排行
             	var monthData=sucData['month'];
             	if(monthData && monthData.length > 0){
             		for(var i=0 ;i < 5 ; i++){
            			var row=monthData[i];
            			var len=row["userName"].length;
               			if( len <2){
            				row["userName"]=row["userName"] + "***";
            			}else{
            				row["userName"]=row["userName"].substr(0,Math.floor(len / 2)) +"***";
            			}
            			row["traceAmount"] = YRHX.toDecimal2(row["traceAmount"] / 10.0/10.0) + '元';
            			var tempHTml=rankHtml.makeHtml(row);
            			$('#rank_month').append(tempHTml);
            		}
            	}else{
            		
            	}
             	$.rankICon('#rank_month li');
            	
        		//日排行
            	var dayData=sucData['day'];
            	if(dayData && dayData.length > 0){
            		for(var i=0 ;i < dayData.length ; i++){
            			var row=dayData[i];
            			var len=row["userName"].length;
            			if( len <2){
            				row["userName"]=row["userName"] + "***";
            			}else{
            				row["userName"]=row["userName"].substr(0,Math.floor(len / 2)) +"***";
            			}
            			row["traceAmount"] = YRHX.toDecimal2(row["traceAmount"] / 10.0/10.0) + '元';
            			var tempHTml=rankHtml.makeHtml(row);
            			$('#rank_day').append(tempHTml);
            		}
            	}else{
            		
            	}
            	$.rankICon('#rank_day li');

        },
        error: function (errData) {

        }
    });
    
   $("#logOut").on('click',function(){
		var loginName = YRHX.Cookie("loginName").get();
		YRHX.Cookie().clear();
		YRHX.Cookie("loginName",loginName).set();
		setTimeout(function(){
			window.location.href = "/" ;
		},300);
	});

});
//运营天数
function GetDateDiff()  
{  
	var date = new Date();
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var strDate = date.getDate();
	var startDate = date.getFullYear() +'-'+(date.getMonth() + 1) + '-'+date.getDate();
	var endDate = '2013-11-11';
    var startTime = new Date(Date.parse(startDate.replace(/-/g,   "/"))).getTime();     
    var endTime = new Date(Date.parse(endDate.replace(/-/g,   "/"))).getTime();     
    var dates = Math.abs((startTime - endTime))/(1000*60*60*24);     
    $('.dayCount').text(dates);
    //return  dates;    
}
//GetDateDiff();

//tab切换

$('.paddingBox p span').hover(function(){
	var index=$(this).index();
	$(this).addClass('selected').siblings().removeClass('selected');
	$('.list_con .rankList').eq(index).show().siblings().hide();
	$('.paddingBox p i').animate({'left':index*115 +'px'},200)
});

/*$('.loan_type li').click(function(){
	var index=$(this).index();
	$(this).addClass('selected').siblings().removeClass('selected');
	$('.index_loan .index_loan_list').eq(index).fadeIn('slow').siblings().fadeOut();
	$('.paddingBox p i').animate({'left':index*115 +'px'},500)
})*/


function indexPop(){
	var date = new Date();
	var year = date.getFullYear();
	var month = date.getMonth() + 1;
	var strDate = date.getDate();
	if (month >= 1 && month <= 9) {
	    month = "0" + month;
	}
	if (strDate >= 0 && strDate <= 9) {
	    strDate = "0" + strDate;
	}
	var currentDate = year +''+ month +'' + strDate;
	
	var cookieV = YRHX.Cookie('popShowDate').get();
	if(cookieV){
		if( cookieV > currentDate){
			$('body,html').css('overflow', 'hidden');
			to_center($('#yinying').show());
			to_center($('.bid_show').show());
			YRHX.Cookie('popShowDate',currentDate).set();
		}
		
	}else{
		$('body,html').css('overflow', 'hidden');
		to_center($('#yinying').show());
		to_center($('.bid_show').show());
		YRHX.Cookie('popShowDate',currentDate).set();
	}
	
	
}

//indexPop()

$('.bid_show .pop_close').click(function(){
	$('body,html').css('overflow', 'auto');
	$('#yinying').hide();
	$('.bid_show').hide();
});
$('.bid_show .pop_close2').click(function(){
	$('body,html').css('overflow', 'auto');
	$('#yinying').hide();
	$('.bid_show').hide();
});