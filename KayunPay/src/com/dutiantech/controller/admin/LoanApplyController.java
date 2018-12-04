package com.dutiantech.controller.admin;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.util.BankUtil;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.UIDUtil;
import com.dutiantech.util.SysEnum.loanUsedType;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class LoanApplyController extends BaseController {
	
	
	private LoanApplyService loanApplyService = getService(LoanApplyService.class);
	private UserService userService = getService( UserService.class ) ;
	private UserInfoService userInfoService = getService( UserInfoService.class ) ;
	private OPUserV2Service opUserService = getService( OPUserV2Service.class ) ;
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private BanksService bankService = getService(BanksService.class);
	@ActionKey("/getUserByMobile")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getUserByMobile(){
		String uMobile = getPara("loanMobile");
		Map<String, Object> map = new HashMap<String, Object>();
		User user = userService.findByMobile(uMobile) ;
		if(user==null){
			//1 根据手机号查询电子账户
			Map<String,String> accMap=JXQueryController.accountQueryByMobile(uMobile);
			if(null!=accMap&&JXController.isRespSuc(accMap)){ //企业户 直接查询jx那边的开户信息
				String accountId= accMap.get("accountId");
				if(!StringUtil.isBlank(accountId)){
				//2 根据电子账号查询企业开户信息
					Map<String,String> coMap=JXQueryController.corprationQuery(accountId);
					BanksV2 banksV2 =new BanksV2();
					banksV2.put("cardid",coMap.get("idNo"));
					banksV2.put("trueName",coMap.get("name"));
					banksV2.put("bankNo",coMap.get("caccount"));
					map.put("invitationNum",0);
					map.put("user",banksV2);
					return succ("ok", map) ;
				}
			}
		}
		else{
			String userState = user.getStr("userState");
			if( "N".equals(userState) ){
				//投标
				List<FundsTrace> fundsTrace = fundsTraceService.queryTraceByUserCodeDate(user.getStr("userCode"), "P", "00000000", "99999999");
				//招标
				List<FundsTrace> fundsTrace2 = fundsTraceService.queryTraceByUserCodeDate(user.getStr("userCode"), "S", "00000000", "99999999");
				map.put("user", user);
				map.put("bidNum", fundsTrace.size());
				map.put("invitationNum", fundsTrace2.size());
				return succ("ok", map ) ;
			}else{
				return error("02","账户不可用！" + userState, null ) ;
			}
		}
		return null;
		
	}
	
	@ActionKey("/getUserByUserName")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getUserByUserName(){
		String userName = getPara("userName","");
		
		User user = userService.findUserAllInfoByUserName(userName);
		if(user==null ){
			return error("02","用户数据异常，请联系管理员",false);
		}
		
		try {
			String userMobile = user.getStr("userMobile");
			if(!StringUtil.isBlank(userMobile)){
				user.put("userMobile", CommonUtil.decryptUserMobile(userMobile));
			}else{
				user.put("userMobile", "");
			}
		} catch (Exception e) {
			user.put("userMobile", "");
		}
		try {
			String userCardId = user.getStr("userCardId");
			if(!StringUtil.isBlank(userCardId)){
				user.put("userCardId",  CommonUtil.decryptUserCardId(userCardId));
			}else{
				user.put("userCardId",  "");
			}
		} catch (Exception e) {
			user.put("userCardId", "");
		}
		
		return succ("查询单个用户全部信息完成", user);
	}
	
	/**
	 * 分页查询分页发标申请
	 */
	@ActionKey("/getLoanApplyList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanApplyList(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		String endDate = getPara("endDate","");
		
		String applyState = getPara("applyState","");
		
		String loanUserCode = getPara("loanUserCode","");
		
		String allKey = getPara("allkey","");
		
		String opUserCode = getUserCode();
		
		OPUserV2 opUser = opUserService.findById(opUserCode);
		
		if(opUser.getStr("isBranch").equals("n")){
			opUserCode = null;
		}
		Page<LoanApply> page = loanApplyService.findByPage(pageNumber, pageSize, beginDate, endDate,applyState, allKey, loanUserCode,opUserCode);
		
		return succ("分页查询发标申请完成", page);
	}
	
	/**
	 * 根据id查询发标申请明细
	 */
	@ActionKey("/getLoanApplyById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanApplyById(){
		String loanCode = getPara("loanCode");
		return succ("根据id查询发标申请明细完成", loanApplyService.findByCode(loanCode));
	}

	@ActionKey("/auditLoanApply")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateAuditDesc(){
		String loanCode = getPara("loanCode","");
		String auditDesc = getPara("auditDesc","");
		String auditState = getPara("auditResult","A");
		
		String userCode = getUserCode() ;
		
		auditDesc = auditDesc + "<br>";
		
		OPUserV2 user = opUserService.findById(userCode);
		String opName = user.getStr("op_name") ;
		String opGroup = user.getStr("op_group") ;
		
		switch (auditState) {
		case "D":
			auditDesc += "\n信审通过!";
			break;
		case "C":
			auditDesc += "\n信审驳回!";
			break;
		case "H":
			auditDesc += "\n风控审核通过!";
			break;
		case "G":
			auditDesc += "\n风控审核失败!";
			break;
		default:
			break;
		}
		auditDesc += String.format("\n(%s[%s]) %s", opName , opGroup , 
				DateUtil.getStrFromDate(new Date(), "yyyy-MM-dd HH:mm"));
		
		boolean upResult = loanApplyService.audit4loan(loanCode, auditDesc , auditState) ;
		if( upResult == true ){
			if("D".equals(auditState) == true){
				LoanApply loanApply = loanApplyService.findByCode(loanCode);
				String loanUserCode = loanApply.getStr("loanUserCode");
				String loanUserInfo = loanApply.getStr("loanUserInfo");
				User loanUser=userService.findById(loanUserCode);
				JSONObject json = JSONObject.parseObject(loanUserInfo);
				if ("1".equals(json.get("loanUserType"))) {	// 如果借款人主体性质为企业，则保存借款人类型为C
					loanUser.set("userType", SysEnum.UserType.C.val());
				} else {
					loanUser.set("userType", SysEnum.UserType.J.val());
				}
				loanUser.update();
			}
			return succ("ok", auditDesc ) ;
		}else{
			return error("01", "更新信审意见异常", null );
		}
		
	}
	
	@ActionKey("/submitLoanApply")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message submitLoanApply(){
		LoanApply loanApply = getModel( LoanApply.class ) ;
		String bankNo=getPara("bankNo","");
//		String loanUserCode = loanApply.getStr("loanUserCode");
//		if(!userService.validateUserState(loanUserCode)){
//			return error("01", "借款人用户状态不是【正常】", null);
//		}
//		if(!userInfoService.isAuthed(loanUserCode).equals("2")){
//			return error("02", "借款人非【已认证】", null);
//		}
		//发布标的时对借款人账户进行存管验证 rain 20180420 
//		String loanUserCode=loanApply.getStr("loanUserCode");
//		if(StringUtil.isBlank(loanUserCode)){
//			return error("01","借款人信息异常",null);
//		}
//		User user=userService.findById(loanUserCode);
//		Map<String,String> accMap=JXQueryController.accountQueryByMobile(loanApply.getStr("loanMobile"));
//		if(null!=accMap&&JXController.isRespSuc(accMap)){
//			String accountId= accMap.get("accountId");
//			if(!StringUtil.isBlank(accountId)){
//			//2 根据电子账号查询企业开户信息
//				Map<String,String> coMap=JXQueryController.corprationQuery(accountId);
//				if(null==coMap){
//					//非企业用户借款人做标时，进行存管验证 rain 20180526
//					if(!JXController.isJxAccount(user)){
//						return error("02","请先给借款人开通存管，再做借款标",null);
//					}
//				}
//			}
//		}
		loanApply.set("applyState", SysEnum.applyState.A.val());
		return doSaveOrUpdate(loanApply,bankNo) ;
	}
	
	
	/**
	 * 保存发标申请
	 * 	状态为 B，在提交的时候重置为A，进入审核流程
	 */
	@ActionKey("/saveLoanApply")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
//	@Before({LoanApplyValidator.class,AuthInterceptor.class,Tx.class})
	public Message saveLoanApply(){
		LoanApply loanApply = getModel( LoanApply.class ) ;
		String bankNo=getPara("bankNo","");
//		String loanUserCode = loanApply.getStr("loanUserCode");
//		if(!userService.validateUserState(loanUserCode)){
//			return error("01", "借款人用户状态不是【正常】", null);
//		}
//		if(!userInfoService.isAuthed(loanUserCode).equals("2")){
//			return error("02", "借款人非【已认证】", null);
//		}
		loanApply.set("applyState", SysEnum.applyState.B.val());
		return doSaveOrUpdate(loanApply,bankNo) ;
	}
	
	private Message doSaveOrUpdate(LoanApply loanApply,String bankNo){
		//获取发标人信息
		String userCode = getUserCode() ;
		OPUserV2 opUser = opUserService.findById(userCode) ;
		
		String uMobile = loanApply.getStr("loanMobile");
		String uMail = loanApply.getStr("loanMail");
		String userName = loanApply.getStr("loanUserName");
		String loanUserCode = loanApply.getStr("loanUserCode");
		String userCardName = loanApply.getStr("loanTrueName");
		String userCardId = loanApply.getStr("loanCardId");
		try {
			userCardId = CommonUtil.encryptUserCardId(userCardId) ;
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			return error("05","身份证信息解析异常","");
		}
		User user = userService.find4mobile(uMobile) ;
		boolean upUserResult = false ;
		BanksV2 bankV2=null;
		String bankName="";
		
		String nameOfBank = BankUtil.getNameOfBank(bankNo);
		if(!StringUtil.isBlank(nameOfBank)){
			bankName = nameOfBank.substring(0, nameOfBank.indexOf("·"));
		}
		String jxAccountId="";
		Map<String,String> accMap=JXQueryController.accountQueryByMobile(uMobile);
		if( user == null ){ //个人用户/企业用户时 未做标
			String qyUserCode=UIDUtil.generate();
			if(null!=accMap&&JXController.isRespSuc(accMap)){
				jxAccountId=accMap.get("accountId");	
				upUserResult = userService.save(qyUserCode,uMobile, uMail , uMobile + "9797" , userName, getRequestIP() , "企业借款用户-申请发标时创建",jxAccountId) ;
			}
			else{
				upUserResult = userService.save(qyUserCode,uMobile, uMail , uMobile + "9797" , userName, getRequestIP() , "个人借款用户-申请发标时创建","") ;
			}
			
			    bankV2 = getModel(BanksV2.class);
				bankV2.set("userCode", qyUserCode);
				bankV2.set("userName",userName);
			    bankV2.set("trueName",userCardName);
				bankV2.set("bankName",bankName ) ;
				bankV2.set("bankNo", bankNo);
				bankV2.set("bankType","") ;
				bankV2.set("cardCity", "");
				try {
					bankV2.set("mobile",CommonUtil.encryptUserMobile(uMobile));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					return error("04","手机号解析异常","");
				}
				bankV2.set("createDateTime", DateUtil.getNowDateTime());
				bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
				bankV2.set("isDefault", "1");
				bankV2.set("status", "0");
				bankV2.set("agreeCode",CommonUtil.genMchntSsn());
				bankV2.set("ssn", CommonUtil.genMchntSsn());
				bankV2.set("cardid", userCardId) ;
				bankV2.save();
			user = userService.findByMobile(uMobile) ;
			loanUserCode = user.getStr("userCode" ) ;
		}else{
			//做过标的个人用户或企业用户
			if(null!=accMap&&JXController.isRespSuc(accMap)){
				jxAccountId=accMap.get("accountId");
	          }
			else{
				return error("08","请先开存管再做借款标","");
			}
			bankV2 = bankService.findByUserCode(loanUserCode); 
			if (bankV2 != null) { 
 				bankV2.set("trueName",userCardName);
 				bankV2.set("bankNo", bankNo);
 				bankV2.set("bankName", bankName);
 				bankV2.set("cardid", userCardId);
 				try {
					bankV2.set("mobile", CommonUtil.encryptUserMobile(uMobile));// 存管手机号
				} catch (Exception e) {
				return error("07","手机号解析异常","");
				}
 				bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
 				bankV2.set("ssn", CommonUtil.genMchntSsn());
 				bankV2.update();
 			}
 			else{ 
 				bankV2 = getModel(BanksV2.class);
				bankV2.set("userCode", loanUserCode);
				bankV2.set("userName", userName);
			    bankV2.set("trueName", userCardName);
				bankV2.set("bankName", bankName ) ;
				bankV2.set("bankNo", bankNo);
				bankV2.set("bankType", "") ;
				bankV2.set("cardCity", "");
				try {
					bankV2.set("mobile",CommonUtil.encryptUserMobile(uMobile));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					return error("04","手机号解析异常","");
				}
				bankV2.set("createDateTime", DateUtil.getNowDateTime());
				bankV2.set("modifyDateTime", DateUtil.getNowDateTime());
				bankV2.set("isDefault", "1");
				bankV2.set("status", "0");
				bankV2.set("agreeCode",CommonUtil.genMchntSsn());
				bankV2.set("ssn", CommonUtil.genMchntSsn());
				bankV2.set("cardid", userCardId) ;
				bankV2.save();
				if (bankV2.save()) {
 					user.set("jxAccountId", jxAccountId);
 					user.update();
 				}
 			}
 			//已开户的用户
 			upUserResult = userService.updateUser(loanUserCode, userName, null , uMail ) ;
		}
			
		Map<String,String> coMap=JXQueryController.corprationQuery(jxAccountId);
		//update userinfo 
		if( upUserResult == true ){
			Map<String , Object> para = new HashMap<String , Object>();
			para.put("userCardName", userCardName ) ;
			//企业用户 idType=20：其他证件（组织机构代码）25：社会信用号
			String idType="01";
			if(null!=coMap&&JXController.isRespSuc(coMap)){
				idType=coMap.get("idType");
			}
			try{
				para.put("idType", idType);
				para.put("userCardId", userCardId ) ;
				para.put("cardImg", "cardimg" ) ;
				para.put("isAuthed", "2" ) ;
			}catch(Exception e){
			}

			para.put("userAdress", loanApply.getStr("loanAddress") ) ;
			upUserResult = userInfoService.updateUserInfo( loanUserCode , para);
		}
		if( upUserResult == false ){
			return error("03", "用户信息更新失败!", null ) ;
		}
		//2，直接创建借款
		loanApply.set("applyUserCode", userCode );
		loanApply.set("applyUserName", opUser.getStr("op_name") );
		loanApply.set("applyUserGroup", opUser.getStr("op_group") );
		loanApply.set("loanUserCode", loanUserCode );
		loanApply.set("loanMail", "");
		loanApply.set("loanAddress", "");
		loanApply.set("loanUsedType", loanUsedType.A.val());
		boolean loanApplyResult = loanApplyService.saveOrUpdateLoanApply(loanApply) ;
		//3，获取当前操作员的信息
		if( loanApplyResult == true )
			return succ("创建申请发标完成", loanApply.getStr("loanCode") );
		return error("01", "操作未生效", null);
	}
	
	/**
	 * 分页查询借款人资金统计信息
	 */
	@ActionKey("/getLoanUserFunds")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getLoanUserFunds(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String allkey = getPara("allkey","");
		
		Page<Funds> page = fundsServiceV2.findLoanUserFundsByPage(pageNumber, pageSize, allkey);
		
		return succ("分页查询借款人资金统计信息", page);
	}
	
	@ActionKey("/copyLoanApplyInfo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message copyLoanApplyInfo(){
		String loanCode = getPara("loanCode","");
		
		if(StringUtil.isBlank(loanCode)){
			return error("02","确定你复制的标是存在的吗？没有被删除吧",false);
		}
		
		LoanApply loanApply = loanApplyService.findByCode(loanCode);
		
		if(loanApply==null){
			return error("03","确定你复制的标是存在的吗？没有被删除吧",false);
		}
		loanApply.set("loanTitle", "【复制】"+loanApply.getStr("loanTitle"));
		loanApply.set("loanCode",UIDUtil.generate());
		loanApply.set("loanNo", null);
		loanApply.set("auditDesc", "");
		loanApply.set("applyState", "A");
		String date = DateUtil.getNowDate();
		String dateTime = DateUtil.getNowDateTime();
		loanApply.set("modifyDate", date);
		loanApply.set("modifyDateTime", dateTime);
		loanApply.set("applyDate", date);
		loanApply.set("applyDateTime", dateTime);
		loanApply.set("contractPics", "");
		loanApply.set("loanPics", "");
		loanApply.set("loanContractPics", "");
		
		if(loanApply.save()){
			return succ("01", true);
		}
		
		return error("03", "操作未生效", false);
	}

}