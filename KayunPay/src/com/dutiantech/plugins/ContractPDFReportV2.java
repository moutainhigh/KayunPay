package com.dutiantech.plugins;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.List;

import javax.servlet.ServletOutputStream;

import com.alibaba.fastjson.JSONObject;
import com.dutiantech.model.BanksV2;
import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.LoanTransfer;
import com.dutiantech.model.User;
import com.dutiantech.model.UserInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.NumberToCN;
import com.dutiantech.util.Property;
import com.dutiantech.util.SysEnum;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * 电子合同  pdf 文件输出类
 * @author shiqingsong
 *
 */
public class ContractPDFReportV2 extends PDFReport{
	
	public ContractPDFReportV2(ServletOutputStream os) {
		super(os);
	}
	
	
	/**
	 * 输出电子合同
	 * @throws DocumentException
	 */
	public void generatePDFForYfq() throws DocumentException {
		//检查是否初始化输出内容
		if(null == contractMap || contractMap.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//获取投标流水   (甲方信息)
//			List<LoanTrace> listLoanTrace = (List<LoanTrace>)contractMap.get("listLoanTrace");
			LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");//标的信息
			User userAllInfo = (User)contractMap.get("userAllInfo");//出借人信息
			User loanUser = (User) contractMap.get("loanUser");//借款人信息
			BanksV2 loanBanks = (BanksV2) contractMap.get("loanBanks");//借款人的银行卡信息
			LoanTrace loanTrace = (LoanTrace) contractMap.get("loanTrace");//投标流水
			BanksV2 userBanks = (BanksV2) contractMap.get("userBanks");//出借人的银行卡信息
			String qq = (String)contractMap.get("qq");
			String wechat = (String)contractMap.get("wechat");
			String email = (String)contractMap.get("email");
			//转换放款日期
			String backDate = DateUtil.getStrFromDate(
					DateUtil.getDateFromString(loanInfo.getStr("effectDate"), "yyyyMMdd"), "yyyy年MM月dd日");
			
//			if(null == listLoanTrace || listLoanTrace.size() <= 0){
//				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
//						Paragraph.ALIGN_CENTER));
			if(null == loanTrace){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//输出标书头信息
				String loanNo = loanInfo.getStr("loanNo");
//				String loanCode = loanInfo.getStr("loanCode");
				
				String loanUserCardId = "";
				//获取借款人的归属地
				String loanCardId = loanInfo.getStr("userCardId");
				String idCardAddress = "";
				try{
					loanCardId = CommonUtil.decryptUserCardId(loanCardId);
					idCardAddress =	new String(Property.getPropertyValueByKey("IDCardAddressCode", loanCardId.substring(0, 6)).getBytes("ISO_8859_1"),"utf-8");
//					idCardAddress = Property.getPropertyValueByKey("IDCardAddressCode", loanCardId.substring(0, 6));
				}catch(Exception e){
					e.printStackTrace();
				}
//				
//				try {
//					loanUserCardId = CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId"));
//					loanUserCardId = "***"+loanUserCardId.substring(loanUserCardId.length()-4, loanUserCardId.length());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				loanUserCardId = (String) contractMap.get("loanUserCardId");
				
				document.add(createParagraph("借款协议书", Titlefont, Paragraph.ALIGN_CENTER));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//添加水印
				document.add(createImage(DZHT_URL,100,620));
				document.add(createParagraph("合同编号："+loanNo, keyfont, Element.ALIGN_RIGHT));
				
				document.add(createParagraph("甲方（借款人）："+loanInfo.getStr("userName"), keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("身份证号："+loanUserCardId, keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("户籍地址："+idCardAddress, keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("经常居住地：", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("平台用户名："+loanUser.getStr("userName"), keyfont, Element.ALIGN_LEFT));
				//借款人的手机号
				
				String loanUserMobile = loanUser.getStr("userMobile");
				try{
				   loanUserMobile =	CommonUtil.decryptUserMobile(loanUserMobile);
				}catch (Exception e){
					e.printStackTrace();
				}
				
				
				document.add(createParagraph("联系电话："+loanUserMobile, keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("电子邮箱："+email, keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("微信号："+wechat+"           " + "QQ号："+qq+"          ", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("收款银行账号："+loanBanks.getStr("bankNo"), keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("开户行："+loanBanks.getStr("bankName"), keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("乙方（出借人）：", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//输出投标流水信息
				PdfPTable table = createTable(10);
				
				table.addCell(createCell("平台用户名", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("出借人姓名", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("身份证号", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("联系电话", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("户籍地址", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("经常居所地", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("出借金额", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("银行卡号", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("出借金额", keyfont,Element.ALIGN_CENTER));
				table.addCell(createCell("全部应收", keyfont,Element.ALIGN_CENTER));
				
				table.addCell(createCell(userAllInfo.getStr("userName"), textfont));
				table.addCell(createCell(userAllInfo.getStr("userCardName").substring(0, 1)+"**", textfont));
				String userCardId = "";
				String userIdAddress = "";
				try{
					userCardId = CommonUtil.decryptUserCardId(userAllInfo.getStr("userCardId"));
					userIdAddress =	new String(Property.getPropertyValueByKey("IDCardAddressCode", userCardId.substring(0, 6)).getBytes("ISO_8859_1"),"utf-8");
//					userIdAddress = Property.getPropertyValueByKey("IDCardAddressCode", userCardId.substring(0, 6));
					userCardId = "***" + userCardId.substring(userCardId.length()-4,userCardId.length());
					
				}catch( Exception e){
					e.printStackTrace();
				}
				table.addCell(createCell(userCardId,textfont));
				
				//联系电话
				String userMobile = userAllInfo.getStr("userMobile");
				String mobile = "";
				try{
					mobile = CommonUtil.decryptUserMobile(userMobile);
					mobile = "***" + mobile.substring(mobile.length()-4,mobile.length());
				}catch(Exception e){
					
				}
				table.addCell(createCell(mobile, textfont));
				table.addCell(createCell(userIdAddress, textfont));
				table.addCell(createCell("", textfont));
				table.addCell(createCell("￥"+ loanTrace.getLong("payAmount")/10/10,textfont));
				
				table.addCell(createCell(userBanks.getStr("bankNo"), textfont));
				table.addCell(createCell(loanTrace.getInt("loanTimeLimit") +"个月", textfont));
				//利率
				int rate = loanTrace.getInt("rateByYear");
				
				//总还本息
				long[] f_004 = CommonUtil.f_004(loanTrace.getLong("payAmount"),rate,loanTrace.getInt("loanTimeLimit"),loanTrace.getStr("refundType"));
				table.addCell(createCell("￥" + (f_004[0]+f_004[1])/10/10, textfont));
				
//				for(int i = 0;i<listLoanTrace.size();i++){
//					LoanTrace loanTrace = listLoanTrace.get(i);
//					UserInfo userInfo = UserInfo.userInfoDao.findById(loanTrace.getStr("payUserCode"));
//					User user = User.userDao.findById(loanTrace.getStr("payUserCode"));
//					String userCardId = "";
//					try{
//						userCardId = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
//						userCardId = "***" + userCardId.substring(userCardId.length()-1,userCardId.length());
//					}catch( Exception e){
//						e.printStackTrace();
//					}
//					table.addCell(createCell(user.getStr("userName"), textfont));
//					table.addCell(createCell(userInfo.getStr("userCardName").substring(0, 1)+"**", textfont));
//					table.addCell(createCell(userCardId,textfont));
//					
//					//联系电话
//					String userMobile = user.getStr("userMobile");
//					String mobile = "";
//					try{
//						mobile = CommonUtil.decryptUserMobile(userMobile);
//						mobile = "***" + mobile.substring(mobile.length()-4,mobile.length());
//					}catch(Exception e){
//						
//					}
//					table.addCell(createCell(mobile, textfont));
//					
//					table.addCell(createCell("户籍地址", textfont));
//					table.addCell(createCell("经常居住地",textfont));
//					table.addCell(createCell("￥"+ loanTrace.getLong("payAmount")/10/10,textfont));
//					table.addCell(createCell("银行卡号",textfont));
//					table.addCell(createCell(loanTrace.getInt("loanTimeLimit") +"个月", textfont));
//					//利率
//					int rate = loanTrace.getInt("rateByYear");
//					
//					//总还本息
//					long[] f_004 = CommonUtil.f_004(loanTrace.getLong("payAmount"),rate,loanTrace.getInt("loanTimeLimit"),loanTrace.getStr("refundType"));
//					table.addCell(createCell("￥" + (f_004[0]+f_004[1])/10/10, textfont));
//				}
				document.add(table);
				
				document.add(createParagraph("丙方(担保人)：武汉春草科技有限公司", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("身份证号/统一社会信用代码: 91420100MA4L0E7Y4K", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("住所地: 武汉市东湖新技术开发区光谷大道3号激光工程设计总部二期研发楼06栋06单元15层5号(Z0731)", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系电话: 15972218256", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("电子邮箱：979685957@qq.com", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("丁方(居间平台服务商)：武汉易融恒信金融信息服务有限公司", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("住所：武汉市江汉区中央商务区泛海国际SOHO城（一期）第2幢6层2号房", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系人： 肖贝", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系电话：027-83356151", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("丁方(居间平台服务商)：武汉易融恒信金融信息服务有限公司", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系电邮：kf@yrhx.com", keyfont, Element.ALIGN_LEFT));
				
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("戊方（债权受让人）:", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("住所：", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系人： ", keyfont, Element.ALIGN_LEFT));
				document.add(createParagraph("联系电话：", keyfont, Element.ALIGN_LEFT));
				
				
				document.add(createParagraph("  鉴于：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、居间平台服务商（本协议丁方）为易融恒信平台（互联网域名为yrhx.com，“易融恒信”）的运营管理人，提供金融信息咨询及相关服务。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、甲方或甲方指定人员、乙方均已在丁方平台注册，甲方及其指定人员、乙方均同意遵守丁方平台的各项行为准则，在充分阅读理解本文本情形下本着诚信自愿原则签订本《借款协议书》。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、丙方接受甲方委托，就上述《借款协议书》项下还款义务提供连带责任保证担保。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("现各方根据平等、自愿的原则，达成协议如下：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("一、借款金额及期限", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方同意通过易融恒信平台向乙方借款，乙方同意通过易融恒信平台向甲方发放该笔借款，具体信息如下：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				
				PdfPTable tableLoan = createTable(9);
				tableLoan.addCell(createCell("借款用途", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("借款本金数额（小写）", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("借款本金数额（大写）", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("借款年化利率", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("还款方式", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("借款起始时间", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("还款日", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("借款期限", keyfont,Element.ALIGN_CENTER));
				tableLoan.addCell(createCell("收款银行账号", keyfont,Element.ALIGN_CENTER));
				
				String loanDesc = loanInfo.getStr("loanDesc");//json格式的字符串
				JSONObject jsonObject = JSONObject.parseObject(loanDesc);
				String usrType = "";
				if(jsonObject != null){
					usrType = jsonObject.get("loanUserPurpose").toString();//借款用途
				}
				
				tableLoan.addCell(createCell(usrType, textfont));
				tableLoan.addCell(createCell(loanInfo.getLong("loanAmount")/10/10+"", textfont));
				tableLoan.addCell(createCell(NumberToCN.number2CNMontrayUnit(new BigDecimal(loanInfo.getLong("loanAmount")/100)), textfont));
				
				
				//计算利率(年利率 //+ 奖励年利率)
				int loanrate = loanInfo.getInt("rateByYear");// + loanTrace.getInt("rewardRateByYear");
				tableLoan.addCell(createCell( (float)loanrate/10/10 + "%", textfont));
				//还款方式
				String refundType = loanInfo.getStr("refundType");
				String refundTypeValue = "";
				if("A".equals(refundType)){
					refundTypeValue = "按月等额本息";
				}else if("B".equals("refundType")){
					refundTypeValue = "先息后本";
				}
				tableLoan.addCell(createCell(refundTypeValue, textfont));
				//借款的起始时间
				tableLoan.addCell(createCell(backDate, textfont));
				tableLoan.addCell(createCell("借款期限类每月"+loanInfo.getInt("clearDay")+"日", textfont));
				tableLoan.addCell(createCell(loanInfo.getInt("loanTimeLimit") +"个月", textfont));
				tableLoan.addCell(createCell(loanBanks.getStr("bankNo"), textfont));
				
				document.add(tableLoan);
				
				document.add(createParagraph("二、借款流程", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2.1本协议成立：乙方在易融恒信平台上对甲方发布的借款需求点击“投标”按钮时，本协议即时成立。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2.2出借资金冻结：乙方点击“投标”按钮即视为其已经向丁方发出不可撤销的授权指令，授权丁方委托第三方存管银行江西银行冻结乙方确认向甲方出借的资金。冻结将在本协议生效时或本协议确定失效时解除。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2.3本协议生效：在投标期限届满之时，若甲方发布的借款需求所对应的出借资金已经全部冻结或冻结资金虽未达借款需求数额但甲方同意仅接受当时已冻结资金的，本协议立即生效。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2.4出借资金划转：本协议生效的同时，甲方即不可撤销地授权丁方委托第三方存管银行江西银行将冻结资金划转至甲方指定人员的第三方存管银行江西银行存管账户中，划转完毕即视为借款发放成功，甲方可通过“提现”操作将划转的资金转至其银行账户上，甲方是否提现资金不影响本协议的效力。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("三、借款资金来源保证", 
						keyfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("乙方保证其所用于出借的资金来源合法，且乙方是该资金的合法支配权人，如第三方对资金归属、支配权、合法性等问题主张异议，给协议其它方造成损失的，应当赔偿全部损失。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("四、连带清偿责任", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("本协议如涉及两人以上借款，任一借款人均应履行本协议项下的义务，对全部借款承担连带清偿责任，债权人有权向任一借款人追索本协议项下全部应付款项，包括但不限于本金、利息、违约金等。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("五、本息偿还方式", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.1甲方必须按照本协议的约定按时、足额偿还乙方的本金和利息以及其他应当由甲方支付的费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.2甲方授权丁方委托第三方存管银行江西银行，按还款计划将金额等同于乙方当期应收金额的资金划转至乙方在第三方存管银行江西银行所开立的账户下，划转完毕即视为本期还款偿还完毕。为此，甲方应保证在每期还款日（且不晚于当日上午8时），甲方指定人员在第三方存管银行江西银行所开立的存管账户余额足以支付当期应付利息和本金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.3乙方为多人的，各出借人按出借金额享有要求还本付息的债权，即每位出借人应收借款本金金额为该出借人实际出借资金，该出借人应收利息金额计算公式为：该出借人出借资金*本次借款年化利率*应收息天数/360。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.4 还款日遇法定假日或公休日的，还款日顺延至假日或公休日后第一个工作日。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.5如遇到还款当月无还款日对应日的月份，还款日为应还款当月的最后一个工作日。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5.6投标冻结期间资金占用费：自乙方出借资金被冻结起至丁方指令向甲方放款或向乙方账户撤回，任何一方不用向乙方支付资金占用费。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("六、担保及服务", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.1丙方同意为本协议下甲方向债权人负有的按时清偿借款本息的义务提供连带保证责任，保证担保的范围为本协议项下的借款本金及利息、违约金、居间服务费，以及甲方根据本协议或适用的中国法律应支付的其他赔偿金和债权人实现本协议下债权的费用（包括诉讼费、保全费、保全担保费、差旅费、律师费（按照诉讼标的额的10%计算）、执行费等）以及其他费用等。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.2在甲方未按照本协议的约定按时、足额清偿任何一期借款本金或利息时，丁方有权代表债权人向丙方发起担保方清偿要求，要求丙方按照本协议的约定承担担保责任。丙方须要在债权期限届满日，将代偿资金划转至丁方指定的监管账户，并授权丁方向债权人账户支付，以履行其在本协议下的担保责任；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("如果在借款本金到期日上午8时前甲方指定人员在第三方存管银行        所开立的存管账户余额不足以覆盖甲方应还借款本息的，丁方有权代表债权人向丙方发起担保方清偿要求，要求丙方按照本协议的约定承担担保责任。丙方需要在借款到期日当日，将代偿资金划转至丁方指定的监管账户，并授权丁方向债权人账户支付，以履行其在本协议下的担保责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.3保证期间：自本协议生效之日起至本协议项下债务履行期限届满之日起2年。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.4丙方根据本条规定履行担保责任后，债权人在本协议项下的所有权利视为已经得到满足和实现，债权人不得再对甲方提出任何请求或主张；丙方可以向甲方进行追偿，债权人应提供合理及必要的协助。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.5债权人在此委托丁方代为提出代偿要求，即在出现丙方承担连带责任保证约定事由后，丁方有权代债权人向丙方提出履行担保责任、向债权人进行代偿的要求。此等情形下，丙方应依照约定履行义务，不得以主体是否适格为由进行抗辩。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.6丙方有权就为本协议借款所提供的服务向甲方收取担保费等费用，上述费用的收取方式由甲丙双方另行约定。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				
				document.add(createParagraph("6.7丁方有权就为本协议借款所提供的服务向甲方收取服务费用，上述费用的收取方式由甲丁双方另行约定。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6.8各方一致同意，由乙方委托【   】作为乙方代表，与甲方签署相应的线下借款协议（若有）、担保协议并代表乙方办理抵押或质押手续，具体担保事宜根据担保协议等文件执行。各方一致确认，【   】仅是作为乙方的代理人办理相应的抵押/质押手续，乙方为实际的债权人和抵押权人。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("七、居间服务费的收取", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7.1在本协议中，“居间服务费”是指因丁方为甲方和乙方提供居间服务包括但不限于网络平台技术支持、信息咨询、信用评估、推荐、还款提醒、账户管理、还款特殊情况沟通等系列信用相关服务而由甲方和乙方支付给丁方的报酬。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7.2对于丁方向甲方提供的居间服务，甲方同意在获得本协议约定的借款资金后按照还款计划中的要求并按照借款期限的时间（与分期还款时间一致）向丁方支付居间服务费。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7.3对于丁方向乙方提供的居间服务，乙方同意在出借本协议约定的借款资金后按照应收利息总金额的8 %并按照借款期限的时间（与甲方分期还款时间一致）采用分期支付向丁方支付居间服务费。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("八、逾期还款", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8.1每期还款日23:59前，甲方或甲方指定人员在第三方支付机构的存管账户中余额不足以支付当月应还款的，则视为逾期。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8.2甲方逾期还款，须按本协议还款计划规定的金额及利率向乙方偿付本息直至甲方归还全部债务之日止。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8.3甲方有下列情形之一或几项发生的，视为甲方违约，乙方有权宣布本协议提前到期，要求甲方按照本协议还款计划规定的金额及利率立即提前偿还部分或全部借款本息。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （1）甲方违反本协议任何一项条款的约定；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （2）甲方在本协议期内未按时归还借款本息；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （3）甲方向乙方提供虚假借款资料的；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （4）甲方在一个付款期内逾期偿还本息达7天以上的；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （5）甲方擅自改变预留的联系方式和送达地址致使乙方无法联系的或乙方按照甲方预留的联系方式和送达地址采取包括但不限于书信、电话、微信、QQ、电子邮箱等任何一种形式多次催收不予回复的；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （6）甲方卷入或即将卷入重大的诉讼或仲裁及其他法律纠纷或有其他缺乏偿债诚意的行为足以影响其偿债能力；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("  （7）其他可能严重影响甲方归还借款本息的行为。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8.4甲方承诺自觉履行本协议各条款的义务，无论任何原因导致乙方的合同利益或合同权利受到损害，甲方将承担本协议项下的借款本金、利息、违约金等，还包括但不限于乙方或丙方在依法主张权利过程中发生的包括但不限于仲裁费、诉讼费、保全费、保全担保费、差旅费、律师费（按照诉讼标的额的10%计算）、公告费、执行费、调查费等费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8.5借款期间内，逾期违约金可根据易融恒信平台相关规则的变化进行相应调整。如相关规则发生变化，则易融恒信平台会在网站公示或以其他合理方式通知您该等规则的变化。如您在上述规则变更生效后，继续使用易融恒信平台服务的，表示您愿意接受变更后的规则，也将遵循变更后的规则使用易融恒信平台服务。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("九、债权、债务转让", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9.1甲方同意：甲方逾期还款达到/日后，本协议项下的乙方债权及丁方收取居间服务费的权利，一并（且同时）自动转让给戊方，且乙方委托丁方对甲方出具债权转让的通知（通知的方式参照本合同第11.2条通知与送达条款的相关规定）。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9.2未经债权人事先书面（包括但不限于电子邮件等方式）同意，甲方不得将本合同项下的任何权利义务转让给任何第三方。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9.3乙方认可：戊方或戊方委托的第三方向乙方分期支付债权转让价款（即：甲方应还而未还的借款本息总额），支付的期限与甲方的还款期限一致，每期支付的时间可早于、等同于或晚于甲方的当期还款时间（即乙方认可：甲方逾期还款后，戊方或戊方委托的第三方支付给乙方的钱款，均可认定为债权转让价款，无论前述钱款支付时债权是否自动发生转移）。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9.4 丁方认可：丁戊之间的转让为有偿转让，转让价款及支付方式双方届时另行协商。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9.5债权转让通知送达甲方后，戊方即刻获得债权，本协议项下其他条款不受影响，甲方应按照本协议的约定向戊方履行义务、承担责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("十、违约", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.1除本协议另有约定外，如果甲方擅自改变本协议第一条规定的借款用途，或严重违反还款义务（如本协议第八条约定），或未经债权人同意擅自转让本协议项下借款债务的，债权人授权丁方有权宣告借款提前到期，同时，甲方应向债权人支付借款本金总额的 10% 作为违约金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.2甲方须在丁方宣告借款提前到期后的三日内，向甲方在第三方存管银行江西银行的存管账户一次性支付余下的所有本金、利息和违约金，丁方再根据其与债权人之间的约定委托第三方存管银行江西银行向债权人划转该等资金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.3甲方保证其提供的信息和资料的真实性，不得提供虚假资料或隐瞒重要事实。甲方提供虚假资料或者故意隐瞒重要事实的，构成违约，应承担违约责任，丁方有权要求提前终止本协议，要求甲方提前还款，甲方应向债权人支付借款本金总额 10% 的金额作为违约金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.4甲方须在丁方要求终止本协议后三日内，向甲方指定在第三方存管银行江西银行的存管账户一次性支付余下的所有本金、利息和违约金，丁方再根据其与债权人之间的约定委托第三方存管银行江西银行向债权人划转该等资金。构成犯罪的，丁方将有权向相关国家机关报案，追究甲方刑事责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.5丁方保留将甲方违约失信的相关信息在媒体披露的权利。因甲方未还款而带来的包括但不限于诉讼费、保全费、保全担保费、差旅费、律师费（按照诉讼标的额的10%计算）、公告费、执行费、调查费等费用将由甲方承担", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.6如发生上述任一违约情况，债权人要求提前终止本协议并且甲方不能按照本条约定向债权人一次性支付余下的所有本金、利息和违约金（剩余借款本息的30%），丙方须履行前述第六条涉及的担保责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10.7若甲方或丙方违反上述约定，债权人授权丁方代为其向违约方主张权利，包括但不限于聘请律师、催收、申请仲裁、提起诉讼、申请强制执行等方式。对此，甲方和丙方不持异议。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("十一、关于电子合同、电子签名、通知送达的约定", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("11.1电子合同及电子签名的使用", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("各方确认，本协议的电子版本与纸质版本具有同等法律效力。发生争议时，电子合同打印件将作为证据提交，各方对其真实性、合法性、关联性无异议。各方如以网络方式签订本协议，则各方以点击“确认”等相关按钮或以电子签名的方式作为对合同内容的同意及确认。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("11.2通知与送达", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("各方因履行本协议而相互发出或者提供的所有通知、文件、资料，均以本协议中所列明的电话、送达地址（若户籍地址与经常居所地不一致的，以本协议约定的经常居所地为送达地址）或者电邮送达，一方如果迁址或者变更电话、电子邮箱的，应当自变更前3日内书面通知到对方且获得对方书面认可方可变更。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("各方选择上述送达方式中的任何一种方式送达即可。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("各方确认，己方在本协议中提供的联系方式、送达地址（若户籍地址与经常居所地不一致的，以本协议约定的经常居所地为送达地址）或者电子邮箱地址等是己方有效的送达地址，适用于己方与下列各方之间的所有通知、文件、资料以及债权转让的送达，且为始终、唯一有效的送达地址。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("11.2.1一方因履行本协议而与他方之间的通知与送达；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("11.2.2各方因以诉讼方式（包括但不限于诉前调解、一审阶段、二审阶段、再审审查及执行程序阶段等全部诉讼程序）解决本协议所涉及的全部争议，各方因与人民法院之间相互发出或者提供的所有通知、文件、资料包括但不限于传票、裁定书、调解书、判决书等，均以各方签订的本协议中所列明的联系方式、地址（若户籍地址与经常居所地不一致的，以本协议约定的经常居所地为送达地址）或者电子邮箱地址作为始终、唯一有效的通讯送达地址及联系方式。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("各方均同意依据相关法律和司法解释的规定接受电子送达，任何一方因自身原因导致的本协议中列明的联系电话、QQ、微信、电子邮箱不能正常接收信息或文件的且又未重新提供有效的联系电话、QQ、微信、电子邮箱的，他方或人民法院依据本协议列明的联系电话、QQ、微信、电子邮箱所有通知、文件、资料包括但不限于传票、裁定书、调解书、判决书等一经发送（拨打），均视为已经成功送达。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("如一方提供的送达地址不确切或者未及时告知变更后的送达地址，一切法律后果由其本人承担。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("十二、关于争议解决方式", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("凡因执行本协议所发生的或者与本协议有关的一切争议，各方应尽可能通过友好协商解决；如协商不能解决，各方均同意向本协议签订地即武汉市江汉区人民法院依法提起诉讼并依据《民事诉讼法》的规定同意适用简易程序审理。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("十三、其他", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.1本协议经各方通过易融恒信平台以网络在线点击确认的方式签订，或者使用本协议项下服务，即视为您已同意本协议。各方均已仔细阅读并准确理解本协议的全部内容（特别是以粗体、下划线标注的内容）。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.2甲方发布的相应借款需求未在2个工作日内被全部满足并已经适当冻结相应拟出借资金的，自第2个工作日24：00起，本协议自动终止，但甲方同意仅接受当时已冻结资金的除外；甲方将本协议下全部本金、利息、违约金及其他相关费用全部偿还完毕之时，本协议亦自动终止。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.3本协议的任何修改、补充均须以易融恒信平台电子文本或其它书面形式作出。如本协议发生变更，导致您的权利义务发生变化，我们将通过网站公告的方式予以公布或者其他合理的方式向您告知，请您留意变更后的合同内容。如您在本协议变更生效后，继续使用易融恒信平台服务的，表示您愿意接受变更后的合同，也将遵循变更后的合同使用易融恒信平台服务。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.4 各方均确认，本协议的签订、生效和履行以不违反法律为前提。如果本协议中的任何一条或多条违反适用的法律，则该条款将被视为无效，但该无效条款并不影响本协议其他条款的效力。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.5本协议签订后，各方可协商订立本协议的补充协议，补充协议与本协议具有同等效力，若补充协议与本协议内容存在冲突，则以补充协议为准。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.6各方委托易融恒信平台保管所有与本协议有关的书面文件或电子信息。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("13.7本协议中，除非另有规定，易融恒信平台享有在法律允许的范围内最终解释权。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方："+loanInfo.getStr("userName"), 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("乙方："+userAllInfo.getStr("userCardName"), 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("丙方：武汉春草科技有限公司", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("丁方：武汉易融恒信金融信息服务有限公司", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("戊方：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("签订时间："+backDate, 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("签订地点：武汉市江汉区", 
						keyfont, Paragraph.ALIGN_LEFT));
	
			}
			document.close();
		}
		
		
	}

	/**
	 * 输出电子合同
	 * @throws DocumentException 
	 */
	@SuppressWarnings("unchecked")
	public void generatePDF() throws DocumentException{
		
		//检查是否初始化输出内容
		if(null == contractMap || contractMap.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//获取投标流水   (甲方信息)
			List<LoanTrace> listLoanTrace = (List<LoanTrace>)contractMap.get("listLoanTrace");
			LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");
//			UserInfo userInfo = (UserInfo)contractMap.get("userInfo");
			//转换放款日期
			String backDate = DateUtil.getStrFromDate(
					DateUtil.getDateFromString(loanInfo.getStr("effectDate"), "yyyyMMdd"), "yyyy年MM月dd日");
			
			if(null == listLoanTrace || listLoanTrace.size() <= 0){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//输出标书头信息
				String loanNo = loanInfo.getStr("loanNo");
//				String loanCode = loanInfo.getStr("loanCode");
				
				String loanUserCardId = "";
//				
//				try {
//					loanUserCardId = CommonUtil.decryptUserCardId(loanInfo.getStr("userCardId"));
//					loanUserCardId = "***"+loanUserCardId.substring(loanUserCardId.length()-4, loanUserCardId.length());
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
				loanUserCardId = (String) contractMap.get("loanUserCardId");
				
				document.add(createParagraph("易融恒信借款协议书", Titlefont, Paragraph.ALIGN_CENTER));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//添加水印
				document.add(createImage(DZHT_URL,100,620));
				PdfPTable tableTitle = createTable(5);
				tableTitle.addCell(createCell("借款协议号：" + loanNo, keyfont, Element.ALIGN_CENTER));
				if (SysEnum.UserType.C.val().equals(contractMap.get("loanUserType"))) {
					tableTitle.addCell(createCell("借款人："+ contractMap.get("loanUserName"), keyfont, Element.ALIGN_CENTER));
					tableTitle.addCell(createCell("统一社会信用代码："+loanUserCardId, keyfont, Element.ALIGN_CENTER));
				} else {
					tableTitle.addCell(createCell("借款人："+loanInfo.getStr("userName"), keyfont, Element.ALIGN_CENTER));
					tableTitle.addCell(createCell("身份证号："+loanUserCardId, keyfont, Element.ALIGN_CENTER));
				}
				tableTitle.addCell(createCell("出借人：详见本协议第一条", keyfont, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("签订日期："+backDate, keyfont, Element.ALIGN_CENTER));
				document.add(tableTitle);
				
				document.add(createParagraph("签订地点：武汉市江汉区", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("居间服务人：武汉易融恒信金融信息服务有限公司", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				
				document.add(createParagraph("  鉴于：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、居间服务人是一家在武汉市合法成立并有效存续的有限公司，拥有并运营易融恒信 （网址： www.yrhx.com ,以下凡提及该网站者，均指居间服务人），向网站注册用户提供咨询、信息提供及各种委托服务等居间服务。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、出借人和借款人均已阅读并同意遵守易融恒信的《注册协议》，注册成为易融恒信的用户，并认可易融恒信通过网站公开发布关于注册用户的各种规则。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、出借人和借款人同意通过居间服务人的服务、以电子合同的形式达成本借款协议，本借款协议的内容经双方充分阅读且知晓每一条款的含义，是双方真实意思表示。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("借款人与各出借人，通过易融恒信的居间服务，就有关借款事项达成如下协议：", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("  第一条：借款详情如下所示：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//输出投标流水信息
				PdfPTable table = createTable(8);
				table.addCell(createCell("出借人(ID)", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("出借人姓名", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借款金额", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借款期限", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("年利率", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借款开始日", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借款到期日", keyfont, Element.ALIGN_CENTER));
				//table.addCell(createCell("月截止还款日", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("总还款本息", keyfont, Element.ALIGN_CENTER));
				for (int i = 0; i < listLoanTrace.size(); i++) {
					LoanTrace loanTrace = listLoanTrace.get(i);
					
					UserInfo user = UserInfo.userInfoDao.findById(loanTrace.getStr("payUserCode"));
					String userCardId = "";
					try {
						userCardId = CommonUtil.decryptUserCardId(user.getStr("userCardId"));
						userCardId = "***"+userCardId.substring(userCardId.length()-4, userCardId.length());
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					table.addCell(createCell(userCardId, textfont));
					table.addCell(createCell(user.getStr("userCardName").substring(0,1)+"**", textfont));
					table.addCell(createCell("￥" + loanTrace.getLong("payAmount") / 10/10 , textfont));
					table.addCell(createCell(loanTrace.getInt("loanTimeLimit") + "个月", textfont));
					
					//计算利率(年利率 //+ 奖励年利率)
					int rate = loanTrace.getInt("rateByYear");// + loanTrace.getInt("rewardRateByYear");
					table.addCell(createCell( (float)rate/10/10 + "%", textfont));
					
					//借款开始日期
					table.addCell(createCell(backDate, textfont));
					//借款到期日
					String endDate = "";
					try{
						Calendar calendar = CommonUtil.anyRepaymentDate(loanInfo.getStr("effectDate"), loanInfo.getInt("loanTimeLimit"));
						endDate = DateUtil.getStrFromDate(calendar.getTime(), "yyyy年MM月dd日");
					}catch(Exception e){
					}
					table.addCell(createCell(endDate, textfont));
					
					//月截止还款日
					//table.addCell(createCell("", textfont));
					
					//总还款本息
					long[] f_004 = CommonUtil.f_004(loanTrace.getLong("payAmount"), rate, loanTrace.getInt("loanTimeLimit"), loanTrace.getStr("refundType"));
					table.addCell(createCell("￥" + (f_004[0]+f_004[1])/10/10, textfont));
				}
				document.add(table);
				
				
				document.add(createParagraph("第二条：还款", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、借款人承诺按照本协议第一条约定的时间和金额按期足额向出借人还款。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、借款人的每一期还款的还款金额计算公式为：每月须还款总金额=每月须还利息+每月须还本金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、借款人的最后一期还款必须包含全部剩余本金、利息及其他根据本协议产生的全部费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第三条：借款、还款的支付方式", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、出借人在同意向借款人出借相应款项时,已委托易融恒信在本借款协议生效后将该笔借款直接划付至借款人帐户。借款人已委托易融恒信将还款直接划付至出借人帐户。出借人及借款人均授权易融恒信委托银行从出借人或借款人账户中划付应支付或应偿还的借款本息及其他应付费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、易融恒信网站通过公告或其他各种方式明示出借人应享有的各种收益（包括但不限于利息、奖励等），均已包含在本协议的借款利息中。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、借款人和出借人均同意，易融恒信接受借款人或出借人委托所采取的划付款项行为，所产生的法律后果均由相应委托方借款人或出借人自行承担。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、本协议中借款和还款的履行地均为易融恒信的公司注册地：湖北省武汉市江汉区。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第四条：逾期还款", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、若借款人逾期仍未还款,借款人除向出借人支付正常利息以外,还应每天承担逾期罚息，且出借人有权提前终止合同要求借款人偿还全部借款本息。出借人和借款人均同意并认可，易融恒信可通过短信、电话、上门催收或委托第三方等方式对借款人逾期未偿还的本息进行催收,且上述逾期罚息，借款人和出借人均同意作为催收服务费用由借款人直接向易融恒信或债权受让方（如有）支付。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、借款人同意，其逾期还款造成出借人因此支付的费用(包括但不限于律师费)由借款人承担。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、借款人同意，如借款人逾期未偿还任何一期还款，出借人及借款人同意并支持易融恒信采取以下措施之一项或几项：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）将借款人的有关身份资料及其他个人信息通过易融恒信“逾期黑名单”等栏目对外公开；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）将借款人的有关身份资料及其他个人信息正式备案在“不良信用记录”,列入全国个人信用评级体系的黑名单(“不良信用记录”数据将为银行、电信、担保公司、人才中心等有关机构提供个人不良信用信息)；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（3）对借款人采取法律措施,由此所产生的所有法律后果将由借款人来承担；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（4）无需催告即可要求借款人偿还全部借款本息，并提前解除借款合同。", 
						textfont2, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第五条：借款债权的转让", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、借款人知晓并同意出借人于本协议履行过程中可能将其因本借款协议而享有的债权的全部或部分转让给不特定的第三人，且转让次数无限定。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、出借人将其债权全部或部分转让给第三人的，应当通知借款人，可以以信件、电子邮件、短信、电话或当面送达等合法形式。该债权转让自出借人或其委托人之通知送达之日起对借款人发生效力。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、就债权转让事宜，出借人在此无条件且不可撤销的授予易融恒信如下委托事项及权限：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）在符合本协议5.7条或本协议其他条款约定或易融恒信公告的债权转让的情形发生时，由易融恒信代表全体出借人与债权受让人（或与债权受让人、借款人一并）签订《债权转让协议》和/或《债权转让确认书》及其他债权转让有关的合同、文件；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）将债权转让事宜（包括债权受让人等）通知借款人；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（3）代收债权转让对价款项；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("上述委托的有效期自本协议生效之日起直至本协议项下全部借款本息及清偿费用全部清偿完毕之日止。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、借款人在此已明确知晓易融恒信的受托权限，承诺在接到易融恒信的债权转让通知时即对债权受让人负有对应债权的清偿义务，而无须易融恒信再行出具出借人的书面委托。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、出借人将其债权全部或部分转让给第三人的，本借款协议中对应权利及义务一并转让给债权受让人，包括但不限于主张罚息、利息、单方提前解除合同等权利及支付中介服务费用的义务。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6、在借款人正常还款的情形下，出借人可按照易融恒信公布的债权转让规则将债权转让给第三人。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7、出借人和借款人均同意，为集中维护各出借人权利，如出现易融恒信认为有需要的（包括但不限于借款人未能按期如数清偿任意一期借款本息或易融恒信认为借款人有逾期风险等）情形下，全体出借人一致同意将本协议项下债权转让给易融恒信或易融恒信居间介绍的其他愿意受让该项债权的第三人。出借人在此授权易融恒信：上述情形发生之后，由易融恒信作为全体出借人的代理人，代为选择债权受让人；代为与债权受让人签订债权转让涉及的所有合同、文件；代为向借款人发送债权转让通知；代为决定或进行其他与债权转让有关的事项，包括但不限于视实际情况独立决定债权转让的价格、支付债权转让对价的时间和方式等。自债权转让通知送达至借款人时，债权受让人取代出借人成为本借款协议新的债权人，取得与债权有关的所有权利。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("第六条：易融恒信的居间服务费等收费", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、居间服务费是指因居间服务人（即易融恒信）为借款人与出借人提供交易信息、信用咨询、信用评估、还款提醒、账户管理、还款特殊情况沟通、债权转让各项委托等系列相关服务而由借款人与出借人分别支付给易融恒信的报酬。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、借款人同意在借款成功时根据借款类型的不同另行向易融恒信支付居间服务费、履约保证金等费用，此笔费用借款人委托易融恒信在借款成功时从借款本金中直接扣除。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、借款人同意在借款成功后按月向易融恒信支付账户管理费用，此笔费用借款人应当按月主动向易融恒信指定账户支付，同时借款人同意由易融恒信在借款到账时直接扣除。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、出借人同意在借款成功后按月向易融恒信支付本次借款所得利息的一定比例作为居间服务费，此笔费用出借人与借款人一致同意易融恒信在借款人支付借款本息时直接扣除。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、如借款人通过其他第三方提供的中介服务而签订本借款协议的，或借款人与第三方签订了与本借款协议有关的易融恒信认可的其他协议的，借款人委托易融恒信在借款成功时从借款本金中直接扣除借款人应支付给第三方的相关费用，由易融恒信代为扣除并直接和第三方结算。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6、包括上述费用在内的各种收费及标准，易融恒信可事先在官方网站公告或与借款人、出借人另行签订《融资服务协议》，出借人和借款人在签订本协议前均对此完全知悉并同意以易融恒信公告或本人签字认可为准。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("第七条：违约责任", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、本协议各方均应严格履行协议义务，任何一方违约，违约方应承担因违约使其他各方产生的费用和损失，包括但不限于调查费、诉讼费、律师费等。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、出借人和借款人双方同意,若借款人出现如下任何一种情况,则本协议项下的全部借款自动提前到期,借款人在收到易融恒信发出的借款提前到期或解除合同等通知后，应立即清偿全部本金、利息、逾期利息及根据本协议产生的其他全部费用：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）借款人因任何原因逾期支付任何一期还款超过3天的；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）借款人的工作单位、职务或住所变更后，未在30天内通知易融恒信；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（3）借款人发生影响其清偿本协议项下借款的其他不利变化，未在7天内通知易融恒信。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、若借款人逾期90天支付任何一期还款的，借款人应当按照借款余额的20%支付违约金。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、借款人的每期还款均应按照如下顺序清偿：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）根据本协议产生的除本款7.4.2–7.4.6项之外的其他全部费用；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）罚息；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（3）拖欠的利息；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（4）拖欠的本金；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（5）正常的利息；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（6）正常的本金。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、双方同意,借款人有权提前清偿全部或者部分借款而不承担任何的违约责任(借款超过1日不足1个月者利息按足月计算)。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("第八条：法律适用和管辖", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、本协议的签订、履行、终止、解释均适用中华人民共和国法律,并由易融恒信注册地即武汉市江汉区人民法院管辖。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("第九条：特别条款", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、出借人保证，用于出借的资金是合法取得的资金。同时借款人保证，借款人将借款用于合法用途，不将所借款项用于任何违法活动(包括但不限于赌博、吸毒、贩毒、卖淫嫖娼等)及一切高风险投资(如证券期货、彩票等)。如借款人违反前述保证或有违反前述保证的嫌疑，则出借人有权采取下列措施：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）宣布提前收回全部借款；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）出借人向公安等有关行政机关举报,追回此款并追究借款人的刑事责任，出借人和借款人均同意并授权易融恒信采取前述措施。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、借款人和出借人均不得利用易融恒信平台进行信用卡套现和其他洗钱等不正当交易行为,若有发现，易融恒信有权向公安等有关行政机关举报,追究其相关法律责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、 借款人以各种方式提供的如下联络通讯方式以供接收与本协议有关的各项通知（包括但不限于还款通知、债权转让通知等）：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（1）移动电话（如有），其中短信方式发送的短信成功发出即为有效送达；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（2）QQ（如有），QQ消息发送成功时即为有效送达；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（3）电子邮箱（如有），邮件发送成功时即为有效送达；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（4）易融恒信提供的借款人站内短信，站内短信发送成功时即为有效送达；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（5）邮件快递：以借款人身份证（或营业执照）地址为准，收寄后三个工作日或邮件签收两者之间先到者即为有效送达；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("（6）当面送达的，以借款人签收的书面文件中落款标明的日期为有效送达日期。借款人认可上述联络通讯方式中任意一种接收到通知即对借款人产生法律效力。借款人如变更其联系方式，需书面告知，否则视为未变更联络通讯方式。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、 出借人授权易融恒信，或易融恒信委托的第三方，与借款人另行签订相关补充协议。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				
				document.add(createParagraph("第十条：其他", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、本协议采用电子文本形式制成,以网络点击的方式在易融恒信签订，出借人、借款人均委托易融恒信保管本协议。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、本协议自借款人在易融恒信发布的借款标的审核成功之日即本协议题头标明的签订日起生效。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、借款人、出借人在履行本协议过程中，应遵守易融恒信在网站上公开发布的在法律允许范围内的各项规则规定。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、出借人、借款人一致同意、授权或认可，易融恒信作为网络借款的中间平台根据本协议的规定、委托和易融恒信的其他规定行使各项权利、发出各项通知或采取各项措施，一切法律后果和风险均由借款人或出借人承担。", 
						textfont, Paragraph.ALIGN_LEFT));
			}
			document.close();
		}
	}
	
	/**
	 * 输出电子债权转让方案一   月付合同
	 * @throws DocumentException 
	 */
	@SuppressWarnings("unchecked")
	public void generatePDFMonth() throws DocumentException{
		//检查是否初始化输出内容
		if(null == contractMap || contractMap.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//获取投标流水   (甲方信息)
			List<LoanTrace> listLoanTrace = (List<LoanTrace>)contractMap.get("listLoanTrace");
			LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");
			LoanTransfer loanTransfer = (LoanTransfer)contractMap.get("loanTransfer");
			if(null == listLoanTrace || listLoanTrace.size() <= 0){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//标题
				document.add(createParagraph("易融恒信借款协议书", Titlefont, Paragraph.ALIGN_CENTER));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//添加水印
				document.add(createImage(DZHT_URL,100,620));
				UserInfo payUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("payUserCode"));
				String userCardId = "";
				try {
					userCardId = CommonUtil.decryptUserCardId(payUserInfo.getStr("userCardId"));
					userCardId = "***"+userCardId.substring(userCardId.length()-4, userCardId.length());
				} catch (Exception e) {
					e.printStackTrace();
				}
				UserInfo gotUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("gotUserCode"));
				document.add(createParagraph("甲方：  "+payUserInfo.getStr("userCardName")+"     "+userCardId,textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("乙方：  "+gotUserInfo.getStr("userCardName"),textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("",textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    甲、乙双方根据《中华人民共和国合同法》等法律之规定，在平",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("等、自愿的基础，经协商一致达成如下条款，以兹共同信守。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    一、甲方系易融恒信平台投资人，在易融恒信平台上与   "+loanInfo.getStr("userName")+"    签",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("订《易融恒信借款协议书》（借款协议号："+loanInfo.getStr("loanNo")+" ），向他人提供借款。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    二、因借款人可能存在逾期还款的可能性，甲方不愿面对此风险，",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方愿意将债权转让给乙方。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    三、为保证甲方利益、降低甲方风险，易融恒信平台引进了第三",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("方机构乙方，且乙方愿意受让该笔债权。",
						textfontBig, Paragraph.ALIGN_LEFT));
				double amount = Math.floor((loanTransfer.getInt("transFee")+1)*80/100)/100;
				document.add(createParagraph("    四、甲方将债权作价债权待收本金的80%即 "+amount+"  元转让给乙",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("方，乙方接受甲方的转让价款。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    五、乙方采用分期支付的方式,每月支付一次，分36期共支付",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("80%待收本金的方式。第一年每月支付2%，第二年每月支付3%，第三",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("年每月支付3.33%（第36期支付3.37%）。开始兑付日以投资人选择",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("方案延后一个月为准，如遇周末或节假日则顺延至节后第一个工作日",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("进行支付。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("举例如下：若投资人2018年10月28日签订本协议",
						textfontBig, Paragraph.ALIGN_CENTER));
				PdfPTable tableTitle = createTable(3);
				tableTitle.addCell(createCell("支付期数", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("支付时间", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("支付金额（元）", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("第1期", textfontBig));
				tableTitle.addCell(createCell("2018年11月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*2%", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("第12期", textfontBig));
				tableTitle.addCell(createCell("2019年10月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*2%", textfontBig));
				tableTitle.addCell(createCell("第13期", textfontBig));
				tableTitle.addCell(createCell("2019年11月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*3%", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("第24期", textfontBig));
				tableTitle.addCell(createCell("2020年10月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*3%", textfontBig));
				tableTitle.addCell(createCell("第25期", textfontBig));
				tableTitle.addCell(createCell("2020年11月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*3.33%", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("第35期", textfontBig));
				tableTitle.addCell(createCell("2021年09月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*3.33%", textfontBig));
				tableTitle.addCell(createCell("第36期", textfont));
				tableTitle.addCell(createCell("2021年10月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*3.37%", textfontBig));
				document.add(tableTitle);
				document.add(createParagraph("    六、债权转让完成后，乙方取代甲方的债权人的地位，借款人的",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("还款由乙方享有，与甲方无关。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    七、债权转让完成后，甲方委托乙方通知借款人及担保人告知债",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("权转让事宜。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    八、本协议是在双方真实意思表示下签订的，双方不得反悔，不",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("履行本协议的视为违约，因此诉至法院的，违约方还应承担守约方因",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("此支出的合理费用（包括但不限于诉讼费、律师费、保全费等）和转",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("让价款20%的违约金。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    九、双方发生争议应协商解决，协商不成的，双方同意由乙方所",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("在地人民法院管辖。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    十、甲方因自身原因和客观原因，无法签署纸质债权转让协议，",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方承诺在易融恒信平台网站浏览电子债权转让协议时点击确认签",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("署协议的行为，视为甲方已充分知晓理解合同内容，甲方同意并愿意",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("履行该债权转让协议。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方："+payUserInfo.getStr("userCardName")+"                  乙方："+gotUserInfo.getStr("userCardName"),
						textfontBig, Paragraph.ALIGN_LEFT));
				String gotDate = loanTransfer.getStr("transDate");
				document.add(createParagraph(DateUtil.getDate2Chinese(gotDate),
						textfontBig, Paragraph.ALIGN_RIGHT));
			}
			document.close();
		}
	}
	
	/**
	 * 输出电子债权转让方案一   季付合同
	 * @throws DocumentException 
	 */
	@SuppressWarnings("unchecked")
	public void generatePDFQtr() throws DocumentException{
		//检查是否初始化输出内容
		if(null == contractMap || contractMap.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//获取投标流水   (甲方信息)
			List<LoanTrace> listLoanTrace = (List<LoanTrace>)contractMap.get("listLoanTrace");
			LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");
			LoanTransfer loanTransfer = (LoanTransfer)contractMap.get("loanTransfer");
			if(null == listLoanTrace || listLoanTrace.size() <= 0){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//标题
				document.add(createParagraph("易融恒信借款协议书", Titlefont, Paragraph.ALIGN_CENTER));
				document.add(createParagraph("", 
						textfont, Paragraph.ALIGN_LEFT));
				//添加水印
				document.add(createImage(DZHT_URL,100,620));
				UserInfo payUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("payUserCode"));
				String userCardId = "";
				try {
					userCardId = CommonUtil.decryptUserCardId(payUserInfo.getStr("userCardId"));
					userCardId = "***"+userCardId.substring(userCardId.length()-4, userCardId.length());
				} catch (Exception e) {
					e.printStackTrace();
				}
				UserInfo gotUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("gotUserCode"));
				document.add(createParagraph("甲方：  "+payUserInfo.getStr("userCardName")+"     "+userCardId,textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("乙方：  "+gotUserInfo.getStr("userCardName"),textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("",textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    甲、乙双方根据《中华人民共和国合同法》等法律之规定，在平",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("等、自愿的基础，经协商一致达成如下条款，以兹共同信守。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    一、甲方系易融恒信平台投资人，在易融恒信平台上与   "+loanInfo.getStr("userName")+"    签",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("订《易融恒信借款协议书》（借款协议号："+loanInfo.getStr("loanNo")+" ），向他人提供借款。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    二、因借款人可能存在逾期还款的可能性，甲方不愿面对此风险，",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方愿意将债权转让给乙方。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    三、为保证甲方利益、降低甲方风险，易融恒信平台引进了第三",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("方机构乙方，且乙方愿意受让该笔债权。",
						textfontBig, Paragraph.ALIGN_LEFT));
				double amount = Math.floor((loanTransfer.getInt("transFee")+1)*80/100)/100;
				document.add(createParagraph("    四、甲方将债权作价债权待收本金的80%即 "+amount+"  元转让给乙",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("方，乙方接受甲方的转让价款。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    五、乙方采用分期支付的方式，每季度支付一次，分11期支",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("付,80%待收本金的的方式。第1-4期每季支付8%，第5-8期每季支",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("付9%，第9-10期每季支付10%，第11期支付12%，如遇周末或节假",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("日则顺延至节后第一个工作日进行支付。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("举例如下：若投资人2018年10月28日签订本协议",
						textfontBig, Paragraph.ALIGN_CENTER));
				PdfPTable tableTitle = createTable(3);
				tableTitle.addCell(createCell("支付期数", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("支付时间", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("支付金额（元）", textfontBig, Element.ALIGN_CENTER));
				tableTitle.addCell(createCell("第1期", textfontBig));
				tableTitle.addCell(createCell("2019年1月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*8%", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("第4期", textfontBig));
				tableTitle.addCell(createCell("2019年10月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*8%", textfontBig));
				tableTitle.addCell(createCell("第5期", textfontBig));
				tableTitle.addCell(createCell("2020年1月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*9%", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("...", textfontBig));
				tableTitle.addCell(createCell("第8期", textfontBig));
				tableTitle.addCell(createCell("2020年10月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*9%", textfontBig));
				tableTitle.addCell(createCell("第9期", textfontBig));
				tableTitle.addCell(createCell("2021年1月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*10%", textfontBig));
				tableTitle.addCell(createCell("第10期", textfontBig));
				tableTitle.addCell(createCell("2021年4月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*10%", textfontBig));
				tableTitle.addCell(createCell("第11期", textfont));
				tableTitle.addCell(createCell("2021年7月28日", textfontBig));
				tableTitle.addCell(createCell("转让价款*12%", textfontBig));
				document.add(tableTitle);
				document.add(createParagraph("    六、债权转让完成后，乙方取代甲方的债权人的地位，借款人的",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("还款由乙方享有，与甲方无关。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    七、债权转让完成后，甲方委托乙方通知借款人及担保人告知债",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("权转让事宜。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    八、本协议是在双方真实意思表示下签订的，双方不得反悔，不",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("履行本协议的视为违约，因此诉至法院的，违约方还应承担守约方因",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("此支出的合理费用（包括但不限于诉讼费、律师费、保全费等）和转",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("让价款20%的违约金。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    九、双方发生争议应协商解决，协商不成的，双方同意由乙方所",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("在地人民法院管辖。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("    十、甲方因自身原因和客观原因，无法签署纸质债权转让协议，",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方承诺在易融恒信平台网站浏览电子债权转让协议时点击确认签",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("署协议的行为，视为甲方已充分知晓理解合同内容，甲方同意并愿意",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("履行该债权转让协议。",
						textfontBig, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方："+payUserInfo.getStr("userCardName")+"                  乙方："+gotUserInfo.getStr("userCardName"),
						textfontBig, Paragraph.ALIGN_LEFT));
				String gotDate = loanTransfer.getStr("transDate");
				document.add(createParagraph(DateUtil.getDate2Chinese(gotDate),
						textfontBig, Paragraph.ALIGN_RIGHT));
			}
			document.close();
		}
	}
	
	/**
	 * 输出电子债权转让方案一   半价合同
	 * @throws DocumentException 
	 */
	public void generatePDFHalf() throws DocumentException{
		//检查是否初始化输出内容
				if(null == contractMap || contractMap.size() <= 0){
					document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
							Paragraph.ALIGN_CENTER));
				}else{
					//获取投标流水   (甲方信息)
					List<LoanTrace> listLoanTrace = (List<LoanTrace>)contractMap.get("listLoanTrace");
					LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");
					LoanTransfer loanTransfer = (LoanTransfer)contractMap.get("loanTransfer");
					if(null == listLoanTrace || listLoanTrace.size() <= 0){
						document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
								Paragraph.ALIGN_CENTER));
					}else{
						//标题
						document.add(createParagraph("易融恒信借款协议书", Titlefont, Paragraph.ALIGN_CENTER));
						document.add(createParagraph("", 
								textfont, Paragraph.ALIGN_LEFT));
						//添加水印
						document.add(createImage(DZHT_URL,100,620));
						UserInfo payUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("payUserCode"));
						UserInfo gotUserInfo = UserInfo.userInfoDao.findById(loanTransfer.getStr("gotUserCode"));
						document.add(createParagraph("甲方（债权转让方）：  "+payUserInfo.getStr("userCardName"),textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("乙方（债权受让方）：  "+gotUserInfo.getStr("userCardName"),textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("鉴于：",textfontBig, Paragraph.ALIGN_LEFT));
						String loanUserCardId = (String) contractMap.get("loanUserCardId");
						String backDate = DateUtil.getStrFromDate(
								DateUtil.getDateFromString(loanInfo.getStr("effectDate"), "yyyyMMdd"), "yyyy年MM月dd日");
						document.add(createParagraph("1、根据甲方与 "+loanInfo.getStr("userName")+"   "+loanUserCardId+" 签订的《易融恒信借",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("款协议书》，截止  "+DateUtil.getDate2Chinese(loanTransfer.getStr("gotDate"))+"，甲方对借款人享有的债权金",
								textfontBig, Paragraph.ALIGN_LEFT));
						double amount = Math.floor(loanTransfer.getInt("transAmount"))/100;
						document.add(createParagraph("额为   "+amount+"     元（其中借款本金   "+amount+"    元）；",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、甲方同意按照本协议约定将上述债权及其从权利转让给乙方，且",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("乙方同意受让上述债权及其从权利。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("鉴此，甲乙双方在平等自愿的基础上，经协商一致，就转让事宜达成",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("以下条款，以兹共同信守。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、甲方将上述债权作价债权待收本金的50%即   "+Math.floor(amount*100/2)/100+"    元转让给",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("乙方，并向易融恒信平台支付债权转让金额2%的手续费，易融恒",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("信平台同意受让该笔债权并收取债权转让金额2%的手续费。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、乙方应在本协议签订后的当日内通过江西银行将债权转让价款一",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("次性全数汇到甲方在易融恒信平台注册的江西银行电子存管账户（账",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("号：                                     ）。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、债权转让完成后，乙方成为新的债权人，取代甲方的债权人地位，",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("由乙方向借款人及担保人催收。",
								textfontBig, Paragraph.ALIGN_CENTER));
						document.add(createParagraph("4、本协议签订后，甲方委托乙方按照《易融恒信借款协议书》约定",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("的方式，就转让行为通知借款人和担保人。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("5、本协议签订后，双方不得反悔，若甲乙双方未按照协议约定履行",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("本协议的，视为违约，违约方应向守约方支付本协议约定转让价款",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("30%的违约金及乙方在清收过程中产生的费用（包括但不限于误工费、",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("交通费等），若守约方诉至法院的，违约方还应承担守约方因此支出",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("的合理费用（包括但不限于律师费、保全费等）。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6、本协议发生纠纷时，双方应协商解决，经协商无法达成一致的，",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("双方均可向乙方所在地人民法院起诉。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("7、甲方因自身原因和客观原因，无法签署纸质债权转让协议，甲方",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("承诺在易融恒信平台网站浏览电子债权转让协议时点击确认签署协",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("议的行为，视为甲方已充分知晓理解合同内容，甲方同意并愿意履行",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("该债权转让协议。",
								textfontBig, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("甲方："+payUserInfo.getStr("userCardName")+"                  乙方："+gotUserInfo.getStr("userCardName"),
								textfontBig, Paragraph.ALIGN_LEFT));
						String gotDate = loanTransfer.getStr("gotDate");
						document.add(createParagraph(DateUtil.getDate2Chinese(gotDate),
								textfontBig, Paragraph.ALIGN_RIGHT));
					}
					document.close();
				}
	}
	
}


















