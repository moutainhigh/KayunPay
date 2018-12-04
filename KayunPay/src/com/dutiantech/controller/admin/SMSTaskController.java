package com.dutiantech.controller.admin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutian.SMSClient;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.SMSLog;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SMSService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.task.SMSThread;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum.SMSType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

/**
 * 短信发送任务
 * @author shiqingsong
 *
 */
public class SMSTaskController extends BaseController {

	private SMSLogService smsLogService = getService(SMSLogService.class);
	private SMSService smsService = getService(SMSService.class);
	private UserService userService = getService(UserService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private TicketsService ticketService = getService(TicketsService.class);
	private static final Logger sendSMS4MoneyLogger = Logger.getLogger("sendSMS4MoneyLogger");
	
	static{
		LoggerUtil.initLogger("sendSMS4Money", sendSMS4MoneyLogger);
	}
	
	@ActionKey("/sendBirthdaySMS")
	@Before({PkMsgInterceptor.class})
	public Message sendBirthdaySMS() {//更新时间20180306
		sendSMS4MoneyLogger.log(Level.INFO, "发送生日特权提醒短信及生日礼包发放开始...");//发送生日双倍积分提醒短信开始...
		List<User> lstUser = userService.findAllInvestor();
		
		int o = 0;
		if (lstUser != null && lstUser.size() > 0) {
			for (User user : lstUser) {
				UserInfo userInfo = userInfoService.findById(user.getStr("userCode"));
				// 判断是否为当天生日
				boolean isBirthday = CommonUtil.isBirth(userInfo.getStr("userCardId"));
				if (isBirthday) {
					// 获取用户手机号
					try {
						String mobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
						//获取当前用户会员等级
						int vipLevel = user.getInt("vipLevel");
						String tempInfo = "";
						//根据设置时间停止发放生日礼包送的现金券
						String pauseDate = "20180913";
						
						if(DateUtil.compareDateByStr("yyyyMMdd", DateUtil.getNowDate(), pauseDate) == -1){
							//发放会员生日礼包：现金券总金额
							int totalAmount = issueBirthdayGift(user.getStr("userCode"), vipLevel);
							if(totalAmount > 0){
								tempInfo = "特赠送您" + totalAmount + "元生日礼包。";
							}
						}
						//生日特权信息
						if(vipLevel >= 7 && vipLevel <= 9){
							tempInfo += "当天投资更有三倍积分赠送哦~";
						}else if(vipLevel >= 4 && vipLevel <= 6){
							tempInfo += "当天投资更有双倍积分赠送哦~";
						}else{
							tempInfo += "";
						}
						// 获取生日短信内容
						String msgContent = CommonUtil.SMS_MSG_BIRTHDAYLOAN.replace("[userName]", user.getStr("userName")).replace("[tempInfo]", tempInfo);
						// 发送生日短信
						int result = smsService.sendSms(mobile, msgContent);
						//记录日志
						SMSLog smsLog = new SMSLog();
						smsLog.set("mobile", mobile);
						smsLog.set("content", msgContent);
						smsLog.set("userCode", user.get("userCode"));
						smsLog.set("userName", user.get("userName"));
						smsLog.set("type", SMSType.BIRTHDAY.val());
						smsLog.set("typeName", SMSType.BIRTHDAY.desc());
						smsLog.set("status", result);
						smsLogService.save(smsLog);
						if (result != 0) {
							o++;
						}
					} catch (Exception e) {
						sendSMS4MoneyLogger.log(Level.INFO, "用户[" + user.getStr("userName") + "][" + user.getStr("userCode") + "]发送生日短信失败");
						o++;
					}
				}
			}
		}
		sendSMS4MoneyLogger.log(Level.INFO, "发送生日特权提醒短信及生日礼包发放结束...");
		return succ("服务执行完成", "服务执行完成，发送失败数量：" + o + " 个");
	}
	
	@ActionKey("/sendAllSMS")
	@Before({PkMsgInterceptor.class})
	public Message sendAllSMS(){
		/*if(CommonUtil.jxPort){//发送回款短信时，先更新短信发送状态
			Db.update("update t_sms_log set `status`=9 where `status`=8 and type=10 and sendDate = ?",DateUtil.getNowDate());
		}*/
		
		int o = 0;
		//int repaymentNum = 0;//还款短信条数
		sendSMS4MoneyLogger.log(Level.INFO,"开始扫描待发短信...");
		Page<SMSLog> smsLogs2 = smsLogService.querySendSMSByStatus(1, 100, 9);
		if(null != smsLogs2 && null != smsLogs2.getList()){
			List<SMSLog> list = smsLogs2.getList();
			for (int i = 0; i < list.size(); i++) {
				try {
					SMSLog smsLog = list.get(i);
					String mobile = smsLog.getStr("mobile");
					String content = smsLog.getStr("content");
					String type = smsLog.getStr("type");
					String typeName = smsLog.getStr("typeName");
					int result = -1;
					if(type.equals("10")){//回款的短信
						//repaymentNum++;
						String payName = "";
						int loanCount = 0;
						int earlyLoanCount = 0;
						long money = 0;
						long earlyMoney=0;
						JSONArray objArr = JSONObject.parseArray("[" + content + "]");
						if(null != objArr){
							for (int j = 0; j < objArr.size(); j++) {
								JSONObject object = objArr.getJSONObject(j);
								payName = object.getString("userName");
								String money2=object.getString("money");
								if(null==money2||"".equals(money2)){
									earlyMoney += object.getLongValue("earlymoney");
									money+=object.getLongValue("earlymoney");
									earlyLoanCount++;
								}else{
								money += object.getLongValue("money");
								}
							}
							loanCount = objArr.size();
							int normalLoanCount=loanCount-earlyLoanCount;
							long normalMoney=money-earlyMoney;
							String msgContent = CommonUtil.SMS_MSG_MONEY.replace("[payName]", payName)
									.replace("[loanCount]", String.valueOf(loanCount))
									.replace("[money]",String.format("%.2f",(money)/10.0/10.0))
									.replace("[normalLoanCount]", String.valueOf(normalLoanCount))
									.replace("[normalMoney]",String.format("%.2f",(normalMoney)/10.0/10.0))
									.replace("[earlyLoanCount]", String.valueOf(earlyLoanCount))
									.replace("[earlyMoney]",String.format("%.2f",(earlyMoney)/10.0/10.0));
							//回款短信定时发送
//							result = smsService.sendSmsByDelay(mobile, msgContent.toString(), DateUtil.getNowDate()+"0800");
							//实时短信发送
							result = smsService.sendSms(mobile, msgContent.toString());
						}
					}else{//其它类型的短信
						result = smsService.sendSms(mobile, content);
					}
					sendSMS4MoneyLogger.log(Level.INFO,content+".....【"+typeName+"】短信发送中...");
					if(0 != result){
						o++;
					}
					smsLog.set("status", result);
					smsLog.update();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						o++;
					}
				} catch (Exception e) {
					sendSMS4MoneyLogger.log(Level.INFO,"短信发送异常，继续下一条" + o + " 个");
					continue;
				}
			}
			
				/*if(CommonUtil.jxPort && list != null && list.size() > 1){//自动批次代偿还款发送短信给监管
					try {
						String report = DateUtil.getStrFromNowDate("yyyy年MM月dd日")+"结算回款,还款"+repaymentNum+"笔,结算完成-回款数据事务正式生效【易融恒信】";
						SMSClient.sendSms("13071238735", report);
						SMSClient.sendSms("13100690680", report);
					} catch (Exception e7) {
						e7.printStackTrace();
						System.out.println("回款监控短信发送异常...");
					}
				}*/
			
		}
		sendSMS4MoneyLogger.log(Level.INFO,"服务执行完成，发送失败数量：" + o + " 个");
		return succ("服务执行完成", "服务执行完成，发送失败数量：" + o + " 个");
	}
	
	@ActionKey("/sendSMS4Money")
	@Before({PkMsgInterceptor.class})
	public Message sendSMS4Money(){
		//查询需要回款  type 10  回款，  状态 9 等待发送短信
		Page<SMSLog> smsLogs = smsLogService.querySendSMS(1, 100, "10","9");
		int o = 0;
		sendSMS4MoneyLogger.log(Level.INFO,"开始扫描待发回款短信...");
		if(null != smsLogs && null != smsLogs.getList()){
			List<SMSLog> list = smsLogs.getList();
			for (int i = 0; i < list.size(); i++) {
				SMSLog smsLog = new SMSLog();
				smsLog = list.get(i);
				String mobile = smsLog.getStr("mobile");
				String content = smsLog.getStr("content");
				JSONArray objArr = JSONObject.parseArray("[" + content + "]");
				String payName = "";
				int loanCount = 0;
				long money = 0;
				if(null != objArr){
					for (int j = 0; j < objArr.size(); j++) {
						JSONObject object = objArr.getJSONObject(j);
						payName = object.getString("userName");
						money += object.getLongValue("money");
					}
					loanCount = objArr.size();
					String msgContent = CommonUtil.SMS_MSG_MONEY.replace("[payName]", payName)
							.replace("[loanCount]", String.valueOf(loanCount))
							.replace("[money]",String.format("%.2f",(money)/10.0/10.0));
					int result = smsService.sendSms(mobile, msgContent.toString());
//					System.out.println(msgContent);
					sendSMS4MoneyLogger.log(Level.INFO,msgContent+".....回款短信发送中...");
					if(0 != result){
						o++;
					}
					smsLog.set("status", result);
					smsLog.update();
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						o++;
					}
				}else{
					o++;
				}
			}
			
		}
		sendSMS4MoneyLogger.log(Level.INFO,"服务执行完成，发送失败数量：" + o + " 个");
		return succ("服务执行完成", "服务执行完成，发送失败数量：" + o + " 个");
	}

	private static Thread thread = null;
	
	@ActionKey("/sendSMS")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message sendSMS(){
		String mobiles = getPara("mobiles","");
		String type = getPara("type","0");
		String content = getPara("content");
		
		//sendSMS4MoneyLogger.log(Level.INFO,"准备发送短信...");
		if(StringUtil.isBlank(content)){
			return error("01", "发送内容不能为空!", "");
		}
		if(SMSThread.isRun == false){
			thread = new SMSThread(mobiles,type,content);
			thread.start();
			//sendSMS4MoneyLogger.log(Level.INFO,"服务启动成功,正在发送...");
			return succ("服务启动成功,正在发送","");
		}else{
			//sendSMS4MoneyLogger.log(Level.WARNING,"服务正在运行请不要重复启动或稍后再试");
			return error("01", "服务正在运行请不要重复启动或稍后再试","");
		}
		
	}
	
	@SuppressWarnings("deprecation")
	@ActionKey("/stopSMS")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message stopSMS(){
		if(SMSThread.isRun == false){
			return succ("服务已经停止或发送完毕","");
		}else{
			if(null != thread){
				thread.stop();//由于必须马上才停止才使用此方法，后期修改
			}
			return succ("服务已经停止","");
		}
		
	}
	
	/**
	 * 20180306
	 * 发放生日礼包（券的有效期为30天）
	 * 新手-1		10元现金券1张	满1000可用
	 * 普通会员-2		20元现金券1张	满1000可用
	 * 青铜会员-3		30元现金券1张	满5000可用
	 * 白银会员-4		50元现金券1张	满10000可用
	 * 黄金会员-5		30元、50元现金券各1张
	 * 白金会员-6		100元现金券1张	满10000可用
	 * 钻石会员-7 	50元、100元现金券各1张
	 * 黑钻会员-8		50元、100元现金券各2张
	 * 至尊会员-9 	50元、100元现金券各3张
	 * @param userCode	用户编码
	 * @param vipLevel	会员等级
	 */
	public int issueBirthdayGift(String userCode, int vipLevel){
		int totalAmount = 0;
		if(userCode != null ){
			switch (vipLevel) {
			case 1:
				ticketService.saveADV(userCode, "10元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 1000, 100000);
				totalAmount=10;
				break;
			case 2:
				ticketService.saveADV(userCode, "20元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 2000, 100000);
				totalAmount=20;
				break;
			case 3:
				ticketService.saveADV(userCode, "30元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
				totalAmount=30;
				break;
			case 4:
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				totalAmount=50;
				break;
			case 5:
				ticketService.saveADV(userCode, "30元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 3000, 500000);
				
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				totalAmount=80;
				break;
			case 6:
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				totalAmount=100;
				break;
			case 7:
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				totalAmount=150;
				break;
			case 8:
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				totalAmount=300;
				break;
			case 9:
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				ticketService.saveADV(userCode, "50元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 5000, 1000000);
				
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				ticketService.saveADV(userCode, "100元现金券【生日礼包】", DateUtil.addDay(DateUtil.getNowDate(), 30), 10000, 1000000);
				totalAmount=450;
				break;
			default:
				System.out.println(userCode + ">>>>>会员等级信息有误");
				break;
			}
		}
		return totalAmount;
	}
	
	
	
}




