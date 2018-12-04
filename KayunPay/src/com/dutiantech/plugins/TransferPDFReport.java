package com.dutiantech.plugins;

import javax.servlet.ServletOutputStream;

import com.dutiantech.model.LoanInfo;
import com.dutiantech.model.LoanTrace;
import com.dutiantech.model.UserInfo;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.StringUtil;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;

/**
 * 流转表 pdf 文件输出类
 * @author shiqingsong
 *
 */
public class TransferPDFReport extends PDFReport{

	public TransferPDFReport(ServletOutputStream os) {
		super(os);
	}
	
	/**
	 * 输出电子合同
	 * @throws DocumentException 
	 */
	public void generatePDF() throws DocumentException{
		
		//检查是否初始化输出内容
		if(null == contractMap || contractMap.size() <= 0){
			document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
					Paragraph.ALIGN_CENTER));
		}else{
			//受让方信息
			UserInfo userInfo = (UserInfo)contractMap.get("userInfo");
			//获取投标流水
			LoanTrace loanTrace = (LoanTrace)contractMap.get("loanTrace");
			//借款标书信息
			LoanInfo loanInfo = (LoanInfo)contractMap.get("loanInfo");
			
			if(null == userInfo  || null == loanTrace || null == loanInfo){
				document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
						Paragraph.ALIGN_CENTER));
			}else{
				//输出标书头信息
				String loanCode = loanInfo.getStr("loanCode");
				String loanNo = loanInfo.getStr("loanNo");
				String traceCode = loanTrace.getStr("traceCode");
				
				//转让方
				String userName = loanInfo.getStr("creditorName");
				String userCardId = loanInfo.getStr("creditorCardId");
				
				//受让方
				String userNameTo = userInfo.getStr("userCardName");
				String userCardIdTo = "未知";
				try {
					userCardIdTo = CommonUtil.decryptUserCardId(userInfo.getStr("userCardId"));
				} catch (Exception e) {
					//e.printStackTrace();
				}
				
				//借款人
				String loanUserName = loanInfo.getStr("userName");
				String loanUserCardId = loanInfo.getStr("userCardId");
				if(StringUtil.isBlank(loanUserCardId) == false && loanUserCardId.length() >= 15){
					loanUserCardId = loanUserCardId.substring(0,5) + "*********" + 
							loanUserCardId.substring(loanUserCardId.length()-4,loanUserCardId.length());
				}
				
				//放款时间
				String effectDate = DateUtil.getStrFromDate(
						DateUtil.getDateFromString(loanInfo.getStr("effectDate"), "yyyyMMdd"), "yyyy年MM月dd日");
//				String effectDay = loanInfo.getStr("effectDate").substring(5, 7);
//				String gotDate = DateUtil.getStrFromDate(
//						DateUtil.getDateFromString(loanTransfer.getStr("gotDate"), "yyyyMMdd"), "yyyy年MM月dd日");
				
				//借款金额
				String loanAmount = "￥" + (loanInfo.getLong("loanAmount") / 10/10);
				//投标金额
				String payAmount = "￥" + (loanTrace.getLong("payAmount") / 10/10);
				//借款期限
				int loanTimeLimit = loanInfo.getInt("loanTimeLimit");
				//借款利率
				String rate = (float)(loanTrace.getInt("rateByYear") + loanTrace.getInt("rewardRateByYear"))/10/10 + "%";
				//还款方式
				String refundType = loanInfo.getStr("refundType").equals("A") ? "按月等额本息" : "先息后本";
				
				
				document.add(createParagraph("协议编号:" + traceCode, textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("标书编号:" + loanNo + "（"+loanCode+"）", textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("债权转让及回购协议", Titlefont, Paragraph.ALIGN_CENTER));
				
				document.add(createParagraph("甲方(转让方)：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		姓名：" + userName + "，身份证号码：" + userCardId,
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("乙方(受让方)：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		姓名：" + userNameTo + "，身份证号码：" + userCardIdTo,
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("丙方(居间人)：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		公司名称：武汉易融恒信金融信息服务有限公司，地址：武汉市江汉区中央商务区华中互联网金融产业基地-泛海国际soho城2栋6楼" ,
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("鉴于：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		1、甲方对第三方（债务人）享有借款本金和利息的债权。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		2、乙方愿意接受甲方对第三方（债务人）的债权转让。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		3、丙方是一家在武汉市江汉区合法成立并有效存续的有限责任公司，拥有www.yrhx.com 网站（以下简称“该网站”）的经营权。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		4、甲、乙、丙三方在签订本协议前务必认真仔细阅读本协议项下的全部条款。甲、乙、丙三方一旦签订本协议，即视为甲、乙、丙三方已充分理解并完全同意本协议项下的所有条款及内容。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲、乙、丙三方在平等自愿、协商一致的基础上，经充分友好协商，现达成如下债权转让及回购协议，以兹共同遵守执行。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("一、债权转让", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		1、" + effectDate + "，甲方依法与债务人"+loanUserName+"签订《借款合同》，甲方借款了人民币"+loanAmount+"元给债务人"+loanUserName+"，甲方依法对债务人"+loanUserName+"享有了借款本金及利息的债权。具体的债权情况如下：", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			借款人姓名：" + loanUserName,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			借款本金数额：" + loanAmount,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			借款年利率：" + rate,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			原借款期限：" + loanTimeLimit + "个月",
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		2、现甲方以本协议约定的条件及方式将上述债权中的部分债权计人民币"+payAmount+"元的债权(下称“出让债权”)转让给乙方，乙方同意受让。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			债权受让人：" + userNameTo,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			出让债权金额：" + payAmount,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			出让年利率：" + rate,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			出让债权还款方式：" + refundType,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("			转让日期	：" + effectDate,
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		3、在本协议签订之当日，甲方应将上述出让债权以附件一《债权转让通知书》通过债务人认可的形式通知债务人，并确认债务人"+loanUserName+"收到该《债权转让通知书》。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		4、上述债权转让自本协议签订之日起立即生效。出让债权的利息根据附件一《借款合同》约定的合同规定借款期内年利率折算成按天计算，本协议签订之前的利息归甲方所有，自本协议签订之日起至回购之日止的利息归乙方所有。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("二、债权回购", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		1、自本协议签订之日起"+loanTimeLimit+"个月后，乙方有权随时对出让债权及相关利息要求甲方予以回购。 如果乙方要求甲方回购，甲方必须无条件予以回购。出让债权自本协议签订之日起至回购之日止的利息归乙方所有。甲方回购后，出让债权的全部利息归甲方所有，甲方继续享有出让债权的一切权利。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		2、丙方承诺，如果甲方到期未按规定对出让债权及相关利息予以回购，丙方自愿立即对出让债权及相关利息无条件予以回购。出让债权自本协议签订之日起至回购之日止的利息归乙方所有。丙方回购后，出让债权的全部利息归丙方所有，丙方继续享有出让债权的一切权利。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		3、甲方或者丙方对出让债权及相关利息予以回购，即视为乙方将出让债权及相关利息又重新转让回给甲方或者丙方。乙方应将出让债权及相关利息的回购行为的《债权转让通知书》通过债务人认可的形式通知债务人 ，并确认债务人收到该《债权转让通知书》。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("三、承诺与保证", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		1、甲方承诺并保证，出让债权系甲方真实取得的债权，是合法有效的债权。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		2、甲方承诺并保证，出让债权完全清结，出让债权上不存在包括但不限于抵押、质押、被法 院查封、冻结、保全或强制执行等在内的任何权利限制以及债务负担，无任何第三人对出让债权主张任何权利。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("四、违约责任", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("本协议签定后，甲、乙、丙各方必须全面适当地履行本协议项下各自的义务及责任。任何一方对本协议项下任一条款之违反，即被视为违约，违约方须对非违约方承担相应之违约责任，赔偿非违约方因违约而遭致的包括但不限于诉讼费用、仲裁费用、律师费用等在内的一切经济损失。", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph("五、争议解决方式", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("协议履行过程中如果发生争议，协商解决，协商不成的，任何一方可提交武汉市江汉区人民法院诉讼解决。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("六、生效及其他", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("本协议自甲、乙、丙三方签字或者盖章之日起立即生效，一式三份，甲、乙、丙三方各持一份，具同等法律效力。", 
						textfont, Paragraph.ALIGN_LEFT));

				document.add(createParagraph("各方签署：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("甲方：" + userName, 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("乙方：" + userNameTo, 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("丙方：武汉易融恒信金融信息服务有限公司", 
						textfont, Paragraph.ALIGN_LEFT));
				
				//添加水印
				document.add(createImage(DZHT_URL,50,100));
				
				document.add(createParagraph("协议签订地点：湖北省武汉市江汉区", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("协议签署日期：" + effectDate, 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(" ", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("附件一：", 
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("债权转让通知书", Titlefont, Paragraph.ALIGN_CENTER));
				
				document.add(createParagraph(loanUserName + "：",
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		根据贵我双方以及武汉易融恒信金融信息服务有限公司三方于"+effectDate+"签订的《借款合同》, 我方向贵方出借了借款人民币"+loanAmount+"元。 我方决定将上述债权中的部分债权计人民币"+payAmount+"元的债权(下称“出让债权”)转让给第三方"+userNameTo+", 上述转让立即生效。出让债权的利息根据《借款合同》约定的合同规定借款期内年利率折算成按天计算, 有关出让债权今天之前的利息归我方所有,有关出让债权今天之后的利息归新债权人"+userNameTo+"所有。", 
						textfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph("		特此告知！", 
						textfont, Paragraph.ALIGN_LEFT));
				
				document.add(createParagraph(userName,
						keyfont, Paragraph.ALIGN_LEFT));
				document.add(createParagraph(effectDate, 
						keyfont, Paragraph.ALIGN_LEFT));
				
			}
			document.close();
		}
	}

}
