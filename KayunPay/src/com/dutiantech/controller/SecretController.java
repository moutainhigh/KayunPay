package com.dutiantech.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.AuthRequire;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.EAuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.Funds;
import com.dutiantech.model.HistoryRecy;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class SecretController extends BaseController {

	private JXTraceService jxTraceService = getService(JXTraceService.class);
	private UserService userService = getService(UserService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private TicketsService ticketsService = getService(TicketsService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private BanksService banksService = getService(BanksService.class);
	private UserInfoService userInfoService =getService(UserInfoService.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);

	private static final Logger secretLogger = Logger.getLogger("secretLogger");

	static{
		LoggerUtil.initLogger("secret", secretLogger);
	}
	
	@ActionKey("/authTest")
	@AuthRequire.Role("SuperAdmin")
	@Before({EAuthInterceptor.class, PkMsgInterceptor.class})
	public Message authTest() {
		return succ("succ", null);
	}
	
	@ActionKey("/syncJxAccount")
	public void syncJxAccount() {
		int limit = getParaToInt("limit", 1);
		List<User> lstUser = User.userDao.find("SELECT * FROM t_user WHERE userCode IN (SELECT userCode FROM t_funds_trace WHERE traceDate > 20180520 GROUP BY userCode) AND NOT ISNULL(jxAccountId) AND ISNULL(userType) LIMIT 1600, ?", limit);
		for (int i = 0; i < lstUser.size(); i++) {
			User user = lstUser.get(i);
			if (!StringUtil.isBlank(user.getStr("jxAccountId"))) {
				try {
					Map<String, String> accountBalance = JXQueryController.balanceQuery(user.getStr("jxAccountId"));
					Map<String, String> freezeBalance = JXQueryController.freezeAmtQuery(user.getStr("jxAccountId"));
					long avBalance = StringUtil.getMoneyCent(accountBalance.get("availBal"));
					long frozeBalance = StringUtil.getMoneyCent(freezeBalance.get("bidAmt")) + StringUtil.getMoneyCent(freezeBalance.get("repayAmt")) + StringUtil.getMoneyCent(freezeBalance.get("plAmt"));
					fundsServiceV2.syncAccount(user.getStr("userCode"), avBalance, frozeBalance);
					secretLogger.log(Level.INFO, "["+(i+1)+"/"+lstUser.size()+"]用户["+user.getStr("userName")+"]["+user.getStr("userCode")+"]同步资金完成...");
				} catch (Exception e) {
					secretLogger.log(Level.INFO, "["+(i+1)+"/"+lstUser.size()+"]用户["+user.getStr("userName")+"]["+user.getStr("userCode")+"]同步资金失败...");
				}
			}
		}
	}
	
	/**
	 * 江西银行存管生成批量文件
	 * 
	 * @return
	 */
	@ActionKey("/CreateFile4JX")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message createUserInfo4JX(){
		//银行编号
		String jxBankNo = "3005";
		//产品编号
		String productCode = "0140";
		//合作编号
		String cooperationCode = "000322";
		//产品发行方
		String publishCode = "N7";
		//机构代码
		String instCode = "01530001";
		//存储路径
		String url  = "//data//jx_transfer_file//";
		//担保账户  等待开户
		String danbao = "6212462490000258458";
		String type = getPara("type");
		if("userInfo".equals(type)){
			long userNum = Db.queryLong("select count(1) from t_user");
			int n=0;
//			for(int i=0;i<1;i++){
			for(int i=0;i<userNum/35000+1;i++){
				String sqlSelect = "select t1.*,t2.userCardName,t2.userCardId ";
				String sqlFrom = " from t_user t1 left join t_user_info t2 on t1.userCode = t2.userCode where t1.jxAccountId is  null";
				String sqlOrder = " order by t1.regDate desc";
				Page<User> pageUser = User.userDao.paginate(i+1, 50000, sqlSelect, sqlFrom+sqlOrder );
				List<User> users = pageUser.getList();
				String str ="";
				for(int j=0;j<users.size();j++){
					User user = users.get(j);
					if(!StringUtil.isBlank(user.getStr("jxAccountId"))){
						continue;
					}
					String userCode = user.getStr("userCode");
					LoanTrace loanTrace = LoanTrace.loanTraceDao.findFirst("select * from t_loan_trace where loanState = 'N' and (payUserCode = ? or loanUserCode = ?)",userCode,userCode);
					if(null == loanTrace){
						continue;
					}
					StringBuffer sb = new StringBuffer("");
					String userType = user.getStr("userType");
					//1 .证件号
					String userCardId="";
					//2 .证件类型
					String idType = "01";
					//3 .用户真实姓名
					String trueName = user.getStr("userCardName");
					//4. 性别
					String gender = " ";
					//5 .手机号
					String userMobile="";
					//6. 账户类别
					String accountType = "";
					try {
						//解密用户手机号
						userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
					} catch (Exception e1) {
						userMobile = formateString("",12);
					}
					//若为企业借款人   企业借款人需要线下开户
					if(!"C".equals(userType)){
						try {
							//解密用户身份证号
							if(StringUtil.isBlank(user.getStr("userCardId"))){
								continue;
							}
							userCardId = CommonUtil.decryptUserCardId(user.getStr("userCardId"));
							if(StringUtil.isBlank(userCardId)||StringUtil.isBlank(trueName)){
								BanksV2 banksV2 = banksService.findByUserCode(userCode);
								if(null==banksV2||StringUtil.isBlank(banksV2.getStr("trueName"))){
									continue;
								}else{
									trueName = banksV2.getStr("trueName");
								}
							}
							if(userCardId.length()<15){
								continue;
							}
						} catch (Exception e) {
							userCardId = formateString("",18);
						}
						idType = "01";
						try {
							gender = IdCardUtils.getGenderByIdCard(userCardId);
						} catch (Exception e) {
							continue;
						}
						if ("M".equals(gender)) {
							gender = "1"; // 性别 男:1
						} else if ("F".equals(gender)) {
							gender = "2"; // 性别 女:2
						}
						accountType = "0";
					}else{
//						userCardId = instCode;
//						idType = "20";
//						gender = " ";
//						accountType = "1";
						continue;
					}
					//1 .证件号
					sb.append(formateString(userCardId,18));
					//2 .证件类型
					sb.append(idType);
					//3 .用户真实姓名
					sb.append(formateString(trueName, 60));
					//4 .性别
					sb.append(gender);
					//5 .手机号
					sb.append(formateString(userMobile,12));
					//6 .账户类别  0 个人  1 企业
					sb.append(accountType);
					//7 .email 地址
					sb.append(formateString("",40));
					//8 .用户编号
					sb.append(formateString(user.getStr("userCode"), 60));
					//9 .营业执照编号
					sb.append(formateString("",9));
					//10 .税务登记号
					sb.append(formateString("",30));
					//11 .渠道推荐码
					sb.append(formateString("",20));
					//12 .账户类型 0 基金账户 1 靠档计息账户 2 活期账户
					sb.append("2");
					//13 .基金公司代码
					sb.append("  ");
					//14 .请求方保留信息
					sb.append(formateString("",100));
					//15 .对公账户号
					String bankNo = "";
//					bankNo = "791910325100012";
//					if("20".equals(idType)){
//						bankNo = user.getStr("bankNo");
//						if(StringUtil.isBlank(bankNo)){
//							continue;
//						}
//					}
					sb.append(formateString(bankNo,32));
					//16 .营业执照编号1
					sb.append(formateString("",18));
					//17 .身份角色
					String role = "1";
					if("J".equals(userType)||"C".equals(userType)){
						role = "2";
					}
					if(userMobile.equals(CommonUtil.OUTCUSTNO)){
						role = "3";
					}
					sb.append(role);
					//18 .保留域
					sb.append(formateString("",26));
					str += sb.toString()+"\r\n";
					if((j+1)%1000==0){
						System.out.println("第"+(i+1)+"个文件生成到第："+(j+1));
					}
				}
				FileOperate file = new FileOperate();
				file.createFile(url+jxBankNo+"-APPZX"+productCode+"-"+(100000+i)+"-"+DateUtil.getNowDate(), str, "GBK");
				System.out.println("生成批量开户文件第"+(i+1)+"个文件");
			}
			//标迁移文件生成
		}else if("loanInfo".equals(type)){
			long loanNum = Db.queryLong("SELECT COUNT(1) from t_loan_info where loanState = 'N' and loanCode not in (SELECT loanCode from t_loan_trace where authCode is not null GROUP BY loanCode);");
//			for(int i=0;i<1;i++){
			for(int i=0;i<loanNum/10000+1;i++){
				List<LoanInfo> loanInfos = LoanInfo.loanInfoDao.find("SELECT * from t_loan_info where loanState = 'N' and loanCode not in (SELECT loanCode from t_loan_trace where authCode is not null GROUP BY loanCode) limit ?,?",i*10000,10000);
				String str = "";
				for(int j = 0;j<loanInfos.size();j++){
					LoanInfo loanInfo = loanInfos.get(j);
					User user = userService.findById(loanInfo.getStr("userCode"));
					StringBuffer sb = new StringBuffer("");
					//1 .银行代号
					sb.append(jxBankNo);
					//2 .批次号
					sb.append(200000+i);
					//3 .标的编号
					sb.append(formateString(loanInfo.getStr("loanCode"), 40));
					//4 .标的描述
					sb.append(formateString(loanInfo.getStr("loanTitle"), 60));
					//5 .借款人电子账号
//					String jxAccountId = "6212462490000080027";
//					sb.append(jxAccountId);
					if(StringUtil.isBlank(user.getStr("jxAccountId"))){
						continue;
					}
					sb.append(formateString(user.getStr("jxAccountId"), 19));
					//6 .借款金额
					String loanAmount = String.valueOf(loanInfo.getLong("loanAmount"));
					int length = 13-loanAmount.length();
					for(int n=0;n<length;n++){
						loanAmount="0"+loanAmount;
					}
					sb.append(loanAmount);
					//7 .付息方式 0 到期与本金一起归还 1 每月固定日支付 2 每月不确定日期支付
					sb.append("1");
					//8 .利息每月支付日
					String effectDate = loanInfo.getStr("effectDate");
					sb.append(effectDate.substring(6,8));
					//9 .项目期限
					int limit = loanInfo.getInt("loanTimeLimit");
					String overDate = DateUtil.addMonth(effectDate, limit);
					String days = DateUtil.differentDaysByMillisecond(effectDate,overDate,"yyyyMMdd")+"";
					int m = 4-days.length();
					for(int n=0;n<m;n++){
						days = "0"+days;
					}
					sb.append(days);
					//10 .预期年化收益率
					int rate = loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear");
					sb.append("0"+rate+"000");
					//11 .担保人电子账号
					sb.append(formateString(danbao, 19));
					//12 .名义借款人电子账号
					sb.append(formateString("", 19));
					//13 .多种借款人模式标志
					sb.append("0");
					//14 .收款人电子账户
					sb.append(formateString("", 19));
					//15 .受托支付标志
					sb.append("0");
					//16 .保留域
					sb.append(formateString("", 100));
					//17 .第三方平台保留域
					sb.append(formateString("", 100));
					str += sb.toString()+"\r\n";
					if((j+1)%1000==0){
						System.out.println("第"+(i+1)+"个文件生成到第："+(j+1));
					}
				}
				FileOperate file = new FileOperate();
				file.createFile(url+jxBankNo+"-BIDIN-"+cooperationCode+"-"+(200000+i)+"-"+DateUtil.getNowDate(), str, "GBK");
				System.out.println("生成批量标迁移文件第"+(i+1)+"个文件");
			}
			//标流水迁移文件生成
		}else if("loanTrace".equals(type)){
			int size = getParaToInt("size");
			int number = getParaToInt("number");
			long loanTraceNum = Db.queryLong("select count(1) from t_loan_trace where loanState = 'N' and authCode is null");
			for(int i=0;i<(loanTraceNum-size)/number+1;i++){
//			for(int i=0;i<1;i++){
//				String str = "";
//				Page<LoanTrace> pageLoanTrace = loanTraceService.findByPage(i+1, 48000,null,null,null,"N");
				List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find("select * from t_loan_trace  where loanState = 'N' and authCode is null  order by loanRecyDate LIMIT ?,?",size+i*number,number);
				for(int j = 0;j<loanTraces.size();j++){
					LoanTrace loanTrace = loanTraces.get(j);
					System.out.println("正在处理第"+(size+i*number+j)+"/"+loanTraceNum+"个标；标流水编号为："+loanTrace.getStr("traceCode"));
					//验证借款人是否开户
					User loanUser = userService.findById(loanTrace.getStr("loanUserCode"));
					String loanUserAccountId = loanUser.getStr("jxAccountId");
					if(StringUtil.isBlank(loanUserAccountId)){
						continue;
					}
					StringBuffer sb = new StringBuffer("");
					//1 .银行代号
					sb.append(jxBankNo);
					//2 .批次号
					sb.append(300000+size+i);
					//3 .债权持有人电子账号
					User user = userService.findById(loanTrace.getStr("payUserCode"));
					if(null==user){
						continue;
					}
					if(StringUtil.isBlank(user.getStr("jxAccountId"))){
						continue;
					}
					sb.append(formateString(user.getStr("jxAccountId"),19));
					//4 .产品发行方
					sb.append(formateString(publishCode, 4));
					//5 .标的号
					sb.append(formateString("", 6));
					//6 .申请流水号 临时占据授权码的位置    处理完结果文件后替换
					String uid = loanTrace.getInt("uid")+"";
					int uidLength = uid.length();
					for(int n=0;n<6-uidLength;n++){
						uid="0"+uid;
					}
					String orderId = DateUtil.getNowDateTime()+uid;
					sb.append(formateString(cooperationCode+"0000"+orderId, 40));
					//7 .当前持有债权金额
					String leftAmount = String.valueOf(loanTrace.getLong("leftAmount"));
					int length = leftAmount.length();
					for(int n=0;n<13-length;n++){
						leftAmount="0"+leftAmount;
					}
					sb.append(leftAmount);
					String loanCode = loanTrace.getStr("loanCode");
					LoanInfo loanInfo = loanInfoService.findById(loanCode);
					if(null ==loanInfo){
						continue;
					}
					//8 .债权获取日期
					sb.append(loanInfo.getStr("effectDate"));
					//9 .起息日
					sb.append(loanInfo.getStr("effectDate"));
					//10 .付息方式
					sb.append("1");
					//11 .利息每月支付日
					String effectDate = loanInfo.getStr("effectDate");
					sb.append(effectDate.substring(6,8));
					//12 .产品到期日
					int limit = loanTrace.getInt("loanTimeLimit");
					String overDate = DateUtil.addMonth(effectDate, limit);
					sb.append(overDate);
					//13 .预期年化收益率
					int rate = loanTrace.getInt("rateByYear")+loanTrace.getInt("rewardRateByYear");
					sb.append("0"+rate+"000");
					//14 .币种
					sb.append("156");
					//15 .标的编号-40
					sb.append(formateString(loanCode, 40));
					//16 .保留域
					sb.append(formateString("", 60));
					if((j+1)%100==0){
						System.out.println("第"+(i+1)+"个文件生成到第："+(j+1));
					}
//					str += sb.toString();
					FileOperate file = new FileOperate();
					String text = "";
					try {
						text = file.readTxt(url+jxBankNo+"-BID-"+(300000+size+i), "GBK");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						text = "";
					}
					text += sb.toString();
					file.createFile(url+jxBankNo+"-BID-"+(300000+size+i), text, "GBK");
				}
//				FileOperate file = new FileOperate();
//				file.createFile(url+jxBankNo+"-BID-"+(300000+size+i), str, "GBK");
				System.out.println("生成批量标流水迁移文件第"+(i+1)+"个文件");
			}
		}else{
			return error("SB", "玩儿呢？叫你们老板来","");
		}
		return succ("生成成功", "");
	}
	
	/**
	 * 处理批量上传文件结果
	 * */
	@ActionKey("/JxFileDispose")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message JxFileDispose(){
//		UploadFile file = UploadFile.makeFile(getRequest());
		FileOperate file = new FileOperate();
		try {
		for(int n = 0;n<1;n++){
//			String urlName ="F://cs//";
			String urlName ="//home//jx_loanTraceDown//";
			String fileName = "3005-BIDRESP-"+(302000+n)+"-20180607";
			String[] files = file.readTxtLine(urlName+fileName, "GBK");
				int len = files.length;
				for(int i = 0;i<len;i++){
					System.out.println("正在处理"+fileName+"文件到第"+(i+1)+"/"+len+"行");
					byte[] loanTraceFile = files[i].getBytes("GBK");
					//获取响应码
					String rspCode = "";
					for(int  j= 160;j<162;j++){
						byte[] temp2 = {loanTraceFile[j]};
						rspCode += new String(temp2,"GBK");
					}
					//获取用户orderId
					String orderId = ""; 
					for(int j = 49;j<79;j++){
						byte[] temp3 = {loanTraceFile[j]};
						orderId += new String(temp3,"GBK");
					}
					orderId = orderId.trim();
					String uid = orderId.substring(14,orderId.length());
					LoanTrace loanTrace = LoanTrace.loanTraceDao.findFirst("select * from t_loan_trace where uid = ?",uid);
					if(null==loanTrace){
						continue;
					}
					if(!StringUtil.isBlank(loanTrace.getStr("orderId"))){
						continue;
					}
					if(!"00".equals(rspCode)){
						continue;
					}
					//获取授权码  记录授权码
					String authCode = "";
					for(int j = 162;j<182;j++){
						byte[] temp4 = {loanTraceFile[j]};
						authCode += new String(temp4,"GBK");
					}
					String amount = "";
					for(int j = 79;j<92;j++){
						byte[] temp5 = {loanTraceFile[j]};
						amount += new String(temp5,"GBK");
					}
					long orgAmount = Long.parseLong(amount);
					authCode = authCode.trim();
					loanTrace.set("orderId", orderId);
					loanTrace.set("orgAmount", orgAmount);
					loanTrace.set("authCode", authCode);
					loanTrace.update();
				}
			
		
		} }catch (Exception e) {
			e.printStackTrace();
		}
		return succ("OK", "成功");
	}
	
	/**
	 * 生成固定字节字符串  为JX存管生成批量文件服务
	 * str 字符
	 * n 字节数
	 * */
	private static String formateString(String str,int n){
		int l=0;
		if(null!=str){
			try {
				l = str.getBytes("GBK").length;
			} catch (Exception e) {
				str="";
			}
		}else{
			str="";
		}
		StringBuffer sb = new StringBuffer(str);
		for(int j=0;j<n-l;j++){
			sb.append(" ");
		}
		return sb.toString();
	}
	
	/**
	 * 生成委托提现excle文件   江西迁移
	 * */
	@ActionKey("/rechangeExcle")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public void rechangeExcle(){
		List<Funds> fundsList = Funds.fundsDao.find("select t1.userCode, t1.avBalance,t2.loginId from t_funds t1 left join t_user t2 on t1.userCode = t2.userCode where t1.avBalance>300 and t2.userType is null and t2.loginId is not null");
		int num = fundsList.size();
		String html = "<table border='1'>";
		html += "<tr><td>序号</td><td>委托提现目标登录名</td><td>委托提现目标中文名称</td><td>委托提现金额</td><td>备注信息</td></tr>";
		for(int i = 0;i<fundsList.size();i++){
			Funds funds = fundsList.get(i);
			long avBalance = funds.getLong("avBalance");
			String loginId = funds.getStr("loginId");
			try {
				loginId = CommonUtil.decryptUserMobile(loginId);
			} catch (Exception e) {
				loginId = "";
			}
			if(loginId.equals(CommonUtil.OUTCUSTNO)){
				System.out.println("第"+(i+1)+"/"+num+"这个是吴总的号，屏蔽掉");
				continue;
			}
			String userCode = funds.getStr("userCode");
			UserInfo userInfo = userInfoService.findById(userCode);
			String userTrueName = "";
			if(null == userInfo){
				List<BanksV2> banksV2 = banksService.findBanks4User(userCode);
				if(null != banksV2&&banksV2.size()>0){
					userTrueName = banksV2.get(0).getStr("trueName");
				}
			}else{
				userTrueName = userInfo.getStr("userCardName");
			}
			html += "<tr><td>"+(i+1)+"</td>";
			html += "<td>"+loginId+"</td>";
			html += "<td>"+userTrueName+"</td>";
			html += "<td>"+Number.longToString2(avBalance-300)+"</td>";
			html += "<td></td></tr>";
			//添加流水
			if(fundsServiceV2.withdrawals4Fuiou(userCode, avBalance, 300, "委托提现")){
				fundsServiceV2.updateTotalWithdraw(userCode, 0-avBalance);
				String userName = userService.findByField(userCode, "userName").getStr("userName");
				String userTureName = userInfoService.findByFields(userCode, "userCardName").getStr("userCardName");
				String withdrawCode = UIDUtil.generate();
				withdrawTraceService.save(withdrawCode,userCode, userName, userTureName, "", "", "", "", "", avBalance,SysEnum.withdrawTraceState.D.val(), "0","委托提现",DateUtil.getNowDateTime(),null,true);
			}
			System.out.println("用户编号："+userCode+"第"+(i+1)+"/"+num);
			}
		html += "</table>";
		HttpServletResponse response = getResponse();
		response.setCharacterEncoding("ISO_8859_1");
		String output_extType = "application/vnd.ms-excel";
		String filename="PWTX_"+DateUtil.getNowDate()+"_0001";
		try {
			filename= new String(filename.getBytes("utf-8"), "ISO_8859_1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setHeader("Content-Disposition", "attachment;filename="+filename+".xlsx");
		renderText(html, output_extType);
	}
	
	/***
	 * 生成批量代扣处理文件
	 */
	@ActionKey("/creatWithholdFile")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message creatWithholdFile(){
		//银行编号
		String jxBankNo = "3005";
		//合作编号
		String cooperationCode = "000322";
		//存储路径
//		String url  = "F://dkds//";
		String url  = "//data//jx_transfer_file//";
		//代偿户电子帐号
//		String dAccountId = "6212462490000060011";
		String dAccountId = "6212462490000258458";
		//代偿户姓名
//		String dName = "李敏";
		String dName = "吴志新";
		String[] jxTraces = {
				"20180527002709127325",		
				"20180527140153144199",		
				"20180527140203139540",		
				"20180527143524116863",		
				"20180527143730111359",		
				"20180527143731149962",		
				"20180527145640119740",		
				"20180527145754109269",		
				"20180527150908145363",		
				"20180527151051134184",		
				"20180527151304104815",		
				"20180527154812104471",		
				"20180527163339106340",		
				"20180527163552129643",		
				"20180527163728142668",		
				"20180527163756124882",		
				"20180527163805147783",		
				"20180528022641147847"
		};
//		String[] jxTraces = {
//				"20180515183411103418",		
//				"20180517113609111468",
//				"20180522173609148719"
//		};
		//生成批量代扣文件文件
		//1.银行代号
		String bankNo = jxBankNo;
		//2.分行代号
		String BRANCH = "9999";
		//3.网点代号
		String BRNO = cooperationCode;
		//4.柜员代号
		String TELLER = "999999";
		//5.批号
		String BATCH = "101118";
		//6.业务类别
		String TYPE = "100";
		//7.文件传输日期
		String Date = DateUtil.getNowDate();
		//8.参考信息
		String ref = "";
		//9.应扣金额
		String amount = "";
		//10.币种
		String curr = "156";
		//11.处理日期
		String dealdate = Date ;
		//12.电子帐号
		String cARDNBR = "";
		//13.客户姓名
		String Name = "";
		//14.保留域
		String rese = "";
		String str = "";  //代扣文件
		String str2 = "";//代收文件
		long amountAll=0;
		for(int i = 0;i<jxTraces.length;i++){
			String jxTraceCode = jxTraces[i];
			JXTrace jxTrace = jxTraceService.findById(jxTraceCode);
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String subPacks = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks);
			for(int j = 0 ; j<parseArray.size();j++){
				System.out.println("开始生成第"+(i+1)+"/"+jxTraces.length+"-"+(j+1)+"/"+parseArray.size()+"个文件");
				JSONObject jsonObject = parseArray.getJSONObject(j);
				cARDNBR = jsonObject.getString("forAccountId");
				User user = userService.findByJXAccountId(cARDNBR);
				if(null == user){
					continue;
				}
				String userCode = user.getStr("userCode");
				BanksV2 banksV2 = banksService.findByUserCode(userCode);
				Name = banksV2.getStr("trueName");
				String txamount = jsonObject.getString("txAmount");
				String intamount = jsonObject.getString("intAmount");
				String txFeeIn = jsonObject.getString("txFeeIn");
				if(StringUtil.isBlank(txFeeIn)){
					txFeeIn = "0.00";
				}
				amount = String.valueOf(StringUtil.getMoneyCent(txamount)+StringUtil.getMoneyCent(intamount)+StringUtil.getMoneyCent(txFeeIn));
				amountAll += Long.parseLong(amount);
				int length = amount.length();
				for(int n=0;n<12-length;n++){
					amount="0"+amount;
				}
				//代扣字符
				StringBuffer sb = new StringBuffer();
				sb.append(bankNo);
				sb.append(BRANCH);
				sb.append(BRNO);
				sb.append(TELLER);
				sb.append(BATCH);
				sb.append(TYPE);
				sb.append(Date);
				sb.append(formateString(ref, 19));
				sb.append(amount);
				sb.append(curr);
				sb.append(dealdate);
				sb.append(formateString(cARDNBR, 19));
				sb.append(formateString(Name, 60));
				sb.append(formateString(rese, 100));
				str += sb.toString()+"\r\n";
				
				//代收字符
				StringBuffer sb2 = new StringBuffer();
				sb2.append(bankNo);
				sb2.append(BRANCH);
				sb2.append(BRNO);
				sb2.append(TELLER);
				sb2.append("101329");
				sb2.append(formateString("", 10));
				sb2.append(TYPE);
				sb2.append(Date);
				sb2.append(formateString(ref, 19));
				sb2.append(amount);
				sb2.append(curr);
				sb2.append(dealdate);
				sb2.append(formateString(dAccountId, 19));
				sb2.append(formateString(dName, 60));
				sb2.append(formateString("",8));
				sb2.append(formateString("", 40));
				sb2.append(formateString(rese, 52));
				str2 += sb2.toString()+"\r\n";
			}
		}
		FileOperate file = new FileOperate();
		file.createFile(url+jxBankNo+"-PAYREQN-"+BATCH+"-"+cooperationCode+"-"+Date, str, "GBK");
		file.createFile(url+jxBankNo+"-TRXN-101329-"+cooperationCode+"-"+Date, str2, "GBK");
		System.out.println("生成批量代扣文件文件,总金额为："+amountAll);
		return succ("01", "文件成功生成");
	}
	
	/******************************************************************************************************/
	

	/**
	 * 生成单条回款计划
	 * @param index
	 * @param loanInfo
	 * @param lstLoanTrace
	 * @return
	 */
	@ActionKey("/doRepaymentTest")
	@Before(PkMsgInterceptor.class)
	public Message generateRepaymentPlan() {
		String startDateTime = DateUtil.getNowDateTime();
		int limit = getParaToInt("limit", 1);
//		List<LoanInfo> lstLoanInfo = LoanInfo.loanInfoDao.find("SELECT * FROM t_loan_info WHERE loanState = 'N' AND loanCode NOT IN (SELECT loanCode FROM t_loan_repayment GROUP BY loanCode) limit 0, ?", limit);
		List<LoanInfo> lstLoanInfo = LoanInfo.loanInfoDao.find("SELECT * FROM t_loan_info WHERE loanCode = '04cf5fb984b242ecb84946efd6ddb00d'");
		for (LoanInfo loanInfo : lstLoanInfo) {
			String loanCode = loanInfo.getStr("loanCode");

			System.out.println("开始生成标[" + loanCode + "]回款计划..." + DateUtil.getNowDateTime());
			generateRepaymentPlan(loanCode);
			System.out.println("生成标[" + loanCode + "]回款计划完成..." + DateUtil.getNowDateTime());
			List<LoanTrace> lstLoanTrace = loanTraceService.findAllByLoanCode(loanCode);
			System.out.println("填补标[" + loanCode + "]已还期数开始..." + DateUtil.getNowDateTime());
			for (LoanTrace loanTrace : lstLoanTrace) {
				String loanTraceCode = loanTrace.getStr("traceCode");
				fillHistoryRepaymentPlan(loanTraceCode, loanCode);
			}
			System.out.println("填补标[" + loanCode + "]已还期数完成..." + DateUtil.getNowDateTime());
		}
		String endDateTime = DateUtil.getNowDateTime();
		System.out.println("批次完成...用时: " + DateUtil.differentMinuteByMillisecond(startDateTime, endDateTime, "yyyyMMddHHmmss") + "分钟");
		return succ("完成", null);
	}
	
	/**
	 * 修复数据条目不正确的回款计划
	 * @return
	 */
	@ActionKey("/validateRepayment")
	@Before(PkMsgInterceptor.class)
	public Message validateRepayment() {
		String startDateTime = DateUtil.getNowDateTime();
		
		List<LoanTrace> lstLoanTrace = LoanTrace.loanTraceDao.find("SELECT loanCode, COUNT(1) * loanTimeLimit AS traceCount FROM t_loan_trace WHERE loanState = 'N' GROUP BY loanCode");
		int i = 1;
		for (LoanTrace loanTrace : lstLoanTrace) {
			String loanCode = loanTrace.get("loanCode");
			long traceCount = loanTrace.getLong("traceCount");
			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanCode = ?", loanCode);
			
			System.out.println("开始验证标["+loanCode+"]回款计划是否完整...[" + i + "/" + lstLoanTrace.size() + "]" + DateUtil.getNowDateTime());
			
			if (traceCount != lstLoanRepayment.size()) {
//				System.out.println("标[" + loanCode + "]回款计划异常，重新生成开始..." + DateUtil.getNowDateTime());
				System.out.println("标[" + loanCode + "]回款计划异常");
				break;
//
//				for (LoanRepayment loanRepayment : lstLoanRepayment) {
//					loanRepayment.delete();
//				}
//				
//				System.out.println("开始生成标[" + loanCode + "]回款计划..." + DateUtil.getNowDateTime());
//				generateRepaymentPlan(loanCode);
//				System.out.println("生成标[" + loanCode + "]回款计划完成..." + DateUtil.getNowDateTime());
//				List<LoanTrace> lstLoanTraceTmp = loanTraceService.findAllByLoanCode(loanCode);
//				System.out.println("填补标[" + loanCode + "]已还期数开始..." + DateUtil.getNowDateTime());
//				for (LoanTrace trace : lstLoanTraceTmp) {
//					String loanTraceCode = trace.getStr("traceCode");
//					fillHistoryRepaymentPlan(loanTraceCode, loanCode);
//				}
//				System.out.println("填补标[" + loanCode + "]已还期数完成..." + DateUtil.getNowDateTime());
//				
//				System.out.println("标[" + loanCode + "]重新生成、填补回款计划完成..." + DateUtil.getNowDateTime());
			}
			i++;
			
		}
		
		return succ("完成...用时..." + DateUtil.differentMinuteByMillisecond(startDateTime, DateUtil.getNowDateTime(), "yyyyMMddHHmmss") + "分钟", null);
	}
	
	// 重新填补未还款数据
	@ActionKey("/reFillRepayment")
	@Before(PkMsgInterceptor.class)
	public Message reFillRepayment() {
		try {
			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE recyStatus = 0 AND repaymentDate <= DATE_FORMAT(now(),'%Y%m%d');");
			for (LoanRepayment loanRepayment : lstLoanRepayment) {
				String loanTraceCode = loanRepayment.getStr("loanTraceCode");
				String payUserCode = loanRepayment.getStr("userCode");
				String loanCode = loanRepayment.getStr("loanCode");
				int period = loanRepayment.getInt("recyPeriod");
				long repaymentPrincipal = 0l;
				long repaymentInterest = 0l;
				long interestFee = 0l;
				int overdueDays = 0;
				long overdueInterest = 0l;
				long payAmount = loanRepayment.getLong("payAmount");
				
				HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT * FROM t_history_recy WHERE loanCode = ? AND payAmount = ? AND recyLimit = ?", loanCode, payAmount, period);
				if (historyRecy != null) {
					if (!payUserCode.equals(historyRecy.getStr("payUserCode"))) {
						loanRepaymentService.update4Transfer(loanTraceCode, historyRecy.getStr("payUserCode"), historyRecy.getStr("payUserName"), period);
					}
					repaymentPrincipal = historyRecy.getLong("recyAmount");
					repaymentInterest = historyRecy.getLong("recyInterest");
					
					System.out.println("填补回款["+loanRepayment.get("rid")+"]...");
					loanRepaymentService.repayment(loanTraceCode, period, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest);
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		return succ("完成...", null);
	}
	
	@ActionKey("/settleRepayment4early")
	@Before(PkMsgInterceptor.class)
	public Message settleRepayment4early() {
		int limit = getParaToInt("limit", 1);
		try {
			List<SettlementEarly> lstSettlementEarly = SettlementEarly.settletmentEaryltDao.find("SELECT * FROM t_settlement_early WHERE loanCode IN (SELECT loanCode FROM t_loan_repayment WHERE recyStatus = 0 GROUP BY loanCode) AND estatus = 'C' ORDER BY settlementDate DESC LIMIT 0, ?", limit);
			for (SettlementEarly settlementEarly : lstSettlementEarly) {
				System.out.println("处理标["+settlementEarly.getStr("loanCode")+"]提前回款计划...");
				String repaymentDate = settlementEarly.getStr("earlyDate");
				List<LoanTrace> lstLoanTrace = loanTraceService.findAllByLoanCode(settlementEarly.getStr("loanCode"));
				for (LoanTrace loanTrace : lstLoanTrace) {
//					List<LoanRepayment> lstLoanRepayment = loanRepaymentService. (loanTrace.getStr("traceCode"));
					List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyStatus = 0", loanTrace.getStr("traceCode"));
					for (LoanRepayment loanRepayment : lstLoanRepayment) {
						HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT * FROM t_history_recy WHERE payUserCode = ? AND loanCode = ? AND recyLimit = ?", loanRepayment.getStr("userCode"), loanRepayment.getStr("loanCode"), loanRepayment.getInt("recyPeriod"));
						if (historyRecy == null) {
							loanRepaymentService.repayment(loanTrace.getStr("traceCode"), loanRepayment.getInt("recyPeriod"), 0, 0, 0, 0, 0, repaymentDate);
						} else {
							loanRepaymentService.repayment(loanTrace.getStr("traceCode"), loanRepayment.getInt("recyPeriod"), historyRecy.getLong("recyAmount"), historyRecy.getLong("recyInterest"), 0, 0, 0, historyRecy.getStr("recyDate"));
						}
					}
				}
				System.out.println("标["+settlementEarly.getStr("loanCode")+"]结算提前回款计划完成...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return succ("完成...", null);
	}
	
	@ActionKey("/checkRepayment")
	@Before(PkMsgInterceptor.class)
	public Message checkRepayment() {
		int limit = getParaToInt("limit", 1);
		List<Object[]> lst = Db.query("SELECT loanTraceCode, COUNT(1), payAmount FROM t_loan_repayment GROUP BY loanTraceCode HAVING sum(repaymentPrincipal) != payAmount LIMIT 0, ?", limit);
		int count = 1;
		for (Object[] str : lst) {
			long payAmount = Long.parseLong(str[2].toString());
			long sumPrincipal = 0l;
			List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ?", str[0].toString());

			int i = 1;
			for (LoanRepayment loanRepayment : lstLoanRepayment) {
				if (i != lstLoanRepayment.size()) {
					sumPrincipal += loanRepayment.getLong("repaymentPrincipal");
				}
				i++;
			}
			
			long leftPrincipal = payAmount - sumPrincipal;
			
			LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", str[0].toString(), lstLoanRepayment.size());
			if (loanRepayment.getLong("repaymentPrincipal") != leftPrincipal) {
				loanRepayment.set("repaymentPrincipal", leftPrincipal);
				loanRepayment.set("repaymentAmount", leftPrincipal + loanRepayment.getLong("repaymentInterest"));
				System.out.println("("+count+"/"+lst.size()+")投标流水["+str[0]+"]金额更新..." + (loanRepayment.update() ? "成功" : "失败"));
				count++;
			}
		}
		return succ("完成...", null);
	}
	
	@ActionKey("/doRepaymentTransfer")
	@Before(PkMsgInterceptor.class)
	public Message doRepaymentTransfer() {
		try {
			String traceCode = getPara("traceCode");
			String days = getPara("days", "");
			String limit = getPara("limit", "");
			List<String> lstLoanTraceCode = null;
			if (!StringUtil.isBlank(limit)) {
				lstLoanTraceCode = Db.query("SELECT loanTraceCode FROM t_loan_repayment WHERE loanTraceCode IN(SELECT traceCode FROM t_loan_transfer WHERE transState = 'B') GROUP BY loanTraceCode LIMIT 0, ?", Integer.parseInt(limit));
			} else if (!StringUtil.isBlank(traceCode)) {
				lstLoanTraceCode = Db.query("SELECT loanTraceCode FROM t_loan_repayment WHERE loanTraceCode IN(SELECT traceCode FROM t_loan_transfer WHERE transState = 'B') AND loanTraceCode = ? GROUP BY loanTraceCode", traceCode);
			} else if (!StringUtil.isBlank(days)) {
				lstLoanTraceCode = Db.query("SELECT loanTraceCode FROM t_loan_repayment WHERE loanTraceCode IN(SELECT traceCode FROM t_loan_transfer WHERE transState = 'B' AND transDate BETWEEN DATE_FORMAT(DATE_SUB(now(),INTERVAL ? DAY), '%Y%m%d') AND DATE_FORMAT(now(), '%Y%m%d')) GROUP BY loanTraceCode", Integer.parseInt(days));
			}
			
			for (int i = 0; i < lstLoanTraceCode.size(); i++) {
				String loanTraceCode = lstLoanTraceCode.get(i);
				System.out.println("("+(i+1)+"/"+lstLoanTraceCode.size()+")流水[" + loanTraceCode + "]债转更新开始...");
				LoanTrace loanTrace = loanTraceService.findById(loanTraceCode);
				LoanInfo loanInfo = loanInfoService.findById(loanTrace.getStr("loanCode"));
				int loanPeriod = loanInfo.getInt("loanTimeLimit");
				// 重新生成回款计划
				String json_tickets = loanTrace.getStr("loanTicket");
				int rate = 0;
				if(StringUtil.isBlank(json_tickets)==false){
					JSONArray ja = JSONArray.parseArray(json_tickets);
					for (int k = 0; k < ja.size(); k++) {
						JSONObject jsonObj = ja.getJSONObject(k);
						if(jsonObj.getString("type").equals("C")){
							rate = jsonObj.getInteger("rate");
						}
					}
				}
				long sumPrincipal = 0l;
				for (int j = 1; j <= loanPeriod; j++) {
					LoanRepayment loanRepayment = LoanRepayment.loanRepaymentDao.findFirst("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod = ?", loanTraceCode, j);
					long payAmount = loanTrace.getLong("payAmount");
					int rateByYear = loanTrace.getInt("rateByYear");
					int rewardRateByYear = loanTrace.getInt("rewardRateByYear");
					String refundType = loanTrace.getStr("refundType");
					long[] repayment = CommonUtil.f_000(payAmount, loanPeriod, rateByYear + rewardRateByYear + rate, j, refundType);
					if (j == loanPeriod) {
						repayment[0] = loanTrace.getLong("payAmount") - sumPrincipal;
					} 
					sumPrincipal += repayment[0];
					loanRepayment.set("repaymentAmount", repayment[0] + repayment[1]);
					loanRepayment.set("repaymentPrincipal", repayment[0]);
					loanRepayment.set("repaymentInterest", repayment[1]);
					loanRepayment.update();
				}
				
				// 处理债转数据
				List<LoanTransfer> lstLoanTransfer = LoanTransfer.loanTransferDao.find("SELECT * FROM t_loan_transfer WHERE traceCode = ? AND transState = 'B'", lstLoanTraceCode.get(i));
				for (LoanTransfer loanTransfer : lstLoanTransfer) {
					String gotUserCode = loanTransfer.getStr("gotUserCode");
					String gotUserName = loanTransfer.getStr("gotUserName");
					long leftAmount = Long.parseLong(loanTransfer.get("leftAmount").toString());
					int leftPeriod = loanTransfer.getInt("loanRecyCount");
					int rateByYear = loanTransfer.getInt("rateByYear");
					int rewardRateByYear = loanTransfer.getInt("rewardRateByYear");
					String refundType = loanTransfer.getStr("refundType");
					
					
					List<LoanRepayment> lstLoanRepayment = LoanRepayment.loanRepaymentDao.find("SELECT * FROM t_loan_repayment WHERE loanTraceCode = ? AND recyPeriod > ?", loanTraceCode, loanPeriod - loanTransfer.getInt("loanRecyCount"));
					long sumRepaymentPrincipal = 0l;
					for (int k = 0; k < lstLoanRepayment.size(); k++) {
						LoanRepayment loanRepayment = lstLoanRepayment.get(k);
						loanRepayment.set("userCode", gotUserCode);
						loanRepayment.set("userName", gotUserName);
						long repaymentAmount = loanRepayment.getLong("repaymentAmount");
						long repaymentYesAmount = loanRepayment.getLong("repaymentYesAmount");
						
						long[] repayment = CommonUtil.f_000(leftAmount, leftPeriod, rateByYear + rewardRateByYear, k + 1, refundType);
						long repaymentPrincipal = repayment[0];
						long repaymentInterest = repayment[1];
						if (k + 1 == lstLoanRepayment.size()) {
							List<LoanRepayment> tmpLoanRepayment = loanRepaymentService.findByLoanTrace(loanTraceCode);
							for (int j = 0; j < tmpLoanRepayment.size(); j++) {
								if ((j + 1) != tmpLoanRepayment.size()) {
									sumRepaymentPrincipal += tmpLoanRepayment.get(j).getLong("repaymentPrincipal");
								}
							}
							repaymentPrincipal = loanTrace.getLong("payAmount") - sumRepaymentPrincipal;
						}
						
						if (repaymentAmount != repaymentYesAmount) {
//							System.out.println("["+loanRepayment.get("rid")+"]updating...");
							loanRepayment.set("repaymentAmount", repaymentPrincipal + repaymentInterest);
							loanRepayment.set("repaymentPrincipal", repaymentPrincipal);
							loanRepayment.set("repaymentInterest", repaymentInterest);
							loanRepayment.update();
						}
					}
				}
				System.out.println("("+(i+1)+"/"+lstLoanTraceCode.size()+")流水[" + loanTraceCode + "]债转更新完成...");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return succ("完成...", null);
	}
	
	public Message generateRepaymentPlan(String loanCode) {
//		String loanCode = "876a9e88d17f48d184cb58f8e3bacf7a";
		List<LoanTrace> lstLoanTrace = loanTraceService.findAllByLoanCode(loanCode);
		for (int j = 0; j < lstLoanTrace.size(); j++) {
//			String traceCode = "4401ef69a82142228ce89598d0f0e479";
			String loanTraceCode = lstLoanTrace.get(j).getStr("traceCode");
			List<LoanRepayment> lstLoanRepayment = loanRepaymentService.findByLoanTrace(loanTraceCode);	// 根据投标记录查询回款计划是否已生成
			if (lstLoanRepayment.size() <= 0) {
				LoanTrace loanTrace = loanTraceService.findById(loanTraceCode);
				LoanInfo loanInfo = loanInfoService.findById(loanTrace.getStr("loanCode"));
				Long payAmount = loanTrace.getLong("payAmount");
				Integer loanTimeLimit = loanInfo.getInt("loanTimeLimit");
				Integer rateByYear = loanInfo.getInt("rateByYear");
				Integer rewardRateByYear = loanTrace.getInt("rewardRateByYear");
				String refundType = loanInfo.getStr("refundType");
				int benefits4new = loanInfo.getInt("benefits4new");
				int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
				
				long sumRepaymentPrincipal = 0l;
				for (int i = 1; i <= loanTimeLimit; i++) {
					long[] repayment = CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear + rewardRateByYear, i, refundType);
					if (i == loanTimeLimit) {
						repayment[0] = payAmount - sumRepaymentPrincipal;
					}
					System.out.println("Limit " + i + "==== Repayment Principal: " + repayment[0] + " #### Repayment Interest: " + repayment[1]);
					String payUserCode = loanTrace.get("payUserCode");
					long repaymentPrincipal = repayment[0];
					long repaymentInterest = repayment[1];
					long interestFee = repayment[1] * 8 / 100;
					sumRepaymentPrincipal += repaymentPrincipal;
					
					// 首月加息活动计算利息
					if (releaseDate >= 20171111 && releaseDate <= 20171117) {
						if (i == 1 && benefits4new == 0) {
							repaymentInterest = CommonUtil.f_004(payAmount, rateByYear + 400, i, refundType)[1];
						}
					}
					
					if( repaymentInterest > 0 ){
						//计算利息管理费
						User user = userService.findUserAllInfoById(payUserCode) ;
						int vipInterestRate = user.getInt("vipInterestRate") ;//利息管理费费率
						String effectDate = loanInfo.getStr("effectDate");//满标日期
						if(Long.parseLong(effectDate) < 20180319){//会员等级更新之前，利息管理费费率不变
							if(user.getInt("beforeVip") != null){
								vipInterestRate = historyInterest(user.getInt("beforeVip"));//根据原用户等级，获取对应等级利息管理费费率
							}
						}
						int vipRiskRate = user.getInt("vipRiskRate") ;
						long tmpInterestFee = 0 ;
						//利息管理费
						if( vipInterestRate > 0 ){
							tmpInterestFee = repaymentInterest * vipInterestRate / 10 / 10 / 10 / 10;
						}
						long tmpRiskFee = 0 ;
						if( vipRiskRate > 0 ){
							tmpRiskFee = repaymentInterest * vipRiskRate / 10 / 10 / 10 / 10;
							if( tmpRiskFee > 0 ){
								Db.update("update t_sys_funds set riskTotal=riskTotal+?,updateDate=?,updateTime=? where id=1",
										tmpRiskFee , DateUtil.getNowDate(),DateUtil.getNowTime());
							}
						}
						interestFee = tmpInterestFee + tmpRiskFee ;
					}
					
					Map<String, Object> para = new HashMap<String, Object>();
					para.put("userCode", payUserCode);
					para.put("userName", loanTrace.get("payUserName"));
					para.put("loanNo", loanInfo.get("loanNo"));
					para.put("loanCode", loanInfo.get("loanCode"));
					para.put("payAmount", loanTrace.get("payAmount"));
					para.put("recyPeriod", i);
					para.put("recyStatus", 0);
					para.put("repaymentAmount", repaymentPrincipal + repaymentInterest);
					para.put("repaymentPrincipal", repaymentPrincipal);
					para.put("repaymentInterest", repaymentInterest);
					para.put("interestFee", interestFee);
					para.put("repaymentDate", DateUtil.addMonth(loanInfo.getStr("effectDate"), i));
					para.put("loanTraceCode", loanTrace.get("traceCode"));
					loanRepaymentService.save(para);
				}
			}
		}
		return succ("test", "success");
	}
	
	public void fillHistoryRepaymentPlan(String loanTraceCode, String loanCode) {
		System.out.println("start fill [" + loanTraceCode + "] history repayment plan..." + DateUtil.getNowDateTime());
		LoanTrace loanTrace = loanTraceService.findById(loanTraceCode);
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		int reciedCount = loanInfo.getInt("reciedCount");
		int recyLimit;
		
		for (int i = 1; i <= reciedCount; i++) {
			System.out.println("repayment period: " + i + "... " + DateUtil.getNowDateTime());
			HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT recyAmount, recyInterest, recyDate FROM t_history_recy WHERE loanCode = ? AND recyLimit = ? AND payUserCode = ? AND payAmount = ?", loanCode, i, loanTrace.getStr("payUserCode"), loanTrace.getLong("payAmount"));
			recyLimit = i;
			if (historyRecy != null) {
				long repaymentPrincipal = historyRecy.getLong("recyAmount");
				long repaymentInterest = historyRecy.getLong("recyInterest");
				long interestFee = 0;
				int overdueDays = 0;
				long overdueInterest = 0;
				String repaymentDate = historyRecy.getStr("recyDate");

				int benefits4new = loanInfo.getInt("benefits4new");
				int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
				// 首月加息活动计算利息
				if (releaseDate >= 20171111 && releaseDate <= 20171117) {
					if (i == 1 && benefits4new == 0) {
						repaymentInterest = CommonUtil.f_004(loanTrace.getLong("payAmount"), loanInfo.getInt("rateByYear") + 400, i, loanInfo.getStr("refundType"))[1];
					}
				}
				
//				FundsTrace fundsTrace = FundsTrace.fundsTraceDao.findFirst("SELECT * FROM t_funds_trace WHERE userCode = '"+loanTrace.getStr("payUserCode")+"' AND traceAmount = "+repaymentInterest+" AND traceRemark like '%["+loanInfo.getStr("loanNo")+"]第"+i+"%' AND traceType = 'L'"); 
//				if (fundsTrace != null) {
//					interestFee = fundsTrace.getInt("traceFee");
//				}
				
				System.out.println("repaymenting...." + DateUtil.getNowDateTime());
				loanRepaymentService.repayment(loanTraceCode, recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
			} else {
				getLoanTransfer(loanTrace.getStr("payUserCode"), loanInfo, loanTrace, recyLimit, loanTrace.getLong("payAmount"));
			}
		}
	}
	
	public int historyInterest(Integer beforeVip){
		switch (beforeVip) {
			case 1://少尉
				return 300;
			case 2://中尉
				return 300;
			case 3://上尉
				return 300;
			case 4://少校
				return 200;
			case 5://中校
				return 200;
			case 6://上校
				return 100;
			case 7://将军
				return 0;
		}
		return 0;
	}
	
	public void getLoanTransfer(String userCode, LoanInfo loanInfo, LoanTrace loanTrace, int recyLimit, long payAmount) {
		LoanTransfer loanTransfer = LoanTransfer.loanTransferDao.findFirst("SELECT * FROM t_loan_transfer WHERE gotUserCode = ? AND loanCode = ?", userCode, loanInfo.getStr("loanCode"));
		if (loanTransfer == null) {
			return;
		}
		userCode = loanTransfer.getStr("payUserCode");
		HistoryRecy historyRecy = HistoryRecy.historyRecyDao.findFirst("SELECT recyAmount, recyInterest, recyDate FROM t_history_recy WHERE loanCode = ? AND payUserCode = ? AND recyLimit = ? AND payAmount = ?", loanInfo.getStr("loanCode"), loanTransfer.getStr("payUserCode"), recyLimit, payAmount);
		if (historyRecy != null) {
			loanRepaymentService.update4Transfer(loanTrace.getStr("traceCode"), userCode, loanTransfer.getStr("payUserName"), recyLimit);
			long repaymentPrincipal = historyRecy.getLong("recyAmount");
			long repaymentInterest = historyRecy.getLong("recyInterest");
			long interestFee = 0;
			int overdueDays = 0;
			long overdueInterest = 0;
			String repaymentDate = historyRecy.getStr("recyDate");
			
			int benefits4new = loanInfo.getInt("benefits4new");
			int releaseDate = Integer.parseInt(loanInfo.getStr("releaseDate"));
			// 首月加息活动计算利息
			if (releaseDate >= 20171111 && releaseDate <= 20171117) {
				if (recyLimit == 1 && benefits4new == 0) {
					repaymentInterest = CommonUtil.f_004(loanTrace.getLong("payAmount"), loanInfo.getInt("rateByYear") + 400, recyLimit, loanInfo.getStr("refundType"))[1];
				}
			}
			
			loanRepaymentService.repayment(loanTrace.getStr("traceCode"), recyLimit, repaymentPrincipal, repaymentInterest, interestFee, overdueDays, overdueInterest, repaymentDate);
		} else {
			getLoanTransfer(userCode, loanInfo, loanTrace, recyLimit, payAmount);
		}
	}
}
