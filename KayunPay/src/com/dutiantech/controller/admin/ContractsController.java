package com.dutiantech.controller.admin;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dutiantech.util.*;
import com.dutiantech.util.Number;
import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.ContractTemplate;
import com.dutiantech.model.Contracts;
import com.dutiantech.model.OPUserV2;
import com.dutiantech.model.TransferUser;
import com.dutiantech.service.ContractsService;
import com.dutiantech.service.OPUserV2Service;
import com.dutiantech.service.TransferUserService;
import com.dutiantech.util.SysEnum.StoreNo;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;

public class ContractsController extends BaseController{
	
	private ContractsService contractsService=getService(ContractsService.class);
	private OPUserV2Service opUserV2Service = getService(OPUserV2Service.class);
	private TransferUserService transferUserService = getService(TransferUserService.class);
	
	/**
	 * 查询合同列表信息
	 * @return
	 */
	@ActionKey("/queryContractsByPage")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryContractsByPage(){
		//获取当前页码、每页显示的数量
		Integer pageNumber=getParaToInt("pageNumber",1);
		pageNumber=pageNumber > 0 ? pageNumber:1;
		
		//获取模糊查询字段
		String dimSearch=getPara("dimSearch","");
		
		Integer pageSize=getParaToInt("pageSize",10);
		
		Page<Contracts> contractsList = contractsService.findContractsByPage(pageNumber, pageSize,dimSearch);
		if(pageNumber>contractsList.getTotalPage() && contractsList.getTotalPage()>0){
			pageNumber=contractsList.getTotalPage();
			contractsList = contractsService.findContractsByPage(pageNumber, pageSize,dimSearch);
		}
		for (int i = 0; i < contractsList.getList().size(); i++) {
			Contracts contracts = contractsList.getList().get(i);
			String opCode = contracts.getStr("opCode");
			//查询后台操作员
			String opName = contractsService.queryOpName(opCode);
			contracts.set("opCode", opName);
		}
		return succ("ok", contractsList);
	}
	
	/**
	 * 查询单条合同信息
	 * @return
	 */
	@ActionKey("/queryContractsById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryContractsById(){
		//获取合同编号
		String contractCode=getPara("contractCode");
		
		Contracts contracts = contractsService.findById(contractCode);
		
		String op_code = contracts.getStr("opCode");
		//查询后台操作员
		String opName = contractsService.queryOpName(op_code);
		
		contracts.set("opCode", opName);
		return succ("ok", contracts);
	}
	
	/**
	 * 更新合同信息
	 * @return
	 */
	@ActionKey("/modContracts")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message modContracts(){
		//获取后台操作员的编码
		String opCode=getUserCode();
		//获取合同信息
		String contractCode=getPara("contractCode");
		Integer uid=getParaToInt("uid");
		String title=getPara("title","");
		String content=getPara("content","");
		String updateDateTime=DateUtil.getNowDateTime();
		
		Map<String, Object> para = new HashMap<String, Object>();
		para.put("uid", uid);
		para.put("title", title);
		para.put("content", content);
		para.put("opCode", opCode);
		para.put("updateDateTime", updateDateTime);
		
		boolean result = contractsService.modContracts(contractCode, para);
		if(result){
			return succ("ok", result);
		}
		return error("no", "修改失败", null);
	}
	
	@ActionKey("/createContract")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public void contractV3(){
		String shopNum = getPara();
		redirect("/contractV3?shopNum="+shopNum,true);
	}
	
	/**
	 * 添加合同信息
	 * @return
	 * @throws ParseException 
	 * @throws NumberFormatException 
	 */
	@ActionKey("/addContracts")
	@AuthNum(value=999)
	@Before({PkMsgInterceptor.class})
	public Message addContracts(){
		// 获取后台操作人员编码
		/*String opUserCode = getUserCode();
		OPUserV2 opUserV2 = opUserV2Service.findById(opUserCode);
		String opGroup = opUserV2.getStr("op_group");
		if (StringUtil.isBlank(opGroup)) {
			return error("01", "获取门店信息失败", null);
		}*/
		String operateIp = getRequestIP();
		// 根据合同类型获取合同模板
		String contractTemplateType = getPara("templateType", "");
		if (StringUtil.isBlank(contractTemplateType)) {
			return error("01", "合同类型错误", null);
		}
		String loanTypeForUser = getPara("loanTypeForUser","");
		if(StringUtil.isBlank(loanTypeForUser)){
			return error("01","借款用户类型为空",null);
		}
		String offlineShop = getPara("shop","");
		if(StringUtil.isBlank(offlineShop)){
			return error("01","线下门店为空",null);
		}
		String offlineShopNum = getPara("shopNum","");
		if(StringUtil.isBlank(offlineShopNum)){
			return error("01","门店号为空",null);
		}
		
		String gatheringName = getPara("gatheringName","");
		if(StringUtil.isBlank(gatheringName)){
			return error("01","收款人不能为空",null);
		}
		String gatheringBankNumber = getPara("gatheringBankNumber","");
		if(StringUtil.isBlank(gatheringBankNumber)){
			return error("01","收款人账户不能为空",null);
		}
		
		String loanTypeForContract = getPara("loanTypeForContract","");//0:车贷  1：信贷
		if(StringUtil.isBlank(loanTypeForContract)){
			return error("01","借款类型不能为空",null);
		}
		
		
		ContractTemplateController conTemplController = new ContractTemplateController();
		List<ContractTemplate> contractTemplates = conTemplController.queryByType(loanTypeForContract,contractTemplateType,loanTypeForUser);
		// 解析页面填写 
		/*String transferUserNo=getPara("transferUserNo", "");//债权人编号
		if(transferUserNo.equals("")){
			return error("02", "获取债权人失败", null);
		}
		TransferUser transferUser = transferUserService.findById(transferUserNo);//根据id查询债权人信息*/
		String loanUserName=getPara("loanUserName", "");	// 借款人姓名
		String loanUserCardId=getPara("loanUserCardId", "");	// 借款人身份证号
		if(loanUserName.equals("")||loanUserCardId.equals("")){
			return error("03", "获取借款人失败", null);
		}
		Long loanAmount=getParaToLong("loanAmount", (long) 0)/100;//借款金额
		if(loanAmount==0){
			return error("04", "获取借款金额失败", null);
		}
		String loanAmountCN = NumberToCN.number2CNMontrayUnit(new BigDecimal(loanAmount));
		Long moneyLoan=getParaToLong("moneyLoan",(long) 0);//现金支付金额
		Long bankLoan=loanAmount-moneyLoan;//银行转账
		//long rateByMonth=(long)((Double.parseDouble(getPara("rateByMonth","0"))/10.0/10.0/12)*100);//借款利率
		long rateByYear = getParaToLong("rateByYear",0L);//年利率，24.01% 则为2401
		
		

		
		
		
		/*Long sumFinancingMoney = getParaToLong("sumFinancingMoney",(long) 0)/100;//一次 融资服务费
		String sumFinancingMoneyCN = NumberToCN.number2CNMontrayUnit(new BigDecimal(sumFinancingMoney));
		
		Long financingMoney = getParaToLong("financingMoney",(long) 0)/100;//融资服务费
		Long fee = getParaToLong("fee",(long) 0)/100;//手续费
		Long guaranteeFee = getParaToLong("guaranteeFee",(long) 0)/100;//担保费 
		Long incidentals = getParaToLong("incidentals",(long) 0)/100;//杂费
		Long detailMoney = getParaToLong("detailMoney",(long) 0)/100;//押金
		Long otherMoney = getParaToLong("otherMoney",(long) 0)/100;//其它费用 
		
		
		String entrustPersonName = getPara("entrustPersonName","");//首次款卡姓名
*/
		
		if(rateByYear==0){
			return error("05", "获取借款利率失败", null);
		}
		/*long rateBySynthesize=getParaToLong("rateBySynthesize",(long)0);//综合利率
*/		/*if(rateBySynthesize==0){
			return error("06", "获取综合利率失败", null);
		}*/
		Integer limit=getParaToInt("limit", 0);//借款期限
		if(limit==0){
			return error("07", "获取借款期限失败", null);
		}
		
		//汽车信息
		String carName="";//汽车名
		String carBrand="";//车辆品牌
		String carNumber="";//车辆牌号
		String engineNumber="";//发动机号
		String carframe="";//车架号码
		String carList="";//抵押车辆列表
		String carMoney = "";
		String owner = "";
		
		String cars=getPara("cars", "");//车辆信息
		if(!"".equals(cars)){
			String[] carsArray=cars.split(",");
			for (String carss : carsArray) {
				String[] carsArrays = carss.split("\\|");
				carList+="<p class='MsoNormal' style='text-indent:21pt;' align='left'>";
				carList+="<span style='line-height:2;'>车辆品牌："+carsArrays[0]+"&nbsp;车辆牌号："+carsArrays[2]+"&nbsp;发动机号："+carsArrays[3]+"&nbsp;车架号码："+carsArrays[4]+"&nbsp;车辆违章费："+carsArrays[5]+"&nbsp;车主姓名："+carsArrays[6]+"</span></p>";
				
				carName+=carsArrays[0]+carsArrays[1]+" ";
				carBrand+=carsArrays[0]+" ";
				carNumber+=carsArrays[2]+" ";
				engineNumber+=carsArrays[3]+" ";
				carframe+=carsArrays[4]+" ";
				carMoney+=carsArrays[5]+" ";
				owner+=carsArrays[6]+" ";
			}
		}else {
			carList="<p class='MsoNormal' style='text-indent:18.05pt;' align='left'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</p>";
		}
		
		//担保人
		ArrayList<String[]> lstDbrs=new ArrayList<String[]>();
		String dbrs=getPara("dbrs", "");
		if("".equals(dbrs)){
			lstDbrs.add(new String[]{"","","",""});
		}else {
			String[] dbrsArray=dbrs.split(",");
			for (String dbr : dbrsArray) {
				String[] split = dbr.split("\\|");
				lstDbrs.add(split);
			}
		}
		String loanDate=getPara("loanDate", "");	// 签订时间
		if(StringUtil.isBlank(loanDate)){
			return error("01","签订时间为空",null);
		}
		String signYear = null, signMonth = null, signDay = null;
		if (loanDate != null && loanDate.length() == 8) {
			signYear = loanDate.substring(0, 4);
			signMonth = loanDate.substring(4, 6);
			signDay = loanDate.substring(6, 8);
		}
		
		String rDate = DateUtil.addMonth(loanDate, 1);
		String rYear = null,rMonth = null,rDay = null;
		if(rDate != null && rDate.length() == 8){
			rYear = rDate.substring(0, 4);
			rMonth = rDate.substring(4, 6);
			rDay = rDate.substring(6, 8);
		}
		int limit1 = 0;
		if("01".equals(signDay)){
			limit1 = limit-1;
		}else{
			limit1 = limit;
		}
		
		String cDate = null;
		String cDay = null;
		if("01".equals(signDay)){
			   if(signYear != null && signMonth != null){
				   cDay = String.valueOf(DateUtil.getDaysByYearMonth(loanDate));
			   }		
		}else {
			cDate = DateUtil.delDay(loanDate, 1);//签约前一天还款
			if(cDate != null && cDate.length() == 8){
				cDay = cDate.substring(6,8);
			}
		}

		String repayDate=DateUtil.addMonth(loanDate, limit1);	// 还款起时间
		String repayYear = null, repayMonth = null, repayDay = null;
		String repayYearStart = null, repayMonthStart = null;//结束时间
		if (repayDate != null && repayDate.length() == 8) {
			repayYear = repayDate.substring(0, 4);
			repayMonth = repayDate.substring(4, 6);
			repayDay = repayDate.substring(6, 8);
		}
		if("01".equals(signDay)){
			repayYearStart = signYear;
			repayMonthStart = signMonth;
			repayDay = cDay;
		}	else{
			repayYearStart =rYear;
			repayMonthStart = rMonth;
		}
		
		
		String breakRuleDate = DateUtil.addMonth(loanDate, 1);//违章消除时间
		String breakRuleYear = null,breakRuleMonth = null,breakRuleDay = null;
		if(breakRuleDate != null && breakRuleDate.length() == 8){
			breakRuleYear = breakRuleDate.substring(0,4);
			breakRuleMonth = breakRuleDate.substring(4,6);
			breakRuleDay = breakRuleDate.substring(6,8);
		}
		
		//违约金
		double badPromiseMoeny = loanAmount * 8 /10.0/10.0/10.0;
		long GPSManagerMoney = getParaToLong("GPSManageMoney",0L);//GPS管理费
		//出借人的每月本息
		//long rate = rateByMonth*12;
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		long benxi= licai.dengebenxi();
		//门店每月商务服务费用
		long rateByService = getParaToLong("rateByService",0L);//月服务费率
		float rateByService1 = (float) (rateByService /10.0/10.0/10/10);
		long serviceMoneyByShopMonth = (long) (loanAmount * rateByService1 * 100); 
		//门店商务服务费用总额
		long serviceMoneyByShop = serviceMoneyByShopMonth * limit;
		//平台每月融资服务费
		long financingByMonth = 0;
		/*if(limit<12){
			LiCai licai2 = new LiCai(loanAmount*100, 18*100, limit);
			long benxi2= licai2.dengebenxi();
			financingByMonth = benxi2-benxi;
		}else{
			LiCai licai2 = new LiCai(loanAmount*100, 21*100, limit);
			long benxi2= licai2.dengebenxi();
			financingByMonth = benxi2-benxi;
		}*/
		  long rateByCompany = getParaToLong("rateByCompany",0L);//平台成本
			LiCai licai2 = new LiCai(loanAmount*100, rateByCompany, limit);
			long benxi2= licai2.dengebenxi();
			financingByMonth = benxi2-benxi;
		
		if(financingByMonth<0){
			return error("01","融资服务费有误,请确认借款月利率",null);
		}
		
		//借款人每期应还总金额
		long amountRepayByMonth = 0;
		if("0".equals(loanTypeForContract)){//车贷
			amountRepayByMonth = benxi + financingByMonth + serviceMoneyByShopMonth + GPSManagerMoney;
		}else if("1".equals(loanTypeForContract)){//信贷
			amountRepayByMonth = benxi + financingByMonth + serviceMoneyByShopMonth;
		}
		
		//出借车辆押金
		double detailMoney = (double)amountRepayByMonth * limit / 12 /100;
		
		/*Long entrustMoney = financingMoney+fee+guaranteeFee+incidentals+(amountRepayByMonth * limit/12/100)+otherMoney;
		String entrustMoneyCN = NumberToCN.number2CNMontrayUnit(new BigDecimal(entrustMoney));//委托支付金额
		Long bankLoan1 = loanAmount-entrustMoney;//余下银行转账
		String bankLoan1CN = NumberToCN.number2CNMontrayUnit(new BigDecimal(bankLoan1));*/
		
		
		
		long materialMoney = getParaToLong("materialMoney",0L);
		String materialMoneyCN = NumberToCN.number2CNMontrayUnit(new BigDecimal(materialMoney/100));
		String legalUserCardId = getPara("legalUserCardId","");//法人身份证号
		//合同编号
		String contractsNum = "";
		StringBuilder contractNo = new StringBuilder();
		contractNo.append(offlineShopNum);
		// 拼接合同门店号
		/*for (StoreNo store : StoreNo.values()) {
			if (store.val().equals(transferUser.getStr("companyName"))) {
				contractNo.append(store.desc());
			}
		}*/
		// 拼接合同生成日期
		contractNo.append(DateUtil.getNowDate());
		// 拼接合同编号
		long tmpLastNo = contractsService.countByDate(DateUtil.getNowDate())+1;
		if (tmpLastNo < 10) {
			contractNo.append("0" + tmpLastNo);
		} else {
			contractNo.append(tmpLastNo);
		}
		
		contractsNum = contractNo.toString();
		
		String templateType = getPara("templateType", "");	// 合同类型
		String refundType = templateType.replace("GPS", "").replace("质押", "");	// 还款方式
		//String contractsNum = getPara("contractsNum","");	// 合同编号 
		/*String refundDetailTable = getRefundDetailTable(loanAmount,limit,rateBySynthesize,rateByYear,refundType,loanDate);	// 还款明细表：每期还款金额
*/		String refundPlanTable = getRefundPlanTable(loanAmount, limit, rateByYear, refundType, loanDate);	// 借款合同（质押） ：还款计划明细表
		String deditPayStandardTable = getDeditPayStandardTable(loanAmount, rateByYear, limit, refundType);	// 借款合同（质押）：提前结清违约金支付标准表
		/*String serviceChargeTable = getServiceChargeTable(loanAmount, limit, rateBySynthesize, rateByYear, refundType, loanDate);	// 融资服务协议：服务费支付时间及金额表
*/		String refundDetailMonthTable ="";
        if("0".equals(loanTypeForContract)){
        	//车贷
        	refundDetailMonthTable = refundDetailMonthTable(GPSManagerMoney,serviceMoneyByShopMonth,financingByMonth,loanAmount, limit,rateByYear, refundType, loanDate);//客户还款明细表 ： 每月还款金额
        }else if("1".equals(loanTypeForContract)){
        	//信贷
        	refundDetailMonthTable = refundDetailMonthTable1(serviceMoneyByShopMonth,financingByMonth,loanAmount, limit,rateByYear, refundType, loanDate);//客户还款明细表 ： 每月还款金额
        }
		
		
        //借款基本信息表
		String loanUserInformationTable = loanUserInformationTable(loanTypeForContract,loanTypeForUser,rateByYear,GPSManagerMoney,serviceMoneyByShopMonth,financingByMonth,loanUserName,contractsNum,loanUserCardId,getPara("loanUserMobile", ""),loanAmount,limit,refundType,getPara("loanBankNo", ""),getPara("loanBankName", ""));
		String detailedListTable = "";
		if("0".equals(loanTypeForContract)){
			detailedListTable=detailedListTable(materialMoney,Double.parseDouble(carMoney.trim()),detailMoney);//首次费用清单表
		}else if("1".equals(loanTypeForContract)){
			detailedListTable = deatailedListTable1(materialMoney,detailMoney);
		}
				



		//利息总额
		 long xiSum = benxi * limit - loanAmount*100;
		 
		// Replace合同模板
		Map<String, String> map = new HashMap<String, String>();
		map.put("[loanUserInformationTable]", loanUserInformationTable);//借款基本信息表
		map.put("[repayYearStart]", repayYearStart);//还款开始时间年
		map.put("[repayMonthStart]", repayMonthStart);//还款开始时间月
		map.put("[detailedListTable]", detailedListTable);//首次费用清单表
		map.put("[serviceMoneyByShopMonth]", Number.longToString(serviceMoneyByShopMonth));
		map.put("[serviceMoneyByShop]", Number.longToString(serviceMoneyByShop));//门店
		map.put("[cDay]", cDay);//还款日
		map.put("[breakRuleYear]",breakRuleYear);//消除违章年
		map.put("[breakRuleMonth]", breakRuleMonth);//消除违章月
		map.put("[breakRuleDay]", breakRuleDay);//消除违章日
		map.put("[shopAddress]",getPara("shopAddress",""));//线下门店地址
		map.put("[shopPhone]",getPara("shopPhone",""));//负责人的电话
		map.put("[materialMoney]",Number.longToString(materialMoney));//人工材料费
		map.put("[materialMoneyCN]",materialMoneyCN);//人工材料费大写
		map.put("[GPSManagerMoney]", Number.longToString(GPSManagerMoney));//GPS费
		map.put("[rateByCompany]",getPara("rateByCompany",""));//平台综合成本  18%   21%
		map.put("[gatheringName]",gatheringName);//收款人姓名
		map.put("[gatheringBankName]",getPara("gatheringBankName",""));//收款人开户行
		map.put("[gatheringBankNumber]",gatheringBankNumber);//收款人卡号
		map.put("[onlineName]",getPara("onlineName",""));//线上平台
		map.put("[onlineAddress]",getPara("onlineAddress",""));//线上平台地址
		map.put("[onlinePhone]",getPara("onlinePhone",""));//线上平台电话
		map.put("[offlineShop]",offlineShop);//线下门店
		map.put("[repayYearEnd]",repayYear);//还款结束年
		map.put("[repayMontEnd]",repayMonth);//还款结束月
		map.put("[financingByMonth]",Number.longToString(financingByMonth));
		map.put("[legalUserCardId]",legalUserCardId);//法人身份证
		map.put("[benxi]",Number.longToString(benxi) );//每月应还本息
		map.put("[xiSum]",Number.longToString(xiSum));//利息总额 
		map.put("[xiByMonth]",Number.longToString(xiSum / limit));//每月的平均利息
		map.put("[xiToloanAmount]", Number.doubleToStrByTwoDecimal((double)xiSum  / limit  / loanAmount)+"%");//利息占借款金额的占比
		map.put("[rzToloanAmount]",Number.doubleToStrByTwoDecimal((double)financingByMonth /loanAmount)+"%");
		map.put("[serviceToLoanAmount]", Number.doubleToStrByTwoDecimal((double)serviceMoneyByShopMonth/loanAmount)+"%");
		map.put("[badPromiseMoeny]", Number.doubleToStr(badPromiseMoeny*100));//违约金
		map.put("[detailMoney]", Number.longToString((long)(detailMoney*100)));
		/*map.put("[transferUserName]", transferUser.getStr("name"));//债权人姓名
		map.put("[transferUserCardId]", transferUser.getStr("cardId"));//债权人身份证号
		map.put("[transferUserAddress]", transferUser.getStr("address"));//债权人住址
		map.put("[transferUserMobile]", transferUser.getStr("mobile"));//债权人联系电话
		map.put("[transferUserSex]", ("M".equals(transferUser.getStr("sex"))?"男":"女"));//债权人性别
		map.put("[transferUserbankNo]", transferUser.getStr("bankNo"));//债权人银行卡卡号
		map.put("[TUbankUserName]", transferUser.getStr("bankUserName"));//债权人银行卡户名
		map.put("[TUbankName]", transferUser.getStr("bankName"));//债权人开户行全称*/
	
		map.put("[loanUserName]", loanUserName);	// 借款人姓名
		map.put("[loanUserCardId]", loanUserCardId);	// 借款人身份证号
		map.put("[loanUserAddress]", getPara("loanUserAddress", ""));	// 借款人家庭住址
		map.put("[loanUserMobile]", getPara("loanUserMobile", ""));	// 借款人手机号
		map.put("[serveMobile]", getPara("serveMobile", ""));	// 客服专号
		map.put("[loanAmount]", loanAmount.toString());	// 贷款金额小写
		map.put("[loanAmountCN]", loanAmountCN);	// 贷款金额大写
		map.put("[moneyLoan]", moneyLoan.toString());	// 现金支付金额
		map.put("[bankLoan]", bankLoan.toString());	// 银行转账
		map.put("[rateByMonth]", Number.doubleToStr(rateByYear/12.0*100)+"%");	// 借款利率
		/*map.put("[rateBySynthesize]", Number.longToString(rateBySynthesize)+"%");	// 综合利率*/
		map.put("[loanUsed]", getPara("loanUsed", ""));//借款用途
		map.put("[limit]", limit.toString());//借款期限
		map.put("[ensureCompanyName]", getPara("ensureCompanyName", ""));//担保人姓名(公司)
		map.put("[companyCode]", getPara("companyCode", ""));//担保人公司组织机构代码(公司)
		map.put("[businessPlace]", getPara("businessPlace", ""));//担保人经营场所(公司)
		map.put("[companyPhone]", getPara("companyPhone", ""));//担保人联系电话(公司)
		map.put("[loanBankNo]", getPara("loanBankNo", ""));//银行卡卡号
		map.put("[loanBankName]", getPara("loanBankName", ""));//银行全称
		map.put("[addYear]", signYear);	// 签订年份
		map.put("[addMonth]", signMonth);	// 签订月份
		map.put("[addDay]", signDay);	// 签订日期
		map.put("[repayYear]", repayYear);	// 还款年份
		map.put("[repayMonth]", repayMonth);	// 还款月份
		map.put("[repayDay]", repayDay);	// 还款日期
		map.put("[templateType]", templateType);	// 合同类型
		map.put("[refundType]", refundType);	// 还款方式
		map.put("[carManager]", templateType.replace("等额本息", "").replace("先息后本", "").replace("等本等息", ""));//车辆管理（GPS、质押）
		map.put("[companyAddress]", getPara("companyAddress", ""));//收车费用标准
		map.put("[contractsNum]", contractsNum);	// 合同编号
		map.put("[addressSign]", getPara("addressSign", ""));	//合同签署地
		map.put("[court]", getPara("court", ""));	//合同签署地法院
		/*map.put("[refundDetailTable]", refundDetailTable);	// 还款明细表：每期还款金额
*/		map.put("[refundPlanTable]", refundPlanTable);	// 借款合同（质押） ：还款计划明细表
		map.put("[deditPayStandardTable]", deditPayStandardTable);	// 借款合同（质押）：提前结清违约金支付标准表
		/*map.put("[serviceChargeTable]", serviceChargeTable);	// 融资服务协议：服务费支付时间及金额表*/
		map.put("[refundDetailMonthTable]",refundDetailMonthTable);//客户还款明细表： 每月还款金额
		
		map.put("[carList]", carList);//抵押车辆列表
		map.put("[carName]", carName);//汽车名
		map.put("[carBrand]", carBrand);//车辆品牌
		map.put("[carNumber]", carNumber);//车辆牌号
		map.put("[engineNumber]", engineNumber);//发动机号
		map.put("[carframe]", carframe);//车架号码
		map.put("[carMoney]", carMoney);
		map.put("[owner]",owner);
		
		/*map.put("[sumFinancingMoney]", sumFinancingMoney.toString());//一次 融资服务费
		map.put("[sumFinancingMoneyCN]", sumFinancingMoneyCN);
		map.put("[financingMoney]", financingMoney.toString());//融资服务费
		map.put("[fee]", fee.toString());//手续费
		map.put("[guaranteeFee]", guaranteeFee.toString());//担保费 
		map.put("[incidentals]", incidentals.toString());//杂费
		map.put("[detailMoney]", detailMoney.toString());//押金
		map.put("[otherMoney]", otherMoney.toString());//其它费用 
		map.put("[entrustMoney]", entrustMoney.toString());//委托支付金额
		map.put("[entrustMoneyCN]", entrustMoneyCN);
		map.put("[bankLoan1]", bankLoan1.toString());//余下银行转账
		map.put("[bankLoan1CN]", bankLoan1CN);
		map.put("[entrustPersonName]", entrustPersonName);//首次款卡姓名
		*/
		
		
		//替换合同模板内容
		String errTitle="";//合同添加错误信息
		for (ContractTemplate contractTemplate : contractTemplates) {
			String title = contractTemplate.getStr("title");//合同标题
			String content = contractTemplate.getStr("content");//合同内容
			Contracts contract = new Contracts();
			contract.set("contractCode", UIDUtil.generate());//合同code
			contract.set("title", title + contractsNum);
			contract.set("contractsNum", contractsNum);//合同编号
			contract.set("opCode", operateIp);
			contract.set("addDateTime", DateUtil.getNowDateTime());
			contract.set("updateDateTime", DateUtil.getNowDateTime());
			boolean save=true;
			
			//循环打印担保人合同
			if("担保承诺书".equals(title)||"担保书（个人版）".equals(title)){
				for (String[] dbr : lstDbrs) {
					map.put("[ensureName]", dbr[0]);//担保人姓名
					map.put("[ensureCardId]", dbr[1]);//担保人 身份证号
					map.put("[ensureMobile]", dbr[2]);//担保人 联系电话
					map.put("[ensureAddress]", dbr[3]);//担保人住所
					String content1 = contractTemplate.getStr("content");
					content1 = conTemplController.replaceTemplateContent(content1, map);//替换合同字段
					contract.set("content", content1);
					save = contractsService.save(contract);
					if(!save){
						errTitle+=" "+title+"-"+dbr[0];
					}
				}
				continue;
			}
			content = conTemplController.replaceTemplateContent(content, map);//替换合同字段
			contract.set("content", content);
			save = contractsService.save(contract);
			if(!save){
				errTitle+="_"+title;
			}
		}
		if("".equals(errTitle)){
			return succ("succ08", contractsNum);
		}else{
			return error("err09", "添加错误："+errTitle, null);
		}
	}
	
	

	//信贷
	private String deatailedListTable1(long materialMoney, double detailMoney) {
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html += "<tr><td>人工费，下户交通费</td><td>" + Number.longToString(materialMoney) +"</td></tr>";
		output_html +="<tr><td>垫付准备押金</td><td>" + Number.longToString((long)(detailMoney*100)) + "</td></tr>";
		long sum = materialMoney+(long)(detailMoney*100);
		output_html +="<tr><td>合计</td><td>" +Number.longToString(sum) + "</td></tr>";
		output_html +="</table>";
		return output_html;
	}
    //车贷
	private String detailedListTable(long materialMoney, double carMoney, double detailMoney) {
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html += "<tr><td>人工费，GPS材料费，下户交通费</td><td>" + Number.longToString(materialMoney) +"</td></tr>";
		output_html +="<tr><td>消除违章押金</td><td>" + Number.longToString((long)(carMoney*100)) + "</td></tr>";
		output_html +="<tr><td>借用车辆押金</td><td>" + Number.longToString((long)(detailMoney*100)) + "</td></tr>";
		long sum = materialMoney+(long)(carMoney*100)+(long)(detailMoney*100);
		output_html +="<tr><td>合计</td><td>" +Number.longToString(sum) + "</td></tr>";
		output_html +="</table>";
		return output_html;
	}

	private String loanUserInformationTable(String loanTypeForContract, String loanTypeForUser,long rateByYear,long GPSManagerMoney,long serviceMoneyByShopMonth,long financingByMonth,String loanUserName,String contractNum,String loanUserCardId,String loanUserMobile,long loanAmount,int limit,String refundType,String loanBankNo,String loanBankName){
		        //年利率
				//long rate = rateByMonth*12;
				String name = "客户姓名";
				String id = "身份证号码";
				if("企业用户".equals(loanTypeForUser)){
					name = "企业名称";
					id = "企业代码";
				}
				//拼接表格 
				String output_html = "<table style='width:100%;text-align: center;' cellpadding='4' cellspacing='0' border='1' bordercolor='#000000'>";
				//计算还款
				LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
				long dengebenxi = licai.dengebenxi();
				long sum = dengebenxi+financingByMonth+serviceMoneyByShopMonth+ GPSManagerMoney;
				output_html += "<tr><td>"+name+"</td><td>" + loanUserName +"</td><td>合同编号</td><td>"+contractNum+"</td></tr>";
				output_html +="<tr><td>"+id+"</td><td>" + loanUserCardId + "</td><td>电话号码</td><td>"+loanUserMobile+"</td></tr>";
				output_html +="<tr><td>借款金额</td><td>" + Number.longToString(loanAmount*100) +"</td><td>借款期数</td><td>"+limit+"</td></tr>";
				output_html +="<tr><td>还款方式</td><td>" + "分期还款" + "</td><td>每期应还总金额</td><td>"+Number.longToString(sum)+"</td></tr>";
				if("0".equals(loanTypeForContract)){
					output_html +="<tr><td>客户类型</td><td>" +"新客户       接力贷" +  "</td><td></td><td></td></tr>";
				}
				
				output_html +="<tr><td>开户行/卡号</td><td colspan='3'>" +loanBankName +" / " +loanBankNo +  "</td></tr>";
				output_html +="</table>";
				return output_html;
	}



	
	/**
	 * 删除单条合同信息
	 * @return
	 */
	@ActionKey("/delContractsById")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message delContractsById(){
		//获取合同编码
		String contractCode=getPara("contractCode");
		
		boolean result = contractsService.delContractsById(contractCode);
		if(result){
			return succ("ok", result);
		}
		return error("no", "删除失败!", null);
	}
	
	/**
	 * 生成合同编号
	 * @return
	 */
	@ActionKey("/generateContractNo")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class, PkMsgInterceptor.class})
	public Message generateContractNo() {
		String opUserCode = getUserCode();
		if (StringUtil.isBlank(opUserCode)) {
			return error("01", "获取用户信息失败", null);
		}
		StringBuilder contractNo = new StringBuilder();
		// 拼接合同门店号
		OPUserV2 opUserV2 = opUserV2Service.findById(opUserCode);
		String opGroup = opUserV2.getStr("op_group");
		for (StoreNo store : StoreNo.values()) {
			if (store.val().equals(opGroup)) {
				contractNo.append(store.desc());
			}
		}
		// 拼接合同生成日期
		contractNo.append(DateUtil.getNowDate());
		// 拼接合同编号
		long tmpLastNo = contractsService.countByDate(DateUtil.getNowDate())+1;
		if (tmpLastNo < 10) {
			contractNo.append("0" + tmpLastNo);
		} else {
			contractNo.append(tmpLastNo);
		}
		if (StringUtil.isBlank(opGroup)) {
			return error("01", "获取门店信息失败", null);
		}
		return succ("suc", contractNo.toString());
	}
	/**
	 * 还款明细表：每期还款金额
	 * @param loanAmount	贷款金额
	 * @param limit		期数
	 * @param rate		利率
	 * @param rateBySynthesize		综合利率
	 * @param rateByYear		年利率
	 * @param refundType	还款方式
	 * @param loanDate		借款日期
	 * @return
	 */
	public String getRefundDetailTable(long loanAmount, int limit, long rateBySynthesize, long rateByYear, String refundType, String loanDate){
		//年利率
		//long rate = rateByMonth*12;
		//借款日期前一天
		String returnDate = DateUtil.delDay(loanDate, 1);
		//获取截止日期
		String dateEndCH = getDateEndCH(limit, loanDate, returnDate);
		
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html+="<tr><td>序号</td><td>还款时间</td><td>本金(元)</td><td>利息(元)</td><td>服务费(元)</td><td>合计(元)</td></tr>";
		//计算还款
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
		if("等本等息".equals(refundType)){
			licaiMap = licai.getDengEList();
		}else{
			licaiMap = licai.getDengXiList();
		}
		for (int i = 0; i < licaiMap.size(); i++) {
			int month = licaiMap.get(i).get("month").intValue();
			//将期数转成汉字
			String index=NumberToCN.toChinese(String.valueOf(month));
			//拼接还款期数
			output_html+="<tr><td>第"+index+"期</td>";
			
			String refundDate = getRefundDate(month, returnDate, dateEndCH);
			//拼接还款日期
			output_html+="<td>"+refundDate+"</td>";
			
			long ben = 0;//本金
			long xi = 0;//利息
			
			ben = licaiMap.get(i).get("ben");
			xi = licaiMap.get(i).get("xi");
			
			//拼接本金、利息
			output_html += "<td>"+Number.doubleToStr(ben)+"</td><td>"+Number.doubleToStr(xi)+"</td>";
			
			long serviceCharge = 0; //服务费
			float fwfll = 0;//服务费利率
			fwfll = (float)(rateBySynthesize-rateByYear/12.0)/10/10/10/10;
			serviceCharge = (long) (fwfll*loanAmount*100);
			
			long total=0;//总计费用
			total = ben+xi+serviceCharge;
			
			//拼接服务费、总计费用
			output_html +="<td>"+Number.doubleToStr(serviceCharge)+"</td><td>"+Number.doubleToStr(total)+"</td></tr>";
		}
		
		output_html +="</table>";
		return output_html;
	}
	
	/*
	 * 每月还款表车贷
	 */
	public String refundDetailMonthTable(long GPSManagerMoney,long serviceMoneyByShopMonth,long financingByMonth,long loanAmount, int limit,long rateByYear, String refundType, String loanDate){
		//年利率
		//long rate = rateByMonth*12;

		//拼接表格 
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		//计算还款
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		long dengebenxi = licai.dengebenxi();
		output_html += "<tr><td>出借人本息</td><td>" + Number.longToString(dengebenxi) +"</td></tr>";
		output_html +="<tr><td>线上平台融资服务费</td><td>" + Number.longToString(financingByMonth) + "</td></tr>";
		output_html +="<tr><td>线下平台商务服务费</td><td>" + Number.longToString(serviceMoneyByShopMonth) + "</td></tr>";
		output_html +="<tr><td>GPS管理费</td><td>" + Number.longToString(GPSManagerMoney) + "</td></tr>";
		long sum = dengebenxi+financingByMonth+serviceMoneyByShopMonth+ GPSManagerMoney;
		output_html +="<tr><td>合计</td><td>" +Number.longToString(sum) + "</td></tr>";
		output_html +="</table>";
		return output_html;
	}
	
	/*
	 * 每月还款表信贷
	 */
		private String refundDetailMonthTable1(long serviceMoneyByShopMonth, long financingByMonth, Long loanAmount,
				Integer limit, long rateByYear, String refundType, String loanDate) {
			//拼接表格 
			String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
			//计算还款
			LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
			long dengebenxi = licai.dengebenxi();
			output_html += "<tr><td>出借人本息</td><td>" + Number.longToString(dengebenxi) +"</td></tr>";
			output_html +="<tr><td>线上平台融资服务费</td><td>" + Number.longToString(financingByMonth) + "</td></tr>";
			output_html +="<tr><td>线下平台商务服务费</td><td>" + Number.longToString(serviceMoneyByShopMonth) + "</td></tr>";
			long sum = dengebenxi+financingByMonth+serviceMoneyByShopMonth;
			output_html +="<tr><td>合计</td><td>" +Number.longToString(sum) + "</td></tr>";
			output_html +="</table>";
			return output_html;
		}
	
	/**
	 * 借款合同 ：还款计划明细表
	 * @param loanAmount	贷款金额
	 * @param limit		期数
	 * @param rate		年利率
	 * @param rateByYear		年利率
	 * @param refundType	还款方式
	 * @param loanDate		借款日期
	 * @return
	 */
	public String getRefundPlanTable(long loanAmount, int limit, long rateByYear, String refundType, String loanDate){
		//年利率
		//long rate = rateByMonth*12;
		//借款日期前一天
		String returnDate = DateUtil.delDay(loanDate, 1);
		//获取截止日期
		String dateEndCH = getDateEndCH(limit, loanDate, returnDate);
		
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html += "<tr><td>序号</td><td>还款时间</td><td>本金</td><td>利率</td><td>利息</td><td>合计</td></tr>";
		
		//计算还款
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
		if("等本等息".equals(refundType)){
			licaiMap = licai.getDengEList();
		}else{
			licaiMap = licai.getDengXiList();
		}
		for (int i = 0; i < licaiMap.size(); i++) {
			int month = licaiMap.get(i).get("month").intValue();
			String index=NumberToCN.toChinese(String.valueOf(month));
			//拼接还款期数
			output_html+="<tr><td>第"+index+"期</td>";
			
			String refundDate = getRefundDate(month, returnDate, dateEndCH);
			//拼接还款日期
			output_html+="<td>"+refundDate+"</td>";
			
			long ben = 0;//本金
			long xi = 0;//利息
			long total=0;//合计
			
			ben = licaiMap.get(i).get("ben");
			xi = licaiMap.get(i).get("xi");
			total = ben+xi;
			//拼接本金、利率、利息、合计
			output_html += "<td>"+Number.doubleToStr(ben)+"元</td><td>"+(float)rateByYear/12.0/100+"%</td><td>"+Number.doubleToStr(xi)+"元</td><td>"+Number.doubleToStr(total)+"元</td></tr>";
		}
		output_html +="</table>";
		return output_html;
	}
	

	/**
	 * 借款合同：提前结清违约金支付标准表
	 * @param loanAmount  贷款金额
	 * @param rate	年利率
	 * @param rateByYear 年利率
	 * @param limit	期数
	 * @param refundType  还款方式
	 * @return
	 */
	public String getDeditPayStandardTable(long loanAmount, long rateByYear, int limit, String refundType){
		//年利率
		//long rate = rateByMonth*12;
		String output_html="<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html += "<tr><td>提前期数</td><td>违约金金额</td></tr>";
		
		//计算还款
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
		if("等本等息".equals(refundType)){
			licaiMap = licai.getDengEList();
		}else{
			licaiMap = licai.getDengXiList();
		}
		
		for (int i = 0; i < licaiMap.size(); i++) {
			if(i>=9){
				break;
			}
			int month=licaiMap.get(i).get("month").intValue();
			
			double a=0;//系数
			switch (i) {
			case 0:
				a=0.9;
				break;
			case 1:
				a=1.7;
				break;
			case 2:
				a=2.4;
				break;
			case 3:
				a=3.0;
				break;
			case 4:
				a=3.5;
				break;
			case 5:
				a=3.9;
				break;
			case 6:
				a=4.2;
				break;
			case 7:
				a=4.4;
				break;
			default:
				a=4.5;
				break;
			}
			//拼接违约金
			if(month<9){
				output_html += "<tr><td>"+month+"</td><td>违约金=借款本金*"+(float)rateByYear/12.0/100+"%*"+a+"</td></tr>";
			}else{
				output_html += "<tr><td>"+month+"期及以上</td><td>违约金=借款本金*"+(float)rateByYear/12.0/100+"%*"+a+"</td></tr>";
			}
		}
		output_html+="</table>";
		return output_html;
	}
	
	
	/**
	 * 融资服务协议：服务费支付时间及金额表
	 * @param loanAmount 贷款金额
	 * @param rate 年利率
	 * @param limit 期数
	 * @param rateBySynthesize 综合利率
	 * @param rateByYear 年利率
	 * @param refundType 还款方式
	 * @param loanDate 借款日期
	 * @return
	 */
	public String getServiceChargeTable(long loanAmount, int limit, long rateBySynthesize, long rateByYear, String refundType, String loanDate){
		//年利率
		//long rate = rateByMonth*12;
		
		float rzfwffl=(float)(rateBySynthesize-rateByYear/12.0)/10/10/10/10;//服务费率
		
		//借款日期前一天
		String returnDate = DateUtil.delDay(loanDate, 1);
		//获取截止日期	
		String dateEndCH=getDateEndCH(limit, loanDate,returnDate);
		
		long serviceCharge=0;//服务费
		serviceCharge=(long) (rzfwffl*loanAmount*100);
		
		String output_html = "<table style='width:100%;text-align: center;' cellpadding='2' cellspacing='0' border='1' bordercolor='#000000'>";
		output_html += "<tr><td>序号</td><td>支付时间</td><td>融资服务费费率</td><td>融资服务费金额</td></tr>";
		
		//计算还款
		LiCai licai = new LiCai(loanAmount*100, rateByYear, limit);
		List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
		if("等本等息".equals(refundType)){
			licaiMap = licai.getDengEList();
		}else{
			licaiMap = licai.getDengXiList();
		}
		for (int i = 0; i < licaiMap.size(); i++) {
			int month = licaiMap.get(i).get("month").intValue();
			String index = NumberToCN.toChinese(String.valueOf(month));
			//拼接序号
			output_html += "<tr><td>第"+index+"期</td>";
			
			String refundDate = getRefundDate(month, returnDate, dateEndCH);
			//拼接支付时间
			output_html += "<td>"+refundDate+"</td>";
			//拼接服务费率、服务费
			output_html += "<td>"+rzfwffl*100+"%</td><td>"+Number.doubleToStr(serviceCharge)+"元</td></tr>";
		}
		output_html += "</table>";
		return output_html;
	}
	/**
	 * 获取截止日期
	 * @param limit 期数
	 * @param loanDate	借款日期
	 * @return
	 */
	public String getDateEndCH(int limit, String loanDate,String returnDate){
		
		//转换日期格式
		/*String dateStartCH = DateUtil.getStrFromDate(
				DateUtil.getDateFromString(loanDate, "yyyyMMdd"), "yyyy年MM月dd日");*/
		String dateEndCH = DateUtil.getStrFromDate(
				DateUtil.getDateFromString(CommonUtil.anyRepaymentDate4string(returnDate, limit), "yyyyMMdd"), "yyyy年MM月dd日");

		//兼容2月
		if(DateUtil.getNowDate().endsWith("01")){
			
			String date4string = CommonUtil.anyRepaymentDate4string(returnDate, limit);
			String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
					Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
			dateEndCH = dateEndCH.substring(0,dateEndCH.length()-3) + end4month + "日";
		}
		return dateEndCH;
	}
	/**
	 * 获取还款日期
	 * @param month 第几期
	 * @param returnDate  借款前一天
	 * @param dateEndCH	 截止日期
	 * @return
	 */
	public String getRefundDate(int month, String returnDate, String dateEndCH){
		String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
		String date4Ch = DateUtil.getStrFromDate(
				DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy年MM月dd日");
		
		//兼容2月
		if(DateUtil.getNowDate().endsWith("01")){
			String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
					Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
			date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
		}
		return date4Ch;
	}
}
