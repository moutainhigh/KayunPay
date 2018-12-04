package com.dutiantech.controller.portal;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.anno.ResponseCached;
import com.dutiantech.controller.BaseController;
import com.dutiantech.controller.FuiouController;
import com.dutiantech.controller.JXController;
import com.dutiantech.controller.JXQueryController;
import com.dutiantech.controller.pay.LianLianPayController;
import com.dutiantech.exception.BaseBizRunTimeException;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.Banks;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.BizLog.BIZ_TYPE;
import com.dutiantech.model.Funds;
import com.dutiantech.model.FundsTrace;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.RechargeTrace;
import com.dutiantech.model.SysFunds;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.model.UserCount;
import com.dutiantech.model.UserInfo;
import com.dutiantech.model.WithdrawTrace;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.BanksService;
import com.dutiantech.service.FuiouTraceService;
import com.dutiantech.service.FundsServiceV2;
import com.dutiantech.service.FundsTraceService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanOverdueService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.service.RechargeTraceService;
import com.dutiantech.service.RecommendRewardService;
import com.dutiantech.service.TicketsService;
import com.dutiantech.service.UserInfoService;
import com.dutiantech.service.UserService;
import com.dutiantech.service.UserTermsAuthService;
import com.dutiantech.service.VIPService;
import com.dutiantech.service.WithdrawFreeService;
import com.dutiantech.service.WithdrawTraceService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.MD5Code;
import com.dutiantech.util.MapSortUtil;
import com.dutiantech.util.Number;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.fundsType;
import com.dutiantech.util.SysEnum.traceState;
import com.dutiantech.util.SysEnum.traceType;
import com.dutiantech.vo.VipV2;
import com.fuiou.data.AppTransReqData;
import com.fuiou.data.P2p500405ReqData;
import com.fuiou.service.FuiouService;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
/**
 * 资金控制层
 * @author shiqingsong
 *
 */
public class FundsController extends BaseController {

	private FundsTraceService fundsTraceService = getService(FundsTraceService.class);
	private FundsServiceV2 fundsServiceV2 = getService(FundsServiceV2.class);
	private WithdrawTraceService withdrawTraceService = getService(WithdrawTraceService.class);
	private BanksService banksService = getService(BanksService.class);
	private UserService userService = getService(UserService.class);
	private RechargeTraceService rechargeTraceService = getService(RechargeTraceService.class);
	private UserInfoService userInfoService = getService(UserInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanTransferService loanTransferService = getService(LoanTransferService.class);
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private WithdrawFreeService withdrawFreeService = getService(WithdrawFreeService.class);
	private VIPService vipService = getService(VIPService.class);
	private LoanOverdueService overdueService = getService(LoanOverdueService.class);
	private RecommendRewardService rrServicce = getService(RecommendRewardService.class);
	private TicketsService ticketsSerevice = getService(TicketsService.class);
	private UserTermsAuthService userTermsAuthService = getService(UserTermsAuthService.class);
//	private FuiouTraceService fuiouTraceService = getService(FuiouTraceService.class);
	
//	/**
//	 * 查询个人资金流水 (资金明细，显示4条记录)
//	 * @return
//	 */
//	@ActionKey("/queryFunds4p")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
//	public Message query4p() {
//		
//		//获取用户标识
//		String userCode = getUserCode();
//		
//		//获取用户资金信息
//		Map<String,Object> resultMap = queryFunds4userCode(userCode);
//		
//		//获取资金流水(4条)
//		Page<FundsTrace> fundsTrace = fundsTraceService.findByPage(1, 4, null, null, null, null, null, null, userCode);
//		resultMap.put("traces", fundsTrace);
//		
//		//返回
//		return succ("查询成功", resultMap);
//	}
	
	
	/**
	 * 查询用户资金账户信息
	 * @return
	 */
	@ActionKey("/queryFunds4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFunds4user() {
		//获取用户标识
		String userCode = getUserCode();
		
		//获取用户资金信息
		Map<String,Object> resultMap = queryFunds4userCode(userCode);
		
		//返回
		return succ("查询成功", resultMap);
	}
	
	
	/**
	 * 查询用户资金流水信息
	 * @return
	 */
	@ActionKey("/queryFundsTrace4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFundsTrace4User() {
		
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
		String beginDate = getPara("beginDate");
		String endDate = getPara("endDate");
		String traceType = getPara("traceType");
		
		//获取用户标识
		String userCode = getUserCode();
		
		//获取用户资金流水
		Map<String,Object> result = fundsTraceService.findByPage(pageNumber, pageSize, 
				beginDate, endDate, traceType, null, userCode);
		long sumTraceAmount = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType, null, userCode);
		result.put("sumTraceAmount", sumTraceAmount);
		//返回
		return succ("查询成功", result);
	}
	
	
	/**
	 * 查询用户充值记录
	 * @return
	 */
	@ActionKey("/queryRechargeTrace4User")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryRechargeTrace4User() {
		
		Integer pageNumber = getPageNumber();
		Integer pageSize = getPageSize();
		String beginDate = getPara("beginDate");
		String endDate = getPara("endDate");
		String traceState = getPara("traceState");

		//获取用户标识
		String userCode = getUserCode();
		
		//获取充值记录
		Map<String, Object> result = rechargeTraceService.queryRechargeTrace4User(
	 			userCode , pageNumber, pageSize, beginDate, endDate , traceState,null);
		long sumTraceAmount = rechargeTraceService.sumRechargeTrace4User(userCode, beginDate, endDate, "B");
		result.put("sumTraceAmount", sumTraceAmount);
		//返回
		return succ("查询成功", result);
	}
	
	/**
	 * 更新理财卡省市信息
	 * @return
	 */
	@ActionKey("/updateCardCity")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message updateCardCity() {
		String userCode = getUserCode();
		String cardCity = getPara("cardCity");
		if(StringUtil.isBlank(cardCity)){
			return error("01", "理财卡省市信息不可为空", false);
		}
		if(cardCity.split("\\|").length<2){
			return error("02", "理财卡省市信息不合法", false);
		}
		if(banksService.updateCardCity(userCode, cardCity)){
			return succ("更新理财卡省市信息完成", true);
		}
		return error("03", "操作未生效", false);
	}
	
	/**
	 * 检查cardCity是否填写
	 * @return
	 */
	@ActionKey("/validateCardCity")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message validateCardCity() {
		String userCode = getUserCode();
		
		if(!banksService.validateBanks(userCode)){
			return succ("理财卡未绑定", "0");
		}else{
			if(banksService.validateCardCity(userCode)){
				return succ("理财卡省市信息非空", "1");
			}else{
				return succ("理财卡省市信息为空", "2");
			}
		}
	}
	
	/**
	 * 查询免费提现次数
	 * @return
	 */
	@ActionKey("/queryFreeWithdrawal4user")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFreeWithdrawal4user() {
		String userCode = getUserCode();
		int x1 = withdrawFreeService.findFreeCountByUserCode(userCode);//已免费提现次数
		int x2 = vipService.findUserVipLevelByUserCode(userCode);
		Map<String,Object> result = new HashMap<String, Object>();
		if(x1<0)
			x1=0;
		result.put("useFree", x1);
		result.put("vipFree", VipV2.getVipByLevel(x2).getVipTxCount());
		int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411", DateUtil.getNowDate());
		if(x > 0){
			result.put("vipFree", 0);
		}
		return succ("查询完成", result);
	}
	
	
	/**
	 * 申请提现
	 * @return
	 */
//	@ActionKey("/withdrawals")
//	@AuthNum(value=999)
//	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
//	public Message withdrawals() {
//		
//		//获取参数并验证
//		Long amount = 0L;
//		try{
//			amount = getParaToLong("amount");
//		}catch(Exception e){
//			return error("05", "请输出正确提现金额", "");
//		}
//		String payPwd = getPara("payPwd");
//		String msgMac = getPara("msgMac");
////		String bankCode = getPara("bankCode");
//		String isScore = getPara("isScore");
//		
//		if(amount < 300){
//			return error("03", "提现金额不能小于3元", "");
//		}
//
//		//获取用户标识
//		String userCode = getUserCode();
//		
//		//限制首次提现金额
//		Page<WithdrawTrace> withdrawTrace = withdrawTraceService.findByPage(userCode, 1, 10, null, null, null, null);
//		if(null == withdrawTrace || withdrawTrace.getTotalRow() <= 0){
//			if(amount < 5000){
//				return error("03", "首次提现金额不能小于50元", "");
//			}
//		}
//		
//		//验证短信验证码
//		if(CommonUtil.validateSMS("SMS_MSG_WITHDRAW_" + userCode, msgMac) == false){
//			//记录日志
//			BIZ_LOG_WARN(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，短信验证码不正确");
//			return error("01", "短信验证码不正确", "");
//		}
//		
//		//获取用户支付密码
//		User user = User.userDao.findById(userCode);
//		if(user == null){
//			return error("02", "用户不存在", "");
//		}
//		
//		boolean result = false;
//		try {
//			//验证支付密码   容错5次
//			if(CommonUtil.validatePwd(userCode, payPwd, user.getStr("payPasswd")) == false ){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，支付密码错误");
//				return error("04", "支付密码错误", "");
//			}
//			
//			//短信验证成功 删除缓存输入支付密码错误次数
//			Memcached.delete("SMS_MSG_WITHDRAW_PWDERROR_" + userCode);
//			
//			//获取银行卡信息
//			BanksV2 bank = BanksV2.bankV2Dao.findById(userCode);
//			
//			//验证卡号是否正确
//			if(bank == null){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，卡号不存在");
//				return error("07", "卡号不存在", "");
//			}
//			
//			try {
//				if(StringUtil.isBlank(bank.getStr("cardCity")))
//					return error("99", "银行卡省市信息不全", "");
//			} catch (Exception e) {
//				return error("99", "银行卡省市信息不全", "");
//			}
//			
//			//判断账户可用资金是否足够提现
//			Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
//			long avBalance = funds.getLong("avBalance") ;
//			
//			//当未投资提现直接扣除10元体验金
////			long beRecyPrincipal = funds.getLong("beRecyPrincipal");//待收回本金
////			long reciedPrincipal = funds.getLong("reciedPrincipal");//已回收本金
////			long reciedInterest = funds.getLong("reciedInterest");//已回收利息
////			long loanTotalAmount = beRecyPrincipal+reciedPrincipal+reciedInterest;
////			if( loanTotalAmount <= 0) {
////				avBalance = avBalance - 1000 ;
////			}
//			if(null == funds ||  avBalance < amount){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户余额不足");
//				return error("08", "账户余额不足", ""); 
//			}
//			
//			String useFree = getPara("useFree","n");//如果使用免费提现，验证免费提现次数
//			if(useFree.equals("y")){
//				int useFreeCount = withdrawFreeService.findFreeCountByUserCode(userCode);//已经免费提现的次数
//				int userVipLevel = user.getInt("vipLevel");
//				VipV2 vip = VipV2.getVipByLevel(userVipLevel);
//				int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411", DateUtil.getNowDate());
//				if(x > 0){
//					return error("03", "新会员免费提现制度2016年4月11后生效", false);
//				}
//				if(vip.getVipTxCount()!=-1){
//					if(useFreeCount >= vip.getVipTxCount()){
//						return error("117", "您已免费提现"+useFreeCount+"次，您的会员级别目前最多可免费提现"+vip.getVipTxCount()+"次", false);
//					}
//				}
//			}
//			if("1".equals(isScore) && useFree.equals("y")){
//				return error("118", "积分抵扣、免费提现只能选择单独一种方式", false);
//			}
//			//判断积分是否足够
//			if("1".equals(isScore) && funds.getLong("points") < 20000){
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户积分不足");
//				return error("09", "账户积分不足", ""); 
//			}
//			
//			//记录日志
//			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "提现冻结余额日志");
//			boolean withdrawals2 = fundsServiceV2.withdrawals2(userCode, amount, 0, "冻结提现金额,提现中");
//			if(withdrawals2 == false){
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "申请提现失败 ：冻结提现金额异常 ", null);
//				return error("10", "提现异常10", ""); 
//			}
//
//			//扣除提现抵扣积分
//			if("1".equals(isScore) && user.getInt("vipLevel") < 7 &&  (useFree.equals("y")==false) ){
//				fundsServiceV2.doPoints(userCode, 1, 20000,"扣除提现抵扣积分");
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "扣除提现抵扣积分");
//			}
//			
//			UserInfo userInfo = userInfoService.findById(userCode);
//			String withdrawCode = UIDUtil.generate();
//			//新增提现申请记录
//			result = withdrawTraceService.save(withdrawCode,userCode, funds.getStr("userName"), userInfo.getStr("userCardName"), bank.getStr("bankNo"), 
//					bank.getStr("bankNo"), bank.getStr("bankType"),bank.getStr("bankName"),
//					bank.getStr("cardCity"), amount,SysEnum.withdrawTraceState.A.val(), isScore,"用户申请提现","",useFree,false);
//			
//			if(result == false){
//				//记录日志
//				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-07", null);
//				return error("07", "提现异常07", "");
//			}else{
//				//记录日志
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ：" + amount/10.0/10.0);
//				//如果使用会员免费提现次数，记录一次
//				if(useFree.equals("y")){
//					withdrawFreeService.setFreeCount(userCode, 1);
//				}
//				//1000元以下自动连连支付提现
//				if(amount<=100000){
//					try {
//						//如果ON   则使用商银信代付
//						String a = "OFF";
//						try {
//							a = (String) CACHED.get("ST.SYX_PAYIN_SWITCH");
//							if(StringUtil.isBlank(a))
//								a = "OFF";
//						} catch (Exception e) {
//							a = "OFF";
//						}
//						if(a.equals("ON")){
//							return zjtx(withdrawCode);//商银信代付，余额不足，卡类型不支持 则转连连支付
//						}else{
//							return lianlian_pay(withdrawCode);
//						}
//						
//					} catch (Exception e) {
//						return error("99", "小于1000元，自动提现无需审核，发生异常", false);
//					}
//				}
//			}
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			//记录日志
//			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
//			return error("06", "提现异常06", "");
//		}
//		
//		//返回
//		return succ("申请提现成功，请等待审核", "");
//	}
//	/**
//	 *  小于1000的，优先商银信提现，商银信不支持的银行卡 或者 商银信余额不足，转连连支付提现
//	 */
//	private Message zjtx(String withdrawCode){
//		WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
//		
//		String userCode = withdrawTrace.getStr("userCode");
//		
//		String oldStatus = withdrawTrace.getStr("status");
//		
//		String cardCity = withdrawTrace.getStr("cardCity");
//		
//		if(!userService.validateUserState(userCode))
//			return error("15", "用户状态非【正常】", false);
//		
//		if(!oldStatus.equals(SysEnum.withdrawTraceState.A.val())){
//			return error("14","该条申请状态非【未审核】",false);
//		}
//		
//		withdrawTrace.set("opUserName", "系统管理员");
//		withdrawTrace.set("opUserCode", "136a56dd77e2da633a1411a81b77f3d2");
//		int isExist = userService.isExist(userCode);
//		if(isExist == 0){
//			try {
//				if(StringUtil.isBlank(cardCity)){
//					return error("91", "银行卡开户省市信息异常", false);
//				}else{
//					String pn = cardCity.split("\\|")[0];
//					String cn = cardCity.split("\\|")[1];
//					if(StringUtil.isBlank(pn) && StringUtil.isBlank(cn)){
//						return error("91", "银行卡开户省市信息异常", false);
//					}
//				}
//			} catch (Exception e) {
//				return error("91", "银行卡开户省市信息异常", false);
//			}
//			if(withdrawTrace.update()){
//				boolean zz = withdrawTraceService.updateById(withdrawCode, SysEnum.withdrawTraceState.B.val(), oldStatus);
//				if(zz){
//					//提现
//					long province_code = 0;
//					long city_code = 0;
//					
//					String province_name = cardCity.split("\\|")[0];
//					String city_name = cardCity.split("\\|")[1];
//					if(!StringUtil.isBlank(province_name) && !StringUtil.isBlank(city_name)){
//						Integer tmp1 = Db.queryInt("select id from t_location where areaname =? or shortname=?",province_name,province_name);
//						if(tmp1!=null)
//							province_code = tmp1.intValue();
//						
//						Integer tmp2 = Db.queryInt("select id from t_location where areaname =? or shortname=?",city_name,city_name);
//						if(tmp2!=null)
//							city_code = tmp2.intValue();
//						else
//							city_code = province_code;
//						
//						if(province_code==110000){
//							city_code = 110000;
//						}
//						if(province_code==120000){
//							city_code = 120000;
//						}
//						if(province_code==310000){
//							city_code = 310000;
//						}
//						if(province_code==500000){
//							city_code = 500000;
//						}
//					}
//					String mm = "";
//					SYXPayController syxc = new SYXPayController();
//					Message aa = syxc.daifu(withdrawCode, province_name, city_name);
//					mm = aa.getReturn_info();
//					if(aa.getReturn_code().equals("00")){
//						return succ("受理成功", true);
//					}else if(aa.getReturn_code().equals("92") || aa.getReturn_code().equals("95")){
//						System.out.println("商银信代付失败，转连连支付："+mm);
//						Db.update("update t_withdraw_trace set `status` = '1' where withdrawCode = ?",withdrawCode);
////						withdrawTraceService.updateById(withdrawCode, SysEnum.withdrawTraceState.B.val(), "0");
//						LianLianPayController lianPay = new LianLianPayController();
//						Message msg = lianPay.repay4lianlian(withdrawCode,province_code+"",city_code+"");
//						mm = msg.getReturn_info();
//						if(msg.getReturn_code().equals("00"))
//							return succ("受理成功", true);
//					} 
//					return error("11", "错误:"+mm, null);
//				}
//			}
//		}else if(isExist > 0){
//			return error("13","用户不存在",false);
//		}
//		return error("17","申请提现失败", false);
//	}
	
	/**
	 * 连连支付提现
	 */
	@SuppressWarnings("unused")
	private Message lianlian_pay(String withdrawCode){
		WithdrawTrace withdrawTrace = withdrawTraceService.findById(withdrawCode);
		
		String userCode = withdrawTrace.getStr("userCode");
		
		String oldStatus = withdrawTrace.getStr("status");
		
		String cardCity = withdrawTrace.getStr("cardCity");
		
		if(!userService.validateUserState(userCode))
			return error("15", "用户状态非【正常】", false);
		
		if(!oldStatus.equals(SysEnum.withdrawTraceState.A.val())){
			return error("14","该条申请状态非【未审核】",false);
		}
		
		withdrawTrace.set("opUserName", "系统管理员");
		withdrawTrace.set("opUserCode", "136a56dd77e2da633a1411a81b77f3d2");
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
					System.out.println("小于1000元，自动进行连连支付提现："+msg.getReturn_info());
					if(msg.getReturn_code().equals("00")){
						return succ("提现成功", true);
					}
				}
			}
		}else if(isExist > 0){
			return error("13","用户不存在",false);
		}
		return succ("申请提现成功，请等待审核", "");
	}
	
	/**
	 * 取消提现
	 * @return
	 */
	@ActionKey("/cancelWithdrawals")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message cancelWithdrawals() {
		//获取参数并验证
		String withdrawCode = getPara("withdrawCode");
		if(StringUtil.isBlank(withdrawCode)){
			return error("01", "参数错误！", ""); 
		}
		
		long amount = 0L;
		
		//获取用户标识
		String userCode = getUserCode();
		try {
			WithdrawTrace withdrawInfo = withdrawTraceService.findById(withdrawCode);
			if(null == withdrawInfo || (withdrawInfo.getStr("userCode").equals(userCode) == false)){
				return error("04", "提现编号错误或不存在！", ""); 
			}
			amount = withdrawInfo.getLong("withdrawAmount");
			String isScore = withdrawInfo.getStr("isScore");
			
			//记录日志
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "取消提现,返还金额");
			fundsServiceV2.withdrawals4funds(userCode,amount);
			//扣除提现抵扣积分
			if("1".equals(isScore)){
				fundsServiceV2.doPoints(userCode, 0, 20000,"返还提现抵扣积分");
				BIZ_LOG_INFO(userCode, BIZ_TYPE.POINT, "返还提现抵扣积分");
			}
			if("y".equals(withdrawInfo.getStr("useFree"))){
				withdrawFreeService.setFreeCount(userCode, -1);
			}
			//修改提现状态
			boolean result = withdrawTraceService.updateById(withdrawCode, "6" , "0");
			if(result == false){
				//记录日志
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "取消提现失败，提现异常03", null);
				return error("03", "提现异常03", "");
			}
		} catch (Exception e) {
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "取消提现失败04", e);
			return error("04", "提现异常04", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户取消提现成功，取消提现金额 ：" + amount/100);
		
		//返回
		return succ("提现已取消", "");
	}

	
	/**
	 * 查询用户提现记录
	 * @return
	 */
	@ActionKey("/withdrawalsHistory")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryWithdrawals4User() {
		
		int pageNumber = getPageNumber();
		int pageSize = getPageSize();
		String beginDate = getPara("beginDate");
		String endDate = getPara("endDate");
		String status = getPara("status");
		
		//获取用户标识
		String userCode = getUserCode();
		//获取用户提现记录
		Page<WithdrawTrace> withdrawTrace = withdrawTraceService.findByPage(
				userCode,pageNumber, pageSize , beginDate, endDate,status,null);

		//返回
		return succ("查询成功", withdrawTrace);
	}
	
	/**
	 * 查询用户余额
	 * @return
	 */
	@ActionKey("/queryAvbalance")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryAvbalance(){
		String userCode = getUserCode();
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		long avBalance = funds.getLong("avBalance");
		return succ("查询完成", avBalance);
	}
	
	
	/**
	 * 查询用户绑定银行卡
	 * @return
	 */
	@ActionKey("/queryBanks")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryBanks(){
		String userCode = getUserCode();
		Map<String, Object> result = new HashMap<String, Object>();
		List<BanksV2> banks = banksService.findBanks4User(userCode);
		UserInfo userInfo = UserInfo.userInfoDao.findById(userCode);
		if(null == userInfo){
			return error("01", "查询异常", "");
		}
		if(banks != null && banks.size() > 0){
			String mobile = banks.get(0).getStr("mobile");
			try {
				mobile = CommonUtil.decryptUserMobile(mobile);
			} catch (Exception e) {
				return error("02", "解析信息异常", "");
			}
			banks.get(0).set("mobile", mobile);
		}
		result.put("banks", banks);
		result.put("isAuthed", userInfo.getStr("isAuthed"));
		result.put("userCardId", IdCardUtils.subCardId(userInfo.getStr("userCardId")));
		result.put("userCardName", userInfo.getStr("userCardName"));
		return succ("查询完成", result);
	}

	/**
	 * 绑定银行卡
	 * @param bankCode
	 * @return
	 */
	@ActionKey("/addBank4user")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message addBank4user(){
		Banks bank = getModel(Banks.class, "bank");
		String msgMac = getPara("msgMac");
		
		//验证
		if(StringUtil.isBlank(msgMac)){
			return error("01", "参数错误", "");
		}
		
		//获取用户标识
		String userCode = getUserCode();
		
		//限制只能绑定一张银行卡
		List<BanksV2> listBank = banksService.findBanks4User(userCode);
		if(null != listBank && listBank.size() > 0){
			return error("04", "只能绑定一张银行卡哦！", "");
		}
		
		//验证短信验证码
		if(CommonUtil.validateSMS("SMS_MSG_BINDCARD_" + userCode, msgMac) == false){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 ,短信验证码不正确", null);
			return error("05", "短信验证码不正确", "");
		}
		
		//添加
		boolean result = false;
		try {
			bank.set("userCode", userCode);
			bank.set("bankCode", MD5Code.crypt(userCode+bank.getStr("bankNo")));
			bank.set("status", "N");
			bank.set("createDateTime", DateUtil.getNowDateTime());
			bank.set("modifyDateTime", DateUtil.getNowDateTime());
			bank.set("bankMac", "0000");
			result = bank.save();
		} catch (UnsupportedEncodingException e) {
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败,加密标识错误", e);
			return Message.error("02", "添加银行卡异常! ", e.getMessage());
		}
		
		if(result == false){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "绑定银行卡失败 添加失败", null);
			return error("03", "添加失败!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, "绑定银行卡成功，卡号为:" + bank.getStr("bankNo"));

		return succ("添加成功","");
	}
	
	/**
	 * 删除绑定银行卡
	 * @param bankCode
	 * @return
	 */
	@ActionKey("/delBank4user")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message delBank4user(){
		String bankCode = getPara("bankCode");
		//验证
		if(StringUtil.isBlank(bankCode)){
			return error("01", "参数错误", null);
		}
		
		//获取用户标识
		String userCode = getUserCode();
		
		//获取银行卡信息并验证
		Banks bank = Banks.bankDao.findById(bankCode);
		if(bank == null || userCode.equals(bank.getStr("userCode")) == false){
			return error("02", "卡号不存在", null);
		}
		
		//保存
		bank.set("status", "S");
		boolean result = bank.update();
		if(result == false){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "删除银行卡失败", null);
			return error("03", "删除失败!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, "删除银行卡成功，卡号为:" + bank.getStr("bankNo"));
		
		return succ("删除成功", "");
	}
	
	
	/**
	 * 设置银行卡为默认
	 * @param bankCode
	 * @return
	 */
	@ActionKey("/setBankDefault")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class})
	public Message setBankDefault(){
		String bankCode = getPara("bankCode");
		
		//获取用户标识
		String userCode = getUserCode();
		
		//取消默认
		int updateResult = Db.update("update t_banks set isDefault = 0 where userCode = ? and isDefault = 1", userCode);
		if(updateResult <= 0){
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "取消默认银行卡失败", null);
		}
		
		//设置指定卡为默认
		int result = Db.update("update t_banks set isDefault = 1,modifyDateTime = ? where bankCode = ?", DateUtil.getNowDateTime(),bankCode);
		if(result <= 0){
			//记录日志
			BIZ_LOG_ERROR(userCode, BIZ_TYPE.BANK, "设置默认银行卡失败", null);
			return error("02", "设置失败!", "");
		}
		
		//记录日志
		BIZ_LOG_INFO(userCode, BIZ_TYPE.BANK, "修改默认银行卡成功");
		
		return succ("设置成功", result);
	}
	
	
	
	/**
	 * 查询个人资金统计信息
	 * @return
	 */
	@ActionKey("/queryFundsCount4user")
	@AuthNum(value=999)
	@ResponseCached(cachedKey="queryFundsCount4user", cachedKeyParm="@userCode",mode="remote" , time=2*60)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFundsCount4user() {
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		if(null == funds){
			return error("01", "获取失败", "");
		}
		Map<String,Object> fundsMap = new HashMap<String, Object>();
		fundsMap.put("reciedInterest", funds.getLong("reciedInterest"));
		fundsMap.put("beRecyInterest", funds.getLong("beRecyInterest"));
		fundsMap.put("reciedPrincipal", funds.getLong("reciedPrincipal"));
		fundsMap.put("beRecyPrincipal", funds.getLong("beRecyPrincipal"));
		fundsMap.put("outFundsCount", funds.getLong("reciedPrincipal") + funds.getLong("beRecyPrincipal"));
		fundsMap.put("haveRecovery", funds.getLong("reciedPrincipal") + funds.getLong("reciedInterest"));
		fundsMap.put("forRecovery", funds.getLong("beRecyPrincipal") + funds.getLong("beRecyInterest"));
		fundsMap.put("beRecyCount", funds.getInt("beRecyCount"));
		fundsMap.put("recyMFee4loan", funds.getInt("recyMFee4loan"));//已收利息管理费
		fundsMap.put("beRecyMFee4loan", funds.getInt("beRecyMFee4loan"));//待还管理费
		//获取可用余额
		fundsMap.put("avBalance", funds.getLong("avBalance"));
		fundsMap.put("vipLevel", user.getInt("vipLevel"));
		fundsMap.put("vipLevelName", user.getStr("vipLevelName"));
		fundsMap.put("userScore", user.getLong("userScore"));
		//增加30天待收统计，统计下一个回款首期的数据
		Object[] nextSUM = Db.queryFirst("select SUM(nextAmount),SUM(nextInterest) from t_loan_trace where traceState=? and payUserCode=?;", 
				traceState.N.val() , userCode );
		if( nextSUM[0] != null ){
			fundsMap.put("nextAmount", nextSUM[0] ) ;
		}
		if( nextSUM[1] != null ){
			fundsMap.put("nextInterest", nextSUM[1] ) ;
		}
		
		long yzyj = rrServicce.queryTZJL(userCode, 3);
		long yyxjq = ticketsSerevice.queryUseAmount1(userCode);
		long dsyj = rrServicce.querySYFY(userCode);
		fundsMap.put("yzyj", yzyj);
		fundsMap.put("yyxjq", yyxjq);
		fundsMap.put("dsyj", dsyj);
		return succ("获取成功", fundsMap);
	}
	
	
	/**
	 * 查询个人贷款统计
	 * @return
	 */
	@ActionKey("/queryFundsCount4Loan")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryFundsCount4Loan(){
		String userCode = getUserCode();
		
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		if(null == funds){
			return error("01", "获取失败", "");
		}
		
		Map<String,Object> fundsMap = new HashMap<String, Object>();
		
		Long dhbx = funds.getLong("beRecyPrincipal4loan")+funds.getLong("beRecyInterest4loan");
		Long yhbx = funds.getLong("loanTotal") - dhbx;
		Integer cgjkbs = funds.getInt("loanSuccessCount") + funds.getInt("loanCount") + funds.getInt("loanBySysCount");
			
		fundsMap.put("loanTotal", funds.getLong("loanTotal"));
		fundsMap.put("yhbx", yhbx);
		fundsMap.put("dhbx", dhbx);
		fundsMap.put("beRecyMFee4loan", funds.getInt("beRecyMFee4loan"));
		fundsMap.put("cgjkbs", cgjkbs);
		fundsMap.put("loanSuccessCount", funds.getInt("loanSuccessCount"));
		fundsMap.put("loanCount", funds.getInt("loanCount"));
		fundsMap.put("loanBySysCount", funds.getInt("loanBySysCount"));
		
		return succ("查询成功", fundsMap);
	}
	
	
	/**
	 * 查询实时财务
	 * @return``
	 */
	@ActionKey("/queryRealtimeFinancial")
	@ResponseCached(cachedKey="realtimeFinancial", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message queryRealtimeFinancial(){
		SysFunds sysFunds = SysFunds.sysFundsDao.findFirst("select * from t_sys_funds");
		String sql = "select COALESCE(sum(reciedInterest),0),COALESCE(sum(beRecyPrincipal),0),COALESCE(sum(beRecyPrincipal+reciedPrincipal),0) from t_funds";
		String sql2 = "select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in('N','O','P','Q')";
		Object[] result = Db.queryFirst(sql);
		long loanAmount = Db.queryBigDecimal(sql2).longValue();
		sysFunds.set("payTotal", Long.parseLong(result[0].toString()));//累积赚取利益
		sysFunds.set("reciedTotal", Long.parseLong(result[1].toString()));//待收金额
		sysFunds.set("payAmountTotal", loanAmount);//交易总额
		return succ("查询成功", sysFunds);
	}
	
	/**
	 * 实施财务 基础统计数据
	 * @return``
	 */
	@ActionKey("/duangBasicData")
	@ResponseCached(cachedKey="duangBasicData", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangBasicData(){
		SysFunds sysFunds = SysFunds.sysFundsDao.findFirst("select * from t_sys_funds");
		String sql = "select COALESCE(sum(reciedInterest),0),COALESCE(sum(beRecyPrincipal),0) from t_funds";
		String sql2 = "select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in('N','O','P','Q')";
		String sql3 = "SELECT COALESCE(SUM(transAmount),0) from t_loan_transfer where transState = 'B' and gotDate>=20180613";
		Object[] result = Db.queryFirst(sql);
		long loanAmount = Db.queryBigDecimal(sql2).longValue();
		long loanTransAmount = Db.queryBigDecimal(sql3).longValue();
		sysFunds.set("payTotal", Long.parseLong(result[0].toString()));//累积赚取利益
		sysFunds.set("reciedTotal", Long.parseLong(result[1].toString()));//待收金额
		sysFunds.set("payAmountTotal", loanAmount+loanTransAmount);//交易总额
		long tzr = Db.queryLong("select count(userCode) from t_funds where userCode not in (select userCode from t_loan_info)");
		long jkr = Db.queryLong("select count(userCode) from t_funds where userCode in (select userCode from t_loan_info)");
		sysFunds.put("tzr", tzr);
		sysFunds.put("jkr",jkr);
		
		//坏账
		Long badAmount = overdueService.totalBadAmount();
		//逾期
		Long overdueAmount = overdueService.totalOverdueAmount();
		
		sysFunds.set("badDebtTotal", new BigDecimal((float)badAmount/loanAmount*100).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		sysFunds.set("overdueTotal", new BigDecimal((float)overdueAmount/loanAmount*100).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		
		return succ("查询成功", sysFunds);
	}
	
	/**
	 * 实施财务 按月/按日统计累计交易金额
	 * @return``
	 */
	@ActionKey("/duangDayCount")
	@ResponseCached(cachedKey="duangDayCount", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangDayOrMonthCount(){
		
		int dayLength = getParaToInt("dayLength",15);
		
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		
		List<String> days = CommonUtil.getAnyDateFromDayLength(dayLength, 0, 0, 0,"yyyyMMdd",0);
		for (int i = 0; i < days.size(); i++) {
			String tmp1 = days.get(i);
//			long sum_loanAmount = loanInfoService.countLoanAmountByEffectDate(tmp1, tmp1);
			long sum_loanNum = loanInfoService.countLoanCountByPayDate(tmp1, tmp1);//投资笔数
			long sum_transNum = loanTransferService.queryTransferByDate(tmp1, "B");//转让笔数
//			long sum_transferAmount = loanTransferService.sumGotAmount4Date(tmp1, tmp1);
			Map<String,Object> tmp2 = new HashMap<String, Object>();
			tmp2.put("date", tmp1);
			tmp2.put("data", sum_loanNum+sum_transNum);
			result.add(tmp2);
		}
		return succ("查询成功", result);
	}
	
	/**
	 * 实施财务 按月/按日统计累计交易金额
	 * @return``
	 */
	@ActionKey("/duangMonthCount")
	@ResponseCached(cachedKey="duangMonthCount", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangMonthCount(){
		
		int monthLength = getParaToInt("monthLength",12);
		
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		
		List<Map<String,String>> months = CommonUtil.getAnyDateFromMonthLength(monthLength, 0, 0, "yyyyMMdd", 0);
		for (int i = 0; i < months.size(); i++) {
			String start = months.get(i).get("start");
			String end = months.get(i).get("end");
//			long sum_loanAmount = loanInfoService.countLoanAmountByEffectDate(start, end);
//			long sum_transferAmount = loanTransferService.sumGotAmount4Date(start, end);
			
			long sum_loanNum = loanInfoService.countLoanCountByPayDate(start, end);//投资笔数
			long sum_transNum = loanTransferService.queryTransferByDate2(start, end ,"B");//转让笔数
			Map<String,Object> tmp = new HashMap<String, Object>();
			tmp.put("date", start.substring(0,6));
			tmp.put("data", sum_loanNum+sum_transNum);
			result.add(tmp);
		}
		return succ("查询成功", result);
	}
	
	/**
	 * 今日报表查询
	 * @return``
	 */
	@ActionKey("/duangTodayReport")
	@ResponseCached(cachedKey="duangTodayReport", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangTodayReport(){
		long today_loanAmount = loanInfoService.countLoanAmountByEffectDate(DateUtil.getNowDate(), DateUtil.getNowDate());
		long today_zrzqbs = loanTransferService.queryTransferByDate(DateUtil.getNowDate(), null);
		long today_okzqbs = loanTransferService.queryTransferByDate(DateUtil.getNowDate(), "B");
		long today_xzlcr = fundsServiceV2.countFundsByDate(DateUtil.getNowDate(), 0);
		long today_xzjkr = fundsServiceV2.countFundsByDate(DateUtil.getNowDate(), 1);
		long today_xujie = fundsServiceV2.countFundsByXuJie(DateUtil.getNowDate());
		long today_tzrc = loanTraceService.countByDate(DateUtil.getNowDate()+"000000",DateUtil.getNowDate()+"235959");
		long[] today_loanTotal = loanInfoService.countPubByDate(DateUtil.getNowDate());
		long today_loanTotal1 = today_loanTotal[0];
		long today_loanTotal2 = today_loanTotal[1];
		long today_loanTotal3 = today_loanTotal[2];
		long today_loanTotal4 = today_loanTotal[3];
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("today_payAmount", today_loanAmount);
		result.put("today_zrzqbs", today_zrzqbs);
		result.put("today_okzqbs", today_okzqbs);
		result.put("today_xzlcr", today_xzlcr);
		result.put("today_xzjkr", today_xzjkr);
		result.put("today_xujie", today_xujie);
		result.put("today_tzrc", today_tzrc);
		result.put("today_loanTotal1", today_loanTotal1);
		result.put("today_loanTotal2", today_loanTotal2);
		result.put("today_loanTotal3", today_loanTotal3);
		result.put("today_loanTotal4", today_loanTotal4);
		return succ("查询成功", result);
	}
	
	/**
	 * 任意日报表查询
	 * @return``
	 */
	@ActionKey("/duangAnyDayReport")
	@ResponseCached(cachedKey="duangAnyDayReport", cachedKeyParm="anyDate",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangAnyDayReport(){
		String anyDate = getPara("anyDate");
		long anyDay_loanAmount = loanInfoService.countLoanAmountByEffectDate(anyDate, anyDate);
		long anyDay_zrzqbs = loanTransferService.queryTransferByDate(anyDate, null);
		long anyDay_okzqbs = loanTransferService.queryTransferByDate(anyDate, "B");
		long anyDay_okzqAmount = loanTransferService.sumGotAmount4Date(anyDate,anyDate);
		long anyDay_xzlcr = fundsServiceV2.countFundsByDate(anyDate, 0);
		long anyDay_xzjkr = fundsServiceV2.countFundsByDate(anyDate, 1);
		long anyDay_xujie = fundsServiceV2.countFundsByXuJie(anyDate);
		long anyDay_tzrc = loanTraceService.countByDate(anyDate+"000000",anyDate+"235959");
		long[] anyDay_loanTotal = loanInfoService.countPubByDate(anyDate);
		long anyDay_loanTotal1 = anyDay_loanTotal[0];
		long anyDay_loanTotal2 = anyDay_loanTotal[1];
		long anyDay_loanTotal3 = anyDay_loanTotal[2];
		long anyDay_loanTotal4 = anyDay_loanTotal[3];
		long anyDay_loanTotal5 = anyDay_loanTotal[4];
		Map<String,Object> result = new HashMap<String, Object>();
		String yesterdayGotDate = DateUtil.delDay(anyDate, 1);
		long[] num = loanTransferService.numTransferFive(yesterdayGotDate);

		result.put("anyDay_payAmount", anyDay_loanAmount+anyDay_okzqAmount);
		result.put("anyDay_zrzqbs", anyDay_zrzqbs);
		result.put("anyDay_okzqbs", anyDay_okzqbs);
		result.put("anyDay_okzqAmount", anyDay_okzqAmount);
		result.put("anyDay_xzlcr", anyDay_xzlcr);
		result.put("anyDay_xzjkr", anyDay_xzjkr);
		result.put("anyDay_xujie", anyDay_xujie);
		result.put("anyDay_tzrc", anyDay_tzrc);
		result.put("anyDay_loanTotal1", anyDay_loanTotal1);
		result.put("anyDay_loanTotal2", anyDay_loanTotal2);
		result.put("anyDay_loanTotal3", anyDay_loanTotal3);
		result.put("anyDay_loanTotal4", anyDay_loanTotal4);
		result.put("anyDay_loanTotal5", anyDay_loanTotal5);
		result.put("yesterdayStart",num[1]);
		result.put("yesterdayEnd",num[0]);
		return succ("查询成功", result);
	}
	
	/**
	 * 圆形报表数据查询
	 * @return``
	 */
	@ActionKey("/duangCircularReport")
	@ResponseCached(cachedKey="duangCircularReport", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangCircularReport(){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		List<String> x = CommonUtil.getAnyDateFromDayLength(30, 0, 0, 0, "yyyyMMdd", 0);
		String startDate = x.get(x.size()-1);
		String endDate = x.get(0);
		long _13 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate,1,3);
		long _46 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate, 4, 6);
		long _79 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate, 7, 9);
		long _1012 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate, 10, 12);
		long _1318 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate, 13, 18);
		long _2424 = loanInfoService.countLoanTimeLimitByDate(startDate, endDate, 24, 24);
		long _all = _13+_46+_79+_1012+_1318 + _2424;
		Map<String,Object> c1 = new HashMap<String, Object>();
		c1.put("_13", _13);c1.put("_46", _46);c1.put("_79", _79);
		c1.put("_1012", _1012);c1.put("_1318", _1318);c1.put("_all", _all);c1.put("_2424", _2424);
		result.add(c1);
		long _debx = loanInfoService.countRefundTypeByDate(startDate, endDate, SysEnum.refundType.A.val());
		long _xxhb = loanInfoService.countRefundTypeByDate(startDate, endDate, SysEnum.refundType.B.val());
		Map<String,Object> c2 = new HashMap<String, Object>();
		c2.put("_debx", _debx);c2.put("_xxhb", _xxhb);
		result.add(c2);
		long _zyb = loanInfoService.sumProductType(SysEnum.productType.A.val());
		long _cwy = loanInfoService.sumProductType(SysEnum.productType.B.val());
		long _fwz = loanInfoService.sumProductType(SysEnum.productType.C.val());
		long _wdt = loanInfoService.sumProductType(SysEnum.productType.G.val());
		long tmp_all = _zyb+_cwy+_fwz + _wdt;
		Map<String,Object> c3 = new HashMap<String, Object>();
		c3.put("_zyb", _zyb);c3.put("_cwy", _cwy);c3.put("_fwz", _fwz);c3.put("_wdt", _wdt);c3.put("_all", tmp_all);
		result.add(c3);
		long _wjb = userService.countRank(1, -1);
		long _s1xjb = userService.countRank(2, -1);
		long _zxjb = userService.countRank(3, -1);
		long _s2xjb = userService.countRank(4, -1);
		long _jjjb = userService.countRank(5, -1);
		long _bjjb = userService.countRank(6, -1);
		long _zsjb = userService.countRank(7, -1);
		long _hzjb = userService.countRank(8, -1);
		long _zzjb = userService.countRank(9, -1);
		
		Map<String,Object> c4 = new HashMap<String, Object>();
		c4.put("_wjb", _wjb);c4.put("_s1xjb", _s1xjb);c4.put("_zxjb", _zxjb);c4.put("_s2xjb", _s2xjb);c4.put("_jjjb", _jjjb);
		c4.put("_bjjb",_bjjb);c4.put("_zsjb",_zsjb);c4.put("_hzjb",_hzjb);c4.put("_zzjb",_zzjb);
		result.add(c4);
		return succ("查询成功", result);
		
	}
	
	/**
	 * 用户统计(区域人数分布、区域投资金额、性别、年龄)
	 * @return``
	 */
	@ActionKey("/duangUserCount")
	@ResponseCached(cachedKey="duangUserCount", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangUserCount(){
		Map<String,Object> result = new HashMap<String, Object>();
		UserCount uc = UserCount.userCountDao.findById(DateUtil.getNowDate());
		String[] f_names = uc._getAttrNames();
		Map<String,Long> sexData = new HashMap<String, Long>();
		Map<String,Long> ageData = new HashMap<String, Long>();
		Map<String,Long> areaData = new HashMap<String, Long>();
		Map<String,Long> amtData = new HashMap<String, Long>();
		for (int i = 0; i < f_names.length; i++) {
			if(f_names[i].startsWith("sex"))
				sexData.put(f_names[i],(long)uc.getInt(f_names[i]));
			else if(f_names[i].startsWith("age"))
				ageData.put(f_names[i],(long)uc.getInt(f_names[i]));
			else if(f_names[i].startsWith("area"))
				areaData.put(f_names[i],(long)uc.getInt(f_names[i]));
			else if(f_names[i].startsWith("amt"))
				amtData.put(f_names[i],uc.getLong(f_names[i]));
		}
		result.put("countDate", uc.getStr("countDate"));
		result.put("countTime", uc.getStr("countTime"));
		result.put("sexData", MapSortUtil.sortMapByLongVal(sexData, 1));
		result.put("ageData", MapSortUtil.sortMapByLongVal(ageData, 1));
		result.put("areaData", MapSortUtil.sortMapByLongVal(areaData, 1));
		result.put("amtData", MapSortUtil.sortMapByLongVal(amtData, 1));
		return succ("查询成功", result);
	}
	
	/**
	 * 投标总金额排行
	 * @return``
	 */
	@SuppressWarnings("unchecked")
	@ActionKey("/duangToubiaoOrder1")
	@ResponseCached(cachedKey="duangToubiaoOrder1", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangToubiaoOrder1(){
		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
		Map<String,Object> pageObj = fundsServiceV2.queryFundsOrderByMoney(1, 10);
		List<LoanTrace> list = (List<LoanTrace>) pageObj.get("list");
		for (int i = 0; i < list.size(); i++) {
			String userCode = list.get(i).getStr("userCode");
			String userName = list.get(i).getStr("userName");
			Map<String,Object> tmp = loanTraceService.countTouBiaoAll(userCode);
			tmp.put("userName", userName);
			result.add(tmp);
		}
		return succ("查询成功", result);
	}
	
	/**
	 * 查询当前登录用户的投资排名
	 * @return``
	 */
	@ActionKey("/duangMyRank")
	@Before( {AuthInterceptor.class,PkMsgInterceptor.class} )
	public Message duangMyRank(){
		String userCode = getUserCode();
		long myrank = fundsServiceV2.countMyRank(userCode);
		return succ("查询成功", myrank);
	}
	
	/**
	 * 按月/日查询投资金额排行
	 * @return``
	 */
	@ActionKey("/duangToubiaoOrder2")
	@ResponseCached(cachedKey="duangToubiaoOrder2", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangToubiaoOrder2(){
		List<Map<String,String>> month_date = CommonUtil.getAnyDateFromMonthLength(0, 0, 0, "yyyyMMdd", 0);
		String x1 = month_date.get(0).get("start");
		String x2 = month_date.get(0).get("end");
		Map<String,Object> result = new HashMap<String,Object>();
//		List<LoanTrace> month = loanTraceService.countToubiaoFromMonth(x1+"000000", x2+"235959", 1, 10);
		List<FundsTrace> month = fundsTraceService.countToubiao(x1+"000000", x2+"235959", 1, 10);
		result.put("month", month);
//		List<String> day_date = CommonUtil.getAnyDateFromDayLength(0, 0, 0, 0, "yyyyMMdd", 0);
		String x3 = DateUtil.getNowDate();
		List<FundsTrace> day = fundsTraceService.countToubiao(x3+"000000", x3+"235959", 1, 10);
//		List<LoanTrace> day = loanTraceService.countToubiaoFromMonth(x3+"000000", x3+"235959", 1, 10);
		result.put("day", day);
//		return succ("查询成功", result);
		return error("01", "查询失败", "");
	}
	
//	/**
//	 * 充值 
//	 * @return
//	 */
//	@ActionKey("/recharge")
//	@AuthNum(value=999)
//	@Before({FundsRechargeValidator.class,AuthInterceptor.class})
//	public Message recharge(){
//		try {
//			Integer payAmount = getParaToInt("amount");
//			
//			String userCode = getUserCode();
//			
//			if(!userService.validateUserState(userCode))
//				return error("04", "用户状态非【正常】", false);
//			
//			if(fundsServiceV2.recharge(userCode, payAmount,0,"前台用户充值(测试)")!=null){
//				return succ("充值操作完成", true);
//			}
//		} catch (Exception e) {
//			return error("02", "充值操作未生效", false);
//		}
//		return error("01", "充值操作未生效", false);
//	}
	
	
	/**
	 * 查询用户积分
	 */
	@ActionKey("/querySumScore")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message querySumScore(){
		String userCode = getUserCode();
		Funds funds = fundsServiceV2.findById(userCode);
		User user = userService.findById(userCode);
		Map<String,Object> map = new HashMap<String, Object>();
		Long points = 0L;
		Long score = 0L;
		if(null != funds){
			points = funds.getLong("points");
		}
		if(null != user){
			score =  user.getLong("userScore");
		}
		map.put("userName", user.getStr("userName"));
		map.put("points", points);
		map.put("scores", score);
		return succ("查询成功", map);
	}
	/**
	 * 登录后查询用户积分  add by WCF   20170522
	 */
	@ActionKey("/querySumScoreByCookie")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message querySumScorebycook(){
		String userCode = getUserCode();
		Funds funds = Funds.fundsDao.find("select points from t_funds where userCode=?",userCode).get(0);
		User user = User.userDao.find("select userscore from t_user where userCode=?", userCode).get(0);
		Map<String,Object> map = new HashMap<String, Object>();
		Long points = 0L;
		Long score = 0L;
		if(null != funds){
			points = funds.getLong("points");
		}
		if(null != user){
			score =  user.getLong("userscore");
		}
		map.put("userName", user.getStr("userName"));
		map.put("points", points);
		map.put("scores", score);
		return succ("查询成功", map);
	}
	/**
	 * 查询单条充值记录
	 */
	@ActionKey("/queryRecharge")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryRecharge(){
		String traceCode = getPara("traceCode","");
		if(StringUtil.isBlank(traceCode)){
			return error("01", "流水标识不可为空", null);
		}
		RechargeTrace aa = rechargeTraceService.findFieldById(traceCode, "traceState");
		String traceState = aa == null ? "P" : aa.getStr("traceState");
		return succ("查询完成", traceState);
	}
	
	/**
	 * 微信实时数据统计
	 * @return``
	 */
	@ActionKey("/duangCountData4WeiXin")
	@ResponseCached(cachedKey="duangCountData4WeiXin", cachedKeyParm="",mode="remote" , time=5*60)
	@Before( PkMsgInterceptor.class )
	public Message duangCountData4WeiXin(){
		Map<String,Object> result = new HashMap<String, Object>();
		String sql_loanAmount = "select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in('N','O','P','Q')";
		long loanAmount = Db.queryBigDecimal(sql_loanAmount).longValue();
		result.put("CJZE", loanAmount);//累积成交总额
		
		String sql_ys_ds = "select COALESCE(sum(reciedInterest),0),COALESCE(sum(beRecyPrincipal+beRecyInterest),0) from t_funds";
		Object[] ys_ds = Db.queryFirst(sql_ys_ds);
		result.put("YSLX", Long.parseLong(ys_ds[0].toString()));//已收利息(已赚取)
		result.put("DSBX", Long.parseLong(ys_ds[1].toString()));//待收本息
		
		
		long amount_day = loanInfoService.countLoanAmountByEffectDate(DateUtil.getNowDate(), DateUtil.getNowDate());
		result.put("RCJE", amount_day);//日成交额
		
		String [] monthDate  = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, "yyyyMMdd");
		String firstDate = monthDate[0];
		String lastDate = monthDate[1];
		long amount_month = loanInfoService.countLoanAmountByEffectDate(firstDate, lastDate);
		result.put("YCJE", amount_month);//月成交额
		//坏账
		Long badAmount = overdueService.totalBadAmount();
		//逾期
		Long overdueAmount = overdueService.totalOverdueAmount();
		
		//坏账率
		result.put("HZL", new BigDecimal((float)badAmount/loanAmount*100).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		//逾期率
		result.put("YQL", new BigDecimal((float)overdueAmount/loanAmount*100).setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
		
		return succ("查询成功", result);
	}
	
	
	/**
	 * 6/8至7/7活动月/日查询投资金额排行
	 * @return``
	 */
	@ActionKey("/act0608")
	@ResponseCached(cachedKey="act0608", cachedKeyParm="",mode="remote" , time=2*60)
	@Before( PkMsgInterceptor.class )
	public Message act0608(){
		String x1 = "20170608";
		String x2 = "20170707";
		Map<String,Object> result = new HashMap<String,Object>();
		List<FundsTrace> month = fundsTraceService.countToubiao(x1+"000000", x2+"235959", 1, 20);
		result.put("month", month);
		String x3 = DateUtil.getNowDate();
		List<FundsTrace> day = fundsTraceService.countToubiao(x3+"000000", x3+"235959", 1, 10);
		result.put("day", day);
		return succ("查询成功", result);
	}
	
	/**
	  * WJW
	  * 资金详情(赏金计划-赏金统计)
	  */
	@ActionKey("/queryRewardDetails")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message queryRewardDetails(){
		String beginDate = getPara("beginDate", "");	// 开始日期
		String endDate = getPara("endDate", "");	// 结束日期
		if (StringUtil.isBlank(beginDate)) {
			return error("00", "开始时间错误", null);
		}
		if (StringUtil.isBlank(endDate)) {
			endDate = DateUtil.getNowDate();
		}
		
		long sumPayAmount = 0l;	// 累计邀请金额
		long sumRewardAcquire = 0l;	// 已回收赏金
		long sumRewardRemain = 0l;	// 待回收赏金
		
		String userCode = getUserCode();
		if (StringUtil.isBlank(userCode)) {
			return error("00", "获取信息失败", null);
		}
		List<LoanTrace> loanTraces = loanTraceService.queryTraceByDate(userCode, beginDate, endDate);	// 查询时间段内注册的被推荐用户投标记录
		sumRewardAcquire = fundsTraceService.sumTraceAmount(beginDate, endDate, traceType.W.val(), fundsType.J.val(), userCode);
		for (LoanTrace loanTrace : loanTraces) {
			Long payAmount = loanTrace.get("payAmount");//投标金额
			sumPayAmount += payAmount;
			sumRewardRemain += rewardRemain(loanTrace);
		}
		HashMap<String, Object> rewardDetails = new HashMap<String,Object>();
		rewardDetails.put("sumPayAmount", Number.longToString(sumPayAmount));
		rewardDetails.put("sumRewardAmount", Number.longToString(sumRewardAcquire + sumRewardRemain));//预期赏金收益
		rewardDetails.put("sumRewardAcquire", Number.longToString(sumRewardAcquire));
		rewardDetails.put("sumRewardRemain", Number.longToString(sumRewardRemain));
		
		return succ("查询成功", rewardDetails);
	}

	
	/**
	  * WJW
	  * 查询单个被推荐用户投标，推荐人所获待回收赏金
	  * @param loanTrace
	  * @return
	  */
	public Long rewardRemain(LoanTrace loanTrace){
		Long payAmount = loanTrace.get("payAmount");//投标金额
		String traceCode=loanTrace.getStr("traceCode");//标书流水编号
		Integer rateByYear=loanTrace.getInt("rateByYear");//年利率
		Integer rewardRateByYear=loanTrace.getInt("rewardRateByYear");//奖励年利率
		Integer loanTimeLimit=loanTrace.getInt("loanTimeLimit");//标期限
		Integer loanRecyCount = loanTrace.getInt("loanRecyCount");//待还款期数
		String refundType = loanTrace.getStr("refundType");//还款方式
		String json_ticket = loanTrace.getStr("loanTicket");//抵用券的使用情况
		String loanState = loanTrace.getStr("loanState");
	
		if(!"N".equals(loanState)){//判断是否是还款中
			return 0L;
		}
		
		boolean isTransfer = loanTransferService.vilidateIsTransfer(traceCode);//标是否有过债权转让
		if(isTransfer){//发生过债权转让
			return 0L;
		}
	
		String code = "";//奖券code
		Integer rate = 0;//奖励利率
		if(!StringUtil.isBlank(json_ticket)){//使用了抵用券
			String type="";//抵用券类型
			Long amount=0L;//抵用券金额(A、B)
			JSONArray jsonArray = JSONArray.parseArray(json_ticket);
			for (int i = 0; i < jsonArray.size(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				type = jsonObject.getString("type");
				amount = jsonObject.getLong("amount");
				code = jsonObject.getString("code");
				rate = jsonObject.getInteger("rate");
			}
		
			if("C".equals(type) && !Tickets.rewardRateInterestTcode.equals(code)){//使用了加息券,加息额度(不包含会员自动加息)
				return 0L;
			}
			
			if("A".equals(type)||"B".equals(type)){//使用了现金抵用券
				payAmount-=amount;
			}
		}	
	
		if(Tickets.rewardRateInterestTcode.equals(code)){//使用了会员自动加息,不享受会员加息部分赏金 
			return (CommonUtil.f_005(payAmount, rateByYear+rewardRateByYear,loanTimeLimit, loanTimeLimit-loanRecyCount, refundType)[1]*5/100)*(rateByYear+rewardRateByYear-rate)/(rateByYear+rewardRateByYear);
		}
		return CommonUtil.f_005(payAmount, rateByYear+rewardRateByYear,loanTimeLimit, loanTimeLimit-loanRecyCount, refundType)[1]*5/100;
	}
	
	@ActionKey("/quickRecharge")
	@AuthNum(value = 999)
	@Before({ AuthInterceptor.class, PkMsgInterceptor.class })
	public void quickRecharge() {
		Message msg = new Message();
		long amount = getParaToLong("amt", 0l);
		String userCode = getUserCode();
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		
		// 充值金额验证
		if (amount <= 0) {
			msg = error("01", "参数异常", amount);
			renderJson(msg);
			return;
		}
		if (amount < 10000) {
			msg = error("02", "最低充值金额100元", amount);
			renderJson(msg);
			return;
		}
		
		// 获取存管电子账号
		String jxAccountId = null;
		String fuiouLoginId = null;
		if (CommonUtil.jxPort) {
			jxAccountId = user.getStr("jxAccountId");
			if (StringUtil.isBlank(jxAccountId)) {
				msg = error("03", "未激活存管账号，请到用户管理页面激活", null);
				renderJson(msg);
				return;
			}
		} else if (CommonUtil.fuiouPort) {
			fuiouLoginId = user.getStr("loginId");
			if (StringUtil.isBlank(fuiouLoginId)) {
				msg = error("03", "未激活存管账号，请到用户管理页面激活", null);
				renderJson(msg);
				return;
			}
		} else {
			msg = error("99", "存管系统对接中...", null);
			renderJson(msg);
			return;
		}
		
		// 银行卡校验
		if (banksV2 == null) {
			msg = error("04", "未查询到理财卡信息", null);
			renderJson(msg);
			return;
		}
		String bankNo = banksV2.getStr("bankNo");
		if (StringUtil.isBlank(bankNo)) {
			msg = error("05", "未绑定理财卡", null);
			renderJson(msg);
			return;
		}

		if (userInfo == null) {
			msg = error("06", "未查询到用户信息", null);
			renderJson(msg);
			return;
		}
		
		String cardId = userInfo.getStr("userCardId");	// 证件号
		String idType = userInfo.getStr("idType");	// 证件类型
		String mobile = banksV2.getStr("mobile");	// 存管预留手机号
		String userCardName = userInfo.getStr("userCardName");	// 真实姓名
		String bankType = banksV2.getStr("bankType");	// 银行卡类型
		String trueName = banksV2.getStr("trueName");	// 真实姓名
		String bankName = banksV2.getStr("bankName");	// 银行卡名称
		
		if (StringUtil.isBlank(cardId)) {
			msg = error("08", "用户未做实名认证", null);
			renderJson(msg);
			return;
		}
		
		// 解析身份证号、手机号
		try {
			cardId = CommonUtil.decryptUserCardId(cardId);
			mobile = CommonUtil.decryptUserMobile(mobile);
		} catch (Exception e) {
			msg = error("07", "解析身份证号、手机号异常", null);
			renderJson(msg);
			return;
		}
		
		// 江西银行存管——验证用户是否设置过交易密码
		String verifyOrderId = getPara("verifyOrderId", "");
		String orginOrderId = (String) Memcached.get("fastByJx_" + verifyOrderId + userCode);
		if (StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orginOrderId)) {
			Map<String, String> pwdMap = null;
			try {
				pwdMap = JXQueryController.passwordSetQuery(jxAccountId);
			} catch (Exception e) {
				msg = error("09", "充值_获取信息超时", "");
				renderJson(msg);
				return;
			}
			if (pwdMap == null || "0".equals(pwdMap.get("pinFlag"))) {
				msg = error("10", "还未设置电子账户交易密码", "");
				renderJson(msg);
				return;
			}
			
			verifyOrderId = CommonUtil.genMchntSsn();
			Memcached.set("fastByJx_" + verifyOrderId + userCode, verifyOrderId, 60 * 1000);
			msg = succ("success", verifyOrderId);
			renderJson(msg);
			return;
		}
		Memcached.delete("fastByJx_" + verifyOrderId + userCode);
		
		// 存管通道选择
		if (CommonUtil.jxPort) {
			// 存储订单信息
			Map<String, String> payParam = new HashMap<String, String>();
			payParam.put("no_order", CommonUtil.genMchntSsn());	// 交易流水号
			payParam.put("user_id", userCode);
			payParam.put("bank_code", "");	// 银行编码
			payParam.put("userBankName", banksV2.getStr("bankName"));	// 银行名称
			payParam.put("info_order", "易融恒信理财充值-快捷充值");
			// 订单入库
			RechargeTrace rechargeTrace = map2trace(payParam);
			rechargeTrace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.FAST.key());
			rechargeTrace.set("traceAmount", amount);
			rechargeTrace.set("bankRemark", "快捷支付,发起申请");
			rechargeTrace.set("traceRemark", "快捷支付,发起申请");
			rechargeTrace.set("userName", user.getStr("userName"));
			rechargeTrace.set("userBankNo", bankNo);
			if (rechargeTrace.save()) {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "生成充值订单成功！");
			} else {
				msg = error("11", "生成充值订单失败!", null);
				renderJson(msg);
				return;
			}
			JXController jxController = new JXController();
			String retUrl = CommonUtil.ADDRESS + "/A00";	// 返回交易页面链接
			String successfulUrl = CommonUtil.ADDRESS + "/paySuccess";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/quickRechargeCallback?traceCode=" + payParam.get("no_order");	// 后台响应链接
			String forgotPwdUrl = CommonUtil.ADDRESS + "/C01";	// 忘记密码链接
			msg = jxController.directRechargePage(jxAccountId, bankNo, userCardName, mobile, idType, cardId, StringUtil.getMoneyYuan(amount), forgotPwdUrl, retUrl, notifyUrl, successfulUrl, getResponse());
			
			String jxTraceCode = (String) msg.getReturn_data();
			if (!StringUtil.isBlank(jxTraceCode)) {
				rechargeTrace.set("bankTraceCode", jxTraceCode.trim());
				if(rechargeTrace.update()){
					BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！");
				}else{
					BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！", null);
				}
			}
			renderNull();
		} else if (CommonUtil.fuiouPort) {
			Map<String, String> payParam = initPayInfo();
			payParam.put("user_id", userCode);
			payParam.put("name_goods", "易融恒信用户充值");
			payParam.put("info_order", "易融恒信理财充值-快捷充值.");
			payParam.put("money_order", String.valueOf(amount));
			payParam.put("userreq_ip", getRequestIP());
			payParam.put("bank_code", bankType);
			payParam.put("money_order", StringUtil.getMoneyYuan(amount));
			payParam.put("id_type", "0");
			payParam.put("id_no", cardId);
			payParam.put("acct_name", trueName);
			payParam.put("userBankName", bankName);
			payParam.put("card_no", bankNo);
			String type = getPara("Type");
			if ("phone".equals(type)) {
				payParam.put("page_notify_url", CommonUtil.ADDRESS + "/fast_showResult4fuiouForP");
			} else {
				String url = getPara("url");
				payParam.put("page_notify_url", url + "/showResult4fuiou");
			}
			// 风控信息
			payParam.put("risk_item", bindRiskInfo(user));
			// 订单入库
			RechargeTrace rechargeTrace = map2trace(payParam);
			rechargeTrace.set("rechargeType", RechargeTrace.RECHARGE_TYPE.FAST.key());
			rechargeTrace.set("traceAmount", amount);
			rechargeTrace.set("bankRemark", "快捷支付,发起申请");
			rechargeTrace.set("traceRemark", "快捷支付,发起申请");
			rechargeTrace.set("userName", user.getStr("userName"));
			rechargeTrace.set("userBankNo", bankNo);
			
			P2p500405ReqData p2p500405ReqData = new P2p500405ReqData();
			AppTransReqData appTransReqData =new AppTransReqData();
			try {
				fuiouLoginId = CommonUtil.decryptUserMobile(fuiouLoginId);
			} catch (Exception e) {
				msg = error("12", "解析存管账号异常", null);
				renderJson(msg);
				return;
			}
			
			if ("phone".equals(type)) {	// 手机快捷充值
				appTransReqData.setAmt(String.valueOf(amount));
				appTransReqData.setLogin_id(fuiouLoginId);
				appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
				appTransReqData.setMchnt_txn_ssn(payParam.get("no_order"));
				appTransReqData.setPage_notify_url(payParam.get("page_notify_url"));
				try {
					FuiouService.app500002(appTransReqData, getResponse());
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {	// PC快捷充值
				p2p500405ReqData.setAmt(String.valueOf(amount));
				p2p500405ReqData.setLogin_id(fuiouLoginId);
				p2p500405ReqData.setMchnt_cd(FuiouController.MCHNT_CD);
				p2p500405ReqData.setMchnt_txn_ssn(payParam.get("no_order"));
				p2p500405ReqData.setPage_notify_url(payParam.get("page_notify_url"));
				try {
					FuiouService.p2p500405(p2p500405ReqData, getResponse());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			// 缓存订单
			Memcached.set("fuiou_recharge_" + payParam.get("no_order"), payParam.get("info_order"), 10 * 60 * 1000);
			if (rechargeTrace.save()) {
				msg = succ("ok", payParam);
				renderJson(msg);
				return;
			} else {
				msg = error("14", "生成充值订单失败!", null);
				renderJson(msg);
				return;
			}
		} else {
			msg = error("99", "存管系统对接中...", null);
			renderJson(msg);
			return;
		}
		
	}
	
	/**
	 * 提现申请
	 * 江西提现通道说明：假设用户提现100元，收1元手续费，则实际用户需付出101元。
	 * 	         企业户提现只能走人行大额通道。							
	 * 调用方式：页面调用，用户需要在页面中输入电子账户密码
	 * 资金扣除方式：提现只会扣除电子账号的可用余额，不会扣除电子账号的冻结金额。
	 * 
	 * 验证是否开通缴费授权，授权金额
	 * @return
	 */
	@ActionKey("/withdraw")
	@AuthNum(value = 999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public void withdraw() {
		Message msg = new Message();
		String userCode = getUserCode();
		long amount = getParaToLong("amount", 0l);
		String routeCode = getPara("routeCode", "");	// 路由代码
		String cardBankCnaps = getPara("cardBankCnaps", "");	// 银行联行号
		User user = userService.findById(userCode);
		UserInfo userInfo = userInfoService.findById(userCode);
		BanksV2 banksV2 = banksService.findByUserCode(userCode);
		
		//保存风险提示书授权
		userTermsAuthService.updateRiskReminder(userCode, "1");
		
		// 参数校验
		if (amount < 300) {
			msg = error("01", "提现金额不能小于3元", "");
			renderJson(msg);
			return;
		}
		
		if ("2".equals(routeCode)) {
			if (amount <= 5000000) {
				msg = error("02", "5万以下的提现，请走普通提现", "");
				renderJson(msg);
				return;
			}
			if (StringUtil.isBlank(cardBankCnaps)) {
				msg = error("03", "大额提现_联行号不能为空", "");
				renderJson(msg);
				return;
			}
		} else {
			if (amount > 5000000) {
				msg = error("04", "5万以上的提现，请走大额提现", "");
				renderJson(msg);
				return;
			}
		}
		
		// 未出借人不允许操作提现
		if (!user.isInvester()) {
			msg = error("05", "非出借人请勿操作", "");
			renderJson(msg);
			return;
		}
		
		// 防诈骗，未投资用户冻结账户
		long loanAmount = fundsTraceService.findTraceAmount(userCode, SysEnum.traceType.P.val());
		long transferAmount = fundsTraceService.findTraceAmount(userCode, SysEnum.traceType.A.val());
		if (loanAmount + transferAmount <= 0 && !SysEnum.userState.F.val().equals(user.getStr("userState"))) {
			user.set("userState", SysEnum.userState.S.val());
			user.update();
			msg = error("05", "此账户提现冻结，请联系客服进行解冻", "");
			renderJson(msg);
			return;
		}
		
		// 冻结账户无法提现
		if (user.isFrozen()) {
			msg = error("55", "账户已被冻结，请联系客服进行解冻", "");
			renderJson(msg);
			return;
		}
		
		// 限制首次提现金额
		Page<WithdrawTrace> withdrawTrace = withdrawTraceService.findByPage(userCode, 1, 10, null, null, null, null);
		if (null == withdrawTrace || withdrawTrace.getTotalRow() <= 0) {
			if (amount < 5000) {
				msg = error("07", "首次提现金额不能小于50元", "");
				renderJson(msg);
				return;
			}
		}
		
		// 验证是否开通存管
		String jxAccountId = null;
		String fuiouLoginId = null;
		if (CommonUtil.jxPort) {
			jxAccountId = user.getStr("jxAccountId");
			if (StringUtil.isBlank(jxAccountId)) {
				msg = error("06", "用户还未激活存管账户", "");
				renderJson(msg);
				return;
			}
		} else if (CommonUtil.fuiouPort) {
			fuiouLoginId = user.getStr("loginId");
			if (StringUtil.isBlank(fuiouLoginId)) {
				msg = error("06", "用户还未激活存管账户", "");
				renderJson(msg);
				return;
			}
		} else {
			msg = error("99", "存管接口对接中...", "");
			renderJson(msg);
			return;
		}
		
		String idType = userInfo.getStr("idType");
		String userCardId = userInfo.getStr("userCardId");
		String userCardName = userInfo.getStr("userCardName");
		
		if (banksV2 == null) {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，未查找到理财卡信息");
			msg = error("05", "未查找到理财卡信息", "");
			renderJson(msg);
			return;
		}
		
		String bankNo = banksV2.getStr("bankNo");
		if (StringUtil.isBlank(bankNo)) {
			msg = error("06", "还未绑定银行卡", "");
			renderJson(msg);
			return;
		}
		
		String userMobile = banksV2.getStr("mobile");	// 存管预留手机号
		try {
			userCardId = CommonUtil.decryptUserCardId(userCardId);
			userMobile = CommonUtil.decryptUserMobile(userMobile);
		} catch (Exception e) {
			msg = error("07", "用户身份证号、手机号解析异常", null);
			renderJson(msg);
			return;
		}
		
		if (CommonUtil.fuiouPort) {	// 富友存管需验证银行卡所在地信息是否完全
			if (StringUtil.isBlank(banksV2.getStr("cardCity"))) {
				msg = error("08", "银行卡省市信息不全", "");
				renderJson(msg);
				return;
			}
		}
		
		// 验证账户资金是否异常
		if (!fundsServiceV2.checkBalance(user)) {
			msg = error("09", "用户资金异常", "");
			renderJson(msg);
			return;
		}
		
		// 判断账户可用资金是否足够提现
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		long avBalance = funds.getLong("avBalance");
		if (avBalance < amount) {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户余额不足");
			msg = error("10", "账户余额不足", "");
			renderJson(msg);
			return;
		}
		
//		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
//		if (CommonUtil.jxPort) {
//			Map<String, String> balanceQuery = JXQueryController.balanceQuery(jxAccountId);
//			String availBal = balanceQuery.get("availBal");	// 可用余额
//			String currBal = balanceQuery.get("currBal");	// 账面余额
//			long a = StringUtil.getMoneyCent(availBal);
//			long b = StringUtil.getMoneyCent(currBal);
//			long avBalance = funds.getLong("avBalance");
//			long frozeBalance = funds.getLong("frozeBalance");
//			if (a != avBalance || (b-a) != frozeBalance) {
//				msg = error("09", "用户资金异常", "");
//				renderJson(msg);
//				return;
//			}
//			if(avBalance < amount){
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户余额不足");
//				msg = error("10", "账户余额不足", "");
//				renderJson(msg);
//				return;
//			}
//			if(a < amount){
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，存管账户可用余额不足，平台可用余额："+StringUtil.getMoneyYuan(avBalance)+",存管可用余额："+availBal);
//				msg = error("11", "存管账户余额不足", "");
//				renderJson(msg);
//				return;
//			}
//		} else if (CommonUtil.fuiouPort) {
//			QueryBalanceResultData fuiouFunds = fuiouTraceService.BalanceFunds(user);
//			if (funds.getLong("avBalance") != Long.parseLong(fuiouFunds.getCa_balance()) ||
//					funds.getLong("frozeBalance") != Long.parseLong(fuiouFunds.getCf_balance())) {
//				msg = error("09", "用户资金异常", "");
//				renderJson(msg);
//				return;
//			}
//			long avBalance = funds.getLong("avBalance");
//			if (null == funds || avBalance < amount) {
//				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户余额不足");
//				msg = error("10", "账户余额不足", "");
//				renderJson(msg);
//				return;
//			}
//		} else {
//			msg = error("99", "存管接口对接中...", "");
//			renderJson(msg);
//			return;
//		}
		
		// 是否使用免费提现次数
		String useFree = getPara("useFree");
		if ("y".equals(useFree)) {
			int useFreeCount = withdrawFreeService.findFreeCountByUserCode(userCode);// 已经免费提现的次数
			int userVipLevel = user.getInt("vipLevel");
			VipV2 vip = VipV2.getVipByLevel(userVipLevel);
			int x = DateUtil.compareDateByStr("yyyyMMdd", "20160411", DateUtil.getNowDate());
			if (x > 0) {
				msg = error("03", "新会员免费提现制度2016年4月11后生效", false);
				renderJson(msg);
				return;
			}
			if (vip.getVipTxCount() != -1) {
				if (useFreeCount >= vip.getVipTxCount()) {
					msg = error("117", "您已免费提现" + useFreeCount + "次，您的会员级别目前最多可免费提现" + vip.getVipTxCount() + "次", false);
					renderJson(msg);
					return;
				}
			}
		}
		
		// 是否使用积分抵扣
		String isScore = getPara("isScore");
		if ("1".equals(isScore) && "y".equals(useFree)) {
			msg = error("110", "积分抵扣、免费提现只能选一种方式", false);
			renderJson(msg);
			return;
		}
		if ("1".equals(isScore) && funds.getLong("points") < 20000) {
			BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，账户积分不足");
			msg = error("11", "账户积分不足", "");
			renderJson(msg);
			return;
		}
		
		// 计算提现手续费
		long fees = withdrawTraceService.calculateSysFee(userCode, amount, isScore, useFree);
		long realAmt = amount - fees;	// 实际提现金额
		
		// 存管通道选择
		if (CommonUtil.jxPort) {
			String verifyOrderId = getPara("verifyOrderId", "");
			String orginOrderId = (String) Memcached.get("withdraw_" + verifyOrderId + userCode);
			if (StringUtil.isBlank(verifyOrderId) || !verifyOrderId.equals(orginOrderId)) {
				// 验证缴费授权
				Map<String, String> authDetail = JXQueryController.termsAuthQuery(jxAccountId);
				if ("1".equals(authDetail.get("paymentAuth"))) {	// 已开通缴费授权
					String paymentDeadline = authDetail.get("paymentDeadline");	// 缴费授权到期日
					String paymentMaxAmt = authDetail.get("paymentMaxAmt");	// 缴费签约最高金额
					int x = DateUtil.compareDateByStr("yyyyMMdd", paymentDeadline, DateUtil.getNowDate());
					if (x < 0) {
						msg = error("12", "缴费授权已过期", "");
						renderJson(msg);
						return;
					}
					if (StringUtil.getMoneyCent(paymentMaxAmt) < fees) {
						msg = error("13", "手续费超过了授权金额", "");
						renderJson(msg);
						return;
					}
				} else {
					msg = error("14", "提现未授权", "");
					renderJson(msg);
					return;
				}
				verifyOrderId = CommonUtil.genMchntSsn();
				Memcached.set("withdraw_" + verifyOrderId + userCode, verifyOrderId, 60 * 1000);
				msg = succ("success", verifyOrderId);
				renderJson(msg);
				return;
			}
			Memcached.delete("withdraw_" + verifyOrderId + userCode);
			// 对公账户提现标识 Y：对公 N：对私  不上送默认为N
			String businessAccountIdFlag = getPara("businessAccountIdFlag", "N");
			
			String withdrawCode = CommonUtil.genMchntSsn();	// 生成交易流水号
			boolean result = false;
			result = withdrawTraceService.save(withdrawCode, userCode,funds.getStr("userName"), userCardName,
					banksV2.getStr("bankNo"), banksV2.getStr("bankNo"),"", banksV2.getStr("bankName"),
					"", amount, "2", isScore, "用户申请提现", "",
					useFree, false);
			if (result == false) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-07", null);
				msg = error("07", "提现异常07", "");
				renderJson(msg);
				return;
			} else {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ：" + StringUtil.getMoneyYuan(amount));
			}
			String retUrl = CommonUtil.ADDRESS + "/B03";
			String forgotPwdUrl = CommonUtil.ADDRESS + "/C01";
			String successfulUrl = CommonUtil.ADDRESS + "/withdrawSuccess";
			String notifyUrl = CommonUtil.CALLBACK_URL + "/withdrawCallback?withdrawCode=" + withdrawCode;
			
			JXController jxController = new JXController();
			msg = jxController.withdrawPage(jxAccountId, userCardName, bankNo, idType, userCardId, userMobile, StringUtil.getMoneyYuan(realAmt), StringUtil.getMoneyYuan(fees), routeCode, cardBankCnaps, retUrl, notifyUrl, forgotPwdUrl, successfulUrl, getResponse(), businessAccountIdFlag);
			String jxTraceCode = (String) msg.getReturn_data();
			if (!StringUtil.isBlank(jxTraceCode)) {
				WithdrawTrace trace = withdrawTraceService.findById(withdrawCode);
				if (trace != null) {
					trace.set("bankTraceCode", jxTraceCode.trim());
					if(trace.update()){
						BIZ_LOG_INFO(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号成功！");
					}else{
						BIZ_LOG_ERROR(userCode, BIZ_TYPE.RECHARGE, "更新银行流水号 失败！", null);
					}
				}
			}
			renderNull();
		} else if (CommonUtil.fuiouPort) {
			String withdrawCode = CommonUtil.genMchntSsn();
			// 新增提现申请记录
			boolean result = false;
			result = withdrawTraceService.save(withdrawCode, userCode,
					funds.getStr("userName"), userInfo.getStr("userCardName"),
					banksV2.getStr("bankNo"), banksV2.getStr("bankNo"),
					banksV2.getStr("bankType"), banksV2.getStr("bankName"),
					banksV2.getStr("cardCity"), amount, "2", isScore, "用户申请提现", "",
					useFree, false);
			if (result == false) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-07", null);
				msg = error("07", "提现异常07", "");
				renderJson(msg);
				return;
			} else {
				BIZ_LOG_INFO(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现成功，提现金额 ：" + StringUtil.getMoneyCent(String.valueOf(amount)));			
			}
			
			int type = getParaToInt("type", 0);
			
			AppTransReqData appTransReqData = new AppTransReqData();
			try {
				fuiouLoginId = CommonUtil.decryptUserMobile(fuiouLoginId);
			} catch (Exception e) {
				msg = error("12", "存管账号解析异常", "");
				renderJson(msg);
				return;
			}
			String url = getPara("url");
			appTransReqData.setAmt(String.valueOf(realAmt));
			appTransReqData.setLogin_id(fuiouLoginId);
			appTransReqData.setMchnt_cd(FuiouController.MCHNT_CD);
			appTransReqData.setMchnt_txn_ssn(withdrawCode);
			if (null == url) {
				appTransReqData.setPage_notify_url(CommonUtil.ADDRESS + "/withdrawPageNotify?tag=" + type);
			} else {
				appTransReqData.setPage_notify_url(url + "/withdrawPageNotify?tag=" + type);
			}
			try {
				if (type == 1) {
					FuiouService.app500003(appTransReqData, getResponse());
				} else {
					FuiouService.p2p500003(appTransReqData, getResponse());
				}
			} catch (Exception e) {
				BIZ_LOG_ERROR(userCode, BIZ_TYPE.WITHDRAWALS, "用户申请提现失败，提现异常-06", e);
				msg = error("06", "提现异常06", "");
				renderJson(msg);
				return;
			}
			msg = succ("申请提现成功，请等待审核", "");
			renderJson(msg);
			return;
		} else {
			msg = error("99", "存管接口对接中...", "");
			renderJson(msg);
			return;
		}
		
	}

	/******************************* private method *********************************************/
	
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
	 * 初始化充值信息 商户号，流水号 
	 * @return
	 */
	private Map<String, String> initPayInfo() {
		Map<String, String> payParam = new TreeMap<String, String>();
		String nowDateTime = DateUtil.getNowDateTime();

		payParam.put("version", "1.0");
		payParam.put("charset_name", "UTF-8");
		payParam.put("oid_partner", CommonUtil.MCHNT_CD); // 商户号
		payParam.put("timestamp", nowDateTime);
		payParam.put("sign_type", "RSA");
		payParam.put("busi_partner", "101001");
		payParam.put("no_order", CommonUtil.genMchntSsn()); // 流水号
		payParam.put("dt_order", nowDateTime);
		payParam.put("valid_order", "30"); // 默认30分钟
		payParam.put("pay_type", "1"); // 默认只支持借记卡

		return payParam;
	}

	private String bindRiskInfo(User user) {
		JSONObject params = new JSONObject();
		if (user != null) {
			String userCode = user.getStr("userCode");
			UserInfo uInfo = UserInfo.userInfoDao.findById(userCode);
			params.put("user_info_mercht_userno", userCode);
			String tmpMobile = user.getStr("userMobile");
			try {
				String uMobile = CommonUtil.decryptUserMobile(tmpMobile);
				params.put("user_info_bind_phone", uMobile);
				params.put("user_info_mercht_userlogin", uMobile);
				params.put("user_info_mail", user.getStr("userEmail"));
			} catch (Exception e) {
				params.put("user_info_bind_phone", "0000000");
			}
			params.put("user_info_dt_register", user.getStr("regDate") + user.getStr("regTime"));
			params.put("user_info_dt_register", user.getStr("regIP"));
			params.put("frms_ware_category", "2009");
			params.put("user_info_full_name", uInfo.getStr("userCardName"));
			try {
				String tmpCardId = CommonUtil.decryptUserCardId(uInfo.getStr("userCardId"));
				if (StringUtil.isBlank(tmpCardId) == false) {
					params.put("user_info_id_no", tmpCardId);
					params.put("user_info_identify_state", "1");
					params.put("user_info_identify_type", "3");
				} else {
					params.put("user_info_identify_state", "0");
				}
			} catch (Exception e) {
				params.put("user_info_id_no", "");
			}
		} else {
			throw new BaseBizRunTimeException("11", "无法获取该用户的信息", null);
		}
		return params.toJSONString();
	}

	/**
	 * 查询用户资金账户信息
	 * @param userCode
	 * @return
	 */
	private Map<String,Object> queryFunds4userCode(String userCode){
		Map<String,Object> resultMap = new HashMap<String, Object>();
		Funds funds = fundsServiceV2.getFundsByUserCode(userCode);
		resultMap.put("avBalance", funds.getLong("avBalance"));
		resultMap.put("frozeBalance", funds.getLong("frozeBalance"));
		resultMap.put("beRecyCount", funds.getInt("beRecyCount"));
		resultMap.put("beRecyPrincipal", funds.getLong("beRecyPrincipal"));
		resultMap.put("beRecyInterest", funds.getLong("beRecyInterest"));
		resultMap.put("score", funds.getLong("points"));
		return resultMap;
	}
	
}

