package com.dutiantech.controller.admin;

import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.task.CallService;
import com.dutiantech.task.DayService;
import com.dutiantech.task.Task;
import com.dutiantech.task.TaskService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class TaskControler extends BaseController{

	@ActionKey("/initFunds")
	@Before({PkMsgInterceptor.class})
	public void initFunds(){
		String key = getPara("key","");
		if( key.equals("selangshiwo.com") == false ){
			renderText("!!");
		}else{
//			CalculationService ccs = new CalculationService();
//			ccs.start();
		}
		renderText("干啥？");
	}
	
	private static boolean isStartService = false ;
	
	@ActionKey("/initService")
	@Before({PkMsgInterceptor.class})
	public void initService(){
		
		String key = getPara("key","");
		StringBuffer buff = new StringBuffer("");
		if( key.equals("selangshiwo.com") == false ){
			buff.append("error!");
		}else{
			
			if( isStartService == false ){
				
				//添加自动投标服务
				TaskService autoLoanService = new CallService("http://127.0.0.1:8899/autoLoan_v3", 30000 , "自动投标服务") ;
				autoLoanService.setSCKey("3.14159265358");
				Task.newTask( autoLoanService );
				buff.append("自动投标服务开启成功!<br />") ;
				
				//开启过期债权处理任务
				TaskService scanTransferService = new CallService("http://127.0.0.1:8899/scanTransfer", 10000 , "自动扫描过期债权服务") ;
				scanTransferService.setSCKey("3.14159265358");
				Task.newTask( scanTransferService );
				buff.append("自动扫描过期债权服务开启成功!<br />") ;
				
				//开启自动扫描满标服务
				TaskService scanManBiaoService = new CallService("http://127.0.0.1:8899/scanManBiao", 10000 , "自动扫描满标服务") ;
				scanManBiaoService.setSCKey("3.14159265358");
				Task.newTask( scanManBiaoService );
				buff.append("自动扫描满标服务开启成功!<br />") ;
				
				//开启自动还款服务
				TaskService scanHuanKuan = new DayService("http://127.0.0.1:8899/autoRecy4loan", "00:30", "自动还款服务");
				scanHuanKuan.setSCKey("nicaizhecanshushiganshade201703031944");
				Task.newTask( scanHuanKuan );
				buff.append("自动还款服务开启成功!<br />") ;
				
				//开启短信发送服务
				TaskService scanSMSService = new CallService("http://127.0.0.1:8899/sendAllSMS", 10000 , "短信发送服务") ;
				scanSMSService.setSCKey("3.14159265358");
				Task.newTask( scanSMSService );
				buff.append("短信发送服务开启成功!<br />") ;
				
				//开启自动扫描过期奖券服务
				TaskService scanTicketService = new DayService("http://127.0.0.1:8899/scanTicket", "00:01", "扫描过期奖券服务");
				scanTicketService.setSCKey("3.14159265358");
				Task.newTask( scanTicketService );
				buff.append("扫描过期奖券服务开启成功!<br />") ;
				
				//扫描会员等级服务 2016年4月11日生效
				TaskService scanVipLevelService = new DayService("http://127.0.0.1:8899/checkVIPLevel", "00:01", "扫描会员等级服务");
				scanVipLevelService.setSCKey("3.14159265358");
				Task.newTask( scanVipLevelService );
				buff.append("扫描会员等级服务开启成功!<br />") ;
				
				//每日平台数据统计，给他妈的一起好发邮件
				TaskService fuckPlatformDataService = new DayService("http://127.0.0.1:8899/fuckPlatformData", "00:01", "每日平台数据统计服务");
				fuckPlatformDataService.setSCKey("3.14159265358");
				Task.newTask( fuckPlatformDataService );
				buff.append("每日平台数据统计服务开启成功!<br />") ;

				// 自动扫描30分钟内未处理的充值提现流水
//				TaskService taskCzTxService = new CallService("http://127.0.0.1:8899/scanFuiouCzTxTrace", 1000 * 60 * 5, "扫描未处理充值提现流水服务");
//				taskCzTxService.setSCKey("3.14159265358");
//				Task.newTask(taskCzTxService);
//				buff.append("扫描未处理提现流水服务开户成功！<br />");
						
//				//扫描商银信代付订单	—— Abandoned
//				TaskService scanSYXPayInOrderService = new CallService("http://127.0.0.1:8899/syxpayin_task_query", 5 * 60 * 1000 , "扫描商银信代付订单是否成功服务") ;
//				scanSYXPayInOrderService.setSCKey("3.14159265358");
//				Task.newTask( scanSYXPayInOrderService );
//				buff.append("扫描商银信代付订单是否成功服务启动成功!<br />") ;
				
				//安存
				TaskService cfca = new CallService("http://127.0.0.1:8899/startCFCA", 3*60*1000, "cfca安存服务");
				cfca.setSCKey("3.14159265358");
				Task.newTask( cfca );
				buff.append("cfca安存服务启动成功!<br />");
				
				//统计用户区域人数、区域投资金额、年龄、性别
				TaskService userDataCountService = new DayService("http://127.0.0.1:8899/madeUserInfoData", "00:05", "每日用户分析统计服务");
				userDataCountService.setSCKey("3.14159265358");
				Task.newTask( userDataCountService );
				buff.append("每日用户分析统计服务启动成功!<br />");
				
				// 生日双倍积分短信提醒
				TaskService birthdayService = new DayService("http://127.0.0.1:8899/sendBirthdaySMS", "08:30" , "生日短信发送服务"); 
				birthdayService.setSCKey("3.14159265358");
				Task.newTask( birthdayService );
				buff.append("生日双倍积分短信提醒服务启动成功！<br/>");
				
				//生成前一日开户数据，生成文件发给存管系统报备        20170619   WCF
//				TaskService regText = new DayService("http://127.0.0.1:8899/createRegText", "05:00", "每日存管开户数据统计服务");
//				regText.setSCKey("3.14159265358");
//				Task.newTask( regText );
//				buff.append("每日存管开户数据统计服务开启成功!<br />") ;
				
				//生成前一日开户数据，生成文件发给存管系统报备        20170619   WCF
//				TaskService projectText = new DayService("http://127.0.0.1:8899/createProjectText", "05:30", "每日存管项目数据统计服务");
//				projectText.setSCKey("3.14159265358");
//				Task.newTask( projectText );
//				buff.append("每日存管项目数据统计服务开启成功!<br />") ;
				
				//生成前一日交易数据，生成文件发给存管系统报备        20170622   WCF
//				TaskService tradeText = new DayService("http://127.0.0.1:8899/createTradeText", "06:00", "每日存管交易数据统计服务");
//				tradeText.setSCKey("3.14159265358");
//				Task.newTask( tradeText );
//				buff.append("每日存管交易数据统计服务开启成功!<br />") ;
				
				// 智投盈定时更新标的状态
//				TaskService nxjdTask = new CallService("http://127.0.0.1:8899/scanNxjdBiao", 1000*60*60*1, "自动更新智投盈标的状态");
//				nxjdTask.setSCKey("3.14159265358");
//				Task.newTask(nxjdTask);
//				buff.append("自动更新智投盈标的状态服务开启成功!<br/>");
				
				//不要的 停止的 Abandoned
				//添加自动投标服务 - Abandoned
//				TaskService autoLoanService = new CallService("http://127.0.0.1:8899/autoLoan", 30000 , "自动投标服务") ;
//				autoLoanService.setSCKey("3.14159265358");
//				Task.newTask( autoLoanService );
//				buff.append("自动投标服务开启成功!<br />") ;
				
//				//开启自动发放虚拟奖品服务 - Abandoned
//				TaskService lotteryService = new CallService("http://127.0.0.1:8899/handleLottery", 10000, "发放虚拟奖品服务");
//				lotteryService.setSCKey("3.14159265358");
//				Task.newTask( lotteryService );
//				buff.append("发放虚拟奖品服务开启成功!<br />") ;
//				
//				//初始化奖品数量
//				TaskService initPrizeService = new DayService("http://127.0.0.1:8899/initPrize", "00:01", "每日初始化奖品数量服务");
//				initPrizeService.setSCKey("3.14159265358");
//				Task.newTask( initPrizeService );
//				buff.append("每日初始化奖品数量服务开启成功!<br />") ;
//				
//				//七夕发奖 - Abandoned
//				TaskService qixifajiang = new CallService("http://127.0.0.1:8899/qixifajiang", 10000, "七夕现金券实时发奖服务");
//				qixifajiang.setSCKey("3.14159265358");
//				Task.newTask( qixifajiang );
//				buff.append("七夕现金券活动发奖服务启动成功!<br />") ;
				
//				12月 让神券飞 嗨赚感恩节 发放加息券
//				TaskService grantPrize = new DayService("http://127.0.0.1:8899/grantPrize", "00:01", "12月 让神券飞 嗨赚感恩节 发放加息券");
//				grantPrize.setSCKey("3.14159265358");
//				Task.newTask( grantPrize );
//				buff.append("让神券飞 嗨赚感恩节 发放加息券成功!<br />") ;
				
				//12月 邀请好友闯关发放加息券 hw
				TaskService queryInviteData = new CallService("http://127.0.0.1:8899/queryInviteData", 1000*60*60*2, "邀请好友有奖");
				queryInviteData.setSCKey("3.14159265358");
				queryInviteData.addParam("startDate","20171201");
				queryInviteData.addParam("endDate","");
				Task.newTask( queryInviteData );
				buff.append("邀请好友有奖<br />") ;
				
				//处理提现状态
				TaskService dealWithdrawState = new CallService("http://127.0.0.1:8899/dealWithdrawResult", 1000*60*10, "更新提现状态");
				dealWithdrawState.setSCKey("3.14159265358");
				Task.newTask( dealWithdrawState );
				buff.append("更新提现状态<br />") ;
				
				//江西aleve文件下载
				TaskService downALEVE = new DayService("http://127.0.0.1:8899/downFileFromFtp", "08:00", "下载aleve文件");
				downALEVE.setSCKey("3.14159265358");
				Task.newTask(downALEVE);
				buff.append("下载aleve文件<br />");
				
				//定时查询发起申请和交易失败的充值、提现流水
				TaskService dealRechargeAndWithdrawTraces = new CallService("http://127.0.0.1:8899/dealTraceStatus", 1000*60*15, "查询并处理充值提现发起申请、交易失败流水");
				dealRechargeAndWithdrawTraces.setSCKey("3.14159265358");
				Task.newTask( dealRechargeAndWithdrawTraces );
				buff.append("查询并处理充值提现发起申请、交易失败流水<br />");
				
				//定时查询并解冻当日批次代偿还款用户冻结资金
				TaskService unfreezeBatchSubstRepay = new CallService("http://127.0.0.1:8899/unfreezeBatchSubstRepay", 1000*60*30, "当日批次代偿还款用户资金解冻");
				unfreezeBatchSubstRepay.setSCKey("3.14159265358");
				Task.newTask(unfreezeBatchSubstRepay);
				buff.append("解冻当日批次代偿还款用户资金<br />");
				
				// 定时创建用户
//				TaskService autoCreateUser = new CallService("http://127.0.0.1:8899/autoCreateUser", 1000*60*60, "定时自动创建用户服务");
//				Task.newTask(autoCreateUser);
//				buff.append("定时自动创建用户成功!<br />") ;
				
				//定时查询并解冻批次债转
				TaskService unfreezeBatchCreditInvest = new CallService("http://127.0.0.1:8899/unfreezeBatchCreditInvest", 1000*60*30, "债转批次解冻");
				unfreezeBatchCreditInvest.setSCKey("3.14159265358");
				Task.newTask(unfreezeBatchCreditInvest);
				buff.append("债转批次解冻<br />");
				
				//定时查询今日本地未发送红包,批次交易
				TaskService sendBatchSubstRepay = new CallService("http://127.0.0.1:8899/sendBatchSubstRepay", 1000*60*30, "发送还款资金交易请求");
				sendBatchSubstRepay.setSCKey("3.14159265358");
				Task.newTask(sendBatchSubstRepay);
				buff.append("发送还款资金交易请求<br />");
			}
			
		}
		renderHtml(buff.toString());
	}
	
}
