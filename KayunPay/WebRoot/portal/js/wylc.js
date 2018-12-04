	$('.project_main').children('ul').children('li').click(function(){
		$(this).parent().find('.active').removeClass('active');
		$(this).addClass('active');
	});

	$.fn.tabChange = function( selector ){
		var that = $(this);

		that.click(function(){
			$(this).addClass('active').siblings().removeClass('active');
			var index = $(this).index();

			$('.'+ selector+'-list').hide();
			$('.'+ selector+'-list:eq('+index+')').show();

		})
	};

	//发表预告 
	
	//债权转让
	$.queryLoanTransfer = function ( pageNumber, pageSize) {
        YRHX.ajax({
            url: CONFIG.getRequestURI("queryLoanTransfer"),
            data: {"pageNumber": pageNumber || 1, "pageSize": pageSize || 10},
            success: function (sucData) {
				var debtTransHtml="<tr>";
				//第一个td【项目名称】
				var debtTransHtml = "<tr>";
                debtTransHtml += "<td width='300'>";
                debtTransHtml += "<div class='transTypeIcon'></div>";//图标
                debtTransHtml += "<h4><span style='float:left;'><a href='Z02_2?transferCode=#{transCode}'>#{loanTitle}</a></span><a class='way_icon' title='#{refundType}'>#{refundType}</a></h4>";
                debtTransHtml += "</td>";
			//第二个td【利率】
                debtTransHtml += "<td width='120'>";
                debtTransHtml += "<div class='blue lv'>#{rateByYear}<span>%</span></div>";
                debtTransHtml += "</td>";
			//第三个td【剩余本金】
                debtTransHtml += "<td width='140'>";
                debtTransHtml += "<div class='money'>#{leftAmount}</div>";
                debtTransHtml += "</td>";
			//第四个td【剩余期限】
				debtTransHtml +="<td width='130'>";
				debtTransHtml +="<div class='qx'><strong>#{loanRecyCount}</strong>个月</div>";
				debtTransHtml +="</td>";
			//第五个td【下一回款日】
                debtTransHtml += "<td width='120'>";
                debtTransHtml += "<div class='receive_next'>#{nextRecyDay}</div>";
                debtTransHtml += "</td>";
			//第六个td【承接价格】
                debtTransHtml += "<td width='120'>";
                debtTransHtml += "<div class='receive_next'>#{transAmount}</div>";
                debtTransHtml += "</td>";
			//第七个td【承接收益】
                debtTransHtml += "<td width='100'>";
                debtTransHtml += "<div class='receive_next'><span>#{transFee}</span></div>";
                debtTransHtml += "</td>";
			//第八个td【操作】
                debtTransHtml += "<td>";
                debtTransHtml += "<div class='meryItem' style='width: 156px; text-align: right;'><button class='#{transStateClass}' icon='#{transTypeIcon}' return_way='#{refundType}' remind_amount='#{leftAmount}' transfer_fee='#{transFee}' recy_day='#{nextRecyDay}' transfer_name='#{payUserName}' transfer_money='#{transAmount}' title='#{loanTitle}'  transferCode=#{transCode}>#{transState}</button></div><div class='clear'></div>";
                debtTransHtml += "</td>";
                debtTransHtml += "</tr>";
				
                var dataPath = sucData["list"];
                if (dataPath && dataPath.length > 0) {
                    $(".table_panel .debtTrans").find("tbody").html("");
                    for (var rowsIndex in dataPath) {
                        var row = dataPath[rowsIndex];
						var row = dataPath[rowsIndex];
                        if (row["transState"] == "A") {
	                        row["transState"] = "我要承接";
	                        row["timeDes"] = "剩余时间";
	                        row["gotUserNameDes"] = "承接收益";
	                        if( row["transFee"] < 0 ){
	                        	row["transFee"] = 0;
	                        }
	                        row["transFee"] = YRHX.toDecimal2(row["transFee"] / 100, "¥");
	                        row["transStateClass"] = "";
	                        row["gotDateTime"] = countLeftTime( strDate2Date(row["transDate"] + row["transTime"]) ,3);
                        } else if (row["transState"] == "B") {
                            row["transState"] = "转让成功";
                            row["transStateClass"] = "d";
                            row["timeDes"] = "承接日期";
                            row["gotUserNameDes"] = "承接人";
                            row["gotUserName"] = row["gotUserName"];
                            row["gotDateTime"] = formatDateTime(row["gotDate"], row["gotTime"])

                        }
                        //还款方式
                        if (row["refundType"] == "A") {
                        	row["refundType"] = "等额本息";
        				}else if (row["refundType"] == "B") {
        					row["refundType"] = "先息后本";
        			
        				} 
                        row["transTypeIcon"] = getLoanType(row["loanTitle"]);
                        row["rateByYear"] = ( row["rateByYear"] + row["rewardRateByYear"] ) / 100;
                        row["leftAmount"] = YRHX.toDecimal2(row["leftAmount"] / 100, "¥");
                        row["transAmount"] = YRHX.toDecimal2(row["transAmount"] / 100, "¥");
                        row["nextRecyDay"] = row["nextRecyDay"].substr(0, 4) + "-" + row["nextRecyDay"].substr(4, 2) + "-" + row["nextRecyDay"].substr(6, 2);
                        
						var tmpHtml = debtTransHtml.makeHtml(row);
                        $(".table_panel .debtTrans").find("tbody").append(tmpHtml);
                      
                    }//END for loop
                }else {
                    //无数据
                }
				 //分页
				$(".debtPage").pagination(sucData["pageNumber"], sucData["pageSize"], sucData["totalPage"], function () {
					var reqIndex = $(this).attr("index");
					$.queryLoanTransfer(reqIndex || 1)
				});
                $(".meryItem button").click(function () {
                   // window.location.href = "Z02_2?transferCode=" + $(this).attr("transferCode")
                	//承接模态框
                	if(!$(this).hasClass('d')){
                		var dislogElement = $('<div>').addClass('transfer_dialog').appendTo('body');
                		var title=$('<h3>').addClass('transfer_title').append(
                				$('<i>').addClass($(this).attr('icon'))
                		).append(
                				$('<span>').html($(this).attr('title'))
                		).append(
                				$('<div>').addClass('way_icon').html($(this).attr('return_way'))
                		).append(
                			$('<a>').addClass('transfer_del').click(function(){
                				$(dislogElement).remove();
                				$('body,html').css('overflow', 'auto');
                        		$('#yinying').hide();
                			})
                		).appendTo(dislogElement);
                		var transfer_con=$('<div>').addClass('transfer_con').appendTo(dislogElement);
                		transfer_con.append(
                				$('<p>').html('剩余本金：').append($('<span>').html($(this).attr('remind_amount')))
                		).append(
                				$('<p>').html('承接价格：').append($('<span>').html($(this).attr('transfer_money')))
                		).append(
                				$('<p>').html('承接收益：').append($('<span class="transer_color">').html($(this).attr('transfer_fee')))
                		).append(
                				$('<p>').html('下一回款日：').append($('<span class="recy_day">').html($(this).attr('recy_day')))
                		).append(
                				$('<p>').html('转让人：').append($('<span>').css('margin-left','35px').html($(this).attr('transfer_name')))
                		);
                		var payPwd=$('<div>').addClass('payPwd').append(
                			$('<input placeholder="请输入支付密码" type="password">')
                		).appendTo(dislogElement);
                		
                		var submit_btn=$('<div>').addClass('submit_btn').html('确认承接').appendTo(dislogElement);
                		var transfer_word=$('<div>').addClass('transfer_ye').html('账号可用余额：').append(
                			$('<span>').html()
                		).appendTo(dislogElement);
                		init_yinying();
                		$('body,html').css('overflow', 'hidden');
                		to_center($('#yinying').show());
                		to_center(dislogElement.show());
                	}
                	
                })
            },
            error: function (errData) {

            }
        })
    };
	
    
  //承接模态框
	function transfer_dialog(){
		
	}
    
    //新手专享
	$.queryNewhand = function ( pageNumber, pageSize) {
		YRHX.ajax({
            url: CONFIG.getRequestURI("queryFinancialBid"),
            data: {"pageNumber": pageNumber || 1, "pageSize": pageSize},
            success: function (sucData) {
                var html_tab = "<tr>";
                html_tab += "<td width='300'>";
                html_tab += "<div class='loanTypeIcon default #{loanTypeIcon}'></div>";//图标
                html_tab += "<h4><a href='Z02_1?loanCode=#{loanCode}' title='#{loanTitle}'>#{loanArea}#{loanTitle}</a><a class='way_icon' title='#{refundType}'>#{refundType}</a></h4>";
                html_tab += "</td>";
			//第二个td
                html_tab += "<td width='120'>";
                html_tab += "<div class='blue lv'>#{rateByYearPlus}<span>%</span></div>";
                //html_tab += "<div class='qx'>期限：#{loanTimeLimit}个月</div>";
                html_tab += "</td>";
			//第三个td
                html_tab += "<td width='170'>";
                html_tab += "<div class='money'>#{loanAmount}</div>";
                //html_tab += "<div class='m_type'>#{refundType}</div>";
                html_tab += "</td>";
			//第四个td
				html_tab +="<td width='130'>";
				html_tab +="<div class='qx'><strong>#{loanTimeLimit}</strong>个月</div>";
				html_tab +="</td>";
			//第五个td
                html_tab += "<td width='100'>";
                html_tab += "<div class='bb'>";
                html_tab += "<div class='bbf #{Zper}' style='background-position: -#{progress_p}px -1px;'></div>";
                html_tab += "<span>#{progress}%</span>";
                html_tab += "<div class='rr'></div>";
                html_tab += "<div class='lr'></div>";
                html_tab += "</div>";
                html_tab += "</td>";
			//第六个td
                html_tab += "<td><div class='t_button'>";
                html_tab += "<input id='a_#{loanCode}' toggle='0' type='text' placeholder='投标金额' maxLength='9' class='bidMoney'  />";
                html_tab += "<button id='b_#{loanCode}' lid='#{loanCode}' loanType='#{loanState}' >#{loanName}</button>";
                html_tab += "</div>";
                html_tab += "<div class='s_time' id='showTime_#{loanCode}' isTime='#{isTime}' startTime='#{startTime}' endTime='#{endTime}'>#{timeDesc} #{zfsj}</div>";
                html_tab += "</td>";
                html_tab += "</tr>";

                var returnData = sucData["loanInfos"]["list"];
                if (returnData && returnData.length > 0) {
                    $(".table_panel table").find("tbody").html("");
                    for (var rowsIndex in returnData) {
                        var row = returnData[rowsIndex];
                        var progress = 0;
                        row["isTime"] = 0;
                        row["loanTypeIcon"] = getLoanType(row["loanTitle"]);
                        if ( row["loanArea"].replace(/[ ]/g,"") ) {
                            row["loanArea"] = "【" + row["loanArea"] + "】"
                        }
                        if (row["loanState"] == "J" && row["loanBalance"] == 0) {
                            row["loanState"] = "M"
                        }
                        if (row["loanState"] == "J") {
                            row["loanName"] = "投标";
                            if (sucData["serverTime"] < row["releaseDate"] + row["releaseTime"]) {
                            	//定时标
                                row["loanName"] = "即将开始";
                                row["timeDesc"] = "等待发布";
                                row["isTime"] = 1;
                                row["startTime"] = strDate2Date(sucData["serverTime"]);
                                row["endTime"] = strDate2Date(row["releaseDate"] + row["releaseTime"]);
                                row["zfsj"] = "!"
                            } else {
                            	//3天倒计时
                                row["timeDesc"] = "";//提示剩余时间
                                var releaseTimeStamp = row["releaseDate"] + row["releaseTime"];
                                row["zfsj"] = "";
                            	//row["zfsj"] = countLeftTime( strDate2Date(releaseTimeStamp) ,3);
                            
                            }

                        } else if (row["loanState"] == "R") {
                            row["loanName"] = "待还已回收";
                            row["timeDesc"] = "";
                            row["zfsj"] = ""
                        } else if (row["loanState"] == "M") {
                            row["loanName"] = "满标待审";
                            row["timeDesc"] = "";
                            row["zfsj"] = ""
                        } else if (row["loanState"] == "N") {
                            $("#a_" + row["loanCode"]).hide();
                            row["loanName"] = "还款中";
                            row["timeDesc"] = "满标时间";
                            if (typeof(row["effectTime"]) == "undefined") {
                                row["zfsj"] = "更新中"
                            } else {
                                row["effectTime"] = row["effectDate"] + row["effectTime"];
                                row["zfsj"] = formatDateTime(row["effectTime"])
                            }
                        } else if (row["loanState"] == "O" || row["loanState"] == "P" || row["loanState"] == "Q") {
                            $("#a_" + row["loanCode"]).hide();
                            row["loanName"] = "已完成";
                            row["timeDesc"] = "满标时间";
                            if (typeof(row["effectTime"]) == "undefined") {
                                row["zfsj"] = "更新中"
                            } else {
                                row["effectTime"] = row["effectDate"] + row["effectTime"];
                                row["zfsj"] = formatDateTime(row["effectTime"])
                            }
                        } else {
                            row["loanName"] = "其他状态";
                            row["timeDesc"] = "满标时间"
                        }

                        //还款方式
                        if (row["refundType"] == "A") {
                            row["refundType"] = "等额本息";
                            row["refundTypeIcon"] = "icon2"
                        } else if (row["refundType"] == "B") {
                            row["refundType"] = "先息后本";
                            row["refundTypeIcon"] = "icon3"
                        } /*else if (row["refundType"] == "C") {
                            row["refundType"] = "到期还本息";
                            row["refundTypeIcon"] = "icon4"
                        }*/

                        //兼容奖励年利率
                        if (row["rewardRateByYear"] > 0) {
                            row["rateByYearPlus"] = YRHX.toDecimal2(row["rateByYear"] / 100) + '+' + YRHX.toDecimal2(row["rewardRateByYear"] / 100);

                        } else {
                            row["rateByYearPlus"] = YRHX.toDecimal2(row["rateByYear"] / 100);
                        }

                        //完成百分比
                        var tmpAmount = parseFloat(row["loanAmount"]);
                        var tmpBalance = parseFloat(row["loanBalance"]);
                        if (tmpBalance == 0) {
                            row["progress"] = 100
                        } else {
                            if (tmpBalance == tmpAmount) {
                                row["progress"] = "0"
                            } else {
                                row["progress"] = tmpBalance == 0 ? 0 : (tmpBalance / tmpAmount) * 100;
                                row["progress"] = Math.floor(100 - row["progress"])
                            }
                        }
                        if (row["progress"] == "0") {
                            row["Zper"] = "Zper";
                        }
                        row["progress_p"] = (row["progress"] - 1) * 83.46;
                        row["loanAmount"] = YRHX.toDecimal2(row["loanAmount"] / 100, "¥");
                        var tmpHtml = html_tab.makeHtml(row);
                        $(".table_panel table").find("tbody").append(tmpHtml);


                        //绑定状态
                        if (row["loanState"] == "J") {
                            if (sucData["serverTime"] < row["releaseDate"] + row["releaseTime"]) {
                                $("#a_" + row["loanCode"]).hide();
                                $("#b_" + row["loanCode"]).addClass("blue_button")
                            } else {
                                $("#b_" + row["loanCode"]).addClass("red_button goVote").attr("bidtoggle", "0");
                                $("#a_" + row["loanCode"]).attr("loanCode", row["loanCode"]);
                                $("#a_" + row["loanCode"]).attr("restcanvote", parseInt(row["loanBalance"] / 100));
                                $("#a_" + row["loanCode"]).attr("maxloanamount", row["maxLoanAmount"] / 100);
                                if (YRHX.Cookie("userName").get()) {
                                    $("#b_" + row["loanCode"]).parent().prepend('<div class="tip"><div class="text" style="line-height: 30px;"><p style="margin: 0px 0px 0px 10px;">可投金额：' + YRHX.toDecimal2(row["loanBalance"] / 100, "¥") + '</p><p style="margin: 0px 0px 0px 10px;" class="restMoney">可用余额：' + '</p></div><div class="jt"></div></div>')
                                } else {
                                    $("#b_" + row["loanCode"]).parent().prepend('<div class="tip"><div class="text" style="line-height: 30px;"><p style="margin: 0px 0px 0px 10px;">可投金额：' + YRHX.toDecimal2(row["loanBalance"] / 100, "¥") + '</p><p style="margin: 0px 0px 0px 10px;">请<a href="javascript:show_login_dialog()" style="float:none">登录</a>后再进行投标</p></div><div class="jt"></div></div>')
                                }
                            }
                        } else if (row["loanState"] == "N") {
                            $("#a_" + row["loanCode"]).hide();
                            $("#b_" + row["loanCode"]).addClass("gray_button")
                        } else if (row["loanState"] == "M") {
                            $("#a_" + row["loanCode"]).hide();
                            row["timeDesc"] = "满标时间";
                            $("#b_" + row["loanCode"]).addClass("blue_button")
                        } else if (row["loanState"] == "R") {
                            $("#a_" + row["loanCode"]).hide();
                            $("#b_" + row["loanCode"]).addClass("gray_button")
                        } else if (row["loanState"] == "O" || row["loanState"] == "P" || row["loanState"] == "Q") {
                            $("#a_" + row["loanCode"]).hide();
                            $("#b_" + row["loanCode"]).addClass("gray_button")
                        } else {

                        }

                    }//END for loop
                }else {
                    //无数据
                }

                //分页
                $(".bidPage").pagination(sucData["loanInfos"]["pageNumber"], sucData["loanInfos"]["pageSize"], sucData["loanInfos"]["totalRow"], function () {
                    var reqIndex = $(this).attr("index");
                    buildPageData(reqIndex || 1, 10)
                });
                //倒计时
                $(".s_time").each(function () {
                    function initInterval(sc, text) {
                        setInterval(function () {
                            var that = $("#" + sc);
                            var startTime = parseInt(that.attr("starttime"));
                            var endTime = parseInt(that.attr("endtime"));
                            
                            that.text(text + showLastTime((endTime - startTime) / 1000));
                            that.attr("starttime", startTime + 1000)
                        }, 1000)
                    }

                    var that = $(this);
                    var isTime = that.attr("istime");
                    if (isTime == "1") {
                        var sc = that.attr("id");
                        initInterval(sc, "剩余时间：")
                    }
                });
                //投标btn
                $(".goVote").click(function () {
                    var that = $(this);
                    
                    
                  
                    if (that.attr("bidtoggle") == "0") {
                        if (YRHX.Cookie("userCode").get()) {
                            var voteMoney = $(that).prev().val();
                            if (voteMoney == "") {
                            	$(that).popUp("err","请输入投标金额");
                                //$.popTips("popTipErr", "请输入投标金额");
                                return false
                            } else {
                                if (voteMoney.indexOf(".") >= 0) {
                                	$(that).popUp("err","金额必须为大于0的整数");
                                    //$.popTips("popTipErr", "请输入整数");
                                    return false
                                }
                            }
                            var loanState = that.attr("loanType");
                            if (loanState == "J") {
                                that.attr("bidToggle", "1");
                                bidding(that.attr("lid"))
                            }
                        } else {
                        	show_login_dialog();
                            /* $.popTips("popTipErr", "请先登录!", function () {
                            	
                            }) */
                        }
                    }
                });
				
                $(".tip").mouseleave(function(){
                	if($('.bidMoney:focus').length == 0){
						$(this).hide();
					}
				});
                
				//投资金额输入框事件
                $(".bidMoney").focus(function () {
                    var that = $(this);
					$(that).prev().show();
					$(that).blur(function(){
						if($('.tip:hover').length > 0){
							return;
						}else{
							$(that).prev().hide();
						}
					});
                    if (YRHX.Cookie("userName").get()) {
                        if (that.attr("toggle") == 0) {
                            YRHX.ajax({
                                url: CONFIG.getRequestURI("queryBidding4User"),
                                data: {
                                    "loanCode": that.attr("loanCode")
                                },
                                success: function (sucData) {
                                    that.attr("toggle", "1");
                                    that.attr("avBalance", parseInt(sucData.avBalance / 100));
                                    var myMaxloanAmountEle = parseInt(sucData["totalAmount"] / 100); //投资人此标最大投标金额
                                    var maxLoanAmountEle = that.attr("maxloanamount");//最大限制投标金额
                                    var loanBalanceEle = that.attr("restcanvote");	//标书剩余金额
                                    var myAvBalanceEle = that.attr("avbalance");	//投资人可用余额
                                    var leftLoanAmountEle = Math.abs(maxLoanAmountEle - myMaxloanAmountEle);//投资人剩余投资金额
                                    var uCanVote = 0;

                                    if (leftLoanAmountEle - loanBalanceEle >= 0) {
                                        if (loanBalanceEle - myAvBalanceEle > 0) {
                                            uCanVote = myAvBalanceEle;
                                        } else {
                                            uCanVote = loanBalanceEle;
                                        }
                                    } else {
                                        if (leftLoanAmountEle - myAvBalanceEle > 0) {
                                            uCanVote = myAvBalanceEle;
                                        } else {
                                            uCanVote = leftLoanAmountEle;
                                        }
                                    }

                                    that.val(uCanVote);
                                    $(".restMoney").each(function () {
                                        $(this).text("可用余额：" + YRHX.toDecimal2(sucData.avBalance / 100, "¥"))
                                    })
                                }, error: function (data) {
                                }
                            })
                        }
                    }
                })
            } ,
            error: function (data) {
            }
        })
	}
	
	//代金券
	var coupon_num;
	//用户现金券的状态
	function ticket(bidAmount,loanMonth,type){
		  YRHX.ajax({
           url: CONFIG.getRequestURI("queryTickets4User"),
           data:{pageNumber:1,pageSize:30,tstate:'A',ttype:type,loanMonth:loanMonth},
           success: function (sucData) {
        	   //判断是否有加息券 
        	   var haveratetickets=sucData["haveratetickets"];
        	   ratevip = sucData["rewardInterest"];//会员自动加息利率
        	   //新手标不能使用加息券
        	   if(isnoobbid){
					$(".user-quan").hide();
				}else{
					$(".user-quan").show();
				}
//       	   else{
//					if(haveratetickets){
//						$(".user-quan").show();
//					}else{
//						$(".user-quan").hide();
//					}
//				}
           $(".coupon_list ul").html("");
           $('.icon_list').html("");
          	var first_ticket=sucData["list"];
          	var sum=first_ticket.length;
          	var arrNo=[];
          	if(thisProductType=="E"&&type=="A"){
          		first_ticket={};
          		sum=0;
          	}
          	if((first_ticket && sum >0)||type=="D"){
          		if(type=="D"){
             		var ticketHtml="<li class='#{disable}' type='#{ttype}'>";
             		ticketHtml+="<div class='coupon_serve' code='#{tcode}'>";
             		ticketHtml+="<div>剩余额度</div>";
             		ticketHtml+="<p><span>#{examount}</span></p>";
             		ticketHtml+="<div class='date'>#{expDate}到期</div>";
             		ticketHtml+="</div>";
             		ticketHtml+="</li>";
             		$('.coupon_none').hide();
               		$('.coupon_none2').hide();
               		$('.coupon_detail').show();
               		$('.vip_coupon_selected').hide();
             		coupon_num=0;
             		$('.coupon_iconl,.coupon_iconr').hide();
             		$('.coupon_list ul').css('width',"0px");
             		var row=first_ticket[0];
             		if(bidAmount*100>row["examount"]){
             		row["disable"]="disable";
             		}
             		row["examount"]=row["examount"]/100;
             		var noHtml=ticketHtml.makeHtml(row);
             		arrNo.push(noHtml);
             }else{
            var ticketHtml="<li class='#{disable}' type='#{ttype}'>";
           	ticketHtml+="<div class='coupon_serve' code='#{tCode}'>";
           	ticketHtml+="<p><span>#{amount}</span></p>";
           	ticketHtml+="<font>投资<s>#{depict}</s>可使用</font>";
           	ticketHtml+="<div>可投<s>#{loanMonth}</s>月标</div>";
           	ticketHtml+="<div class='date'>#{expDate}到期</div>";
           	ticketHtml+="</div>";
           	ticketHtml+="</li>";
           	
           		$('.coupon_none').hide();
           		$('.coupon_none2').hide();
           		$('.coupon_detail').show();
           		$('.vip_coupon_selected').hide();
           		coupon_num=Math.ceil(sum / 3);
           		if(coupon_num < 2){
           			$('.coupon_iconl,.coupon_iconr').hide();
           		}else{
           			$('.coupon_iconl,.coupon_iconr').show();
           		}
           		var coupon_width=coupon_num * 484 + "px"
           		$('.coupon_list ul').css('width',coupon_width);
           		
           		
           		for(var i = 0; i < first_ticket.length; i++){
           			var row=first_ticket[i];
           			var limit=row["useEx"];
           			var limitData=JSON.parse(limit);
           			row["amount"]="￥"+row["amount"] /100;
           			if(row["ttype"]=="C"){
           			row["amount"]="+"+row["rate"] /100+"%";
           			}
           			if(row["ttype"]=="A"){
             			row["depict"]=limitData.amount / 100+"以上";
					}else if(row["ttype"]=="C"){
						if(limitData.amount==0){
						row["depict"]="任意金额";
						}else{
						row["depict"]=limitData.amount / 100+"以内";
						}
					}
					ticketHtml.replace("#{depict}", row["depict"]);
           			row["expDate"] = row["expDate"].dateformat();
           			if(row["loanMonth"]==null||row["loanMonth"]==""||row["loanMonth"]=="0"){
      					row["loanMonth"]="任意";
      				}else{
      					row["loanMonth"]=row["loanMonth"].replace(/-/g,",");
      				}
           			if(row["ttype"]=="A"){//投资金额大于券面金额
        				if(bidAmount > limitData.amount / 100 - 1){
               				var tmpHtml=ticketHtml.makeHtml(row);
               				$(".coupon_list ul").append(tmpHtml)
               			}else{
             				row["disable"]="disable";
             				var noHtml=ticketHtml.makeHtml(row);
             				arrNo.push(noHtml);
             			}
        			}else if(row["ttype"]=="C"){
        					var rewardrateexamount=limitData.amount / 100;
        				if(rewardrateexamount==0){
        					var tmpHtml=ticketHtml.makeHtml(row);
               				$(".coupon_list ul").append(tmpHtml)
        				}else if(rewardrateexamount>0){
        					if(bidAmount<rewardrateexamount+1){
        					var tmpHtml=ticketHtml.makeHtml(row);
               				$(".coupon_list ul").append(tmpHtml)
        					}else{
        					row["disable"]="disable";
             				var noHtml=ticketHtml.makeHtml(row);
             				arrNo.push(noHtml);
        					}
        				}
        			}
             	}  
             }
           		for(var i=0;i<arrNo.length;i++){
           			$(".coupon_list ul").append(arrNo[i]);
           		} 
           		
           		//图标
           		if(coupon_num >= 1){
           			for(var i = 0; i < coupon_num; i++){
           				var iconHtml="<b></b>";
           				$('.icon_list').append(iconHtml);
           			}
           			var icon_width=$('.icon_list').width();
               		var icon_left=(484 - icon_width) / 2 +"px";
               		$('.icon_list').css('left',icon_left); 
           		}
           		if(coupon_num > 1){
           			$('.coupon_iconr').addClass('more');
           		}
           		$('.icon_list').find('b').eq(0).addClass('cur');
           		//现金券的默认选择
           		 $('.coupon_list li').each(function(){
             			var that=$(this);
           			if(!that.hasClass('disable')){
           				cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
						var rate=thisrate+ratevip*100;
   		    			var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
   		    			this_income.text(incomePer);
           					/*that.addClass('cur');
           					cash=that.find('p span').html();
     		    			var finalA=parseFloat(bidAmount);
     		    			if(that.attr("type")=="A"){
     		    			cash=cash.substring(1,cash.length);
     		    			cash_ele.html("投资成功,可抵扣现金<font>"+cash+"</font>元");
	               	     	finalA-=parseFloat(cash);
	               	     	this_income.text(thisincome);
     		    			}else if(that.attr("type")=="C"){
     		    			cash=cash.substring(1,cash.length-1);
     		    			cash_ele.html("投资成功,可加息<font>"+cash+"</font>%");
     		    			var rate=thisrate+cash*100;
     		    			var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
     		    			this_income.text(incomePer);
     		    			}else if(that.attr("type")=="D"){
           		    			cash_ele.html("投资成功可加息<font>"+1+"</font>%");
           		    			var rate=thisrate+1*100;
           		    			var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
           		    			this_income.text(incomePer);
           		    			};*/
   		    			    var finalA=parseFloat(bidAmount);
	                   		final_amount.text(finalA);
             	       		/*t_code=that.find('.coupon_serve').attr('code');*/
	                   		return false;
           			}else{
           				if(that.attr("type")=="A"){
								/*cash_ele.html("投资成功可抵扣现金<font>0</font>元");*/
           					      cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
                			      var rate=thisrate+ratevip*100;
        		    		      var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
        		    		      this_income.text(incomePer);
								}else if(that.attr("type")=="C"||that.attr("type")=="D"){
								/*cash_ele.html("投资成功可加息<font>0</font>%");*/
									cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
				        			var rate=thisrate+ratevip*100;
						    		var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
						    		this_income.text(incomePer);
								}
     		    				final_amount.text(bidAmount);
           			}
           		}); 
      		}else{
      			if(type=="A"){
        			$('.coupon_none').show();
        			$('.coupon_none2').hide();
        			}else if(type=="C"){
        			$('.coupon_none').hide();
        			$('.coupon_none2').show();
        			}
      			vip_cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
    			var rate=thisrate+ratevip*100;
	    		var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
	    		this_income.text(incomePer);
           		$('.coupon_detail').hide();
           		$('.vip_coupon_selected').show();
           		$('.coupon_list ul').html('');
           		this_amount.html(bidAmount);
           		final_amount.html(bidAmount);
           	}
           }
       });
	}
	$('#usable').click(function (){
		var bidAmount=thisbidAmount;
		var loanMonth=thisloanmonth;
		var that=$(this);
		t_code="";
		$('#usable2').removeClass("act");
		$('#usable3').removeClass("act");
		that.addClass("act");
		$('.remind').hide();
		ticket(bidAmount,loanMonth,"A");
	});
	$('#usable2').click(function (){
		var bidAmount=thisbidAmount;
		var loanMonth=thisloanmonth;
		var that=$(this);
		t_code="";
		$('#usable').removeClass("act");
		$('#usable3').removeClass("act");
		that.addClass("act");
		$('.remind').hide();
		ticket(bidAmount,loanMonth,"C");
	});
	$('#usable3').click(function (){
		var bidAmount=thisbidAmount;
		var loanMonth=thisloanmonth;
		var that=$(this);
		t_code="";
		$('#usable').removeClass("act");
		$('#usable2').removeClass("act");
		that.addClass("act");
		$('.remind').show();
		ticket(bidAmount,loanMonth,"D");
	});
	//取消投标
	$('.coupon_cancle,.couponDialog .del,.coupon_foot .next_close').click(function(){
		window.location.reload();
	});
	
	
	//选择现金券
	$('body').delegate(".coupon_list ul li","click",function(){
		var that=$(this);	
		if(that.hasClass('disable')){
			return false;
		}else{
			var use_cash=$(this).find('p span').html();
			if($(this).hasClass('cur')){
				$(this).removeClass('cur');
				final_amount.text(this_amount.html());
				this_income.text(thisincome);
				if(that.attr("type")=="A"){
				/*cash_ele.html("投资成功可抵扣现金<font>0</font>元");*/
					cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
        			var rate=thisrate+ratevip*100;
		    		var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
		    		this_income.text(incomePer);
				}else if(that.attr("type")=="C"||that.attr("type")=="D"){
				/*cash_ele.html("投资成功可加息<font>0</font>%");*/
					cash_ele.html("投资成功可享受会员 加息<font>"+ratevip+"</font>%");
        			var rate=thisrate+ratevip*100;
		    		var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
		    		this_income.text(incomePer);
				}
				t_code="";
			}else{
				$(this).addClass('cur').siblings().removeClass('cur');
				t_code=$(this).find('.coupon_serve').attr('code');
				cash=$(this).find('p span').html();
				if(that.attr("type")=="A"){
       		    	cash=cash.substring(1,cash.length);
       		    	cash_ele.html("现金劵金额满标放款以红包形式返还<font>"+cash+"</font>元");
					//var confirm_amount=parseFloat($('.this_amount').html())-parseFloat(cash);
       		    	var confirm_amount=parseFloat($('.this_amount').html());
					final_amount.text(confirm_amount);
					this_income.text(thisincome);
       		    }else if(that.attr("type")=="C"){
       		    	cash=cash.substring(1,cash.length-1);
       		    	cash_ele.html("投资成功可加息<font>"+cash+"</font>%");
       		    	var rate=thisrate+cash*100;
       		    	var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
       		    	this_income.text(incomePer);
       		    }else if(that.attr("type")=="D"){
       		    	cash_ele.html("投资成功可加息<font>"+1+"</font>%");
       		    	var rate=thisrate+1*100;
       		    	var incomePer=getincomePer(rate,thisbidAmount,thisloanmonth,thisrefundType);
       		    	this_income.text(incomePer);
       		    };
			}
			
		}
	});
	function getincomePer(rateYear,bidAmount,time,refundType_row){
		var incomePer ;
		if( refundType_row == "A"){
		   	incomePer =  Math.floor ( YRHX.licai( bidAmount * 100 ,rateYear , time).denge4year()[0]["benxi"]*  time - bidAmount * 100) / 100 ;
		}else if(refundType_row == "B"){
		   	//此标投一元钱总收益            按月付息，到期还本
		   	incomePer = Math.floor(  parseFloat(rateYear/12/10.0/10.0/10.0/10.0) * time * bidAmount * 100 ) / 100;
		} 
		return incomePer;
	}
	//代金券切换
	$('.coupon_iconl').click(function(){
		move(0,'.coupon_list',484,500);	
	});
	$('.coupon_iconr').click(function(){
		move(1,'.coupon_list',484,500);	
	});
	var icon_index=0;
	function move(index,hall,width,speed){//index(左移还是右移)，hall(scrolleft),inner(容器),img(单个的个数),width(外层的宽度),
		var hall=$(hall);
		var icon=$(icon);
		var left = hall.scrollLeft();
		var max = width * (coupon_num-1);
		if(index == 0){
			if(!hall.is(':animated')){
				if(left == 0){
					return false;
				}else{
					icon_index--;
					$('.coupon_iconr').removeClass('more').addClass('more');
					if(icon_index ==0){
						$('.coupon_iconl').removeClass('more');
					}else{
						$('.coupon_iconl').removeClass('more').addClass('more');
					}
					hall.animate({scrollLeft:(left - width) + "px"},speed);
					$('.icon_list b').eq(icon_index).addClass('cur').siblings().removeClass('cur');
				}
			}
		}else{
			if(!hall.is(':animated')){
				if(left == max){
					return false;
				}else{
					if($('.icon_list b').length==1){
						return false;
					}else{
						icon_index++;
						$('.coupon_iconl').removeClass('more').addClass('more');
						if(icon_index ==coupon_num -1){
							$('.coupon_iconr').removeClass('more');
						}else{
							$('.coupon_iconr').removeClass('more').addClass('more');
						}
						hall.animate({scrollLeft:(left + width) + "px"});
						$('.icon_list b').eq(icon_index).addClass('cur').siblings().removeClass('cur');
					}
					
				}
			}
		}
	}

	    
