package com.dutiantech.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dutiantech.anno.AuthNum;
import com.dutiantech.controller.BaseController;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.FuiouTrace;
import com.dutiantech.model.LoanApply;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.TmpUserReg;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.service.LoanApplyService;
import com.dutiantech.service.LoanInfoService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.jfinal.core.ActionKey;
import com.jfinal.plugin.activerecord.Db;

public class Report extends BaseController {

	@ActionKey("/createRegText")
	@AuthNum(value = 999)
	/**
	 * 开户报备生成text文件
	 */
	public void createRegText() throws Exception {
		int No = 1;
		String nowDate = DateUtil.getNowDate();
		String yesterday = DateUtil.delDay(nowDate, 1); // 前一日日期
		String startTime = "20170924" + "235959";
		String endTime = "20170929" + "170000";
		String record = ""; // 每日记录内容
		String transNo = "";
		String[] buqi = { "0000", "000", "00", "0" };
		String NoString = ""; // Text文档编号，每满2000条记录，新建另一个文档，编号+1
		String fileName = ""; // Text文件名

		// 查询当日开户所需报备数据
		List<Object[]> obj = Db
				.query("SELECT t2.ssn,t1.userName,t1.loginId,t2.trueName,t2.cardid,t1.userMobile,t2.modifyDateTime FROM t_user t1,t_banks_v2 t2 WHERE t1.userCode = t2.userCode AND t2.modifyDateTime>=? AND t2.modifyDateTime<=? AND loginId <> '' AND ssn <> '' AND cardid <> 'f2af8ce442e3c757' ORDER BY modifyDateTime ASC",
						startTime, endTime);

		if (obj == null || obj.size() == 0) { // 如果当日无开户记录，也要报备一个空文件
			transNo = String.valueOf(No);
			NoString = buqi[transNo.length()] + transNo;
			fileName = "P2P_PW10_" + yesterday + "_" + NoString + ".txt";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						CommonUtil.reportPath + fileName));
				writer.write("今日无开户记录");
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < obj.size(); i++) {
				Object[] tmp = obj.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = ((String) tmp[0]).substring(0, 20); // 平台注册流水
				String userName = ((String) tmp[3]).trim(); // 平台用户名
				String tmpLogin_id = (String) tmp[2]; // 存管账户登录用户名
				String login_id = CommonUtil.decryptUserMobile(tmpLogin_id);
				String age = "||"; // 年龄(可为空)
				String trueName = ((String) tmp[3]).trim(); // 户名
				String idType = "0"; // 证件类型 0:居民身份证 1:护照 2:军官证 7:其他
				String idNumber = CommonUtil.decryptUserCardId((String) tmp[4]); // 证件号
				String gender = IdCardUtils.getGenderByIdCard(idNumber);
				if ("M".equals(gender)) {
					gender = "0"; // 性别 男:0
				} else if ("F".equals(gender)) {
					gender = "1"; // 性别 女:1
				}
				String mobileNum = CommonUtil
						.decryptUserMobile((String) tmp[5]); // 手机号
				String address = "||"; // 地址(可为空)
				String userType = "3"; // 用户属性 借款人:1 借款人:2 贷款人 3:借贷和一
				String tmpRegDate = (String) tmp[6];
				String regDate = tmpRegDate.substring(0, 8); // 注册日期
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID
				String opType = "ADD"; // 操作类型 ADD:增加 MOD:修改 DEL:删除
				String remark = "易融恒信_新开用户"; // 备注

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + userName + "|"
						+ login_id + age + trueName + "|" + idType + "|"
						+ idNumber + "|" + gender + "|" + mobileNum + address
						+ userType + "|" + regDate + "|" + payCpnyID + "|"
						+ opType + "|" + remark + "\r\n";

				if (i == obj.size() - 1 || (i % 1999 == 0 && i != 0)) {
					try {
						transNo = String.valueOf(No);
						NoString = buqi[transNo.length()] + transNo;
						fileName = "P2P_PW10_" + yesterday + "_" + NoString
								+ ".txt";
						File f = new File(CommonUtil.reportPath + fileName);
						if (!f.exists()) {
							f.createNewFile();
						}
						OutputStreamWriter write = new OutputStreamWriter(
								new FileOutputStream(f));
						BufferedWriter writer = new BufferedWriter(write);
						writer.write(record);
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@ActionKey("/aaa")
	@AuthNum(value = 999)
	/**
	 */
	public void aaa() throws Exception {
		// TODO Auto-generated method stub
		String loanCode = getPara("loanCode");
		Report.createProjectText(loanCode);
	}

	// @ActionKey("/createProjectText")
	/**
	 * 项目报备生成text文件
	 * 
	 * @param loanCode
	 *            借款标编号
	 */
	public static void createProjectText(String loanCode) throws Exception {
		int No = 1;
		String nowDate = DateUtil.getNowDate();
		String transNo = "";
		String[] buqi = { "0000", "000", "00", "0" };
		String NoString = ""; // text文档编号
		String fileName = ""; // text文件名

		// 客服发标后，立即报备
		LoanInfoService loanInfoService = new LoanInfoService();
		LoanInfo loanInfo = loanInfoService.findById(loanCode);

		String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
		String mchnt_txn_ssn = loanInfo.getStr("ssn"); // 项目流水号
		String proNo = loanInfo.getStr("loanNo"); // 项目编号

		String tmpLoanType = loanInfo.getStr("loanType");
		String loanType = ""; // 借款类型 0:抵押 1:担保 2:信用标 3:净值标 4:流转标 5:秒标 6:其他
		if ("B".equals(tmpLoanType)) {
			loanType = "0";
		} else if ("C".equals(tmpLoanType)) {
			loanType = "1";
		} else if ("A".equals(tmpLoanType)) {
			loanType = "2";
		} else if ("D".equals(tmpLoanType)) {
			loanType = "4";
		} else {
			loanType = "6";
		}

		String loanTitle = loanInfo.getStr("loanTitle"); // 项目标题
		String recommend = "||"; // 推荐机构(可为空)

		String tmpLoanUsedType = loanInfo.getStr("loanUsedType"); // 借款用途
		String LoanUsedType = "";
		if ("A".equals(tmpLoanUsedType)) {
			LoanUsedType = "短期周转";
		} else if ("B".equals(tmpLoanUsedType)) {
			LoanUsedType = "个人消费";
		} else if ("C".equals(tmpLoanUsedType)) {
			LoanUsedType = "投资创业";
		} else if ("D".equals(tmpLoanUsedType)) {
			LoanUsedType = "购车借款";
		} else if ("E".equals(tmpLoanUsedType)) {
			LoanUsedType = "装修借款";
		} else if ("F".equals(tmpLoanUsedType)) {
			LoanUsedType = "婚礼筹备";
		} else if ("G".equals(tmpLoanUsedType)) {
			LoanUsedType = "教育培训";
		} else if ("H".equals(tmpLoanUsedType)) {
			LoanUsedType = "医疗支出";
		} else if ("I".equals(tmpLoanUsedType)) {
			LoanUsedType = "其他借款";
		} else if ("J".equals(tmpLoanUsedType)) {
			LoanUsedType = "购房借款";
		} else {
			LoanUsedType = "其他用途";
		}

		long loanAmount = loanInfo.getLong("loanAmount"); // 借款金额
		long totalRate = loanInfo.getInt("rateByYear")
				+ loanInfo.getInt("rewardRateByYear")
				+ loanInfo.getInt("benefits4new"); // 预期总收益 年利率 + 奖励年利率
		String loanName = loanInfo.getStr("loanTitle"); // 产品名称 与项目标题一样

		String tmpRefundType = loanInfo.getStr("refundType");
		String refundType = ""; // 还款方式 0:一次性还本付息 1:先息后本 2:等额本息/等额本金 3:其他
		if ("A".equals(tmpRefundType)) {
			refundType = "2";
		} else if ("B".equals(tmpRefundType)) {
			refundType = "1";
		} else {
			refundType = "3";
		}

		String loanApplyDate = loanInfo.getStr("releaseDate"); // 筹标起始日:即项目起始日
		if ("".equals(loanApplyDate) || loanApplyDate == null) {
			loanApplyDate = DateUtil.getNowDate();
		}
		String loanLimitDay = DateUtil.subDay(loanApplyDate, -3); // 借款期限:即项目期限,项目到期的最后一天

		long eachAmt = 100; // 每份投标金额 一元 单位:分
		int minCopies = 1; // 最小投标份数:1份起

		long maxAmt = 0; // 最大投标金额: 单位：分(可以为借款金额的100%)
		int isNew = loanInfo.getInt("benefits4new");
		if (isNew > 0) {
			maxAmt = 1000000;
		} else {
			maxAmt = loanAmount;
		}

		String loanUserTrueName = loanInfo.getStr("userName"); // 借款人平台用户名:存管系统的开户姓名(企业户就是开户企业名称)
		User user = User.userDao.findById(loanInfo.getStr("userCode"));
		String loanUserLoginId = CommonUtil.decryptUserMobile(user
				.getStr("loginId")); // 借款人存管账户登录名
		String loanDesc = "";
		String loanNo = loanInfo.getStr("loanNo");
		int tmpLoanNo = Integer.parseInt(loanNo);
		LoanApply loanApply = new LoanApplyService().findById(tmpLoanNo);
		String tmpLoanDesc = loanApply.getStr("loanDesc");
		JSONArray array = JSONArray.parseArray(tmpLoanDesc);
		for (int i = 0; i < array.size(); i++) {
			JSONObject json = array.getJSONObject(i);
			String title = json.getString("title");
			String content = json.getString("content");
			if (title.indexOf("报备") != -1) {
				loanDesc = content.replaceAll("\n", "");
				break;
			}
		}

		long fees = 0; // 费用项
		String loanStatus = "||"; // 筹集情况(可为空)
		int loanMonths = loanInfo.getInt("loanTimeLimit"); // 还款期数
		String riskee = "||"; // 备用金额(可为空)
		String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID
		String record = mchnt_cd + "|" + mchnt_txn_ssn + "|" + proNo + "|"
				+ loanType + "|" + loanTitle + recommend + LoanUsedType + "|"
				+ loanAmount + "|" + totalRate + "|" + loanName + "|"
				+ refundType + "|" + loanLimitDay + "|" + loanApplyDate + "|"
				+ eachAmt + "|" + minCopies + "|" + maxAmt + "|"
				+ loanUserTrueName + "|" + loanUserLoginId + "|" + loanDesc
				+ "|" + fees + loanStatus + loanMonths + riskee + payCpnyID
				+ "\r\n";

		for (int i = 0; i < 999; i++) {
			transNo = String.valueOf(No);
			NoString = buqi[transNo.length()] + transNo;
			fileName = "P2P_PWXM_" + nowDate + "_" + NoString + ".txt";
			File file = new File(CommonUtil.reportPath + fileName);
			if (file.exists()) {
				No++;
				i++;
				continue;
			} else {
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(
							CommonUtil.reportPath + fileName));
					writer.write(record);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	}

	@ActionKey("/createProjectTextIfNull")
	/**
	 * 前一日无项目发布,报备一份空文档
	 */
	public void createProjectTextIfNull() {
		String nowDate = DateUtil.getNowDate();
		String yesterday = DateUtil.delDay(nowDate, 1); // 前一日日期
		List<LoanInfo> list = LoanInfo.loanInfoDao.find(
				"SELECT * FROM t_loan_info WHERE releaseDate = ?", yesterday);
		if (list.size() < 1 || list == null) {
			String fileName = "P2P_PWXM_" + yesterday + "_" + "0001.txt";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						CommonUtil.reportPath + fileName));
				writer.write("今日无项目记录");
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@ActionKey("/createProjectText")
	/**
	 * 前一日无项目发布,报备一份空文档
	 */
	public void createProjectText() {
		int No = 1;
		String record = ""; // 每日记录内容
		String transNo = "";
		String[] buqi = { "0000", "000", "00", "0" };
		String NoString = ""; // Text文档编号，每满2000条记录，新建另一个文档，编号+1
		String fileName = ""; // Text文件名

		String nowDate = DateUtil.getNowDate();
		String yesterday = DateUtil.delDay(nowDate, 1); // 前一日日期
		String startDate = "20170924";
		String endDate = "20170929";
		List<LoanInfo> list = LoanInfo.loanInfoDao
				.find("SELECT * FROM t_loan_info WHERE releaseDate BETWEEN ? AND ? AND createDate >= 20170816",
						startDate, endDate);
		if (list.size() < 1 || list == null) {
			fileName = "P2P_PWXM_" + yesterday + "_" + "0001.txt";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						CommonUtil.reportPath + fileName));
				writer.write("今日无项目记录");
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				LoanInfo loanInfo = list.get(i);
				User user = User.userDao.findById(loanInfo.getStr("userCode"));
				String loanUserLoginId = "";
				try {
					loanUserLoginId = CommonUtil.decryptUserMobile(user
							.getStr("loginId"));
					if (loanUserLoginId == null) {
						continue;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = loanInfo.getStr("ssn"); // 项目流水号
				String proNo = loanInfo.getStr("loanNo"); // 项目编号

				String tmpLoanType = loanInfo.getStr("loanType");
				String loanType = ""; // 借款类型 0:抵押 1:担保 2:信用标 3:净值标 4:流转标 5:秒标
										// 6:其他
				if ("B".equals(tmpLoanType)) {
					loanType = "0";
				} else if ("C".equals(tmpLoanType)) {
					loanType = "1";
				} else if ("A".equals(tmpLoanType)) {
					loanType = "2";
				} else if ("D".equals(tmpLoanType)) {
					loanType = "4";
				} else {
					loanType = "6";
				}

				String loanTitle = loanInfo.getStr("loanTitle"); // 项目标题
				String recommend = "||"; // 推荐机构(可为空)

				String tmpLoanUsedType = loanInfo.getStr("loanUsedType"); // 借款用途
				String LoanUsedType = "";
				if ("A".equals(tmpLoanUsedType)) {
					LoanUsedType = "短期周转";
				} else if ("B".equals(tmpLoanUsedType)) {
					LoanUsedType = "个人消费";
				} else if ("C".equals(tmpLoanUsedType)) {
					LoanUsedType = "投资创业";
				} else if ("D".equals(tmpLoanUsedType)) {
					LoanUsedType = "购车借款";
				} else if ("E".equals(tmpLoanUsedType)) {
					LoanUsedType = "装修借款";
				} else if ("F".equals(tmpLoanUsedType)) {
					LoanUsedType = "婚礼筹备";
				} else if ("G".equals(tmpLoanUsedType)) {
					LoanUsedType = "教育培训";
				} else if ("H".equals(tmpLoanUsedType)) {
					LoanUsedType = "医疗支出";
				} else if ("I".equals(tmpLoanUsedType)) {
					LoanUsedType = "其他借款";
				} else if ("J".equals(tmpLoanUsedType)) {
					LoanUsedType = "购房借款";
				} else {
					LoanUsedType = "其他用途";
				}

				long loanAmount = loanInfo.getLong("loanAmount"); // 借款金额
				long totalRate = loanInfo.getInt("rateByYear")
						+ loanInfo.getInt("rewardRateByYear")
						+ loanInfo.getInt("benefits4new"); // 预期总收益 年利率 + 奖励年利率
				String loanName = loanInfo.getStr("loanTitle"); // 产品名称 与项目标题一样

				String tmpRefundType = loanInfo.getStr("refundType");
				String refundType = ""; // 还款方式 0:一次性还本付息 1:先息后本 2:等额本息/等额本金
										// 3:其他
				if ("A".equals(tmpRefundType)) {
					refundType = "2";
				} else if ("B".equals(tmpRefundType)) {
					refundType = "1";
				} else {
					refundType = "3";
				}

				String loanApplyDate = loanInfo.getStr("releaseDate"); // 筹标起始日:即项目起始日
				if ("".equals(loanApplyDate) || loanApplyDate == null) {
					loanApplyDate = DateUtil.getNowDate();
				}
				String loanLimitDay = DateUtil.subDay(loanApplyDate, -3); // 借款期限:即项目期限,项目到期的最后一天

				long eachAmt = 100; // 每份投标金额 一元 单位:分
				int minCopies = 1; // 最小投标份数:1份起

				long maxAmt = 0; // 最大投标金额: 单位：分(可以为借款金额的100%)
				int isNew = loanInfo.getInt("benefits4new");
				if (isNew > 0) {
					maxAmt = 1000000;
				} else {
					maxAmt = loanAmount;
				}

				String loanUserTrueName = loanInfo.getStr("userName").trim(); // 借款人平台用户名:存管系统的开户姓名(企业户就是开户企业名称)

				String loanDesc = "";
				String loanNo = loanInfo.getStr("loanNo");
				int tmpLoanNo = Integer.parseInt(loanNo);
				LoanApply loanApply = new LoanApplyService()
						.findById(tmpLoanNo);
				String tmpLoanDesc = loanApply.getStr("loanDesc");
				JSONArray array = JSONArray.parseArray(tmpLoanDesc);
				for (int k = 0; k < array.size(); k++) {
					JSONObject json = array.getJSONObject(k);
					String title = json.getString("title");
					String content = json.getString("content");
					if (title.indexOf("报备") != -1) {
						loanDesc = content.replaceAll("\n", "");
						break;
					}
				}

				long fees = 0; // 费用项
				String loanStatus = "||"; // 筹集情况(可为空)
				int loanMonths = loanInfo.getInt("loanTimeLimit"); // 还款期数
				String riskee = "||"; // 备用金额(可为空)
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID
				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + proNo + "|"
						+ loanType + "|" + loanTitle + recommend + LoanUsedType
						+ "|" + loanAmount + "|" + totalRate + "|" + loanName
						+ "|" + refundType + "|" + loanLimitDay + "|"
						+ loanApplyDate + "|" + eachAmt + "|" + minCopies + "|"
						+ maxAmt + "|" + loanUserTrueName + "|"
						+ loanUserLoginId + "|" + loanDesc + "|" + fees
						+ loanStatus + loanMonths + riskee + payCpnyID + "\r\n";

				if (i == list.size() - 1 || (i % 1999 == 0 && i != 0)) {
					try {
						transNo = String.valueOf(No);
						NoString = buqi[transNo.length()] + transNo;
						fileName = "P2P_PWXM_" + yesterday + "_" + NoString
								+ ".txt";
						File f = new File(CommonUtil.reportPath + fileName);
						if (!f.exists()) {
							f.createNewFile();
						}
						OutputStreamWriter write = new OutputStreamWriter(
								new FileOutputStream(f));
						BufferedWriter writer = new BufferedWriter(write);
						writer.write(record);
						writer.flush();
						writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	@ActionKey("/createTradeText")
	/**
	 * 报备交易
	 */
	public void createTradeText() throws Exception {
		int No = 20;
		String nowDate = DateUtil.getNowDate();
		String yesterday = DateUtil.delDay(nowDate, 1); // 前一日日期
		String startTime = "20170924" + "235959";
		String endTime = "20170929" + "170000";
		String transNo = "";
		String[] buqi = { "0000", "000", "00", "0" };
		String NoString = ""; // Text文档编号，每满2000条记录，新建另一个文档，编号+1
		String fileName = ""; // Text文件名
		String record = ""; // 记录数据
		LoanInfoService loanInfoService = new LoanInfoService();

		// 投标交易(冻结)
		List<FuiouTrace> listDJ = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType = 'C' AND traceDate >= ? AND traceDate <= ? ORDER BY traceDate ASC",
						startTime, endTime);
		if (!(listDJ == null || listDJ.size() == 0)) {
			for (int i = 0; i < listDJ.size(); i++) {
				FuiouTrace tmp = listDJ.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWDJ"; // 交易类型:冻结
				// LoanInfo loanInfo = loanInfoService.findById(tmp
				// .getStr("loanCode"));
				String tradeNo = "FuiouF0001";// loanInfo.getStr("loanNo");"FuiouF0001"
												// // 借款标编号
				String contractNo = "FuiouF0001";// loanInfo.getStr("contractNo");"FuiouF0001"
													// // 合同编号

				String outLogin_id = tmp.getStr("InLoginId"); // 出账人存管系统用户名
				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名
				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = ""; // 入账人存管系统用户名
				String inTrueName = ""; // 入账人存管系统真实姓名
				String preTrueName = ""; // (原)投资人用户名
				String preLogin_id = ""; // (原)投资人存管账户系统登陆用户名
				String businessType = "0"; // 投标
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ "|" + preTrueName + "|" + preLogin_id + "|"
						+ businessType + "|" + payCpnyID + "\r\n";
				transNo = String.valueOf(No);
				NoString = buqi[transNo.length()] + transNo;
				fileName = "P2P_PWJY_" + yesterday + "_" + NoString + ".txt";
				if (i == listDJ.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("冻结记录完毕");

		// 投标交易(解冻)
		List<FuiouTrace> listJD = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType = 'D' AND traceDate >= ? AND traceDate <= ? ORDER BY traceDate ASC",
						startTime, endTime);
		if (!(listJD == null || listJD.size() == 0)) {
			for (int i = 0; i < listJD.size(); i++) {
				FuiouTrace tmp = listJD.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWJD"; // 交易类型:解冻
				// LoanInfo loanInfo = loanInfoService.findById(tmp
				// .getStr("loanCode"));
				String tradeNo = "FuiouF0001";// loanInfo.getStr("loanNo");"FuiouF0001"
												// // 借款标编号
				String contractNo = "FuiouF0001";// loanInfo.getStr("contractNo");"FuiouF0001"
													// // 合同编号

				String outLogin_id = tmp.getStr("InLoginId"); // 出账人存管系统用户名
				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名
				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = ""; // 入账人存管系统用户名
				String inTrueName = ""; // 入账人存管系统真实姓名
				String preTrueName = ""; // (原)投资人用户名
				String preLogin_id = ""; // (原)投资人存管账户系统登陆用户名
				String businessType = "4"; // 其他
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ "|" + preTrueName + "|" + preLogin_id + "|"
						+ businessType + "|" + payCpnyID + "\r\n";
				transNo = String.valueOf(No);
				NoString = buqi[transNo.length()] + transNo;
				fileName = "P2P_PWJY_" + yesterday + "_" + NoString + ".txt";
				if (i == listJD.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("解冻记录完毕");

		// 满标放款
		List<FuiouTrace> manBiao = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType = 'R' AND traceDate >= ? AND traceDate <= ? AND loanCode <> '' ORDER BY traceDate ASC",
						startTime, endTime);
		if (!(manBiao == null || manBiao.size() == 0)) {
			for (int i = 0; i < manBiao.size(); i++) {
				FuiouTrace tmp = manBiao.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWDZ"; // 交易类型:动账
				LoanInfo loanInfo = loanInfoService.findById(tmp
						.getStr("loanCode"));
				String tradeNo = loanInfo.getStr("loanNo");// loanInfo.getStr("loanNo");
															// //"FuiouF0001";//
															// 借款标编号
				String contractNo = loanInfo.getStr("contractNo");// loanInfo.getStr("contractNo");"FuiouF0001";//
																	// //合同编号

				String outLogin_id = tmp.getStr("OutLoginId"); // 出账人存管系统用户名
				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("OutUserCode"));
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名
				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = tmp.getStr("InLoginId"); // 入账人存管系统用户名
				UserInfo userInfo2 = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String inTrueName = userInfo2.getStr("userCardName").trim(); // 入账人存管系统真实姓名
				String preTrueName = "||"; // (原)投资人用户名
				String preLogin_id = "|"; // (原)投资人存管账户系统登陆用户名
				String businessType = "1"; // 满标
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ preTrueName + preLogin_id + businessType + "|"
						+ payCpnyID + "\r\n";
				transNo = String.valueOf(No);
				NoString = buqi[transNo.length()] + transNo;
				fileName = "P2P_PWJY_" + yesterday + "_" + NoString + ".txt";
				if (i == manBiao.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("满标记录完毕");
		// 债权转让
		List<LoanTransfer> transfers = LoanTransfer.loanTransferDao
				.find("select * from t_loan_transfer where transState = 'B' and CONVERT(CONCAT(gotDate,gotTime),SIGNED) >= ? AND CONVERT(CONCAT(gotDate,gotTime),SIGNED) <= ? ORDER BY uid ASC",
						startTime, endTime);
		if (!(transfers == null || transfers.size() == 0)) {
			for (int i = 0; i < transfers.size(); i++) {
				LoanTransfer tmp = transfers.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String loanCodeString = tmp.getStr("transCode");
				String inUserCodeString = tmp.getStr("payUserCode");
				String outUserCodeString = tmp.getStr("gotUserCode");
				FuiouTrace fuiouTrace = FuiouTrace.fuiouTraceDao
						.findFirst(
								"SELECT * FROM t_fuiou_trace WHERE loanCode = ? AND InUserCode = ? AND OutUserCode = ?",
								loanCodeString, inUserCodeString,
								outUserCodeString);
				String mchnt_txn_ssn = fuiouTrace.getStr("traceCode"); // 项目流水号
				String tradeDate = tmp.getStr("gotDate"); // 交易日期
				String tradeType = "PWDZ"; // 交易类型:动账
				String tradeNo = tmp.getStr("loanNo");// 借款标编号 "FuiouF0001";

				LoanInfo loanInfo = loanInfoService.findById(tmp
						.getStr("loanCode"));
				String contractNo = loanInfo.getStr("contractNo");// 合同编号"FuiouF0001";

				User user = User.userDao.findById(tmp.getStr("gotUserCode"));
				UserInfo userInfo = UserInfo.userInfoDao.findById(user
						.getStr("userCode"));
				String outLogin_id = CommonUtil.decryptUserMobile(user
						.getStr("loginId")); // 出账人存管系统用户名
				String outTrueName = userInfo.getStr("userCardName"); // 出账人存管系统真实姓名

				long amt = tmp.getInt("transAmount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费

				User user2 = User.userDao.findById(tmp.getStr("payUserCode"));
				UserInfo userInfo2 = UserInfo.userInfoDao.findById(tmp
						.getStr("payUserCode"));
				String inLogin_id = CommonUtil.decryptUserMobile(user2
						.getStr("loginId")); // 入账人存管系统用户名
				String inTrueName = userInfo2.getStr("userCardName"); // 入账人存管系统真实姓名
				String preTrueName = inTrueName; // (原)投资人用户名
				String preLogin_id = inLogin_id; // (原)投资人存管账户系统登陆用户名
				String businessType = "2"; // 债权转让
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ "|" + preTrueName + "|" + preLogin_id + "|"
						+ businessType + "|" + payCpnyID + "\r\n";

				if (i == transfers.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("债转记录完毕");

		// 流标
		List<LoanTrace> liubiao = LoanTrace.loanTraceDao
				.find("SELECT * FROM t_loan_trace WHERE loanDateTime >= ? AND loanDateTime <= ? AND traceState = 'H' ORDER BY uid ASC",
						startTime, endTime);
		if (!(liubiao == null || liubiao.size() == 0)) {
			for (int i = 0; i < liubiao.size(); i++) {
				LoanTrace tmp = (LoanTrace) liubiao.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode").substring(0, 20); // 项目流水号
				String tradeDate = tmp.getStr("loanDateTime").substring(0, 8); // 交易日期
				String tradeType = "PWJD"; // 交易类型:冻结
				String tradeNo = tmp.getStr("loanNo"); // 借款标编号

				LoanInfo loanInfo = loanInfoService.findById(tmp
						.getStr("loanCode"));
				String contractNo = loanInfo.getStr("contractNo"); // 合同编号

				String outLogin_id = "||"; // 出账人存管系统用户名
				String outTrueName = "|"; // 出账人存管系统真实姓名
				long amt = tmp.getLong("payAmount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = "||"; // 入账人存管系统用户名
				String inTrueName = "|"; // 入账人存管系统真实姓名
				String preTrueName = "|"; // (原)投资人用户名
				String preLogin_id = "|"; // (原)投资人存管账户系统登陆用户名
				String businessType = "5"; // 流标
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ outLogin_id + outTrueName + amt + "|" + fees
						+ inLogin_id + inTrueName + preTrueName + preLogin_id
						+ businessType + "|" + payCpnyID + "\r\n";

				if (i == liubiao.size() - 1) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("流标记录完毕");

		// 平台手续费
		List<FuiouTrace> feeList = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType IN('V','I') AND traceDate BETWEEN ? AND ?",
						startTime, endTime);
		if (!(feeList == null || feeList.size() == 0)) {
			for (int i = 0; i < feeList.size(); i++) {
				FuiouTrace tmp = feeList.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 项目流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWDZ"; // 交易类型:动账
				String tradeNo = ""; // 借款标编号
				String contractNo = ""; // 合同编号
				if ("I".equals(tmp.getStr("traceType"))) {
					tradeNo = "FuiouF0001";
					contractNo = "FuiouF0001";
				}
				if ("V".equals(tmp.getStr("traceType"))) {
					LoanTransfer loanTransfer = LoanTransfer.loanTransferDao
							.findById(tmp.getStr("loanCode"));
					LoanInfo loan = LoanInfo.loanInfoDao.findById(loanTransfer
							.getStr("loanCode"));
					tradeNo = loan.getStr("loanNo");
					contractNo = loan.getStr("contractNo");
				}

				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("OutUserCode"));
				String outLogin_id = tmp.getStr("OutLoginId"); // 出账人存管系统用户名
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名

				if (outTrueName == null || "".equals(outTrueName)) {
					continue;
				}

				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = CommonUtil.OUTCUSTNO; // 入账人存管系统用户名--划拨到吴总账户
				UserInfo userInfo2 = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String inTrueName = userInfo2.getStr("userCardName").trim(); // 入账人存管系统真实姓名

				if (inTrueName == null || "".equals(inTrueName)) {
					continue;
				}

				String preTrueName = "||"; // (原)投资人用户名
				String preLogin_id = "|"; // (原)投资人存管账户系统登陆用户名
				String businessType = "6"; // 平台手续费
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ preTrueName + preLogin_id + businessType + "|"
						+ payCpnyID + "\r\n";

				if (i == feeList.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("手续费记录完毕");

		// 平台代偿
		List<FuiouTrace> repay = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType IN('HKB','HKL') AND traceDate >= ? AND traceDate <= ? AND OutUserCode <> ''",
						startTime, endTime);
		if (!(repay == null || repay.size() == 0)) {
			for (int i = 0; i < repay.size(); i++) {
				FuiouTrace tmp = repay.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 项目流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWDZ"; // 交易类型:冻结
				LoanInfo loanInfo = loanInfoService.findById(tmp
						.getStr("loanCode"));
				String tradeNo = loanInfo.getStr("loanNo");// 借款标编号"FuiouF0001";
															// 
				String contractNo = loanInfo.getStr("contractNo");// 合同编号"FuiouF0001";
																	// 

				String outLogin_id = tmp.getStr("OutLoginId"); // 出账人存管系统用户名
				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("OutUserCode"));
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名

				if (outTrueName == null || "".equals(outTrueName)) {
					continue;
				}

				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费
				String inLogin_id = tmp.getStr("InLoginId"); // 入账人存管系统用户名
				UserInfo userInfo2 = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String inTrueName = userInfo2.getStr("userCardName").trim(); // 入账人存管系统真实姓名

				if (inTrueName == null || "".equals(inTrueName)) {
					continue;
				}

				String preTrueName = "||"; // (原)投资人用户名
				String preLogin_id = "|"; // (原)投资人存管账户系统登陆用户名
				String businessType = "3"; // 还款
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ preTrueName + preLogin_id + businessType + "|"
						+ payCpnyID + "\r\n";

				if (i == repay.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

			}
		}
		System.out.println("代偿记录完毕");
		// 平台营销
		List<FuiouTrace> list = FuiouTrace.fuiouTraceDao
				.find("SELECT * FROM t_fuiou_trace WHERE traceType = 'Q' AND traceDate BETWEEN ? AND ?",
						startTime, endTime);
		if (!(list == null || list.size() == 0)) {
			for (int i = 0; i < list.size(); i++) {
				FuiouTrace tmp = list.get(i);
				String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
				String mchnt_txn_ssn = tmp.getStr("traceCode"); // 流水号
				String tradeDate = tmp.getStr("traceDate").substring(0, 8); // 交易日期
				String tradeType = "PWDZ"; // 交易类型:动账
				String tradeNo = "FuiouF0001"; // 项目编号
				String contractNo = "FuiouF0001"; // 合同编号
				String outLogin_id = tmp.getStr("OutLoginId"); // 出账人存管系统用户名
				UserInfo userInfo = UserInfo.userInfoDao.findById(tmp
						.getStr("OutUserCode"));
				String outTrueName = userInfo.getStr("userCardName").trim(); // 出账人存管系统真实姓名

				if (outTrueName == null || "".equals(outTrueName)) {
					continue;
				}

				long amt = tmp.getLong("amount"); // 交易金额,单位(分)
				String fees = "0"; // 手续费

				UserInfo userInfo2 = UserInfo.userInfoDao.findById(tmp
						.getStr("InUserCode"));
				String inLogin_id = tmp.getStr("InLoginId"); // 入账人存管系统用户名
				String inTrueName = userInfo2.getStr("userCardName").trim(); // 入账人存管系统真实姓名

				if (inTrueName == null || "".equals(inTrueName)) {
					continue;
				}

				String preTrueName = "||"; // (原)投资人用户名
				String preLogin_id = "|"; // (原)投资人存管账户系统登陆用户名
				String businessType = "8"; // 平台手续费
				String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID

				record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + tradeDate
						+ "|" + tradeType + "|" + tradeNo + "|" + contractNo
						+ "|" + outLogin_id + "|" + outTrueName + "|" + amt
						+ "|" + fees + "|" + inLogin_id + "|" + inTrueName
						+ preTrueName + preLogin_id + businessType + "|"
						+ payCpnyID + "\r\n";
				if (i == list.size() - 1 || (i % 1999 == 0 && i != 0)) {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PWJY_" + yesterday + "_" + NoString
							+ ".txt";
					try {
						BufferedWriter writer = new BufferedWriter(
								new FileWriter(CommonUtil.reportPath + fileName));
						writer.write(record);
						writer.flush();
						writer.close();
						record = "";
						No++;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		System.out.println("平台营销记录完毕");
	}

	@ActionKey("/piLiang")
	@AuthNum(value = 999)
	/**
	 * 开户报备生成text文件
	 */
	public void piLiang() throws Exception {
		int No = 1;
		String nowDate = DateUtil.getNowDate();
		String record = ""; // 每日记录内容
		String transNo = "";
		String[] buqi = { "0000", "000", "00", "0" };
		String NoString = ""; // Text文档编号，每满2000条记录，新建另一个文档，编号+1
		String fileName = ""; // Text文件名

		// 查询当日开户所需报备数据
		List<TmpUserReg> list = TmpUserReg.tmpuserregDao
				.find("SELECT  * FROM t_tmp_userReg WHERE issucces <> 'C' GROUP BY userCode");

		for (int i = 0; i < list.size(); i++) {
			TmpUserReg tmp = list.get(i);
			String mchnt_cd = CommonUtil.MCHNT_CD; // 商户号
			String mchnt_txn_ssn = tmp.getStr("ssn"); // 平台注册流水
			String userName = tmp.getStr("userName"); // 平台用户名
			String login_id = tmp.getStr("mobile"); // 存管账户登录用户名
			String age = "||"; // 年龄(可为空)
			String trueName = tmp.getStr("userName"); // 户名
			String idType = "0"; // 证件类型 0:居民身份证 1:护照 2:军官证 7:其他
			String userCode = tmp.getStr("userCode");
			BanksV2 banksV2 = BanksV2.bankV2Dao.findById(userCode);
			String idNumber = CommonUtil.decryptUserCardId(banksV2
					.getStr("cardid")); // 证件号
			String gender = IdCardUtils.getGenderByIdCard(idNumber);
			if ("M".equals(gender)) {
				gender = "0"; // 性别 男:0
			} else if ("F".equals(gender)) {
				gender = "1"; // 性别 女:1
			}
			String mobileNum = tmp.getStr("mobile"); // 手机号
			String address = "||"; // 地址(可为空)
			String userType = "3"; // 用户属性 借款人:1 借款人:2 贷款人 3:借贷和一
			String regDate = DateUtil.getNowDate(); // 注册日期
			String payCpnyID = CommonUtil.payCpnyID; // 第三方支付公司ID
			String opType = "ADD"; // 操作类型 ADD:增加 MOD:修改 DEL:删除
			String remark = "易融恒信_新开用户"; // 备注

			record += mchnt_cd + "|" + mchnt_txn_ssn + "|" + userName + "|"
					+ login_id + age + trueName + "|" + idType + "|" + idNumber
					+ "|" + gender + "|" + mobileNum + address + userType + "|"
					+ regDate + "|" + payCpnyID + "|" + opType + "|" + remark
					+ "\r\n";

			if (i == list.size() - 1 || (i % 1999 == 0 && i != 0)) {
				try {
					transNo = String.valueOf(No);
					NoString = buqi[transNo.length()] + transNo;
					fileName = "P2P_PW10_" + nowDate + "_" + NoString + ".txt";
					File f = new File(CommonUtil.reportPath + fileName);
					if (!f.exists()) {
						f.createNewFile();
					}
					OutputStreamWriter write = new OutputStreamWriter(
							new FileOutputStream(f));
					BufferedWriter writer = new BufferedWriter(write);
					writer.write(record);
					writer.flush();
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	//
	@ActionKey("/projectsBMD")
	@AuthNum(value = 999)
	/**
	 * 项目白名单报备生成excel文件
	 */
	public void projectsBMD() throws Exception {
		File file = new File("E:report/PWXM_20170815_0001.xls");
		file.createNewFile();
		WritableWorkbook workbook = Workbook.createWorkbook(file);
		WritableSheet sheet = workbook.createSheet("项目白名单", 0);
		Label label1 = new Label(0, 0, "商户号");
		Label label2 = new Label(1, 0, "平台流水号");
		Label label3 = new Label(2, 0, "项目编号");
		Label label4 = new Label(3, 0, "借款类型");
		Label label5 = new Label(4, 0, "借款标题");
		Label label6 = new Label(5, 0, "推荐机构");
		Label label7 = new Label(6, 0, "借款用途");
		Label label8 = new Label(7, 0, "借款金额");
		Label label9 = new Label(8, 0, "预期收益");
		Label label10 = new Label(9, 0, "产品名称");
		Label label11 = new Label(10, 0, "还款方式");
		Label label12 = new Label(11, 0, "借款期限");
		Label label13 = new Label(12, 0, "筹标起始日");
		Label label14 = new Label(13, 0, "每份投标金额");
		Label label15 = new Label(14, 0, "最低投标份数");
		Label label16 = new Label(15, 0, "最多投标金额");
		Label label17 = new Label(16, 0, "借款人平台用户名");
		Label label18 = new Label(17, 0, "借款人金账户用户名");
		Label label19 = new Label(18, 0, "借款人项目概述");
		Label label20 = new Label(19, 0, "费用项");
		Label label21 = new Label(20, 0, "筹集情况");
		Label label22 = new Label(21, 0, "还款期数");
		Label label23 = new Label(22, 0, "备用金额");
		Label label24 = new Label(23, 0, "第三方支付公司ID");
		sheet.addCell(label1);
		sheet.addCell(label2);
		sheet.addCell(label3);
		sheet.addCell(label4);
		sheet.addCell(label5);
		sheet.addCell(label6);
		sheet.addCell(label7);
		sheet.addCell(label8);
		sheet.addCell(label9);
		sheet.addCell(label10);
		sheet.addCell(label11);
		sheet.addCell(label12);
		sheet.addCell(label13);
		sheet.addCell(label14);
		sheet.addCell(label15);
		sheet.addCell(label16);
		sheet.addCell(label17);
		sheet.addCell(label18);
		sheet.addCell(label19);
		sheet.addCell(label20);
		sheet.addCell(label21);
		sheet.addCell(label22);
		sheet.addCell(label23);
		sheet.addCell(label24);
		List<LoanInfo> list = LoanInfo.loanInfoDao
				.find("SELECT * FROM t_loan_info WHERE reciedCount < loanTimeLimit ORDER BY lastPayLoanDateTime DESC");
		for (int i = 0; i < list.size(); i++) {
			LoanInfo tmp = list.get(i);
			Label label_1 = new Label(0, i + 1, CommonUtil.MCHNT_CD);
			Label label_2 = new Label(1, i + 1, tmp.getStr("loanCode")
					.substring(0, 20));
			Label label_3 = new Label(2, i + 1, tmp.getStr("loanNo"));
			String tmpLoanType = tmp.getStr("loanType");
			String loanType = ""; // 借款类型 0:抵押 1:担保 2:信用标 3:净值标 4:流转标 5:秒标 6:其他
			if ("B".equals(tmpLoanType)) {
				loanType = "0";
			} else if ("C".equals(tmpLoanType)) {
				loanType = "1";
			} else if ("A".equals(tmpLoanType)) {
				loanType = "2";
			} else if ("D".equals(tmpLoanType)) {
				loanType = "4";
			} else {
				loanType = "6";
			}
			Label label_4 = new Label(3, i + 1, loanType);
			Label label_5 = new Label(4, i + 1, tmp.getStr("loanTitle"));
			Label label_6 = new Label(5, i + 1, "");
			String tmpLoanUsedType = tmp.getStr("loanUsedType"); // 借款用途
			String LoanUsedType = "";
			if ("A".equals(tmpLoanUsedType)) {
				LoanUsedType = "短期周转";
			} else if ("B".equals(tmpLoanUsedType)) {
				LoanUsedType = "个人消费";
			} else if ("C".equals(tmpLoanUsedType)) {
				LoanUsedType = "投资创业";
			} else if ("D".equals(tmpLoanUsedType)) {
				LoanUsedType = "购车借款";
			} else if ("E".equals(tmpLoanUsedType)) {
				LoanUsedType = "装修借款";
			} else if ("F".equals(tmpLoanUsedType)) {
				LoanUsedType = "婚礼筹备";
			} else if ("G".equals(tmpLoanUsedType)) {
				LoanUsedType = "教育培训";
			} else if ("H".equals(tmpLoanUsedType)) {
				LoanUsedType = "医疗支出";
			} else if ("I".equals(tmpLoanUsedType)) {
				LoanUsedType = "其他借款";
			} else if ("J".equals(tmpLoanUsedType)) {
				LoanUsedType = "购房借款";
			} else {
				LoanUsedType = "其他用途";
			}
			Label label_7 = new Label(6, i + 1, LoanUsedType);
			Label label_8 = new Label(7, i + 1, String.valueOf(tmp
					.getLong("loanAmount")));
			Label label_9 = new Label(8, i + 1, String.valueOf(tmp
					.getInt("rateByYear")
					+ tmp.getInt("rewardRateByYear")
					+ tmp.getInt("benefits4new")));
			Label label_10 = new Label(9, i + 1, tmp.getStr("loanTitle"));
			String tmpRefundType = tmp.getStr("refundType");
			String refundType = ""; // 还款方式 0:一次性还本付息 1:先息后本 2:等额本息/等额本金 3:其他
			if ("A".equals(tmpRefundType)) {
				refundType = "2";
			} else if ("B".equals(tmpRefundType)) {
				refundType = "1";
			} else {
				refundType = "3";
			}
			Label label_11 = new Label(10, i + 1, refundType);
			String releaseDate = tmp.getStr("releaseDate");
			if (!("".equals(releaseDate) || releaseDate == null)) {
				Label label_12 = new Label(11, i + 1, DateUtil.subDay(
						releaseDate, -3));
				Label label_13 = new Label(12, i + 1, releaseDate);
				sheet.addCell(label_12);
				sheet.addCell(label_13);
			}

			Label label_14 = new Label(13, i + 1, "100");
			Label label_15 = new Label(14, i + 1, "1");
			long maxAmt = 0; // 最大投标金额: 单位：分(可以为借款金额的100%)
			int isNew = tmp.getInt("benefits4new");
			if (isNew > 0) {
				maxAmt = 1000000;
			} else {
				maxAmt = tmp.getLong("loanAmount");
			}
			Label label_16 = new Label(15, i + 1, String.valueOf(maxAmt));
			Label label_17 = new Label(16, i + 1, tmp.getStr("userName"));
			User user = User.userDao.findById(tmp.getStr("userCode"));
			String loanUserLoginId = CommonUtil.decryptUserMobile(user
					.getStr("loginId"));
			Label label_18 = new Label(17, i + 1, loanUserLoginId);
			String loanDesc = "";
			String loanNo = tmp.getStr("loanNo");
			int tmpLoanNo = Integer.parseInt(loanNo);
			LoanApply loanApply = new LoanApplyService().findById(tmpLoanNo);
			if (loanApply != null) {
				String tmpLoanDesc = loanApply.getStr("loanDesc");
				if (!"".equals(tmpLoanDesc)) {
					JSONArray array = JSONArray.parseArray(tmpLoanDesc);
					for (int j = 0; j < array.size(); j++) {
						JSONObject json = array.getJSONObject(j);
						String title = json.getString("title");
						String content = json.getString("content");
						if (title.indexOf("借款人") != -1) {
							loanDesc = content.replaceAll("\n", "").substring(
									0, 50);
							break;
						}
					}
				}
			}

			Label label_19 = new Label(18, i + 1, loanDesc);
			Label label_20 = new Label(19, i + 1, "0");
			Label label_21 = new Label(20, i + 1, "");
			Label label_22 = new Label(21, i + 1, String.valueOf(tmp
					.getInt("loanTimeLimit")));
			Label label_23 = new Label(22, i + 1, "");
			Label label_24 = new Label(23, i + 1, CommonUtil.payCpnyID);
			sheet.addCell(label_1);
			sheet.addCell(label_2);
			sheet.addCell(label_3);
			sheet.addCell(label_4);
			sheet.addCell(label_5);
			sheet.addCell(label_6);
			sheet.addCell(label_7);
			sheet.addCell(label_8);
			sheet.addCell(label_9);
			sheet.addCell(label_10);
			sheet.addCell(label_11);

			sheet.addCell(label_14);
			sheet.addCell(label_15);
			sheet.addCell(label_16);
			sheet.addCell(label_17);
			sheet.addCell(label_18);
			sheet.addCell(label_19);
			sheet.addCell(label_20);
			sheet.addCell(label_21);
			sheet.addCell(label_22);
			sheet.addCell(label_23);
			sheet.addCell(label_24);
		}
		workbook.write();
		workbook.close();
	}

}
