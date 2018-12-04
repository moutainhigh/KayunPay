$(function() {
	var date = new Date();
	var now_y = date.getFullYear();
	var now_m = date.getMonth() + 1;
	var now_d = date.getDate();
	// 初始化日历
	buildCalender(now_y, now_m);

	var today_m, today_d, today_y;
	$('#year').html(now_y);
	$('#month').html(now_m);
	var year = now_y;
	var month = now_m;
	addCur(now_d);

	var today = format_time(now_y, now_m, now_d);
	report(today);

	$('.left').click(function() {
		if (month == 1) {
			$('#month').text('12');
			$('#year').text(year - 1);
			year = year - 1;
			month = 12;
			buildCalender(year, month);
			addCur(1);
			var firstDay = format_time(year, month, 1);
			report(firstDay);

			if (year == now_y && month == now_m) {
				addCur(now_d);
				report(today);

			}
		} else {
			$('#month').text(month - 1);
			month = month - 1;
			buildCalender(year, month);
			addCur(1);
			var firstDay = format_time(year, month, 1);
			report(firstDay);
			if (year == now_y && month == now_m) {
				addCur(now_d);
				report(today);
			}

		}
	});
	$('.right').click(function() {
		if (year == now_y && month == now_m) {
			return false;
		} else {
			if (month == 12) {
				$('#month').text(1);
				$('#year').text(year + 1);
				month = 1;
				year = year + 1;
				buildCalender(year, month);
				addCur(1);
				var firstDay = format_time(year, month, 1);
				report(firstDay);
				if (year == now_y && month == now_m) {
					addCur(now_d);
					report(today);
				}
			} else {
				$('#month').text(month + 1);
				month = month + 1;
				buildCalender(year, month);
				addCur(1);
				var firstDay = format_time(year, month, 1);
				report(firstDay);
				if (year == now_y && month == now_m) {
					addCur(now_d);
					report(today);
				}
			}
		}

	});
})
var private_Day_title = [ '日', '一', '二', '三', '四', '五', '六' ];
// 日期格式化
function format_time(first, second, third) {
	if (second < 10) {
		second = '0' + second;
	} else {
		second = second.toString();
	}
	if (third < 10) {
		third = '0' + third;
	} else {
		third = third.toString();
	}
	var time = first + second + third;
	return time;
}

function addCur(now_d) {
	$('.calender-cell').each(function() {
		if (!$(this).hasClass('calender-cell-dark')) {
			if ($(this).html() == now_d) {
				$(this).addClass('cur').siblings().removeClass('cur');
			}
		}

	});
}

/* 获取某一月有多少天 */
function getDateLengthInMonth(year, month) {
	return new Date(year, month, 0).getDate();
}
/* 获取某一月第一天是周几 */
function getFirstDayInMonth(year, month) {
	return new Date(year, month - 1, 1).getDay();
}

/* 绘制单月表格 */
function PaintMonthTable(year, month) {
	var firstDay = getFirstDayInMonth(year, month), length = getDateLengthInMonth(
			year, month), lastMonthLength = getDateLengthInMonth(year,
			month - 1), i, html = '<div class="calender-month">';

	// 构建星期
	html += '<div class="calender-caption">';
	for (i = 0; i < 7; i++) {
		html += '<div class="calender-cell">' + private_Day_title[i] + '</div>';
	}
	html += '</div>';// caption结尾的标签

	// 构建日期列表
	html += '<div class="calender-content">';
	// 循环输出月前空格
	for (i = 1; i < firstDay + 1; i++) {
		html += '<div class="calender-cell calender-cell-dark">'
				+ (lastMonthLength - firstDay + i) + '</div>';
	}
	// 循环输出当前月所有天
	for (i = 1; i < length + 1; i++) {
		html += '<div class="calender-cell">' + i + '</div>';
	}
	// 循环输出下个月初日期
	for (i = 1; i < 8 - (length + firstDay) % 7; i++) {
		html += '<div class="calender-cell calender-cell-dark">' + i + '</div>';
	}
	return html;
}
// 绘制日历
function buildCalender(year, month) {
	calender_html = PaintMonthTable(year, month);
	document.getElementById('calender').innerHTML = calender_html;
	var date = new Date();
	var now_y = date.getFullYear();
	var now_m = date.getMonth() + 1;
	var now_d = date.getDate();
	if (year == now_y && month == now_m) {
		$('.calender-cell').each(function() {
			var that = $(this);
			if (!that.hasClass('calender-cell-dark')) {
				if (that.html() > now_d) {
					that.addClass('calender-cell-dark');
				}
			}
		});
	}

	$('.calender-cell').click(function() {
		if (!$(this).hasClass('calender-cell-dark')) {
			$(this).addClass('cur').siblings().removeClass('cur');
			var date_m = $('#month').html();
			var date_d = $(this).html();

			if (date_m.length < 2) {
				date_m = "0" + date_m;
			}
			if (date_d.length < 2) {
				date_d = "0" + date_d;
			}
			var time = $('#year').html() + date_m + date_d;
			report(time);
		}
	});
}
// 每日数据
function report(time) {
	YRHX.ajax({
		url : CONFIG.getRequestURI("duangAnyDayReport"),
		data : {
			anyDate : time
		},
		success : function(sucData) {
			var anyDay_payAmount = YRHX.toDecimal2(sucData.anyDay_payAmount / 100);
			var anyDay_zrzqbs = sucData.anyDay_zrzqbs;
			var anyDay_okzqbs = sucData.anyDay_okzqbs;
			var anyDay_okzqAmount = YRHX.toDecimal2(sucData.anyDay_okzqAmount / 100);;
			var anyDay_xzlcr = sucData.anyDay_xzlcr;
			var anyDay_xzjkr = sucData.anyDay_xzjkr;
			var anyDay_xujie = sucData.anyDay_xujie;
			var anyDay_tzrc = sucData.anyDay_tzrc;
			var anyDay_loanTotal1 = sucData.anyDay_loanTotal1;
			var anyDay_loanTotal2 = sucData.anyDay_loanTotal2;
			var anyDay_loanTotal3 = sucData.anyDay_loanTotal3;
			//var anyDay_loanTotal4 = sucData.anyDay_loanTotal4;
			var anyDay_loanTotal5 = sucData.anyDay_loanTotal5;
			var yesterdayStart = sucData.yesterdayStart;
			var yesterdayEnd = sucData.yesterdayEnd;

			$('.day_amount').text(anyDay_payAmount);
			$('.day_amount2').text(anyDay_okzqAmount);
			$('.day_count').text(anyDay_zrzqbs);
			$('.suc_count').text(anyDay_okzqbs);
			$('.newly_finance').text(anyDay_xzlcr);
			$('.newly_borrow').text(anyDay_xzjkr);
			$('.renew').text(anyDay_xujie);
			$('.invest_count').text(anyDay_tzrc);
			$('.bid_count').text(anyDay_loanTotal1);
			$('.pledge_count').text(anyDay_loanTotal2);
			$('.mortgage_count').text(anyDay_loanTotal3);
			//$('.other_count').text(anyDay_loanTotal4);
			$('.wdl_count').text(anyDay_loanTotal5);
			//$('.yesterdayStart_count').text(yesterdayStart);
			//$('.yesterdayEnd_count').text(yesterdayEnd);

		},
		error : function(errData) {
			// alert(errData.return_info || "请求服务失败");
		}
	});
}

// 累计交易切换
$('.deal_all .icon a').click(function() {
	var i = $(this).index();
	$(this).addClass('active').siblings().removeClass('active');
	$('.deal_canvas .pic1').eq(i).show().siblings().hide();

});

function dateChange(value) {
	var str1 = value.substr(0, 4);
	var str2 = value.substr(4, 2);
	var str3 = value.substr(6, 2);
	var len = value.length;
	if (len == 6) {
		var value = str1 + "-" + str2;
	} else {
		var value = str1 + "-" + str2 + '-' + str3;
	}
	return value;
}
var colData1_x = [], colData1_y = [];
var colData2_x = [], colData2_y = [];
$.columnData = function(type) {
	if (type == "B") {
		var ajaxUrl = "duangMonthCount";
	} else if (type == "A") {
		var ajaxUrl = "duangDayCount";
	}

	YRHX.ajax({
		url : CONFIG.getRequestURI(ajaxUrl),
		success : function(sucData) {
			var len = sucData.length - 1;
			if (type == "A") {
				for (var i = len; i >= 0; i--) {
					sucData[i].date = dateChange(sucData[i].date);
					sucData[i].data = sucData[i].data ;
					colData1_x.push(sucData[i].date);
					colData1_y.push(sucData[i].data);
				}
				col1();
			} else if (type == "B") {
				for (var i = len; i >= 0; i--) {
					sucData[i].date = dateChange(sucData[i].date);
					sucData[i].data = sucData[i].data ;
					colData2_x.push(sucData[i].date);
					colData2_y.push(sucData[i].data);
				}
				col2();
			}

		},
		error : function(errData) {
			// alert(errData.return_info || "请求服务失败");
		}
	});
}
$.columnData('A');
$.columnData('B');

// 按日累计交易笔数
function col1() {
	var myChart1 = echarts.init(document.getElementById('canvas1'));
	var option1 = {
		title : {
			text : '',
			subtext : '单位(笔)',
			x : '20px',
			subtextStyle : {
				fontSize : 14,
				color : '#666'
			}
		},
		tooltip : {
			trigger : 'axis',
			padding : [ 10, 13, 10, 13 ],
			axisPointer : { // 坐标轴指示器，坐标轴触发有效
				type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
			}
		},
//		legend : {
//			data : [ '其他' ]
//		},
		grid : {
			left : '3%',
			right : '4%',
			bottom : '3%',
			width : '1034px',
//			borderColor : 'yellow',
			containLabel : true
		},
		xAxis : [ {
			type : 'category',
			nameGap : '50px',
			data : (function() {
				return colData1_x;
			})()
		} ],
		yAxis : [ {
			type : 'value'
		} ],
		series : [ {
			name : '按日累计交易笔数',
			type : 'bar',
			barWidth : 22,
			itemStyle : {
				normal : {
					color : '#A8D5F6',
					barBorderRadius : 3

				}
			},
			data : (function() {
				return colData1_y;
			})()
		// 显示实时数据
		},

		]
	};
	myChart1.setOption(option1);
}

// 按月累计交易笔数
function col2() {
	var myChart6 = echarts.init(document.getElementById('canvas6'));
	var option6 = {
		title : {
			text : '',
			subtext : '单位(笔)',
			x : '20px',
			subtextStyle : {
				fontSize : 14,
				color : '#666'
			}
		},
		tooltip : {
			trigger : 'axis',
			axisPointer : { // 坐标轴指示器，坐标轴触发有效
				type : 'shadow' // 默认为直线，可选为：'line' | 'shadow'
			}
		},
//		legend : {
//			data : [ '其他' ]
//		},
		grid : {
			left : '3%',
			right : '4%',
			bottom : '3%',
			width : '1034px',
//			borderColor : 'yellow',
			containLabel : true
		},
		xAxis : [ {
			type : 'category',
			nameGap : '50px',
			data : (function() {
				return colData2_x;
			})()
		} ],
		yAxis : [ {
			type : 'value'
		} ],
		series : [ {
			name : '按月累计交易笔数',
			type : 'bar',
			barWidth : 33,
			itemStyle : {
				normal : {
					color : '#A8D5F6',
					barBorderRadius : 3

				}
			},
			data : (function() {
				return colData2_y;
			})()
		// 显示实时数据
		},

		]
	};
	myChart6.setOption(option6);
}

// 饼图
var circleData1_x = [], circleData1_y = [];

$.circleData = function() {
	YRHX.ajax({
		url : CONFIG.getRequestURI("duangCircularReport"),
		success : function(sucData) {
			var len = sucData.length;
			// 近30日借款期限分布
			var one = sucData['0'];
			var two = sucData['1'];
			var three = sucData['2'];
			var four = sucData['3'];
			pie1(one);
			pie2(two);
			pie3(three);
			pie4(four);

		},
		error : function(errData) {
			// alert(errData.return_info || "请求服务失败");
		}
	});
	
	YRHX.ajax({
		url : CONFIG.getRequestURI("duangUserCount"),
		success : function(sucData) {
//			var len = sucData.length;
//			console.log(sucData)
			
			var amtData = sucData.amtData;
			var sort = []
			for(var index in amtData){
				sort.push(amtData[index] )
			}
			sort = sort.sort(function(a,b){return b - a})
			
			var newOrder = []
			for(var i = 0;i<10;i++){ //TOP10
				for(var index in amtData){
					if( sort[i] == amtData[index]){
						var formData = {}
						formData[chinaProvice[index]] = YRHX.toDecimal2(amtData[index] / 10.0/10.0,"¥")
						newOrder.push(formData)
					}
				}	
			}
//			console.log(newOrder)
			
			var outHtmlAll = '';
			for(var i = 0  ; i< 10;i++){
				if( i == 0){
					var rankClass = 'rankFirst';
					var rankNumber = ''
				}else if( i == 1){
					var rankClass = 'rankSecond';
					var rankNumber = ''
				}else if( i == 2){
					var rankClass = 'rankThird';
					var rankNumber = ''
				}else{
					var rankClass = 'rankNo';
					var rankNumber = i+1
				}
				for(var key in newOrder[i]){
					outHtmlAll += '<li><i class='+rankClass+'>'+rankNumber+'</i>'+key+'<span class="rankMoney">'+newOrder[i][key]+'</span></li>'
				}
			}
			$("#rankRow").html(outHtmlAll)
			
			var areaData = sucData.areaData
			var newAreaData = [];
			for(var index in areaData){
				var proviceN = index.replace('area','amt')
				newAreaData.push({
					'name': chinaProvice[proviceN],
					'value': areaData[index]
				})
			}
//			console.log(newAreaData)
			
			pie5({
				'age10_19': sucData.ageData.age10_19,
				'age20_29': sucData.ageData.age20_29,
				'age30_39': sucData.ageData.age30_39,
				'age40_49': sucData.ageData.age40_49,
				'age50_59': sucData.ageData.age50_59,
				'age60_150': sucData.ageData.age60_150
			});
			pie6({
				'sexF': sucData.sexData.sexF,
				'sexM': sucData.sexData.sexM
			});
			mapData(newAreaData)
			

		},
		error : function(errData) {
			// alert(errData.return_info || "请求服务失败");
		}
	});
	
}
$.circleData();
// 近30日借款标期限分布
function pie1(one) {
	var myChart2 = echarts.init(document.getElementById('canvas2'));
	var option2 = {
		tooltip : {
			trigger : 'item',
			formatter : "{a} <br/>{b}: {c} ({d}%)",
		},
//		legend : {
//			orient : 'vertical',
//			x : 'left',
//		// data:['直达','营销广告','搜索引擎','邮件营销','联盟广告','视频广告','百度','谷歌','必应','其他']
//		},
		series : [ {
			name : '期限分布',
			type : 'pie',
			radius : [ '50%', '75%' ],

			data : [ {
				value : one['_13'],
				name : '1-3月标',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : one['_46'],
				name : '4-6月标',
				itemStyle : {
					normal : {
						color : '#B176C6'
					}
				}
			}, {
				value : one['_79'] + one['_1012'],
				name : '7-12月标',
				itemStyle : {
					normal : {
						color : '#F5CD1E'
					}
				}
			},
			/*
			 * { value:one['_1012'], name:'10-12月标', itemStyle:{ normal:{
			 * color:'#F55C5E' } } },
			 */
			{
				value : one['_1318'],
				name : '13-18月标',
				itemStyle : {
					normal : {
						color : '#2DD37F'
					}
				}
			}, {
				value : one['_2424'],
				name : '24月标',
				itemStyle : {
					normal : {
						color : '#ff6600'
					}
				}
			} ]
		} ]
	};
	myChart2.setOption(option2);
};

// 近30日还款方式分布
function pie2(two) {
	var myChart3 = echarts.init(document.getElementById('canvas3'));
	var two_num = two['_debx'] + two['_xxhb'];
	var option3 = {
		tooltip : {
			trigger : 'item',
			formatter : "{a} <br/>{b}: {c} ({d}%)",
		},
//		legend : {
//			orient : 'vertical',
//			x : 'left',
//		// data:['直达','营销广告','搜索引擎','邮件营销','联盟广告','视频广告','百度','谷歌','必应','其他']
//		},
		series : [ {
			name : '共' + two_num + '笔',
			type : 'pie',
			radius : [ '50%', '75%' ],

			data : [ {
				value : two['_xxhb'],
				name : '先息后本',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : two['_debx'],
				name : '等额本息',
				itemStyle : {
					normal : {
						color : '#F6B068'
					}
				}
			} ]
		} ]
	};
	myChart3.setOption(option3);
}

// 产品类型分布
function pie3(three) {
	var myChart4 = echarts.init(document.getElementById('canvas4'));
	var option4 = {
		tooltip : {
			trigger : 'item',
			formatter : function(params, ticket, callback) {
				var res = params.seriesName;
				res += '<br/>' + params.name + ' : '
						+ YRHX.toDecimal2(params.value, "¥").replace(".00", "")
						+ '(' + params.percent + "%)";
				return res;
			}
		/* "{a} <br/>{b}: {c}元 ({d}%) ", */
		},
//		legend : {
//			orient : 'vertical',
//			x : 'left',
//		// data:['直达','营销广告','搜索引擎','邮件营销','联盟广告','视频广告','百度','谷歌','必应','其他']
//		},
		series : [ {
			name : '产品类型',
			type : 'pie',
			radius : [ '50%', '75%' ],

			data : [ {
				value : three['_zyb'] / 10.0 / 10.0,
				name : '质押宝',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : three['_cwy'] / 10.0 / 10.0,
				name : '车稳盈',
				itemStyle : {
					normal : {
						color : '#2DD37F'
					}
				}
			}, {
				value : three['_wdt'] / 10.0 / 10.0,
				name : '稳定投',
				itemStyle : {
					normal : {
						color : '#B176C6'
					}
				}
			} ]
		} ]
	};
	myChart4.setOption(option4);
}

// 会员等级分布
function pie4(four) {
	var myChart5 = echarts.init(document.getElementById('canvas5'));
	var option5 = {
		tooltip : {
			trigger : 'item',
			formatter : "{a} <br/>{b}: {c} ({d}%)"
		},
		legend : {
			orient : 'vertical',
			x : '5px',
			y : '5px',
			data : [ '新手', '普通会员', '青铜会员', '白银会员', '黄金会员','白金会员','钻石会员','黑钻会员','至尊会员' ]
		},
		series : [ {
			name : '会员等级',
			type : 'pie',
			radius : [ '50%', '30%' ],
			avoidLabelOverlap : false,
			label : {
				normal : {
					show : false,
					position : 'center'
				},
				emphasis : {
					show : true,
					textStyle : {
						fontSize : '18',
						fontWeight : 'bold'
					}
				}
			},
			labelLine : {
				normal : {
					show : true
				}
			},
			data : [ {
				value : four['_wjb'],
				name : '新手',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : four['_s1xjb'],
				name : '普通会员',
				itemStyle : {
					normal : {
						color : '#F5B166'
					}
				}
			}, {
				value : four['_zxjb'],
				name : '青铜会员',
				itemStyle : {
					normal : {
						color : '#B176C6'
					}
				}
			}, {
				value : four['_s2xjb'],
				name : '白银会员',
				itemStyle : {
					normal : {
						color : '#30D27F'
					}
				}
			}, {
				value : four['_jjjb'],
				name : '黄金会员',
				itemStyle : {
					normal : {
						color : 'orange'
					}
				}
			} , {
				value : four['_bjjb'],
				name : '白金会员',
				itemStyle : {
					normal : {
						color : 'gray'
					}
				}
			} , {
				value : four['_zzjb'],
				name : '钻石会员',
				itemStyle : {
					normal : {
						color : 'violet'
					}
				}
			} , {
				value : four['_hzjb'],
				name : '黑钻会员',
				itemStyle : {
					normal : {
						color : 'black'
					}
				}
			} , {
				value : four['_zzjb'],
				name : '至尊会员',
				itemStyle : {
					normal : {
						color : 'red'
					}
				}
			} ]
		} ]
	};
	myChart5.setOption(option5);
}

// 出借人年龄比例
function pie5(data) {
	var myChart = echarts.init(document.getElementById('canvas7'));
	var option = {
		tooltip : {
			trigger : 'item',
			formatter : function(params, ticket, callback) {
				var res = params.name + ' <br>' + params.value + ' 人' + '(' + params.percent + "%)";
				return res;
			}
		/* "{a} <br/>{b}: {c}元 ({d}%) ", */
		},
//		legend : {
//			orient : 'vertical',
//			x : 'left',
//		// data:['直达','营销广告','搜索引擎','邮件营销','联盟广告','视频广告','百度','谷歌','必应','其他']
//		},
		series : [ {
			name : '出借人年龄比例',
			type : 'pie',
			radius : [ '50%', '75%' ],

			data : [ {
				value : data['age10_19'],
				name : '< 20岁',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : data['age20_29'],
				name : '20-29岁',
				itemStyle : {
					normal : {
						color : '#F5B166'
					}
				}
			}, {
				value : data['age30_39'],
				name : '30-39岁',
				itemStyle : {
					normal : {
						color : '#B176C6'
					}
				}
			}, {
				value : data['age40_49'],
				name : '40-49岁',
				itemStyle : {
					normal : {
						color : '#30D27F'
					}
				}
			}, {
				value : data['age50_59'],
				name : '50-59岁',
				itemStyle : {
					normal : {
						color : '#F55D5C'
					}
				}
			}, {
				value : data['age60_150'],
				name : '> 60岁',
				itemStyle : {
					normal : {
						color : 'red'
					}
				}
			} ]
		} ]
	};
	myChart.setOption(option);
}

//出借人性别比例
function pie6(data) {
	var myChart = echarts.init(document.getElementById('canvas8'));
	var option = {
		tooltip : {
			trigger : 'item',
			formatter : function(params, ticket, callback) {
				var res = params.name + ' <br>' + params.value + ' 人'+ '(' + params.percent + "%)";
				return res;
			}
		},
		series : [ {
			name : '出借人性别比例',
			type : 'pie',
			radius : [ '50%', '75%' ],

			data : [ {
				value : data['sexM'],
				name : '男',
				itemStyle : {
					normal : {
						color : '#78BCE9'
					}
				}
			}, {
				value : data['sexF'],
				name : '女',
				itemStyle : {
					normal : {
						color : '#2DD37F'
					}
				}
			} ]
		} ]
	};
	myChart.setOption(option);
}

// 出借人数分布 / 投资金额排名
function mapData(data) {
	var myChart = echarts.init(document.getElementById('canvasMap'));
	var option = {
			 
		    tooltip : {
		        trigger: 'item'
		    },
		    dataRange: {
		        min: 0,
		        max: 2500,
		        x: 'left',
		        y: 'bottom',
		      
		    },
		    series : [
		        {
		            name: '出借人数',
		            type: 'map',
		            mapLocation: {
		                y : 'top',
		                height : 450
		            },
		            mapType: 'china',
		            roam: false,
		            itemStyle:{
		                normal:{label:{show:true}},
		                emphasis:{label:{show:true}}
		            },
		            data: data || []
	            	/*[
		                {name: '北京',value: Math.round(Math.random()*1000)},
		                {name: '天津',value: Math.round(Math.random()*1000)},
		                {name: '上海',value: Math.round(Math.random()*1000)},
		                {name: '重庆',value: Math.round(Math.random()*1000)},
		                {name: '河北',value: Math.round(Math.random()*1000)},
		                {name: '河南',value: Math.round(Math.random()*1000)},
		                {name: '云南',value: Math.round(Math.random()*1000)},
		                {name: '辽宁',value: Math.round(Math.random()*1000)},
		                {name: '黑龙江',value: Math.round(Math.random()*1000)},
		                {name: '湖南',value: Math.round(Math.random()*1000)},
		                {name: '安徽',value: Math.round(Math.random()*1000)},
		                {name: '山东',value: Math.round(Math.random()*1000)},
		                {name: '新疆',value: Math.round(Math.random()*1000)},
		                {name: '江苏',value: Math.round(Math.random()*1000)},
		                {name: '浙江',value: Math.round(Math.random()*1000)},
		                {name: '江西',value: Math.round(Math.random()*1000)},
		                {name: '湖北',value: Math.round(Math.random()*1000)},
		                {name: '广西',value: Math.round(Math.random()*1000)},
		                {name: '甘肃',value: Math.round(Math.random()*1000)},
		                {name: '山西',value: Math.round(Math.random()*1000)},
		                {name: '内蒙古',value: Math.round(Math.random()*1000)},
		                {name: '陕西',value: Math.round(Math.random()*1000)},
		                {name: '吉林',value: Math.round(Math.random()*1000)},
		                {name: '福建',value: Math.round(Math.random()*1000)},
		                {name: '贵州',value: Math.round(Math.random()*1000)},
		                {name: '广东',value: Math.round(Math.random()*1000)},
		                {name: '青海',value: Math.round(Math.random()*1000)},
		                {name: '西藏',value: Math.round(Math.random()*1000)},
		                {name: '四川',value: Math.round(Math.random()*1000)},
		                {name: '宁夏',value: Math.round(Math.random()*1000)},
		                {name: '海南',value: Math.round(Math.random()*1000)},
		                {name: '台湾',value: Math.round(Math.random()*1000)},
		                {name: '香港',value: Math.round(Math.random()*1000)},
		                {name: '澳门',value: Math.round(Math.random()*1000)}
		            ]*/
		        }
		    ]
		};
		                    

	myChart.setOption(option);
}
var chinaProvice = {
	'amt_11' : '北京',
	'amt_12' : '天津',
	'amt_13' : '河北',
	'amt_14' : '山西',
	'amt_15' : '内蒙古',
	'amt_21' : '辽宁',
	'amt_22' : '吉林',
	'amt_23' : '黑龙江',
	'amt_31' : '上海',
	'amt_32' : '江苏',
	'amt_33' : '浙江',
	'amt_34' : '安徽',
	'amt_35' : '福建',
	'amt_36' : '江西',
	'amt_37' : '山东',
	'amt_41' : '河南',
	'amt_42' : '湖北',
	'amt_43' : '湖南',
	'amt_44' : '广东',
	'amt_45' : '广西',
	'amt_46' : '海南',
	'amt_50' : '重庆',
	'amt_51' : '四川',
	'amt_52' : '贵州',
	'amt_53' : '云南',
	'amt_54' : '西藏',
	'amt_61' : '陕西',
	'amt_62' : '甘肃',
	'amt_63' : '青海',
	'amt_64' : '宁夏',
	'amt_65' : '新疆',
	'amt_71' : '台湾',
	'amt_81' : '香港',
	'amt_82' : '澳门',
	'amt_91' : '国外'
}

