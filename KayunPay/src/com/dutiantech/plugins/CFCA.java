package com.dutiantech.plugins;
import com.ancun.ops.client.OpsClient;
import com.ancun.ops.client.OpsClientConfiguration;
import com.ancun.ops.dto.OpsRequest;
import com.ancun.ops.dto.OpsResponse;
import com.ancun.ops.dto.OpsUserInfo;

public class CFCA{
	
//	private static String apiAddress = "http://pre-openapi.ancun-inc.com";
//	private static String accessKey = "b9c15576a040e73b1633b31b69646efb";
//	private static String secretKey = "7b989d5df6af5653d9e1c4a7a2c631179a1a9d54";
	
	private static String apiAddress = "http://openapi.ancun.com";
	private static String accessKey = "b881ca9e4e5c7a32fa64f388703d4289";
	private static String secretKey = "241c46540a28199d09d896109748991490e449ca";
	
	static OpsClient opsClient = null ;
	
	static{
		//初始化配置
		OpsClientConfiguration conf = new OpsClientConfiguration();
		conf.setConnectionTimeout(50000);
		conf.setMaxErrorRetry(3);
		conf.setMaxConnections(128);
		conf.setSocketTimeout(18000);
		opsClient = new OpsClient(apiAddress,accessKey,secretKey, conf);
	}
	
	
	/**
	 * 用户信息保全
	 * @param userName
	 * @param idCard
	 * @param mobile
	 * @param email
	 * @return
	 */
	public static OpsResponse cfca4user(String userName ,String idCard ,String mobile ,String email){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494001");//业务编号，平台分配
		opsRequest.setFlowNo("X-0875001");//流程编号，平台分配
		
		OpsUserInfo opsUserInfo=new OpsUserInfo();
		opsUserInfo.getOtherInfo().put("userName",userName);
		opsUserInfo.setIdCard(idCard);//身份证或组织机构代码
		opsUserInfo.setMobile(mobile);
		opsUserInfo.setEmail(email);
		opsRequest.getUserInfos().add(opsUserInfo);
		
		return opsClient.save(opsRequest);
		
	}
	
	/**
	 * 项目信息保全
	 * @param prjName					项目名称
	 * @param prjNo						项目编号
	 * @param prjtype					产品类型
	 * @param loanRate					预期年利率
	 * @param loanAmount				项目金额
	 * @param investPeriod				项目期限
	 * @param repayType					还款方式
	 * @param repaymentDate				还款日期   YYYY-MM-DD 
	 * @param releaseTime				发布时间   YYYY-MM-DD HH:MM:SS
	 * @param raiseEndTime				满标结束时间
	 * @param auditPassTime				审核通过时间
	 * @param borrowerName				借款人姓名
	 * @param borrowerIdCardNo			借款人身份证号
	 * @param projectDescription		项目描述
	 * @return
	 */
	public static OpsResponse cfca4loan(String prjName ,String prjNo ,String prjtype ,String loanRate,
			String loanAmount ,String investPeriod ,String repayType ,String repaymentDate ,
			String releaseTime , String raiseEndTime , String auditPassTime,String borrowerName,
			String borrowerIdCardNo,String projectDescription){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494003");//业务编号，平台分配
		opsRequest.setFlowNo("X-0877001");//流程编号，平台分配
		
		//项目保全字段
		opsRequest.putPreserveData("prjName", prjName);//项目名称
		opsRequest.putPreserveData("prjNo", prjNo);//项目编号
		opsRequest.putPreserveData("Prjtype", prjtype);//产品类型
		opsRequest.putPreserveData("loanRate", loanRate);//预期年利率
		opsRequest.putPreserveData("loanAmount", loanAmount);//项目金额
		opsRequest.putPreserveData("investPeriod", investPeriod);//项目期限
		opsRequest.putPreserveData("repayType", repayType);//还款方式
		opsRequest.putPreserveData("RepaymentDate", repaymentDate);//还款日期   YYYY-MM-DD HH:MM:SS
		opsRequest.putPreserveData("ReleaseTime", releaseTime);//发布时间
		opsRequest.putPreserveData("raiseEndTime", raiseEndTime);//满标结束时间
		opsRequest.putPreserveData("auditPassTime", auditPassTime);//审核通过时间
		opsRequest.putPreserveData("borrowerName", borrowerName);//借款人姓名
		opsRequest.putPreserveData("borrowerIdCardNo", borrowerIdCardNo);//借款人身份证号
		opsRequest.putPreserveData("ProjectDescription", projectDescription);//项目描述
		
		return opsClient.save(opsRequest);
		
	}
	
	/**
	 * 投标信息保全
	 * @param prjName					项目名称
	 * @param prjNo						项目编号
	 * @param prjtype					产品类型
	 * @param loanRate					预期年利率
	 * @param loanAmount				项目金额
	 * @param investPeriod				项目期限
	 * @param repayType					还款方式
	 * @param repaymentDate				还款日期   YYYY-MM-DD 
	 * @param releaseTime				发布时间   YYYY-MM-DD HH:MM:SS
	 * @param raiseEndTime				满标结束时间
	 * @param auditPassTime				审核通过时间
	 * @param borrowerName				借款人姓名
	 * @param borrowerIdCardNo			借款人身份证号
	 * @param name						投资人姓名
	 * @param idCardNo					投资人身份证号
	 * @param availableAmount			剩余金额
	 * @param investAmount				投标金额
	 * @param buyTime					投标时间
	 * @param mode						投标方式
	 * @param paySucTime				支付成功时间
	 * @param investNo					交易流水号
	 * @return
	 */
	public static OpsResponse cfca4loanTrace(String prjName ,String prjNo ,String prjtype ,String loanRate,
			String loanAmount ,String investPeriod ,String repayType ,String repaymentDate ,
			String releaseTime , String raiseEndTime , String auditPassTime,String borrowerName,
			String borrowerIdCardNo,String name,String idCardNo , String availableAmount,
			String investAmount ,String buyTime ,String mode, String paySucTime,String investNo,String mobile){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494002");//业务编号，平台分配
		opsRequest.setFlowNo("X-0876001");//流程编号，平台分配
		
		
		//业务数据的用户信息
		OpsUserInfo opsUserInfo=new OpsUserInfo();
		opsUserInfo.getOtherInfo().put("userName",name);
		opsUserInfo.setIdCard(idCardNo);//身份证或组织机构代码
		opsUserInfo.setMobile(mobile);
		//opsUserInfo.setEmail(email);  //暂不推送邮箱
		opsRequest.getUserInfos().add(opsUserInfo);
		
		/**
		 * 项目保全字段
		 */
		
		//投资人信息
		opsRequest.putPreserveData("name", name);//姓名
		opsRequest.putPreserveData("idCardNo", idCardNo);//身份证号
		
		//标书信息
		opsRequest.putPreserveData("prjName", prjName);//项目名称
		opsRequest.putPreserveData("prjNo", prjNo);//项目编号
		opsRequest.putPreserveData("Prjtype", prjtype);//产品类型
		opsRequest.putPreserveData("loanRate", loanRate);//预期年利率
		opsRequest.putPreserveData("loanAmount", loanAmount);//项目金额
		opsRequest.putPreserveData("AvailableAmount", availableAmount);//剩余金额
		opsRequest.putPreserveData("investPeriod", investPeriod);//项目期限
		opsRequest.putPreserveData("repayType", repayType);//还款方式
		opsRequest.putPreserveData("RepaymentDate", repaymentDate);//还款日期   YYYY-MM-DD HH:MM:SS
		opsRequest.putPreserveData("ReleaseTime", releaseTime);//发布时间
		opsRequest.putPreserveData("raiseEndTime", raiseEndTime);//满标结束时间
		opsRequest.putPreserveData("auditPassTime", auditPassTime);//审核通过时间
		opsRequest.putPreserveData("borrowerName", borrowerName);//借款人姓名
		opsRequest.putPreserveData("borrowerIdCardNo", borrowerIdCardNo);//借款人身份证号
		
		//投标信息
		opsRequest.putPreserveData("investAmount", investAmount);//投标金额
		opsRequest.putPreserveData("buyTime", buyTime);//投标时间
		opsRequest.putPreserveData("Mode", mode);//投标方式
		opsRequest.putPreserveData("paySucTime", paySucTime);//支付成功时间
		opsRequest.putPreserveData("investNo", investNo);//交易流水号
		
		return opsClient.save(opsRequest);
		
	}
	
	/**
	 * 投标回款保全
	 * @param recordNo			保全号
	 * @param actRepayTime		实际回款日期
	 * @param actRepayAmount	实际回款金额
	 * @return
	 */
	public static OpsResponse cfca4loanBack(String recordNo ,String actRepayTime ,String actRepayAmount ){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494002");//业务编号，平台分配
		opsRequest.setFlowNo("X-0876002");//流程编号，平台分配
		
		/**
		 * 项目保全字段
		 */
		
		//回款信息
		opsRequest.putPreserveData("recordNo", recordNo);//保全号
		opsRequest.putPreserveData("actRepayTime", actRepayTime);//实际回款日期
		opsRequest.putPreserveData("actRepayAmount", actRepayAmount);//实际回款金额
		
		return opsClient.save(opsRequest);
		
	}
	
	
	/**
	 * 债权转让保全
	 * @param transferName				转让人姓名
	 * @param transferUserName			转让人用户名
	 * @param transferIdCardNo			转让人身份证号
	 * @param transfereeName			受让人姓名
	 * @param transfereeUserName		受让人用户名
	 * @param transfereeIdCardNo		受让人身份证号
	 * @param prjName					原债权项目名称
	 * @param prjNo						原债权项目编号
	 * @param debtCapital				债权本金
	 * @param debtPeriod				债权期限
	 * @param remainPeriod				剩余期限
	 * @param debtor					债务人姓名
	 * @param debtorIdCardNo			债务人身份证号
	 * @param loanRate					债权年化收益率
	 * @param transferAmount			剩余金额
	 * @param remainingPortion			承接价格
	 * @param eachPrice					承接收益
	 * @param transfereePrice			承接实际价款
	 * @param transfereeTime			承接时间
	 * @param paymentDate				下一个回款日
	 * @param incomePaymentWay			收益支付方式
	 * @return
	 */
	public static OpsResponse cfca4transfer(String transferName ,String transferUserName ,String transferIdCardNo ,
			String transfereeName,String transfereeUserName ,String transfereeIdCardNo ,String prjName ,
			String prjNo ,String debtCapital , String debtPeriod , String remainPeriod,String debtor,
			String debtorIdCardNo,String loanRate,String transferAmount , String remainingPortion,String eachPrice,
			String transfereePrice ,String transfereeTime, String paymentDate,String incomePaymentWay){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494006");//业务编号，平台分配
		opsRequest.setFlowNo("X-0880001");//流程编号，平台分配
		
		/**
		 * 项目保全字段
		 */
		
		//转让人/受让人信息
		opsRequest.putPreserveData("transferName", transferName);//转让人姓名
		opsRequest.putPreserveData("transferUserName", transferUserName);//转让人用户名
		opsRequest.putPreserveData("transferIdCardNo", transferIdCardNo);//转让人身份证号
		opsRequest.putPreserveData("transfereeName", transfereeName);//受让人姓名
		opsRequest.putPreserveData("transfereeUserName", transfereeUserName);//受让人用户名
		opsRequest.putPreserveData("transfereeIdCardNo", transfereeIdCardNo);//受让人身份证号
		
		//原债权信息
		opsRequest.putPreserveData("PrjName", prjName);//项目名称
		opsRequest.putPreserveData("PrjNo", prjNo);//项目编号
		opsRequest.putPreserveData("debtCapital", debtCapital);//债权本金
		opsRequest.putPreserveData("debtPeriod", debtPeriod);//债权期限
		opsRequest.putPreserveData("debtor", debtor);//债务人姓名
		opsRequest.putPreserveData("debtorIdCardNo", debtorIdCardNo);//债务人身份证号
		
		//转让债权信息
		opsRequest.putPreserveData("loanRate", loanRate);//债权年化收益率
		opsRequest.putPreserveData("remainPeriod", remainPeriod);//债权剩余期限
		opsRequest.putPreserveData("transferAmount", transferAmount);//剩余金额
		opsRequest.putPreserveData("RemainingPortion", remainingPortion);//承接价格
		opsRequest.putPreserveData("EachPrice", eachPrice);//承接收益
		opsRequest.putPreserveData("transfereePrice", transfereePrice);//承接实际价款
		opsRequest.putPreserveData("transfereeTime", transfereeTime);//承接时间
		opsRequest.putPreserveData("PaymentDate", paymentDate);//下一个回款日
		opsRequest.putPreserveData("incomePaymentWay", incomePaymentWay);//收益支付方式
		
		return opsClient.save(opsRequest);
		
	}

	/**
	 * 债权回款保全
	 * @param recordNo			保全号
	 * @param actRepayTime		实际回款日期
	 * @param actRepayAmount	实际回款金额
	 * @return
	 */
	public static OpsResponse cfca4transferBack(String recordNo ,String actRepayTime ,String actRepayAmount ){
		//封装请求报文
		OpsRequest opsRequest = OpsRequest.create();
		opsRequest.setItemKey("I-0494006");//业务编号，平台分配
		opsRequest.setFlowNo("X-0880002");//流程编号，平台分配
		
		/**
		 * 项目保全字段
		 */
		
		//回款信息
		opsRequest.putPreserveData("recordNo", recordNo);//保全号
		opsRequest.putPreserveData("actRepayTime", actRepayTime);//实际回款日期
		opsRequest.putPreserveData("actRepayAmount", actRepayAmount);//实际回款金额
		
		return opsClient.save(opsRequest);
		
	}
	
}















