package com.dutiantech.controller.admin;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.admin.validator.FundsConvertBalanceValidator;
import com.dutiantech.controller.admin.validator.FundsRechargeValidator;
import com.dutiantech.controller.admin.validator.FundsUpdateTraceSynStateValidator;
import com.dutiantech.controller.admin.validator.FundsWithdrawalsByApplyValidator;
import com.dutiantech.controller.admin.validator.FundsWithdrawalsValidator;
import com.dutiantech.controller.pay.LianLianPayController;
import com.dutiantech.controller.pay.SYXPayController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.RechargeTrace.BANK_STATE;
import com.dutiantech.model.RechargeTrace.TRACE_STATE;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.model.YiStageUserInfo;
import com.dutiantech.plugin.syxpay.SYXPayKit;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.service.YiStageUserInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.FuiouTraceType;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.util.UIDUtil;
import com.fuiou.data.AppTransReqData;
import com.fuiou.data.CommonRspData;
import com.fuiou.data.QueryBalanceResultData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.ext.interceptor.GET;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jx.service.JXService;

import net.sf.json.JSONObject;

public class FundsController extends BaseController {
	
	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	
	private UserService userService = getService(UserService.class);
	
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	
	private UserInfoService userInfoService = getService(UserInfoService.class);
	
	private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	
	private OPUserV2Service opUserService = getService( OPUserV2Service.class ) ;
	private FuiouTraceService fuiouTraceService=getService(FuiouTraceService.class);
	private LoanApplyService loanApplyService=getService(LoanApplyService.class);
	
	private RechargeTraceService rechargeService = getService(RechargeTraceService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private BanksService banksService = getService(BanksService.class);
	private YiStageUserInfoService yiStageUserInfoService = getService(YiStageUserInfoService.class);
	/**
	 * 根据用户ID查询单个用户资金账户明细
	 */
	@ActionKey("/getFundsById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFundsById(){
		
		String userCode = getPara("fundsUserCode","");
		
		Funds funds = fundsServiceV2.findById(userCode);
		
		if(funds==null || funds.get("avBalance")==null){
			return error("02", "该资金账户不存在", false);
		}
		
		return succ("查询单个用户资金账户明细完成", funds);
		
	}
	
	/**
	 * 冻结可用余额  或 冻结金额解冻
	 */
	@ActionKey("/convertBalance")
	@AuthNum(value=999)
	@Before({FundsConvertBalanceValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message convertBalance(){
		Integer amount = getParaToInt("amount");//拦截器拦截验证不可为空
		
		String userCode = getPara("fundsUserCode");//拦截器拦截验证不可为空
		//1 可用余额划入冻结余额       0 冻结余额划入可用余额 
		Integer type = getParaToInt("type");//拦截器拦截验证不可为空
		
		String remark = getPara("remark");
		
		if(StringUtil.isBlank(remark)){
			if(type==1){ 
				remark = "人工冻结资金"+DateUtil.getNowDateTime();
			}else{
				remark = "人工解冻资金"+DateUtil.getNowDateTime();
			}
			
		}
		
		if(!userService.validateUserState(userCode))
			return error("02", "用户状态非【正常】", false);
		if(fundsServiceV2.convertBalance(amount, userCode, type, 0, remark))
			return succ("可用余额-冻结金额 转换操作完成", true);
		else
			return error("01", "可用余额-冻结金额 转换操作未生效", false);
	}

	@ActionKey("/modifyPointAndScore")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modifyPointAndScore(){

		String userCode = getPara("userCode","") ;
		if( StringUtil.isBlank( userCode) ){
			return error("01", "缺少必要参数!", null ) ;
		}
		String opValue = getPara("score");
		String opType = "+";
		int opType2 = 0;
		if( opValue.indexOf("-") == 0 ){
			opType = "-";
			opType2 = 1;
			opValue = opValue.replace("-", "") ;
		}
		int point = 0 ;
		try{
			point = Integer.parseInt( opValue ) ;
		}catch(Exception e){
			return error("02", "积分应该为数值，比如1003,-4302", null ) ;
		}
		boolean upResult = userService.modifyUserScore(userCode, opType, point) ;	// keyong
		String resultDesc = "";
		if( upResult == true ){
			resultDesc = "0";
		}else{
			resultDesc = "1";
		}

		if( fundsServiceV2.doPoints(userCode, opType2, point,"后台修改积分")!=null){
			resultDesc += "0";
		}else{
			resultDesc += "1";
		}
		return succ("ok", resultDesc) ;
	}
	
	
	@ActionKey("/modifyFundsPoints")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modifyPoints(){
		String userCode = getPara("userCode","") ;
		if( StringUtil.isBlank( userCode) ){
			return error("01", "缺少必要参数!", null ) ;
		}
		String opValue = getPara("score");
		int opType = 0;
		if( opValue.indexOf("-") == 0 ){
			opType = 1;
			opValue = opValue.replace("-", "") ;
		}
		int point = 0 ;
		try{
			point = Integer.parseInt( opValue ) ;
		}catch(Exception e){
			return error("02", "积分应该为数值，比如1003,-4302", null ) ;
		}
		
		if(fundsServiceV2.doPoints(userCode, opType, point,"后台修改积分")!=null){
			return succ("ok", null ) ;
		}
		return error("03", "修改失败", null ) ;
	}
	
	/**
	 * 充值
	 * @return
	 */
	@ActionKey("/recharge")
	@AuthNum(value=999)
	@Before({FundsRechargeValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message recharge(){
		try {
			Integer payAmount = getParaToInt("amount");
			
			String userCode = getPara("fundsUserCode");
			
			String remark = getPara("remark");
			if(StringUtil.isBlank(remark)){
				remark = "人工充值"+DateUtil.getNowDateTime();
			}
			
			int isExist = userService.isExist(userCode);
			User user= userService.findById(userCode);
			
			if(null!=user.getStr("loginId")&& !"".equals(user.getStr("loginId"))){
        	 if(isExist == 0){
 				if(!userService.validateUserState(userCode))
 					return error("04", "用户状态非【正常】", false);
 				
 				User outUser=userService.findByMobile(CommonUtil.OUTCUSTNO);
 				//调用富有的划拨接口 2017.6.8 rain
 				CommonRspData commonRspData= fuiouTraceService.refund(Long.valueOf(payAmount),FuiouTraceType.Q,outUser,userService.findById(userCode));
 				
 				if( ("0000").equals(commonRspData.getResp_code())&& fundsServiceV2.recharge(userCode, payAmount,0, remark,SysEnum.traceType.D.val())!=null){
 					
                     rechargeTraceService.saveRechargeTrace(null,commonRspData.getMchnt_txn_ssn(), payAmount, "B", remark, userCode, "", "", "", "B", "SYS", remark);
 					return succ("充值操作完成", true);
 				}
 			}else if(isExist > 0){
 				return error("03","用户不存在",false);
 			} 
        	 
         }else{
        	 
        	 return error("02","用户未开通存管",false);
         }
			return error("02", "充值操作未生效", false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return error("01", "充值操作未生效", false);
	}

	
	/**
	 * 分页查询用户提现申请
	 * @return
	 */
	@ActionKey("/getApplyWithdrawalsByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getApplyWithdrawalsByPage(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String allkey = getPara("allkey","");
		
		String status = getPara("status","");
		
		Map<String,Object> result = withdrawTraceService.findByPage4Apply4Noob(pageNumber, pageSize,allkey,status);
		
		if(StringUtil.isBlank(allkey)){
			Map<String,Object> result_sum = withdrawTraceService.findByPage4Apply4NoobWithSum(pageNumber, pageSize, allkey, status);
			result.putAll(result_sum);
		}
		
		return succ("分页查询用户申请提现记录完成", result);
	}
	
	/**
	 * 连连支付提现！
	 * @return
	 */
	@ActionKey("/withdrawalsByApplyCode")
	@AuthNum(value=999)
	@Before({FundsWithdrawalsByApplyValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message withdrawalsByApplyCode(){
		String withdrawCodes = getPara("withdrawCode");
		
		String[] withdrawCodea = withdrawCodes.split(",");
		
		int success = 0;
		for (int i = 0; i < withdrawCodea.length; i++) {
			String withdrawCode = withdrawCodea[i];
			WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
			Long payAmount = withdrawTrace.getLong("withdrawAmount");
			
			String userCode = withdrawTrace.getStr("userCode");
			
			String userName = withdrawTrace.getStr("userName");
			
			String userTrueName = withdrawTrace.getStr("userTrueName");
			
			String oldStatus = withdrawTrace.getStr("status");
			
			String cardCity = withdrawTrace.getStr("cardCity");
			
			if(!oldStatus.equals("0")){
				continue;
			}
			
			String info_lable = "【"+userName+"("+userTrueName+")，申请提现:"+(payAmount/10.0/10.0)+"】";
			
			if(!userService.validateUserState(userCode))
				return error("15", info_lable+"该条记录用户状态非【正常】", false);
			
			if(!oldStatus.equals(SysEnum.withdrawTraceState.A.val())){
				return error("14",info_lable+"该条申请状态非【未审核】",false);
			}
			
			String opUserCode = getUserCode() ;
			OPUserV2 opUser = opUserService.findById(opUserCode);
			if(opUser==null){
				return error("17", "操作员信息异常", false);
			}
			String opName = opUser.getStr("op_name");
			String opCode = opUser.getStr("op_code");
			if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
				return error("18", "操作员信息异常", false);
			}
			withdrawTrace.set("opUserName", opName);
			withdrawTrace.set("opUserCode", opCode);
			int isExist = userService.isExist(userCode);
			if(isExist == 0){
//				long avBlance = fundsServiceV2.findAvBalanceById(userCode);
//				if(payAmount > avBlance)
//					return error("02", "可用余额不足", false);
				try {
					if(StringUtil.isBlank(cardCity)){
						continue;
					}else{
						String pn = cardCity.split("\\|")[0];
						String cn = cardCity.split("\\|")[1];
						if(StringUtil.isBlank(pn) && StringUtil.isBlank(cn)){
							continue;
						}
					}
				} catch (Exception e) {
					continue;
				}
				if(withdrawTrace.update()){
					boolean zz = withdrawTraceService.updateById(withdrawCode, SysEnum.withdrawTraceState.B.val(), oldStatus);
					if(zz){
						//提现
						LianLianPayController lianPay = new LianLianPayController();
						long province_code = 0;
						long city_code = 0;
						if(!StringUtil.isBlank(cardCity)){
							String province_name = cardCity.split("\\|")[0];
							String city_name = cardCity.split("\\|")[1];
							if(!StringUtil.isBlank(province_name) && !StringUtil.isBlank(city_name)){
								Integer tmp1 = Db.queryInt("select id from t_location where areaname =? or shortname=?",province_name,province_name);
								if(tmp1!=null)
									province_code = tmp1.intValue();
								
								Integer tmp2 = Db.queryInt("select id from t_location where areaname =? or shortname=?",city_name,city_name);
								if(tmp2!=null)
									city_code = tmp2.intValue();
								else
									city_code = province_code;
								
								if(province_code==110000){
									city_code = 110000;
								}
								if(province_code==120000){
									city_code = 120000;
								}
								if(province_code==310000){
									city_code = 310000;
								}
								if(province_code==500000){
									city_code = 500000;
								}
							}
						}
						Message msg = lianPay.repay4lianlian(withdrawCode,province_code!=0?province_code+"":"",city_code!=0?city_code+"":"");
						if(msg.getReturn_code().equals("00")){
							success ++;
						}
						System.out.println(msg.getReturn_info());
					}
				}
			}else if(isExist > 0){
				return error("13",info_lable+"该条申请的用户不存在",false);
			}
		}
		if(success>0){
			return succ(success+"笔提现操作已受理,共"+withdrawCodea.length+"笔", true);
		}
		return error("11", success+"笔提现操作已受理,共"+withdrawCodea.length+"笔", false);
		
	}
	
	/**
	 * 商银信提现！
	 * @return
	 */
	@ActionKey("/withdrawalsByApplyCode4SYX")
	@AuthNum(value=999)
	@Before({FundsWithdrawalsByApplyValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message withdrawalsByApplyCode4SYX(){
		String withdrawCodes = getPara("withdrawCode");
		
		String[] withdrawCodea = withdrawCodes.split(",");
		
		int success = 0;
		for (int i = 0; i < withdrawCodea.length; i++) {
			String withdrawCode = withdrawCodea[i];
			WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
			Long payAmount = withdrawTrace.getLong("withdrawAmount");
			
			String userCode = withdrawTrace.getStr("userCode");
			
			String userName = withdrawTrace.getStr("userName");
			
			String userTrueName = withdrawTrace.getStr("userTrueName");
			
			String oldStatus = withdrawTrace.getStr("status");
			
			String cardCity = withdrawTrace.getStr("cardCity");
			
			if(!oldStatus.equals("0")){
				continue;
			}
			
			String info_lable = "【"+userName+"("+userTrueName+")，申请提现:"+(payAmount/10.0/10.0)+"】";
			
			if(!userService.validateUserState(userCode))
				return error("15", info_lable+"该条记录用户状态非【正常】", false);
			
			if(!oldStatus.equals(SysEnum.withdrawTraceState.A.val())){
				return error("14",info_lable+"该条申请状态非【未审核】",false);
			}
			
			String opUserCode = getUserCode() ;
			OPUserV2 opUser = opUserService.findById(opUserCode);
			if(opUser==null){
				return error("17", "操作员信息异常", false);
			}
			String opName = opUser.getStr("op_name");
			String opCode = opUser.getStr("op_code");
			if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
				return error("18", "操作员信息异常", false);
			}
			withdrawTrace.set("opUserName", opName);
			withdrawTrace.set("opUserCode", opCode);
			int isExist = userService.isExist(userCode);
			if(isExist == 0){
				try {
					if(StringUtil.isBlank(cardCity)){
						continue;
					}else{
						String pn = cardCity.split("\\|")[0];
						String cn = cardCity.split("\\|")[1];
						if(StringUtil.isBlank(pn) && StringUtil.isBlank(cn)){
							continue;
						}
					}
				} catch (Exception e) {
					continue;
				}
				if(withdrawTrace.update()){
					boolean zz = withdrawTraceService.updateById(withdrawCode, SysEnum.withdrawTraceState.B.val(), oldStatus);
					if(zz){
						String province_name = cardCity.split("\\|")[0];
						String city_name = cardCity.split("\\|")[1];
						SYXPayController syxc = new SYXPayController();
						if(syxc.daifu(withdrawCode, province_name, city_name).getReturn_code().equals("00")){
							success ++;
						}
					}
				}
			}else if(isExist > 0){
				return error("13",info_lable+"该条申请的用户不存在",false);
			}
		}
		if(success>0){
			return succ(success+"笔提现操作已受理,共"+withdrawCodea.length+"笔", true);
		}
		return error("11", success+"笔提现操作已受理,共"+withdrawCodea.length+"笔", false);
		
	}
	
	
	/**
	 * 人工处理提现申请
	 * @return
	 */
	@ActionKey("/changeRengongTixian")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message changeRengongTixian(){
		String withdrawCodes = getPara("withdrawCode");
		
		String[] withdrawCodea = withdrawCodes.split(",");
		
		int success = 0;
		for (int i = 0; i < withdrawCodea.length; i++) {
			String withdrawCode = withdrawCodea[i];
			WithdrawTrace trace = WithdrawTrace.withdrawTraceDao.findById(withdrawCode) ;
			String oldStatus = trace.getStr("status");
			if(!oldStatus.equals("0") || oldStatus.equals("3")){
				continue;
			}
			trace.set("status", "3" ) ;
			trace.set("withdrawType","SYS");
			trace.set("withdrawRemark", "处理提现申请,转人工提现"+DateUtil.getNowDateTime()) ;
			trace.set("okDateTime", DateUtil.getNowDateTime());//成功时间
			trace.update();
			fundsServiceV2.withdrawals3_ok(trace.getStr("userCode"), trace.getLong("withdrawAmount"), trace.getInt("sxf"),"处理提现申请，转人工提现",SysEnum.traceType.E);
			success ++;
		}
		
		if(success>0){
			return succ("操作完成", true);
		}
		return error("11", "提现操作未生效", false);
	}
	
	/**
	 * 审核申请提现(进入提现中)
	 * @return
	 */
	@ActionKey("/withdrawalsByAll")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message withdrawalsByAll(){
		
		List<WithdrawTrace> list = withdrawTraceService.findByPage4WSH();
		int success = 0;
		for (int i = 0; i < list.size(); i++) {
			String withdrawCode = list.get(i).getStr("withdrawCode");

			WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
			
			String userCode = withdrawTrace.getStr("userCode");
			
			String oldStatus = withdrawTrace.getStr("status");
			
			String cardCity = withdrawTrace.getStr("cardCity");
			
			if(!userService.validateUserState(userCode))
				return error("15", "用户状态非【正常】", false);
			
			if(!oldStatus.equals(SysEnum.withdrawTraceState.A.val())){
				return error("14","该条申请状态非【未审核】",false);
			}
			
			String opUserCode = getUserCode() ;
			OPUserV2 opUser = opUserService.findById(opUserCode);
			if(opUser==null){
				return error("17", "操作员信息异常", false);
			}
			String opName = opUser.getStr("op_name");
			String opCode = opUser.getStr("op_code");
			if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
				return error("18", "操作员信息异常", false);
			}
			withdrawTrace.set("opUserName", opName);
			withdrawTrace.set("opUserCode", opCode);
			int isExist = userService.isExist(userCode);
			if(isExist == 0){
				if(withdrawTrace.update()){
					boolean zz = withdrawTraceService.updateById(withdrawCode, SysEnum.withdrawTraceState.B.val(), oldStatus);
					if(zz){
						//提现
						LianLianPayController lianPay = new LianLianPayController();
						
						long province_code = 0;
						long city_code = 0;
						if(!StringUtil.isBlank(cardCity)){
							String province_name = cardCity.split("\\|")[0];
							String city_name = cardCity.split("\\|")[1];
							if(!StringUtil.isBlank(province_name) && !StringUtil.isBlank(city_name)){
								Integer tmp1 = Db.queryInt("select id from t_location where areaname =? or shortname=?",province_name,province_name);
								if(tmp1!=null)
									province_code = tmp1.intValue();
								
								Integer tmp2 = Db.queryInt("select id from t_location where areaname =? or shortname=?",city_name,city_name);
								if(tmp2!=null)
									city_code = tmp2.intValue();
								else
									city_code = province_code;
								
								if(province_code==110000){
									city_code = 110000;
								}
								if(province_code==120000){
									city_code = 120000;
								}
								if(province_code==310000){
									city_code = 310000;
								}
								if(province_code==500000){
									city_code = 500000;
								}
							}
						}
						Message msg = lianPay.repay4lianlian(withdrawCode,province_code+"",city_code+"");
						if(msg.getReturn_code().equals("00")){
							success ++;
						}
					}
				}
			}else if(isExist > 0){
				return error("13","用户不存在",false);
			}
		}
		if(success>0){
			return succ("提现操作已受理", true);
		}
		return error("11", "提现操作未生效", false);
	}
	
	/**
	 * 人工提现
	 * @return
	 */
	@ActionKey("/withdrawals")
	@AuthNum(value=999)
	@Before({FundsWithdrawalsValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message withdrawals(){
		
		Long payAmount = getParaToLong("amount");
		
		String userCode = getPara("fundsUserCode");
		
		String remark = getPara("remark");
		
		if(StringUtil.isBlank(remark)){
			remark = "人工提现"+DateUtil.getNowDateTime();
		}
		
		int isExist = userService.isExist(userCode);
		if(isExist == 0){
			if(!userService.validateUserState(userCode))
				return error("04", "用户状态非【正常】", false);
			
			long avBlance = fundsServiceV2.findAvBalanceById(userCode);
			if(payAmount > avBlance)
				return error("02", "可用余额不足", false);
			
			if(fundsServiceV2.withdrawals1(userCode, payAmount,0,remark)){
				String userName = userService.findByField(userCode, "userName").getStr("userName");
				String userTureName = userInfoService.findByFields(userCode, "userCardName").getStr("userCardName");
				String withdrawCode = UIDUtil.generate();
				withdrawTraceService.save(withdrawCode,userCode, userName, userTureName, "", "", "", "", "", payAmount,SysEnum.withdrawTraceState.D.val(), "0",remark,DateUtil.getNowDateTime(),null,true);
				return succ("提现操作完成", true);
			}
		}else if(isExist > 0){
			return error("03","用户不存在",false);
		}
		return error("01", "提现操作未生效", false);
	}
	
	/**
	 * 分页查询理财人资金账户
	 */
	@ActionKey("/getFundsByPage1")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFundsByPage1(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		if(!StringUtil.isBlank(beginDate))
			beginDate = beginDate + "000000";
		
		String endDate = getPara("endDate","");
		
		if(!StringUtil.isBlank(endDate))
			endDate = endDate + "235959";
		
		String allkey = getPara("allkey","").trim();
		
		Map<String,Object> result = fundsServiceV2.findByPage4Noob1(pageNumber, pageSize, beginDate, endDate,allkey);
		
		Map<String,Object> result_sum = fundsServiceV2.findByPage4Noob1WithSum(beginDate, endDate, allkey);
		
		result.putAll(result_sum);

		return succ("分页查询会员资金账户信息完成", result);
	}
	
	/**
	 * 分页查询借款人资金账户
	 */
	@ActionKey("/getFundsByPage2")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFundsByPage2(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		if(!StringUtil.isBlank(beginDate))
			beginDate = beginDate + "000000";
		
		String endDate = getPara("endDate","");
		
		if(!StringUtil.isBlank(endDate))
			endDate = endDate + "235959";
		
		String allkey = getPara("allkey","");
		
		Map<String,Object> result = fundsServiceV2.findByPage4Noob2(pageNumber, pageSize, beginDate, endDate,allkey);
		
		Map<String,Object> result_sum = fundsServiceV2.findByPage4Noob2WithSum(beginDate, endDate, allkey);
		
		result.putAll(result_sum);
		
		return succ("分页查询会员资金账户信息完成", result);
	}
	
	/**
	 * 根据id查询一条资金流水明细记录
	 */
	@ActionKey("/getFundsTraceById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFundsTraceById(){
		
		String traceCode = getPara("traceCode","");
		
		FundsTrace fundsTrace = fundsTraceService.findById(traceCode);
		return succ("查询单条资金流水明细完成", fundsTrace);
	}
	
	/**
	 * 更新流水记录对账状态
	 */
	@ActionKey("/updateTraceSynState")
	@Before({FundsUpdateTraceSynStateValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	@AuthNum(value=999)
	public Message updateTraceSynState(){
		
		String traceCode = getPara("traceCode");
		String traceSynState = getPara("traceSynState");
		
		if(fundsTraceService.updateTraceSynState(traceCode, traceSynState))
			return succ("更新资金流水对账状态完成", true);
		else
			return error("01", "更新资金流水对账状态未生效", false);
	}
	
	/**
	 * 分页查询提现流水记录
	 * @return
	 */
	@ActionKey("/getWithdrawalsTraceByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getWithdrawalsTraceByPage(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		if(!StringUtil.isBlank(beginDate))
			beginDate = beginDate + "000000";
		
		String endDate = getPara("endDate","");
		
		if(!StringUtil.isBlank(endDate))
			endDate = endDate + "235959";
		
		String userCode = getPara("fuserCode");
		
		String allkey = getPara("allkey","");
		
		String status = getPara("status","");
		
		String sharen = getPara("sharen","");
		
		String withdrawType = getPara("withdrawType","");

		Map<String,Object> result = withdrawTraceService.findByPage4Noob(userCode,pageNumber, pageSize,beginDate,endDate,allkey,status,sharen,withdrawType);
		
		Map<String,Object> result_sum = withdrawTraceService.findByPage4NoobWithSum(userCode, beginDate, endDate, allkey, status,sharen,withdrawType);
		
		result.putAll(result_sum);
		
		return succ("分页查询资金流水完成", result);
	}
	
	/**
	 * 分页查询充值流水记录
	 * @return
	 */
	@ActionKey("/getRechargeTraceByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getRechargeTraceByPage(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		if(!StringUtil.isBlank(beginDate))
			beginDate = beginDate + "000000";
		
		String endDate = getPara("endDate","");
		
		if(!StringUtil.isBlank(endDate))
			endDate = endDate + "235959";
		
		String userCode = getPara("fuserCode");
		
		String allkey = getPara("allkey","");
		
		String traceState = getPara("traceState","");
		
		String sharen = getPara("sharen","");
		
		String rechargeType = getPara("rechargeType","");
		
		Map<String,Object> result = rechargeTraceService.findByPage4Noob(userCode, pageNumber, pageSize, beginDate, endDate, allkey, traceState,sharen,rechargeType);

		Map<String,Object> result_sum = rechargeTraceService.findByPage4NoobWithSum(userCode, beginDate, endDate, allkey, traceState,sharen,rechargeType);
		
		result.putAll(result_sum);
		
		return succ("分页查询资金流水完成", result);
	}
	
	/**
	 * 分页查询资金流水
	 */
	@ActionKey("/getFundsTraceByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message getFundsTraceByPage(){
		
		Integer pageNumber = getParaToInt("pageNumber",1);
		
		pageNumber = pageNumber > 0 ? pageNumber : 1;
		
		Integer pageSize = getParaToInt("pageSize",10);
		
		String beginDate = getPara("beginDate","");
		
		String endDate = getPara("endDate","");
		
		if(!StringUtil.isBlank(beginDate))
			beginDate = beginDate + "000000";
		
		if(!StringUtil.isBlank(endDate))
			endDate = endDate + "235959";
		
		String traceType = getPara("traceType","");
		
		String fundsType = getPara("fundsType","");
		
		String userCode = getPara("userCode");
		
		Map<String, Object> result = fundsTraceService.findByPage4Noob(pageNumber, pageSize,beginDate, endDate, traceType, fundsType , userCode);
		System.out.println("---"+result.get("list"));
		 ArrayList fundsTraces= (ArrayList) result.get("list");
				   
		
		for(int i=0;i<fundsTraces.size();i++){
              FundsTrace fundstrace=(FundsTrace) fundsTraces.get(i);
              try {
				
				fundstrace.put("userMobile", CommonUtil.decryptUserMobile(fundstrace.getStr("userMobile")));
				result.put("list", fundsTraces);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			   
			  }
		Map<String, Object> result_sum = fundsTraceService.findByPage4NoobWithSum(beginDate, endDate, traceType, fundsType, userCode);
		
		result.putAll(result_sum);
		
		return succ("分页查询资金流水完成", result);
	}
	
	// TODO 商银信商户提现
	/**
	 * 商银信商户提现-非理财人
	 */
	@ActionKey("/abcdefghijklmn")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message abcdefghijklmn(){
		String cardName = getPara("cardName","");
		String cardNo = getPara("cardNo","");
		String cardProv = getPara("cardProv","");
		String cardCity = getPara("cardCity","");
		String cardType = getPara("cardType","");
		long payAmount = getParaToLong("payAmount");
		
		if(StringUtil.isBlank(cardNo))
			return error("01", "请填写卡号", false);
		if(StringUtil.isBlank(cardName))
			return error("02", "请填写户名", false);
		if(StringUtil.isBlank(cardProv))
			return error("03", "请填写省份", false);
		if(StringUtil.isBlank(cardCity))
			return error("04", "请填写城市", false);
		if(StringUtil.isBlank(cardType))
			return error("05", "请选择银行", false);
		try {
			if(payAmount < 10000)
				return error("06", "交易金额必须大于100元", false);
		} catch (Exception e) {
			return error("06", "交易金额必须大于100元", false);
		}
		
		
		String orderNo = CommonUtil.genShortUID();
		
		Map<String, String> result = SYXPayKit.payIn(orderNo, payAmount, "商户提现", cardName,
				cardNo, cardType, cardProv, cardCity, "商户提现");
		if(result.get("retCode").equals("0000")){
			if(result.get("status").equals("04")){
				return succ("操作完成", "受理成功|交易成功|订单号："+orderNo);
			}else if(result.get("status").equals("00")){
				return succ("操作完成", "受理成功|交易处理中|订单号："+orderNo);
			}else{
				return succ("操作完成", "受理成功|交易失败:"+result.get("retMsg")+"|订单号："+orderNo);
			}
			
		}else if(result.get("retCode").equals("1009")){
			return error("98", "商户账户余额不足", null);
		}else{
			return error("99", "受理失败："+result.get("retMsg"), null);
		}
		
	}
	
	
	/**
	 * 获取投资排行数据
	 * @return
	 */
	@ActionKey("/fuck4date")
	@ResponseCached(cachedKey="fuck4date", cachedKeyParm="",mode="remote" , time=3)
	@Before( {GET.class,PkMsgInterceptor.class} )
	public Message activity4month(){
		
		String startDate = getPara("startDate");
		String endDate = getPara("endDate");
		
		if(StringUtil.isBlank(startDate)){
			startDate = DateUtil.getNowDate();
		}
		if(StringUtil.isBlank(endDate)){
			endDate = DateUtil.getNowDate();
		}
		
		Map<String,Object> resultMap = new HashMap<String,Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(startDate+"000000", endDate + "235959", 1, 10);
		
		List<FundsTrace> result = new ArrayList<FundsTrace>();
		if(null != month){
			for (int i = 0; i < month.size(); i++) {
				
				FundsTrace fundsTrace = month.get(i);
				String userCode = fundsTrace.getStr("userCode");
				User user = userService.findById(userCode);
				String userMobile = "";
				try {
					userMobile = CommonUtil.decryptUserMobile(user.getStr("userMobile"));
				} catch (Exception e) {
					e.printStackTrace();
				}
				fundsTrace.set("traceRemark", userMobile);
				result.add(fundsTrace);
			}
		}
		
		resultMap.put("month", result);
		return succ("查询成功", resultMap);
	}
	

	/**
	 * 同步用户存管资金至平台资金
	 */
	@ActionKey("/syncDepositoryAccount")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message syncDepositoryAccount() {
		String userCode = getPara("userCode");
		if (null == userCode || "".equals(userCode)) {
			return error("01", "获取用户信息失败", null);
		}
		User user = userService.findById(userCode);
		if (CommonUtil.fuiouPort) {
			if (!userService.isFuiouAccount(userCode) || !fuiouTraceService.isfuiouAcount(user)) {
				return error("02", "用户还未开通存管账号", null);
			}
			QueryBalanceResultData fuiouFunds = fuiouTraceService.BalanceFunds(user);
			Funds funds = fundsServiceV2.syncAccount(userCode, Long.parseLong(fuiouFunds.getCa_balance()), Long.parseLong(fuiouFunds.getCf_balance()));
			if (funds == null) {
				return error("03", "同步账户资金失败", null);
			}
			OPUserV2 opUserV2 = opUserService.findById(getUserCode());
			BIZ_LOG_INFO(getUserCode(), BIZ_TYPE.SYNC, "[" + userCode + "][" + user.getStr("userName") + "]同步存管资金账户，操作人["+opUserV2.getStr("op_code")+"]["+opUserV2.getStr("op_name")+"]");
			return succ("同步账户资金成功", "同步账户资金成功");
			
		} else if(CommonUtil.jxPort) {
			if(user != null && StringUtil.isBlank(user.getStr("jxAccountId"))){
				return error("02","用户还未开通存管账户",null);
			}
			Map<String, String> balanceQuery = JXQueryController.balanceQuery(user.getStr("jxAccountId"));
			long availBal = StringUtil.getMoneyCent(balanceQuery.get("availBal"));
			long currBal = StringUtil.getMoneyCent(balanceQuery.get("currBal"));
			long freezeBal = currBal - availBal;
			Funds funds = fundsServiceV2.syncAccount(userCode, availBal,freezeBal );
			if(funds == null){
				return error("01","同步资金失败",null);
			}
			OPUserV2 opUserV2 = opUserService.findById(getUserCode());
			BIZ_LOG_INFO(getUserCode(), BIZ_TYPE.SYNC, "[" + userCode + "][" + user.getStr("userName") + "]同步存管资金账户，操作人["+opUserV2.getStr("op_code")+"]["+opUserV2.getStr("op_name")+"]");
			return succ("同步账户资金成功", "同步账户资金成功");
		}
		return error("01","平台存管暂未开启",null);
	}
	
	/**
	 * 检查资金同步状态
	 */
	@ActionKey("/depositorySyncState")
	@Before({PkMsgInterceptor.class})
	public Message depositorySyncState() {
		String userCode = getPara("userCode");
		if (null == userCode || "".equals(userCode)) {
			return error("01", "获取用户信息失败", null);
		}
		User user = userService.findById(userCode);
		if (!userService.isFuiouAccount(userCode) || !fuiouTraceService.isfuiouAcount(user)) {
			return error("02", "未开通", null);
		}
		Funds funds = fundsServiceV2.findById(userCode);
		if (null == funds) {
			return error("03", "获取用户资金信息异常", null);
		}
		QueryBalanceResultData fuiouFunds = fuiouTraceService.BalanceFunds(user);
		if (null == fuiouFunds) {
			return error("12", "获取存管账户信息异常", null);
		}
		if (funds.getLong("avBalance") != Long.parseLong(fuiouFunds.getCa_balance())
				|| funds.getLong("frozenBalance") != Long.parseLong(fuiouFunds.getCf_balance())) {
			return error("04", "资金不同步", null);
		}
		return succ("sync", "正常");
	}

	/**
	 * 放款至借款人账户
	 */
	@ActionKey("/fkToAccount")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public Message fkToAccount() {
		String loanNo = getPara("loanNo");
		if (null == loanNo || "".equals(loanNo)) {
			return error("01", "获取标信息失败", null);
		}
		LoanApply loanApply = loanApplyService.findById(Integer
				.parseInt(loanNo));
		String loanUserCode = loanApply.getStr("loanUserCode");
		Long loanAmount = loanApply.getLong("loanAmount");
		User user = userService.findById(loanUserCode);
		if (!userService.isFuiouAccount(loanUserCode)
				|| !fuiouTraceService.isfuiouAcount(user)) {
			return error("02", "借款人未开通存管", null);
		}
		Funds funds = fundsServiceV2.findById(loanUserCode);
		QueryBalanceResultData fuiouFunds = fuiouTraceService
				.BalanceFunds(user);
		if (funds.getLong("avBalance") != Long.parseLong(fuiouFunds
				.getCa_balance())
				|| funds.getLong("frozeBalance") != Long.parseLong(fuiouFunds
						.getCf_balance())) {
			return error("03", "资金异常无法放款", null);
		}
		User wzuser = userService.findByMobile(CommonUtil.OUTCUSTNO);
		String userCode = getUserCode();
		OPUserV2 opUser = opUserService.findById(userCode);
		Funds zz = fundsServiceV2.doAvBalance4biz(loanUserCode,
				Long.valueOf(loanAmount), 0, traceType.HF, fundsType.J, "后台放款");
		if (zz != null) {
			bizLog(userCode, BIZ_TYPE.R, "I", opUser.getStr("op_name")
					+ "操作放款成功");
			loanApply.set("applyState", SysEnum.applyState.Y.val());
			loanApplyService.saveOrUpdateLoanApply(loanApply);
			fuiouTraceService.refund(Long.valueOf(loanAmount),
					FuiouTraceType.HFK, wzuser, user,
					loanApply.getStr("loanCode"));
		}
		return succ("放款成功", "放款成功");

	}

	/**
	 * 借款人提现
	 * */
	@ActionKey("/loanUserWithdraw")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message loanUserWithdraw(){
		OPUserV2 opUserV2 = opUserService.findById(getUserCode());
		String loanNo = getPara("loanNo");
		String userMobile = getPara("userMobile");
		User user = userService.find4mobile(userMobile);
		if(null==user||StringUtil.isBlank(user.getStr("loginId"))){
			return error("01", "未查到相关借款人信息", "");
		}
		String userCode = user.getStr("userCode");//借款人编号
		long amount = getParaToLong("amount")*100;
		LoanApply loanapply = loanApplyService.findByLoanNoAndUserCode(loanNo,userCode);
		if(null==loanapply){
			return error("02", "借款信息不合", "");
		}else{
			if(loanapply.getStr("applyUserGroup").equals(opUserV2.getStr("op_group"))||"总部".equals(opUserV2.getStr("op_group"))){
				//验证是否已提现成功
//				WithdrawTrace withdrawTrace = withdrawTraceService.findByUcodeAndLcode(userCode,loanapply.getStr("loanCode"),"3");
//				if(null!=withdrawTrace){
//					return error("06", "此标借款人已提现完成", "");
//				}
				//提现操作
				UserInfo userInfo = userInfoService.findById(userCode);
				BanksV2 bank = BanksV2.bankV2Dao.findById(userCode);
				String withdrawCode = CommonUtil.genMchntSsn();
				// 新增提现申请记录
				boolean result = false;
				result = withdrawTraceService.save4loanUser(withdrawCode, userCode,
						user.getStr("userName"), userInfo.getStr("userCardName"),
						bank.getStr("bankNo"), bank.getStr("bankNo"),
						bank.getStr("bankType"), bank.getStr("bankName"),
						bank.getStr("cardCity"), amount, "2", "0", "借款人申请提现",
						opUserV2.getStr("opCode"),opUserV2.getStr("opUserName"),
						loanapply.getStr("loanInfoCode"));
				if (result == false) {
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS,
							"借款人申请提现失败，提现异常-07", null);
					return error("07", "提现异常07", "");
				} else {
					// 记录日志
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "借款人申请提现成功，提现金额 ："
							+ amount / 10.0 / 10.0+";操作员编号："+opUserV2.getStr("opCode")+"标号:"+loanNo);
				}
				//存管数据
				AppTransReqData appTransReqData = new AppTransReqData();
				String login_id = user.getStr("loginId");
				try {
					login_id = CommonUtil.decryptUserMobile(login_id);
				} catch (Exception e) {
					return error("05", "系统错误", "");
				}
				appTransReqData.setAmt(String.valueOf(amount));
				appTransReqData.setLogin_id(login_id);
				appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
				appTransReqData.setMchnt_txn_ssn(withdrawCode);
				appTransReqData.setPage_notify_url("http://niux.yrhx.com:8899/withdrawPageNotify");
				try {
					FuiouService.p2p500003(appTransReqData, getResponse());
				} catch (Exception e) {
					return error("04", "操作失败", "");
				}
				//end
			}else{
				return error("03", "操作人身份验证失败", "");
			}
		}
		return null;
	}
	
	/**
	 * 后台接收富友返回参数 20180328 ws
	 */
	@ActionKey("/withdrawPageNotify")
	@AuthNum(value = 999)
	@Before({ PkMsgInterceptor.class })
	public void withdrawPageNotify() {
		String resp_code = getPara("resp_code");
		String mchnt_cd = getPara("mchnt_cd");
		String mchnt_txn_ssn = getPara("mchnt_txn_ssn");
		
		WithdrawTrace withdrawTrace = withdrawTraceService
				.findById(mchnt_txn_ssn);
		if (withdrawTrace == null) {
			redirect("/withdrawFaild?err=" + resp_code, true);
			return ;
		}
		String userCode = withdrawTrace.getStr("userCode");
		try {
			if (mchnt_cd == null || !mchnt_cd.equals(FuiouController.MCHNT_CD)) {
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "提现失败_商户号不符", null);
				redirect("/withdrawFaild?err=" + resp_code, true);
					return ;
			}
			long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
			
		//  如果交易状态已更新，直接返回成功响应
			if ("3".equals(withdrawTrace.getStr("status"))) {
				forward("/pay/withdrawSuccess.html", true);
				return ;
				}
					
			if ("0000".equals(resp_code)) {
				withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
				withdrawTrace.set("status", "3");
				withdrawTrace.set("withdrawRemark", "借款人提现成功");
				User user = userService.findById(userCode);
				if (withdrawTrace.update()) {
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "借款人提现成功，提现金额 ："
									+ withdrawAmount / 10.0 / 10.0);// 扣除提现抵扣积分
					fuiouTraceService.fuiouTraceContent(mchnt_txn_ssn, user,
							withdrawAmount, FuiouTraceType.B);
					
				//  修改资金账户可用余额
					boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
							userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "借款人提现成功");
					if (withdrawals4Fuiou == false) {
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现修改资金失败 ", null);
						redirect("/withdrawFaild?err=" + resp_code, true);
							return ;
					} else {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现同步修改资金" + withdrawAmount);
						forward("/pay/withdrawSuccess.html", true);
							return ;
					}
				} else {
					redirect("/withdrawFaild?err=" + resp_code, true);
						return ;
				}
			} else {
				withdrawTrace.set("status", "4");
				withdrawTrace.set("withdrawRemark", "提现失败");
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败" + resp_code, null);
				withdrawTrace.update();
				redirect("/withdrawFaild?err=" + resp_code, true);
				return ;
			}
		} catch (Exception e) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "借款人提现失败,系统异常", null);
			redirect("/withdrawFaild?err=" + resp_code, true);
		}

	}
	
	/**
	 * 借款人提现——江西银行
	 * @return
	 */
	@ActionKey("/loanUserWithdrawByJX")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message loanUserWithdrawByJX(){
		String opUserCode = getUserCode();
		OPUserV2 opUserV2 = opUserService.findById(opUserCode);
		if(opUserV2 == null){
			return error("11", "操作员信息异常", "");
		}
		String opName = opUserV2.getStr("op_name");
		String opCode = opUserV2.getStr("op_code");
		if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
			return error("11", "操作员信息异常", false);
		}
		String loanNo = getPara("loanNo");
		String userMobile = getPara("userMobile");
		User user = userService.find4mobile(userMobile);
		if(null==user||StringUtil.isBlank(user.getStr("jxAccountId"))){
			return error("01", "未查到相关借款人信息", "");
		}
		String userCode = user.getStr("userCode");//借款人编号
		String amt = getPara("amount","0");
		long amount = StringUtil.getMoneyCent(amt.trim());
		if(amount<=0){
			return error("01", "提现金额必须大于0", "");
		}
		LoanApply loanapply = loanApplyService.findByLoanNoAndUserCode(loanNo,userCode);
		if(null==loanapply){
			return error("02", "借款信息不合", "");
		}else{
			long loanAmount = loanapply.getLong("loanAmount");
			//单次提现金额不能超过标的金额
			if(amount>loanAmount){
				return error("03", "提现金额不能大于标的金额", "");
			}
			if(loanapply.getStr("applyUserGroup").equals(opUserV2.getStr("op_group"))||"总部".equals(opUserV2.getStr("op_group"))){
				//验证累计提现金额不能超过标的金额
				long totalAmount = Db.queryBigDecimal("select COALESCE(sum(withdrawAmount),0) from t_withdraw_trace where userCode=? and loanApplyCode=? and status in(?,?)",userCode,loanapply.getStr("loanInfoCode"),"1","3").longValue();
				if(totalAmount+amount>loanAmount){
					return error("05", "该标的已经提现成功："+totalAmount+",请确保提现金额在借款标的范围内。", "");
				}
				//提现操作
				UserInfo userInfo = userInfoService.findById(userCode);
				BanksV2 bank = banksService.findByUserCode(userCode);
				String withdrawCode = CommonUtil.genMchntSsn();
				// 新增提现申请记录
				boolean result = false;
				result = withdrawTraceService.save4loanUser(withdrawCode, userCode,user.getStr("userName"), userInfo.getStr("userCardName"),
						bank.getStr("bankNo"), bank.getStr("bankNo"),"", bank.getStr("bankName"),"", amount, "2", "0", "借款人申请提现",
						opCode,opName,loanapply.getStr("loanInfoCode"));
				if (result == false) {
					// 记录日志
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS,"借款人申请提现失败，提现异常-07", null);
					return error("07", "提现异常07", "");
				} else {
					// 记录日志
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "借款人申请提现成功，提现金额 ："
							+ amount / 10.0 / 10.0+";操作员编号："+opUserV2.getStr("opCode")+"标号:"+loanNo);
				}
				String jxAccountId = user.getStr("jxAccountId");//存管电子账户
				String idType = userInfo.getStr("idType");//证件类型
				String userCardId = userInfo.getStr("userCardId");//证件号码
				try {
					userCardId = CommonUtil.decryptUserCardId(userCardId);
				} catch (Exception e1) {
					return error("05", "证件号码解析异常", "");
				}
				String trueName = userInfo.getStr("userCardName");//姓名
				String mobile = bank.getStr("mobile");//存管手机号
				try {
					mobile = CommonUtil.decryptUserMobile(mobile);
				} catch (Exception e1) {
					return error("05", "手机号解析异常", "");				
				}
				String bankNo = bank.getStr("bankNo");//银行卡号
				String txAmount = StringUtil.getMoneyYuan(amount);
				String txFee = "0";//借款人提现不收手续费
				String routeCode = getPara("routeCode","");
				String cardBankCnaps = getPara("cardBankCnaps","");
				//对公账户提现标识  Y:对公  N:对私 不上送默认为N
				String businessAccountIdFlag = "N";
				if(!"01".equals(idType) && !StringUtil.isBlank(idType)){
					//企业户上送Y
					businessAccountIdFlag = "Y";
				}
				String retUrl = CommonUtil.NIUX_URL+"/main";
				String forgotPwdUrl = CommonUtil.NIUX_URL+"/main";
				String notifyUrl = CommonUtil.NIUX_URL+"/loanUserWithdrawNotify?withdrawCode=" + withdrawCode;//http://niux.yrhx.com:8899/withdrawPageNotify
				
				try {
					JXController jxController = new JXController();
					HttpServletResponse response = getResponse();
					jxController.withdrawPage(jxAccountId, trueName, bankNo, idType, userCardId, mobile, txAmount, txFee, routeCode, cardBankCnaps, retUrl, notifyUrl, forgotPwdUrl, "", response, businessAccountIdFlag);
				} catch (Exception e) {
					return error("04", "操作失败", "");
				}
				//end
			}else{
				return error("03", "操作人身份验证失败", "");
			}
		}
		return null;
	}
	
	@ActionKey("/loanUserWithdrawNotify")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void loanUserWithdrawNotify(){
		HttpServletRequest request = getRequest();
		String withdrawCode = getPara("withdrawCode","");
		String parameter = request.getParameter("bgData");
		@SuppressWarnings("unchecked")
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
		
		//检查数据中有没有此流水的提现申请
		if(withdrawTrace == null){
			return ;
		}
		String userCode = withdrawTrace.getStr("userCode");
		
		long sxf = (long)withdrawTrace.getInt("sxf");
		long amount = StringUtil.getMoneyCent(resMap.get("txAmount"));
		
		long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
		if(withdrawAmount != (amount + sxf)){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "平台提现金额与存管提现金额不符", null);
			return ;
		}
		
		//检查交易状态是否已更新
		if("3".equals(withdrawTrace.getStr("status"))){
			renderText("success");
			return ;
		}
		
		withdrawTrace.set("bankTraceCode", jxTraceCode);
		
		if(dealRetCode(resMap.get("retCode"))){
			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "1");
			withdrawTrace.set("withdrawRemark", "已审核");
			
			if(withdrawTrace.update()){
				//修改资金账户可用余额
				boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
						userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
				if (withdrawals4Fuiou == false) {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_修改资金失败 ", null);
					return ;
				} else {//成功
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_同步修改资金" + withdrawAmount);
				}
				
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_流水更新失败", null);
				return ;
			}
		}else{
			withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "4");
			withdrawTrace.set("withdrawRemark", "提现失败:" + resMap.get("retCode") + "_" + resMap.get("retMsg"));
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_" + resMap.get("retCode"), null);
			if (withdrawTrace.update()) {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_更新提现流水");
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_流水未更新", null);
			}
		}
		renderText("success");
	}
	/**
	 * 处理提现响应码
	 * @param retCode
	 * @return
	 */
	public static boolean dealRetCode(String retCode){
		if("00000000".equals(retCode) || "CE999028".equals(retCode) || "CT9903".equals(retCode) || "CT990300".equals(retCode)
				|| "CE999999".equals(retCode) || "510000".equals(retCode) || "502".equals(retCode) || "504".equals(retCode)){
			return true;
		}
		return false;
	}
	
	/**
	 * 圈存(暂时不适用)
	 * @return
	 */
	@ActionKey("/creditForLoad")
	@AuthNum(value = 999)
	@Before({FundsRechargeValidator.class,AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message creditForLoad(){
		long payAmount = getParaToLong("amount");
		String userCode = getPara("fundsUserCode");
		
		String remark = getPara("remark");
		if(StringUtil.isBlank(remark)){
			remark = "圈存"+DateUtil.getNowDateTime();
		}
		
		int isExist = userService.isExist(userCode);
		User user= userService.findById(userCode);
		if(user == null){
			return error("03", "用户信息不存在", "");
		}
		String jxAccountId = user.getStr("jxAccountId");
		if(!StringUtil.isBlank(jxAccountId)){
       	 if(isExist == 0){
       		 UserInfo userInfo = userInfoService.findById(userCode);
       		 BanksV2 banksV2 = banksService.findByUserCode(userCode);
       		 String idType = userInfo.getStr("idType");
       		 String idNo = userInfo.getStr("userCardId");
       		 String name = userInfo.getStr("userCardName");
       		 String mobile = banksV2.getStr("mobile");//存管预留手机号
       		 try {
       			idNo = CommonUtil.decryptUserCardId(idNo);
       			mobile = CommonUtil.decryptUserMobile(mobile);
			} catch (Exception e) {
				return error("05", "身份信息解析异常", "");
			}
       		 
       		 String bankNo = banksV2.getStr("bankNo");
       		 if(StringUtil.isBlank(idType) || StringUtil.isBlank(idNo) || StringUtil.isBlank(name) || StringUtil.isBlank(mobile) 
       				 || StringUtil.isBlank(bankNo)){
       			 return error("06", "请求参数异常", "");
       		 }
       		//存储订单信息
     		Map<String, String> payParam = new HashMap<String, String>(); 	
     		//流水号
     		payParam.put("no_order", CommonUtil.genMchntSsn());
     		payParam.put("user_id", userCode);
     		payParam.put("bank_code", "");//银行编码
     		payParam.put("userBankName", banksV2.getStr("bankName"));//银行名称
     		payParam.put("info_order", "易融恒信理财充值-圈存");
     		
     		//订单入库
     		RechargeTrace trace = map2trace(payParam);
     		trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.QC.key());//圈存
     		trace.set("traceAmount", payAmount);
     		trace.set("bankRemark", "快捷支付,发起申请");
     		trace.set("traceRemark", "快捷支付,发起申请");
     		trace.set("userName", user.getStr("userName"));
     		trace.set("userBankNo", bankNo);
     		
       		//返回交易页面链接
     		String retUrl = CommonUtil.NIUX_URL+"/main";
     		String successfulUrl = "";
     		//后台响应链接
     		String notifyUrl = CommonUtil.NIUX_URL+"/showCreditForLoadResult?traceCode=" + payParam.get("no_order");
     		//忘记密码链接
     		String forgotPwdUrl = CommonUtil.NIUX_URL;
     		JXController jxController = new JXController();
    		HttpServletResponse response = getResponse();
    		jxController.creditForLoadPage(jxAccountId, bankNo, StringUtil.getMoneyYuan(payAmount), idType, idNo, mobile, name, retUrl, forgotPwdUrl, successfulUrl, notifyUrl, response);
    		
    		if(trace.save()){
    			return Message.succ("ok", payParam);
    		}else{
    			return Message.error("10", "生成充值订单失败!", null);
    		}
			}else if(isExist > 0){
				return error("03","用户不存在",false);
			} 
       	 
        }else{
       	 return error("02","用户未开通存管",false);
        }
		return error("01", "充值操作未生效", false);
	}
	
	private RechargeTrace map2trace(Map<String, String> map) {
		RechargeTrace trace = new RechargeTrace();
		String userCode = String.valueOf(map.get("user_id"));
		UserInfo info = userInfoService.findById(userCode);
		if (info != null) {
			trace.set("userTrueName", info.getStr("userCardName"));
		}
		String tmpBankCode = String.valueOf(map.get("bank_code"));
		trace.set("userBankCode", tmpBankCode);
		trace.set("userBankName", map.get("userBankName"));
		trace.set("traceCode", map.get("no_order"));
		trace.set("bankTraceCode", map.get("no_order"));
		trace.set("bankState", RechargeTrace.BANK_STATE.ACCEPT.key()); // A-已受理
		trace.set("traceState", RechargeTrace.TRACE_STATE.DOING.key());
		trace.set("userCode", userCode);
		trace.set("traceDateTime", DateUtil.getNowDateTime());
		trace.set("traceDate", DateUtil.getNowDate());
		trace.set("modifyDateTime", DateUtil.getNowDateTime());
		trace.set("modifyDate", DateUtil.getNowDate());

		return trace;
	}
	
	/**
	 * 圈存响应
	 */
	@ActionKey("/showCreditForLoadResult")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showCreditForLoadResult(){
		
		HttpServletRequest request = getRequest();
		String traceCode = getPara("traceCode");
		//响应数据
		String parameter = request.getParameter("bgData");
		
		@SuppressWarnings("unchecked")
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		
		RechargeTrace trace = RechargeTrace.rechargeTraceDao.findById(traceCode);
		//检查是否存在发起充值的流水记录
		if(trace == null){
			return ;
		}
		String userCode = trace.getStr("userCode");
		long traceAmount = trace.getLong("traceAmount");
		String txAmount = resMap.get("txAmount");
		long amt = Double.valueOf(txAmount).longValue()*100;
		//检查充值金额是否一致
		if(amt != traceAmount){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值金额不符", null);
			return ;
		}
		
		//检查订单交易状态
		if("B".equals(trace.getStr("traceState"))){
			return ;
		}
		if("F".equals(trace.getStr("traceState"))){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "重复接收响应：充值订单["+ traceCode+"]处于锁定状态", null);
			return;
		}
		//锁定订单,避免重复操作
		int lockOrder = Db.update("update t_recharge_trace set traceState=? where traceCode=? and traceState=?" , 
				TRACE_STATE.LOCKED.key() , traceCode ,TRACE_STATE.DOING.key() );
		if( lockOrder < 1 ){
			BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值订单已被锁定，正在处理中……", traceCode);
			return ;
		}
		
		if("00000000".equals(resMap.get("retCode"))){
			//更新订单状态
			trace.set("bankTraceCode", jxTraceCode);
			trace.set("bankState", BANK_STATE.SUCCESS.key());
			trace.set("traceState", TRACE_STATE.SUCCESS.key());
			if("QC".equals(trace.getStr("rechargeType"))){
				trace.set("traceRemark", "圈存_充值成功");
				trace.set("bankRemark", "圈存_充值成功");
			}
			trace.set("okDateTime", DateUtil.getNowDateTime());
			
			
			if(trace.update()){
				//修改资金账户
				if("QC".equals(trace.getStr("rechargeType"))){
					fundsServiceV2.recharge(userCode, (long)(Double.parseDouble(resMap.get("txAmount"))*100), 0, "快捷支付，充值成功", SysEnum.traceType.C.val());
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值同步修改资金:" + resMap.get("txAmount"));
				}else{
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值同步修改失败", null);
				}
			}else{
				//资金修改不成功--充值订单解锁
				int unLockOrder = Db.update("update t_recharge_trace set traceState=? where traceCode=? and traceState=?" , 
						TRACE_STATE.DOING.key() , traceCode ,TRACE_STATE.LOCKED.key() );
				if( unLockOrder > 0 ){
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败_充值订单解锁成功", traceCode);
				}else{
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败_充值订单解锁失败", traceCode);
				}
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值流水更新失败", null);
				return ;
			}
		}else{//充值失败
			trace.set("bankTraceCode", jxTraceCode);
			trace.set("bankState", BANK_STATE.FAILD.key());
			trace.set("traceState", TRACE_STATE.FAILD.key());
			if ("QC".equals(trace.getStr("rechargeType"))) {
				trace.set("traceRemark", "圈存_充值失败");
				trace.set("bankRemark", "圈存_充值失败");
			}
			if (trace.update()) {					
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值失败_更新充值流水", null);
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败_充值流水未更新", null);
			}
		}	
		renderText("success");
	}
	
	/**
	 * 圈提(暂时不适用)
	 * @return
	 */
	@ActionKey("/creditForUnload")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message creditForUnload(){
		String opUserCode = getUserCode();
		OPUserV2 opUserV2 = opUserService.findById(opUserCode);
		if(opUserV2 == null){
			return error("11", "操作员信息异常", "");
		}
		String opName = opUserV2.getStr("op_name");
		String opCode = opUserV2.getStr("op_code");
		if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
			return error("11", "操作员信息异常", false);
		}
		long payAmount = getParaToLong("amount");
		String userCode = getPara("fundsUserCode");
		
		String remark = getPara("remark");
		if(StringUtil.isBlank(remark)){
			remark = "圈提"+DateUtil.getNowDateTime();
		}
		
		int isExist = userService.isExist(userCode);
		User user= userService.findById(userCode);
		String jxAccountId = user.getStr("jxAccountId");
		if(!StringUtil.isBlank(jxAccountId)){
			if(isExist == 0){
				if(!"总部".equals(opUserV2.getStr("op_group"))){
					return error("03", "非权限范围内请勿操作", "");
				}
				UserInfo userInfo = userInfoService.findById(userCode);
				BanksV2 banksV2 = banksService.findByUserCode(userCode);
	       		 String idType = userInfo.getStr("idType");
	       		 String idNo = userInfo.getStr("userCardId");
	       		 String trueName = userInfo.getStr("userCardName");
	       		 String name = user.getStr("userName");
	       		 String mobile = banksV2.getStr("mobile");//存管预留手机号
	       		 try {
	       			idNo = CommonUtil.decryptUserCardId(idNo);
	       			mobile = CommonUtil.decryptUserMobile(mobile);
				} catch (Exception e) {
					return error("05", "身份信息解析异常", "");
				}
	       		 
	       		 String bankNo = banksV2.getStr("bankNo");
	       		 if(StringUtil.isBlank(idType) || StringUtil.isBlank(idNo) || StringUtil.isBlank(name) || StringUtil.isBlank(mobile) 
	       				 || StringUtil.isBlank(bankNo)){
	       			 return error("06", "请求参数异常", "");
	       		 }
	       		 String withdrawCode = CommonUtil.genMchntSsn();
	       		// 新增提现申请记录
	     		boolean result = false;
	     		result = withdrawTraceService.savePlatUser(withdrawCode, userCode, name, trueName, bankNo, bankNo, "", banksV2.getStr("bankName"), "", payAmount, "2", "0", "平台账户申请提现_"+remark, opCode, opName);
	     		if (result == false) {
	     			// 记录日志
	     			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS,
	     					"平台账户申请提现失败，提现异常-07", null);
	     			return error("07", "提现异常07", "");
	     		} else {
	     			// 记录日志
	     			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "平台账户申请提现成功，提现金额 ："
	     					+ payAmount / 10.0 / 10.0);				
	     		}
	       		//返回交易页面链接
	      		String retUrl = CommonUtil.NIUX_URL+"/main";
	      		String successfulUrl = "";
	      		//后台响应链接
	      		String notifyUrl = CommonUtil.NIUX_URL+"/showCreditForUnloadResult?withdrawCode="+withdrawCode;
	      		//忘记密码链接
	      		String forgotPwdUrl = CommonUtil.NIUX_URL;
	      		JXController jxController = new JXController();
	     		HttpServletResponse response = getResponse();
	     		jxController.creditForUnloadPage(jxAccountId, bankNo, "0", StringUtil.getMoneyYuan(payAmount), idType, idNo, mobile, trueName, forgotPwdUrl, retUrl, notifyUrl, successfulUrl, response);
	     		return succ("申请提现成功，请等待审核", "");
			}else if(isExist > 0){
				return error("03","用户不存在",false);
			} 
		}else{
	       	 return error("02","用户未开通存管",false);
	    }
		return error("01", "圈提操作未生效", false);
	}
	
	@ActionKey("/showCreditForUnloadResult")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void showCreditForUnloadResult(){
		HttpServletRequest request = getRequest();
		String withdrawCode = getPara("withdrawCode","");
		String parameter = request.getParameter("bgData");
		@SuppressWarnings("unchecked")
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
		
		//检查数据中有没有此流水的提现申请
		if(withdrawTrace == null){
			return ;
		}
		String userCode = withdrawTrace.getStr("userCode");
		
		long sxf = (long)withdrawTrace.getInt("sxf");
		long amount = StringUtil.getMoneyCent(resMap.get("txAmount"));
		
		long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
		if(withdrawAmount != (amount + sxf)){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "平台提现金额与存管提现金额不符", null);
			return ;
		}
		
		//检查交易状态是否已更新
		if("3".equals(withdrawTrace.getStr("status"))){
			renderText("success");
			return ;
		}
		
		withdrawTrace.set("bankTraceCode", jxTraceCode);
		
		if(dealRetCode(resMap.get("retCode"))){
			withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "1");
			withdrawTrace.set("withdrawRemark", "已审核");
			
			if(withdrawTrace.update()){
				//修改资金账户可用余额
				boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
						userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
				if (withdrawals4Fuiou == false) {
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_修改资金失败 ", null);
					return ;
				} else {//成功
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_同步修改资金" + withdrawAmount);
				}
				
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_流水更新失败", null);
				return ;
			}
		}else{
			withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
			withdrawTrace.set("status", "4");
			withdrawTrace.set("withdrawRemark", "提现失败:" + resMap.get("retCode") + "_" + resMap.get("retMsg"));
			// 记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_" + resMap.get("retCode"), null);
			if (withdrawTrace.update()) {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_更新提现流水");
			}else{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_流水未更新", null);
			}
		}
		renderText("success");
	}
	
	/**
	 * 检查账户用途
	 * @return
	 */
	@ActionKey("/validAcctUse")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message validAcctUse(){
		String userCode = getPara("fundsUserCode");
		User user = userService.findById(userCode);
		if(user == null){
			return error("01", "未查找到用户信息", "");
		}
		String jxAccountId = user.getStr("jxAccountId");
		if(StringUtil.isBlank(jxAccountId)){
			return error("02", "还未开通存管账户", "");
		}
		Map<String, String> balanceQuery = JXQueryController.balanceQuery(jxAccountId);
		if(balanceQuery == null){
			return error("03", "未查找到用户的存管余额", "");
		}
		String acctUse = balanceQuery.get("acctUse");
		if(StringUtil.isBlank(acctUse)){
			return error("666", "非红包、手续费账户不支持该操作", "");
		}
		if("10000".equals(acctUse) || "01000".equals(acctUse)){
			return succ("ok", "00");
		}
		return error("666", "非红包、手续费账户不支持该操作", "");
	}
	
	
	/**
	 * 平台自有资金账户充值——圈存
	 */
	@ActionKey("/creditForLoad_QC")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message creditForLoad_QC(){
		String opUserCode = getUserCode();
		OPUserV2 opUserV2 = opUserService.findById(opUserCode);
		if(opUserV2 == null){
			return error("11", "操作员信息异常", "");
		}
		String opName = opUserV2.getStr("op_name");
		String opCode = opUserV2.getStr("op_code");
		if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
			return error("11", "操作员信息异常", false);
		}
		if(!"总部".equals(opUserV2.getStr("op_group"))){
			return error("03", "非权限范围内请勿操作", "");
		}
		String type = getPara("type");
		long amt = getParaToLong("amount");
		String accountId = "";
		String idType = "";
		String idNo = "";
		String mobile = "";
		String bankNo = "791913717300033";
		String name = "武汉易融恒信金融信息服务有限公司";
		if("1".equals(type)){//红包账户
			accountId = JXService.RED_ENVELOPES;
			mobile = JXService.RED_ACCOUNT_MOBILE;
			idType = "25";
			idNo = "91420103303535758R";
		}else{//手续费账户
			accountId = JXService.FEES;
			mobile = JXService.FEES_ACCOUNT_MOBILE;
			idType = "20";
			idNo = "G1042010304144850U";
		}
		//返回交易页面链接
 		String retUrl = CommonUtil.NIUX_URL+"/main";
 		String successfulUrl = "";
 		//后台响应链接
 		String notifyUrl = CommonUtil.NIUX_URL+"/showResult_QC_OR_QT" ;
 		//忘记密码链接
 		String forgotPwdUrl = CommonUtil.NIUX_URL;
 		JXController jxController = new JXController();
		HttpServletResponse response = getResponse();
		jxController.creditForLoadPage(accountId, bankNo, StringUtil.getMoneyYuan(amt), idType, idNo, mobile, name, retUrl, forgotPwdUrl, successfulUrl, notifyUrl, response);
		BIZ_LOG_INFO(opUserCode, BIZ_TYPE.RECHARGE, opName+"_"+opCode+"正在操作圈存");
		return succ("ok", "圈存操作成功");
	}
	
	/**
	 * 平台自有资金账户提现——圈提现
	 */
	@ActionKey("/creditForLoad_QT")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message creditForLoad_QT(){
		String opUserCode = getUserCode();
		OPUserV2 opUserV2 = opUserService.findById(opUserCode);
		if(opUserV2 == null){
			return error("11", "操作员信息异常", "");
		}
		String opName = opUserV2.getStr("op_name");
		String opCode = opUserV2.getStr("op_code");
		if(StringUtil.isBlank(opName) || StringUtil.isBlank(opCode)){
			return error("11", "操作员信息异常", false);
		}
		if(!"总部".equals(opUserV2.getStr("op_group"))){
			return error("03", "非权限范围内请勿操作", "");
		}
		String type = getPara("type");
		long amt = getParaToLong("amount");
		String accountId = "";
		String idType = "";
		String idNo = "";
		String mobile = "";
		String bankNo = "791913717300033";
		String name = "武汉易融恒信金融信息服务有限公司";
		if("1".equals(type)){//红包账户
			accountId = JXService.RED_ENVELOPES;
			mobile = JXService.RED_ACCOUNT_MOBILE;
			idType = "25";
			idNo = "91420103303535758R";
		}else{//手续费账户
			accountId = JXService.FEES;
			mobile = JXService.FEES_ACCOUNT_MOBILE;
			idType = "20";
			idNo = "G1042010304144850U";
		}
		//返回交易页面链接
 		String retUrl = CommonUtil.NIUX_URL+"/main";
 		String successfulUrl = "";
 		//后台响应链接
 		String notifyUrl = CommonUtil.NIUX_URL+"/showResult_QC_OR_QT";
 		//忘记密码链接
 		String forgotPwdUrl = CommonUtil.NIUX_URL;
 		JXController jxController = new JXController();
		HttpServletResponse response = getResponse();
		jxController.creditForUnloadPage(accountId, bankNo, "0", StringUtil.getMoneyYuan(amt), idType, idNo, mobile, name, forgotPwdUrl, retUrl, notifyUrl, successfulUrl, response);
		BIZ_LOG_INFO(opUserCode, BIZ_TYPE.RECHARGE, opName+"_"+opCode+"正在操作圈提");
		return succ("ok", "圈提操作成功");
	}
	
	/**
	 * 圈存、圈提回调
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/showResult_QC_OR_QT")
	@AuthNum(value = 999)
	public void showResult_QC(){
		HttpServletRequest request = getRequest();
		//响应数据
		String parameter = request.getParameter("bgData");
		
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		
		String jxTraceCode = "" + resMap.get("txDate") + resMap.get("txTime") + resMap.get("seqNo");
		//存储响应报文
		boolean bool = JXService.updateJxTraceResponse(jxTraceCode.trim(), resMap, JSON.toJSON(resMap).toString().replace(",", ",\r\n"));
		if(bool){
			renderText("success");
		}
	}
	
	/**
	 * 线下充值回调:支付宝转账，网银转账等
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/offlineRecharge_callBack")
	@AuthNum(value = 999)
	@Before({PkMsgInterceptor.class})
	public void offlineRechargeCallBack(){
		HttpServletRequest request = getRequest();
		String parameter = request.getParameter("bgData");
		Map<String, String> resMap = JSONObject.fromObject(parameter);
		//线下充值流水号记录原流水号
		String orgJxTraceCode = "" + resMap.get("orgTxDate") + resMap.get("orgTxTime") + resMap.get("orgSeqNo");//原交易流水号——用于查询单笔资金交易
		Long x = Db.queryLong("select count(1) from t_jx_trace where jxTraceCode=?",orgJxTraceCode);
		if(x<1){
			JXService.addJxTraceResponse(orgJxTraceCode, resMap, JSONObject.fromObject(parameter).toString().replace(",", ",\r\n"));
		}
		String jxAccountId = resMap.get("accountId");
		User user = userService.findByJXAccountId(jxAccountId);
		if(user == null){
			BIZ_LOG_ERROR("", BIZ_TYPE.RECHARGE, "线下充值_未查找到用户信息",null);
			return ;
		}
		String userCode = user.getStr("userCode");
		String txAmount = resMap.get("txAmount");
		String name = resMap.get("name");
		
		RechargeTrace rechargeTrace = rechargeService.findById(orgJxTraceCode);
		if(rechargeTrace != null){
			BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "线下充值流水已存在:" + orgJxTraceCode);
			return ;
		}
		
		//存入充值流水
		RechargeTrace trace = new RechargeTrace();
		trace.set("userTrueName", name);
		trace.set("userBankCode", "");
		trace.set("userBankName", "");
		trace.set("traceCode", orgJxTraceCode);
		trace.set("bankTraceCode", orgJxTraceCode);
		trace.set("userCode", userCode);
		trace.set("traceDateTime", DateUtil.getNowDateTime());
		trace.set("traceDate", DateUtil.getNowDate());
		trace.set("modifyDateTime", DateUtil.getNowDateTime());
		trace.set("modifyDate", DateUtil.getNowDate());
		trace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.OFFLINE.key());
		trace.set("traceAmount", StringUtil.getMoneyCent(txAmount));
		trace.set("userName", user.getStr("userName"));
		trace.set("userBankNo", jxAccountId);
		
		Map<String, String> singleFundQuery = JXQueryController.fundTransQuery(jxAccountId, resMap.get("orgTxDate"), resMap.get("orgTxTime"), resMap.get("orgSeqNo"));
		if(singleFundQuery == null){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "线下充值_未查找到交易：" + orgJxTraceCode,null);
			return ;
		}
		if("00000000".equals(singleFundQuery.get("retCode")) && "00".equals(singleFundQuery.get("result")) && "0".equals(singleFundQuery.get("orFlag"))){//线下充值成功
			trace.set("bankState", RechargeTrace.BANK_STATE.SUCCESS.key());
			trace.set("traceState", RechargeTrace.TRACE_STATE.SUCCESS.key());
			trace.set("bankRemark", "线下支付，充值成功");
			trace.set("traceRemark", "线下支付，充值成功");
			trace.set("okDateTime", DateUtil.getNowDateTime());
			if(trace.save()){
				//修改资金
				fundsServiceV2.recharge(userCode, StringUtil.getMoneyCent(txAmount), 0, "线下支付，充值成功", SysEnum.traceType.C.val());
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "线下充值同步修改资金:" + resMap.get("txAmount"));
			}else{
				// 记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "线下充值流水更新失败", null);
			}
		}else{
			trace.set("bankState", RechargeTrace.BANK_STATE.FAILD.key());
			trace.set("traceState", RechargeTrace.TRACE_STATE.FAILD.key());
			trace.set("bankRemark", "线下支付，充值失败");
			trace.set("traceRemark", "线下支付，充值失败");
			trace.set("okDateTime", DateUtil.getNowDateTime());
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "充值失败" + resMap.get("retCode") + "," + resMap.get("retMsg"), null);
			if(trace.save()){
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "线下充值失败_添加充值流水", null);
			}{
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "线下充值失败_添加充值流水失败", null);
			}
		}
		renderText("success");
	}
	
	/**
	 * 批量处理提现结果
	 */
	@ActionKey("/dealWithdrawResult")
	@Before({PkMsgInterceptor.class})
	public Message dealWithdrawResult(){
		String status = "1";
		//查出所有提现已审核状态的提现流水
		List<WithdrawTrace> withdrawTraces = withdrawTraceService.findByStatus(status);
		if(withdrawTraces != null && withdrawTraces.size() > 0){
			for (int i = 0; i < withdrawTraces.size(); i++) {
				String orgTxDate = "";
				String orgTxTime = "";
				String orgSeqNo = "";
				//获取单条流水
				WithdrawTrace withdrawTrace = withdrawTraces.get(i);
				String userCode = withdrawTrace.getStr("userCode");
				long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
				User user = userService.findById(userCode);
				if(user == null ){
					continue;
				}
				String bankTraceCode = withdrawTrace.getStr("bankTraceCode");
				orgTxDate = bankTraceCode.substring(0, 8);
				orgTxTime = bankTraceCode.substring(8, 14);
				orgSeqNo = bankTraceCode.substring(14, 20);
				
				//根据流水号查询提现结果
				Map<String, String> fundQuery = JXQueryController.fundTransQuery(user.getStr("jxAccountId"), orgTxDate, orgTxTime, orgSeqNo);
				
				//根据提现结果处理数据
				if(fundQuery == null){
					continue;
				}
				YiStageUserInfo yiStageUserInfo = yiStageUserInfoService.queryByUserCode(userCode);
				Map<String, String> reMap = new HashMap<String, String>();
				String notify = CommonUtil.YISTAGE_URL+"/lyfq/front/user/loanMoneyLimit/updateWithdraw";//易分期接收通知地址
				String withdrawCode = withdrawTrace.getStr("withdrawCode");
				String accountId = user.getStr("jxAccountId");
				//查询成功，且没有冲正/撤消
				if("00000000".equals(fundQuery.get("retCode")) && !"1".equals(fundQuery.get("orFlag"))){
					withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "3");
					withdrawTrace.set("withdrawRemark", "提现成功");
					if(null!=yiStageUserInfo){ //易分期用户提现冲正，给他们发送通知
						reMap.put("status", "3");
						reMap.put("accountId", accountId);
						reMap.put("txAmount", StringUtil.getMoneyYuan(withdrawAmount));//提现金额
						reMap.put("withdrawCode", withdrawCode);
						HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					}
					// 记录日志
					BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "查询提现状态_成功");
					if (withdrawTrace.update()) {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_更新流水" );
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_更新流水失败" , null);
					}
					continue;
				}
				//提现冲正——只改状态，结果由提现冲正接口处理
				if("00".equals(fundQuery.get("result")) && "1".equals(fundQuery.get("orFlag"))){
					withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "6");
					withdrawTrace.set("withdrawRemark", "提现冲正");
					// 记录日志
					//BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正待处理");
					withdrawTrace.update();
					/*if (withdrawTrace.update()) {
						BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_更新流水" );
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现冲正_流水未更新" , null);
					}*/
					continue;
				}
				//未查找到原交易——资金回退
				if("CA100884".equals(fundQuery.get("retCode"))){
					withdrawTrace.set("modifyDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "4");
					withdrawTrace.set("withdrawRemark", "提现失败");
					if(null!=yiStageUserInfo){ //易分期用户提现冲正，给他们发送通知
						reMap.put("status", "4");
						reMap.put("accountId", accountId);
						reMap.put("txAmount", StringUtil.getMoneyYuan(withdrawAmount));//提现金额
						reMap.put("withdrawCode", withdrawCode);
						HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					}
					if(withdrawTrace.update()){
						//回退用户资金
						boolean result = fundsServiceV2.withdrawRevers(userCode, withdrawAmount, "提现失败");
						if (result) {//资金修改成功
							BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_修改资金" + withdrawAmount);
						} else {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_未修改资金 ", null);
						}
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现失败_更新提现流水失败 ", null);
					}
				}
			}
		}
		return succ("ok", "提现结果批量处理完成！");
	}
	
	/**
	 * 处理充值状态为A,C、提现状态为2,4的流水
	 * @return
	 */
	@ActionKey("/dealTraceStatus")
	public Message dealTraceStatus(){
		Date date = new Date();
		String startTime = DateUtil.updateDate(date, -30, Calendar.MINUTE, "yyyyMMddHHmmss");
		String endTime = DateUtil.updateDate(date, -15, Calendar.MINUTE, "yyyyMMddHHmmss");
		String rechargeStates = "A,C";
		String withdrawStates = "2,4";
		List<RechargeTrace> rechargeTraces = rechargeTraceService.findByStatesAndTime(rechargeStates, startTime, endTime);
		List<WithdrawTrace> withdrawTraces = withdrawTraceService.findByStatusAndTime(withdrawStates, startTime, endTime);
		
		String orgTxDate = "";
		String orgTxTime = "";
		String orgSeqNo = "";
		//处理充值状态为A的流水
		if(rechargeTraces != null && rechargeTraces.size() > 0){
			for (int i = 0; i < rechargeTraces.size(); i++) {
				//获取单条流水
				RechargeTrace rechargeTrace = rechargeTraces.get(i);
				String userCode = rechargeTrace.getStr("userCode");
				User user = userService.findById(userCode);
				if(user == null){//未查找到用户信息
					continue;
				}
				String accountId = user.getStr("jxAccountId");
				if(StringUtil.isBlank(accountId)){//未开通存管账号
					continue;
				}
				long traceAmount = rechargeTrace.getLong("traceAmount");//充值金额
				String traceCode = rechargeTrace.getStr("traceCode");
				String bankTraceCode = rechargeTrace.getStr("bankTraceCode");
				if(StringUtil.isBlank(traceCode) || StringUtil.isBlank(bankTraceCode) || bankTraceCode.equals(traceCode)){
					//一般情况bankTraceCode与traceCode是不一致的
					continue;
				}
				orgTxDate = bankTraceCode.substring(0, 8);
				orgTxTime = bankTraceCode.substring(8, 14);
				orgSeqNo = bankTraceCode.substring(14, 20);
				
				//根据流水号查询充值结果
				Map<String, String> fundQuery = JXQueryController.fundTransQuery(accountId, orgTxDate, orgTxTime, orgSeqNo);
				if(fundQuery == null){
					continue;
				}
				//查询成功，交易处理结果为00且冲正标志为0
				if("00000000".equals(fundQuery.get("retCode")) && "0".equals(fundQuery.get("orFlag"))  && "00".equals(fundQuery.get("result"))){
					rechargeTrace.set("bankState", BANK_STATE.SUCCESS.key());
					rechargeTrace.set("traceState", TRACE_STATE.SUCCESS.key());
					rechargeTrace.set("traceRemark", "快捷支付,充值成功");
					rechargeTrace.set("bankRemark", "快捷支付,充值成功");
					rechargeTrace.set("okDateTime", DateUtil.getNowDateTime());
					if(rechargeTrace.update()){
						fundsServiceV2.recharge(userCode, traceAmount, 0, "快捷支付，充值成功", SysEnum.traceType.C.val());
						BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "充值同步修改资金:" + StringUtil.getMoneyYuan(traceAmount));
					}
					
				}
			}
			
		}
		
		//处理提现状态为2的流水
		if(withdrawTraces != null && withdrawTraces.size() > 0){
			for (int i = 0; i < withdrawTraces.size(); i++) {
				//获取单条流水
				WithdrawTrace withdrawTrace = withdrawTraces.get(i);
				String userCode = withdrawTrace.getStr("userCode");
				User user = userService.findById(userCode);
				if(user == null ){
					continue;
				}
				String accountId = user.getStr("jxAccountId");
				if(StringUtil.isBlank(accountId)){
					continue;
				}
				long withdrawAmount = withdrawTrace.getLong("withdrawAmount");
				String withdrawCode = withdrawTrace.getStr("withdrawCode");
				String bankTraceCode = withdrawTrace.getStr("bankTraceCode");
				if(StringUtil.isBlank(withdrawCode) || StringUtil.isBlank(bankTraceCode) || bankTraceCode.equals(withdrawCode)){
					continue;
				}
				orgTxDate = bankTraceCode.substring(0, 8);
				orgTxTime = bankTraceCode.substring(8, 14);
				orgSeqNo = bankTraceCode.substring(14, 20);
				//根据流水号查询充值结果
				Map<String, String> fundQuery = JXQueryController.fundTransQuery(accountId, orgTxDate, orgTxTime, orgSeqNo);
				if(fundQuery == null){
					continue;
				}
				YiStageUserInfo yiStageUserInfo = yiStageUserInfoService.queryByUserCode(userCode);
				Map<String, String> reMap = new HashMap<String, String>();
				String notify=CommonUtil.YISTAGE_URL+"/lyfq/front/user/loanMoneyLimit/updateWithdraw";//易分期接收通知地址
				if("00000000".equals(fundQuery.get("retCode")) && "0".equals(fundQuery.get("orFlag"))  && "00".equals(fundQuery.get("result"))){
					withdrawTrace.set("okDateTime", DateUtil.getNowDateTime());
					withdrawTrace.set("status", "3");
					withdrawTrace.set("withdrawRemark", "提现成功");
					if(null!=yiStageUserInfo){ //易分期用户提现成功，给他们发送通知
						reMap.put("status", "3");
						reMap.put("accountId", accountId);
						reMap.put("txAmount", StringUtil.getMoneyYuan(withdrawAmount));//提现金额
						reMap.put("withdrawCode", withdrawCode);
						HttpRequestUtil.sendPost(notify, JSON.toJSONString(reMap));
					}
					if(withdrawTrace.update()){
						// 如果使用会员免费提现次数，记录一次				
						if(null == yiStageUserInfo){
							String useFree = withdrawTrace.getStr("useFree");
							if (useFree.equals("y")) {
								boolean ok = withdrawFreeService.setFreeCount(userCode, 1);
								if (ok) {
									BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户提现成功_提现金额 ：" + withdrawAmount / 10.0 / 10.0);
								}
							}
							// 扣除提现抵扣积分
							String isScore = withdrawTrace.getStr("isScore");
							if ("1".equals(isScore) && user.getInt("vipLevel") < 8
									&& (useFree.equals("y") == false)) {
								fundsServiceV2.doPoints(userCode, 1, 20000, "扣除提现抵扣积分");
								BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
							}
						}
						
						//修改资金账户可用余额
						boolean withdrawals4Fuiou = fundsServiceV2.withdrawals4Fuiou(
								userCode, withdrawAmount, withdrawTrace.getInt("sxf"), "提现成功");
						if (withdrawals4Fuiou == false) {
							BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_修改资金失败 ", null);
						} else {//成功
							BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现成功_同步修改资金" + withdrawAmount);
						}
					}
				}
			}
		}
		return succ("666666", "流水处理完成…………再也不用补流水了");
	}
}




