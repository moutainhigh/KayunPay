package com.dutiantech.controller.admin;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSONObject;
import com.ancun.ops.dto.OpsResponse;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.plugins.CFCA;
import com.dutiantech.plugins.Memcached;
import com.dutiantech.service.CFCAService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.service.LoanTraceService;
import com.dutiantech.service.LoanTransferService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LoggerUtil;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;

public class CFCAController extends BaseController {
	
	private LoanInfoService loanInfoService = getService(LoanInfoService.class);
	private LoanTraceService loanTraceService = getService(LoanTraceService.class);
	private LoanTransferService loanTtansferService = getService(LoanTransferService.class);
	private CFCAService cfcaService = getService(CFCAService.class);
	
	private static final Logger scanCFCALogger = Logger.getLogger("scanCFCALogger");
	
	static{
		LoggerUtil.initLogger("scanCFCA", scanCFCALogger);
	}
	
	@ActionKey("/startCFCA")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message startCFCA() {
		
		//项目,投资信息保全
		saveCFCALoan();
		
		//债权转让数据保全
		saveCFCALoanTransfer();
		
		return succ("已完成。 时间 ： " + DateUtil.getNowDateTime() , "");
	}
	

	/**
	 * 保全标相关信息
	 */
	public void saveCFCALoan(){
		String effectDate = DateUtil.getNowDate();
		String effectTime = DateUtil.getNowTime();
		
		Object objectDate = Memcached.get("cfca_effectDate");
		Object objectTime = Memcached.get("cfca_effectTime");
		if(null != objectDate){
			effectDate = objectDate.toString();
		}
		if(null != objectTime){
			effectTime = objectTime.toString();
		}

		//获取已满标信息
		List<LoanInfo> loanInfoList = loanInfoService.findLoan4effect(effectDate, effectTime);
		
		//设置下一次获取标的起始时间
		Memcached.set("cfca_effectDate", DateUtil.getNowDate());
		Memcached.set("cfca_effectTime", DateUtil.getNowTime());
		
		if(null != loanInfoList && loanInfoList.size() > 0){
			
			//循环处理进行安存
			for (int i = 0; i < loanInfoList.size(); i++) {
				
				LoanInfo loanInfo = loanInfoList.get(i);
				//暂定说明  需要后续调整 20180902
				String loanDesc = "";
				if(SysEnum.productType.E.val().equals(loanInfo.getStr("productType"))){
					loanDesc = "此为个人旅游消费分期产品，每个借款人的身份信息等实体数据都进行关联分析审核，多角度筛选，全方位进行风险量化，确保借款人的还款来源稳定。";
				}else{
					loanDesc = loanInfo.getStr("loanDesc");
				}
				//标信息保全
				try {
					OpsResponse loanInfoResponse = CFCA.cfca4loan(loanInfo.getStr("loanTitle"), 
							loanInfo.getStr("loanNo"),getProductTypeName(loanInfo.getStr("productType")),
//							new BigDecimal((loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear")+loanInfo.getInt("benefits4new"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
							new BigDecimal((loanInfo.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
						    new BigDecimal(loanInfo.getLong("loanAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",
							loanInfo.getInt("loanTimeLimit")+"个月", 
							getRefundType(loanInfo.getStr("refundType")), 
							getFormatDate(loanInfo.getStr("backDate")), 
							getFormatDateTime(loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime")),
							getFormatDateTime(loanInfo.getStr("lastPayLoanDateTime")), 
							getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")),
							loanInfo.getStr("userName"),
							CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
							loanDesc);
					
					if(100000 != loanInfoResponse.getCode()){
						scanCFCALogger.warning("安存标信息保全失败 ！ 错误信息: " + loanInfoResponse.getMsg());
						continue;
					}
					
					//标保全号入库
					JSONObject parseObject = JSONObject.parseObject(loanInfoResponse.getData());
					boolean saveCFCA = cfcaService.saveCFCA(loanInfo.getStr("loanCode"), parseObject.getString("recordNo"), "2");
					if(false == saveCFCA){
						scanCFCALogger.warning("标保全号入库失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
						continue;
					}
					
					//保全投标流水
					saveCFCALoanTrace(loanInfo);
				} catch (Exception e) {
					scanCFCALogger.warning("【标书信息保全失败】借款人身份证号解密失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	/**
	 * 保全投标流水
	 * @param loanInfo
	 */
	public void saveCFCALoanTrace(LoanInfo loanInfo){
		//查询投标流水
		List<LoanTrace> loanTraceList = loanTraceService.findAllByLoanCode(loanInfo.getStr("loanCode"));
				
		//投标流水保全
		if(null != loanTraceList && loanTraceList.size() > 0){
			for (int j = 0; j < loanTraceList.size(); j++) {
				
				LoanTrace loanTrace = loanTraceList.get(j);
				
				UserInfo userInfo = UserInfo.userInfoDao.findById(loanTrace.getStr("payUserCode"));
				User user = User.userDao.findById(userInfo.getStr("userCode"));
				
				try {
					OpsResponse loanTranceResponse = CFCA.cfca4loanTrace(loanInfo.getStr("loanTitle"), 
							loanInfo.getStr("loanNo"), 
							getProductTypeName(loanTrace.getStr("productType")), 
//							new BigDecimal((loanTrace.getInt("rateByYear")+loanTrace.getInt("rewardRateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
							new BigDecimal((loanTrace.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
							new BigDecimal(loanInfo.getLong("loanAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",
							loanInfo.getInt("loanTimeLimit")+"个月", 
							getRefundType(loanInfo.getStr("refundType")), 
							getFormatDate(loanInfo.getStr("backDate")), 
							getFormatDateTime(loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime")),
							getFormatDateTime(loanInfo.getStr("lastPayLoanDateTime")), 
							getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")),
							loanInfo.getStr("userName"),
							CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
							userInfo.getStr("userCardName"), 
							CommonUtil.decryptUserCardId(userInfo.getStr("userCardId")), 
							new BigDecimal((loanTrace.getLong("leftAmount") + loanTrace.getLong("leftInterest"))/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",  
							new BigDecimal(loanTrace.getLong("payAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
							getFormatDateTime(loanTrace.getStr("loanDateTime")),
							getLoanTypeName(loanTrace.getStr("loanType")), 
							getFormatDateTime(loanTrace.getStr("loanDateTime")), 
							loanTrace.getStr("traceCode"),
							CommonUtil.decryptUserMobile(user.getStr("userMobile")));
					
					if(100000 != loanTranceResponse.getCode()){
						scanCFCALogger.warning("安存投标流水保全失败 ！ 错误信息: " + loanTranceResponse.getMsg());
						continue;
					}
					
					//投资保全号入库
					JSONObject parseObject = JSONObject.parseObject(loanTranceResponse.getData());
					boolean saveCFCA = cfcaService.saveCFCA(loanTrace.getStr("traceCode"), parseObject.getString("recordNo"), "1");
					if(false == saveCFCA){
						scanCFCALogger.warning("投标流水保全号入库失败! traceCode : " + loanTrace.getStr("traceCode"));
						continue;
					}
				} catch (Exception e) {
					scanCFCALogger.warning("【投标流水保全失败】身份证号解密失败! traceCode : " + loanTrace.getStr("traceCode"));
					e.printStackTrace();
					continue;
				}
			}
		}
	}
	
	
	/**
	 * 保全债权转让相关信息
	 */
	public void saveCFCALoanTransfer(){
		String effectDate = DateUtil.getNowDate();
		String effectTime = DateUtil.getNowTime();
		
		Object objectDate = Memcached.get("cfca_loanTransfer_effectDate");
		Object objectTime = Memcached.get("cfca_loanTransfer_effectTime");
		if(null != objectDate){
			effectDate = objectDate.toString();
		}
		if(null != objectTime){
			effectTime = objectTime.toString();
		}
		
		//查询债权列表
		List<LoanTransfer> listTransfer = loanTtansferService.queryLoanTransferByDate(effectDate, effectTime);
		
		//设置下一次获取标的起始时间
		Memcached.set("cfca_loanTransfer_effectDate", DateUtil.getNowDate());
		Memcached.set("cfca_loanTransfer_effectTime", DateUtil.getNowTime());
		
		if(null == listTransfer || listTransfer.size() <= 0){
			return;	
		}
		
		//循环保全
		for (int i = 0; i < listTransfer.size(); i++) {
			LoanTransfer loanTransfer = listTransfer.get(i);
			
			UserInfo payUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("payUserCode"));
			UserInfo gotUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("gotUserCode"));
			LoanTrace loanTrace = LoanTrace.loanTraceDao.findById(loanTransfer.getStr("traceCode"));
			LoanInfo loanInfo = LoanInfo.loanInfoDao.findById(loanTransfer.getStr("loanCode"));
			
			try {
				OpsResponse transferResponse = CFCA.cfca4transfer(
						payUserInfo.getStr("userCardName"), 
						loanTransfer.getStr("payUserName"), 
						CommonUtil.decryptUserCardId(payUserInfo.getStr("userCardId")),
						gotUserInfo.getStr("userCardName"), 
						loanTransfer.getStr("gotUserName"), 
						CommonUtil.decryptUserCardId(gotUserInfo.getStr("userCardId")), 
						loanTransfer.getStr("loanTitle"),
						loanTransfer.getStr("loanNo"), 
						new BigDecimal(loanTransfer.getInt("payAmount")/10.0/10).setScale(0, BigDecimal.ROUND_HALF_UP)+ "元",
						loanInfo.getInt("loanTimeLimit")+"个月",
						loanTransfer.getInt("loanRecyCount") + "个月", 
						loanInfo.getStr("userName"),
						CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
//						new BigDecimal((loanTransfer.getInt("rateByYear")+loanTransfer.getInt("rewardRateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
						new BigDecimal((loanTransfer.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
						new BigDecimal(loanTransfer.getInt("leftAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
						new BigDecimal(loanTransfer.getInt("transAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
						new BigDecimal(loanTransfer.getInt("transFee")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
						new BigDecimal(loanTransfer.getInt("transAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP)+ "元",
						getFormatDateTime(loanTransfer.getStr("gotDate")+loanTransfer.getStr("gotTime")), 
						getFormatDate(loanTrace.getStr("loanRecyDate")), 
						"平台账户余额支付"
				);
				
				if(100000 != transferResponse.getCode()){
					scanCFCALogger.warning("安存债权转让保全失败 ！ 错误信息: " + transferResponse.getMsg());
					continue;
				}
				
				//投资保全号入库
				JSONObject parseObject = JSONObject.parseObject(transferResponse.getData());
				boolean saveCFCA = cfcaService.saveCFCA(loanTransfer.getStr("transCode"), parseObject.getString("recordNo"), "5");
				if(false == saveCFCA){
					scanCFCALogger.warning("债权保全号入库失败! transCode : " + loanTransfer.getStr("transCode"));
					continue;
				}
				
			} catch (Exception e) {
				scanCFCALogger.warning("【债权转让保全失败】身份证号解密失败! transCode : " + loanTransfer.getStr("transCode"));
				e.printStackTrace();
				continue;
			}
			
		}
		
	}
	
	
	
	/**
	 * 保全标相关信息
	 */
	@ActionKey("/saveCFCA4Loan")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void saveCFCA4Loan(){
		
		String effectDate = getPara("effectDate");
		String effectTime = getPara("effectTime");
		
		if(StringUtil.isBlank(effectDate)){
			effectDate = DateUtil.getNowDate();
		}
		if(StringUtil.isBlank(effectTime)){
			effectTime = DateUtil.getNowTime();
		}
		
		//获取已满标信息
		List<LoanInfo> loanInfoList = loanInfoService.findLoan4effect(effectDate, effectTime);
		
		if(null != loanInfoList && loanInfoList.size() > 0){
			
			//循环处理进行安存
			for (int i = 0; i < loanInfoList.size(); i++) {
				
				LoanInfo loanInfo = loanInfoList.get(i);
				
				//标信息保全
				try {
					OpsResponse loanInfoResponse = CFCA.cfca4loan(loanInfo.getStr("loanTitle"), 
							loanInfo.getStr("loanNo"),getProductTypeName(loanInfo.getStr("productType")),
							new BigDecimal((loanInfo.getInt("rateByYear"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
//							new BigDecimal((loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear")+loanInfo.getInt("benefits4new"))/10.0/10).setScale(1, BigDecimal.ROUND_HALF_UP)+ "%",
						    new BigDecimal(loanInfo.getLong("loanAmount")/10.0/10).setScale(2, BigDecimal.ROUND_HALF_UP) + "元",
							loanInfo.getInt("loanTimeLimit")+"个月", 
							getRefundType(loanInfo.getStr("refundType")), 
							getFormatDate(loanInfo.getStr("backDate")), 
							getFormatDateTime(loanInfo.getStr("releaseDate")+loanInfo.getStr("releaseTime")),
							getFormatDateTime(loanInfo.getStr("lastPayLoanDateTime")), 
							getFormatDateTime(loanInfo.getStr("effectDate")+loanInfo.getStr("effectTime")),
							loanInfo.getStr("userName"),
							CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId")), 
							loanInfo.getStr("loanDesc"));
					
					if(100000 != loanInfoResponse.getCode()){
						scanCFCALogger.warning("安存标信息保全失败 ！ 错误信息: " + loanInfoResponse.getMsg());
						continue;
					}
					
					//标保全号入库
					JSONObject parseObject = JSONObject.parseObject(loanInfoResponse.getData());
					boolean saveCFCA = cfcaService.saveCFCA(loanInfo.getStr("loanCode"), parseObject.getString("recordNo"), "2");
					if(false == saveCFCA){
						scanCFCALogger.warning("标保全号入库失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
						continue;
					}
					
					//保全投标流水
					saveCFCALoanTrace(loanInfo);
				} catch (Exception e) {
					scanCFCALogger.warning("【标书信息保全失败】借款人身份证号解密失败! loanNo : " + loanInfo.getStr("loanNo") + " cardId : " + loanInfo.getStr("userCardId"));
					e.printStackTrace();
					continue;
				}
			}
		}
	}

	
	
	/**
	 * 获取产品类型
	 * @param productType
	 * @return
	 */
	public String getProductTypeName(String productType){
		String productTypeName = "";
		switch (productType) {
			case "A":
				productTypeName = "质押宝";
				break;
			case "B":
				productTypeName = "车稳盈";
				break;
			case "C":
				productTypeName = "房稳赚";
				break;
			case "E":
				productTypeName = "易分期";
				break;
			default:
				productTypeName = "其它";
				break;
		}
		return productTypeName;
	}
	
	/**
	 * 获取还款方式
	 * @param refundType
	 * @return
	 */
	public String getRefundType(String refundType){
		String refundTypeName = "";
		switch (refundType) {
			case "A":
				refundTypeName = "按月等额本息";
				break;
			case "B":
				refundTypeName = "先息后本";
				break;
			default:
				refundTypeName = "其它";
				break;
		}
		return refundTypeName;
	}
	
	/**
	 * 获取投标方式
	 * @param refundType
	 * @return
	 */
	public String getLoanTypeName(String loanType){
		String loanTypeName = "";
		switch (loanType) {
			case "A":
				loanTypeName = "自动";
				break;
			case "M":
				loanTypeName = "手动";
				break;
			default:
				loanTypeName = "其它";
				break;
		}
		return loanTypeName;
	}
	
	public String getFormatDate(String date){
		if(date.length() < 8){
			return "0000-00-00";
		}
		return date.substring(0,4) + "-" + date.substring(4,6)+ "-" + date.substring(6,8);
	}
	
	public String getFormatDateTime(String dateTime){
		if(dateTime.length() < 14){
			return "0000-00-00 00:00:00";
		}
		return dateTime.substring(0,4) + "-" + dateTime.substring(4,6)+ "-" + dateTime.substring(6,8)
				 + " " + dateTime.substring(8,10) + ":" + dateTime.substring(10,12) + ":" + dateTime.substring(12,14);
	}
}






