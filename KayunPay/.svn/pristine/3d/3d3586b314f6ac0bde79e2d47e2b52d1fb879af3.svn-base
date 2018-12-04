package com.dutiantech.controller.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.admin.validator.LoanAuditNoticeValidator;
import com.dutiantech.controller.admin.validator.LoanCreateNoticeValidator;
import com.dutiantech.controller.admin.validator.LoanDeleteLoanValidator;
import com.dutiantech.controller.admin.validator.LoanDeleteNoticeValidator;
import com.dutiantech.controller.admin.validator.LoanPubLoanByAuditValidator;
import com.dutiantech.controller.admin.validator.LoanPubLoanByQuick;
import com.dutiantech.controller.admin.validator.LoanUpdateLoanStateValidator;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.JXTrace;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanNotice;
import com.dutiantech.model.LoanNxjd;
import com.dutiantech.model.LoanNxjd.STATUS;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanRepayment;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.RepaymentCount;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.User;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.HistoryRecyService;
import com.dutiantech.service.JXTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanNoticeService;
import com.dutiantech.service.LoanNxjdService;
import com.dutiantech.service.LoanOverdueService;
import com.dutiantech.service.LoanRepaymentService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.RepaymentCountService;
import com.dutiantech.service.SMSLogService;
import com.dutiantech.service.SettlementEarlyService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.RepaymentCountEnum.batchStatus;
import com.dutiantech.util.RepaymentCountEnum.repaymentCountStatus;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.jxTxCode;
import com.dutiantech.util.SysEnum.loanOverdueType;
import com.dutiantech.util.SysEnum.loanUsedType;
import com.dutiantech.util.SysEnum.refundType;
import com.dutiantech.util.UIDUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import com.jx.service.JXService;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class LoanController extends BaseController {
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	
	private UserService userService = getService(UserService.class);
	
	private LoanNoticeService loanNoticeService = getService(LoanNoticeService.class);
	
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	
	private LoanOverdueService overdueTraceService = getService(LoanOverdueService.class);
	
	private OPUserV2Service opUserService = getService( OPUserV2Service.class ) ;
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private JXTraceService jxTraceService = getService(JXTraceService.class);
	
	private SettlementEarlyService settlementEarlyService = getService(SettlementEarlyService.class);
	private LoanNxjdService loanNxjdService = getService(LoanNxjdService.class);
	private LoanRepaymentService loanRepaymentService = getService(LoanRepaymentService.class);
	private SMSLogService smsLogService = getService(SMSLogService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	private HistoryRecyService historyRecyService = getService(HistoryRecyService.class);
	private RepaymentCountService repaymentCountService = getService(RepaymentCountService.class);
//	private SettlementPlanService settlementPlanService = getService(SettlementPlanService.class);
	
	private static final Logger overdueSetLogger = Logger.getLogger("overdueSetLogger");
	static{
		LoggerUtil.initLogger("overdueSet", overdueSetLogger);
	}
	
	/**
	 * 新增提前还款配置
	 * @return
	 */
	@ActionKey("/addSettlementEarlySettings")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message addSettlementEarlySettings(){
		String loanCode = getPara("loanCode","");
		if(StringUtil.isBlank(loanCode))
			return error("01", "借款标编码不可为空", false);
		
		String earlyDate = getPara("earlyDate","");
		if(StringUtil.isBlank(loanCode))
			return error("02", "提前还款日期不可为空", false);
		
		LoanInfo loan = loanInfoService.findById(loanCode);
		
		String backDate = loan.getStr("backDate");
		int result = DateUtil.compareDateByStr("yyyyMMdd", earlyDate, backDate);
		if(result > 0){
			return error("03", "提前还款日不可大于此借款标的下一还款日期",false);
		}
		
		
		int loanTimeLimit = loan.getInt("loanTimeLimit");
		int earlyLimit = loan.getInt("reciedCount")+1;
		String userCode = loan.getStr("userCode");
		String userTrueName = loan.getStr("userName");
		String userMobile = "";
		String y = userService.findByField(userCode, "userMobile").getStr("userMobile");
		try {
			y = CommonUtil.decryptUserMobile(y);
			if(CommonUtil.isMobile(y)){
				userMobile = y;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(userMobile);
		boolean z = settlementEarlyService.save(loanCode, userCode, userMobile, userTrueName, earlyDate, earlyLimit, loanTimeLimit);
		if(z)
			return succ("提前还款设置成功", true);
		
		return error("110", "操作未生效", false);
	}
	
	@ActionKey("/modifyLoanInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message modifyLoanInfo(){
		//获取发标人信息
		String opCode = getUserCode() ;
		String opName = opUserService.findUserNameById(opCode);
		LoanInfo loan = getModel( LoanInfo.class ) ;
		if(loan.getInt("benefits4new")>0){
			loan.set("minLoanAmount", 10000);
			loan.set("maxLoanAmount", 1000000);
		}
		String loanState = loanInfoService.findStateById(loan.getStr("loanCode"));
		if(loanState.equals(SysEnum.loanState.J.val())){
			return error("01", "招标中不可修改发标资料", "");
		}
		if(loanState.equals(SysEnum.loanState.N.val())){
			return error("02", "还款中不可修改发标资料", "");
		}
		if(loanState.equals(SysEnum.loanState.O.val()) || loanState.equals(SysEnum.loanState.P.val()) || loanState.equals(SysEnum.loanState.Q.val())){
			return error("03", "已完成的贷款标不可修改发标资料", "");
		}
		if(loanState.equals(SysEnum.loanState.T.val())){
			return error("04", "贷款标状态不符合修改发标资料要求", "");
		}
		loan.set("opCode", opCode);
		loan.set("opName", opName);
		loan.remove("userName");
		loan.remove("userCardId");
		loan.set("loanState", SysEnum.loanState.H.val());
		if(loan.getInt("benefits4new")>0){
			loan.set("loanTitle", "100元起投 限额10000元");
			loan.set("loanArea", "新手标");
		}
		loan.update() ;
		return succ("操作完成", true) ;
	}
	
	@ActionKey("/madeLoanByApply")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class} )
	public Message madeLoanByApply(){
		
		int loanApplyNo= getParaToInt("loanApplyNo",0);
		int madeTotal = getParaToInt("total",0);
		
		if( loanApplyNo == 0 ){
			return error("03", "no apply", loanApplyNo ) ;
		}
		
		//获取申请
		LoanApply loanApply = LoanApply.loanApplyDao.findById( loanApplyNo ) ;
		if( loanApply == null )
			return error("09", "无此申请!", null ) ;
		
		if(!loanApply.getStr("applyState").equals(SysEnum.applyState.H.val())){
			return error("119", "此条发标申请的状态不是【风控审核通过】", loanApplyNo);
		}
		
		String applyState = loanApply.getStr("applyState");
		if( "H".equals(applyState) == true ){
			//转换为LoanInfo
			LoanInfo loan = apply2loan(loanApply) ;
			//期限 还款方式判断
			String refundType = loanApply.getStr("refundType");
			long loanAmount = loanApply.getLong("loanAmount");
			int rateByYear = loanApply.getInt("rateByYear") ;
			int loanTimeLimit = loanApply.getInt("loanTimeLimit") ;
			loan.set("isAutoLoan", 1);
			if( madeTotal == 1 ){
				/*不拆分标*/
				loan.set("refundType", refundType) ;
				String uid = UIDUtil.generate();
				loan.set("loanCode", uid ) ;
				try {
					loan.set("loanIndexByUser", MD5Code.md5(uid+loan.getStr("userCode")) ) ;
				} catch (Exception e) {
				}
				loan.set("rateByYear", rateByYear );
				loan.set("loanAmount", loanAmount ) ;
				loan.set("loanBalance", loanAmount ) ;
				loan.set("loanTimeLimit", loanTimeLimit ) ;
				loan.save() ;
				
			}else{
				/*----------------拆分标 开始--------------------*/
				if( "A".equals(refundType) ){
					//等额本息，按月拆分
					madeTotal = loanTimeLimit ;
				}else if( "B".equals( refundType ) ){
					//先息后本，按金额拆分
					if( madeTotal == 0 ){
						madeTotal = loanTimeLimit ;	//默认按还款期限拆分
					}
				}else{
					return error("06", "未知还款类型", refundType ) ;
				}
				loan.set("refundType", "B") ;
				long leftAmount = loanAmount ;	//整数处理
				for(int si = 1 ; si <= madeTotal ; si ++ ){
					long amount = 0 ; 
					int rate = rateByYear;
					int limit = loanTimeLimit;
					if( si == madeTotal ){
						//最后一次
						amount = leftAmount ;
						if( "A".equals(refundType) ){
							limit = si;
							rate = LoanInfo.RATE_MAP.get(si);
						}
					}else{
						if( "A".equals(refundType) ){
							amount = CommonUtil.f_0014loan( loanAmount , loanTimeLimit , rateByYear ,  si)[0];	//本金
							System.out.println("批次：" + si );
							limit = si;
							rate = LoanInfo.RATE_MAP.get(si);
//							loan.set("loanTimeLimit", si ) ;
//							loan.set("rateByYear", LoanInfo.RATE_MAP.get(si));
						}else{
							amount = loanAmount / madeTotal ;
//							loan.set("loanTimeLimit", loanTimeLimit ) ;
//							loan.set("rateByYear", rateByYear);
						}
						long tmpLeftAmount = amount%100 ;
//						leftAmount += tmpLeftAmount ;
						amount = amount - tmpLeftAmount;
						leftAmount = leftAmount - amount ;
					}
					
					loan.set("loanTimeLimit", limit ) ;
					loan.set("rateByYear", rate);				
					
					
					String uid = UIDUtil.generate();
					loan.set("loanCode", uid ) ;
					try {
						loan.set("loanIndexByUser", MD5Code.md5(uid+loan.getStr("userCode")) ) ;
					} catch (Exception e) {
						loan.set("loanIndexByUser", uid ) ;
						e.printStackTrace();
					}
					
					//loan.set("rateByYear", LoanInfo.RATE_MAP.get(si));
					loan.set("loanAmount", amount ) ;
					loan.set("loanBalance", amount ) ;
					loan.save() ;
				}
				/*----------------拆分标 结束--------------------*/
			}
			
		}else{
			return error("02", "未通过审核或已制作新标!" , applyState ) ;
		}
		
		loanApply.set("applyState", SysEnum.applyState.E.val());
		loanApply.set("modifyDate", DateUtil.getNowDate() );
		loanApply.set("modifyDateTime", DateUtil.getNowDateTime() );
		boolean doResult = loanApply.update() ;
		if( doResult == true ){
			return succ("处理完成!", null ) ;
		}else{
			return error("11", "标处理完成，但更新申请状态失败", null ) ;
		}
	}
	
	
	
	private LoanInfo apply2loan(LoanApply apply){
		String nowDate = DateUtil.getNowDate();
		String nowTime = DateUtil.getNowTime();
		OPUserV2 tmp = opUserService.findById(apply.getStr("applyUserCode"));
		OPUserV2 user = getUserInfo();
		LoanInfo loan = new LoanInfo() ;
		String loanArea = " ";
		int loanNo = apply.getInt("loanNo") ;
		if(!StringUtil.isBlank(tmp.getStr("isBranch")) && tmp.getStr("isBranch").equals("y")){
			loanArea = tmp.getStr("branchArea");
		}
		String tmpContractNo = "";
		try {
			tmpContractNo = apply.getStr("contractNo");
			if(StringUtil.isBlank(tmpContractNo))
				tmpContractNo = "";
		} catch (Exception e) {
			tmpContractNo = "";
		}
		loan.set("contractNo", tmpContractNo);
		loan.set("productType", apply.getStr("productType"));
		loan.set("loanArea", loanArea);
		loan.set("loanCode", UIDUtil.generate() ) ;
		loan.set("loanNo", loanNo ) ;
		loan.set("loanTitle", apply.getStr("loanTitle") ) ;
		loan.set("createDate", nowDate );
		loan.set("createTime", nowTime );
		loan.set("updateDate", nowDate );
		loan.set("updateTime", nowTime );
//		loan.set("loanAmount", apply.getLong("loanAmount") ) ;
//		loan.set("loanBalance", apply.getLong("loanAmount") ) ;
		loan.set("maxLoanCount", 999 ) ;
		loan.set("maxLoanAmount", 100000000 ) ;
		loan.set("minLoanAmount", 5000);
		loan.set("userCode", apply.getStr("loanUserCode") ) ;
//		loan.set("loanDesc", apply.getStr("loanDesc") ) ;
		loan.set("loan_pic", apply.getStr("loanPics") ) ;
		loan.set("userName", apply.getStr("loanTrueName") ) ;
		try {
			loan.set("userCardId", CommonUtil.encryptUserCardId( apply.getStr("loanCardId") )) ;
		} catch (Exception e) {
			loan.set("userCardId", apply.getStr("loanCardId") ) ;
		}
		//loan desc
		String loanDesc = apply.getStr("loanDesc") ;
		StringBuffer buff = new StringBuffer("");
		try{
			JSONArray array = JSONArray.parseArray(loanDesc) ;
			int len = array.size() ;
			String[] numConv =  new String[]{"一","二","三","四","五","六","七","八","九","十"} ;
			for(int i = 0 ; i < len ; i ++){
				JSONObject json = array.getJSONObject(i) ;
				String index = numConv[ i];
				buff.append("\n<t>" +index +"、" + json.getString("title") + "：</t>\n") ;
				buff.append("  " + json.getString("content") +"\n");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		loan.set("loanDesc", buff.toString() ) ;
		loan.set("loanState", SysEnum.loanState.H.val() ) ;	//审核发标材料
		loan.set("opCode", user.getStr("op_code") ) ;
		loan.set("opName", user.getStr("op_name") ) ;
		loan.set("refundType", apply.getStr("refundType") ) ;
		loan.set("rateByYear", apply.getInt("rateByYear") ) ;
		String loanType = apply.getStr("loanType");
		if( SysEnum.loanType.D.val().equals( loanType ) ){
			//流转标，补全债权人信息
			loan.set("loanType", SysEnum.loanType.D.val() );
			loan.set("loanTypeDesc", SysEnum.loanType.D.desc());
			String applyCode = apply.getStr("applyUserCode");
			OPUserV2 opUser = OPUserV2.OPUserV2Dao.findById(applyCode);
			loan.set("creditorName", opUser.getStr("creditorName"));
			loan.set("creditorCardId", opUser.getStr("creditorCardId") );
		}else{
			loan.set("loanType", loanType );
			loan.set("loanTypeDesc", SysEnum.loanType.valueOf( loanType ).desc() );
			loan.set("creditorName", "非债权标");
			loan.set("creditorCardId", "1" );
		}
		
		return loan ;
	}
	
	/**
	 * 更改标书状态
	 */
	@ActionKey("/updateLoanState")
	@AuthNum(value=999)
	@Before({LoanUpdateLoanStateValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateLoanState(){
		//拦截器验证不可为空-------begin
		String loanCode = getPara("loanCode");
		String loanState = getPara("loanState");
		String oLoanState = getPara("oLoanState");
		//拦截器验证不可为空-------end
		LoanInfo loan = loanInfoService.findById(loanCode);
		if(!userService.validateUserState(loan.getStr("userCode"))){
			return error("02", "借款人用户状态不是【正常】", null);
		}
		if(loanInfoService.updateLoanState(loanCode, loanState,oLoanState))
			return succ("更新标书状态操作完成", true);
		else
			return error("01", "更新标书状态操作未生效", false);
	}
	
	/**
	 * 审核发标
	 * @return
	 */
	@ActionKey("/pubLoanByAudit")
	@AuthNum(value=999)
	@Before({LoanPubLoanByAuditValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message pubLoanByAudit(){
		String loanCodes = getPara("loanCode");
		String[] loanCodeArray = loanCodes.split(",");
		String userCode = "";
		LoanInfo loan = null;
		String failLoanNo ="";
		String successLoanNo ="";
		String retMsg ="";
		String message ="";
		List<String> successCodeList=new ArrayList<String>();
		Map<String, String> reqMap = null;
		for (int i = 0; i < loanCodeArray.length; i++) {
			String loanCode = loanCodeArray[i];
			String pubDate = getPara("pubDate");
			String pubTime = getPara("pubTime");
			loan = loanInfoService.findById(loanCode);
		    userCode = loan.getStr("userCode");
			if (!userService.validateUserState(loan.getStr("userCode"))) {
				return error("03","借款人用户[" + loan.getStr("userName") + "]状态不是【正常】", null);
			}
			if (!loan.get("loanState").equals("H")) {
				return error("02", loan.getStr("loanNo") + loan.getStr("loanTitle")+ "-必须为待审核的标,才可以进行立即发标", false);
			}
			if (!loanInfoService.isFlow(loanCode)) {
				return error("04",loan.getStr("loanNo") + loan.getStr("loanTitle")+ "-该借款标是流转标,请检查债权人信息是否完善", null);
			}
			if (StringUtil.isBlank(loan.getStr("loanArea"))) {
				return error("05", loan.getStr("loanNo") + loan.getStr("loanTitle")+ "-请检查标的来源(地区/机构)，不可为空", null);
			}
			if (StringUtil.isBlank(userCode)) {
				return error("06", "借款人账户信息异常", null);
			}
			if (loanInfoService.pubLoan(loanCode, pubDate, pubTime)) {
				successCodeList.add(loanCode);
			}
		}
		
		if(null!=successCodeList && successCodeList.size()>0){
			for (int i = 0; i < successCodeList.size(); i++) {
				loan = loanInfoService.findById(successCodeList.get(i));
				userCode=loan.getStr("userCode");
				User user = userService.findById(userCode);
				String productId = loan.getStr("loanCode");
				String raiseDate = loan.getStr("releaseDate");
				String raiseEndDate = DateUtil.addDay(raiseDate, 60);
				String intType = "2";
				String duration = String.valueOf(loan.getInt("loanTimeLimit") * 30);
				String txAmount = StringUtil.getMoneyYuan((loan.getLong("loanAmount")));
				String rate = StringUtil.getMoneyYuan((loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")));
				String bailAccountId = JXService.RISK_RESERVE;
				// rain 20180419
				try {
					if(!"E".equals(loan.getStr("productType"))){
						reqMap = JXController.debtRegister(
								user.getStr("jxAccountId"), productId, raiseDate,
								raiseEndDate, intType, "", duration, txAmount, rate,
								bailAccountId);
					}else{
						reqMap = new HashMap<String,String>();
						reqMap.put("retCode", "00000000");
					}
					if(null==reqMap){
						loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
					}
					else if (!JXController.isRespSuc(reqMap)) {
						//更改loaninfo的信息
						loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
						failLoanNo+= loan.getStr("loanNo")+",";
						retMsg+= reqMap.get("retMsg")+",";
					}
					else{
						successLoanNo+= loan.getStr("loanNo")+",";
					}
					
				} catch (Exception e) {
					//更改loaninfo的信息
					loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
				}
			
			}
			if(!StringUtil.isBlank(successLoanNo)){
				message+= "标号"+successLoanNo+"发标成功---";
			}
			if(!StringUtil.isBlank(failLoanNo)){
				message+= "标号"+failLoanNo+"发标失败;原因:"+retMsg;	
			}
		}
		return succ("定时发标操作",message);
	}
	
	/**
	 * 快速发标
	 * @return
	 */
	@ActionKey("/pubLoanByQuick")
	@AuthNum(value=999)
	@Before({LoanPubLoanByQuick.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message pubLoanByQuick(){
		String loanCodes = getPara("loanCode");
		String[] loanCodeArray = loanCodes.split(",");
		LoanInfo loan = null;
		Map<String, String> reqMap = null;
		String userCode = "";
		String failLoanNo ="";
		String successLoanNo ="";
		String retMsg ="";
		String message ="";
		List<String> successCodeList=new ArrayList<String>();
		for (int i = 0; i < loanCodeArray.length; i++) {
			String pubDate = DateUtil.getNowDate();
			String pubTime = DateUtil.getNowTime();
			String loanCode = loanCodeArray[i];
			loan = loanInfoService.findById(loanCode);
			userCode = loan.getStr("userCode");
		if (!userService.validateUserState(loan.getStr("userCode"))) {
			return error("03",
					"借款人用户[" + loan.getStr("userName") + "]状态不是【正常】", null);
		}
		if (!loan.get("loanState").equals("H")) {
			return error("02", loan.getStr("loanNo") + loan.getStr("loanTitle")
					+ "-必须为待审核的标,才可以进行立即发标", false);
		}
		if (!loanInfoService.isFlow(loanCode)) {
			return error("04", loan.getStr("loanNo") + loan.getStr("loanTitle")
					+ "-该借款标是流转标,请检查债权人信息是否完善", null);
		}
		if (StringUtil.isBlank(loan.getStr("loanArea"))) {
			return error("05", loan.getStr("loanNo") + loan.getStr("loanTitle")
					+ "-请检查标的来源(地区/机构)，不可为空", null);
		}
		if (StringUtil.isBlank(userCode)) {
			return error("06", "借款人账户信息异常", null);
		}
		if (loanInfoService.pubLoan(loanCode, pubDate, pubTime)) {
			successCodeList.add(loanCode);
		}
		}
		if(null!=successCodeList && successCodeList.size()>0){
		for (int i = 0; i < successCodeList.size(); i++) {
			loan = loanInfoService.findById(successCodeList.get(i));
			userCode=loan.getStr("userCode");
			User user = userService.findById(userCode);
			String productId = loan.getStr("loanCode");
			String raiseDate = loan.getStr("releaseDate");
			String raiseEndDate = DateUtil.addDay(raiseDate, 60);
			String intType = "2";
			String duration = String.valueOf(loan.getInt("loanTimeLimit") * 30);
			String txAmount = StringUtil.getMoneyYuan((loan.getLong("loanAmount")));
			String rate = StringUtil.getMoneyYuan((loan.getInt("rateByYear") + loan.getInt("rewardRateByYear")));
			String bailAccountId = JXService.RISK_RESERVE;
			// rain 20180419
			try {
				
				if(!"E".equals(loan.getStr("productType"))){
					reqMap = JXController.debtRegister(
							user.getStr("jxAccountId"), productId, raiseDate,
							raiseEndDate, intType, "", duration, txAmount, rate,
							bailAccountId);
				}else{
					reqMap = new HashMap<String,String>();
					reqMap.put("retCode", "00000000");
				}
				
				if(null==reqMap ){
					loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
				}
				else if (!JXController.isRespSuc(reqMap)) {
					//更改loaninfo的信息
					loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
					failLoanNo+= loan.getStr("loanNo")+",";
					retMsg+= reqMap.get("retMsg")+",";
				}
				else{
					successLoanNo+= loan.getStr("loanNo")+",";
				}
				
			} catch (Exception e) {
				//更改loaninfo的信息
				loanInfoService.updateLoanState4pubLoan(productId,SysEnum.loanState.H.val(), SysEnum.loanState.J.val());
			}
		}
		if(!StringUtil.isBlank(successLoanNo)){
			message+= "标号"+successLoanNo+"发标成功---";
		}
		if(!StringUtil.isBlank(failLoanNo)){
			message+= "标号"+failLoanNo+"发标失败;原因:"+retMsg;	
		}
		}
		return succ("立即发标操作",message);
	}
	
	/**
	 * 删除贷款标
	 * @return
	 */
	@ActionKey("/deleteLoanById")
	@AuthNum(value=999)
	@Before({LoanDeleteLoanValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message deleteById(){
		String loanCode = getPara("loanCode");
		long x = loanTraceService.countTraces(loanCode);
		if(x>0){
			return error("02", "该借款标已有投标记录，不可删除，请尝试【流标】操作", null);
		}
//		LoanInfo loan = loanInfoService.findById(loanCode);
//		if(!userService.validateUserState(loan.getStr("userCode"))){
//			return error("02", "借款人用户状态不是【正常】", null);
//		}
		if(loanInfoService.deleteById(loanCode)){
			return succ("操作完成", true);
		}
		return error("01", "借款标当前流程状态无法删除", false);
	}
	
	/**
	 * 根据id查询单个标书明细
	 */
	@ActionKey("/getLoanInfoById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanInfoById(){
		String loanCode = getPara("loanCode","");//拦截器验证不可为空
		
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		String x = loanInfo.get("userCardId");
		try {
			String y = CommonUtil.decryptUserCardId(x);
			loanInfo.put("userCardId", y);
		} catch (Exception e) {
			return error("02", "查询用户身份证信息解密时发生错误", false);
		}
		return succ("查询单个标书信息明细完成", loanInfo);
	}

	/**
	 * 查询满标待审信息 WJW
	 * @return
	 */
	@ActionKey("/getFullLoanInfoList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFullLoanInfoList(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		pageSize = pageSize > 10 ? 10 : pageSize;
		Page<LoanInfo> page = loanInfoService.findByPage4full(pageNumber, pageSize);
		List<LoanInfo> loanInfos = page.getList();
		JSONArray jsonArray = new JSONArray();
		for (LoanInfo loanInfo : loanInfos) {
			String loanCode = loanInfo.getStr("loanCode");
			List<JXTrace> jxTraces = jxTraceService.queryBatchLendPayByLoanCode(loanCode);
			if(jxTraces != null && jxTraces.size() > 0 ){
				JXTrace jxTrace = jxTraces.get(jxTraces.size() - 1);//最后一个批次放款记录
				String jxTraceCode = jxTrace.getStr("jxTraceCode");
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject jsonObject = JSONObject.parseObject(requestMessage);
				String batchNo = jsonObject.getString("batchNo");
				String txDate = jsonObject.getString("txDate");
				String txTime = jsonObject.getString("txTime");
				int jxTraceState = jxTraceService.jxTraceState(jxTraceCode);
				JSONObject jsonObject2 = new JSONObject();
				jsonObject2.put("batchNo", batchNo);
				jsonObject2.put("txDate", DateUtil.getStrFromDate(DateUtil.getDateFromString(txDate+txTime,"yyyyMMddHHmmss"), "yyyy-MM-dd HH:mm:ss"));
				if(jxTraceState == 1 || jxTraceState == 2 || jxTraceState == 3){
					jsonObject2.put("batchState", "处理中");
				}else if(jxTraceState == 4 || jxTraceState == 7){
					jsonObject2.put("batchState", "处理失败");
				}else if (jxTraceState == 5) {
					jsonObject2.put("batchState", "部分失败");
				}else if(jxTraceState == 6){
					jsonObject2.put("batchState", "处理成功");
				}else {
					jsonObject2.put("batchState", "查询失败");
				}
				
				jsonObject2.put("loanCode", loanCode);
				jsonObject2.put("loanNo", loanInfo.getStr("loanNo"));
				jsonObject2.put("loanTitle", loanInfo.getStr("loanTitle"));
				jsonObject2.put("loanAmount", loanInfo.getLong("loanAmount"));
				jsonObject2.put("rateByYear", loanInfo.getInt("rateByYear"));
				jsonObject2.put("rewardRateByYear", loanInfo.getInt("rewardRateByYear"));
				jsonObject2.put("benefits4new", loanInfo.getInt("benefits4new"));
				jsonObject2.put("refundType", loanInfo.getStr("refundType"));
				jsonObject2.put("loanStateDesc", loanInfo.getStr("loanStateDesc"));
				jsonObject2.put("loanArea", loanInfo.getStr("loanArea"));
				jsonObject2.put("userName", loanInfo.getStr("userName"));
				jsonArray.add(jsonObject2);
				
			}
		}
		return succ("ok", new Page<>(jsonArray, pageNumber, pageSize, page.getTotalPage(), page.getTotalRow())) ;
	}
	
	/**
	 * 分页查询标书
	 */
	@ActionKey("/getLoanInfoList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanInfoList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		String endDate = getPara("endDate","");
		
		String rBeginDateTime = getPara("rBeginDateTime","");
		
		String loanTimeLimit = getPara("loanLimit", "");
		
		if(StringUtil.isBlank(rBeginDateTime) || rBeginDateTime.length()!=15 || rBeginDateTime.indexOf("-")!=8){
			rBeginDateTime = null;
		}
		
		String rEndDateTime = getPara("rEndDateTime","");
		
		if(StringUtil.isBlank(rEndDateTime) || rEndDateTime.length()!=15 || rEndDateTime.indexOf("-")!=8){
			rEndDateTime = null;
		}
		
		String loanState = getPara("loanState","");
		
		String loanType = getPara("loanType","");
		
		String allkey = getPara("allkey", "") ;
		
		Map<String,Object> result = loanInfoService.findByPage4Noob3(null,pageNumber, pageSize, beginDate, endDate,rBeginDateTime,rEndDateTime, loanState,loanType, loanTimeLimit, allkey);
		Map<String,Object> result_sum = loanInfoService.findByPage4Noob3WithSum(null,pageNumber, pageSize, beginDate, endDate,rBeginDateTime,rEndDateTime, loanState,loanType, loanTimeLimit, allkey);
		result.putAll( result_sum);
		return succ("分页查询标书完成", result);
	}
	
	/**
	 * 分页查询标书
	 */
	@ActionKey("/getLoanInfoList4Recy")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanInfoList4Recy(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		String endDate = getPara("endDate","");
		
		String allkey = getPara("allkey", "") ;
		
		String userCode = getPara("fuserCode","");
		
		String backDate = getPara("backDate","");
		
		String loanState = getPara("loanState", "");
		if(StringUtil.isBlank(loanState)){
			loanState = "N,O,P,Q";
		}
		
		if(backDate.equals("y")){
			backDate = DateUtil.getNowDate();
			loanState = "N";
		}
		
		Map<String,Object> result = loanInfoService.findByPage4Noob(userCode,pageNumber, pageSize, beginDate, endDate, loanState,allkey,backDate);
//		Map<String,Object> result_sum = loanInfoService.findByPage4NoobWithSum(userCode,pageNumber, pageSize, beginDate, endDate, loanState,allkey,backDate);
//		result.putAll(result_sum);
		if(StringUtil.isBlank(allkey)){
			Map<String,Object> result_sum2 = loanInfoService.findByPage4NoobWithSum2(userCode,pageNumber, pageSize, beginDate, endDate, loanState,allkey,backDate);
			result.putAll(result_sum2);
		}
		if((int)result.get("totalRow") == 0){
			long yhbj = fundsServiceV2.sumAdmountByDate(DateUtil.getNowDate());
			long yhlx = fundsServiceV2.sumInterestByDate(DateUtil.getNowDate());
			result.put("jryhbj", yhbj);
			result.put("jryhlx", yhlx);
		}
		return succ("分页查询标书完成", result);
	}
	
	
	/**
	 * 查询今日还款的标
	 */
	@ActionKey("/getTodayJieSuanList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getTodayLoanList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String allkey = getPara("allkey", "");
		
		Page<LoanInfo> pageLoanInfo = loanInfoService.findByTodayJieSuan(null,pageNumber, pageSize,allkey);
		
		if(pageNumber > pageLoanInfo.getTotalPage() && pageLoanInfo.getTotalPage() > 0){
			pageNumber = pageLoanInfo.getTotalPage();
			pageLoanInfo = loanInfoService.findByTodayJieSuan(null,pageNumber, pageSize,allkey);
		}
		return succ("分页查询标书完成", pageLoanInfo);
	}
	
	/**
	 * 根据ID查询一条标书流水明细
	 */
	@ActionKey("/getLoanTraceById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanTraceById(){
		
		String traceCode = getPara("traceCode","");
		
		LoanTrace loanTrace = loanTraceService.findById(traceCode);
		
		return succ("查询单条标书流水明细完成", loanTrace);
		
	}
	
	/**
	 * 分页查询用户的投标流水
	 */
	@ActionKey("/getPayUserLoanTraceList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getPayUserLoanTraceList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDateTime = getPara("beginDate","");
		
		beginDateTime = StringUtil.isBlank(beginDateTime)?"":beginDateTime+"000000";
		
		String endDateTime = getPara("endDate","");
		
		endDateTime = StringUtil.isBlank(endDateTime)?"":endDateTime+"235959";
		
		String payUserCode = getPara("payUserCode","");
		
		Map<String,Object> result = loanTraceService.findByPageJoinUser4Noob(pageNumber, pageSize, beginDateTime, endDateTime, payUserCode);
		
		Map<String,Object> result_sum = loanTraceService.findByPageJoinUser4NoobWithSum(pageNumber, pageSize, beginDateTime, endDateTime, payUserCode);
		
		result.putAll(result_sum);
		
		return succ("分页查询标书流水完成", result);
	}
	
	/**
	 * 分页查询投标流水
	 */
	@ActionKey("/getLoanTraceList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanTraceList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDateTime = getPara("beginDate","");
		
		beginDateTime = StringUtil.isBlank(beginDateTime)?"":beginDateTime+"000000";
		
		String endDateTime = getPara("endDate","");
		
		endDateTime = StringUtil.isBlank(endDateTime)?"":endDateTime+"235959";
		
		String loanUserCode = getPara("loanUserCode","");
		
		String payUserCode = getPara("payUserCode","");
		
		String loanCode = getPara("loanCode","");
		
		String allkey = getPara("allkey", "");
		
		Page<LoanTrace> pageLoanTrace = loanTraceService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime, loanUserCode, payUserCode, loanCode,allkey);
		if(pageNumber>pageLoanTrace.getTotalPage() && pageLoanTrace.getTotalPage()>0){
			pageNumber = pageLoanTrace.getTotalPage();
			pageLoanTrace = loanTraceService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime, loanUserCode, payUserCode, loanCode,allkey);
		}
		
		return succ("分页查询标书流水完成", pageLoanTrace);
	}
	
	/**
	 * 分页查询发标公告
	 */
	@ActionKey("/getLoanNoticeList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanNoticeList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDateTime = getPara("beginDate","");
		
		beginDateTime = StringUtil.isBlank(beginDateTime)?"":beginDateTime+"000000";
		
		String endDateTime = getPara("endDate","");
		
		endDateTime = StringUtil.isBlank(endDateTime)?"":endDateTime+"000000";
		
		String state = getPara("state","");
		
		String allkey = getPara("allkey","");
		
		Page<LoanNotice> pageLoanNotice = loanNoticeService.findByPage2(pageNumber, pageSize, beginDateTime, endDateTime,state,allkey);
		if(pageNumber>pageLoanNotice.getTotalPage() && pageLoanNotice.getTotalPage()>0){
			pageNumber = pageLoanNotice.getTotalPage();
			pageLoanNotice = loanNoticeService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime,state,allkey);
		}
		return succ("分页查询发标公告完成", pageLoanNotice);
	}
	
	@ActionKey("/getLoanNoticeById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanNoticeById(){
		String noticeCode = getPara("noticeCode","");
		LoanNotice loanNotice = loanNoticeService.findById(noticeCode);
		return succ("查询单个发标公告明细完成", loanNotice);
	}
	
	@ActionKey("/createLoanNotice")
	@AuthNum(value=999)
	@Before({LoanCreateNoticeValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message createLoanNotice(){
		String content = getPara("content");
		String overDateTime = getPara("overDateTime");
		
		if(loanNoticeService.save(content, overDateTime)){
			return succ("创建发标公告完成", true);
		}
		return error("01", "创建发标公告失败", false);
	}
	
	@ActionKey("/auditLoanNotice")
	@AuthNum(value=999)
	@Before({LoanAuditNoticeValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message auditLoanNotice(){
		String noticeCode = getPara("noticeCode");
		String state = getPara("state","2");
		
		if(loanNoticeService.audit(noticeCode, state)){
			return succ("审核发标公告完成", true);
		}
		return error("01", "审核发标公告失败", false);
	}
	
	@ActionKey("/deleteLoanNoticeById")
	@AuthNum(value=999)
	@Before({LoanDeleteNoticeValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message deleteLoanNoticeById(){
		String noticeCode = getPara("noticeCode");
		
		if(loanNoticeService.deleteById(noticeCode)){
			return succ("删除发标公告完成", true);
		}
		return error("01", "删除发标公告失败", false);
	}

	/**
	 * 分页查询债权转让记录
	 * @return
	 */
	@ActionKey("/getLoanTransferList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanTransferList(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDateTime = getPara("beginDate","");
		
		String endDateTime = getPara("endDate","");
		
		String transState = getPara("transState","");
		
		String allkey = getPara("allkey","");
		
		Page<LoanTransfer> pageLoanTransfer = loanTransferService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime,transState,allkey);
		if(pageNumber>pageLoanTransfer.getTotalPage() && pageLoanTransfer.getTotalPage()>0){
			pageNumber = pageLoanTransfer.getTotalPage();
			pageLoanTransfer = loanTransferService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime,transState,allkey);
		}
		return succ("分页查询债权转让记录完成", pageLoanTransfer);
	}
	
	/**
	 * 添加逾期记录
	 * @return
	 */
	@ActionKey("/addOverdueLoan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addOverdueLoan(){
		String loanNo = getPara("loanNo","");
		String remark = getPara("remark","");
		long overdueAmount = getParaToLong("overdueAmount", 0L);
		String overdueDate = getPara("overdueDate","");
		
		if(StringUtil.isBlank(overdueDate)){
			return error("1", "逾期时间不能为空!", null );
		}
		
		overdueDate = overdueDate.replace("-", "");
		
		if(StringUtil.isBlank(loanNo)){
			return error("1", "借款编号不能为空!", null );
		}
//		int overdueLimit = 0;
//		try {
//			overdueLimit = getParaToInt("overdueLimit");
//		} catch (Exception e) {
//			return error("1", "逾期期数!", null );
//		}
		
		/**
		 * 获取标列表
		 */
		List<LoanInfo> loanInfoList = loanInfoService.findByLoanNo(loanNo);
		if(null == loanInfoList || loanInfoList.size() <= 0){
			return error("2", "未查询到该标信息!", null );
		}
		//循环插入逾期表
		long loanAmount = 0;
		for (int i = 0; i < loanInfoList.size(); i++) {
			loanAmount += loanInfoList.get(i).getLong("loanAmount");
		}
		
		int limit = loanInfoList.get(0).get("loanTimeLimit");
		
//		if(limit < overdueLimit){
//			return error("3", "逾期期数错误!", null );
//		}
		
//		int rate = loanInfoList.get(0).getInt("rateByYear") + loanInfoList.get(0).getInt("rewardRateByYear");
		
		String rt = loanInfoList.get(0).get("refundType");
		
//		String overdueDate = CommonUtil.anyRepaymentDate4string(loanInfoList.get(0).getStr("effectDate"), overdueLimit);
		
		if(Integer.parseInt(DateUtil.getNowDate()) < Integer.parseInt(overdueDate)){
			return error("3", "该标还没有逾期呢!", null );
		}
		
		//计算逾期金额
//		long overdueAmount = 0;
//		long[] amount = CommonUtil.f_000(loanAmount, limit, rate, overdueLimit, rt);
//		overdueAmount = amount[0] + amount[1];
		
		Map<String, Object> para = new HashMap<String, Object>();
		para.put("loanCode", loanInfoList.get(0).get("loanCode"));
		para.put("loanNo", loanNo);
		para.put("loanUserCode", loanInfoList.get(0).get("userCode"));
		para.put("loanUserName", loanInfoList.get(0).get("userName"));
		para.put("loanAmount", loanAmount);
		para.put("loanTitle", loanInfoList.get(0).get("loanTitle"));
		para.put("loanTimeLimit", limit);
		para.put("repayIndex", loanInfoList.get(0).get("reciedCount"));
		para.put("principal", 0);
		para.put("interest", 0);
		para.put("overdueAmount", overdueAmount);
		para.put("refundType", rt);
		para.put("overdueDate", overdueDate);
		para.put("overdueTime", "000000");
		para.put("remark", remark);
		boolean b = overdueTraceService.save(para);
		if(b == false){
			return error("4", "添加失败!", null );
		}
		return succ("添加成功", "");
	}
	
	/**
	 * 根据ID查询单个债权转让记录的明细
	 * @return
	 */
	@ActionKey("/getLoanTransferById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanTransferById(){
		String transCode = getPara("transCode","");
		LoanTransfer loanTransfer = loanTransferService.findById(transCode);
		return succ("查询单个债权转让明细完成", loanTransfer);
	}
	
	
	/**
	 * 分页查询逾期记录
	 * @return
	 */
	@ActionKey("/getLoanOverdueList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanOverdueList(){
		
		Integer pageNumber = getPageNumber();
		
		Integer pageSize = getPageSize() ;
		
		String beginDateTime = getPara("beginDate","");
		
		String endDateTime = getPara("endDate","");
		
		String disposeStatus = getPara("disposeStatus","");
		
		String allkey = getPara("allkey","");
		
		Page<LoanOverdue> pageOverdueTrace = overdueTraceService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime,disposeStatus,allkey);
//		if(pageNumber>pageOverdueTrace.getTotalPage() && pageOverdueTrace.getTotalPage()>0){
//			pageNumber = pageOverdueTrace.getTotalPage();
//			pageOverdueTrace = overdueTraceService.findByPage(pageNumber, pageSize, beginDateTime, endDateTime,disposeStatus,allkey);
//		}
		return succ("分页查询逾期记录完成", pageOverdueTrace);
	}
	
	
	/**
	 * 修改逾期记录
	 * @return
	 */
	@ActionKey("/disposeOverdue")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message disposeOverdue(){
		String overdueCode = getPara("overdueCode");
		String disposeStatus = getPara("disposeStatus");
		String remark = getPara("remark");
		if(StringUtil.isBlank(overdueCode)){
			return error("01", "逾期标识为空!", "");
		}
		if(StringUtil.isBlank(disposeStatus)){
			return error("01", "处理状态不能为空!", "");
		}
		
		boolean b = overdueTraceService.disposeOverdue(overdueCode,disposeStatus,remark);
		if(b == false){
			return error("02", "操作异常!", "");
		}
		return succ("操作成功", "");
	}
	
	/**
	 * 根据ID查询单个逾期记录明细
	 * @return
	 */
	@ActionKey("/getLoanOverdueById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanOverdueById(){
		String overdueCode = getPara("overdueCode","");
		LoanOverdue overdueTrace = overdueTraceService.findById(overdueCode);
		return succ("查询单个逾期记录明细完成", overdueTrace);
	}
	
	/**
	 * 删除逾期记录
	 * @return
	 */
	@ActionKey("/deleteLoanOverdue")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message deleteLoanOverdue(){
		String overdueCode = getPara("overdueCode","");
		if(StringUtil.isBlank(overdueCode)){
			return error("01", "参数错误", "");
		}
		boolean b = overdueTraceService.deleteOverdue(overdueCode);
		if(b == false){
			return error("02", "删除失败", "");
		}
		return succ("删除成功", "");
	}
	
	@ActionKey("/yfq/pubLoan4Nxjd")
	@Before({PkMsgInterceptor.class})
	public void pubLoan4Nxjd() {
		// 验证来源
		String[] allowIp = {"118.25.53.62"};
		System.out.println(getRequestIP());
		if (!Arrays.asList(allowIp).contains(getRequestIP())) {
			renderJson(error4json("ERA1", "渣渣，你没资格请求", ""));
			return;
		}

			// TODO 验证密匙
			// 获取参数
//			String traceCodeYfq = (String)getRequest().getAttribute("traceCodeForYfq");//获取请求接口的traceCode
//			String params = (String)getRequest().getAttribute("params");//获取请求接口的参数
			String params = getRequestString4stream();
		if(StringUtil.isBlank(params)){
			renderJson(error4json("UGR1", "请求未收到", ""));
			return;
		}
		JSONObject jsonObject = JSONObject.parseObject(params);
		String amount = jsonObject.getString("loanAmount");
		long loanAmount = StringUtil.getMoneyCent(amount);
		String fee = jsonObject.getString("txFee");
		int txFee = (int)StringUtil.getMoneyCent(fee);
		if(txFee != 100000){
			renderJson(error4json("TFER", "手续费金额错误", ""));
			return;
		}
//		String loanCode = jsonObject.getString("loanCode");
		String loanNumber = jsonObject.getString("loanNumber");
//		int term = jsonObject.getIntValue("term");
		int loanTimeLimit = jsonObject.getIntValue("loanTimeLimit");
		String loanUserName = jsonObject.getString("userName");
		String loanUserCardId = jsonObject.getString("userCardId");
		String userMobile = jsonObject.getString("mobile");
//		try {
//			userMobile = CommonUtil.encryptUserMobile(userMobile);
//		} catch (Exception e) {
//			renderResult("PNE1", "手机号错误", loanNumber);
//			return;
//		}
		YiStageUserInfo yfqUserInfo = yiStageUserInfoService.findYiStageUserInfo(userMobile);
		if(null == yfqUserInfo){
			renderJson(error4json("UFU1", "未找到此用户", loanNumber));
			return;
		}
		String userCode = yfqUserInfo.getStr("userCode");
		User user = userService.findById(userCode);
		//获取用户存管电子帐号
		String jxAccountId = user.getStr("jxAccountId");
		//验证是否开通存管
		if(!JXController.isJxAccount(user)){
			renderJson(error4json("NOJA", "用户未开通账户", loanNumber));
			return;
		}
		//验证是否开通授权
		Map<String,String> authDetail = JXQueryController.termsAuthQuery(jxAccountId);
		if ("1".equals(authDetail.get("paymentAuth"))) {	// 已开通缴费授权，验证是否已过期
			String paymentDeadline = authDetail.get("paymentDeadline");
			int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline, DateUtil.getNowDate());
			if (x < 0) {
				renderJson(error4json("0001", "缴费授权已过期", loanNumber));
				return;
			}
		} else {
			renderJson(error4json("0002", "还未开通缴费授权", loanNumber));
			return;
		}
		//验证借款金额是否超限20W
		long loanUserAmount = loanInfoService.sumLoanUserAmount(userCode);
		if(loanUserAmount>20000000){
			renderJson(error4json("0003", "借款金额已超限", loanNumber));
			return;
		}
		//获取用户基本信息
		String sex = yfqUserInfo.getStr("sex");//性别
		int age = yfqUserInfo.getInt("age");//年龄
		String address = yfqUserInfo.getStr("address");//住址
		String education = yfqUserInfo.getStr("education");//学历
		String workCity = yfqUserInfo.getStr("workCity");//工作城市
		String workType = yfqUserInfo.getStr("workType");//工作类型
		String loanPurpose = jsonObject.getString("loanPurpose");//借款用途;
		JSONObject userDesc = new JSONObject();
		userDesc.put("loanUserSex", sex);
		userDesc.put("loanUserAge", age);
		userDesc.put("loanUserAddress", address);
		userDesc.put("loanUserEducation", education);
		userDesc.put("loanUserWorkCity", workCity);
		userDesc.put("loanUserWorkType", workType);
		userDesc.put("loanUserPurpose", loanPurpose);
		String userInfoDesc = userDesc.toJSONString();
		//计算利息
		int rateByYear = 1000;
		switch (loanTimeLimit) {
		case 1:
			rateByYear+=200;
			break;
		case 2:
			rateByYear+=200;
			break;
		case 3:
			rateByYear+=200;
			break;
		default:
			renderJson(error4json("0010", "期限错误", loanNumber));
			return;
		}
		int rewardRateByYear = 0;
		long loanNo = 900000 +loanNxjdService.sumLoanNxjdNum()+1;
		
		// 验证参数
		if (StringUtil.isBlank(loanNumber)) {
			renderJson(error4json("0014", "请填写正确的标编号", loanNumber));
			return;
		}
		if (StringUtil.isBlank(String.valueOf(loanAmount)) || !StringUtil.isNumeric(String.valueOf(loanAmount))) {
			renderJson(error4json("0011", "请填写正确的借款金额", loanNumber));
			return;
		}
		if (StringUtil.isBlank(String.valueOf(rateByYear)) || !StringUtil.isNumeric(String.valueOf(rateByYear))) {
			renderJson(error4json("0012", "请填写正确的年化利率", loanNumber));
			return;
		}
		if (StringUtil.isBlank(String.valueOf(loanTimeLimit)) || !StringUtil.isNumeric(String.valueOf(loanTimeLimit))) {
			renderJson(error4json("0013", "请填写正确的借款期限", loanNumber));
			return;
		}
//		if (StringUtil.isBlank(String.valueOf(term))) {
//			return error("01", "请填写正确的借款天数", "");
//		}
		if (StringUtil.isBlank(loanUserName)) {
			renderJson(error4json("0015", "请填写正确的借款人姓名", loanNumber));
			return;
		}
		if (StringUtil.isBlank(loanUserCardId)) {
			renderJson(error4json("0016", "请填写正确的借款人身份证号", loanNumber));
			return;
		}
		if (StringUtil.isBlank(String.valueOf(rewardRateByYear))) {
			rewardRateByYear = 0;
		}
		
		// 生成易分期标
		LoanInfo loanInfo = null;
		LoanNxjd loanNxjd = null;
		Map<String, String> condition = new HashMap<String, String>();
		condition.put("ztyLoanCode", loanNumber);
		List<LoanNxjd> loanNxjds = loanNxjdService.findByCondition(condition);
		if (loanNxjds.size() > 0) {
			for(int i = 0 ;i<loanNxjds.size();i++){
				LoanNxjd nxjd = loanNxjds.get(i);
				String loanCode = nxjd.getStr("loanCode");
				LoanInfo tmpLoanInfo = loanInfoService.findById(loanCode);
				if(tmpLoanInfo != null){
					renderJson(error4json("0020", "借款标已发布", loanNumber));
					return;
				}
			}
			renderJson(error4json("0021", "借款标等待数据处理中", loanNumber));
			return;
		}
//		if (StringUtil.isBlank(loanCode)) {	// 无loanCode 生成新标
			String loanCode = UIDUtil.generate();
			loanInfo = new LoanInfo();
			loanInfo.set("loanCode", loanCode);
			loanInfo.set("loanNo", loanNo);
			loanInfo.set("contractNo", loanNo);
			try {
				loanInfo.set("loanIndexByUser", MD5Code.md5(loanCode + loanCode.substring(0, 15)));
			} catch (Exception e) {
				renderJson(error4json("0030", "generate loan index error", loanNumber));
				return;
			}
			loanInfo.set("loanTitle", SysEnum.productType.E.desc() + loanNo);
			loanInfo.set("loanType", LoanInfo.loanType.J.val());
			loanInfo.set("loanTypeDesc", LoanInfo.loanType.J.desc());
			loanInfo.set("hasInvedByTrips", 1);//是否实地考察
			loanInfo.set("isInterest", 1);//本息保障
			loanInfo.set("isAutoLoan", 1);//自动放款
			loanInfo.set("hasCaptcha", 0);//是否需要验证码
			loanInfo.set("createDate", DateUtil.getNowDate());
			loanInfo.set("createTime", DateUtil.getNowTime());
			loanInfo.set("updateDate", DateUtil.getNowDate());
			loanInfo.set("updateTime", DateUtil.getNowTime());
			loanInfo.set("loanAmount", loanAmount);
			loanInfo.set("loanATAmount", loanAmount);
			loanInfo.set("loanBalance", loanAmount);
			loanInfo.set("invedTripFees", 0);//实地考察费用
			loanInfo.set("serviceFees", txFee);//服务费
			loanInfo.set("managerRate", 0);//管理费
			loanInfo.set("riskRate", 0);//风险储备金率
			loanInfo.set("userCode", userCode);
			loanInfo.set("userName", loanUserName);
			try {
				loanInfo.set("userCardId", CommonUtil.encryptUserCardId(loanUserCardId));
			} catch (Exception e) {
				renderJson(error4json("0040", "加载错误", loanNumber));
				return;
			}
			loanInfo.set("loanArea", "易分期");//地区
			loanInfo.set("loanState", com.dutiantech.util.SysEnum.loanState.H.val());
			loanInfo.set("opCode", "fffffffffffffffff");
			loanInfo.set("opName", "系统");
			loanInfo.set("releaseDate", DateUtil.getNowDate());
			loanInfo.set("releaseTime", DateUtil.getNowTime());
			loanInfo.set("loanTimeLimit", loanTimeLimit);
			loanInfo.set("reciedCount", 0);
			loanInfo.set("loadByDay", 0);
			loanInfo.set("refundType", refundType.A.val());
			loanInfo.set("rateByYear", rateByYear);
			loanInfo.set("rewardRateByYear", rewardRateByYear);
			loanInfo.set("loanTotal", 0);//贷款总额
			loanInfo.set("loanCount", 0);//贷款总次数
			loanInfo.set("minLoanAmount", 10000);
			loanInfo.set("maxLoanAmount", 100000000);
			loanInfo.set("benefits4new", 0);//新人专享福利
			loanInfo.set("maxLoanCount", 999);//最大投标次数
			loanInfo.set("loanUsedType", loanUsedType.I.val());
			loanInfo.set("loanDesc", userInfoDesc);
			loanInfo.set("backDate", "");
			loanInfo.set("loanMac", "");
			loanInfo.set("effectDate", "");
			loanInfo.set("effectTime", "");
			loanInfo.set("clearDate", "0000");
			loanInfo.set("loan_pic", "[]");
			loanInfo.set("productType", SysEnum.productType.E.val());
			loanInfo.set("ssn", CommonUtil.genMchntSsn());
//		} else {	// 	更新现有标地状态
//			loanInfo = loanInfoService.findById(loanCode);
//			
//			if (loanInfo == null) {
//				return error("02", "标的编号错误", null);
//			}
//			if (loanAmount != loanInfo.getLong("loanAmount")) {
//				return error("02", "标的借款金额不匹配", null);
//			}
//			int addDays = loanInfo.getInt("loanTimeLimit") * 30;
//			String exprieDate = DateUtil.addDay(loanInfo.getStr("effectDate"), addDays);	// 按天数计算标的截止日期
//			int leftDays = DateUtil.differentDaysByMillisecond(DateUtil.getNowDate(), exprieDate, "yyyyMMdd");
//			if (leftDays < term) {
//				return error("02", "借款天数不可大于标的剩余天数", null);
//			}
//			
//			// 更新标的借款人信息
//			loanInfo.set("userName", loanUserName);
//			try {
//				loanInfo.set("userCardId", CommonUtil.encryptUserCardId(loanUserCardId));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
		// 关联标信息
		loanNxjd = new LoanNxjd();
		loanNxjd.set("loanCode", loanCode);
		loanNxjd.set("ztyLoanCode", loanNumber);
		loanNxjd.set("loanAmount", loanAmount);
		loanNxjd.set("term", loanTimeLimit);
		loanNxjd.set("status", STATUS.A.key());
		loanNxjd.set("userName", loanUserName);
		loanNxjd.set("userCardId", loanUserCardId);
		
		//即信发送标信息
		String raiseDate = DateUtil.getNowDate();
		String raiseEndDate = DateUtil.addDay(raiseDate, 60);
		String duration = String.valueOf(loanTimeLimit*30);
		String txAmount = StringUtil.getMoneyYuan(loanAmount);
		String rate = StringUtil.getMoneyYuan(rateByYear+rewardRateByYear);
		String bailAccountId = JXService.RISK_RESERVE;
		Map<String, String> resultMap = JXController.debtRegister(jxAccountId, loanCode, raiseDate, raiseEndDate, "2", "", duration, txAmount, rate, bailAccountId);
		if("00000000".equals(resultMap.get("retCode"))){
			loanNxjdService.saveOrUpdate(loanNxjd);
			loanInfoService.saveOrUpdate(loanInfo);
			renderJson(succ4json("发布成功", loanNumber));
			return;
		}else{
			renderJson(error4json("0050", "发布失败", loanNumber));
			return;
		}
	}
	
	/**
	 * 返回需要补标的智投盈标的
	 * @return
	 */
	@ActionKey("/query4Nxjd")
	@Before({PkMsgInterceptor.class})
	public Message queryLoan4Nxjd() {
		// 验证来源
		String[] allowIp = {"127.0.0.1", "192.168.2.188"};
		if (!Arrays.asList(allowIp).contains(getRequestIP())) {
			return error("Error Request Address", "Error Request Address", "Error Request Address");
		}
		
		List<Map<String, Object>> result = new ArrayList<Map<String,Object>>();
		List<LoanInfo> lstLoanInfo = loanInfoService.findByNxjd();
		for (LoanInfo loanInfo : lstLoanInfo) {
			Map<String, Object> map = new HashMap<String, Object>();
			// 标的状态为还款中时，校验是否需要补标
			if ("N".equals(loanInfo.getStr("loanState"))) {
				String effectDate = loanInfo.getStr("effectDate");
				int addDays = loanInfo.getInt("loanTimeLimit") * 30;
				String exprieDate = DateUtil.addDay(effectDate, addDays);	// 按天数计算标的截止日期
				int leftDays = DateUtil.differentDaysByMillisecond(DateUtil.getNowDate(), exprieDate, "yyyyMMdd");
				LoanNxjd loanNxjd = loanNxjdService.findLastByLoanCode(loanInfo.getStr("loanCode"));
				
				if (LoanNxjd.STATUS.B.key().equals(loanNxjd.getStr("status")) && leftDays > 0) {
					map.put("loanCode", loanInfo.get("loanCode"));
					map.put("loanAmount", loanInfo.get("loanAmount"));
					map.put("leftDays", leftDays);
					result.add(map);
				}
			}
//			map.put("loanCode", loanInfo.get("loanCode"));	// 标的编号
////			map.put("loanTitle", loanInfo.get("loanTitle"));	// 标的名称
//			map.put("loanAmount", loanInfo.get("loanAmount"));	// 借款金额
////			map.put("loanState", loanState.valueOf(loanInfo.getStr("loanState")));	// 标的状态
////			map.put("exprieDate", loanInfo.get("backDate"));	// 标的截止日期
//			
//			if ("N".equals(loanInfo.getStr("loanState"))) {	// 标的状态为还款中时
//				String effectDate = loanInfo.getStr("effectDate");
//				int addDays = loanInfo.getInt("loanTimeLimit") * 30;
//				String exprieDate = DateUtil.addDay(effectDate, addDays);	// 按天数计算标的截止日期
//				int leftDays = DateUtil.differentDaysByMillisecond(DateUtil.getNowDate(), exprieDate, "yyyyMMdd");
//				LoanNxjd loanNxjd = loanNxjdService.findLastByLoanCode(loanInfo.getStr("loanCode"));
//				// 如果标的放款日期，加上关联标累计借款天数，大于或等于当天日期，则需要补标
////				String nxjdLoanExprieDate = DateUtil.addDay(effectDate, loanNxjdService.sumBorrowTimeByLoanCode(loanInfo.getStr("loanCode")));
////				int compareDate = DateUtil.compareDateByStr("yyyyMMdd", nxjdLoanExprieDate, DateUtil.getNowDate());
//				
//				// 如果关联标已到期，且剩余天数大于0，则返回需要补标
//				if (LoanNxjd.STATUS.B.key().equals(loanNxjd.getStr("status")) && leftDays > 0) {
////				if (compareDate == 0 || compareDate == -1) {	// 是否需要补标
//					map.put("needPub", true);
//				} else {
//					map.put("needPub", false);
//				}
//				map.put("leftDays", leftDays);	// 标的剩余天数
//			}
//			result.add(map);
		}
		return succ("succ", result);
	}
	
	
//	@ActionKey("/settlement1")
//	@AuthNum(value=157,pval=26,type=1,desc="正常还款")
//	@Before({LoanPubLoanByQuick.class,AuthInterceptor.class})
//	public Message settlement1(){
//		String loanCode = getPara("loanCode");
//		LoanInfo loanInfo = loanInfoService.findById(loanCode);
//		String loanUserCode = loanInfo.get("userCode","") ;
//		long avBalance = fundsService.findAvBalanceById(loanUserCode);//【借款人】可用余额
//		int[] tmpAmount = getTmpAmount(loanInfo) ;
//		int beRecyPrincipal4loan = tmpAmount[0] ;
//		int beRecyInterest4loan = tmpAmount[1] ;
//		//【借款人】可用余额不够此次还款
//		if((beRecyPrincipal4loan + beRecyInterest4loan) > avBalance){
//			return error("02", loanCode, loanCode);
//		}
//		boolean isOk = false;
//		String faildDesc = "";
//		try{
//			isOk = doLoan(loanInfo);
//			faildDesc = "清算时发现异常标流水!";
//		}catch(Exception e){
//			e.printStackTrace();
//			isOk = false ;
//			faildDesc = "清算异常：" + e.getMessage() ;
//		}
//		if( isOk == true){
//			//标结算成功，处理成功
//			loanInfoService.update4clearSuccess(loanInfo, SysEnum.loanState.N) ;
//			return succ("还款成功", true);
//		}else{
//			//标结算失败，处理失败,将标信息置为异常并记录
//			logSettlementService.saveLoan(loanCode, loanInfo.getStr("loanTitle"), "标结算异常，原因：" + faildDesc);
//			//修改异常流水状态
//			loanInfoService.update4clearException(loanCode, SysEnum.loanState.T);
//		}
//		return error("01", "还款失败", null);
//	}
//	
//	@ActionKey("/settlement2")
//	@AuthNum(value=158,pval=12,type=1,desc="系统代还(借款人可用余额不足)")
//	@Before({LoanPubLoanByQuick.class,AuthInterceptor.class})
//	public Message settlement2(){
//		String loanCode = getPara("loanCode");
//		LoanInfo loanInfo = loanInfoService.findById(loanCode);
//		boolean isOk = false;
//		String faildDesc = "";
//		try{
//			isOk = doLoan(loanInfo);
//			faildDesc = "清算时发现异常标流水!";
//		}catch(Exception e){
//			e.printStackTrace();
//			isOk = false ;
//			faildDesc = "清算异常：" + e.getMessage() ;
//		}
//		if( isOk == true){
//			//标结算成功，处理成功
//			loanInfoService.update4clearSuccess(loanInfo, SysEnum.loanState.N) ;
//			return succ("还款成功", true);
//		}else{
//			//标结算失败，处理失败,将标信息置为异常并记录
//			logSettlementService.saveLoan(loanCode, loanInfo.getStr("loanTitle"), "标结算异常，原因：" + faildDesc);
//			//修改异常流水状态
//			loanInfoService.update4clearException(loanCode, SysEnum.loanState.T);
//		}
//		return error("01", "还款失败", null);
//	}
//	
//	private boolean doLoan(LoanInfo loanInfo ){
//		//查询借款人账户余额是否充足
//		String loanUserCode = loanInfo.get("userCode","") ;
//		String loanCode = loanInfo.getStr("loanCode");
//		long avBalance = fundsService.findAvBalanceById(loanUserCode);//【借款人】可用余额
//		//TODO 年奖励让在另外一个任务计算
////		int rewardRateByYear = loanInfo.getInt("rewardRateByYear");//奖励年利率
//		int[] tmpAmount = getTmpAmount(loanInfo) ;
//		int beRecyPrincipal4loan = tmpAmount[0] ;
//		int beRecyInterest4loan = tmpAmount[1] ;
//		//【借款人】可用余额不够此次还款
//		if((beRecyPrincipal4loan + beRecyInterest4loan) > avBalance){
//			//isProxy = true;//代还
//			//不考虑平台代扣钱，根据借款人账户资金正负判断
//			/** 增加逾期流水  **/
//			Map<String, Object> para = new HashMap<String, Object>();
//			para.put("loanCode", loanCode);
//			para.put("loanUserCode", loanUserCode);
//			para.put("loanUserName", loanInfo.getStr("userName"));
//			para.put("loanAmount", loanInfo.getLong("loanAmount"));
//			para.put("loanTitle", loanInfo.getStr("loanTitle"));
//			para.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
//			para.put("principal", beRecyPrincipal4loan);
//			para.put("interest", beRecyInterest4loan);
//			para.put("overdueAmount", (beRecyPrincipal4loan + beRecyInterest4loan)-avBalance);
//			para.put("refundType", loanInfo.getStr("refundType"));
//			para.put("repayIndex", loanInfo.getInt("reciedCount")+1);
//			overdueTraceService.save(para);
//			userService.updateUserState(loanUserCode, SysEnum.userState.S.val());
//		}
//		
//		//获取理财人的标流水
//		List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode( loanCode ) ;
//		List<String> faildResult = new ArrayList<String>();
//		doLoanTrace( 0 , loanInfo , loanTraces , faildResult );
//		boolean loanTraceResult = true;
//		if( faildResult.size() > 0 ){
//			//发现有标流水处理异常
//			//logSettlementService.saveTrace(loanInfo ,  "日结算失败！详情：" + faildResult );
//			Log.info( "标编号：" + loanCode + "，日结算失败！详情：" + faildResult );
//			loanTraceResult = false ;	
//		}
//		int loanTimeLimit = loanInfo.getInt("loanTimeLimit");//还款期数 
//		int reciedCount = loanInfo.getInt("reciedCount");
//		boolean x1 = fundsService.updateByRepayment4LoanUser(loanUserCode, beRecyPrincipal4loan, beRecyInterest4loan, 0, 0);
//		boolean isLastLimit = (loanTimeLimit-reciedCount) == 1 ? true : false;//(还款期数-已还期数)==1 ? 最后一次到期还款 :
//		if(x1){
//			if(isLastLimit){
//				boolean x2 = fundsService.updateByRepaymentOver4LoanUser(loanUserCode);
//				if(!x2)
//					return false;
//			}
//			if(loanTraceResult){
//				return true;
//			}
//		}
//		return false;
//	}
//	
//	/**
//	 * 	处理标流水
//	 * @return
//	 */
//	private void doLoanTrace(int index , LoanInfo loan , List<LoanTrace> traces , List<String> result ){
//		
//		if( traces.size() == index ){
//			return ;
//		}
//		LoanTrace trace = traces.get(index) ;
//		index ++ ;//next
////		String loanUserCode = loan.getStr("userCode") ;
//		String traceCode = trace.get("traceCode"); 
//		String payUserCode = trace.get("payUserCode","");//投标人编码 
//		int payAmount = trace.getInt("payAmount");//投标金额 
//		String trace_refundType = trace.get("refundType","O");//投标还款方式 
//		int trace_rateByYear = trace.getInt("rateByYear");//投标年利率 
//		//年奖励利息开服务单独计算
//		int loanTimeLimit = loan.getInt("loanTimeLimit");//还款期数 
//		int reciedCount = loan.getInt("reciedCount");
//		boolean isLastLimit = (loanTimeLimit-reciedCount) == 1 ? true : false;//(还款期数-已还期数)==1 ? 最后一次到期还款 :
//		int[] tmp2 = new int[]{0,0};
//		if(trace_refundType.equals( SysEnum.refundType.A.val() )){//按月等额本息
//			tmp2 = CommonUtil.f_001(payAmount, reciedCount+1 , trace_rateByYear , loanTimeLimit );
//		}else if(trace_refundType.equals( SysEnum.refundType.B.val() )){//按月付息，到期还本
//			tmp2 = CommonUtil.f_002(payAmount, trace_rateByYear, loanTimeLimit , isLastLimit);
//		}else if(trace_refundType.equals( SysEnum.refundType.C.val() )){//到期还本息
//			tmp2 = CommonUtil.f_003(payAmount, trace_rateByYear, loanTimeLimit , isLastLimit);
//		}
//		int principal = tmp2[0];
//		int interest = tmp2[1];
//		
//		try{
//			boolean x2 = fundsService.updateByRepayment4PayUser(payUserCode, principal, interest, 0);
//			if(x2){
//				if(isLastLimit){//最后一期还款完成，投标人资金账户；投标流水状态
//					fundsService.updateByRepaymentOver4PayUser(payUserCode);
//					//最外面标结算完成后批量改状态
////					loanTraceService.updateTraceState(traceCode, SysEnum.traceState.B.val(),  traceState);
//				}
//				doLoanTrace( index , loan , traces , result );
//			}
//		}catch(Exception e){
//			e.printStackTrace();
//			String loanCode = trace.getStr("loanCode");
//			String loanTitle = trace.getStr("loanTitle");
//			loanTraceService.updateTraceState(traceCode, SysEnum.traceState.E.val(),trace.getStr("traceState"));
//			logSettlementService.saveTrace(loanCode, traceCode, loanTitle, "处理标流水异常：" + e.getMessage() ) ;
//			result.add("\r\n"+traceCode+"处理标流水异常：" + e.getMessage());
//			doLoanTrace( index , loan , traces , result );
//		}
//		
//	}
//	
//	private int[] getTmpAmount(LoanInfo loanInfo){
//		String refundType = loanInfo.getStr("refundType");//还款方式 
//		long loanAmount = loanInfo.getLong("loanAmount");//贷款金额
//		int reciedCount = loanInfo.getInt("reciedCount");//已还笔数 
//		int rateByYear = loanInfo.getInt("rateByYear");//年利率
//		int loanTimeLimit = loanInfo.getInt("loanTimeLimit");//还款期数 
//		boolean isLastLimit = checkIsLastLimit(loanInfo) ;
//		int[] tmpAmount = new int[]{0,0};//[本金,利息]
//		if(refundType.equals("A")){//按月等额本息
//			tmpAmount = CommonUtil.f_001(loanAmount, reciedCount+1,rateByYear, loanTimeLimit);
//		}else if(refundType.equals("B")){//按月付息，到期还本
//			tmpAmount = CommonUtil.f_002(loanAmount, rateByYear, loanTimeLimit, isLastLimit);
//		}else if(refundType.equals("C")){//到期还本息
//			tmpAmount = CommonUtil.f_003(loanAmount, rateByYear, loanTimeLimit, isLastLimit);
//		}
//		
//		return tmpAmount ;
//	}
//	
//	private boolean checkIsLastLimit(LoanInfo loanInfo ){
//		int loanTimeLimit = loanInfo.getInt("loanTimeLimit");//还款期数 
//		int reciedCount = loanInfo.getInt("reciedCount");//已还笔数
//		boolean isLastLimit = (loanTimeLimit-reciedCount) == 1 ? true : false;//(还款期数-已还期数)==1 ? 最后一次到期还款 : 
//		return isLastLimit ;
//	}
	
//	@ActionKey("/revokeLoan")
//	@AuthNum(value=999,pval=32,type=1,desc="撤回借款标")
//	@Before({LoanAuditLoanByLoanValidator.class,AuthInterceptor.class,Tx.class})
//	public Message revokeLoan(){
//		String loanCode = getPara("loanCode");
//		if( StringUtil.isBlank( loanCode ) == true )
//			return error("01", "请求参数异常!", null) ;
//		
//		if( loanCode.length() != 32)
//			return error("02", "请求参数异常！", null );
//		
//		LoanInfo loan = loanInfoService.findById(loanCode);
//		if( SysEnum.loanState.J.val().equals( loan.getStr("loanState") ) == false){
//			return error("02", "当前标的状态为：" + loan.getStr("loanState") + ",不满足审核条件！",
//					null ) ;
//		}
//		
//		//新人福利 benefits4new
//		List<LoanTrace> traces = loanTraceService.findAllByLoanCode( loanCode );
//		//失败流水记录
//		
//		for(LoanTrace trace : traces ){
//			//处理标流水
//			String payUserCode = trace.getStr("payUserCode");
//			long payAmount = trace.getLong("payAmount");
//			//冻结资金转可用资金
//			fundsServiceV2.revokeLoan4Funds(payUserCode, payAmount) ;
//			trace.delete();
//		}
//		
//		//更新标状态
//		loan.set("updateDate", DateUtil.getNowDate() ) ;
//		loan.set("updateTime", DateUtil.getNowTime() ) ;
//		loan.set("loanState", loanState.S.val() ) ;
//		loan.set("loanBalance", loan.getLong("loanAmount"));
//		loan.set("releaseDate", "");
//		loan.set("releaseTime", "");
//		if( loan.update() == false ){
//			return error("06","操作未生效",false);
//		}
//		return succ("流标处理完成!",true);
//		
//	}
	
	/**
	 * 分页查询待提现标流水
	 * @return
	 */
	@ActionKey("/getLoanList4Withdraw")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanList4Withdraw(){
		OPUserV2 opUserV2 = opUserService.findById(getUserCode());
		String group = opUserV2.getStr("op_group");
		if("总部".equals(group)||"技术部".equals(group)){
			group = "";
		}
		String state = getPara("state");
		if("N".equals(state)){
			state="";
		}
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","20180412");
		
		String endDate = getPara("endDate","");
		Page<LoanInfo> pageLoanInfo = loanInfoService.findByPage4Withdraw(pageNumber,pageSize,beginDate,endDate,state,group);
		return succ("OK", pageLoanInfo);
	}
	/**
	 * 查询标借款人基本信息
	 * */
	@ActionKey("/queryLoanAndUser")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryLoanAndUser(){
		String loanCode = getPara("loanCode");
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		if (loanInfo == null) {
			return error("02", "借款标获取失败", null);
		}
		User user = userService.findById(loanInfo.getStr("userCode"));
		String userMobile = user.getStr("userMobile");
		try {
			userMobile = CommonUtil.decryptUserMobile(userMobile);
		} catch (Exception e) {
			return error("01", "信息获取失败", null);
		}
		user.set("userMobile",userMobile);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("loanInfo", loanInfo);
		map.put("user", user);
		return succ("OK", map);
	}

	/**
	 * 查询当日还款总览信息 WJW
	 * @return
	 */
	@ActionKey("/repaymentByDay")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message repaymentByDay(){
		String date = getPara("date");//标还款日期
		if(StringUtil.isBlank(date) || date.length() != 8 || !StringUtil.isNumeric(date)){
			return error("", "日期输错了,别打瞌睡了!!!", null);
		}
		String txDate = getPara("txDate",date);//结算日期(批次,红包发送日期,逾期还款日期)
		if(Long.valueOf(date) >= 20180815){//20180815之后,还款改为T+1
			txDate = DateUtil.addDay(date, 1);
		}
		
		//20180917之后使用回款统计表数据统计
		if(Long.valueOf(date) >= 20180917){
			Map<String, Object> loanMap = new HashMap<String,Object>();//当日还款信息
			//还款短信
			long readySMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 8);//准备发送中短信数量
			long soonSMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 9);//准备发送完成短信数量
			long sucSMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 0);//发送完成短信数量
			
			//回款短信
			loanMap.put("SMS", sucSMSNum+"/"+(sucSMSNum+readySMSNum+soonSMSNum));//还款短信数量(已发/应发)
			
			RepaymentCount repaymentCount = repaymentCountService.findByRepaymentDate(date);
			List<String> thawBatchNos = new ArrayList<String>();//资金未解冻批次号
			List<String> failBatchNos = new ArrayList<String>();//失败批次号
			List<String> aisleBatchNos = new ArrayList<String>();//失败已处理批次号
			List<String> reissueBatchNos = new ArrayList<String>();//补发批次号
			if(repaymentCount != null){
				String batchRecord = repaymentCount.getStr("batchRecord");
				if(!StringUtil.isBlank(batchRecord)){
					Map<String, ?> parseObject = JSONObject.parseObject(batchRecord);
					Map<String, JSONObject> batchRecordMap = (Map<String, JSONObject>) parseObject;
					for(Map.Entry<String, JSONObject> entry:batchRecordMap.entrySet()){
						String batchNo = entry.getKey();
						JSONObject batchRecordJsonObject = entry.getValue();
						int status = batchRecordJsonObject.getInteger("status");
						int type = batchRecordJsonObject.getInteger("type");
						if(status == 0){//未解冻
							thawBatchNos.add(batchNo);
						}
						if(status == 2 || status == 3){//2:全部失败,3:部分失败
							failBatchNos.add(batchNo);
						}
						if(status == 4){//失败已处理
							aisleBatchNos.add(batchNo);
						}
						if(type == 4){//补发
							reissueBatchNos.add(batchNo);
						}
					}
					
					Collections.sort(thawBatchNos,new Comparator<String>() {
						@Override
						public int compare(String thawBatchNo1, String thawBatchNo2) {
							return Integer.parseInt(thawBatchNo1) - Integer.parseInt(thawBatchNo2);
						}
					});
					Collections.sort(failBatchNos,new Comparator<String>() {
						@Override
						public int compare(String failBatchNo1, String failBatchNo2) {
							return Integer.parseInt(failBatchNo1) - Integer.parseInt(failBatchNo2);
						}
					});
					Collections.sort(aisleBatchNos,new Comparator<String>() {
						@Override
						public int compare(String aisleBatchNo1, String aisleBatchNo2) {
							return Integer.parseInt(aisleBatchNo1) - Integer.parseInt(aisleBatchNo2);
						}
					});
					Collections.sort(reissueBatchNos,new Comparator<String>() {
						@Override
						public int compare(String reissueBatchNo1, String reissueBatchNo2) {
							return Integer.parseInt(reissueBatchNo1) - Integer.parseInt(reissueBatchNo2);
						}
					});
					
				}
				
				//回款进度
				loanMap.put("repaymentStatus", repaymentCountStatus.valueOf(repaymentCount.getStr("repaymentStatus")).desc());//A:批次发送中,B:批次发送完成,C:还款完成,N:未还款
				
				//批次资金信息
				loanMap.put("pcdczbj", StringUtil.getMoneyYuan(repaymentCount.getLong("pcdcshzbj"))+"元/"+StringUtil.getMoneyYuan(repaymentCount.getLong("pcdcyhzbj"))+"元");//批次代偿总本金(实还/应还)
				loanMap.put("pcdczlx", StringUtil.getMoneyYuan(repaymentCount.getLong("pcdcshzlx"))+"元/"+StringUtil.getMoneyYuan(repaymentCount.getLong("pcdcyhzlx"))+"元");//批次代偿总利息(实还/应还)
				loanMap.put("pcdcshzsxf", StringUtil.getMoneyYuan(repaymentCount.getLong("pcdcshzsxf"))+"元");//批次代偿实收总手续费
				
				//批次数量信息
				loanMap.put("dcscpcsl", jxTraceService.countByTxCodeAndTxDate(jxTxCode.batchSubstRepay.val(), txDate)+"/"+(repaymentCount.getInt("yfzchkpcs")+repaymentCount.getInt("yfyqbjpcs")+repaymentCount.getInt("yfyqdflxpcs")));//代偿生成批次数量(实生成/应生成)
				loanMap.put("zchkpcs", repaymentCount.getInt("sfzchkpcs")+"/"+repaymentCount.getInt("yfzchkpcs"));//正常还款批次数(实还/应还)
				loanMap.put("yqbjpcs", repaymentCount.getInt("sfyqbjpcs")+"/"+repaymentCount.getInt("yfyqbjpcs"));//逾期本金批次数(实还/应还)
				loanMap.put("yqdflxpcs", repaymentCount.getInt("sfyqdflxpcs")+"/"+repaymentCount.getInt("yfyqdflxpcs"));//逾期垫付利息批次数(实还/应还)
				loanMap.put("pczjjdsl", repaymentCount.getInt("pczjyjdsl")+"/"+(repaymentCount.getInt("pczjwjdsl")+repaymentCount.getInt("pczjyjdsl")));//批次资金解冻数量(已解冻/应解冻)
				loanMap.put("sbpcsl", repaymentCount.getInt("yclsbpcsl")+"/"+repaymentCount.getInt("sbpcsl"));//失败批次数量(已处理失败/失败)
				
				//批次号详情信息
				loanMap.put("thawBatchNos", thawBatchNos);//资金未解冻批次号
				loanMap.put("failBatchNos", failBatchNos);//失败批次号
				loanMap.put("aisleBatchNos", aisleBatchNos);//失败已处理批次号
				loanMap.put("reissueBatchNos", reissueBatchNos);//补发批次号
				
				//红包资金信息
				loanMap.put("hbzbj", StringUtil.getMoneyYuan(repaymentCount.getLong("hbshzbj"))+"元/"+StringUtil.getMoneyYuan(repaymentCount.getLong("hbyhzbj"))+"元");//红包总本金(实还/应还)
				loanMap.put("hbzlx", StringUtil.getMoneyYuan(repaymentCount.getLong("hbshzlx"))+"元/"+StringUtil.getMoneyYuan(repaymentCount.getLong("hbyhzlx"))+"元");//红包总利息(实出账:利息-手续费-未开户/应还)
				loanMap.put("hbfysfze", StringUtil.getMoneyYuan(repaymentCount.getLong("hbfysfze"))+"元");//红包返佣实发总额
				loanMap.put("wkhhkze", StringUtil.getMoneyYuan(repaymentCount.getLong("wkhhkze"))+"元");//未开户还款总额(本金+利息+佣金)
				
				//红包数量信息
				loanMap.put("sfhbsl", repaymentCount.getInt("sfhbsl"));//实发红包数量
				loanMap.put("sbhbsl", repaymentCount.getInt("yclsbhbsl")+"/"+repaymentCount.getInt("sbhbsl"));//失败红包数量(失败已处理/失败)
				
				//标资金信息
				loanMap.put("sjhkl", StringUtil.getPercentage(repaymentCount.getLong("sjzchkzbj")+repaymentCount.getLong("sjzchkzlx")+repaymentCount.getLong("yqhkzbj")+repaymentCount.getLong("yqdfzlx"), repaymentCount.getLong("yhzbj")+repaymentCount.getLong("yhzlx")));//实际回款率
				loanMap.put("yhzbj", StringUtil.getMoneyYuan(repaymentCount.getLong("yhzbj"))+"元");//应还总本金
				loanMap.put("yhzlx", StringUtil.getMoneyYuan(repaymentCount.getLong("yhzlx"))+"元");//应还总利息
				loanMap.put("sjzchkzbj", StringUtil.getMoneyYuan(repaymentCount.getLong("sjzchkzbj"))+"元");//实际正常还款总本金
				loanMap.put("sjzchkzlx", StringUtil.getMoneyYuan(repaymentCount.getLong("sjzchkzlx"))+"元");//实际正常还款总利息
				loanMap.put("yqhkzbj", StringUtil.getMoneyYuan(repaymentCount.getLong("yqhkzbj"))+"元");//逾期还款总本金
				loanMap.put("yqdfzlx", StringUtil.getMoneyYuan(repaymentCount.getLong("yqdfzlx"))+"元");//逾期垫付总利息
				
				//标数量信息
				loanMap.put("zchkbds", (repaymentCount.getInt("yhzchkbds")-repaymentCount.getInt("whzchkbds"))+"/"+repaymentCount.getInt("yhzchkbds"));//正常还款标的数(实还/应还)
				loanMap.put("tqhkbds", (repaymentCount.getInt("yhtqhkbds")-repaymentCount.getInt("whtqhkbds"))+"/"+repaymentCount.getInt("yhtqhkbds"));//提前还款标的数(实还/应还)
				loanMap.put("sjzchkbds", (repaymentCount.getInt("sjzchkbds")-repaymentCount.getInt("whsjzchkbds"))+"/"+repaymentCount.getInt("sjzchkbds"));//实际正常还款标的数(实还/应还)
				loanMap.put("sjtqhkbds", (repaymentCount.getInt("sjtqhkbds")-repaymentCount.getInt("whsjtqhkbds"))+"/"+repaymentCount.getInt("sjtqhkbds"));//实际提前还款标的数(实还/应还)
				loanMap.put("yqhbjbds", (repaymentCount.getInt("yqhbjbds")-repaymentCount.getInt("whyqbjbds"))+"/"+repaymentCount.getInt("yqhbjbds"));//逾期还本金标的数(实还/应还)
				loanMap.put("yqdflxbds", (repaymentCount.getInt("yqdflxbds")-repaymentCount.getInt("whyqdflxbds"))+"/"+repaymentCount.getInt("yqdflxbds"));//逾期垫付利息标的数(实还/应还)
				loanMap.put("yqbdflxbds", (repaymentCount.getInt("yqbdflxbds")-repaymentCount.getInt("whyqbdflxbds"))+"/"+repaymentCount.getInt("yqbdflxbds"));//逾期不垫付利息标的数(实还/应还)
				loanMap.put("fjsrhkbds", repaymentCount.getInt("fjsrhkbds"));//非结算日还款标的数
				
				//公布信息
				loanMap.put("gbhkl", StringUtil.getPercentage(repaymentCount.getLong("sjzchkzbj")+repaymentCount.getLong("sjzchkzlx")-repaymentCount.getLong("qtsjzchkzbj")-repaymentCount.getLong("qtsjzchkzlx")+repaymentCount.getLong("yqdfzlx")+repaymentCount.getLong("yqhkzbj"), repaymentCount.getLong("yhzbj")-repaymentCount.getLong("qtyhzbj")+repaymentCount.getLong("sjzchkzlx")));//公布回款率(含逾期本金还款)
				loanMap.put("bhqtyhze", StringUtil.getMoneyYuan(repaymentCount.getLong("yhzbj")-repaymentCount.getLong("qtyhzbj")+repaymentCount.getLong("sjzchkzlx"))+"元");//不含齐通应还总额(中州正常回款本金+中州逾期本金+实际还款利息)
				loanMap.put("bhqtsjhkzje", StringUtil.getMoneyYuan(repaymentCount.getLong("sjzchkzbj")+repaymentCount.getLong("sjzchkzlx")-repaymentCount.getLong("qtsjzchkzbj")-repaymentCount.getLong("qtsjzchkzlx")+repaymentCount.getLong("yqdfzlx"))+"元");//不含齐通实际还款总额(中州正常回款本金利息+中州齐通垫付利息)
				loanMap.put("bhqtyhbds", repaymentCount.getInt("yhzchkbds")+repaymentCount.getInt("yhtqhkbds")-repaymentCount.getInt("qtyhkbds"));//不含齐通应还标的数
				loanMap.put("bhqtsjhkbds", repaymentCount.getInt("sjzchkbds")+repaymentCount.getInt("sjtqhkbds")-repaymentCount.getInt("qtsjhkbds"));//不含齐通实际还款标的数
				
				
				//每日回款具体金额（带逾期，区分代偿与红包）
//				Map<String, Long> backAmount = settlementPlanService.queryBackAmount(DateUtil.getNowDate());
//				loanMap.put("dNormalAmount", StringUtil.getMoneyYuan(backAmount.get("dNormalAmount")));//代偿正常回款
//				loanMap.put("dOverdueInterest", StringUtil.getMoneyYuan(backAmount.get("dOverdueInterest")));//代偿逾期利息
//				loanMap.put("dOverduePrincipal", StringUtil.getMoneyYuan(backAmount.get("dOverduePrincipal")));//代偿逾期本金
//				loanMap.put("hNormalAmount", StringUtil.getMoneyYuan(backAmount.get("hNormalAmount")));//红包正常回款
//				loanMap.put("hOverdueInterest", StringUtil.getMoneyYuan(backAmount.get("hOverdueInterest")));//红包逾期利息
//				loanMap.put("hOverduePrincipal", StringUtil.getMoneyYuan(backAmount.get("hOverduePrincipal")));//红包逾期本金
				
				//用户编码
				loanMap.put("opCode", getUserCode());
				
				return succ("",loanMap);
			}else {
				Map<String, Long> repaymentCountDataMap = loanInfoService.repaymentCount(date);
				
				//回款进度
				loanMap.put("repaymentStatus", repaymentCountStatus.N.desc());//A:批次发送中,B:批次发送完成,C:还款完成,N:未开始
				
				//批次资金信息
				loanMap.put("pcdczbj", "0元/"+StringUtil.getMoneyYuan(repaymentCountDataMap.get("pcdcyhzbj"))+"元");//批次代偿总本金(实还/应还)
				loanMap.put("pcdczlx", "0元/"+StringUtil.getMoneyYuan(repaymentCountDataMap.get("pcdcyhzlx"))+"元");//批次代偿总利息(实还/应还)
				loanMap.put("pcdcshzsxf", "0元");//批次代偿实收总手续费
				
				//批次数量信息
				loanMap.put("dcscpcsl", jxTraceService.countByTxCodeAndTxDate(jxTxCode.batchSubstRepay.val(), txDate)+"/"+(repaymentCountDataMap.get("yfzchkpcs")+repaymentCountDataMap.get("yfyqbjpcs")+repaymentCountDataMap.get("yfyqdflxpcs")));//代偿生成批次数量(实生成/应生成)
				loanMap.put("zchkpcs", "0/"+repaymentCountDataMap.get("yfzchkpcs"));//正常还款批次数(实还/应还)
				loanMap.put("yqbjpcs", "0/"+repaymentCountDataMap.get("yfyqbjpcs"));//逾期本金批次数(实还/应还)
				loanMap.put("yqdflxpcs", "0/"+repaymentCountDataMap.get("yfyqdflxpcs"));//逾期垫付利息批次数(实还/应还)
				loanMap.put("pczjjdsl", "0/0");//批次资金解冻数量(已解冻/应解冻)
				loanMap.put("sbpcsl", "0/0");//失败批次数量(已处理失败/失败)
				
				//批次号详情信息
				loanMap.put("thawBatchNos", thawBatchNos);//资金未解冻批次号
				loanMap.put("failBatchNos", failBatchNos);//失败批次号
				loanMap.put("aisleBatchNos", aisleBatchNos);//失败已处理批次号
				loanMap.put("reissueBatchNos", reissueBatchNos);//补发批次号
				
				//红包资金信息
				loanMap.put("hbzbj", "0元/"+StringUtil.getMoneyYuan(repaymentCountDataMap.get("hbyhzbj"))+"元");//红包总本金(实还/应还)
				loanMap.put("hbzlx", "0元/"+StringUtil.getMoneyYuan(repaymentCountDataMap.get("hbyhzlx"))+"元");//红包总利息(实出账:利息-手续费-未开户/应还)
				loanMap.put("hbfysfze", "0元");//红包返佣实发总额
				loanMap.put("wkhhkze", "0元");//未开户还款总额(本金+利息+佣金)
				
				//红包数量信息
				loanMap.put("sfhbsl", 0);//实发红包数量
				loanMap.put("sbhbsl", "0/0");//失败红包数量(失败已处理/失败)
				
				//标资金信息
				loanMap.put("sjhkl", StringUtil.getPercentage(repaymentCountDataMap.get("sjzchkzbj")+repaymentCountDataMap.get("sjzchkzlx")+repaymentCountDataMap.get("yqhkzbj")+repaymentCountDataMap.get("yqdfzlx"),repaymentCountDataMap.get("yhzbj")+repaymentCountDataMap.get("yhzlx")));//实际回款率
				loanMap.put("yhzbj", StringUtil.getMoneyYuan(repaymentCountDataMap.get("yhzbj"))+"元");//应还总本金
				loanMap.put("yhzlx", StringUtil.getMoneyYuan(repaymentCountDataMap.get("yhzlx"))+"元");//应还总利息
				loanMap.put("sjzchkzbj", StringUtil.getMoneyYuan(repaymentCountDataMap.get("sjzchkzbj"))+"元");//实际正常还款总本金
				loanMap.put("sjzchkzlx", StringUtil.getMoneyYuan(repaymentCountDataMap.get("sjzchkzlx"))+"元");//实际正常还款总利息
				loanMap.put("yqhkzbj", StringUtil.getMoneyYuan(repaymentCountDataMap.get("yqhkzbj"))+"元");//逾期还款总本金
				loanMap.put("yqdfzlx", StringUtil.getMoneyYuan(repaymentCountDataMap.get("yqdfzlx"))+"元");//逾期垫付总利息
				
				//标数量信息
				loanMap.put("zchkbds", "0/"+repaymentCountDataMap.get("yhzchkbds"));//正常还款标的数(实还/应还)
				loanMap.put("tqhkbds", "0/"+repaymentCountDataMap.get("yhtqhkbds"));//提前还款标的数(实还/应还)
				loanMap.put("sjzchkbds", "0/"+repaymentCountDataMap.get("sjzchkbds"));//实际正常还款标的数(实还/应还)
				loanMap.put("sjtqhkbds", "0/"+repaymentCountDataMap.get("sjtqhkbds"));//实际提前还款标的数(实还/应还)
				loanMap.put("yqhbjbds", "0/"+repaymentCountDataMap.get("yqhbjbds"));//逾期还本金标的数(实还/应还)
				loanMap.put("yqdflxbds", "0/"+repaymentCountDataMap.get("yqdflxbds"));//逾期垫付利息标的数(实还/应还)
				loanMap.put("yqbdflxbds", "0/"+repaymentCountDataMap.get("yqbdflxbds"));//逾期不垫付利息标的数(实还/应还)
				loanMap.put("fjsrhkbds", 0);//非结算日还款标的数
				
				//公布信息
				loanMap.put("gbhkl", StringUtil.getPercentage(repaymentCountDataMap.get("sjzchkzbj")+repaymentCountDataMap.get("sjzchkzlx")-repaymentCountDataMap.get("qtsjzchkzbj")-repaymentCountDataMap.get("qtsjzchkzlx")+repaymentCountDataMap.get("yqdfzlx")+repaymentCountDataMap.get("yqhkzbj"), repaymentCountDataMap.get("yhzbj")-repaymentCountDataMap.get("qtyhzbj")+repaymentCountDataMap.get("sjzchkzlx")));//公布回款率(含逾期本金还款)
				loanMap.put("bhqtyhze", StringUtil.getMoneyYuan(repaymentCountDataMap.get("yhzbj")-repaymentCountDataMap.get("qtyhzbj")+repaymentCountDataMap.get("sjzchkzlx"))+"元");//不含齐通应还总额(中州正常回款本金+中州逾期本金+实际还款利息)
				loanMap.put("bhqtsjhkzje", StringUtil.getMoneyYuan(repaymentCountDataMap.get("sjzchkzbj")+repaymentCountDataMap.get("sjzchkzlx")-repaymentCountDataMap.get("qtsjzchkzbj")-repaymentCountDataMap.get("qtsjzchkzlx")+repaymentCountDataMap.get("yqdfzlx"))+"元");//不含齐通实际还款总额(中州正常回款本金利息+中州齐通垫付利息)
				loanMap.put("bhqtyhbds", repaymentCountDataMap.get("yhzchkbds")+repaymentCountDataMap.get("yhtqhkbds")-repaymentCountDataMap.get("qtyhkbds"));//不含齐通应还标的数
				loanMap.put("bhqtsjhkbds", repaymentCountDataMap.get("sjzchkbds")+repaymentCountDataMap.get("sjtqhkbds")-repaymentCountDataMap.get("qtsjhkbds"));//不含齐通实际还款标的数
				
				//每日回款具体金额（带逾期，区分代偿与红包）
//				Map<String, Long> backAmount = settlementPlanService.queryBackAmount(DateUtil.getNowDate());
//				loanMap.put("dNormalAmount", StringUtil.getMoneyYuan(backAmount.get("dNormalAmount")));//代偿正常回款
//				loanMap.put("dOverdueInterest", StringUtil.getMoneyYuan(backAmount.get("dOverdueInterest")));//代偿逾期利息
//				loanMap.put("dOverduePrincipal", StringUtil.getMoneyYuan(backAmount.get("dOverduePrincipal")));//代偿逾期本金
//				loanMap.put("hNormalAmount", StringUtil.getMoneyYuan(backAmount.get("hNormalAmount")));//红包正常回款
//				loanMap.put("hOverdueInterest", StringUtil.getMoneyYuan(backAmount.get("hOverdueInterest")));//红包逾期利息
//				loanMap.put("hOverduePrincipal", StringUtil.getMoneyYuan(backAmount.get("hOverduePrincipal")));//红包逾期本金
				
				//用户编码
				loanMap.put("opCode", getUserCode());
				
				return succ("",loanMap);
			}
		}
		
		String clearDate = date.substring(4, 8);
		
		long sumNextVoucherAmount = 0;//明日红包还款预计总金额
		long sumNextBatchAmount = 0;//明日代偿还款预计总金额
		Map<String, String> accountIdMap = new HashMap<String,String>();//key:userCode,value:accountId
		
		//正常还款
		List<String> loanCodes = new ArrayList<String>();//还款日正常还款标号
		List<LoanRepayment> loanRepayments = loanRepaymentService.queryByRepaymentDate(date);//还款日正常还款债权信息
		for (LoanRepayment loanRepayment : loanRepayments) {
			String loanCode = loanRepayment.getStr("loanCode");
			if(loanCodes.indexOf(loanCode) == -1){
				loanCodes.add(loanCode);
			}
		}
		List<String> undoneLoanCodes = loanInfoService.undoneLoanCode(date, clearDate);//还款日未结算标号(不含提前还款)
		
		List<String> notBatchLoanCodes = loanTraceService.queryLoanCodeByLoanRecyDate(date);//当日未发批次标号(不含提前还款)
		List<LoanInfo> loanInfos = loanInfoService.findByJLSJ(date);//当日满标信息
		for (LoanInfo loanInfo : loanInfos) {
			String loanCode = loanInfo.getStr("loanCode");
			if(notBatchLoanCodes.indexOf(loanCode) != -1){
				notBatchLoanCodes.remove(loanCode);//剔除当日满标标号
			}
		}
		
		//提前还款
		List<String> earlyLoanCodes = new ArrayList<String>();//当日提前还款标号
		List<String> nextEarlyLoanCodes = new ArrayList<String>();//明日提前还款标号
		List<String> undoneEarlyLoanCodes = new ArrayList<String>();//当日提前还款未结算标号
		List<SettlementEarly> SettlementEarlys = settlementEarlyService.queryEarlyAll();//提前还款标
		for (SettlementEarly settlementEarly : SettlementEarlys) {
			String loanCode = settlementEarly.getStr("loanCode");
			String earlyDate = settlementEarly.getStr("earlyDate");
			String estatus = settlementEarly.getStr("estatus");
			
			//提前还款明日资金统计
			if(DateUtil.addDay(date, 1).equals(earlyDate)){
				if(nextEarlyLoanCodes.indexOf(loanCode) == -1){
					nextEarlyLoanCodes.add(loanCode);
				}
				List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
				for (LoanTrace loanTrace : loanTraces) {
					String authCode = loanTrace.getStr("authCode");
					if(StringUtil.isBlank(authCode)){//红包还款
						String payUserCode = loanTrace.getStr("payUserCode");
						String accountId = "";
						if(accountIdMap.containsKey(payUserCode)){
							accountId = accountIdMap.get(payUserCode);
						}else {
							User user = userService.findById(payUserCode);
							accountId = user.getStr("jxAccountId");
							accountIdMap.put(payUserCode, accountId);
						}
						
						if(StringUtil.isBlank(accountId)){
							continue;
						}
						
						sumNextVoucherAmount += loanTrace.getLong("leftAmount")+loanTrace.getLong("nextInterest");
					}else {//批次还款
						sumNextBatchAmount += loanTrace.getLong("leftAmount")+loanTrace.getLong("nextInterest");
					}
				}
			}
						
			if(date.equals(earlyDate)){//当日提前还款标号
				earlyLoanCodes.add(loanCode);
				if("A".equals(estatus)){//未结算标号
					undoneEarlyLoanCodes.add(loanCode);
				}
			}
			
			if("C".equals(estatus)){//已提前还款
				if(loanCodes.indexOf(loanCode) != -1 && Integer.valueOf(date) > Integer.valueOf(earlyDate)){//原结算日期还款标已提前还款
					loanCodes.remove(loanCode);//移除已提前还款标号
				}
			}
			if("A".equals(estatus)){//准备提前还款
				if(loanCodes.indexOf(loanCode) != -1 && Integer.valueOf(date) > Integer.valueOf(earlyDate)){//原定结算日期还款,已设置其他日期提前还款
					loanCodes.remove(loanCode);//移除标号
				}
			}
		}
		
		long sumRepaymentAmount = 0;//当日预计还款金额(本金+利息)
		long sumInterestFee = 0;//当日预计收取手续费
		
		//当日正常还款资金统计
		for(LoanRepayment loanRepayment:loanRepayments){
			String loanCode = loanRepayment.getStr("loanCode");
			if(loanCodes.indexOf(loanCode) == -1){
				continue;
			}
			if(earlyLoanCodes.indexOf(loanCode) != -1){
				continue;
			}
			long repaymentAmount = loanRepayment.getLong("repaymentAmount");
			long interestFee = loanRepayment.getLong("interestFee");
			
			sumRepaymentAmount += repaymentAmount;
			sumInterestFee += interestFee;
			
		}
		
		//当日提前还款资金统计
		for(String loanCode:earlyLoanCodes){
			LoanInfo loanInfo = loanInfoService.findById(loanCode);
			int reciedCount = loanInfo.getInt("reciedCount");
			String loanState = loanInfo.getStr("loanState");
			List<LoanRepayment> queryByLoanCode = loanRepaymentService.queryByLoanCode(loanCode);
			for (LoanRepayment loanRepayment : queryByLoanCode) {
				String repaymentDate = loanRepayment.getStr("repaymentDate");
				int recyPeriod = loanRepayment.getInt("recyPeriod");
				if(Integer.valueOf(date) <= Integer.valueOf(repaymentDate)){
					long repaymentPrincipal = loanRepayment.getLong("repaymentPrincipal");
					long repaymentInterest = loanRepayment.getLong("repaymentInterest");
					long interestFee = loanRepayment.getLong("interestFee");
					sumRepaymentAmount += repaymentPrincipal;
					
					//利息只统计下一期利息
					if(("P".equals(loanState) && reciedCount+1 == recyPeriod) || ("N".equals(loanState) && reciedCount+2 == recyPeriod)){
						sumRepaymentAmount += repaymentInterest;
						sumInterestFee += interestFee;
						
					}
				}
			}
		}
		
		long sumActualRepaymentAmount = 0;//当日实际还款金额(本金+利息)
		long sumActualInterestFee = 0;//当日实际收取手续费
		
		List<LoanRepayment> queryByRepaymentYesDate = loanRepaymentService.queryByRepaymentYesDate(txDate);
		for (LoanRepayment loanRepayment : queryByRepaymentYesDate) {
			long repaymentYesAmount = loanRepayment.getLong("repaymentYesAmount");
			long yesInterestFee = loanRepayment.getLong("yesInterestFee");
			int recyStatus = loanRepayment.getInt("recyStatus");
			
			if(recyStatus != -1 && recyStatus != 0){//还款成功
				sumActualRepaymentAmount += repaymentYesAmount;
				sumActualInterestFee += yesInterestFee;
			}
		}
		
		
		//正常还款明日资金统计
		List<LoanTrace> nextLoanTraces = loanTraceService.queryByLoanRecyDate(DateUtil.addDay(date, 1));//明日正常还款投标流水
		for (LoanTrace loanTrace : nextLoanTraces) {
			String authCode = loanTrace.getStr("authCode");
			String loanCode = loanTrace.getStr("loanCode");
			
			if(nextEarlyLoanCodes.indexOf(loanCode) != -1){
				continue;
			}
			
			if(StringUtil.isBlank(authCode)){//红包还款
				String payUserCode = loanTrace.getStr("payUserCode");
				String accountId = "";
				if(accountIdMap.containsKey(payUserCode)){
					accountId = accountIdMap.get(payUserCode);
				}else {
					User user = userService.findById(payUserCode);
					accountId = user.getStr("jxAccountId");
					accountIdMap.put(payUserCode, accountId);
				}
				
				if(StringUtil.isBlank(accountId)){
					continue;
				}
				
				sumNextVoucherAmount += loanTrace.getLong("nextAmount")+loanTrace.getLong("nextInterest");
			}else {//批次还款
				sumNextBatchAmount += loanTrace.getLong("nextAmount")+loanTrace.getLong("nextInterest");
			}
		}
		
		List<String> notBatchEarlyLoanCodes = new ArrayList<String>();//当日未发批次提前还款标号
		for (String loanCode : earlyLoanCodes) {
			List<JXTrace> jxTraces = jxTraceService.queryLoanTrace(txDate, loanCode);
			List<LoanTrace> loanTraces = loanTraceService.queryLoanTraces(loanCode);//该标含有authCode的投标流水
			if(jxTraces == null || jxTraces.size() < 1 && loanTraces != null && loanTraces.size() > 0){
				notBatchEarlyLoanCodes.add(loanCode);
			}
		}
		
		List<JXTrace> queryBatchSubstRepayByTxDate = jxTraceService.queryBatchSubstRepayByTxDate(txDate);//当日已发代偿批次
		List<String> failBatchNos = new ArrayList<String>();//当日失败批次号
		List<String> aisleBatchNos = new ArrayList<String>();//当日通道异常已处理批次号
		List<String> thawBatchNos = new ArrayList<String>();//当日批次资金未解冻批次号
		long sumBatchAmount = 0;//当日已发批次代偿交易成功总金额
		long sumTxFeeIn = 0;//当日已发批次代偿交易成功总手续费
		for (JXTrace jxTrace : queryBatchSubstRepayByTxDate) {
			String requestMessage = jxTrace.getStr("requestMessage");
			String responseMessage = jxTrace.getStr("responseMessage");
			String remark = jxTrace.getStr("remark");
			if(StringUtil.isBlank(requestMessage)){
				continue;
			}
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String batchNo = parseObject.getString("batchNo");
			String retCode = jxTrace.getStr("retCode");
			if(!StringUtil.isBlank(retCode) && !"00000000".equals(retCode)){
				if(failBatchNos.indexOf(batchNo) == -1){
					failBatchNos.add(batchNo);
				}
			}
			//响应报文为空,发送批次通道异常或即信未回调
			if(StringUtil.isBlank(responseMessage)){
				if(failBatchNos.indexOf(batchNo) == -1){
					failBatchNos.add(batchNo);
				}
			}
			int jxTraceState = jxTraceState(jxTrace);
			if(jxTraceState == 4 || jxTraceState == 5 || jxTraceState == 7 || jxTraceState == 8){//批次交易失败
				if(failBatchNos.indexOf(batchNo) == -1){
					failBatchNos.add(batchNo);
				}
			}
			//部分成功
			if(jxTraceState == 5){
				ArrayList<String> sucAuthCode = new ArrayList<String>();//成功authCode
				Map<String, Object> batchDetailsQueryAll = JXQueryController.batchDetailsQueryAll(txDate, batchNo, "1");
				if(batchDetailsQueryAll == null){
					return error("", "批次代偿信息查询失败", null);
				}
				String queryAllRetCode = String.valueOf(batchDetailsQueryAll.get("retCode"));//响应代码
				String queryAllRetMsg = String.valueOf(batchDetailsQueryAll.get("retMsg"));//响应描述
				if(!"00000000".equals(queryAllRetCode)){
					return error("", queryAllRetMsg, null);
				}
				List<Map<String, String>> quereyAllSubPacks = (List<Map<String, String>>) batchDetailsQueryAll.get("subPacks");
				for (Map<String, String> subPack : quereyAllSubPacks) {
					String authCode = subPack.get("authCode");
					sucAuthCode.add(authCode);
				}
				
				String subPacks = parseObject.getString("subPacks");
				JSONArray parseArray = JSONArray.parseArray(subPacks);
				for (int i = 0; i < parseArray.size(); i++) {
					JSONObject jsonObject = parseArray.getJSONObject(i);
					String authCode = jsonObject.getString("authCode");
					if(sucAuthCode.indexOf(authCode) != -1){
						long txAmount = StringUtil.getMoneyCent(jsonObject.getString("txAmount"));
						long intAmount = StringUtil.getMoneyCent(jsonObject.getString("intAmount"));
						long txFeeIn = StringUtil.getMoneyCent(StringUtil.isBlank(jsonObject.getString("txFeeIn"))?"0":jsonObject.getString("txFeeIn"));
						sumBatchAmount += txAmount+intAmount;
						sumTxFeeIn += txFeeIn;
					}
				}
			}
			//全部成功
			if(jxTraceState == 6){
				String subPacks = parseObject.getString("subPacks");
				JSONArray parseArray = JSONArray.parseArray(subPacks);
				for (int i = 0; i < parseArray.size(); i++) {
					JSONObject jsonObject = parseArray.getJSONObject(i);
					long txAmount = StringUtil.getMoneyCent(jsonObject.getString("txAmount"));
					long intAmount = StringUtil.getMoneyCent(jsonObject.getString("intAmount"));
					long txFeeIn = StringUtil.getMoneyCent(StringUtil.isBlank(jsonObject.getString("txFeeIn"))?"0":jsonObject.getString("txFeeIn"));
					sumBatchAmount += txAmount+intAmount;
					sumTxFeeIn += txFeeIn;
				}
			}
			if(!"y".equals(remark) && !"JX900664".equals(retCode) && !StringUtil.isBlank(responseMessage)){
				thawBatchNos.add(batchNo);
			}
			if(("JX900664".equals(retCode) || StringUtil.isBlank(responseMessage)) && "y".equals(remark)){
				aisleBatchNos.add(batchNo);
			}
		}
		int sendBatchNum = queryBatchSubstRepayByTxDate.size();//当日已发送批次数量
		
		List<String> batchLoanCodes = new ArrayList<String>();//当日应发批次标号
		batchLoanCodes.addAll(notBatchLoanCodes);
		batchLoanCodes.addAll(notBatchEarlyLoanCodes);
		List<String> errorLoanCodes = new ArrayList<String>();//批次发送日错误标号
		
		for (JXTrace jxTrace : queryBatchSubstRepayByTxDate) {
			String requestMessage = jxTrace.getStr("requestMessage");
			JSONObject parseObject = JSONObject.parseObject(requestMessage);
			String subPacks = parseObject.getString("subPacks");
			JSONArray parseArray = JSONArray.parseArray(subPacks);
			for (int i = 0; i < parseArray.size(); i++) {
				JSONObject jsonObject = parseArray.getJSONObject(i);
				String productId = jsonObject.getString("productId");//标号
				if(loanCodes.indexOf(productId) != -1 || earlyLoanCodes.indexOf(productId) != -1){//当日已发送还款标号
					if(batchLoanCodes.indexOf(productId) == -1){
						batchLoanCodes.add(productId);
					}
				}else{
					if(errorLoanCodes.indexOf(productId) == -1){
						errorLoanCodes.add(productId);
					}
				}
			}
		}
		
		int batchNum = batchLoanCodes.size();//当日应发批次数量
		List<LoanTrace> loanTraceByLengths = loanTraceService.queryLoanTraceByLength(150);
		for (LoanTrace loanTrace : loanTraceByLengths) {
			String loanCode = loanTrace.getStr("loanCode");
			if(batchLoanCodes.indexOf(loanCode) != -1){
				Long num = loanTrace.getLong("num");
				batchNum += num%150 == 0?num/150-1:num/150;
			}
		}
		
		int voucherPaySucNum = 0;//当日红包发放成功数量
		int voucherPayFailNum = 0;//失败未处理红包数量
		long sumSucVoucherAmount = 0;//当日红包发放成功总金额
		List<JXTrace> voucherPays = jxTraceService.queryRepaymentVoucherPay(txDate);//还款、返佣红包记录(含补发红包)
		for (JXTrace jxTrace : voucherPays) {
			String retCode = jxTrace.getStr("retCode");
			if("00000000".equals(retCode)){//发放成功
				voucherPaySucNum++;
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				sumSucVoucherAmount += StringUtil.getMoneyCent(parseObject.getString("txAmount"));
			}
			if(StringUtil.isBlank(retCode)){//未回调,但发送成功
				String orgTxDate = jxTrace.getStr("txDate");
				String orgTxTime = jxTrace.getStr("txTime");
				String orgSeqNo = jxTrace.getStr("seqNo");
				Map<String, String> fundTransQuery = JXQueryController.fundTransQuery(JXService.RED_ENVELOPES, orgTxDate, orgTxTime, orgSeqNo);
				if("00000000".equals(fundTransQuery.get("retCode")) && "0".equals(fundTransQuery.get("orFlag"))  && "00".equals(fundTransQuery.get("result"))){
					voucherPaySucNum++;
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject parseObject = JSONObject.parseObject(requestMessage);
					sumSucVoucherAmount += StringUtil.getMoneyCent(parseObject.getString("txAmount"));
				}
			}
			if(!"00000000".equals(retCode) && !"y".equals(jxTrace.getStr("remark"))){//失败未处理
				voucherPayFailNum++;
			}
		}
		
		//逾期信息统计
		//当日应回总额,标的数
		Long[] sumAmountAndCount = loanInfoService.sumAmountAndCount(date,1);
		Long[] sumEarlyAmountAndCount = loanInfoService.sumEarlyAmountAndCount(date, 1);
		long sumAmount = sumAmountAndCount[0]+sumEarlyAmountAndCount[0];//当日应回总额
		long countLoan = sumAmountAndCount[1]+sumEarlyAmountAndCount[1];//当日应回标的数
		
		//当日实际正常回收总额,标的数
		Long[] sumYesAmountAndCount = loanInfoService.sumAmountAndCount(date,2);
		Long[] sumYesEarlyAmountAndCount = loanInfoService.sumEarlyAmountAndCount(date, 2);
		long sumYesAmount = sumYesAmountAndCount[0]+sumYesEarlyAmountAndCount[0];//当日实际回收总额
		long countYesLoan = sumYesAmountAndCount[1]+sumYesEarlyAmountAndCount[1];//当日实际回收标的数
		
		//当日逾期垫付利息
		Long[] sumAdvanceInterestAndCount = loanInfoService.sumAdvanceInterestAndCount(date);
		long sumAdvanceInterest = sumAdvanceInterestAndCount[0];//当日垫付利息总额
		long countAdvanceInterest = sumAdvanceInterestAndCount[1];//当日垫付利息标的数
		
		//当日逾期回款标总额,标的数
		long sumYesOverdueAmount = 0;//当日实际逾期还款总额
		List<String> loanOverdueLoanCodes = overdueTraceService.queryBydisposeStatusAndEndDate("n", DateUtil.getNowDate());
		int countYesOverdue = loanOverdueLoanCodes.size();//当日实际逾期还款标的数
		for (String loanCode : loanOverdueLoanCodes) {
			List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
			int countAuthCode = 0;//存在authCode投标条数
			for (LoanTrace loanTrace : loanTraces) {
				long overdueAmount = loanTrace.getLong("overdueAmount");
				String authCode = loanTrace.getStr("authCode");
				
				sumYesOverdueAmount += overdueAmount;
				if(!StringUtil.isBlank(authCode)){
					countAuthCode++;
				}
			}
			batchNum += countAuthCode%150 == 0?countAuthCode/150:countAuthCode/150+1;
		}
		
		//还款短信
		long readySMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 8);//准备发送中短信数量
		long soonSMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 9);//准备发送完成短信数量
		long sucSMSNum = smsLogService.countByDateAndTypeAndStatus(txDate, 10, 0);//发送完成短信数量
		
		Map<String, Object> loanMap = new HashMap<String,Object>();//当日还款信息
		//还款标信息
		loanMap.put("loan", loanCodes.size()-undoneLoanCodes.size()+"/"+loanCodes.size());//正常还款标(已结算/应结算)
		loanMap.put("earlyLoan", earlyLoanCodes.size()-undoneEarlyLoanCodes.size()+"/"+earlyLoanCodes.size());//提前还款标(已结算/应结算)
		loanMap.put("repaymentAmount", StringUtil.getMoneyYuan(sumActualRepaymentAmount)+"元/"+StringUtil.getMoneyYuan(sumRepaymentAmount)+"元");//当日还款(实际还款/预估还款)
		loanMap.put("interestFee", StringUtil.getMoneyYuan(sumActualInterestFee)+"元/"+StringUtil.getMoneyYuan(sumInterestFee)+"元");//当日手续费(实际收取手续费/预估手续费)
		
		//还款批次信息
		loanMap.put("batch", sendBatchNum+"/"+batchNum);//当日批次数量(实发/应发)
		loanMap.put("failBatchNos", failBatchNos);//当日失败批次
		loanMap.put("aisleBatchNos", aisleBatchNos);//当日通道异常已处理批次号
		loanMap.put("thawBatchNos", thawBatchNos);//当日批次资金未解冻批次
		loanMap.put("sumBatchAmount", StringUtil.getMoneyYuan(sumBatchAmount));//当日批次代偿成功交易总金额
		loanMap.put("sumTxFeeIn", StringUtil.getMoneyYuan(sumTxFeeIn));//当日批次代偿成功交易总手续费
		loanMap.put("sumNextBatchAmount", StringUtil.getMoneyYuan(sumNextBatchAmount));//明日代偿还款预计总金额
		
		//还款红包信息
		loanMap.put("voucherPaySucNum", voucherPaySucNum);//当日实发还款红包数量(含返佣)
		loanMap.put("sumSucVoucherAmount", StringUtil.getMoneyYuan(sumSucVoucherAmount));//当日红包成功发放总金额
		loanMap.put("voucherPayFailNum", voucherPayFailNum);//当日还款红包发送失败未处理数量
		loanMap.put("sumNextVoucherAmount", StringUtil.getMoneyYuan(sumNextVoucherAmount));//明日红包还款预计总金额
		
		//回款短信
		loanMap.put("SMS", sucSMSNum+"/"+(sucSMSNum+readySMSNum+soonSMSNum));//还款短信数量(已发/应发)
		
		//回款前逾期信息
		loanMap.put("sumAmount", StringUtil.getMoneyYuan(sumAmount));//当日应回总额
		loanMap.put("countLoan", countLoan);//当日应回标的数
		loanMap.put("sumYesAmount", StringUtil.getMoneyYuan(sumYesAmount));//当日实际回收总额
		loanMap.put("countYesLoan", countYesLoan);//当日实际回收标的数
		loanMap.put("sumYesOverdueAmount", StringUtil.getMoneyYuan(sumYesOverdueAmount));//当日实际逾期还款总额
		loanMap.put("countYesOverdue", countYesOverdue);//当日实际逾期还款标的数
		loanMap.put("sumAdvanceInterest", StringUtil.getMoneyYuan(sumAdvanceInterest));//当日垫付利息总额
		loanMap.put("countAdvanceInterest", countAdvanceInterest);//当日垫付利息标的数
		
		loanMap.put("opCode", getUserCode());
		return succ("",loanMap);
	}
	
	/**
	 * 查询当日标还款列表信息 WJW
	 * @return
	 */
	@ActionKey("/repaymentByDayList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message repaymentByDayList(){
		String date = getPara("date",DateUtil.getNowDate()).replaceAll("-", "");//标还款日期
		String txDate = getPara("txDate",date);//结算日期(批次,红包发送日期)
		String allkey = getPara("allkey","");//关键字
		Integer pageNumber = getParaToInt("pageNumber",1);
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		Integer pageSize = getParaToInt("pageSize",10);
		pageSize = pageSize > 10 ? 10 : pageSize;
		
		List<String> allkeyLoanCodes = new ArrayList<String>();//模糊查询标号
		if(!StringUtil.isBlank(allkey) && StringUtil.isNumeric(allkey)){
			if(allkey.length() == 6){//allkey可能为loanNo
				List<LoanInfo> loanInfos= loanInfoService.findByLoanNo(allkey);
				for (LoanInfo loanInfo : loanInfos) {
					String loanCode = loanInfo.getStr("loanCode");
					allkeyLoanCodes.add(loanCode);
				}
			}
			if(allkey.length() <= 5){//allkey可能为batchNo
				JXTrace jxTrace = jxTraceService.findByBatchNoAndTxDate(txDate, allkey);
				String txCode = jxTrace.getStr("txCode");
				if("batchSubstRepay".equals(txCode)){
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject parseObject = JSONObject.parseObject(requestMessage);
					String subPacks = parseObject.getString("subPacks");
					JSONArray parseArray = JSONArray.parseArray(subPacks);
					for (int i = 0; i < parseArray.size(); i++) {
						JSONObject jsonObject = parseArray.getJSONObject(i);
						String productId = jsonObject.getString("productId");
						if(allkeyLoanCodes.indexOf(productId) == -1){
							allkeyLoanCodes.add(productId);
						}
					}
				}
			}
		}
		
		List<String> loanCodeAlls = new ArrayList<String>();
		
		if(allkeyLoanCodes.size()>0){//关键字查询
			loanCodeAlls = allkeyLoanCodes;
		}else {//查询全部
			//正常还款
			List<String> loanCodes = new ArrayList<String>();//还款日正常还款标号
			List<LoanRepayment> loanRepayments = loanRepaymentService.queryByRepaymentDate(date);//还款日正常还款债权信息
			for (LoanRepayment loanRepayment : loanRepayments) {
				String loanCode = loanRepayment.getStr("loanCode");
				if(loanCodes.indexOf(loanCode) == -1){
					loanCodes.add(loanCode);
				}
			}
			
			List<String> notBatchLoanCodes = loanTraceService.queryLoanCodeByLoanRecyDate(date);//当日未发批次标号(不含提前还款)
			List<LoanInfo> loanInfos = loanInfoService.findByJLSJ(date);//当日满标信息
			for (LoanInfo loanInfo : loanInfos) {
				String loanCode = loanInfo.getStr("loanCode");
				if(notBatchLoanCodes.indexOf(loanCode) != -1){
					notBatchLoanCodes.remove(loanCode);//剔除当日满标标号
				}
			}
			
			//提前还款
			List<String> earlyLoanCodes = new ArrayList<String>();//当日提前还款标号
			List<String> undoneEarlyLoanCodes = new ArrayList<String>();//当日提前还款未结算标号
			List<SettlementEarly> SettlementEarlys = settlementEarlyService.queryEarlyAll();//提前还款标
			for (SettlementEarly settlementEarly : SettlementEarlys) {
				String loanCode = settlementEarly.getStr("loanCode");
				String earlyDate = settlementEarly.getStr("earlyDate");
				String estatus = settlementEarly.getStr("estatus");
				
				if(date.equals(earlyDate)){//当日提前还款标号
					earlyLoanCodes.add(loanCode);
					if("A".equals(estatus)){//未结算标号
						undoneEarlyLoanCodes.add(loanCode);
					}
				}
				
				if("C".equals(estatus)){//已提前还款
					if(loanCodes.indexOf(loanCode) != -1 && Integer.valueOf(date) > Integer.valueOf(earlyDate)){//原结算日期还款标已提前还款
						loanCodes.remove(loanCode);//移除已提前还款标号
					}
				}
				if("A".equals(estatus)){//准备提前还款
					if(loanCodes.indexOf(loanCode) != -1 && Integer.valueOf(date) > Integer.valueOf(earlyDate)){//原定结算日期还款,已设置其他日期提前还款
						loanCodes.remove(loanCode);//移除标号
					}
				}
			}
			
			List<JXTrace> queryBatchSubstRepayByTxDate = jxTraceService.queryBatchSubstRepayByTxDate(txDate);//当日已发代偿批次
			List<String> notBatchEarlyLoanCodes = new ArrayList<String>();//当日未发批次提前还款标号
			for (String loanCode : earlyLoanCodes) {
				List<JXTrace> jxTraces = jxTraceService.queryLoanTrace(txDate, loanCode);
				List<LoanTrace> loanTraces = loanTraceService.queryLoanTraces(loanCode);//该标含有authCode的投标流水
				if(jxTraces == null || jxTraces.size() < 1 && loanTraces != null && loanTraces.size() > 0){
					notBatchEarlyLoanCodes.add(loanCode);
				}
			}
			
			List<String> batchLoanCodes = new ArrayList<String>();//当日已发批次标号
			List<String> errorLoanCodes = new ArrayList<String>();//批次发送日错误标号
			
			for (JXTrace jxTrace : queryBatchSubstRepayByTxDate) {
				String requestMessage = jxTrace.getStr("requestMessage");
				JSONObject parseObject = JSONObject.parseObject(requestMessage);
				String subPacks = parseObject.getString("subPacks");
				JSONArray parseArray = JSONArray.parseArray(subPacks);
				for (int i = 0; i < parseArray.size(); i++) {
					JSONObject jsonObject = parseArray.getJSONObject(i);
					String productId = jsonObject.getString("productId");//标号
					if(loanCodes.indexOf(productId) != -1 || earlyLoanCodes.indexOf(productId) != -1){//当日已发送还款标号
						if(batchLoanCodes.indexOf(productId) == -1){
							batchLoanCodes.add(productId);
						}
					}else{
						if(errorLoanCodes.indexOf(productId) == -1){
							errorLoanCodes.add(productId);
						}
					}
				}
			}
			
			loanCodeAlls.addAll(loanCodes);
			loanCodeAlls.addAll(earlyLoanCodes);
		}
		
		int num = loanCodeAlls.size() > pageNumber * pageSize ? pageNumber * pageSize:loanCodeAlls.size();
		
		List<String> loanCodeSubs = loanCodeAlls.subList((pageNumber - 1) * pageSize, num);
		
		JSONArray jsonArray = new JSONArray();
		for (String loanCode : loanCodeSubs) {
			List<JXTrace> jxTraces = jxTraceService.queryLoanTrace(txDate, loanCode);
			if(jxTraces.size()> 0){
				for (JXTrace jxTrace : jxTraces) {
					JSONObject jsonObject = new JSONObject();
					LoanInfo loanInfo = loanInfoService.findById(loanCode);
					jsonObject.put("loanNo", loanInfo.getStr("loanNo"));
					jsonObject.put("loanTitle", loanInfo.getStr("loanTitle"));
					jsonObject.put("reciedCount", loanInfo.getInt("reciedCount"));
					jsonObject.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
					String loanState = loanInfo.getStr("loanState");
					if("P".equals(loanState)){
						jsonObject.put("loanState", "已提前还款");
					}else if("N".equals(loanState)){
						jsonObject.put("loanState", "还款中");
					}else if("O".equals(loanState)){
						jsonObject.put("loanState", "已还完");
					}
					String requestMessage = jxTrace.getStr("requestMessage");
					JSONObject parseObject = JSONObject.parseObject(requestMessage);
					String batchNo = parseObject.getString("batchNo");
					jsonObject.put("batchNo", batchNo);
					jsonObject.put("txDate", txDate);
					
					int jxTraceState = jxTraceState(jxTrace);
					if("y".equals(jxTrace.getStr("remark"))){
						jsonObject.put("jxTraceState", "失败已处理");
					}else if(jxTraceState == 4){
						jsonObject.put("jxTraceState", "未通过数据合法校验");
					}else if(jxTraceState == 5){
						jsonObject.put("jxTraceState", "部分成功");
					}else if(jxTraceState == 6){
						jsonObject.put("jxTraceState", "全部成功");
					}else if(jxTraceState == 7){
						jsonObject.put("jxTraceState", "全部失败");
					}else {
						jsonObject.put("jxTraceState", "未处理");
					}
					jsonArray.add(jsonObject);
				}
			}else {
				JSONObject jsonObject = new JSONObject();
				LoanInfo loanInfo = loanInfoService.findById(loanCode);
				jsonObject.put("loanNo", loanInfo.getStr("loanNo"));
				jsonObject.put("loanTitle", loanInfo.getStr("loanTitle"));
				jsonObject.put("reciedCount", loanInfo.getInt("reciedCount"));
				jsonObject.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
				String loanState = loanInfo.getStr("loanState");
				if("P".equals(loanState)){
					jsonObject.put("loanState", "已提前还款");
				}else if("N".equals(loanState)){
					jsonObject.put("loanState", "还款中");
				}else if("O".equals(loanState)){
					jsonObject.put("loanState", "已还完");
				}
				jsonObject.put("batchNo", "--");
				jsonObject.put("txDate", "--");
				jsonArray.add(jsonObject);
			}
			
		}
		
		int totalPage = loanCodeAlls.size()%pageSize == 0 ? loanCodeAlls.size()/pageSize:loanCodeAlls.size()/pageSize+1;
		Page<Object> page = new Page<>(jsonArray, pageNumber, pageSize, totalPage, loanCodeAlls.size());
		return succ("查询成功", page);
		
	}
	
	/**
	 * 判断批次交易状态 WJW
	 * @param jxTrace
	 * @return	1:请求未发送(或江西未实时响应) 2:未收到任何异步通知 3:通过数据合法校验 4:未通过数据合法校验 5:部分成功 6:全部成功 7:全部失败 8:响应报文存的什么鬼
	 */
	public int jxTraceState(JXTrace jxTrace){
		//读取t_jx_trace流水号对应交易响应报文,进行本地流水处理
		if(jxTrace == null){//批量迁移
			return 6;
		}
		
		String responseMessage = jxTrace.getStr("responseMessage");//响应报文
		
		if("admin".equals(responseMessage)){//已人工处理
			return 6;
		}
		
		JSONObject parseObject = null;
		try {
			parseObject = JSONObject.parseObject(responseMessage);
		} catch (Exception e) {
			return 8;
		}
		
		if(StringUtil.isBlank(responseMessage)){
			return 1;
		}
		
		String received = parseObject.getString("received");//success接收成功
		if(!StringUtil.isBlank(received)){
			return 2;
		}
		
		String txCounts = parseObject.getString("txCounts");//交易笔数(业务处理结果的异步通知)
		String retCode = parseObject.getString("retCode");//响应代码(业务处理结果的异步通知)
		if(!StringUtil.isBlank(txCounts) && !StringUtil.isBlank(retCode)){
			if("00000000".equals(retCode)){//通过数据合法校验
				return 3;
			}else {//未通过数据合法校验
				return 4;
			}
		}
		
		int sucCounts = 0;//成功交易笔数
		double failAmount = 0;//失败交易金额
		int failCounts =0;//失败交易笔数
		if(!StringUtil.isBlank(parseObject.getString("sucCounts"))){
			sucCounts = Integer.valueOf(parseObject.getString("sucCounts"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failAmount"))){
			failAmount = Double.valueOf(parseObject.getString("failAmount"));
		}
		if(!StringUtil.isBlank(parseObject.getString("failCounts"))){
			failCounts = Integer.valueOf(parseObject.getString("failCounts"));
		}
		
		if(sucCounts>0 && (failAmount>0 || failCounts>0)){
			return 5;//部分成功
		}
		if(sucCounts>0 && (failAmount==0 || failCounts==0)){
			return 6;//全部成功
		}
		if(sucCounts==0 && (failAmount>0 || failCounts>0)){
			return 7;//全部失败
		}
		if(sucCounts==0 && (failAmount==0 || failCounts==0)){
			return 8;//响应报文存的什么鬼
		}
		return 0;
	}
	
	/**
	 * 设置逾期
	 * */
	@ActionKey("/setOverdue")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public synchronized Message setOverdue(){
		String loanCode = getPara("loanCode");
		String overdueType = getPara("overdueType");
		List<LoanOverdue> loanOverdues = overdueTraceService.findByLoanCode(loanCode, "n", null);
		if(loanOverdues != null&&loanOverdues.size() > 0){
			return error("01", "此逾期标已有设置成功未处理，请确认", "");
		}
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		if(null == loanInfo){
			return error("02", "未找到此标，请确认", "");
		}
		if(!"N".equals(loanInfo.getStr("loanState"))){
			return error("03", "此标状态不为还款中", "");
		}
		if(StringUtil.isBlank(overdueType)){
			return error("04", "逾期类型为空", "");
		}
		try {
			loanOverdueType.valueOf(overdueType);
		} catch (Exception e) {
			return error("05", "逾期类型不存在", "");
		}
		
		//取消债转
		List<LoanTransfer> loanTransfers = loanTransferService.queryList4loanCode(loanCode, "A");
		for(int i=0;i<loanTransfers.size();i++){
			LoanTransfer loanTransfer = loanTransfers.get(i);
			String userCode = loanTransfer.getStr("payUserCode");
			String transferCode = loanTransfer.getStr("transCode");
			LoanTransfer cancelLoanTransfer = new LoanTransfer();
			cancelLoanTransfer.set("transCode", transferCode);
			cancelLoanTransfer.set("transState", "C");
			boolean updateResult = false;
			try{
				updateResult = cancelLoanTransfer.update();
			}catch(Exception e){
				return error("11", "债权转让取消失败", "");
			}
			
			if( updateResult == false ){
				return error("12", "债权取消失败", "");
			}
			//修改标书债权状态
			boolean updateTraceState = false;
			if(loanTransferService.vilidateIsTransfer(loanTransfer.getStr("traceCode"))){
				updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
						"A", "B");
			}else{
				updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
						"A", "C");
			}
			if(updateTraceState == false){
				return error("13", "债权取消成功,但标书债权状态修改异常", "");
			}
			
			//回滚积分
			int transScore = loanTransfer.getInt("transScore");
//			fundsServiceV2.doPoints(userCode, 0 , transScore , "取消债权转让,返回冻结积分!");
		}
		
		if(loanOverdueType.I == loanOverdueType.valueOf(overdueType)){//逾期垫付利息
			//记录当前逾期利息
			List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
			for(int i = 0;i<loanTraces.size();i++){
				LoanTrace loanTrace = loanTraces.get(i);
				loanTrace.set("overdueInterest", loanTrace.getLong("nextInterest"));
				loanTrace.update();
			}
		}
		
		Map<String, Object> loanOverdueMap = new HashMap<String, Object>();
		loanOverdueMap.put("loanCode", loanInfo.getStr("loanCode"));
		loanOverdueMap.put("loanNo", loanInfo.getStr("loanNo"));
		loanOverdueMap.put("loanUserCode", loanInfo.getStr("userCode"));
		loanOverdueMap.put("loanUserName", loanInfo.getStr("userName"));
		loanOverdueMap.put("loanAmount", loanInfo.getLong("loanAmount"));
		loanOverdueMap.put("loanTitle", loanInfo.getStr("loanTitle"));
		loanOverdueMap.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
		loanOverdueMap.put("repayIndex", loanInfo.getInt("reciedCount")+1);
		long nextAmount = loanTraceService.sumNextAmount(loanCode);
		loanOverdueMap.put("principal", nextAmount);
		loanOverdueMap.put("interest", 0);
		loanOverdueMap.put("overdueAmount", nextAmount);
		loanOverdueMap.put("refundType", loanInfo.getStr("refundType"));
		loanOverdueMap.put("overdueType", overdueType);//逾期类型
		loanOverdueMap.put("overdueDate", DateUtil.getNowDate());
		loanOverdueMap.put("overdueTime", DateUtil.getNowTime());
		loanOverdueMap.put("remark", "");
		
		boolean result = overdueTraceService.save(loanOverdueMap);
		if(result){
			return succ("OK", "标号："+loanInfo.getStr("loanNo")+",设置逾期成功");
		}else {
			return error("04", "设置失败", "");
		}
	}
	/**
	 * 取消逾期，待隔日还款
	 * */
	@ActionKey("/cancelOverdue")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message cancelOverdue(){
		String loanCode = getPara("loanCode");
		List<LoanOverdue> loanOverdues = overdueTraceService.findByLoanCode(loanCode, "n", null);
		if(loanOverdues==null||loanOverdues.size()==0){
			return error("01", "此标还未设置逾期，请确认", "");
		}
		LoanInfo loanInfo = loanInfoService.findById(loanCode);
		if(null == loanInfo){
			return error("02", "未找到此标，请确认", "");
		}
//		if(!"N".equals(loanInfo.getStr("loanState"))){
//			return error("03", "此标状态不为还款中", "");
//		}
		//清除当前逾期利息
//		List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
//		for(int i = 0;i<loanTraces.size();i++){
//			LoanTrace loanTrace = loanTraces.get(i);
//			loanTrace.set("overdueInterest", 0);
//			loanTrace.update();
//		}
		boolean result = overdueTraceService.updateDisposeDate(loanCode, DateUtil.getNowDateTime(), "");
		if(result){
			return succ("OK", "标号："+loanInfo.getStr("loanNo")+",取消逾期成功");
		}else{
			return error("04", "设置失败", "");
		}
	}
	
//	/**
//	 * 逾期列表下载 WJW
//	 */
//	@ActionKey("/overdueExportFile")
//	@AuthNum(value=999)
//	@Before({PkMsgInterceptor.class})
//	public void overdueExportFile(){
//		String date = getPara("date",DateUtil.getNowDate());//逾期追回日期
//		if(StringUtil.isBlank(date) || date.length() != 8 || !StringUtil.isNumeric(date)){
//			renderJson(error("", "日期输错了,别打瞌睡了!!!", null));
//			return;
//		}
//		
//		String disposeDate = Long.valueOf("20180825") < Long.valueOf(DateUtil.getNowDate()) ?DateUtil.addDay(date, 1) : DateUtil.addDay(date, 2);//逾期追回日期
//		//当日逾期回款标总额,标的数
//		List<LoanOverdue> queryYesByDate = overdueTraceService.queryByDisposeDate(disposeDate);
//		JSONArray jsonArray = new JSONArray();
//		for (LoanOverdue loanOverdue : queryYesByDate) {
//			String loanCode = loanOverdue.getStr("loanCode");
//			int repayIndex = loanOverdue.getInt("repayIndex");
//			String disposeDateTime = loanOverdue.getStr("disposeDateTime");
//			List<HistoryRecy> historyRecies = historyRecyService.queryByLoanCodeAndRecyLimit(loanCode, repayIndex);
//			if(historyRecies == null || historyRecies.size() == 0){//历史还款记录不存在,仍为有效逾期
//				long overdueAmount = loanOverdue.getInt("overdueAmount");
//				String loanNo = loanOverdue.getStr("loanNo");
//				
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("disposeDate", date);
//				jsonObject.put("loanNo", loanNo);
//				jsonObject.put("overdueAmount", StringUtil.getMoneyYuan(overdueAmount));
//				jsonArray.add(jsonObject);
//				continue;
//			}
//			HistoryRecy historyRecy = historyRecies.get(0);
//			String recyDate = historyRecy.getStr("recyDate");
//			String recyTime = historyRecy.getStr("recyTime");
//			if(Long.valueOf(recyDate+recyTime) < Long.valueOf(disposeDateTime)){//实际还款日期在逾期还款时间之前,即产生实际逾期
//				long overdueAmount = loanOverdue.getInt("overdueAmount");
//				String loanNo = loanOverdue.getStr("loanNo");
//				
//				JSONObject jsonObject = new JSONObject();
//				jsonObject.put("disposeDate", date);
//				jsonObject.put("loanNo", loanNo);
//				jsonObject.put("overdueAmount", StringUtil.getMoneyYuan(overdueAmount));
//				jsonArray.add(jsonObject);
//			}
//		}
//		
//		try {
//			String userAgent = getRequest().getHeader("USER-AGENT");
//			String fileName = DateUtil.getStrFromDate(DateUtil.getDateFromString(date, "yyyyMMdd"), "yyyy-MM-dd")+"逾期追回列表.xls";//文件名
//			if (userAgent.contains("MSIE")) {// IE浏览器
//				fileName = URLEncoder.encode(fileName, "UTF8");
//			} else if (userAgent.contains("Mozilla")) {// google,火狐浏览器
//				fileName = new String(fileName.getBytes(), "ISO8859-1");
//			} else {
//				fileName = URLEncoder.encode(fileName, "UTF8");// 其他浏览器
//			}
//
//			HttpServletResponse response = getResponse();
//			response.reset();
//			response.addHeader("Content-Disposition", "attachment;filename="
//					+ fileName);
//			response.addHeader("Content-Length", "" + overdueExportFile(jsonArray).getBytes().length);
//			response.setContentType("application/vnd.ms-excel;charset=UTF-8;");
//			OutputStream os = new BufferedOutputStream(getResponse()
//					.getOutputStream());
//			os.write(overdueExportFile(jsonArray).getBytes("gbk"));
//			os.flush();
//			os.close();
//		} catch (Exception e) {
//		}
//		renderNull();
//	}
	
//	private String overdueExportFile(JSONArray jsonArray) throws UnsupportedEncodingException{
//		String output_html = "<table border='1'>";
//		output_html += "<tr><td>追回日期</td><td>标的编号</td><td>追回金额(元)</td></tr>";
//		for (int i = 0; i < jsonArray.size(); i++) {
//			JSONObject jsonObject = jsonArray.getJSONObject(i);
//			output_html += "<tr>";
//			output_html += "<td>"+jsonObject.getString("disposeDate")+"</td>";
//			output_html += "<td>"+jsonObject.getString("loanNo")+"</td>";
//			output_html += "<td>"+jsonObject.getString("overdueAmount")+"</td>";
//			output_html += "</tr>";
//		}
//		output_html += "</table>";
//		return output_html;
//	
//	}
	
	/**
	 * 更新回款统计表失败批次状态
	 * @return
	 */
	@ActionKey("/updateRepaymentCountBatchStatus")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message updateRepaymentCountBatchStatus(){
		String txDate = getPara("txDate",DateUtil.getNowDate());
		String batchNo = getPara("batchNo");
		if(StringUtil.isBlank(txDate) || txDate.length() != 8 || !StringUtil.isNumeric(txDate)){
			return error("", "日期输错了,别打瞌睡了!!!", null);
		}
		if(!StringUtil.isNumeric(batchNo)){
			return error("", "批次号是数字吗？", "");
		}
		RepaymentCount repaymentCount = repaymentCountService.findByRepaymentYesDate(txDate);
		String batchRecord = repaymentCount.getStr("batchRecord");
		Map<String, ?> parseObject = JSONObject.parseObject(batchRecord);
		Map<String, JSONObject> batchRecordMap = (Map<String, JSONObject>) parseObject;
		JSONObject jsonObject = batchRecordMap.get(batchNo);
		if(jsonObject == null){
			return error("", "批次不存在", "");
		}
		int status = jsonObject.getInteger("status");
		if(!(status == batchStatus.C.val() || status == batchStatus.D.val())){
			return error("", "该批次状态不是失败", "");
		}
		boolean updateBatchStatus = repaymentCountService.updateBatchStatus(txDate, batchNo, batchStatus.E);
		if(updateBatchStatus){
			return succ("设置成功", null);
		}else {
			return error("设置失败", "", null);
		}
	}
	
	@ActionKey("/exportExcelyuqi")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message exportExcel(){
		UploadFile file = getFile("importFile");
		String overdueType = getPara("overdueType");
		String fileName = file.getFileName();
		String uploadPath = file.getUploadPath();

		File file2 = new File(uploadPath+File.separator+fileName);
		List<List<String>> list = new ArrayList<List<String>>();//总共需要设置逾期的标
		List<String> list2 = new ArrayList<String>();//失败设置逾期的标
		List<String> list3 = new ArrayList<String>();//成功的笔数
		List<String> list4 = new ArrayList<String>();//一个标号对应多个标的
		InputStream in =null;
		try{
			in = new FileInputStream(file2);
			Workbook wb = Workbook.getWorkbook(in);
			Sheet s = wb.getSheet(0);//取第一个sheet工作表
			//总行数
			int rows = s.getRows();
			//列数
			int cols = s.getColumns();
			
			//循环遍历将每一行的每一列作为list的 一个元素，然后将这个list作为一个对象存放在另一个list里面
			for(int row =1;row < rows;row++){
				List<String> listyu = new ArrayList<String>();//存放每一行的数据
				for(int col = 0;col<cols;col++){
					String loanCode = s.getCell(col, row).getContents();
					listyu.add(loanCode);
				}
				list.add(listyu);
			}

			
		}catch(FileNotFoundException e){
			e.printStackTrace();
		}catch(BiffException e){
			e.printStackTrace();
			return error("78", "表格文件有问题，请将文件另存为后缀为.xls的文件后，导入另存的.xls文件即可", "");
		}catch(IOException e){
			e.printStackTrace();
		}finally{
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		overdueSetLogger.log(Level.INFO, "共有："+list.size()+"条需要设置，导入文件的时间："+DateUtil.getNowDateTime());
	
		if(!list.isEmpty() && list.size()>0){
			int count = 1;
			for(List<String> excelyu : list){//excelyu  里面存放的是excel表中每一列代表的数据，标code在第一列
				//按excel表格依次取没一列的数据
				//标号
				String loanNo= excelyu.get(0);
				overdueSetLogger.log(Level.INFO, "[" + count + "/" + list.size() + "]标的["+loanNo+"]设置逾期中...");
				List<LoanInfo> loanInfos = loanInfoService.findByLoanNo(loanNo);
				if(loanInfos.isEmpty()){
					overdueSetLogger.log(Level.INFO, "未找到此标号："+loanNo);
					list2.add(loanNo);
					continue;
				}
				if(!loanInfos.isEmpty() && loanInfos.size()>1){
					overdueSetLogger.log(Level.INFO, "此标号对应多个标的，请手动处理："+loanNo);
					list4.add(loanNo);
					list2.add(loanNo);
					continue;
				}
				LoanInfo loanInfo = loanInfos.get(0);
				String loanCode = loanInfos.get(0).getStr("loanCode");
//				String loanTitle = excelyu.get(1);
				List<LoanOverdue> loanOverdues = overdueTraceService.findByLoanCode(loanCode, "n", null);
	    		if(loanOverdues != null&&loanOverdues.size() > 0){
	    			overdueSetLogger.log(Level.INFO, "此标已有设置成功未处理："+loanCode);
	    			list2.add(loanCode);
	    			continue;
	    		}

	    		if(!"N".equals(loanInfo.getStr("loanState"))){
	    			overdueSetLogger.log(Level.INFO, "此标的状态不为还款中："+loanCode);
	    			list2.add(loanCode);
	    			continue;
	    		}
	    		
	    		//取消债转
	    		List<LoanTransfer> loanTransfers = loanTransferService.queryList4loanCode(loanCode, "A");
	    		for(int i=0;i<loanTransfers.size();i++){
	    			LoanTransfer loanTransfer = loanTransfers.get(i);
	    			String userCode = loanTransfer.getStr("payUserCode");
	    			String transferCode = loanTransfer.getStr("transCode");
	    			LoanTransfer cancelLoanTransfer = new LoanTransfer();
	    			cancelLoanTransfer.set("transCode", transferCode);
	    			cancelLoanTransfer.set("transState", "C");
	    			boolean updateResult = false;
	    			try{
	    				updateResult = cancelLoanTransfer.update();
	    			}catch(Exception e){
	    				overdueSetLogger.log(Level.INFO, "债权转让异常取消失败:"+transferCode);
	    				list2.add(loanCode);
	    				continue;
	    			}
	    			
	    			if( updateResult == false ){
	    				overdueSetLogger.log(Level.INFO, "债权转让取消失败:"+transferCode);
	    				list2.add(loanCode);
	    				continue;
	    			}
	    			//修改标书债权状态
	    			boolean updateTraceState = false;
	    			if(loanTransferService.vilidateIsTransfer(loanTransfer.getStr("traceCode"))){
	    				updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
	    						"A", "B");
	    			}else{
	    				updateTraceState = loanTraceService.updateTransferState(loanTransfer.getStr("traceCode"),
	    						"A", "C");
	    			}
	    			if(updateTraceState == false){
	    				overdueSetLogger.log(Level.INFO, "债权取消成功,但标书债权状态修改异常:"+loanTransfer.getStr("traceCode"));
	    				list2.add(loanCode);
	    				continue;
	    			}
	    			
	    			//回滚积分
	    			int transScore = loanTransfer.getInt("transScore");
//	    			fundsServiceV2.doPoints(userCode, 0 , transScore , "取消债权转让,返回冻结积分!");
	    		}
	    		
	    		//记录当前的逾期利息
	    		List<LoanTrace> loanTraces = loanTraceService.findAllByLoanCode(loanCode);
	    		for(int i = 0;i<loanTraces.size();i++){
	    			LoanTrace loanTrace = loanTraces.get(i);
	    			loanTrace.set("overdueInterest", loanTrace.getLong("nextInterest"));
	    			loanTrace.update();
	    		}
	    		Map<String, Object> loanOverdueMap = new HashMap<String, Object>();
	    		loanOverdueMap.put("loanCode", loanInfo.getStr("loanCode"));
	    		loanOverdueMap.put("loanNo", loanInfo.getStr("loanNo"));
	    		loanOverdueMap.put("loanUserCode", loanInfo.getStr("userCode"));
	    		loanOverdueMap.put("loanUserName", loanInfo.getStr("userName"));
	    		loanOverdueMap.put("loanAmount", loanInfo.getLong("loanAmount"));
	    		loanOverdueMap.put("loanTitle", loanInfo.getStr("loanTitle"));
	    		loanOverdueMap.put("loanTimeLimit", loanInfo.getInt("loanTimeLimit"));
	    		loanOverdueMap.put("repayIndex", loanInfo.getInt("reciedCount")+1);
	    		long nextAmount = loanTraceService.sumNextAmount(loanCode);
	    		loanOverdueMap.put("principal", nextAmount);
	    		loanOverdueMap.put("interest", 0);
	    		loanOverdueMap.put("overdueAmount", nextAmount);
	    		loanOverdueMap.put("refundType", loanInfo.getStr("refundType"));
	    		loanOverdueMap.put("overdueDate", DateUtil.getNowDate());
	    		loanOverdueMap.put("overdueTime", DateUtil.getNowTime());
	    		loanOverdueMap.put("remark", "");
	    		if(!StringUtil.isBlank(overdueType)){
	    			loanOverdueMap.put("overdueType",overdueType);//逾期类型
	    		}
	    		boolean result = overdueTraceService.save(loanOverdueMap);
	    		if(result){
	    			overdueSetLogger.log(Level.INFO, "[" + count + "/" + list.size() + "]标的["+loanNo+"],设置逾期成功...");
	    			list3.add(loanCode);
	    		}else {
	    			overdueSetLogger.log(Level.INFO, "[" + count + "/" + list.size() + "]标的["+loanNo+"],设置逾期失败...");
	    			list2.add(loanCode);
	    		}
				count++;
			}
		}
		
		Map<String, Object> map = new HashMap<String,Object>();
		map.put("size",list.size());
		map.put("errSize",list2.size());
		map.put("sucSize", list3.size());
		map.put("dealLoanInfo",list4.toString());
		if(!list2.isEmpty()&&list2.size()>0){
			overdueSetLogger.log(Level.INFO, "失败标code:"+list2.toString());
		}else{
			overdueSetLogger.log(Level.INFO, "没有失败记录");
		}
		if(!list3.isEmpty()&&list3.size()>0){
			overdueSetLogger.log(Level.INFO, "成功标code:"+list3.toString());
		}else{
			overdueSetLogger.log(Level.INFO, "没有成功记录");
		}
		return succ("success", map);
		
	}
	
	
}
