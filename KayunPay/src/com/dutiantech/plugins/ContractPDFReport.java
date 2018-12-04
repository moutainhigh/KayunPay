package com.dutiantech.plugins;

import java.util.List;

import javax.servlet.ServletOutputStream;

import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.TypeUtil;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * 电子合同  pdf 文件输出类
 * @author shiqingsong
 *
 */
public class ContractPDFReport extends PDFReport{

	public ContractPDFReport(ServletOutputStream os) {
		super(os);
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
			
			if(null == listLoanTrace || listLoanTrace.size() <= 0){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//输出标书头信息
				String loanNo = loanInfo.getStr("loanNo");
				String loanCode = loanInfo.getStr("loanCode");
				document.add(createParagraph("文件编号:" + loanCode, textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("借款编号:" + loanNo, textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("借款协议", Titlefont, Paragraph.ALIGN_CENTER));
				
				//输出投标流水信息
				PdfPTable table = createTable(5);
				table.addCell(createCell("甲方（出借人）：", keyfont, Element.ALIGN_LEFT, 5,false));
				table.addCell(createCell("易融恒信用户名", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借出金额", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("借款期限", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("总利率", keyfont, Element.ALIGN_CENTER));
				table.addCell(createCell("还款方式", keyfont, Element.ALIGN_CENTER));
				for (int i = 0; i < listLoanTrace.size(); i++) {
					LoanTrace loanTrace = listLoanTrace.get(i);
					table.addCell(createCell(loanTrace.getStr("payUserName"), textfont));
					table.addCell(createCell("￥" + loanTrace.getLong("payAmount") / 10/10 , textfont));
					table.addCell(createCell(loanTrace.getInt("loanTimeLimit") + "个月", textfont));
					
					//计算利率(年利率 + 奖励年利率)
					int rate = loanTrace.getInt("rateByYear") + loanTrace.getInt("rewardRateByYear");
					table.addCell(createCell( (float)rate/10/10 + "%", textfont));
					
					//转换还款方式
					String refundTypeKey = loanTrace.getStr("refundType");
					String refundTypeValue = "按月付息,到期还本";
					if("C".equals(refundTypeKey)){
						refundTypeValue = "到期还本息";
					}else if("A".equals(refundTypeKey)){
						refundTypeValue = "等额本息";
					}
					table.addCell(createCell(refundTypeValue, textfont));
				}
				document.add(table);
				
				//乙方信息
				document.add(createParagraph("乙方（借款人）：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(loanInfo.getStr("userName"), 
						textfont, Paragraph.ALIGN_LEFT));

//				document.add(createParagraph("易融恒信用户名：",
//						textfont, Paragraph.ALIGN_LEFT));
				
				//丙方信息
				document.add(createParagraph("丙方（见证人）：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("武汉易融恒信金融信息服务有限公司", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("联系方式：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("武汉市江汉区中央商务区华中互联网金融产业基地-泛海国际soho城2栋6楼",
						textfont, Paragraph.ALIGN_LEFT));
				
				//其它
				document.add(createParagraph("鉴于：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、丙方是一家在武汉市江汉区合法成立并有效存续的有限责任公司，拥有www.yrhx.com网站（以下简称“该网站”）的经营权，提供信用咨询，为交易提供信息服务；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、乙方已在该网站注册，并承诺其提供给丙方的信息是完全真实的；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、甲方承诺对本协议涉及的借款具有完全的支配能力，是其自有闲散资金，为其合法所得；并承诺其提供给丙方的信息是完全真实的；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、乙方有借款需求，甲方亦同意借款，双方有意成立借贷关系；", 
						textfont, Paragraph.ALIGN_LEFT));
				
				/**
				 * 协议条款
				 */
				//转换放款日期
				String backDate = DateUtil.getStrFromDate(
						DateUtil.getDateFromString(loanInfo.getStr("effectDate"), "yyyyMMdd"), "yyyy年MM月dd日");
				
				document.add(createParagraph("各方经协商一致，于 " + backDate +" 签订如下协议，共同遵照履行：", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第一条 借款基本信息", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("借款用途：" + TypeUtil.getLoanUsedType(loanInfo.getStr("loanUsedType")), 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("借款本金数额：￥" + (loanInfo.getLong("loanAmount") / 10/10) + "（各出借人借款本金数额详见本协议文首表格）", 
						textfont, Paragraph.ALIGN_LEFT));
				
				//计算偿还总额
				Long loanAmount = loanInfo.getLong("loanAmount");
				Integer rate = loanInfo.getInt("rateByYear")+loanInfo.getInt("rewardRateByYear");
				Integer limit = loanInfo.getInt("loanTimeLimit");
				String ltype = loanInfo.getStr("refundType");
				long[] amountAttr = CommonUtil.f_004(loanAmount, rate, limit, ltype);
				long amount = 0L;
				if(null != amountAttr && amountAttr.length == 2){
					amount = (amountAttr[0]+amountAttr[1]) / 10/10;
				}
				document.add(createParagraph("总共偿还本息数额：￥" + (amount == 0 ? "金额异常" : amount) + "（各出借人借款本金数额详见本协议文首表格）", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("还款分期月数：" + limit + "个月", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("还款日：自" + backDate + "起，每月当天（24：00前，节假日不顺延），持续" + limit + "个月", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第二条 各方权利和义务", 
						keyfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("甲方的权利和义务", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、甲方应按合同约定的借款期限起始日期将足额的借款本金支付给乙方。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、甲方享有其所出借款项所带来的利息收益。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、如乙方违约，甲方有权要求丙方提供其已获得的乙方信息，丙方应当提供。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("4、无须通知乙方，甲方可以根据自己的意愿进行本协议下其对乙方债权的转让。在甲方的债权转让后，乙方需对债权受让人继续履行本协议下其对甲方的还款义务，不得以未接到债权转让通知为由拒绝履行还款义务。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("5、甲方应主动缴纳由利息所得带来的可能的税费。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("6、如乙方实际还款金额少于本协议约定的本金、利息及违约金的，甲方各出借人同意各自按照其于本协议文首约定的借款比例收取还款。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("7、甲方应根据会员等级向丙方支付一定利息管理费，该费用在甲方获得乙方利息时自动扣除。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（1）少尉：收取利息所得的3%作为利息管理费.其中1%归入风险备用金率；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（2）中尉：收取利息所得的3%作为利息管理费.其中1%归入风险备用金率；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（3）上尉：收取利息所得的3%作为利息管理费.其中1%归入风险备用金率；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（4）少校：收取利息所得的2%作为利息管理费.其中1%归入风险备用金率；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（5）中校：收取利息所得的2%作为利息管理费.其中1%归入风险备用金率；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（6）上校：收取利息所得的1%作为利息管理费；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（7）将军：不收取利息管理费。", 
						textfont2, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("8、 甲方应确保其提供信息和资料的真实性，不得提供虚假信息或隐瞒重要事实。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("乙方权利和义务", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、 乙方必须按期足额向甲方偿还本金和利息。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、 乙方必须按期足额向丙方支付借款管理费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、 乙方承诺所借款项不用于任何违法用途。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、 乙方应确保其提供的信息和资料的真实性，不得提供虚假信息或隐瞒重要事实。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、 乙方有权了解其在丙方的信用评审进度及结果。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6、 乙方不得将本协议项下的任何权利义务转让给任何其他方。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("丙方的权利和义务", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、甲乙双方同意丙方有权代甲方每期收取甲方出借款项所对应的乙方每期偿还的本息，代收后按照甲方的要求进行处置。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、甲方同意向乙方出借相应款项时，已委托丙方在本协议生效时将该笔借款直接划付至乙方账户。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、甲乙双方同意丙方有权代甲方在有必要时对乙方进行其所借款的违约提醒及催收工作，包括但不限于电话通知、发律师函、对乙方提起诉讼等。甲方在此确认明确委托丙方为其进行以上工作，并授权丙方可以将此工作委托给其他方进行。乙方对前述委托的提醒、催收事项已明确知晓并应积极配合。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、丙方有权按月向乙方收取双方约定的借款管理费，并在有必要时对乙方进行违约提醒及催收工作，包括但不限于电话通知、发律师函、对乙方提起诉讼等。 丙方有权将此违约提醒及催收工作委托给本协议外的其他方进行。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、丙方接受甲乙双方的委托行为所产生的法律后果由相应委托方承担。如因乙方或甲方或其他方（包括但不限于技术问题）造成的延误或错误，丙方不承担任何责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6、丙方应对甲方和乙方的信息及本协议内容保密；如任何一方违约，或因相关权力部门要求（包括但不限于法院、仲裁机构、金融监管机构等），丙方有权披露。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7、丙方根据本协议对乙方进行违约提醒及催收工作时，可在其认为必要时进行上门催收提醒，即丙方派出人员（至少2名）至乙方披露的住所地或经常居住地（联系地址）处催收和进行违约提醒，同时向乙方发送催收通知单，乙方应当签收，乙方不签收的，不影响上门催收提醒的进行。丙方采取上门催收提醒的，乙方应当向丙方支付上门提醒费用，收费标准为每次人民币1000.00元，此外，乙方还应向丙方支付进行上门催收提醒服务的差旅费（包括但不限于交通费、食宿费等）。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第三条 借款管理费及居间服务费", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、在本协议中，“借款管理费”和“居间服务费”是指因丙方为乙方提供信用咨询、评估、还款提醒、账户管理、还款特殊情况沟通、本金保障等系列信用相关服务（统称“信用服务”）而由乙方支付给丙方的报酬。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、对于丙方向乙方提供的一系列信用服务，乙方同意在借款成功时向丙方支付本协议第一条约定借款本金总额的0.0%(即人民币0.00元)作为居间服务费，该“居间服务费”由乙方授权并委托丙方在丙方根据本协议规定的“丙方的权利和义务”第2款规定向乙方划付出借本金时从本金中予以扣除，即视为乙方已缴纳。在本协议约定的借款期限内，乙方应每月向丙方支付本协议第一条约定借款本金总额的0.5%(即人民币750.00元)，作为借款管理费用，共需支付12期，共计人民币9000.00元，借款管理费的缴纳时间与本协议第一条约定的还款日一致。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("本条所称的“借款成功时”系指本协议签署日。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、如乙方和丙方协商一致调整借款管理费和居间服务费时，无需经过甲方同意。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("第四条 违约责任", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、协议各方均应严格履行合同义务，非经各方协商一致或依照本协议约定，任何一方不得解除本协议。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、任何一方违约，违约方应承担因违约使得其他各方产生的费用和损失，包括但不限于调查、诉讼费、律师费等，应由违约方承担。如违约方为乙方的，甲方有权立即解除本协议，并要求乙方立即偿还未偿还的本金、利息、罚息、违约金。此时，乙方还应向丙方支付所有应付的借款管理费。如本协议提前解除时，乙方在www.yrhx.com网站的账户里有任何余款的，丙方有权按照本协议第四条第3项的清偿顺序将乙方的余款用于清偿，并要求乙方支付因此产生的相关费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、乙方的每期还款均应按照如下顺序清偿：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（1）根据本协议产生的其他全部费用；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（2）逾期罚息", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（3）逾期借款管理费", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（4）拖欠的利息；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（5）拖欠的本金；", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、乙方应严格履行还款义务，如乙方逾期还款，则应按照下述条款向甲方支付逾期罚息：罚息总额 =( 逾期本息总额+逾期借款管理费)×罚息利率0.8%×逾期天数；", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("5、如果乙方逾期支付任何一期还款超过30天，或乙方在逾期后出现逃避、拒绝沟通或拒绝承认欠款事实等恶意行为，本协议项下的全部借款本息及借款管理费均提前到期，乙方应立即清偿本协议下尚未偿付的全部本金、利息、罚息、借款管理费及根据本协议产生的其他全部费用。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("6、如果乙方逾期支付任何一期还款超过30天，或乙方在逾期后出现逃避、拒绝沟通或拒绝承认欠款事实等恶意行为，丙方有权将乙方的“逾期记录”记入人民银行公民征信系统，丙方不承担任何法律责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("7、如果乙方逾期支付任何一期还款超过30天，或乙方在逾期后出现逃避、拒绝沟通或拒绝承认欠款事实等恶意行为，丙方有权将乙方违约失信的相关信息及乙方其他信息向媒体、用人单位、公安机关、检查机关、法律机关披露，丙方不承担任何责任。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("8、在乙方还清全部本金、利息、借款管理费、罚息、逾期管理费之前，罚息及逾期管理费的计算不停止", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("9、本协议中的所有甲方与乙方之间的借款均是相互独立的，一旦乙方逾期未归还借款本息，甲方中的任何一个出借人均有权单独向乙方追索或者提起诉讼。如乙方逾期支付借款管理费或提供虚假信息的，丙方亦可单独向乙方追索或者提起诉 讼。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("10、甲方和乙方均同意，如乙方逾期当天仍未清偿借款本息,则甲方将本协议项下的债权转让给丙方；转让价格为不超过乙方逾期仍未偿还的借款本息；丙方向甲方支付乙方逾期仍未偿还的本息即可取得甲方在本协议项下的债权，有权向乙方追收借款本金、利息、逾期罚息等；坏账风险由丙方承担，丙方依法清收所产生的费用由乙方承担。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("第五条 提前还款", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("1、乙方可在借款期间任何时候提前偿还剩余借款。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("2、提前偿还全部剩余借款", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（1）乙方提前清偿全部剩余借款时，应向甲方支付当期应还本息。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（2）乙方提前清偿全部剩余借款时，应向丙方支付当期借款管理费，乙方无需支付剩余还款期的借款管理费。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("3、提前偿还部分借款", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（1）乙方提前偿还部分借款，仍应向甲方支付全部借款利息。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		（2）乙方提前偿还部分借款，仍应向丙方支付全部应付的借款管理费。", 
						textfont2, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("4、任何形式的提前还款不影响丙方向乙方收取在本协议第三条中说明的居间服务费。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("第六条 法律及争议解决", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("本协议的签订、履行、终止、解释均适用中华人民共和国法律，如因本协议发生争议，甲、乙、丙三方一致同意由丙方所在地武汉市硚口区人民法院管辖。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("第七条 附则", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		1、本协议采用电子文本形式制成，并永久保存在丙方为此设立的专用服务器上备查，各方均认可该形式的协议效力。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		2、本协议自文本最终生成之日生效。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		3、本协议签订之日起至借款全部清偿之日止，乙方或甲方有义务在下列信息变更三日内提供更新后的信息给丙方：本人、本人的家庭联系人及紧急联系人、工作单位、居住地址、住所电话、手机号码、电子邮箱、银行账户的变更。若因任何一方不及时提供上述变更信息而带来的损失或额外费用应由该方承担。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		4、如果本协议中的任何一条或多条违反适用的法律法规，则该条将被视为无效，但该无效条款并不影响本协议其他条款的 效力。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph(backDate, 
						keyfont, Paragraph.ALIGN_RIGHT));
			
			}
			document.close();
		}
	}
}
