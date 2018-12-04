(function(){


	var CONFIG = window.CONFIG = {};
	CONFIG.IMGURL = '/portal/images/';
//    CONFIG.SAFEINURL = 'https://pre.51cunzheng.com/investment-detail?recordNo='
	CONFIG.SAFEINURL = 'https://www.51cunzheng.com/investment-detail?recordNo='

    
    //**********Request Address*******************
    //CONFIG["ROOT"] = "http://192.168.2.188:8080" ;
	CONFIG["ROOT"] = "";
    CONFIG["REQUEST"] = {} ;
    CONFIG["REQUEST"]["login"] = "/mobileLogin" ;
    CONFIG["REQUEST"]["register"] = "/register4mobile" ;
    CONFIG["REQUEST"]["queryFunds4p"] = "/queryFunds4p" ;
    CONFIG["REQUEST"]["queryLoan4My"] = "/queryLoan4My" ;
    CONFIG["REQUEST"]["queryFundsTrace4User"] = "/queryFundsTrace4User" ;
    CONFIG["REQUEST"]["rechargeHistory"] = "/rechargeHistory" ;
    CONFIG["REQUEST"]["withdrawalsHistory"] = "/withdrawalsHistory" ;
    CONFIG["REQUEST"]["queryAvbalance"] = "/queryAvbalance" ;
    CONFIG["REQUEST"]["queryBanks"] = "/queryBanks" ;
    CONFIG["REQUEST"]["delBank4user"] = "/delBank4user" ;
    CONFIG["REQUEST"]["setBankDefault"] = "/setBankDefault" ;
    CONFIG["REQUEST"]["queryInvest4My"] = "/queryInvest4My" ;
    CONFIG["REQUEST"]["queryMainPage"] = "/queryMainPage";
    CONFIG["REQUEST"]["queryMoney4Back"] = "/queryMoney4Back";
    CONFIG["REQUEST"]["queryFinancialBid"] = "/queryFinancialBid";
    CONFIG["REQUEST"]["bidding"] = "/bidding";
    CONFIG["REQUEST"]["signIn"] = "/signIn";
    CONFIG["REQUEST"]["signHistory"] = "/signHistory";
    CONFIG["REQUEST"]["isSign"] = "/isSign";
    CONFIG["REQUEST"]["isLogin"] = "/isLogin";
//    CONFIG["REQUEST"]["bidding"] = "/doex/action?pn=doLoanBidding";
    
    CONFIG["REQUEST"]["queryBidDetail"] = "/queryBidDetail";
    CONFIG["REQUEST"]["queryLoanTrace"] = "/queryLoanTrace";
    CONFIG["REQUEST"]["queryTrailerLoan"] = "/queryTrailerLoan";
    CONFIG["REQUEST"]["sendMsgMac"] = "/sendMsgMac";
    CONFIG["REQUEST"]["queryAutoLoanState"] = "/queryAutoLoanState";
    CONFIG["REQUEST"]["setAutoLoanState"] = "/setAutoLoanState";
    CONFIG["REQUEST"]["queryAutoLoanSettings"] = "/queryAutoLoanSettings";
    CONFIG["REQUEST"]["saveAutoLoanSettings"] = "/saveAutoLoanSettings";
    //CONFIG["REQUEST"]["sendMsgMobile"] = "/newSendMsgMobile";
    CONFIG["REQUEST"]["nSendMsgMobile"] = "/nSendMsgMobile";
    CONFIG["REQUEST"]["updateLoginPwd"] = "/updateLoginPwd";
    CONFIG["REQUEST"]["updatePayPwd"] = "/updatePayPwd";
    CONFIG["REQUEST"]["certification"] = "/certification";
    CONFIG["REQUEST"]["uploadFile"] = "/uploadFile";
    CONFIG["REQUEST"]["sendEmail"] = "/sendEmail";
    CONFIG["REQUEST"]["getAuthInfo"] = "/getAuthInfo";
    CONFIG["REQUEST"]["queryUserInfo"] = "/queryUserInfo";
    CONFIG["REQUEST"]["updateUserInfo"] = "/updateUserInfo";
    CONFIG["REQUEST"]["queryFunds4User"] = "/queryFunds4User";
    CONFIG["REQUEST"]["queryBidDetailNoLogin"] = "/queryBidDetailNoLogin";
    CONFIG["REQUEST"]["queryFundsCount4user"] = "/queryFundsCount4user";
    CONFIG["REQUEST"]["queryRealtimeFinancial"] = "/queryRealtimeFinancial";
    CONFIG["REQUEST"]["queryCanTransferList"] = "/queryCanTransferList";
    CONFIG["REQUEST"]["queryLoanInfo4Week"] = "/queryLoanInfo4Week";
    CONFIG["REQUEST"]["queryCanTransferList"] = "/queryCanTransferList";
    CONFIG["REQUEST"]["queryLoanTransfer4user"] = "/queryLoanTransfer4user";
    CONFIG["REQUEST"]["withdrawals"] = "/withdrawals";
    /*CONFIG["REQUEST"]["hasPayPwd"] = "/hasPayPwd";*/
    CONFIG["REQUEST"]["withdrawalsInit"] = "/withdrawalsInit";
    CONFIG["REQUEST"]["queryFundsCount4Loan"] = "/queryFundsCount4Loan";
    CONFIG["REQUEST"]["queryGotLoanTransfer4user"] = "/queryGotLoanTransfer4user";
    CONFIG["REQUEST"]["queryGotTransferTotal"] = "/queryGotTransferTotal";
    CONFIG["REQUEST"]["debentureTransfer"] = "/debentureTransfer";
    CONFIG["REQUEST"]["queryLoanTransfer"] = "/queryLoanTransfer";
    CONFIG["REQUEST"]["debentureTransfer"] = "/debentureTransfer";
    CONFIG["REQUEST"]["queryLoanTransfer4Code"] = "/queryLoanTransfer4Code";
    CONFIG["REQUEST"]["carryOnTransfer"] = "/carryOnTransfer";
    CONFIG["REQUEST"]["queryOverdueTrace30"] = "/queryOverdueTrace30";
    CONFIG["REQUEST"]["queryOverdueTrace4yes"] = "/queryOverdueTrace4yes";
    CONFIG["REQUEST"]["queryNotice"] = "/queryNotice";
    CONFIG["REQUEST"]["findPwd4user"] = "/findPwd4user";
    CONFIG["REQUEST"]["cancelTransfer"] = "/cancelTransfer";
    CONFIG["REQUEST"]["recharge"] = "/recharge";
    CONFIG["REQUEST"]["queryBidding4User"] = "/queryBidding4User";
    CONFIG["REQUEST"]["queryRechargeTrace4User"] = "/queryRechargeTrace4User";
    CONFIG["REQUEST"]["queryNewsByPage"] = "/queryNewsByPage";
    CONFIG["REQUEST"]["queryShareByPage"] = "/queryShareByPage";
    CONFIG["REQUEST"]["queryShareCount"] = "/queryShareCount";
    CONFIG["REQUEST"]["queryNewsDetail"] = "/queryNewsDetail";
    CONFIG["REQUEST"]["querySumScore"] = "/querySumScore";
    CONFIG["REQUEST"]["queryMarketByPage"] = "/queryMarketByPage";
    CONFIG["REQUEST"]["queryMarketLCQByPage"] = "/queryMarketLCQByPage";
    CONFIG["REQUEST"]["queryMarketSWSPByPage"] = "/queryMarketSWSPByPage";
    CONFIG["REQUEST"]["queryMarketDZKByPage"] = "/queryMarketDZKByPage";
    CONFIG["REQUEST"]["queryMarketDetail"] = "/queryMarketDetail";
    CONFIG["REQUEST"]["exchange"] = "/exchange";
    CONFIG["REQUEST"]["queryExchange"] = "/queryExchange";
    CONFIG["REQUEST"]["queryPointTop5"] = "/queryPointTop5";
    CONFIG["REQUEST"]["queryPoint6_10"] = "/queryPoint6_10";
    CONFIG["REQUEST"]["queryExchangeNow"] = "/queryExchangeNow";
    CONFIG["REQUEST"]["downContractPDF"] = "/downContractPDF";
    CONFIG["REQUEST"]["sendEmail4Bind"] = "/sendEmail4Bind";
    CONFIG["REQUEST"]["authEmail4Bind"] = "/authEmail4Bind";
    CONFIG["REQUEST"]["queryInvest4Latest"] = "/queryInvest4Latest";
    CONFIG["REQUEST"]["queryAllInvest4My"] = "/queryAllInvest4My";
    CONFIG["REQUEST"]["logout"] = "/logout";
    CONFIG["REQUEST"]["cancelWithdrawals"] = "/cancelWithdrawals";
    CONFIG["REQUEST"]["queryBankByBin"] = "/lianlian_queryBankByBin"; 
    CONFIG["REQUEST"]["bankv3Save"] = "/bankv3_save";
    CONFIG["REQUEST"]["bankv2List"] = "/bankv2_list";
    CONFIG["REQUEST"]["certificationAuto"] = "/certificationAuto" ;
    CONFIG["REQUEST"]["queryCountData1"] = "/queryCountData1";
    CONFIG["REQUEST"]["validateCardCity"] = "/validateCardCity" ;
    CONFIG["REQUEST"]["updateCardCity"] = "/updateCardCity" ;
    CONFIG["REQUEST"]["queryFundsOrderByBenJin"] = "/queryFundsOrderByBenJin" ;
    CONFIG["REQUEST"]["duangBasicData"] = "/duangBasicData" ;
    CONFIG["REQUEST"]["duangDayCount"] = "/duangDayCount";
    CONFIG["REQUEST"]["duangMonthCount"] = "/duangMonthCount";
    CONFIG["REQUEST"]["duangCircularReport"] = "/duangCircularReport";
    CONFIG["REQUEST"]["duangToubiaoOrder1"] = "/duangToubiaoOrder1";
    CONFIG["REQUEST"]["duangToubiaoOrder2"] = "/duangToubiaoOrder2";
    CONFIG["REQUEST"]["duangMyRank"] = "/duangMyRank";
    CONFIG["REQUEST"]["duangTodayReport"] = "/duangTodayReport";
    CONFIG["REQUEST"]["duangAnyDayReport"] = "/duangAnyDayReport";
    CONFIG["REQUEST"]["activity4month201603"] = "/activity4month201603";
    CONFIG["REQUEST"]["queryTickets4User"] = "/queryTickets4User";
    CONFIG["REQUEST"]["validate4mobile"] = "/validate4mobile";
    CONFIG["REQUEST"]["xjq_test"] = "/xjq_test";
    CONFIG["REQUEST"]["queryFreeWithdrawal4user"] = "/queryFreeWithdrawal4user";
    CONFIG["REQUEST"]["queryServerTime"] = "/queryServerTime";
    CONFIG["REQUEST"]["queryOverdueTraceByCode"] = "/queryOverdueTraceByCode";
    CONFIG["REQUEST"]["aapay_authPay"] = "/aapay_authPay";
    CONFIG["REQUEST"]["queryRecharge"] = "/queryRecharge";
    CONFIG["REQUEST"]["queryBackDate4User"] = "/queryBackDate4User";
    CONFIG["REQUEST"]["countBackInfo4User"] = "/countBackInfo4User";
    CONFIG["REQUEST"]["queryAutoLoanRankDetail"] = "/queryAutoLoanRankDetail";
    CONFIG["REQUEST"]["queryAutoLoanRankNum"] = "/queryAutoLoanRankNum";
    CONFIG["REQUEST"]["userCFCA"] = "/userCFCA";
    CONFIG["REQUEST"]["queryCFCA"] = "/queryCFCA";
    CONFIG["REQUEST"]["duangUserCount"] = "/duangUserCount";
	CONFIG["REQUEST"]["bind4Platform"] = "/platform/bind";
	CONFIG["REQUEST"]["querySumScoreByCookie"] = "/querySumScoreByCookie";
	CONFIG["REQUEST"]["changeBank"] = "/changeBankV2";
	CONFIG["REQUEST"]["haveBackDate4User"] = "/haveBackDate4User";//20170905搜索当月回款日期
	CONFIG["REQUEST"]["BackDetil8Day"] = "/BackDetil8Day";//20170905搜索当日回款详情
	CONFIG["REQUEST"]["queryDeductionMoney"] = "/queryDeductionMoney";
	CONFIG["REQUEST"]["exportExcel4User"] = "/exportExcel4User";
	CONFIG["REQUEST"]["updateYrMobile"] = "/updateYrMobile";//更换平台手机号
	CONFIG["REQUEST"]["updateHfMobile"] = "/updateHfMobile";//更换存管手机号
	CONFIG["REQUEST"]["queryRewardDetails"] = "/queryRewardDetails";	// 查询赏金计划奖励
	CONFIG["REQUEST"]["queryRewardByUserCode"] = "/queryRewardByUserCode";	// 查询邀请奖励详情
	CONFIG["REQUEST"]["queryRewardRanking"] = "/queryRewardRanking";	// 查询邀请奖励排名
	
	CONFIG["REQUEST"]["replenishSignIn"] = "/replenishSignIn";	// 补签

    CONFIG["REQUEST"]["queryMsgResult"] = "http://192.168.2.155/kafkaService/queryKafkaMessageResult";
    CONFIG.getRequestURI = function(key){
    	return CONFIG["ROOT"] + CONFIG["REQUEST"][key];
    };
	
	
	CONFIG.header = {};
	CONFIG.header.topRight = [
	/*	{text: '易融学院', url: '', isHot: false},
		{text: '易融社区', url: 'http://bbs.yrhx.com/', isHot: true},*/
		{text: '注册', url: CONFIG["ROOT"]+'/register', isHot: false},
		{text: '登录', url: CONFIG["ROOT"]+'/login', isHot: false},
		//{text: '新手指南', url: CONFIG["ROOT"]+'/Y01', isHot: false}
	];
	CONFIG.header.topNav = [
		{text: '首页',    url: CONFIG["ROOT"]+'/index'},
		{text: '我要出借', url: CONFIG["ROOT"]+'/Z02'},
		{text: '实时数据', url: CONFIG["ROOT"]+'/Z03'},
		{text: '新手指南', url: CONFIG["ROOT"]+'/Y01'},
		{text: '关于我们', url: CONFIG["ROOT"]+'/Y06_01'}
	];
	topNavSelect = 0;
	CONFIG.footer = {};
	CONFIG.footer.bottomMenu = [
		{
			label: '新闻中心',
			menu:[
				{text: '公司新闻', url: CONFIG["ROOT"]+'/X02'},
				{text: '活动专题', url: CONFIG["ROOT"]+'/X06'},
				{text: '运营报告', url: CONFIG["ROOT"]+'/X09'},
				//{text: '更新日志', url: CONFIG["ROOT"]+'/X08'}
			]
		},
		{
			label: '新手帮助',
			menu:[
				{text: '新手指南', url: CONFIG["ROOT"]+'/Y01'},
				{text: '帮助中心', url: CONFIG["ROOT"]+'/K01'},
				{text: '资费详情', url: CONFIG["ROOT"]+'/Y07'}
			]
		},
		{
			label: '安全保障',
			menu:[
				{text: '安全保障', url: CONFIG["ROOT"]+'/Y03'},	
				{text: '合作伙伴', url: CONFIG["ROOT"]+'/Y06_06'},
				{text: '资金存管', url: CONFIG["ROOT"]+'http://topic.yrhx.com/zt/2018/20180521/'}
			]
		},
		{
			label: '用户协议',
			menu:[
			    {text: '隐私条款', url: CONFIG["ROOT"]+'/Y05_02'},
			    {text: '企业执照', url: CONFIG["ROOT"]+'/Y05_05'},
			    {text: '版权申明', url: CONFIG["ROOT"]+'/Y05_03'}
			]
		}
	];

window.mobilecheck = function() {
	var check = false;
	(function(a,b){if(/(android|bb\d+|meego).+mobile|avantgo|bada\/|blackberry|blazer|compal|elaine|fennec|hiptop|iemobile|ip(hone|od)|iris|kindle|lge |maemo|midp|mmp|mobile.+firefox|netfront|opera m(ob|in)i|palm( os)?|phone|p(ixi|re)\/|plucker|pocket|psp|series(4|6)0|symbian|treo|up\.(browser|link)|vodafone|wap|windows ce|xda|xiino/i.test(a)||/1207|6310|6590|3gso|4thp|50[1-6]i|770s|802s|a wa|abac|ac(er|oo|s\-)|ai(ko|rn)|al(av|ca|co)|amoi|an(ex|ny|yw)|aptu|ar(ch|go)|as(te|us)|attw|au(di|\-m|r |s )|avan|be(ck|ll|nq)|bi(lb|rd)|bl(ac|az)|br(e|v)w|bumb|bw\-(n|u)|c55\/|capi|ccwa|cdm\-|cell|chtm|cldc|cmd\-|co(mp|nd)|craw|da(it|ll|ng)|dbte|dc\-s|devi|dica|dmob|do(c|p)o|ds(12|\-d)|el(49|ai)|em(l2|ul)|er(ic|k0)|esl8|ez([4-7]0|os|wa|ze)|fetc|fly(\-|_)|g1 u|g560|gene|gf\-5|g\-mo|go(\.w|od)|gr(ad|un)|haie|hcit|hd\-(m|p|t)|hei\-|hi(pt|ta)|hp( i|ip)|hs\-c|ht(c(\-| |_|a|g|p|s|t)|tp)|hu(aw|tc)|i\-(20|go|ma)|i230|iac( |\-|\/)|ibro|idea|ig01|ikom|im1k|inno|ipaq|iris|ja(t|v)a|jbro|jemu|jigs|kddi|keji|kgt( |\/)|klon|kpt |kwc\-|kyo(c|k)|le(no|xi)|lg( g|\/(k|l|u)|50|54|\-[a-w])|libw|lynx|m1\-w|m3ga|m50\/|ma(te|ui|xo)|mc(01|21|ca)|m\-cr|me(rc|ri)|mi(o8|oa|ts)|mmef|mo(01|02|bi|de|do|t(\-| |o|v)|zz)|mt(50|p1|v )|mwbp|mywa|n10[0-2]|n20[2-3]|n30(0|2)|n50(0|2|5)|n7(0(0|1)|10)|ne((c|m)\-|on|tf|wf|wg|wt)|nok(6|i)|nzph|o2im|op(ti|wv)|oran|owg1|p800|pan(a|d|t)|pdxg|pg(13|\-([1-8]|c))|phil|pire|pl(ay|uc)|pn\-2|po(ck|rt|se)|prox|psio|pt\-g|qa\-a|qc(07|12|21|32|60|\-[2-7]|i\-)|qtek|r380|r600|raks|rim9|ro(ve|zo)|s55\/|sa(ge|ma|mm|ms|ny|va)|sc(01|h\-|oo|p\-)|sdk\/|se(c(\-|0|1)|47|mc|nd|ri)|sgh\-|shar|sie(\-|m)|sk\-0|sl(45|id)|sm(al|ar|b3|it|t5)|so(ft|ny)|sp(01|h\-|v\-|v )|sy(01|mb)|t2(18|50)|t6(00|10|18)|ta(gt|lk)|tcl\-|tdg\-|tel(i|m)|tim\-|t\-mo|to(pl|sh)|ts(70|m\-|m3|m5)|tx\-9|up(\.b|g1|si)|utst|v400|v750|veri|vi(rg|te)|vk(40|5[0-3]|\-v)|vm40|voda|vulc|vx(52|53|60|61|70|80|81|83|85|98)|w3c(\-| )|webc|whit|wi(g |nc|nw)|wmlb|wonu|x700|yas\-|your|zeto|zte\-/i.test(a.substr(0,4)))check = true})(navigator.userAgent||navigator.vendor||window.opera);
	
	if( check ){
		$(".web_enter").show();
	};
}
window.mobilecheck();

})();