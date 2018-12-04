package com.dutiantech.plugins;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.IdCardUtils;
import com.dutiantech.util.LiCai;
import com.dutiantech.util.NumberToCN;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * 房产抵押  pdf 合同输出类
 * @author shiqingsong
 *
 */
public class HouseReport extends PDFReport{

	//车辆质押  抵押  合同生成  初始化数据
	public static Map<String,String> mapInfo = new HashMap<String, String>();
	public static Map<String,String> mapLoan = new HashMap<String, String>();	
	
	static{
		//添加固定信息
		
		mapInfo.put("baileeName", "祁守华");  			//《融资服务协议》受托方姓名
		mapInfo.put("cardId", "420984198807127514");	//《融资服务协议》受托方身份证
		mapInfo.put("mobile", "18627187141");  			//《融资服务协议》受托方联系电话
		mapInfo.put("companyAddress", "武汉市江汉区马场路金华都十栋");  //《融资服务协议》受托方单位住址
		mapInfo.put("companyTel", "400-027-0707");  	//《融资服务协议》受托方单位电话

	}
	
	public HouseReport(ServletOutputStream os) {
		super(os);
	}
	
	/**
	 * 输出电子合同
	 * @throws DocumentException 
	 */
	public void generatePDF() throws DocumentException{
		
		//检查是否初始化输出内容
		if(null == mapLoan || mapLoan.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//获取乙方（出借人）信息
			String transferUserNo = mapLoan.get("transferUserNo");
			//贷款金额  并转换成大写
			Long loanAmount = Long.parseLong(mapLoan.get("loanAmount"));
			String loanAmountCN = NumberToCN.number2CNMontrayUnit(new BigDecimal(loanAmount/100));
			int limit = Integer.parseInt(mapLoan.get("limit"));
			
			//月利率
			int monthRate = Integer.parseInt(mapLoan.get("rateByMonth"));
			
			//年利率
			int rate = monthRate * 12;
			
			String refundType = mapLoan.get("refundType");
			//借款日期
			String loanDate = mapLoan.get("loanDate");
			//借款日期前一天
			String returnDate = DateUtil.delDay(loanDate, 1);
			//转换日期格式
			String dateStartCH = DateUtil.getStrFromDate(
					DateUtil.getDateFromString(loanDate, "yyyyMMdd"), "yyyy年MM月dd日");
			String dateEndCH = DateUtil.getStrFromDate(
					DateUtil.getDateFromString(CommonUtil.anyRepaymentDate4string(returnDate, limit), "yyyyMMdd"), "yyyy年MM月dd日");

			String rtype = "等额本息";
			if("B".equals(refundType)){
				rtype = "先息后本";
			}
			
			//借款人姓名
			String loanUserNames = mapLoan.get("loanUserName");
			String loanUserName = loanUserNames.split("、")[0];
			//借款人身份证号
			String loanUserCardIds = mapLoan.get("loanUserCardId");
			String loanUserCardId = loanUserCardIds.split("、")[0];
			
			//验证身份证是否合法
			if(IdCardUtils.validateCard(loanUserCardId)){
				Map<String, String> transferUser = VehiclePledgeReport.getTransferUser(transferUserNo);
				if(null == transferUser){
					document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
							Paragraph.ALIGN_CENTER));
				}else{
					
					//添加合同内容
					document.add(createParagraph("借款合同", Titlefont, Paragraph.ALIGN_CENTER));
					
					document.add(createParagraph(new Phrase[]{createPhrase("    本借款合同（以下简称“本合同”）由下列双方于"),
							createPhrase(createChunk(loanAmountCN)),createPhrase(dateStartCH),
							createPhrase("于中华人民共和国湖北省武汉市江汉区泛海国际SOHO城(一期)第2幢6层2号房签署并履行。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					
					document.add(createParagraphChunk("编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));				
					document.add(createParagraph("甲方（借款人）：	" + loanUserNames, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + loanUserCardIds, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("现住址：	" + mapLoan.get("loanUserAddress"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系方式：	" + mapLoan.get("loanUserMobile"), 
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph(new Phrase[]{createPhrase("紧急联系人1：	"),
							createPhrase(createChunk(mapLoan.get("emergencyName"))),
							createPhrase("        与借款人关系："),
							createPhrase(createChunk(mapLoan.get("emergencyRelation"))), 
							createPhrase("        紧急联系人电话："),
							createPhrase(createChunk(mapLoan.get("emergencyMobile")))} , 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("紧急联系人2：	"),
							createPhrase(createChunk(mapLoan.get("emergencyName"))),
							createPhrase("        与借款人关系："),
							createPhrase(createChunk(mapLoan.get("emergencyRelation"))), 
							createPhrase("        紧急联系人电话："),
							createPhrase(createChunk(mapLoan.get("emergencyMobile")))} , 
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("乙方（出借人）：	" + transferUser.get("name"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + transferUser.get("cardId"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("住所地：	" + transferUser.get("address"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系方式：	" + transferUser.get("mobile"),
							textfont, Paragraph.ALIGN_LEFT));
					
					
					document.add(createParagraph("一、借款金额及利息",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、借款金额：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    本合同所涉甲方向乙方借款金额为人民币（大写）"),createPhrase(createChunk(loanAmountCN)),createPhrase("，（小写）￥  "),createPhrase(createChunk(loanAmount/100 + "元")),createPhrase("（若大小写不一致，以大写为准）；")},
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("2、借款利息：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    1）、本合同借款年利率为： "), createPhrase(createChunk((float)rate/100 + "%")),createPhrase("；")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    2）、具体本息还款时间及金额明细表如下：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
		
					
					//计算还款
					LiCai licai = new LiCai(loanAmount, rate, limit);
					List<Map<String, Long>> licaiMap = new ArrayList<Map<String,Long>>();
					if("A".equals(refundType)){
						licaiMap = licai.getDengEList();
					}else{
						licaiMap = licai.getDengXiList();
					}
					
					//创建表格
					PdfPTable table = createTable(3);
					table.addCell(createCell("还款期数", keyfont, Element.ALIGN_CENTER));
					table.addCell(createCell("还款时间", keyfont, Element.ALIGN_CENTER));
					table.addCell(createCell("本息合计应还款金额（元）", keyfont, Element.ALIGN_CENTER));
					//循环输出还款明细表格
					for (int i = 0; i < licaiMap.size(); i++) {
						int month = licaiMap.get(i).get("month").intValue();
						table.addCell(createCell("第" + month + "期", keyfont));
						String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
						String date4Ch = DateUtil.getStrFromDate(
								DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy年MM月dd日");
						table.addCell(createCell(date4Ch, textfont));
	//					table.addCell(createCell("￥" + (int)Math.floor((float)licaiMap.get(i).get("benxi") / 100) , textfont));
						long a = 0;
						if(licaiMap.size() - i <= 1){
							a = licaiMap.get(i).get("benxi")+licaiMap.get(i).get("balance");
						}else{
							a = licaiMap.get(i).get("benxi");
						}
						table.addCell(createCell("￥" + new BigDecimal(a/10.0/10).setScale(0, BigDecimal.ROUND_HALF_UP) , textfont));
					}
					document.add(table);
					
					//借款用途
					document.add(createParagraph("二、借款用途：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    甲方向乙方借款系用于： "),
							createPhrase(createChunk(mapLoan.get("loanUsed"))), 
							createPhrase("  所用。甲方承诺并保证本合同借款用途的唯一性。若甲方私自改变借款用途，乙方一经发现，有权终止借款、提前收回借款或者解除本合同。乙方采取前述措施后，甲方仍需偿还借款全额的本金和利息。甲方因私自改变借款用途所产生的一切法律后果由其自行承担。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//借款期限
					document.add(createParagraph("三、借款期限：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    本合同借款期限从  "),
							createPhrase(createChunk(dateStartCH)),createPhrase(" 起至  "),
							createPhrase(createChunk(dateEndCH)),createPhrase(" 止，合计："),
							createPhrase(createChunk(limit+"")),createPhrase("期。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//借款支付方式
					document.add(createParagraph("四、借款支付方式：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    本合同签订时，甲方应当向乙方提供银行卡账户，乙方通过银行转账的方式出借借款。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("甲方账户名称  ："),
							createPhrase(createChunk(loanUserName)),createPhrase(" ；")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("开户行（全称）："),
							createPhrase(createChunk(mapLoan.get("loanBankName"))),createPhrase("  ；")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("甲方银行卡账号："),
							createPhrase(createChunk(mapLoan.get("loanBankNo"))),createPhrase("   。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//借款还款方式及还款要求
					document.add(createParagraph("五、借款还款方式及还款要求：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("1、本合同采取的借款还款方式为： "),
							createPhrase(createChunk(rtype)),createPhrase(" ，甲方根据本合同第一条第二款第二项的约定向乙方还款时，应当将还款本息存入乙方指定的下列账户。若甲方未按本条约定的还款方式还款的，乙方不予认可，视为甲方未还款。")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("账户名："),
							createPhrase(createChunk(transferUser.get("name"))),createPhrase(" ；")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("开户行："),
							createPhrase(createChunk(transferUser.get("bankName"))),createPhrase("  ；")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("卡号 ： "),
							createPhrase(createChunk(transferUser.get("bankNo"))),createPhrase("   。")},
							textfont, Paragraph.ALIGN_LEFT));
		
					document.add(createParagraph("2、还款要求",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    1）、甲方每期还款时间以实际转款日前一天为准。(例：本月8日转款，次月7日为还款日) 甲方必须按月足额偿还借款本金和利息。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    2）、甲方应当在每月还款日当日（不得迟于17:00）或之前将本合同约定的当月偿还本息金额存入乙方指定账户中；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    3）、还款日遇法定节假日或公休日时，还款日期提前，不得顺延；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    4）、如还款日为每月31日的，遇天数不足31天的，还款日为当月的最后一日；甲方未按以上要求进行还款的视为违约，按本合同约定的违约责任承担后果。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//TODO 提前还款的相关约定
					document.add(createParagraph("六、提前还款的相关约定：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、如甲方在借款期内提前还款的：甲方应当向乙方支付利息损失，利息损失为借款本金额的3%。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//TODO 质押物的处置
					document.add(createParagraph("七、本合同的变更、终止：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、自本合同签订之日起至借款本息全部清偿时止，甲方出现下列情形（包含但不限于）：甲方本人、家庭联系人及紧急联系人工作单位、居住地址、联系方式、电子邮箱等发生变更时，甲方应当将变更后的新信息在三日内向乙方提供，双方就变更后的信息签订书面协议，该书面协议作为本合同的附件；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、出现变更事宜但甲方未履行通知义务导致乙方债权受损的，由甲方承担违约责任；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3、甲方有下列情形之一的，乙方有权终止本合同：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    1）、甲方出现任何一期未能按时偿还借款本息的；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    2）、借款期间，甲方因债务纠纷被他人提请诉讼或者仲裁，涉案金额在20万元以上的或者甲方（包括其配偶）的主要资产（包括但不限于住房、商铺、银行存款、有价证券、股权、对外投资等）被司法机关查封、扣押、冻结、执行的；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    3）、借款期间乙方与甲方失去联系或者下落不明的；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    4）、其它影响甲方偿还能力的事件或者严重影响乙方实现债权的情形。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//TODO 违约责任
					document.add(createParagraph("八、违约责任",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、若甲方因逾期还款导致本合同解除的，除应当承担逾期违约金外，还需承担乙方为实现债权所支出的一切费用（包括但不限于上门所需住宿费、差旅费、律师诉讼代理费等）；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、若甲方在签订合同时提供虚假资料或者故意隐瞒重要事实的、乙方一经发现有权解除合同。合同解除后三日内甲方应当一次性支付尚未清偿的借款本金及利息，同时，还应当支付（包括但不限于）违约金及其他费用。甲方行为构成犯罪的，依法由司法机关追究其刑事责任；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3、甲方信息变更未履行通知义务导致乙方债权受损的，甲方应按借款本金总额的20%承担违约金；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("4、甲方在履行本合同过程中有任何违反合同义务或者诚实信用行为的，乙方有权保留将甲方违约失信相关信息在媒体或者同行业披露的权利；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("5、本合同签订后，乙方未按合同约定出借借款的，甲方有权解除合同。",
							textfont, Paragraph.ALIGN_LEFT));
						
						
					//TODO 本合同的债权转让
					document.add(createParagraph("九、本合同的债权转让：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、乙方有权转让本合同的债权。乙方债权转让时应当通知甲方。经双方商定，债权转让通知采取电子邮件的方式进行，乙方将《债权转让通知书》发至甲方电子邮箱后即完成通知义务，视为通知到达，无须甲方回复。甲方电子邮箱为    "+ mapLoan.get("loanUserEmail") +" ；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、债权转让后，甲方应当对债权受让人继续履行本合同对乙方的还款义务，不得以未收到债权转让通知为由拒绝履行还款义务，否则，一切后果由甲方自负。",
							textfont, Paragraph.ALIGN_LEFT));
	
					
					//TODO 合同的生效及效力
					document.add(createParagraph("十、合同的生效及效力：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、本合同自双方签章（捺手印）时生效；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、本合同及其附件的任何修改、补充均须以书面形式作出，本合同的补充协议及附件与本合同具有同等的法律效力；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3、本合同一式二份，甲、乙双方各持一份。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//TODO 争议解决方式
					document.add(createParagraph("十一、争议解决方式：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、甲、乙双方均确认，本合同的签订、生效、履行以不违反中华人民共和国的法律、法规为前提，如本合同中的任何一条或多条违反现行法律、法规的，则属于无效条款。但无效条款并不影响本合同其他条款的效力；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、本合同在履行过程中发生任何争议时，双方应本着互利互惠的原则友好协商解决；协商不成的，双方均有权向本协议签署地的人民法院提请诉讼。",
							textfont, Paragraph.ALIGN_LEFT));
					
					
					document.add(createParagraph("（以下无正文）",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("甲方（借款人）：                                  乙方（出借人）：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("日期：" + dateStartCH + "                              日期：" + dateStartCH,
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("签署地点：" + transferUser.get("companyAddress"),
							textfont, Paragraph.ALIGN_LEFT));
				
					
					
					//TODO 融资服务协议
					document.newPage();
					document.add(createParagraph("融资服务协议", Titlefont, Paragraph.ALIGN_CENTER));
					document.add(createParagraphChunk("编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));
					
					document.add(createParagraph("委托方（甲方）：	" + loanUserNames, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + loanUserCardIds, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系电话：	" + mapLoan.get("loanUserMobile"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("家庭住址：	" + mapLoan.get("loanUserAddress"), 
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("受托方（乙方）：	" + mapInfo.get("baileeName"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + mapInfo.get("cardId"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系方式：	" + mapInfo.get("mobile"),
							textfont, Paragraph.ALIGN_LEFT));				
					document.add(createParagraph("单位住址：	" + mapInfo.get("companyAddress"),
							textfont, Paragraph.ALIGN_LEFT));
	//				document.add(createParagraph("单位电话：	" + mapInfo.get("companyTel"),
	//						textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("    甲方因经营周转需要融资，委托乙方提供融资服务，并通过乙方推荐的资出借方而得到融资资金。现甲乙双方本着自愿、平等的原则，就乙方提供融资服务有关事宜达成如下协议条款：",
							textfont, Paragraph.ALIGN_LEFT));
					
					//融资服务事项
					document.add(createParagraph("一、融资服务事项",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    乙方为甲方指定融资顾问，针对甲方融资需求，为甲方寻找合适融资渠道和设计融资方案，并推荐合适的资金出借方与甲方达成签署融资借款相关合同、协议，实现甲方融资借款的目的。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//甲方义务
					document.add(createParagraph("二、甲方义务",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.1、甲方应于本协议签署时，向乙方出具授权委托书，并向乙方提交其个人身份证复印件（或组织机构的相关主体资格证照）、工作名片或工作牌（工作证）复印件、抵押担保财产证照复印件等资料，并确保所提交资料的真实性。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.2、甲方应充分向乙方真实的披露其个人信用征信状况，包括但不限于个人的银行欠款记录、司法诉讼查封记录、法院执行未结案记录、涉嫌经济犯罪之刑事违法记录等。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.3、甲方应在乙方或乙方所推荐资金出借方通知的时限内提交签署融资借款相应合同文本时所需的全部资料（如果需要原件，甲方应提交原件查验）。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.4、甲方应确保提交给乙方和乙方所推荐资金出借方的联络方式（包括但不限于居住地址、手机、电子邮箱、固定电话、传真、紧急联络人）处于畅通状态，联络方式如有变更，应及时通知乙方和乙方所推荐的资金出借方。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.5、甲方应对所提交的上述材料或个人信息的真实性、合法性、完整性负责，如因提供资料虚假或非法等原因造成乙方或资金出借方损失的，甲方应当承担一切责任，并赔偿由此造成的一切损失。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.6、甲方理解并明白，如甲方对资金出借方违约，将会严重影响乙方在资金出借方处的声誉并影响乙方融资服务的市场质量。为此，甲方应按期足额向资金出借方支付融资借款本息，如有逾期，甲方除向资金出借方承担违约责任外，还应依照本协议的约定向乙方承担违约责任。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2.7、甲方应按约定支付服务费。乙方接受委托开展融资服务工作后，一旦甲方与乙方推荐的某一（或多位）资金出借方签署融资借款相关合同、协议，并获得借款，即表示乙方已经实现融资服务，甲方应依照本合同的约定向乙方支付服务费。",
							textfont, Paragraph.ALIGN_LEFT));
	
					
					//乙方服务内容和义务
					document.add(createParagraph("三、乙方服务内容和义务",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.1、受甲方委托，代为了解资金出借方发放借款的条件，了解借款所需的基本资料，并及时向甲方提交所了解到的相关资料或政策指引。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.2、代理甲方向资金出借方提交甲方借款的个人信息资料和财产状况资料。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.3、了解和跟踪资金出借方的出借意向（包括但不限于有关具体借款额度、利率、违约金、担保要求、信用审查等），为甲方和资金出借方达成融资借款协议进行前期磋商，促成双方签署融资借款相关合同、协议。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.4、了解融资借款有关的相关办理流程，代理或协助办理有关担保抵押登记及相关事宜。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.5、甲方充分理解并同意，即使甲方与资金出借方已经达成融资借款相关合同、协议，甲方仍然授权乙方提供后续的融资服务，乙方提供的后续融资服务事项有：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.5.1、在甲方履行融资借款相关合同、协议过程中，及时提醒甲方依照融资借款相关合同、协议的约定，履行还款义务或其他义务，避免甲方不必要的违约行为发生；为此，在甲方为全额清偿资金出借方的借款本息、违约金等之前，甲方授权乙方有监督权利（包括但不限于授权乙方提醒甲方及时还款、向甲方送达还款通知、催促甲方交付抵押物、代为保管抵押物等权利）。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.5.2、在甲方履行融资借款相关合同、协议过程中因逾期还款而违约时，为避免资金出借方采取查封、强制执行等措施而影响甲方的个人信用或资产受限，或为避免甲方违约损失的扩大，甲方授权乙方有协商及代理的权利（包括但不限于向资金出借方申请借款延期、代理甲方在融资借款协议约定的条件下处置抵押物、在甲方面临资金出借方的诉讼过程中代理收取诉讼法律文书或代为委托诉讼代理人的权利等）。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.6、甲方承诺，乙方为方便行使上述事项中的代理职责时，可转委托。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3.7、甲方理解并明白，乙方为甲方提供融资服务以及在提供融资服务过程中行使代理职权时，并不表示乙方作为保证人而向资金出借方或其他方承担保证责任。甲乙双方均明确，乙方不承担任何保证担保责任。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//《借款合同》的签署
					document.add(createParagraph("四、《借款合同》的签署",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("甲方经乙方推荐，与出借人"),
							createPhrase(createChunk(transferUser.get("name"))),createPhrase("于"),
							createPhrase(createChunk(dateStartCH)),createPhrase("签署了合同编号为"),
							createPhrase(createChunk(mapLoan.get("loanNo"))),createPhrase("的《借款合同》，借款本金为人民币（大写）"),
							createPhrase(createChunk(loanAmountCN)),createPhrase("，（小写￥"),
							createPhrase(createChunk(loanAmount/10/10 + "")),createPhrase("元）。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//融资服务期
					document.add(createParagraph("五、融资服务期",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("自本协议生效之日起至甲方全额清偿资金出借方的借款本息和违约金（如有）之日止。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//融资服务期
					document.add(createParagraph("六、融资服务费用及支付方式",
							keyfont, Paragraph.ALIGN_LEFT));
					
					
					//计算融资费用
					long rzfwf = 0;
					if("A".equals(refundType)){
						rzfwf = loanAmount/limit + loanAmount*monthRate/10/10/10/10 - licaiMap.get(0).get("benxi");
					}else{
						rzfwf = loanAmount*monthRate/10/10/10/10 - licaiMap.get(0).get("xi");
					}
					
					//融资费用总额转换中文
					BigDecimal bd = new BigDecimal(rzfwf/10.0/10).setScale(0, BigDecimal.ROUND_HALF_UP);
					
					int rzfwfTotal = bd.intValue()*limit;
					//long rzfwfTotal = rzfwf*limit/10/10;
					String rzfwfTotalCH = NumberToCN.number2CNMontrayUnit(new BigDecimal(rzfwfTotal));;
					document.add(createParagraph(new Phrase[]{
							createPhrase("6.1、甲方同意向资金出借方支付融资服务费。融资服务费总额为人民币（大写）："),
							createPhrase(createChunk(rzfwfTotalCH)),createPhrase("（小写￥"),
							createPhrase(createChunk(rzfwfTotal + "")),createPhrase("元）。")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{
							createPhrase("6.2、甲乙双方经协商，同意以按月分期支付方式支付融资服务费，共分"),
							createPhrase(createChunk(limit+"")),createPhrase("期，每期"),
							createPhrase(createChunk(bd + "")),createPhrase("元（无法整除部分四舍五入），支付时间及金额见下表：")},
							textfont, Paragraph.ALIGN_LEFT));
					
					
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
	
					//创建表格
					PdfPTable table2 = createTable(3);
					table2.addCell(createCell("项目", keyfont, Element.ALIGN_CENTER));
					table2.addCell(createCell("支付时间", keyfont, Element.ALIGN_CENTER));
					table2.addCell(createCell("融资服务费金额（元）", keyfont, Element.ALIGN_CENTER));
					//循环输出还款明细表格
					for (int i = 0; i < licaiMap.size(); i++) {
						int month = licaiMap.get(i).get("month").intValue();
						table2.addCell(createCell(month+"", keyfont));
						String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
						String date4Ch = DateUtil.getStrFromDate(
								DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy年MM月dd日");
						table2.addCell(createCell(date4Ch, textfont));
						table2.addCell(createCell("￥" + bd , textfont));
					}
					document.add(table2);
	
					document.add(createParagraph("6.3、甲方同意，当甲方未按《借款合同》中约定的还款期限还款时，在甲方全额清偿资金出借方的应还款本息、违约金的同时，甲方还须向乙方支付融资服务违约金。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6.4、上述融资服务费计算日期不足一个月的，按照一个月计算。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6.5支付方式：融资服务费在资金出借人实际出借给甲方之日起按月支付。支付融资服务费日期为资金出借的当日及其每月的对应日。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6.6、上述融资服务费支付至乙方指定的账户。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6.7、乙方在协助或代理甲方办理融资服务有关事项过程中发生的所有费用由甲方承担。",
							textfont, Paragraph.ALIGN_LEFT));
	
					
					//协议的变更和终止
					document.add(createParagraph("七、协议的变更和终止",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("7.1、非经双方协商一致，任何乙方不得擅自变更协议的内容。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("7.2、在甲方与乙方推荐的资金出借方成功签订借款相关合同、协议，并取得借款资金，至甲方全额清偿资金出借方借款本息、违约金（如有）之日止，乙方即全部履行完毕协议所承担义务。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//违约责任
					document.add(createParagraph("八、违约责任",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("8.1、甲方逾期向资金出借方偿还借款本息，应向乙方支付每日按融资借款本金金额的3‰计算的融资服务违约金。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("8.2、违约方应支付对方在依法主张权力过程中发生的诉讼费或仲裁费、公告费、评估费、拍卖费、律师费、因诉讼财产保全而提供担保财产的担保费用或折旧损失或利息（按24%的利率计算）、保险、鉴定费、催告费、拖车费、保管费、登记费等费用。",
							textfont, Paragraph.ALIGN_LEFT));
													
					//争议解决
					document.add(createParagraph("九、争议解决",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    甲乙双方因本协议及履行所发生的任何争议，双方应协商解决。经协商无法达成一致，任何乙方应向本协议签署地的人民法院提起诉讼。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//其他
					document.add(createParagraph("十、其他",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("10.1、本协议自双方签字盖章之日起生效。本合同一式二份，甲乙双方各执一份。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("10.2、本协议执行中的附件（如授权委托书、融资服务确认书、甲方与资金出借方签署的融资借款法律文件等）或补充性文件，是本协议的有效组成部分，对甲、乙双方均有相应法律约束力。",
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("    甲方声明：本协议中，对免除或限制乙方责任及其条款，乙方已经作了特别提示和充分说明；甲方已全面准确理解本协议各条款及其他相关交易文件并自愿签署本协议。",
							keyfont, Paragraph.ALIGN_LEFT));
	
					document.add(createParagraph("（以下无正文）",
							textfont, Paragraph.ALIGN_LEFT));
	
					document.add(createParagraph("甲方（签字、指模）：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("乙方（签字、指模）：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("签约日期：" + dateStartCH,
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("签署地点：" + transferUser.get("companyAddress"),
							textfont, Paragraph.ALIGN_LEFT));
				}
			}else{
				document.add(createParagraph("借款人身份证输入错误!请认真填写!", Titlefont,
						Paragraph.ALIGN_CENTER));
			}
			
			document.close();
		}
	}

}



















