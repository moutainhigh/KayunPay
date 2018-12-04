package com.dutiantech.controller.admin;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dutiantech.Message;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.interceptor.AuthInterceptor;
import com.dutiantech.interceptor.PkMsgInterceptor;
import com.dutiantech.model.ContractTemplate;
import com.dutiantech.model.TransferUser;
import com.dutiantech.service.ContractTemplateService;
import com.dutiantech.service.TransferUserService;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.NumberToCN;
import com.dutiantech.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

public class ContractTemplateController extends BaseController {

	private ContractTemplateService templateService = getService(ContractTemplateService.class);
	private TransferUserService transferUserService = getService(TransferUserService.class);
	
	static HashMap<String, Integer> wordMap = new HashMap<String,Integer>();//空值下滑线长度
	static{
		wordMap.put("[transferUserName]",10);//债权人姓名
		wordMap.put("[transferUserCardId]",20);//债权人身份证号
		wordMap.put("[transferUserAddress]",30);//债权人住址
		wordMap.put("[transferUserMobile]",15);//债权人联系电话
		wordMap.put("[transferUserSex]",5);//债权人性别
		wordMap.put("[transferUserbankNo]",20);//债权人银行卡卡号
		wordMap.put("[TUbankUserName]",10);//债权人银行卡户名
		wordMap.put("[TUbankName]",20);//债权人开户行全称
		wordMap.put("[loanUserName]",10);//借款人姓名
		wordMap.put("[loanUserCardId]",20);//借款人身份证号
		wordMap.put("[loanUserAddress]",30);//借款人家庭住址
		wordMap.put("[loanUserMobile]",15);//借款人手机号
		wordMap.put("[serveMobile]",15);//客服专号
		wordMap.put("[loanAmount]",10);//贷款金额小写
		wordMap.put("[loanAmountCN]",20);//贷款金额大写
		wordMap.put("[moneyLoan]",10);//现金支付金额
		wordMap.put("[bankLoan]",10);//银行转账
		wordMap.put("[rateByMonth]",10);//借款利率
		wordMap.put("[rateBySynthesize]",10);//综合利率
		wordMap.put("[loanUsed]",15);//借款用途
		wordMap.put("[limit]",10);//借款期限
		wordMap.put("[ensureCompanyName]",10);//担保人姓名(公司)
		wordMap.put("[companyCode]",20);//担保人公司组织机构代码(公司)
		wordMap.put("[businessPlace]",30);//担保人经营场所(公司)
		wordMap.put("[companyPhone]",15);//担保人联系电话(公司)
		wordMap.put("[loanBankNo]",20);//银行卡卡号
		wordMap.put("[loanBankName]",20);//银行全称
		wordMap.put("[addYear]",5);//签订年份
		wordMap.put("[addMonth]",5);//签订月份
		wordMap.put("[addDay]",5);//签订日期
		wordMap.put("[repayYear]",5);//还款年份
		wordMap.put("[repayMonth]",5);//还款月份
		wordMap.put("[repayDay]",5);//还款日期
		wordMap.put("[repayYearStart]",5);//还款开始年
		wordMap.put("[repayMonthStart]",5);//还款开始月
		wordMap.put("[repayDayStart]",5);//还款开始日
		wordMap.put("[breakRuleYear]",5);//消除违章年
		wordMap.put("[breakRuleMonth]",5);//消除违章月
		wordMap.put("[breakRuleDay]",5);//消除违章日
		wordMap.put("[cDay]",5);//还款日
		wordMap.put("[templateType]",10);//合同类型
		wordMap.put("[refundType]",10);//还款方式
		wordMap.put("[carManager]",5);//车辆管理（GPS、质押）
		wordMap.put("[companyAddress]",30);//收车费用标准
		wordMap.put("[contractsNum]",15);//合同编号
		wordMap.put("[addressSign]",30);//合同签署地
		wordMap.put("[court]",30);//合同签署地法院
		wordMap.put("[carName]",10);//汽车名
		wordMap.put("[carBrand]",10);//车辆品牌
		wordMap.put("[carNumber]",10);//车辆牌号
		wordMap.put("[engineNumber]",15);//发动机号
		wordMap.put("[carframe]",15);//车架号码
		wordMap.put("[ensureName]",10);//担保人姓名
		wordMap.put("[ensureCardId]",20);//担保人身份证号
		wordMap.put("[ensureMobile]",15);//担保人联系电话
		wordMap.put("[ensureAddress]",30);//担保人住所
		wordMap.put("[entrustPersonName]",10);//首次款卡姓名
		wordMap.put("[legalUserCardId]", 30);//法人身份证号
		wordMap.put("[offlineShop]",25);//线下门店
		wordMap.put("[onlineName]",25);//线上平台
		wordMap.put("[shopAddress]",25);//线下地址
		wordMap.put("[shopPhone]",25);//线下负责人电话
		wordMap.put("[onlineAddress]",25);//线上平台地址
		wordMap.put("[onlinePhone]",25);//线上平台电话
		wordMap.put("[carMoney]",10);//车辆违章押金
		wordMap.put("[owner]",10);//车主姓名
		wordMap.put("[gatheringName]",10);//收款人姓名
		wordMap.put("[gatheringBankName]",10);//收款人账户名称
		wordMap.put("[gatheringBankNumber]",30);//收款人卡号
		wordMap.put("[materialMoney]",10);//人工材料费
		wordMap.put("[GPSManagerMoney]",10);//GPS管理费
		wordMap.put("[rateByCompany]",10);//平台成本率
		
		
	
	}
	
	//查询合同模板列表
	@ActionKey("/queryTemplateList")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,PkMsgInterceptor.class})
	public Message queryTemplateList(){
		Integer pageNumber = getParaToInt("pageNumber",1);
		Integer pageSize = getParaToInt("pageSize",10);
		String allkey = getPara("allkey","");
		Page<ContractTemplate> pageTemplate = templateService.findAllByPage(pageNumber, pageSize, allkey);
		if(pageTemplate!=null){
		if(pageNumber > pageTemplate.getTotalPage()&&pageTemplate.getTotalPage()>0){
			pageNumber = pageTemplate.getTotalPage();
			pageTemplate = templateService.findAllByPage(pageNumber, pageSize, allkey);
		}
		}
		
		return succ("操作完成",pageTemplate);
	}
	
	//查询合同模板详情
	@ActionKey("/findTemplateByCode")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message findTemplateByCode(){
		String templateCode = getPara("templateCode");
		ContractTemplate contractTemplate = templateService.findById(templateCode);
		String content = contractTemplate.get("content");
		String regex = "(\\[[^\\]]*\\])";//匹配中括号间内容，含中括号
		Pattern pattern = Pattern.compile (regex);  
		Matcher matcher = pattern.matcher (content);  
		HashSet<String> hashSet = new HashSet<String>();
		while (matcher.find ()){  
			 hashSet.add(matcher.group ());
		 }
		 for (String set : hashSet) {
			System.out.print(set+"\t");
		}
		 System.out.println();
		return succ("查询成功",contractTemplate);
	}
	
	//修改合同模板
	@ActionKey("/modTemplate")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message modTemplate() throws ParseException{

		ContractTemplate contractTemplate = getModel(ContractTemplate.class,"contractTemplate");
		
		String updateDateTime = DateUtil.getDateStrFromDateTime(new Date());//获取更新时间
		//String addDateTime = contractTemplate.getStr("addDateTime").replace("-","").replace(":","").replace(" ","");
		//时间格式转化
		 String addDateTime =contractTemplate.getStr("addDateTime");//获取添加时间
		 SimpleDateFormat sdf =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
		 Date date = sdf.parse(addDateTime);
		 addDateTime = DateUtil.getDateStrFromDateTime(date);
		contractTemplate.put("updateDateTime",updateDateTime);
		contractTemplate.put("addDateTime",addDateTime);
		boolean result = templateService.changeByCode(contractTemplate);
		if(result) return succ("修改成功",result);
		return error("no","修改失败",null);
	}
	
	//删除模板
	@ActionKey("/deleteContractTemplate")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message deleteTemplate(){
		String templateCode = getPara("templateCode");
		boolean deleteByCode = templateService.deleteByCode(templateCode);
		if(deleteByCode){
			return succ("删除成功",true);
		}else{
			return error("no","删除失败",null);
		}
	}
	
	//新增模板
	@ActionKey("/newTemplate")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message newTemplate(){
		ContractTemplate contractTemplate = getModel(ContractTemplate.class,"contractTemplate");
		
		boolean saveTemplate = templateService.saveTemplate(contractTemplate);
		if(saveTemplate) return succ("添加成功！",true);
		return error("no","添加失败！",null);
	}
	
	//禁用或解禁合同模板
	@ActionKey("/disabledTemplate")
	@AuthNum(value=999)
	@Before({AuthInterceptor.class,Tx.class,PkMsgInterceptor.class})
	public Message disabledTemplate(){
		
		
		String templateCode = getPara("unUsedTemplateCode");//拦截器验证不可为空
		String status = getPara("TemplateStatus");//拦截器验证不可为空
		String[] tem = templateCode.split(",");//传过来的字符串截取成Code

			if(templateService.updateByCode(tem,status)){
				return succ("ok",true);
			};


		return error("no","操作失败！",null);
	}
	
	/**
	 * 根据合同类型查询需要生成的合同模板
	 * @param contractType
	 * @param loanTypeForContract 0:车贷  1：信贷
	 * @return
	 */
	public List<ContractTemplate> queryByType(String loanTypeForContract,String contractType,String loanTypeForUser) {
		List<ContractTemplate> result = new ArrayList<ContractTemplate>();
		Map<String, ContractTemplate> mapContractTemplate = templateService.findAll();
		if("0".equals(loanTypeForContract)){
			if("个人用户".equals(loanTypeForUser)){
				if("等额本息".equals(contractType)){
					result.add(mapContractTemplate.get("P2P网络融资委托书"));
					result.add(mapContractTemplate.get("个人-车辆出借协议书"));
					result.add(mapContractTemplate.get("个人-车辆出借协议书"));
					result.add(mapContractTemplate.get("担保承诺书"));
					result.add(mapContractTemplate.get("个人-车辆质押协议书"));
					result.add(mapContractTemplate.get("个人-车辆质押协议书"));
					result.add(mapContractTemplate.get("借据-个人版"));
					result.add(mapContractTemplate.get("借款承诺书"));
					result.add(mapContractTemplate.get("借款提前告知书"));
					result.add(mapContractTemplate.get("借款提前告知书"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("车辆处置授权委托书"));
					result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
					result.add(mapContractTemplate.get("线下门店商务服务协议"));
					result.add(mapContractTemplate.get("线下门店商务服务协议"));
					result.add(mapContractTemplate.get("消除车辆违章承诺书"));
					result.add(mapContractTemplate.get("客户经理承诺书"));
				}
			}else if("企业用户".equals(loanTypeForUser)){
				result.add(mapContractTemplate.get("P2P网络融资委托书"));
				result.add(mapContractTemplate.get("担保承诺书"));
				result.add(mapContractTemplate.get("公司-车辆出借协议书"));
				result.add(mapContractTemplate.get("公司-车辆出借协议书"));
				result.add(mapContractTemplate.get("公司-车辆质押协议书"));
				result.add(mapContractTemplate.get("公司-车辆质押协议书"));
				result.add(mapContractTemplate.get("借据-公司版"));
				result.add(mapContractTemplate.get("借款承诺书"));
				result.add(mapContractTemplate.get("借款提前告知书"));
				result.add(mapContractTemplate.get("借款提前告知书"));
				result.add(mapContractTemplate.get("客户还款明细表"));
				result.add(mapContractTemplate.get("客户还款明细表"));
				result.add(mapContractTemplate.get("车辆处置授权委托书"));
				result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
				result.add(mapContractTemplate.get("线下门店商务服务协议"));
				result.add(mapContractTemplate.get("线下门店商务服务协议"));
				result.add(mapContractTemplate.get("消除车辆违章承诺书"));
				result.add(mapContractTemplate.get("客户经理承诺书"));
			}
		}else if("1".equals(loanTypeForContract)){
            if("个人用户".equals(loanTypeForUser)){
				if("等额本息".equals(contractType)){
					result.add(mapContractTemplate.get("P2P网络融资委托书"));
					result.add(mapContractTemplate.get("担保承诺书"));
					result.add(mapContractTemplate.get("借据-个人版"));
					result.add(mapContractTemplate.get("信贷借款提前告知书"));
					result.add(mapContractTemplate.get("信贷借款提前告知书"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
					result.add(mapContractTemplate.get("信贷线下门店商务服务协议"));
					result.add(mapContractTemplate.get("信贷线下门店商务服务协议"));
					result.add(mapContractTemplate.get("客户经理承诺书"));
				}
			}else if("企业用户".equals(loanTypeForUser)){
                if("等额本息".equals(contractType)){
                	result.add(mapContractTemplate.get("P2P网络融资委托书"));
					result.add(mapContractTemplate.get("担保承诺书"));
					result.add(mapContractTemplate.get("借据-公司版"));
					result.add(mapContractTemplate.get("信贷借款提前告知书"));
					result.add(mapContractTemplate.get("信贷借款提前告知书"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("客户还款明细表"));
					result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
					result.add(mapContractTemplate.get("信贷线下门店商务服务协议"));
					result.add(mapContractTemplate.get("信贷线下门店商务服务协议"));
					result.add(mapContractTemplate.get("客户经理承诺书"));
				}
			}
		}
		
		/*switch (loanTypeForUser) {
		case "GPS等额本息":	// GPS等 额本息
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("车辆质押与出借协议书"));
			result.add(mapContractTemplate.get("担保书（个人版）"));
			result.add(mapContractTemplate.get("担保书（公司版）"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("借据收据"));
			result.add(mapContractTemplate.get("借款承诺书"));
			result.add(mapContractTemplate.get("借款合同"));
			result.add(mapContractTemplate.get("融资服务协议"));
			result.add(mapContractTemplate.get("借条与委托支付单"));
			result.add(mapContractTemplate.get("融资服务协议书"));
			break;
		case "个人用户":	// 
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("个人-车辆出借协议书"));
			result.add(mapContractTemplate.get("个人-车辆出借协议书"));
			result.add(mapContractTemplate.get("担保承诺书"));
			result.add(mapContractTemplate.get("个人-车辆质押协议书"));
			result.add(mapContractTemplate.get("个人-车辆质押协议书"));
			result.add(mapContractTemplate.get("借据-个人版"));
			result.add(mapContractTemplate.get("借款承诺书"));
			result.add(mapContractTemplate.get("借款提前告知书"));
			result.add(mapContractTemplate.get("客户还款明细表"));
			result.add(mapContractTemplate.get("客户还款明细表"));
			result.add(mapContractTemplate.get("车辆处置授权委托书"));
			result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
			result.add(mapContractTemplate.get("线下门店商务服务协议"));
			result.add(mapContractTemplate.get("线下门店商务服务协议"));
			result.add(mapContractTemplate.get("消除车辆违章承诺书"));
			result.add(mapContractTemplate.get("客户经理承诺书"));
			break;
		case "GPS先息后本":	// GPS先息后本
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("车辆质押与出借协议书"));
			result.add(mapContractTemplate.get("担保书（个人版）"));
			result.add(mapContractTemplate.get("担保书（公司版）"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("借据收据"));
			result.add(mapContractTemplate.get("借款承诺书"));
			result.add(mapContractTemplate.get("借款合同"));
			result.add(mapContractTemplate.get("融资服务协议"));
			result.add(mapContractTemplate.get("借条与委托支付单"));
			result.add(mapContractTemplate.get("融资服务协议书"));
			break;
		case "质押等额本息":	// 质押等额本息
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("担保书（个人版）"));
			result.add(mapContractTemplate.get("担保书（公司版）"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("借据收据"));
			result.add(mapContractTemplate.get("质押借款合同"));
			result.add(mapContractTemplate.get("融资服务协议"));
			result.add(mapContractTemplate.get("借条与委托支付单"));
			result.add(mapContractTemplate.get("融资服务协议书"));
			break;
		case "企业用户":	// 
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("担保承诺书"));
			result.add(mapContractTemplate.get("公司-车辆出借协议书"));
			result.add(mapContractTemplate.get("公司-车辆出借协议书"));
			result.add(mapContractTemplate.get("公司-车辆质押协议书"));
			result.add(mapContractTemplate.get("公司-车辆质押协议书"));
			result.add(mapContractTemplate.get("借据-公司版"));
			result.add(mapContractTemplate.get("借款承诺书"));
			result.add(mapContractTemplate.get("借款提前告知书"));
			result.add(mapContractTemplate.get("客户还款明细表"));
			result.add(mapContractTemplate.get("客户还款明细表"));
			result.add(mapContractTemplate.get("车辆处置授权委托书"));
			result.add(mapContractTemplate.get("P2P网络融资服务承诺书"));
			result.add(mapContractTemplate.get("线下门店商务服务协议"));
			result.add(mapContractTemplate.get("线下门店商务服务协议"));
			result.add(mapContractTemplate.get("消除车辆违章承诺书"));
			result.add(mapContractTemplate.get("客户经理承诺书"));
			break;
		case "质押先息后本":	// 质押先息后本
			result.add(mapContractTemplate.get("P2P网络融资委托书"));
			result.add(mapContractTemplate.get("担保书（个人版）"));
			result.add(mapContractTemplate.get("担保书（公司版）"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("还款明细表"));
			result.add(mapContractTemplate.get("借据收据"));
			result.add(mapContractTemplate.get("质押借款合同"));
			result.add(mapContractTemplate.get("融资服务协议"));
			result.add(mapContractTemplate.get("借条与委托支付单"));
			result.add(mapContractTemplate.get("融资服务协议书"));
			break;
		default:
			break;
		}*/
		return result;
	}
	
	/**
	 * 空值长度（带下划线）
	 * @param word
	 * @return
	 */
	public String wordValue(String word){
		switch (wordMap.get(word)) {
		case 5:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		case 10:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		case 15:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		case 20:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		case 25:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		case 30:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		default:
			return "<u>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</u>";
		}
	}
	
	/**
	 * 替换合同模板内容
	 * @param template	模板
	 * @param map	替换键值
	 * @return
	 */
	public String replaceTemplateContent(String template, Map<String, String> map) {
		for (Map.Entry<String, String> entry : map.entrySet()) {
			if(entry.getValue()!=null && !"".equals(entry.getValue().trim())){
				template=template.replace(entry.getKey(), entry.getValue());
			}else{
				template=template.replace(entry.getKey(), wordValue(entry.getKey()));
			}
		}
		return template;
	}
	
	@ActionKey("/queryContent")
	public String queryContent(){
		
		String businessPlace = getPara("");//经营场所
		String ensureCompany = getPara("");//保证人（公司版）
		String companyPhone = getPara("");//保证人电话（公司版）
		String companyCode = getPara("");//公司组织机构代码(公司版担保书)
		String ensureAddress = getPara("");//保证人地址（个人版）
		String ensureName = getPara("");//保证人姓名
		String ensureNumber = getPara("");//保证人电话（个人）
		String ensureCardId = getPara("");//保证人ID(个人)
		
		String carNumber = getPara("");//车牌号
		String engineNumber = getPara("");//发动机牌
		String carBrand = getPara("");//车的品牌
		String carframe = getPara("");//车架
		String carManager = getPara("");//车辆管理方式
		
		String lendMonth = getPara("");//借款期限
		String borrowerAddress = getPara("");//借款人地址
		String moneyLoan = getPara("");//现金支付金额
		String zRate = getPara("");//综合利率
		String borrowerName = getPara("");//借款人姓名
		String lowerRMB = getPara("");//借款金额（小写）
		String borrowerMobile = getPara("");//借款人电话
		String bankLoan = getPara("");//银行卡支付金额
		String useStatus = getPara("");//资金借贷用途
		String monthRate = getPara("");//月利率
		String borrowerCardId = getPara("");//借款人身份证
		String repaymentStatus = getPara("");//还款方式
		String transferUserNo = getPara("transferUserNo");//债权人标码
		
		
		
		TransferUser transferUser = transferUserService.findById(transferUserNo);
		String repayCount = transferUser.getStr("bankNo");//还款账户
		String lenderMobile = transferUser.getStr("mobile");//出借人电话
		String lenderName = transferUser.getStr("name");//出借人姓名
		String bankName = transferUser.getStr("bankName");//开户行
		String contractCode;//合同编号
		String court;//当地法院
		String addressSign = transferUser.getStr("companyAddress");//合同签署地
		String lenderAddress = transferUser.getStr("address");//出借人地址
		String lenderCardId = transferUser.getStr("cardId");//出借人身份证
		String companyAddress = transferUser.getStr("companyAddress");//公司地址
		String sex = transferUser.getStr("sex");//出借人性别
		
		String bigRMB = NumberToCN.number2CNMontrayUnit(new BigDecimal(Double.parseDouble(lowerRMB)));//将小写金额转成大写
		Calendar cal = Calendar.getInstance();
        int addYear = cal.get(Calendar.YEAR);//获取年份
        int addMonth=cal.get(Calendar.MONTH);//获取月份
        int addDay=cal.get(Calendar.DATE);//获取日
		
		
		
		
		
		
		
		
		return null;
	}
	
}