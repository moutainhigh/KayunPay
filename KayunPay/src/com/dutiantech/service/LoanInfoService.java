package com.dutiantech.service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanOverdue;
import com.dutiantech.model.LoanInfo.productType;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.SettlementEarly;
import com.dutiantech.model.Tickets;
import com.dutiantech.model.User;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.LiCai;
import com.dutiantech.util.StringUtil;
import com.dutiantech.util.SysEnum;
import com.dutiantech.util.SysEnum.loanOverdueType;
import com.dutiantech.util.UIDUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

public class LoanInfoService extends BaseService {
	
	public List<LoanInfo> findJTHK(){
		String backDate = DateUtil.getNowDate();
		return LoanInfo.loanInfoDao.find("select * from t_loan_info where backDate = ? and loanState='N' order by uid desc,releaseDate desc,loanBalance desc ",backDate);
	}
	
	/**
	 * 根据结算日期查询标 WJW
	 * @param date 日期
	 * @return
	 */
	public List<LoanInfo> findJTHK(String date){
		return LoanInfo.loanInfoDao.find("select * from t_loan_info where backDate = ? and loanState='N' and productType='E' order by uid desc,releaseDate desc,loanBalance desc ",date);
	}
	
	/**
	 * 根据结算日期查询标 WJW
	 * @param date 日期
	 * @param nowDate 屏蔽结算日期
	 * @return
	 */
	public List<LoanInfo> findJTHK(String date,String nowDate){
		return LoanInfo.loanInfoDao.find("select * from t_loan_info where backDate = ? and loanState='N' and clearDate!=? order by uid desc,releaseDate desc,loanBalance desc ",date,nowDate);
	}
	
	/**
	 * 根据标编号查询标列表
	 * @param loanNo
	 * @return
	 */
	public List<LoanInfo> findByLoanNo(String loanNo){
		return LoanInfo.loanInfoDao.find("select * from t_loan_info where loanNo = ? ",loanNo);
	}
	
	/**
	 * 根据id查询一个标书明细
	 * @param loanCode	标书编码
	 * @return
	 */
	public LoanInfo findById(String loanCode){
		return LoanInfo.loanInfoDao.findById(loanCode);
	}
	
	public String findFieldById(String loanCode, String fieldName){
		return LoanInfo.loanInfoDao.findByIdLoadColumns(loanCode, fieldName).getStr(fieldName);
	}
	
	/**
	 * 根据id查询一个贷款标的状态
	 * @param loanCode	标书编码
	 * @return
	 */
	public String findStateById(String loanCode){
		return LoanInfo.loanInfoDao.findByIdLoadColumns(loanCode, "loanState").getStr("loanState");
	}
	
	/**
	 * 创建标书基本信息,(等待材料)
	 * 
	 * @param para Map集合，包含如下字段<br><br>
	 * loanTitle	标书标题<br>
	 * loanDesc		标书其他描述<br>
	 * loanType		标书贷款类型<br>
	 * loanTypeDesc	标书贷款类型描述<br>
	 * loanAmount	标书贷款金额<br>
	 * loanArea		标书贷款地区<br>
	 * loanTimeLimit标书还款期限<br>
	 * refundType	还款方式<br>
	 * rateByYear	年利率<br>
	 * loanUsedType	借款用途<br>
	 * userCode		借款人用户userCode<br>
	 * userName		借款人真实姓名<br>
	 * userCardId	借款人身份证号<br>
	 * opCode		操作员userCode<br>
	 * opName		操作员昵称<br>
	 * @return
	 */
	public boolean createBasicLoan(Map<String,Object> para){
		LoanInfo loan = new LoanInfo();
		loan._setAttrs(para);
		
		Integer amount = (Integer) para.get("loanAmount");
		String nowDate = DateUtil.getNowDate();
		String nowTime = DateUtil.getNowTime();
		
//		loan.set("loanNo", "");
//		loan.set("loanIndexByUser", "");
		loan.set("reciedCount", 0);
		loan.set("loadByDay", 0);
		loan.set("hasInvedByTrips", 0);
		loan.set("isInterest", 0);
		loan.set("isAutoLoan", 0);
		loan.set("hasCaptcha", 0);
		loan.set("createDate", nowDate);
		loan.set("createTime", nowTime);
		loan.set("updateDate", nowDate);
		loan.set("updateTime", nowTime);
		loan.set("loanBalance", amount);
		loan.set("invedTripFees", 0);
		loan.set("serviceFees", 0);
		loan.set("managerRate", 0);
		loan.set("riskRate", 0);
		loan.set("loanState", "I");
		loan.set("releaseDate", "");
		loan.set("releaseTime", "");
		loan.set("rewardRateByYear", 0);
		loan.set("loanTotal", 0);
		loan.set("loanCount", 0);
		loan.set("minLoanAmount", 0);
		loan.set("maxLoanAmount", 0);
		loan.set("benefits4new", 0);
		loan.set("maxLoanCount", 0);
		loan.set("loanDesc", "");
		loan.set("loanBaseDesc", "");
		loan.set("loanUsedDesc", "");
		loan.set("loanerDataDesc", "");
		loan.set("loanInvDesc", "");
		loan.set("backDate", "");
		loan.set("effectDate", "");
		loan.set("effectTime", "");
		return loan.save();
	}
	
	/**
	 * 标书-补全材料(进入待审核)
	 * @param para Map集合，包含以下字段<br><br>
	 * hasInvedByTrips		是否实地考察<br>
	 * isInterest			是否本息保障<br>
	 * isAutoLoan			是否自动放款<br>
	 * hasCaptcha			是否需要验证码<br>
	 * invedTripFees		实地考察费用<br>
	 * serviceFees			服务费<br>
	 * managerRate			管理费率<br>
	 * riskRate				风险储备金费率<br>
	 * rewardRateByYear		奖励年利率<br>
	 * minLoanAmount		最小投标金额<br>
	 * maxLoanAmount		最大投标金额<br>
	 * benefits4new			新人专享福利<br>
	 * maxLoanCount			最大投标次数<br>
	 * loanBaseDesc			基本说明<br>
	 * loanUsedDesc			借款用途描述<br>
	 * loanerDataDesc		借款人提供的资料描述<br>
	 * loanInvDesc			借款人被考察描述<br>
	 * @return
	 */
	public boolean subLoanInfo(String loanCode, Map<String,Object> para){
		LoanInfo loan = LoanInfo.loanInfoDao.findById(loanCode);
		String nowDate = DateUtil.getNowDate();
		String nowTime = DateUtil.getNowTime();
		loan.set("loanState","G");
		loan.set("updateDate", nowDate);
		loan.set("updateTime", nowTime);
		loan._setAttrs(para);
		return loan.update();
	}
	
	/**
	 * 更改标书状态
	 * @param loanCode		标书编码
	 * @param loanState		标书状态
	 * @return
	 */
	public boolean updateLoanState(String loanCode, String loanState, String oLoanState){
		LoanInfo loan = LoanInfo.loanInfoDao.findByIdLoadColumns(loanCode, "loanCode,loanState,updateDate,updateTime");
		if(loan.get("loanState").equals(oLoanState)){
			loan.set("loanState", loanState);
			String nowDate = DateUtil.getNowDate();
			String nowTime = DateUtil.getNowTime();
			loan.set("updateDate", nowDate);
			loan.set("updateTime", nowTime);
			Db.update("update t_loan_trace set loanState = ? where loanCode = ?",loanState,loanCode);
			return loan.update();
		}
		return false;
	}
	/**
	 * 更改标书状态
	 * @param loanCode		标书编码
	 * @param loanState		标书状态
	 * @return
	 */
	public boolean updateLoanState4pubLoan(String loanCode, String loanState, String oLoanState){
		LoanInfo loan = LoanInfo.loanInfoDao.findByIdLoadColumns(loanCode, "loanCode,loanState,updateDate,updateTime");
		if(loan.get("loanState").equals(oLoanState)){
			loan.set("loanState", loanState);
			loan.set("releaseDate", "");
			loan.set("releaseTime", "");
			String nowDate = DateUtil.getNowDate();
			String nowTime = DateUtil.getNowTime();
			loan.set("updateDate", nowDate);
			loan.set("updateTime", nowTime);
			Db.update("update t_loan_trace set loanState = ? where loanCode = ?",loanState,loanCode);
			return loan.update();
		}
		return false;
	}
	
	/**
	 * 判断流转标的债权人信息是否合法
	 * @param loanCode
	 * @return
	 */
	public boolean isFlow(String loanCode){
		LoanInfo loanInfo = LoanInfo.loanInfoDao.findFirst("select loanType,creditorName,creditorCardId from t_loan_info where loanCode = ?",loanCode);
		if(loanInfo.getStr("loanType").equals(SysEnum.loanType.D.val())){
			try {
				if(loanInfo.getStr("creditorName").equals("系统") && loanInfo.getStr("creditorCardId").equals("0")){
					return false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("流转标的债权人信息异常...");
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 发标
	 * @param loanCode		标书编码
	 * @param pubDate		发标日期
	 * @param pubTime		发标时间
	 * @return
	 */
	public boolean pubLoan(String loanCode, String pubDate,String pubTime){
		LoanInfo loan = LoanInfo.loanInfoDao.findByIdLoadColumns(loanCode, "loanCode,loanState,releaseDate,releaseTime,loanAmount,benefits4new,minLoanAmount,productType");
		if(loan.get("loanState").equals(SysEnum.loanState.H.val())){
			loan.set("loanState", SysEnum.loanState.J.val());
			loan.set("releaseDate", pubDate);
			loan.set("releaseTime", pubTime);
			long loanAmount = loan.getLong("loanAmount");
			loan.set("loanBalance", loan.getLong("loanAmount"));
			// 按百分比计算自动投标可投金额, 默认80%
			String loanATAmountPercent = "0.8";
			if((loan.getStr("productType")).equals(SysEnum.productType.E.val())){	// 易分期产品自动可投比例100%
				loanATAmountPercent = "1";
			}
			long xx = CommonUtil.yunsuan(loanAmount+"", loanATAmountPercent, "cheng", 0).longValue();
			xx = xx - xx % 100;
			loan.set("loanATAmount", xx);
			loan.set("updateDate", DateUtil.getNowDate());
			loan.set("updateTime", DateUtil.getNowTime());
			loan.set("ssn", CommonUtil.genMchntSsn());  //  20170622  新增加流水记录字段    by WCF
			Db.update("delete from t_loan_trace where loanCode = ?",loanCode);
//			long amount = Long.parseLong( loan.get("loanAmount").toString() ) ;
//			Memcached.storeCounter( LOAN.BALANCE.key() + loanCode , 0 ) ;
			if(loan.getInt("benefits4new")>0){
				if(loan.getInt("minLoanAmount")<10000){
					loan.set("minLoanAmount", 10000);
				}
			}
			return loan.update();
		}
		return false;
	}
	
	/**
	 * 删除一个标书, 是否删流水？
	 * @param loanCode	标书编码
	 * @return
	 */
	public boolean deleteById(String loanCode){
		
		int x2 = Db.update("delete from t_loan_info where loanCode = ? and loanState in ('H','G','F','I','L','S')", loanCode);
		if(x2>0){
//			int x1 = Db.update("delete from t_loan_trace where loanCode = ?", loanCode);
//			if(x1>0){
//				
//			}
			return true;
		}
		return false;
	}
	
	/**
	 * 分页查询标书(前台)
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @param minLimit		最小贷款期限
	 * @param maxLimit		最大贷款期限
	 * @return
	 */
	public Page<LoanInfo> findByPortal(Integer pageNumber, Integer pageSize,String loanState,
			String type,String productType,String minLimit,String maxLimit){
		long time = new Date().getTime() + 2*60*60*1000;
		String dateTime = DateUtil.getStrFromDate(new Date(time), "yyyyMMddHHmmss");
		String sqlSelect = "select loanCode,loanNo,loanTitle,loanType,loanAmount,loanBalance,userCode,loanState,releaseDate,releaseTime,loanTimeLimit,refundType,rateByYear,rewardRateByYear,minLoanAmount,maxLoanAmount,benefits4new,effectDate,effectTime,lastPayLoanDateTime,productType ";
		String sqlFrom = " from t_loan_info where concat(releaseDate,releaseTime) <= " + dateTime;
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by loanState,loanBalance desc,lastPayLoanDateTime desc";
		List<Object> paras = new ArrayList<Object>();
		if(StringUtil.isBlank(type) == false){
			if("1".equals(type)){
				makeExp(buff, paras, "benefits4new", ">", "0", "and");
			}else if("2".equals(type)){
				makeExp(buff, paras, "benefits4new", "=", "0", "and");
			}
		} else {
			makeExp(buff, paras, "benefits4new", ">=", "0", "and");	
		}
		
		if(!StringUtil.isBlank(productType)){
			makeExp(buff, paras, "productType", "=", productType, "and");
		}
		if(StringUtil.isBlank(minLimit) == false){
			makeExp(buff, paras, "loanTimeLimit", ">=", minLimit, "and");
		}
		if(StringUtil.isBlank(maxLimit) == false){
			makeExp(buff, paras, "loanTimeLimit", "<=", maxLimit, "and");
		}		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "loanState", loanStates,"and");
		System.out.println(sqlSelect + " "+ sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+sqlOrder);
		
		List<LoanInfo> loanInfos = LoanInfo.loanInfoDao.find(sqlSelect + " "+ sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+" and productType = 'E' and loanState='J'"+sqlOrder,paras.toArray());
		Page<LoanInfo> pageLoanInfo1 = LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+" and ((productType != 'E')or(productType = 'E' and loanState !='J')) "+sqlOrder,paras.toArray());
		List<LoanInfo> loanInfoList1 = pageLoanInfo1.getList();
		List<LoanInfo> tempList = new ArrayList<LoanInfo>();
		int size = loanInfos.size();
		int n = 0;
		if(size>5){
			n=5;
		}else{
			n=size;
		}
		if(pageNumber==1){
			for(int i = 0;i<n;i++){
				tempList.add(loanInfos.get(i));
			}
			for(int i = 0;i<pageSize-n;i++){
				if(i+1>loanInfoList1.size()){
					break;
				}
				tempList.add(loanInfoList1.get(i));
			}
		}else{
			Page<LoanInfo>  pageLoanInfo2 = LoanInfo.loanInfoDao.paginate(pageNumber-1, pageSize, sqlSelect,sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+" and ((productType != 'E')or(productType = 'E' and loanState !='J')) "+sqlOrder,paras.toArray());
			List<LoanInfo> loanInfoList2 = pageLoanInfo2.getList();
			for(int i = pageSize-n;i<pageSize;i++){
				tempList.add(loanInfoList2.get(i));
			}
			for(int i = 0;i<pageSize-n;i++){
				if(i+1>loanInfoList1.size()){
					break;
				}
				tempList.add(loanInfoList1.get(i));
			}
		}
		int totalRow = pageLoanInfo1.getTotalRow()+n;
		int totalPage = totalRow/pageSize;
		if(totalRow%pageSize>0){
			totalPage+=1;
		}
		Page<LoanInfo> pageLoanInfo = new Page<LoanInfo>(tempList, pageNumber, pageSize, totalPage, totalRow);
		return pageLoanInfo;
	}
	
	/**
	 * 分页查询标书（返利投接口） 
	 * @param pageNumber	页码
	 * @param pageSize	每页显示内容数
	 * @param loanState	标的状态
	 * @param type	标的类型
	 * @param productType	产品类型
	 * @param minLimit	标的最小期限
	 * @param maxLimit	标的最大期限
	 * @param exceptBeginDate	不包含日期开始时间
	 * @param exceptEndDate	不包含日期结束时间
	 * @return
	 */
	public Page<LoanInfo> findByPortal4Flt(Integer pageNumber, Integer pageSize,String loanState,
			String type,String productType,String minLimit,String maxLimit,String exceptBeginDate,String exceptEndDate){
		long time = new Date().getTime() + 2*60*60*1000;
		String dateTime = DateUtil.getStrFromDate(new Date(time), "yyyyMMddHHmmss");
		String sqlSelect = "select loanCode,loanNo,loanTitle,loanType,loanAmount,loanBalance,userCode,loanState,releaseDate,releaseTime,loanTimeLimit,refundType,rateByYear,rewardRateByYear,minLoanAmount,maxLoanAmount,benefits4new,effectDate,effectTime,lastPayLoanDateTime,productType ";
		String sqlFrom = " from t_loan_info where concat(releaseDate,releaseTime) <= " + dateTime +" and loanTimeLimit=18 ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by loanState,loanBalance desc,lastPayLoanDateTime desc";
		List<Object> paras = new ArrayList<Object>();
		if(StringUtil.isBlank(type) == false){
			if("1".equals(type)){
				makeExp(buff, paras, "benefits4new", ">", "0", "and");
			}else if("2".equals(type)){
				makeExp(buff, paras, "benefits4new", "=", "0", "and");
			}
		}
		
		//  20170719   WCF
		if (StringUtil.isBlank(type) == true) {
			makeExp(buff, paras, "benefits4new", ">=", "0", "and");
		}
		//  end
		
		if(!StringUtil.isBlank(productType)){
			makeExp(buff, paras, "productType", "=", productType, "and");
		}
		if(StringUtil.isBlank(minLimit) == false){
			makeExp(buff, paras, "loanTimeLimit", ">=", minLimit, "and");
		}
		if(StringUtil.isBlank(maxLimit) == false){
			makeExp(buff, paras, "loanTimeLimit", "<=", maxLimit, "and");
		}
		if (StringUtil.isBlank(exceptBeginDate) == false&&StringUtil.isBlank(exceptEndDate) == false) {
			sqlFrom+=" and (releaseDate<"+exceptBeginDate+" or releaseDate>"+exceptEndDate+") ";
		}
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "loanState", loanStates,"and");
		System.out.println(sqlSelect + " "+ sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+sqlOrder);
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4WhereHasWhere(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 分页查询标书(后台)
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Page<LoanInfo> findByPage(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate,String rBeginDateTime,String rEndDateTime, String loanState,String allkey){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.uid desc,t1.releaseDate desc,t1.loanBalance desc";
		List<Object> paras = new ArrayList<Object>();

		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.createDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDate", "<=", endDate, "and");
		if(!StringUtil.isBlank(rBeginDateTime) ){
			makeExp(buff, paras, "t1.releaseDate", ">=", rBeginDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", ">=", rBeginDateTime.split("-")[1], "and");
		}
		
		if(!StringUtil.isBlank(rEndDateTime)){
			makeExp(buff, paras, "t1.releaseDate", "<=", rEndDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", "<=", rEndDateTime.split("-")[1], "and");
		}
		
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 分页查询标书(后台) 可用于生成Excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4Noob2(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate,String rBeginDateTime,String rEndDateTime, String loanState,String loanType,String allkey){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.releaseDate desc,t1.releaseTime desc,t1.uid desc,t1.loanBalance desc";
		List<Object> paras = new ArrayList<Object>();

		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.loanType", "=", loanType, "and");
		
		makeExp(buff, paras, "t1.createDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDate", "<=", endDate, "and");
		if(!StringUtil.isBlank(rBeginDateTime) ){
			makeExp(buff, paras, "t1.releaseDate", ">=", rBeginDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", ">=", rBeginDateTime.split("-")[1], "and");
		}
		
		if(!StringUtil.isBlank(rEndDateTime)){
			makeExp(buff, paras, "t1.releaseDate", "<=", rEndDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", "<=", rEndDateTime.split("-")[1], "and");
		}
		
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		Page<LoanInfo> pageLoanInfo = LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pageLoanInfo.isFirstPage());
		result.put("lastPage", pageLoanInfo.isLastPage());
		result.put("pageNumber", pageLoanInfo.getPageNumber());
		result.put("pageSize", pageLoanInfo.getPageSize());
		result.put("totalPage", pageLoanInfo.getTotalPage());
		result.put("totalRow", pageLoanInfo.getTotalRow());
		result.put("list", pageLoanInfo.getList());
		return result;
	}
	
	/**
	 * 分页查询标书(后台) 可用于生成Excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4Noob3(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate,String rBeginDateTime,String rEndDateTime, String loanState,String loanType, String loanTimeLimit, String allkey){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.releaseDate desc,t1.releaseTime desc,t1.uid desc,t1.loanBalance desc";
		List<Object> paras = new ArrayList<Object>();

		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.loanType", "=", loanType, "and");
		
		makeExp(buff, paras, "t1.loanTimeLimit", "=", loanTimeLimit, "and");
		
		makeExp(buff, paras, "t1.createDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDate", "<=", endDate, "and");
		if(!StringUtil.isBlank(rBeginDateTime) ){
			makeExp(buff, paras, "t1.releaseDate", ">=", rBeginDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", ">=", rBeginDateTime.split("-")[1], "and");
		}
		
		if(!StringUtil.isBlank(rEndDateTime)){
			makeExp(buff, paras, "t1.releaseDate", "<=", rEndDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", "<=", rEndDateTime.split("-")[1], "and");
		}
		
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		Page<LoanInfo> pageLoanInfo = LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pageLoanInfo.isFirstPage());
		result.put("lastPage", pageLoanInfo.isLastPage());
		result.put("pageNumber", pageLoanInfo.getPageNumber());
		result.put("pageSize", pageLoanInfo.getPageSize());
		result.put("totalPage", pageLoanInfo.getTotalPage());
		result.put("totalRow", pageLoanInfo.getTotalRow());
		result.put("list", pageLoanInfo.getList());
		return result;
	}
	
	/**
	 * 分页查询标书信息 对应的统计，可用于生成Excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4Noob2WithSum(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate,String rBeginDateTime,String rEndDateTime, String loanState,String loanType,String allkey){
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();

		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.loanType", "=", loanType, "and");
		
		makeExp(buff, paras, "t1.createDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDate", "<=", endDate, "and");
		if(!StringUtil.isBlank(rBeginDateTime) ){
			makeExp(buff, paras, "t1.releaseDate", ">=", rBeginDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", ">=", rBeginDateTime.split("-")[1], "and");
		}
		
		if(!StringUtil.isBlank(rEndDateTime)){
			makeExp(buff, paras, "t1.releaseDate", "<=", rEndDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", "<=", rEndDateTime.split("-")[1], "and");
		}
		
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		long count_loanAmount = Db.queryBigDecimal("select COALESCE(sum(t1.loanAmount),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("count_loanAmount", count_loanAmount);
		return result;
	}
	
	/**
	 * 分页查询标书信息 对应的统计，可用于生成Excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4Noob3WithSum(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate,String rBeginDateTime,String rEndDateTime, String loanState,String loanType, String loanTimeLimit, String allkey){
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();

		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.loanType", "=", loanType, "and");
		
		makeExp(buff, paras, "t1.loanTimeLimit", "=", loanTimeLimit, "and");
		
		makeExp(buff, paras, "t1.createDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.createDate", "<=", endDate, "and");
		if(!StringUtil.isBlank(rBeginDateTime) ){
			makeExp(buff, paras, "t1.releaseDate", ">=", rBeginDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", ">=", rBeginDateTime.split("-")[1], "and");
		}
		
		if(!StringUtil.isBlank(rEndDateTime)){
			makeExp(buff, paras, "t1.releaseDate", "<=", rEndDateTime.split("-")[0], "and");
			makeExp(buff, paras, "t1.releaseTime", "<=", rEndDateTime.split("-")[1], "and");
		}
		
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		long count_loanAmount = Db.queryBigDecimal("select COALESCE(sum(t1.loanAmount),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("count_loanAmount", count_loanAmount);
		return result;
	}
	
	/**
	 * 还款管理 分页查询标(后台),返回Map,可用于生成excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(还款日期)
	 * @param endDate		结束日期(还款日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4Noob(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate, String loanState,String allkey,String backDate){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.backDate asc,t1.releaseDate desc,t1.uid desc";
		List<Object> paras = new ArrayList<Object>();
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.backDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.backDate", "<=", endDate, "and");
		
		makeExp(buff, paras, "t1.backDate", "=", backDate, "and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		Page<LoanInfo> pageLoanInfo = LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
		Map<String,Object> result = new HashMap<String, Object>();
		
		result.put("firstPage", pageLoanInfo.isFirstPage());
		result.put("lastPage", pageLoanInfo.isLastPage());
		result.put("pageNumber", pageLoanInfo.getPageNumber());
		result.put("pageSize", pageLoanInfo.getPageSize());
		result.put("totalPage", pageLoanInfo.getTotalPage());
		result.put("totalRow", pageLoanInfo.getTotalRow());
		
		long nowAllbj = 0;//当前页下期待还本金
		long nowAlllx = 0;//当前页下前待还利息
		long nowLoanAmount = 0;//当前页借款总额
		List<LoanInfo> loans = new ArrayList<LoanInfo>();
		
		for (int i = 0; i < pageLoanInfo.getList().size(); i++) {
			LoanInfo tmp = pageLoanInfo.getList().get(i);
			long b1 = 0,x1 = 0;//已还
			long b2 = 0,x2 = 0;//待还
			long b3 = 0,x3 = 0;//下期待还
			boolean hasNext = true;
			nowLoanAmount = nowLoanAmount + tmp.getLong("loanAmount");
			int reciedCount = tmp.getInt("reciedCount");
			if(tmp.getStr("loanState").equals("O") || tmp.getStr("loanState").equals("P") || tmp.getStr("loanState").equals("Q")){
//				reciedCount = tmp.getInt("loanTimeLimit");
				hasNext = false;
			}
			LiCai ff = new LiCai( tmp.getLong("loanAmount") , tmp.getInt("rateByYear") + tmp.getInt("rewardRateByYear") + tmp.getInt("benefits4new"), tmp.getInt("loanTimeLimit") );
			if(tmp.getStr("refundType").equals("A")){
				if(reciedCount+1 <= tmp.getInt("loanTimeLimit") && hasNext){
					Map<String,Long> sad = ff.getDengE4month(reciedCount+1);
					b3 = b3 + sad.get("ben");
					x3 = x3 + sad.get("xi");
				}
				List<Map<String , Long>> xxx = ff.getDengEList() ;
				for (int j = 1; j <= xxx.size(); j++) {
					Map<String,Long> ck = xxx.get(j-1);
					if(j <= reciedCount){
						b1 = b1 + ck.get("ben");
						x1 = x1 + ck.get("xi");
					}else{
						b2 = b2 + ck.get("ben");
						x2 = x2 + ck.get("xi");
					}
				}
				if(tmp.getStr("loanState").equals("P")){
					b2 = 0;
					x2 = 0;
					b3 = 0;
					x3 = 0;
					b1 = tmp.getLong("loanAmount");
				}
			}else if(tmp.getStr("refundType").equals("B")){
				if(reciedCount+1 <= tmp.getInt("loanTimeLimit") && hasNext){
					Map<String,Long> sad = ff.getDengXi4month(reciedCount+1);
					b3 = b3 + sad.get("ben");
					x3 = x3 + sad.get("xi");
				}
				
				x1 = x1 + (long) (ff.dengxi()*reciedCount);
				x2 = x2 + (long) (ff.dengxi()* (tmp.getInt("loanTimeLimit") - reciedCount));
				if(reciedCount < tmp.getInt("loanTimeLimit")){
					b2 = b2 + tmp.getLong("loanAmount");
				}
				if(reciedCount == tmp.getInt("loanTimeLimit") ){
					b1 += tmp.getLong("loanAmount");
				}
				if(tmp.getStr("loanState").equals("P")){
					b2 = 0;
					x2 = 0;
					b3 = 0;
					x3 = 0;
					b1 = tmp.getLong("loanAmount");
				}
			}
			nowAllbj += b3;
			nowAlllx += x3;
			tmp.put("xqbj",b3);
			tmp.put("xqlx",x3);
			tmp.put("yinghuanbx",b1+x1+b2+x2);
			tmp.put("yihuanbx",b1+x1);
			tmp.put("yihuanbj",b1);tmp.put("yihuanlx",x1);
			tmp.put("daihuanbx",b2+x2);
			tmp.put("daihuanbj",b2);tmp.put("daihuanlx",x2);
			long xqlx_jx = 0;
			long xqbj_jx = 0;
			try {
				xqlx_jx = Db.queryBigDecimal("select COALESCE(SUM(nextInterest),0) from t_loan_trace where loanCode = ?",tmp.getStr("loanCode")).longValue();
			} catch (Exception e) {
				xqlx_jx = 0;
			}
			try {
				xqbj_jx = Db.queryBigDecimal("select COALESCE(SUM(nextAmount),0) from t_loan_trace where loanCode = ?",tmp.getStr("loanCode")).longValue();
			} catch (Exception e) {
				xqbj_jx = 0;
			}
			tmp.put("xqlx_jx",xqlx_jx);
			tmp.put("xqbj_jx",xqbj_jx);
			Object[] x = Db.queryFirst("select earlyDate,estatus from t_settlement_early where loanCode = ?", tmp.getStr("loanCode"));
			if(x==null){
				tmp.put("earlyDate","nil");
				tmp.put("estatus","nil");
			}else{
				tmp.put("earlyDate",StringUtil.isBlank((String)x[0])?"nil":x[0]);
				tmp.put("estatus",StringUtil.isBlank((String)x[1])?"nil":x[1]);
			}
			Object[] y = Db.queryFirst("select * from t_loan_overdue where loanCode = ? and disposeStatus = 'n' ", tmp.getStr("loanCode"));
			if(y==null){
				tmp.put("isOverdue","否");
			}else{
				tmp.put("isOverdue","是");
			}
			loans.add(tmp);
		}
		result.put("nowLoanAmount",nowLoanAmount);
		result.put("nowAllbj", nowAllbj);
		result.put("nowAlllx", nowAlllx);
		result.put("list", loans);
		
		return result;
	}
	
	
	/**
	 * 还款管理 分页查询标(后台),对应的统计信息,可用于生成excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(标书创建日期)
	 * @param endDate		结束日期(标书创建日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSum(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate, String loanState,String allkey,String backDate){
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.backDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.backDate", "<=", endDate, "and");
		
		makeExp(buff, paras, "t1.backDate", "=", backDate, "and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		long count_loanAmount = Db.queryBigDecimal("select COALESCE(sum(t1.loanAmount),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("count_loanAmount", count_loanAmount);
		return result;
	}
	
	/**
	 * 还款管理 分页查询标(后台),对应的统计信息(统计剩余本金、剩余利息),可用于生成excel
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(还款日期)
	 * @param endDate		结束日期(还款日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSum3(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate, String loanState,String allkey,String backDate){
		String sqlFrom = " from t_loan_trace t3 left join t_loan_info t1 on t3.loanCode = t1.loanCode left join t_user t2 on t3.loanUserCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		makeExp(buff, paras, "t3.loanUserCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.backDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.backDate", "<=", endDate, "and");
		
		makeExp(buff, paras, "t1.backDate", "=", backDate, "and");
		
		String[] keys = new String[]{"t3.loanTitle","t1.userName","t2.userEmail","t2.userName","t3.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		long count_nextAmount = Db.queryBigDecimal("select COALESCE(sum(t3.nextAmount),0) "+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		long count_nextInterest = Db.queryBigDecimal("select COALESCE(sum(t3.nextInterest),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("count_nextAmount", count_nextAmount);
		result.put("count_nextInterest", count_nextInterest);
		return result;
	}
	
	/**
	 * 还款管理 分页查询标(后台),对应的统计信息(统计剩余本金、剩余利息),可用于生成excel
	 * 增加结清统计
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(还款日期)
	 * @param endDate		结束日期(还款日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSum2(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate, String loanState,String allkey,String backDate){
		String sqlFrom = " from t_loan_trace t3 left join t_loan_info t1 on t3.loanCode = t1.loanCode left join t_user t2 on t3.loanUserCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] loanStates = loanState.split(",");
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		makeExp(buff, paras, "t3.loanUserCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.backDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.backDate", "<=", endDate, "and");
		
		makeExp(buff, paras, "t1.backDate", "=", backDate, "and");
		
		String[] keys = new String[]{"t3.loanTitle","t1.userName","t2.userEmail","t2.userName","t3.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		/*
		 * count_leftAmount     总计 待还本金
		 * count_leftInterest   总计 待还利息
		 * count_settleInterest 结清统计 待还利息
		 */
		long count_settleInterest=0;
		long count_leftAmount = Db.queryBigDecimal("select COALESCE(sum(t3.leftAmount),0) "+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		long count_leftInterest = Db.queryBigDecimal("select COALESCE(sum(t3.leftInterest),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		//long count_nextAmount = Db.queryBigDecimal("select COALESCE(sum(t3.nextAmount),0) "+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		//long count_nextInterest = Db.queryBigDecimal("select COALESCE(sum(t3.nextInterest),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		
		if(loanState.equals("P")||loanState.length()>1){
			//结清统计查询提前还款状态
			for (int i = 0; i < paras.size(); i++) {
				if(paras.get(i).equals("N")||paras.get(i).equals("O")||paras.get(i).equals("Q")){
					paras.set(i, "P");
				}
			}
			count_settleInterest=Db.queryBigDecimal("select COALESCE(sum(t3.leftInterest),0)"+sqlFrom+(makeSql4Where(buff)),paras.toArray()).longValue();
		}
		
		Map<String,Object> result = new HashMap<String, Object>();
		result.put("count_leftAmount", count_leftAmount);
		result.put("count_leftInterest", count_leftInterest);
		//result.put("count_nextAmount", count_nextAmount);
		//result.put("count_nextInterest", count_nextInterest);
		result.put("count_settleInterest", count_settleInterest);
		
		//查询统计 下期应还本金，利息
		Map<String,Object> result_next=findByPage4NoobWithSumNext(userCode, pageNumber, pageSize, beginDate, endDate, loanState, allkey, backDate);
		result.putAll(result_next);
		return result;
	}
	
	
	/**
	 * 还款管理  统计信息(总计下期应还本金、总计下期应还利息)
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param beginDate		开始日期(还款日期)
	 * @param endDate		结束日期(还款日期)
	 * @param loanState		标书状态   单个值如 G  多个值格式如：I,G,H
	 * @return
	 */
	public Map<String,Object> findByPage4NoobWithSumNext(String userCode,Integer pageNumber, Integer pageSize, 
			String beginDate, String endDate, String loanState,String allkey,String backDate){
		String sqlFrom = "select t1.* from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		List<Object> paras = new ArrayList<Object>();
		
		String[] loanStates = loanState.split(",");
		
		makeExp4In(buff, paras, "t1.loanState", loanStates,"and");
		
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		
		makeExp(buff, paras, "t1.backDate", ">=", beginDate, "and");
		makeExp(buff, paras, "t1.backDate", "<=", endDate, "and");
		
		makeExp(buff, paras, "t1.backDate", "=", backDate, "and");
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");		
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		
		List<LoanInfo> lists = LoanInfo.loanInfoDao.find(sqlFrom+(makeSql4Where(buff)), paras.toArray());
		Map<String,Object> result = new HashMap<String, Object>();
		
		long count_nextAmount=0;   //总计  下期应还本金
		long count_nextInterest=0; //总计  下期应还利息
		
		for (int i = 0; i < lists.size(); i++) {
			LoanInfo tmp=lists.get(i);
			long b=0,x=0;  //下期待还本、息
			boolean hasNext=true;
			int reciedCount=tmp.getInt("reciedCount");
			if(tmp.getStr("loanState").equals("O")||tmp.getStr("loanState").equals("P")||tmp.getStr("loanState").equals("Q")){
				hasNext = false;
			}
			LiCai ff=new LiCai(tmp.getLong("loanAmount") , tmp.getInt("rateByYear") + tmp.getInt("rewardRateByYear") + tmp.getInt("benefits4new"), tmp.getInt("loanTimeLimit"));
			if(tmp.getStr("refundType").equals("A")){
				if(reciedCount+1 <= tmp.getInt("loanTimeLimit") && hasNext){
					Map<String,Long> sad = ff.getDengE4month(reciedCount+1);
					b = b + sad.get("ben");
					x = x + sad.get("xi");
				}
				if(tmp.getStr("loanState").equals("P")){
					b=0;
					x=0;
				}
			}else if(tmp.getStr("refundType").equals("B")){
				if(reciedCount+1 <= tmp.getInt("loanTimeLimit") && hasNext){
					Map<String,Long> sad = ff.getDengXi4month(reciedCount+1);
					b = b + sad.get("ben");
					x = x + sad.get("xi");
				}
				if(tmp.getStr("loanState").equals("P")){
					b=0;
					x=0;
				}
			}
			count_nextAmount +=b;
			count_nextInterest +=x;
		}
		result.put("count_nextAmount", count_nextAmount);
		result.put("count_nextInterest", count_nextInterest);
		return result;
	}
	
	/**
	 * 分页查询今天日还款的标
	 * @param userCode		借款人userCode
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param allkey		模糊查询
	 * @return
	 */
	public Page<LoanInfo> findByTodayJieSuan(String userCode,Integer pageNumber, Integer pageSize, String allkey){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.uid desc,t1.releaseDate desc,t1.loanBalance desc";
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t2.userEmail","t2.userName","t1.loanNo"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
		}
		makeExp(buff, paras, "t1.userCode", "=", userCode, "and");
		makeExp(buff, paras, "t1.loanState", "=", "N", "and");
		makeExp(buff, paras, "t1.backDate", "=", DateUtil.getNowDate() , "and");
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 查询满标待审信息 WJW
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	public Page<LoanInfo> findByPage4full(Integer pageNumber, Integer pageSize){
		String select = "select *";
		String sqlExceptSelect = " from t_loan_info where loanBalance=0 and loanState='J'";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, select, sqlExceptSelect);
	}
	
	public Page<LoanInfo> findByPage4full(Integer pageNumber , Integer pageSize, String allkey){
		String sqlSelect = "select t1.* ";
		String sqlFrom = " from t_loan_info t1 inner join t_user t2 on t1.userCode = t2.userCode ";
		StringBuffer buff = new StringBuffer("");
		String sqlOrder = " order by t1.uid desc";
		List<Object> paras = new ArrayList<Object>();
		
		String[] keys = new String[]{"t1.loanTitle","t1.userName","t1.loanNo","t2.userName","t2.userEmail"};
		makeExp4AnyLike(buff, paras, keys, allkey, "and","or");
		
		if(!StringUtil.isBlank(allkey)){
			String x = "";
			try {
				x = CommonUtil.encryptUserMobile(allkey);
			} catch (Exception e) {
				x = "";
			}
			makeExp(buff, paras, "t2.userMobile", "=" , x , "or");
			buff.insert(0, "(");
			buff.append(")");
		}
		makeExp(buff, paras, "1", "!=" , "2" , "and");
		makeExp(buff, paras, "t1.loanState", "=" , "M" , "and");
		makeExp(buff, paras, "t1.loanBalance", "=" , "0" , "and");
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect,  
				sqlFrom+(makeSql4Where(buff)).toString()+sqlOrder,paras.toArray());
	}
	
	/**
	 * 分页查询还款中的标书，用于结算
	 * @param pageNumber
	 * @param pageSize
	 * @param settleDate 结算日
	 * @return
	 */
	public Page<LoanInfo> findByPage4Settlement(Integer pageNumber, Integer pageSize ){
		String sqlSelect = "select *";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState = ? and loanBalance = 0 and clearDate!=? and (";
		Integer[] clearDays = getClearDay() ;
		for( int i = 0 ; i < clearDays.length ; i ++){
			sqlWhere += "clearDay=" + clearDays[i];
			if( clearDays.length != (i+1) ){
				sqlWhere += " or ";
			}
		}
		sqlWhere+= ") ";
		String sqlOrder = " order by createDate desc,createTime desc,releaseDate desc,releaseTime desc";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, 
				sqlFrom+sqlWhere+sqlOrder,SysEnum.loanState.N.val(),DateUtil.getStrFromDate(new Date(), "MMdd"));
	}
	
	/**
	 * 	更新标的统计信息
	 * @param loan
	 * @return
	 */
	public boolean updateLoan4clear(LoanInfo loan){
		loan.set("clearDate", DateUtil.getStrFromDate( new Date(), "MMdd")) ;
		return loan.update() ;
	}
	
	private Integer[] getClearDay(){
		Calendar nowCal = Calendar.getInstance() ;
//		nowCal.set(2015, 1, 31, 0, 0, 0);
//		nowCal.set(Calendar.MONTH, 0);
//		nowCal.set(Calendar.DAY_OF_MONTH, 31);
		int nowDay = nowCal.get( Calendar.DAY_OF_MONTH ) ;
//		nowCal.set(2015, 3, 12, 0, 0, 0);
		nowCal.set(Calendar.DAY_OF_MONTH , -1 );
		int lastDay = nowCal.get( Calendar.DAY_OF_MONTH );
		Integer[] days = null ;
		if( nowDay < lastDay ){
			days = new Integer[]{nowDay};
		}else{
			int dayLimit = 32 - nowDay ;//(31-nowDay) + 1
			days = new Integer[ dayLimit ] ;
			for(int count = 0 ; count < dayLimit ; count ++ ){
				days[count] = nowDay + count ;
			}
		}
		return days ;
	}
	
	/**
	 * 查询招标中自动投标后还没投满的标
	 * @return
	 */
	public List<LoanInfo> findByPage4SubAutoLoan(){
		String sqlSelect = "select loanCode,loanTitle,loanState,loanATAmount,loanBalance,releaseDate,releaseTime ";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState='J' and loanATAmount=0 and loanBalance>0 ";
		String sqlOrder = "order by releaseDate asc,releaseTime desc";
		return LoanInfo.loanInfoDao.find(sqlSelect + sqlFrom + sqlWhere + sqlOrder);
	}
	
	/**
	 * 分页查询可以自动投标的标书<br>
	 * 固定条件：<br>
	 * 状态		招标中<br>
	 * 剩余额度	大于0
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @return
	 */
	public Page<LoanInfo> findByPage4AutoLoan(Integer pageNumber, Integer pageSize){
		//select * from t_loan_info where loanState = 'J' and loanBalance > 0 
		//and (releaseDate+releaseTime)<='20150813152500' order by releaseDate asc,releaseTime asc
		String sqlSelect = "select * ";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState = ? and loanBalance > 0 and loanATAmount>0 and benefits4new <= 0 ";
		sqlWhere += "and CONCAT(releaseDate,releaseTime)<=?  " ;
		//String sqlOrder = " order by createDate desc,createTime desc,releaseDate desc,releaseTime desc";
		String sqlOrder = "order by releaseDate asc,releaseTime desc";
		
		Date d = new Date( System.currentTimeMillis() + 15*60*1000 );	//提前15分钟的时候开始
		String tmpDateTime = DateUtil.getDateStrFromDateTime( d );
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, 
				sqlFrom+sqlWhere+sqlOrder,SysEnum.loanState.J.val(), tmpDateTime );
	}
	
	/**
	 * 投标-更新贷款标剩余额度及时间，增加投标流水
	 * @param loan		标对象的rewardRateByYear必须是包含新手奖励利率的
	 * @param amount
	 * @param payUserCode
	 * @param opType
	 * @return
	 */
	public boolean update4prepareBid(LoanInfo loan,long amount,String payUserCode ,
			String opType , long rankValue , List<Tickets> tickets,int zdjl){
//		LoanInfo loan = LoanInfo.loanInfoDao.findById(loanCode);
		String loanCode = loan.getStr("loanCode");
		String refundType = loan.getStr("refundType") ;
		int rateByYear = loan.getInt("rateByYear") ;
		int rewardRateByYear = loan.getInt("rewardRateByYear") ;//已经加了新手奖励利率与加息券
//		int benefits4new = loan.getInt("benefits4new");
		int limit = loan.getInt("loanTimeLimit") ;
		
		User user = User.userDao.findByIdLoadColumns(payUserCode,"userName");
		//新增投标流水
		LoanTrace loanTrace = new LoanTrace();
		String traceCode = UIDUtil.generate() ;
		loanTrace.set("traceCode", traceCode);
		loanTrace.set("loanCode", loanCode);
		loanTrace.set("loanNo", loan.getStr("loanNo"));
		if(StringUtil.isBlank(loan.getStr("loanArea"))){
			loanTrace.set("loanTitle", loan.getStr("loanTitle"));
		}else{
			loanTrace.set("loanTitle","【"+ loan.getStr("loanArea")+"】" + loan.getStr("loanTitle"));
			
		}
		loanTrace.set("productType", loan.getStr("productType"));
		loanTrace.set("loanDateTime", DateUtil.getNowDateTime(1000));
		loanTrace.set("loanType", opType);//投标流水表，投标方式，A自动 M WEB手动 N 移动端手动
		loanTrace.set("payAmount", amount);
		loanTrace.set("creditorName", loan.getStr("creditorName"));
		loanTrace.set("creditorCardId", loan.getStr("creditorCardId"));
		
		//冗余代收本金和利息，by five 2015-12-4
		long[] benxi = CommonUtil.f_004(amount, (rateByYear+rewardRateByYear), limit, refundType );
		loanTrace.put("leftAmount", amount ) ;
		loanTrace.put("leftInterest", benxi[1] ) ;
		
		loanTrace.set("payUserCode", payUserCode);
		loanTrace.set("loanTimeLimit", limit );
		loanTrace.set("payUserName", user.getStr("userName"));
		loanTrace.set("loanUserCode", loan.getStr("userCode"));
		loanTrace.set("loanUserName", loan.getStr("userName"));
		loanTrace.set("isInterest", loan.getInt("isInterest"));
		loanTrace.set("isAutoLoan", loan.getInt("isAutoLoan"));
		loanTrace.set("loanRecyDate", "00000000");
		loanTrace.set("loanRecyCount", limit);
		loanTrace.set("rankValue", rankValue ) ;
		loanTrace.set("refundType", refundType );
		loanTrace.set("rateByYear", rateByYear );
		loanTrace.set("rewardRateByYear", rewardRateByYear +zdjl );
		loanTrace.set("traceState", SysEnum.traceState.A.val());
		loanTrace.set("loanState", loan.getStr("loanState"));
		loanTrace.set("isTransfer", "C");
		
		//ticket描述同步到loanTrace
		if( tickets != null && tickets.size() > 0){
			JSONArray ja = new JSONArray();
			for (int i = 0; i < tickets.size(); i++) {
				Tickets ticket = tickets.get(i);
				JSONObject ticketInfo = new JSONObject() ;
				ticketInfo.put("code", ticket.getStr("tCode") ) ;
				ticketInfo.put("type", ticket.getStr("ttype")) ;
				ticketInfo.put("amount", ticket.getInt("amount")) ;
				ticketInfo.put("rate", ticket.getInt("rate")) ;
				//20170519添加兑换券使用额度
//				JSONObject json = JSONObject.parseObject(ticket.getStr("useEx"));
//				ticketInfo.put("examount", json.getLongValue("amount"));
				//end
				//20170726添加债转是否扣除
				ticketInfo.put("isDel", ticket.getStr("isDel"));
				//end
				ja.add(ticketInfo);
			}
			loanTrace.put("loanTicket", ja.toJSONString() );
		}
		
		if(loanTrace.save()){
			int zz = Db.update("update t_loan_info set loanBalance = loanBalance - ?,updateDate=?,updateTime=? "
					+ "where loanCode=? and (loanBalance-?)>=0",
					amount,DateUtil.getNowDate(),DateUtil.getNowTime(),loanCode , amount);
			if(zz>0){
				return true;
			}else{
				loanTrace.deleteById(traceCode) ;
			}
		}
		return false;
	}
	
	
	/**
	 * 投标-更新贷款标剩余额度及时间，增加投标流水
	 * @param loan		标对象的rewardRateByYear必须是包含新手奖励利率的
	 * @param amount
	 * @param payUserCode
	 * @param opType
	 * @return
	 */
	public boolean update4prepareBid(LoanInfo loan,long amount,String payUserCode ,
			String opType , long rankValue , List<Tickets> tickets,int zdjl,String jxTraceCode,String authCode ){
//		LoanInfo loan = LoanInfo.loanInfoDao.findById(loanCode);
		String loanCode = loan.getStr("loanCode");
		String refundType = loan.getStr("refundType") ;
		int rateByYear = loan.getInt("rateByYear") ;
		int rewardRateByYear = loan.getInt("rewardRateByYear") ;//已经加了新手奖励利率与加息券
//		int benefits4new = loan.getInt("benefits4new");
		int limit = loan.getInt("loanTimeLimit") ;
		
		User user = User.userDao.findByIdLoadColumns(payUserCode,"userName");
		//新增投标流水
		LoanTrace loanTrace = new LoanTrace();
		String traceCode = UIDUtil.generate() ;
		loanTrace.set("traceCode", traceCode);
		loanTrace.set("loanCode", loanCode);
		loanTrace.set("loanNo", loan.getStr("loanNo"));
		if(StringUtil.isBlank(loan.getStr("loanArea"))){
			loanTrace.set("loanTitle", loan.getStr("loanTitle"));
		}else{
			loanTrace.set("loanTitle","【"+ loan.getStr("loanArea")+"】" + loan.getStr("loanTitle"));
			
		}
		loanTrace.set("productType", loan.getStr("productType"));
		loanTrace.set("loanDateTime", DateUtil.getNowDateTime(1000));
		loanTrace.set("loanType", opType);//投标流水表，投标方式，A自动 M WEB手动 N 移动端手动
		loanTrace.set("payAmount", amount);
		loanTrace.set("creditorName", loan.getStr("creditorName"));
		loanTrace.set("creditorCardId", loan.getStr("creditorCardId"));
		
		//冗余代收本金和利息，by five 2015-12-4
		long[] benxi = CommonUtil.f_004(amount, (rateByYear+rewardRateByYear), limit, refundType );
		loanTrace.put("leftAmount", amount ) ;
		loanTrace.put("leftInterest", benxi[1] ) ;
		
		loanTrace.set("payUserCode", payUserCode);
		loanTrace.set("loanTimeLimit", limit );
		loanTrace.set("payUserName", user.getStr("userName"));
		loanTrace.set("loanUserCode", loan.getStr("userCode"));
		loanTrace.set("loanUserName", loan.getStr("userName"));
		loanTrace.set("isInterest", loan.getInt("isInterest"));
		loanTrace.set("isAutoLoan", loan.getInt("isAutoLoan"));
		loanTrace.set("loanRecyDate", "00000000");
		loanTrace.set("loanRecyCount", limit);
		loanTrace.set("rankValue", rankValue ) ;
		loanTrace.set("refundType", refundType );
		loanTrace.set("rateByYear", rateByYear );
		loanTrace.set("rewardRateByYear", rewardRateByYear +zdjl );
		loanTrace.set("traceState", SysEnum.traceState.A.val());
		loanTrace.set("loanState", loan.getStr("loanState"));
		loanTrace.set("isTransfer", "C");
		loanTrace.set("jxTraceCode",jxTraceCode);
		loanTrace.set("authCode",authCode);
		//ticket描述同步到loanTrace
		if( tickets != null && tickets.size() > 0){
			JSONArray ja = new JSONArray();
			for (int i = 0; i < tickets.size(); i++) {
				Tickets ticket = tickets.get(i);
				JSONObject ticketInfo = new JSONObject() ;
				ticketInfo.put("code", ticket.getStr("tCode") ) ;
				ticketInfo.put("type", ticket.getStr("ttype")) ;
				ticketInfo.put("amount", ticket.getInt("amount")) ;
				ticketInfo.put("rate", ticket.getInt("rate")) ;
				//20170519添加兑换券使用额度
//				JSONObject json = JSONObject.parseObject(ticket.getStr("useEx"));
//				ticketInfo.put("examount", json.getLongValue("amount"));
				//end
				//20170726添加债转是否扣除
				ticketInfo.put("isDel", ticket.getStr("isDel"));
				//end
				ja.add(ticketInfo);
			}
			loanTrace.put("loanTicket", ja.toJSONString() );
		}
		
		if(loanTrace.save()){
			int zz = Db.update("update t_loan_info set loanBalance = loanBalance - ?,updateDate=?,updateTime=? "
					+ "where loanCode=? and (loanBalance-?)>=0",
					amount,DateUtil.getNowDate(),DateUtil.getNowTime(),loanCode , amount);
			if(zz>0){
				return true;
			}else{
				loanTrace.deleteById(traceCode) ;
			}
		}
		return false;
	}
	/**
	 * 设置满标待审
	 * @param loanCode 	标书编码
	 * @return
	 */
	public boolean updateLoanByFull(String loanCode){
		int x1 = Db.update("update t_loan_info set loanState='M',updateDate=?,updateTime=? where loanCode = ?",DateUtil.getNowDate(),DateUtil.getNowTime(),loanCode);
		int x2 = Db.update("update t_loan_trace set traceState='D',loanState='M' where loanCode=?",loanCode);
		if(x1>0 && x2 >0){
			return true;
		}
		return false;
	}
	
	/**
	 * 审核满标确认放款，更新标状态、放款时间、投标流水冗余字段等<br>
	 * 前置条件：必须更新借款人、投标人资金账户后
	 * @param loanCode
	 * @param timeLimit 	还款总期数
	 * @return
	 */
	public boolean updateLoanByLent(String loanCode, String loanUserCode, int timeLimit ){
		String nowDate = DateUtil.getNowDate() ;
		String nowTime = DateUtil.getNowTime() ;
		String clearDay = nowDate.substring(6, 8) ;
		String clearDate = nowDate.substring(4,8);//防止今天放款，今天放款的话，clearDate不等于今天，那今晚结算就还款了
		
		Calendar nextRepaymentDate = CommonUtil.anyRepaymentDate(nowDate,1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String backDate = sdf.format(nextRepaymentDate.getTime());//首次还款日期
		
		int x1 = Db.update("update t_loan_info set clearDay = ?, clearDate=?,loanState=?,backDate=?,effectDate=?,"
				+ "effectTime=?,updateDate=?,updateTime=?,clearDay=? where loanCode = ?",
				clearDay,clearDate,SysEnum.loanState.N.val(), backDate,nowDate,nowTime,nowDate,nowTime,clearDay,loanCode);
		//int yy = Db.update("update t_loan_info set loanTotal=?,loanCount=? where userCode=?",loanTotal,loanCount,loanUserCode);//更新借款人所有标的冗余数据
		int x2 = Db.update("update t_loan_trace set loanTimeLimit=?, loanRecyCount = ?,loanRecyDate=?,traceState=?,loanState=? where loanCode=?",
				timeLimit,timeLimit,backDate,SysEnum.traceState.N.val() , SysEnum.loanState.N.val() ,loanCode);
		if(x1>0 &&  x2 >0){
			return true;
		}
		return false;
	}
	
	/**
	 * 当天结算后，如果标有异常，用这个方法更新标和流水的状态
	 * @param loanCode		贷款标编码
	 * @param loanState		标状态枚举
	 * @param traceState	流水状态
	 * @return
	 */
	public boolean update4clearException(String loanCode,SysEnum.loanState loanState){
		String clearDate = DateUtil.getStrFromDate( new Date(), "MMdd") ;
		int x1 = Db.update("update t_loan_info set loanState=? , clearDate=?,updateDate=?,updateTime=? where loanCode=?", loanState.val(), clearDate,DateUtil.getNowDate(),DateUtil.getNowTime() , loanCode );
		int x2 = Db.update("update t_loan_trace set loanState=?",loanState.val());
		return (x1>0) && (x2>0);
	}
	
//	/**
//	 * 当天结算完成后，更新标的统计信息，以及投标流水的统计信息
//	 * @param loanInfo		贷款标model
//	 * @param loanState		贷款标状态枚举
//	 * @param limit			第几期还款
//	 * @return
//	 */
//	public boolean update4clearSuccess(LoanInfo loanInfo ,SysEnum.loanState loanState,int limit){
//		String loanCode = loanInfo.getStr("loanCode" ) ;
//		String clearDate = DateUtil.getStrFromDate( new Date(), "MMdd") ;
//		int x1 = Db.update("update t_loan_info set reciedCount = reciedCount + 1 , loanState=? , clearDate=?,updateDate=?,updateTime=? where loanCode=?",
//				loanState.val() , clearDate ,DateUtil.getNowDate(),DateUtil.getNowTime(), loanCode );
//		Calendar nextRepaymentDate = CommonUtil.anyRepaymentDate(loanInfo.getStr("effectDate"),limit+1);
//		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
//		String nextDate = sdf.format(nextRepaymentDate.getTime());//下次还款日期
//		int x2 = Db.update("update t_loan_trace set loanRecyDate = ?,loanRecyCount = loanRecyCount-1 where loanCode = ?",nextDate,loanCode);
//		if( x1 == 1 && x2 > 0 ){
//			//如果是最后一期，则直接更新
//			int lastUpdate = Db.update("update t_loan_info set loanState='O',updateDate=?,updateTime=? where loanCode=? and loanTimeLimit=reciedCount and loanState='N'",
//					DateUtil.getStrFromDate( new Date(), "yyyyMMdd"), DateUtil.getStrFromDate( new Date(), "HHmmss") , loanCode );
//			if( lastUpdate == 1 ){
//				//更新标流水状态
//				int lastUpdateResult = Db.update("update t_loan_trace set traceState = 'B',loanState='O' where loanCode=? and traceState='N'" , loanCode ) ;
//				return lastUpdateResult>0;
//			}
//		}
//		return false ;
//	}

	
	/**
	 * 应回收欠款列表
	 * @param pageNunber
	 * @param pageSize
	 * @param date  指定日期   默认一个星期
	 */
	public Page<LoanInfo> findLoanInfo4Week(Integer pageNumber, Integer pageSize,
			String date) {
		
		String beginDate = DateUtil.getNowDate();
		String endDate = DateUtil.addDay(beginDate, 7);
		
		if(StringUtil.isBlank(date) == false){
			beginDate = date;
			endDate = date;
		}
		
		String sqlSelect = "select loanNo,CONCAT(left(userName,1),'**') userName,backDate,releaseDate,releaseTime,loanState,loanAmount,rateByYear,rewardRateByYear,benefits4new, loanTimeLimit,refundType,reciedCount ";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState = 'N' ";
		sqlWhere += "and backDate between ? and ? " ;
		String sqlOrder = "order by backDate,loanNo";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, 
				sqlFrom+sqlWhere+sqlOrder,beginDate,endDate);
		
	}

	
	/**
	 * 待发布总额
	 * @return
	 */
	public long totalBeAmount(){
		String sql = "select sum(loanAmount) from t_loan_info where loanState='H'";
		BigDecimal queryBigDecimal = Db.queryBigDecimal(sql);
		return null == queryBigDecimal ? 0L : queryBigDecimal.longValue();
	}
	
	/**
	 * 今日已发布总额
	 * @return
	 */
	public long totalRelAmount(){
		String nowDate = DateUtil.getNowDate();
		String sql = "select sum(loanAmount) from t_loan_info where releaseDate = ? and loanState in ('J','M','N')";
		BigDecimal queryBigDecimal =  Db.queryBigDecimal(sql,nowDate);
		return null == queryBigDecimal ? 0L : queryBigDecimal.longValue();
	}
	
	/**
	 * 今日已满标总额
	 * @return
	 */
	public long totalSuccRelAmount(){
		String nowDate = DateUtil.getNowDate();
		String sql = "select sum(loanAmount) from t_loan_info where releaseDate = ? and loanState in ('M','N')";
		BigDecimal queryBigDecimal = Db.queryBigDecimal(sql,nowDate);
		return null == queryBigDecimal ? 0L : queryBigDecimal.longValue();
	}
	/**
	 * 根据日期查询发标数量和借款标类型统计
	 * @return long[借款标数量,质押标,抵押标,其它]
	 */
	public long[] countPubByDate(String date){
		long[] x = new long[5];
		x[0] = 0;
		x[1] = Db.queryLong("select count(loanCode) from t_loan_info where releaseDate = ? and loanState in ('N','O','P') and productType = ?",date,SysEnum.productType.A.val());
		x[2] = Db.queryLong("select count(loanCode) from t_loan_info where releaseDate = ? and loanState in ('N','O','P') and productType = ?",date,SysEnum.productType.B.val());
		x[3] = Db.queryLong("select count(loanCode) from t_loan_info where releaseDate = ? and loanState in ('N','O','P') and productType = ?",date,SysEnum.productType.C.val());
		x[4] = Db.queryLong("select count(loanCode) from t_loan_info where releaseDate = ? and loanState in ('N','O','P') and productType = ?",date,SysEnum.productType.G.val());
		x[0] = x[1] + x[2] + x[3] + x[4];
		return x;
	}
	
	/**
	 * 根据借款标还款期限和起止日期查询数量(不包含招标中的)
	 * @param startDate
	 * @param endDate
	 * @param minLimit 最小还款期限
	 * @param maxLimit 最大还款期限
	 * @return
	 */
	public long countLoanTimeLimitByDate(String startDate, String endDate, int minLimit, int maxLimit){
		return Db.queryLong("select count(userCode) from t_loan_info where loanState in ('N','O','P','Q') and releaseDate>= ? and releaseDate <=? and loanTimeLimit >= ? and loanTimeLimit <= ? ",startDate,endDate,minLimit,maxLimit);
	}
	
	/**
	 * 根据借款标还款方式和起止日期查询数量(不包含招标中的)
	 * @param startDate
	 * @param endDate
	 * @param refundType	还款方式
	 * @return
	 */
	public long countRefundTypeByDate(String startDate, String endDate, String refundType){
		return Db.queryLong("select count(userCode) from t_loan_info where loanState in ('N','O','P','Q') and releaseDate>= ? and releaseDate <=? and refundType = ? ",startDate,endDate,refundType);
	}
	
	/**
	 * 根据借款标借款标类型和起止日期查询数量
	 * @param startDate
	 * @param endDate
	 * @param loanType	借款标类型
	 * @return
	 */
	public long countLoanTypeByDate(String startDate, String endDate, String loanType){
		if(StringUtil.isBlank(loanType)){
			return Db.queryLong("select count(userCode) from t_loan_info where loanState = 'N' and releaseDate>= ? and releaseDate <=? ",startDate,endDate);
		}
		return Db.queryLong("select count(userCode) from t_loan_info where loanState = 'N' and releaseDate>= ? and releaseDate <=? and loanType = ? ",startDate,endDate,loanType);
	}

	/**
	 * 根据借款标产品类型统计查询标的金额(不包含招标中)
	 * @param productType
	 * @return
	 */
	public long sumProductType(String productType){
		return Db.queryBigDecimal("select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in ('N','O','P') and productType = ? ",productType).longValue();
	}
	
	/**
	 * 根据满标时间查询借款标的金额统计(不包含预投标)
	 * @return
	 */
	public long countLoanAmountByEffectDate(String startDate,String endDate){
		return Db.queryBigDecimal("select COALESCE(sum(loanAmount),0) from t_loan_info where effectDate >= ? and effectDate <= ? and loanState in('N','O','P','Q')",startDate,endDate).longValue();
	}
	
	 /**
	  *  根据投标时间查询交易的笔数 20181031 hw
	  */
	public long countLoanCountByPayDate(String startDate,String endDate){
		return Db.queryLong("select count(payUserCode) from t_loan_trace where DATE_FORMAT(loanDateTime,'%Y%m%d') between ? and ? and loanState in('N','O','P','Q','J')", startDate,endDate);
	}

	/**
	 * 统计总投标金额(根据标信息统计)
	 * @return
	 */
	public long sumLoanAmount(){
		return Db.queryBigDecimal("select COALESCE(sum(loanAmount),0) from t_loan_info where loanState in ('N','O','P','Q') ").longValue();
	}
	
	/**
	 * 统计待收、已收金额
	 * @param productType
	 * @return
	 */
	public long sumPayAmount(){
		return Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal+reciedPrincipal),0) from t_funds").longValue();
	}
	
	
	/**
	 * 统计总投标金额(根据资金账户统计)
	 * @return
	 */
	public long sumLoanAmount4Funds(){
		return Db.queryBigDecimal("select COALESCE(sum(beRecyPrincipal+reciedPrincipal),0) from t_funds").longValue();
	}	
	
	
	
	
	
	/**
	 * 分页查询标书（网贷之家数据接口）
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param startDate		满标开始时间
	 * @param endDate		满标结束时间
	 * @return
	 */
	public Page<LoanInfo> findByWDZJ(Integer pageNumber, Integer pageSize,String startDate,String endDate){
		String sd = startDate.replaceAll("-", "");
		String ed = endDate.replaceAll("-", "");
		String selectSql = "select * ";
		String sql = "from t_loan_info where effectDate >= ? and effectDate <= ? and loanState in ('M','N','O','P') order by lastPayLoanDateTime asc";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize,selectSql,sql,sd,ed);
	}
	
	/**
	 * 分页查询提前还款标书（网贷之家数据接口）
	 * @param pageNumber
	 * @param pageSize
	 * @param startDate
	 * @param endDate
	 * @return
	 */
	public Page<LoanInfo> findByWDZJPrepayment(Integer pageNumber, Integer pageSize,String startDate,String endDate){
		String sd = startDate.replaceAll("-", "");
		String ed = endDate.replaceAll("-", "");
		String selectSql = "select loanCode,loanTimeLimit ";
		String sql = "from t_loan_info where loanState='P' and effectDate >= ? and effectDate <= ? order by lastPayLoanDateTime asc";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize,selectSql,sql,sd,ed);
	}
	
	
	/**
	 * 分页查询标书（天眼数据接口）
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param status 是否是满标（1 就是查询满标时间，如果不是1就是发标时间）
	 * @param startDate		满标开始时间
	 * @param endDate		满标结束时间
	 * @return
	 */
	public Page<LoanInfo> findByTY(Integer pageNumber, Integer pageSize,int status,String startDate,String endDate){
		String sd = startDate.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		String ed = endDate.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		String sql;
		if(status==1){//查询满标状态
			sql = "from t_loan_info where  loanState in('N','O','P') and  CONCAT(effectDate,effectTime) >= ? and CONCAT(effectDate,effectTime) <= ? order by lastPayLoanDateTime asc";
		}else{//投标中
			sql = "from t_loan_info where loanState in('J') and CONCAT(releaseDate,releaseTime) >= ? and CONCAT(releaseDate,releaseTime) <= ?  order by lastPayLoanDateTime asc";
		}
		
		
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, "select * ",sql,sd,ed);
	}
	/**
	 * 不分页
	 * @param status 是否是满标（1 就是查询满标时间，如果不是1就是发标时间）
	 * @param startDate		满标开始时间
	 * @param endDate		满标结束时间
	 * @return
	 */
	public List<LoanInfo> findByTY1(int status, String startDate, String endDate){
		String sd = startDate.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		String ed = endDate.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		String sql = "select * ";
		if(status==1){
			sql += "from t_loan_info where  loanState in('N','O','P') and  CONCAT(effectDate,effectTime) >= ? and CONCAT(effectDate,effectTime) <= ? order by lastPayLoanDateTime asc";
		}else{
			sql += "from t_loan_info where loanState in('J') and CONCAT(releaseDate,releaseTime) >= ? and CONCAT(releaseDate,releaseTime) <= ?  order by lastPayLoanDateTime asc";
		}
		
		return LoanInfo.loanInfoDao.find(sql, sd ,ed);
	}
	
	/**
	 * 分页查询标书（贷出去数据接口）
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @return
	 */
	public Page<LoanInfo> findByDCQ(Integer pageNumber, Integer pageSize){
		String sel = "select *";
		String selExp = " from t_loan_info where 1=1 order by lastPayLoanDateTime asc";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sel , selExp );
	}
	
	
	
	/**
	 * 按时间查询标书（佳璐数据）
	 * @param today 当天的日期
	 */
	public List<LoanInfo> findByJLSJ(String today){
		String td = today.replaceAll("-", "");
		String sql = "select * from t_loan_info where effectDate =? order by lastPayLoanDateTime asc";
		return LoanInfo.loanInfoDao.find(sql, td);
	}
	
	
	
	/**
	 * 统计标总金额（网贷之家数据接口）
	 * @param startDate		满标开始时间
	 * @param endDate		满标结束时间
	 * @return
	 */
	public long countWDZJ(String startDate,String endDate){
		String sd = startDate.replaceAll("-", "");
		String ed = endDate.replaceAll("-", "");
		String sql = "select COALESCE(sum(loanAmount),0) from t_loan_info where effectDate >= ? and effectDate <= ?";
		return Db.queryBigDecimal(sql,sd,ed).longValue();
	}
	
	
	/**
	 * 根据时间查询已满标记录(CFCA 使用)
	 * @param effectDate
	 * @param effectTime
	 * @return
	 */
	public List<LoanInfo> findLoan4effect(String effectDate , String effectTime){
		String sqlSelect = "select * ";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState = 'N' and CONVERT(CONCAT(effectDate , effectTime),SIGNED) >= ? ";
		return LoanInfo.loanInfoDao.find(sqlSelect + sqlFrom + sqlWhere,effectDate+effectTime);
	}
	
	/**
	 * 根据借款标产品类型统计查询标的数量
	 * @param productType
	 * @return
	 */
	public long countByProductType(String productType) {
		return Db.queryLong("SELECT count(1) FROM t_loan_info WHERE  productType = ? ",productType); 
	}
	
	public boolean saveOrUpdate(LoanInfo loanInfo) {
		String querySql = "SELECT * FROM t_loan_info WHERE loanCode = ?";
		String loanCode = loanInfo.getStr("loanCode");
		boolean doResult = false;
		if (StringUtil.isBlank(loanCode) == false) {
			LoanInfo tmpLoanInfo = LoanInfo.loanInfoDao.findFirst(querySql, loanCode);
			if (tmpLoanInfo == null) {
				doResult = loanInfo.save();
			} else {
				tmpLoanInfo._setAttrs(loanInfo);
				tmpLoanInfo.set("updateDate", DateUtil.getNowDate());
				tmpLoanInfo.set("updateTime", DateUtil.getNowTime());
				doResult = tmpLoanInfo.update();
			}
		} else {
			doResult = loanInfo.save();
		}
		return doResult ;
	}

	public List<LoanInfo> findByNxjd() {
		String sql = "select * from t_loan_info where productType = ? AND loanState IN ('J', 'N')";
		return LoanInfo.loanInfoDao.find(sql, productType.YFQ.val());
	}
	
	/**
	 * 查询当天满标记录（新手标除外）
	 * @param effectDate 满标日期
	 * @return
	 */
	public List<LoanInfo> findAllFullLoan(String effectDate){
		String sqlSelect = "select * ";
		String sqlFrom = "from t_loan_info ";
		String sqlWhere = "where loanState = 'N' and CONVERT(effectDate,SIGNED) = ? and benefits4new = 0 ORDER BY effectTime DESC";
		return LoanInfo.loanInfoDao.find(sqlSelect+sqlFrom+sqlWhere , effectDate);
	}
	
	/**
	 * 分页查询还款标（仍在放款）
	 * */
	public Page<LoanInfo> findByPage(Integer pageNumber, Integer pageSize){
		String sqlSelect = "select t1.*,t2.jxAccountId ";
		String sqlFrom = " from t_loan_info t1 left join t_user t2 on t1.userCode=t2.userCode";
		String sqlOrder = " where loanState = 'N' order by createDate desc";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder);
	}
	
	/**
	 * 分页查询放款列表
	 */
	public Page<LoanInfo> findByPage4Withdraw(Integer pageNumber, Integer pageSize,String beginDate,String endDate,String state,String group){
		String sqlSelect = "select * ";
		String sqlFrom = "from (SELECT t1.*,t3.applyUserGroup ,t1.loanAmount-ifnull(t2.withdrawAmount,0) leftWithdrawAmount from t_loan_info t1 LEFT JOIN t_loan_apply t3 on t1.loanNo=t3.loanNo LEFT JOIN (SELECT SUM(withdrawAmount) withdrawAmount,loanApplyCode from t_withdraw_trace where status='3' GROUP BY loanApplyCode) t2 on t1.loanCode = t2.loanApplyCode where t1.productType != 'E') t4 where t4.loanState in ('N','O','P') ";
		if(!StringUtil.isBlank(beginDate)){
			sqlFrom+=" and t4.effectDate>="+beginDate;
		}
		if(!StringUtil.isBlank(endDate)){
			sqlFrom+=" and t4.effectDate <= "+endDate;
		}
		if(StringUtil.isBlank(state)){
			sqlFrom+=" and t4.leftWithdrawAmount > 0 ";
		}else{
			sqlFrom+=" and t4.leftWithdrawAmount <= 0 ";
		}
		if(!StringUtil.isBlank(group)){
			sqlFrom+=" and t4.applyUserGroup = '"+group+"'";
		}
		System.out.println(sqlSelect+sqlFrom);
		String sqlOrder = " ORDER BY t4.effectDate desc ";
		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, sqlSelect, sqlFrom+sqlOrder);
	}
	
	/**
	 * 根据结算日期查询标loanCode WJW
	 * @param date 日期
	 * @param nowDate 屏蔽结算日期
	 * @return
	 */
	public List<String> undoneLoanCode(String date,String nowDate){
		return Db.query("select loanCode from t_loan_info where backDate = ? and loanState='N' and clearDate!=?",date,nowDate);
	}
	
	/**
	 * 分页查询标书（贷罗盘数据接口）
	 * @param pageNumber	第几页
	 * @param pageSize		每页几条
	 * @param date		满标时间
	 * @return
	 */
	public Page<LoanInfo> findByDLP(Integer pageNumber, Integer pageSize,String date){
		String md = date.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", "");
		String sql;
		//查询满标状态
		sql = "from t_loan_info where  loanState in('N','O','P') and  effectDate = ? order by lastPayLoanDateTime asc";

		return LoanInfo.loanInfoDao.paginate(pageNumber, pageSize, "select * ",sql,md);
	}
	
	/**
	 * 借款人总借款金额
	 * */
	public long sumLoanUserAmount(String userCode){
		return Db.queryBigDecimal("select COALESCE(sum(loanAmount),0) from t_loan_info where userCode = ? and loanState in ('J','O','P','N')",userCode).longValue();
	}
	
	/**
	 * 根据日期查询当日还款本息金额及标的数(不含提前还款) WJW
	 * @param date
	 * @param type	1:应还正常,2:实际正常
	 * @return
	 */
	public Long[] sumAmountAndCount(String date,int type){
		String sql = "select COALESCE(sum(t2.nextAmount+t2.nextInterest),0) amount,count(DISTINCT t1.loanCode) num from t_loan_info t1 left join t_loan_trace t2 on t1.loanCode=t2.loanCode where t1.backDate=? and t1.loanState='N' and t1.loanCode not in (select loanCode from t_settlement_early where earlyDate=? and estatus='A')";
		if(type == 2){
			sql += " and t1.loanCode not in (select loanCode from t_loan_overdue where disposeDateTime='' and disposeStatus='n')";
		}
		LoanInfo loanInfo = LoanInfo.loanInfoDao.find(sql,date,date).get(0);
		return new Long[]{loanInfo.getBigDecimal("amount").longValue(),loanInfo.getLong("num")};
	}
	
	/**
	 * 根据日期查询当日提前还款本息金额及标的数 WJW
	 * @param date
	 * @param type	1:应还正常,2:实际正常
	 * @return
	 */
	public Long[] sumEarlyAmountAndCount(String date,int type){
		String sql = "select COALESCE(sum(t3.leftAmount+t3.nextInterest),0) amount,count(DISTINCT t1.loanCode) num from t_settlement_early t1 inner join t_loan_info t2 on t1.loanCode=t2.loanCode inner join t_loan_trace t3 on t1.loanCode=t3.loanCode where t1.earlyDate=? and t1.estatus='A' and t2.loanState='N'";
		if(type == 2){
			sql += " and t1.loanCode not in (select loanCode from t_loan_overdue where disposeDateTime='' and disposeStatus='n')";
		}
		LoanInfo loanInfo = LoanInfo.loanInfoDao.find(sql,date).get(0);
		return new Long[]{loanInfo.getBigDecimal("amount").longValue(),loanInfo.getLong("num")};
	}
	
	/**
	 * 根据日期查询当日垫付利息及标的数 WJW
	 * @param date
	 * @return
	 */
	public Long[] sumAdvanceInterestAndCount(String date){
		String sql = "select COALESCE(sum(t2.nextInterest),0) amount,count(DISTINCT t1.loanCode) num from t_loan_info t1 left join t_loan_trace t2 on t1.loanCode=t2.loanCode where t1.backDate=? and t1.loanState='N' and t1.loanCode in (select loanCode from t_loan_overdue where disposeDateTime='' and disposeStatus='n')";
		LoanInfo loanInfo = LoanInfo.loanInfoDao.find(sql,date).get(0);
		return new Long[]{loanInfo.getBigDecimal("amount").longValue(),loanInfo.getLong("num")};
	}
	
	/**
	 * 根据结算日查询标期数已结清,仍逾期标(不含当日已结算)
	 * @param clearDay
	 * @return
	 */
	public List<LoanInfo> queryEndOverdueLoan(int startClearDay,int endClearDay){
		String nowClearDate = DateUtil.getStrFromDate( new Date(), "MMdd");
		String sql = "select DISTINCT t1.loanCode,t1.effectDate,t1.loanArea from t_loan_info t1 left join (select t.loanCode,t.disposeStatus,t.overdueType from (select * from t_loan_overdue where disposeStatus='n' and disposeDateTime='' order by repayIndex desc) t group by loanCode) t2 on t1.loanCode=t2.loanCode where t1.loanState in ('O','P') and t1.clearDay between ? and ? and t1.clearDate<>? and t2.disposeStatus='n' and t2.overdueType='I'";
		return LoanInfo.loanInfoDao.find(sql,startClearDay,endClearDay,nowClearDate);
	}
	
	/**
	 * 根据实际结算日期查询标的 WJW
	 * @param repaymentYesDate
	 * @return
	 */
	public List<LoanInfo> queryRepaymentYesLoan(String repaymentYesDate){
		String sql = "select * from t_loan_info  where effectDate <> ? and ((loanState='N' and  if(substring(backDate,5,2)=01,CONCAT(LEFT(backDate,4)-1,'12',RIGHT(clearDate,2)),CONCAT(LEFT(backDate,4),clearDate)) =?) or (loanState in ('O','P') and backDate=?))";
		return LoanInfo.loanInfoDao.find(sql,repaymentYesDate,repaymentYesDate,repaymentYesDate);
	}
	
	/**
	 * 根据结算日期返回当天标结算日(起始/结束) WJW
	 * @param date
	 * @return
	 */
	public int[] getClearDay(String date){
		int startClearDay = Integer.valueOf(date.substring(6));//结算日起始日
		Date[] dates = null;
		try {
			dates = DateUtil.getMonthBetween(DateUtil.getDateFromString(date, "yyyyMMdd"));
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		String monthLastDate = DateUtil.getStrFromDate(dates[1], "yyyyMMdd");//结算日月份最后一天日期
		int endClearDay = monthLastDate.equals(date)?31:startClearDay;//结算日结束日(若结算日期为本月最后一日,则取最大值31)
		return new int[]{startClearDay,endClearDay};
	}
	
	/**
	 * 统计当日待还款数据 WJW
	 * @param repaymentDate	还款日期
	 * return{
	 * pcdcyhzbj:批次代偿应还总本金,
	 * pcdcyhzlx:批次代偿应还总利息,
	 * hbyhzbj:红包应还总本金,
	 * hbyhzlx:红包应还总利息,
	 * yhzbj:应还总本金,
	 * yhzlx:应还总利息,
	 * sjzchkzbj:实际正常还款总本金,
	 * sjzchkzlx:实际正常还款总利息,
	 * yqhkzbj:逾期还款总本金,
	 * yqdfzlx:逾期垫付总利息,
	 * yhzchkbds:应还正常还款标的数,
	 * yhtqhkbds:应还提前还款标的数,
	 * sjzchkbds:实际正常还款标的数,
	 * sjtqhkbds:实际提前还款标的数,
	 * yqhbjbds:逾期还本金标的数,
	 * yqdflxbds:逾期垫付利息标的数,
	 * yfzchkpcs:应发正常还款批次数(含提前还款),
	 * yfyqbjpcs:应发逾期本金批次数,
	 * yfyqdflxpcs:应发逾期垫付利息批次数}			
	 * 
	 */
	public Map<String, Long> repaymentCount(String repaymentDate){
		long pcdcyhzbj = 0; // 批次代偿应还总本金
		long pcdcyhzlx = 0; // 批次代偿应还总利息
		long hbyhzbj = 0; // 红包应还总本金
		long hbyhzlx = 0; // 红包应还总利息

		long yhzbj = 0; // 应还总本金
		long yhzlx = 0; // 应还总利息
		long sjzchkzbj = 0; // 实际正常还款总本金
		long sjzchkzlx = 0; // 实际正常还款总利息
		long yqhkzbj = 0; // 逾期还款总本金
		long yqdfzlx = 0; // 逾期垫付总利息
		
		long qtyhzbj = 0; // 齐通应还总本金
		long qtyhzlx = 0; // 齐通应还总利息
		long qtsjzchkzbj = 0; // 齐通实际正常还款总本金
		long qtsjzchkzlx = 0; // 齐通实际正常还款总利息
		long qtyqdfzlx = 0; // 齐通逾期垫付总利息
		long qtyhkbds = 0; // 齐通应还款标的数
		long qtsjhkbds = 0; // 齐通实际还款标的数

		long yhzchkbds = 0; // 应还正常还款标的数
		long yhtqhkbds = 0; // 应还提前还款标的数
		long sjzchkbds = 0; // 实际正常还款标的数
		long sjtqhkbds = 0; // 实际提前还款标的数
		long yqhbjbds = 0; // 逾期还本金标的数
		long yqdflxbds = 0; // 逾期垫付利息标的数
		long yqbdflxbds = 0;// 逾期不垫付利息标的数

		long yfzchkpcs = 0; // 应发正常还款批次数(含提前还款)
		long yfyqbjpcs = 0; // 应发逾期本金批次数
		long yfyqdflxpcs = 0; // 应发逾期垫付利息批次数
		
		//扫描逾期中列表
		Map<String, loanOverdueType> loanOverdueNotMap = new HashMap<String,loanOverdueType>();//key:loanCode,value:overdueType
		List<LoanOverdue> loanOverdueNots = LoanOverdue.overdueTraceDao.find("select * from (select * from t_loan_overdue where disposeStatus='n' and disposeDateTime='' order by repayIndex desc) t1 group by loanCode");
		for (LoanOverdue loanOverdue : loanOverdueNots) {
			String loanCode = loanOverdue.getStr("loanCode");
			String overdueType = loanOverdue.getStr("overdueType");
			
			loanOverdueNotMap.put(loanCode, loanOverdueType.valueOf(overdueType));
		}
		
		//扫描当日逾期结算列表
		List<String> loanOverdueLoanCodes = Db.query("select DISTINCT loanCode from t_loan_overdue where disposeStatus='n' and DATE_FORMAT(disposeDateTime,'%Y%m%d')<=?",DateUtil.getNowDate());
		yqhbjbds = loanOverdueLoanCodes.size();
		for (String loanCode : loanOverdueLoanCodes) {
			List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanCode = ?", loanCode);
			int authCodeNum = 0;
			for (LoanTrace loanTrace : loanTraces) {
				long overdueAmount = loanTrace.getLong("overdueAmount");
				String authCode = loanTrace.getStr("authCode");
				
				if(StringUtil.isBlank(authCode)){//红包还款
					hbyhzbj += overdueAmount;
				}else {//代偿户还款
					pcdcyhzbj += overdueAmount;
					
					if(overdueAmount > 0){
						authCodeNum++;
					}
				}
				
				yqhkzbj += overdueAmount;
			}
			
			yfyqbjpcs += authCodeNum%150 == 0?authCodeNum/150:authCodeNum/150+1;
		}
		
		//提前还款资金统计
		List<SettlementEarly> settlementEarlies = SettlementEarly.settletmentEaryltDao.find("select * from t_settlement_early where earlyDate = ? and estatus = 'A'",repaymentDate);
		List<String> settlementLoanCodes = new ArrayList<String>();//提前还款标号
		List<String> sjtqhkbdsLoanCodes = new ArrayList<String>();//实际提前还款loanCode
		for (SettlementEarly settlementEarly : settlementEarlies) {
			String loanCode = settlementEarly.getStr("loanCode");
			settlementLoanCodes.add(loanCode);
			LoanInfo loanInfo = findById(loanCode);
			String productType = loanInfo.getStr("productType");//标类型
			String loanArea = loanInfo.getStr("loanArea");//标来源
			boolean isQT = loanArea.indexOf("齐通") != -1;//true:齐通
			qtyhkbds += isQT ? 1:0;
			yqbdflxbds += loanOverdueType.N == loanOverdueNotMap.get(loanCode) ? 1:0;
			List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanCode = ?", loanCode);
			int authCodeNum = 0;//该标的含有authCode数量
			int yfyqdflxpcsAuthCodeNum = 0;//应发逾期垫付利息标的含有authCode数量
			for (LoanTrace loanTrace : loanTraces) {
				long leftAmount = loanTrace.getLong("leftAmount");
				long nextInterest = loanTrace.getLong("nextInterest");
				long overdueInterest = loanTrace.getLong("overdueInterest");
				String loanState = loanTrace.getStr("loanState");
				String authCode = loanTrace.getStr("authCode");
				nextInterest = overdueInterest > 0 ? overdueInterest : nextInterest;
				nextInterest = loanOverdueType.N == loanOverdueNotMap.get(loanCode) ? 0:nextInterest;
				nextInterest = "E".equals(productType) ? nextInterest:0;//标类型非易分期利息为0
				leftAmount = loanOverdueNotMap.containsKey(loanCode) ? 0:leftAmount;
				
				if(!"N".equals(loanState)){//该标不是还款中,跳过
					continue;
				}
				if(StringUtil.isBlank(authCode)){//红包还款
					hbyhzbj += leftAmount;
					hbyhzlx += nextInterest;
				}else {//代偿户还款
					pcdcyhzbj += leftAmount;
					pcdcyhzlx += nextInterest;
					
					authCodeNum = loanOverdueNotMap.containsKey(loanCode) ? authCodeNum:authCodeNum+1;
					yfyqdflxpcsAuthCodeNum = loanOverdueType.I == loanOverdueNotMap.get(loanCode) ? yfyqdflxpcsAuthCodeNum+1:yfyqdflxpcsAuthCodeNum;
				}
				
				yhzbj += loanTrace.getLong("leftAmount");
				yhzlx += loanTrace.getLong("nextInterest");
				
				if(isQT){
					qtyhzbj += loanTrace.getLong("leftAmount");
					qtyhzlx += loanTrace.getLong("nextInterest");
				}
				
				if(!loanOverdueNotMap.containsKey(loanCode)){
					sjzchkzbj += leftAmount;
					sjzchkzlx += nextInterest;
					
					if(isQT){
						qtsjzchkzbj += leftAmount;
						qtsjzchkzlx += nextInterest;
					}
					
					if(sjtqhkbdsLoanCodes.indexOf(loanCode) == -1){
						sjtqhkbdsLoanCodes.add(loanCode);
						qtsjhkbds += isQT ? 1:0;
					}
				}else if(loanOverdueType.I == loanOverdueNotMap.get(loanCode)){
					yqdfzlx += nextInterest;
					
					qtyqdfzlx += isQT ? nextInterest:0;
				}
			}
			
			yfzchkpcs += authCodeNum%150 == 0?authCodeNum/150:authCodeNum/150+1;
			yfyqdflxpcs += yfyqdflxpcsAuthCodeNum%150 == 0?yfyqdflxpcsAuthCodeNum/150:yfyqdflxpcsAuthCodeNum/150+1;
		}
		
		yhtqhkbds += settlementLoanCodes.size();
		sjtqhkbds += sjtqhkbdsLoanCodes.size();
		
		//正常还款资金统计
		List<LoanTrace> loanTraces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanRecyDate=? and productType='E'",repaymentDate);
		List<String> loanCodes = new ArrayList<String>();//应还正常还款标loanCode
		List<String> sjzchkbdsLoanCodes = new ArrayList<String>();//实际正常还款标loanCode
		Map<String, Integer> yfzchkpcsMap = new HashMap<String,Integer>();//key:loanCode,value:authCodeNum
		Map<String, Integer> yfyqdflxpcsMap = new HashMap<String,Integer>();//key:loanCode,value:yfyqdflxpcsAuthCodeNum
		
		Map<String, Boolean> isQTMap = new HashMap<String,Boolean>();//key:loanCode,value:true(齐通)
		List<LoanInfo> loanInfos = findJTHK(repaymentDate);
		for (LoanInfo loanInfo : loanInfos) {
			String loanCode = loanInfo.getStr("loanCode");
			if(settlementLoanCodes.indexOf(loanCode) != -1){
				continue;
			}
			
			String loanArea = loanInfo.getStr("loanArea");//标来源
			boolean isQT = loanArea.indexOf("齐通") != -1;//true:齐通
			isQTMap.put(loanCode, isQT);
			qtyhkbds += isQT ? 1:0;
			yqbdflxbds += loanOverdueType.N == loanOverdueNotMap.get(loanCode) ? 1:0;
		}
		for (LoanTrace loanTrace : loanTraces) {
			String loanState = loanTrace.getStr("loanState");
			String loanCode = loanTrace.getStr("loanCode");
			String productType = loanTrace.getStr("productType");//标类型
			String authCode = loanTrace.getStr("authCode");
			long nextAmount = loanTrace.getLong("nextAmount");
			long nextInterest = loanTrace.getLong("nextInterest");
			long overdueInterest = loanTrace.getLong("overdueInterest");
			nextInterest = overdueInterest > 0 ? overdueInterest : nextInterest;
			nextInterest = loanOverdueType.N == loanOverdueNotMap.get(loanCode) ? 0:nextInterest;
			nextInterest = "E".equals(productType) ? nextInterest:0;//标类型非易分期利息为0
			nextAmount = loanOverdueNotMap.containsKey(loanCode) ? 0:nextAmount;
			
			if(isQTMap.get(loanCode) == null){//标异常
				continue;
			}
			if(!"N".equals(loanState)){//该标不是还款中,跳过
				continue;
			}
			if(settlementLoanCodes.indexOf(loanCode) != -1){//该标在当日提前还款,跳过
				continue;
			}
			
			if(loanCodes.indexOf(loanCode) == -1){
				loanCodes.add(loanCode);
			}
			
			if(StringUtil.isBlank(authCode)){//红包还款
				hbyhzbj += nextAmount;
				hbyhzlx += nextInterest;
			}else {//代偿户还款
				pcdcyhzbj += nextAmount;
				pcdcyhzlx += nextInterest;
			}
			
			yhzbj += loanTrace.getLong("nextAmount");
			yhzlx += loanTrace.getLong("nextInterest");
			
			if(isQTMap.get(loanCode)){
				qtyhzbj += loanTrace.getLong("nextAmount");
				qtyhzlx += loanTrace.getLong("nextInterest");
			}
			
			if(!loanOverdueNotMap.containsKey(loanCode)){//不逾期
				sjzchkzbj += nextAmount;
				sjzchkzlx += nextInterest;
				
				if(isQTMap.get(loanCode)){
					qtsjzchkzbj += nextAmount;
					qtsjzchkzlx += nextInterest;
				}
				
				if(sjzchkbdsLoanCodes.indexOf(loanCode) == -1){
					sjzchkbdsLoanCodes.add(loanCode);
					qtsjhkbds += isQTMap.get(loanCode) ? 1:0;
				}
				
				if(yfzchkpcsMap.containsKey(loanCode)){
					int authCodeNum = yfzchkpcsMap.get(loanCode);
					yfzchkpcsMap.put(loanCode, StringUtil.isBlank(authCode)?authCodeNum:authCodeNum+1);
				}else {
					yfzchkpcsMap.put(loanCode, StringUtil.isBlank(authCode)?0:1);
				}
			}else if(loanOverdueType.I == loanOverdueNotMap.get(loanCode)){
				yqdfzlx += nextInterest;
				qtyqdfzlx += isQTMap.get(loanCode) ? nextInterest:0;
				
				if(yfyqdflxpcsMap.containsKey(loanCode)){
					int authCodeNum = yfyqdflxpcsMap.get(loanCode);
					yfyqdflxpcsMap.put(loanCode, StringUtil.isBlank(authCode)?authCodeNum:authCodeNum+1);
				}else {
					yfyqdflxpcsMap.put(loanCode, StringUtil.isBlank(authCode)?0:1);
				}
			}
		}
		
		for(Map.Entry<String, Integer> entry:yfzchkpcsMap.entrySet()){
			int authCodeNum = entry.getValue();
			yfzchkpcs += authCodeNum%150 == 0?authCodeNum/150:authCodeNum/150+1;
		}
		
		for(Map.Entry<String, Integer> entry:yfyqdflxpcsMap.entrySet()){
			int authCodeNum = entry.getValue();
			yfyqdflxpcs += authCodeNum%150 == 0?authCodeNum/150:authCodeNum/150+1;
		}
		
		yhzchkbds += loanCodes.size();
		sjzchkbds += sjzchkbdsLoanCodes.size();
		
		yqdflxbds += yhtqhkbds - sjtqhkbds + yhzchkbds - sjzchkbds - yqbdflxbds;
		
		//扫描标已结清,今日需补发逾期利息标
		int[] clearDays = getClearDay(repaymentDate);
		List<LoanInfo> queryEndOverdueLoan = queryEndOverdueLoan(clearDays[0],clearDays[1]);
		yqdflxbds += queryEndOverdueLoan.size();
		for (LoanInfo loanInfo : queryEndOverdueLoan) {
			String loanCode = loanInfo.getStr("loanCode");
			String loanArea = loanInfo.getStr("loanArea");//标来源
			boolean isQT = loanArea.indexOf("齐通") != -1;//true:齐通
			List<LoanTrace> traces = LoanTrace.loanTraceDao.find("select * from t_loan_trace where loanCode = ?", loanCode);
			int authCodeNum = 0;
			for (LoanTrace loanTrace : traces) {
				String authCode = loanTrace.getStr("authCode");
				long overdueInterest = loanTrace.getLong("overdueInterest");
				if(StringUtil.isBlank(authCode)){//红包还款
					hbyhzlx += overdueInterest;
				}else {
					pcdcyhzlx += overdueInterest;
					authCodeNum++;
				}
				yqdfzlx += overdueInterest;
				qtyqdfzlx += isQT ? overdueInterest:0;
			}
			
			yfyqdflxpcs += authCodeNum%150 == 0?authCodeNum/150:authCodeNum/150+1;
		}
		
		Map<String, Long> repaymentCountMap = new HashMap<String,Long>();
		repaymentCountMap.put("pcdcyhzbj",pcdcyhzbj);
		repaymentCountMap.put("pcdcyhzlx",pcdcyhzlx);
		repaymentCountMap.put("hbyhzbj",hbyhzbj);
		repaymentCountMap.put("hbyhzlx",hbyhzlx);
						
		repaymentCountMap.put("yhzbj",yhzbj);
		repaymentCountMap.put("yhzlx",yhzlx);
		repaymentCountMap.put("sjzchkzbj",sjzchkzbj);
		repaymentCountMap.put("sjzchkzlx",sjzchkzlx);
		repaymentCountMap.put("yqhkzbj",yqhkzbj);
		repaymentCountMap.put("yqdfzlx",yqdfzlx);
		
		repaymentCountMap.put("qtyhzbj", qtyhzbj);
		repaymentCountMap.put("qtyhzlx", qtyhzlx);
		repaymentCountMap.put("qtsjzchkzbj", qtsjzchkzbj);
		repaymentCountMap.put("qtsjzchkzlx", qtsjzchkzlx);
		repaymentCountMap.put("qtyqdfzlx", qtyqdfzlx);
		repaymentCountMap.put("qtyhkbds", qtyhkbds);
		repaymentCountMap.put("qtsjhkbds", qtsjhkbds);
						
		repaymentCountMap.put("yhzchkbds",yhzchkbds);
		repaymentCountMap.put("yhtqhkbds",yhtqhkbds);
		repaymentCountMap.put("sjzchkbds",sjzchkbds);
		repaymentCountMap.put("sjtqhkbds",sjtqhkbds);
		repaymentCountMap.put("yqhbjbds",yqhbjbds);
		repaymentCountMap.put("yqdflxbds",yqdflxbds);
		repaymentCountMap.put("yqbdflxbds", yqbdflxbds);
						
		repaymentCountMap.put("yfzchkpcs",yfzchkpcs);
		repaymentCountMap.put("yfyqbjpcs",yfyqbjpcs);
		repaymentCountMap.put("yfyqdflxpcs",yfyqdflxpcs);
		return repaymentCountMap;
	}
	
}