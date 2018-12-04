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
import com.dutiantech.util.StringUtil;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;

/**
 * 车辆质押  pdf 合同输出类
 * @author shiqingsong
 *
 */
public class VehiclePledgeReport extends PDFReport{

	//车辆质押  抵押  合同生成  初始化数据
	public static List<Map<String,String>> listTransferUser = new ArrayList<Map<String,String>>();
	public static Map<String,String> mapInfo = new HashMap<String, String>();
	public static Map<String,String> mapLoan = new HashMap<String, String>();
	
	
	/**
	 * 获取利率
	 * @param limit
	 * @param b
	 * @return
	 */
	private int getRate(int limit,boolean b){
		if(b){
			if(limit >= 1 && limit <=3){
				return 1200;
			}
			else if(limit >= 4 && limit <=6){
				return 1330;
			}
			else if(limit >= 7 && limit <=12){
				return 1440;
			}
			else if(limit >= 13 && limit <= 18){
				return 1500;
			}
			else if(limit > 18){
				return 1500;
			}
		}else{
			if(limit >= 1 && limit <=3){
				return 1260;
			}
			else if(limit >= 4 && limit <=6){
				return 1330;
			}
			else if(limit >= 7 && limit <=12){
				return 1440;
			}
			else if(limit >= 13 && limit <= 18){
				return 1500;
			}
			else if(limit > 18){
				return 1500;
			}
		}
		return 1;
	}
	
	
	static{
		//添加固定信息
		
		mapInfo.put("baileeName", "张妮");  			//《融资服务协议》受托方姓名
		mapInfo.put("cardId", "420102198301012000");	//《融资服务协议》受托方身份证
		mapInfo.put("mobile", "15071388666");  			//《融资服务协议》受托方联系电话
		mapInfo.put("companyAddress", "武汉市江岸区湖边坊257号8楼2号");  //《融资服务协议》受托方单位住址
		mapInfo.put("companyTel", "400-027-0707");  	//《融资服务协议》受托方单位电话

		
		//添加乙方(出借人)信息
		//顺序     [编号, 姓名,身份证号,住所地,联系方式,银行卡帐号,账户姓名,开户行(全称),区域,单位名称,单位电话,单位住址,企业执照,《个人授权委托书》受托人姓名,《个人授权委托书》受托人身份证]
		listTransferUser.add(
				setTransferUser("0,____________________,____________________,____________________,____________________,____________________,____________________,____________________,________,____________________,____________________,____________________,____________________,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("1,肖松,420984198706259091,湖北省汉川市仙女山街道办事处北桥村1-107,18871209888,6214 8327 0582 9716,肖松,招商银行武汉分行营业部,贵阳,武汉中州车商务咨询有限公司贵阳分公司,____________________,贵州省贵阳市南明区花果园金融街金融大厦24楼2403A,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("2,祁守江,420984198909127515,湖北省汉川市韩集乡窑场片115号,18062116767,6214 8327 0462 7665,祁守江,招行武汉汉街支行,昆明,武汉中州车商务咨询有限公司昆明分公司,____________________,西山区云纺商业城东南亚B座1504,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("3,祁守江,420984198909127515,湖北省汉川市韩集乡窑场片115号,18062116767,6214 8327 0633 8907,祁守江,招行武汉硚口支行,曲靖,武汉中州车商务咨询有限公司曲靖分公司,____________________,曲靖市麒麟区曲一中正对面书苑彼岸A栋,____________________,刘梦琪,420115199508120062,曲靖市区")
		);
		listTransferUser.add(
				setTransferUser("4,祁守江,420984198909127515,湖北省汉川市韩集乡窑场片115号,18062116767,6214 8502 7699 6953,祁守江,招行武汉硚口支行,楚雄,武汉中州车商务顾问有限公司楚雄分公司,0878-3212818,云南省楚雄彝族自治州楚雄市鹿城镇鹿城北路70号鑫茂时代雅居808室,91532301MA6K4LGC5P,刘梦琪,420115199508120062,楚雄市")
		);
		listTransferUser.add(
				setTransferUser("5,李少华,420111197709274450,武汉市洪山区珞狮路186号1栋2单元2楼2号,18671732920,6236 6828 3000 2124 369,李少华,宜昌建行江海路支行,宜昌,宜昌易融恒信商务咨询有限公司,0717-6449666,湖北省宜昌市伍家岗区沿江大道特168-5号,420500000215792,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("6,林有才,420106196401190431,武汉市武昌区三角路水岸国际,13995555864,6212 2632 0201 7470 567,林有才,中国工商银行武汉梦湖水岸支行,汉口,武汉重盈恒信商务咨询有限公司,____________________,武汉市江岸区瑞通广场802室,____________________,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("7,林有才,420106196401190431,武汉市武昌区三角路水岸国际,13995555864,6212 2632 0201 7470 567,林有才,中国工商银行武汉梦湖水岸支行,武昌,武汉重盈恒信商务咨询有限公司,____________________,武汉市武昌区三角路水岸国际,____________________,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("8,叶成,420106197909242436,武汉市武昌区三角路水岸国际K6-1栋27楼,17786410073,6217 0028 7004 7715 956,叶成,中国建设银行天门墩支行,武昌,武汉重盈恒信商务咨询有限公司,____________________,武汉市武昌区三角路水岸国际,91420106MA4KMA5447,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("9,余华倩,420582199205110045,湖北省当阳市广州路锦绣江南9-203,15090926588,6214 8327 0569 6875,余华倩,招行武汉硚口支行,乌鲁木齐,____________________,____________________,新疆乌鲁木齐市天山区光明路59号时代广场A座8-1,____________________,刘梦琪,420115199508120062,乌鲁木齐市区")
		);
		listTransferUser.add(
				setTransferUser("10,余华倩,420582199205110045,湖北省当阳市广州路锦绣江南9-203,15090926588,6214 8327 0645 9323,余华倩,招行武汉积玉桥支行,荆门,____________________,____________________,湖北省荆门市掇刀区虎牙关9号星球酒店,____________________,刘梦琪,420115199508120062,荆门市区")
		);
		listTransferUser.add(
				setTransferUser("11,余华倩,420582199205110045,湖北省当阳市广州路锦绣江南9-203,15090926588,6214 8327 0275 7746,余华倩,招行武汉分行,襄阳,武汉中州车商务顾问有限公司襄阳分公司,0710-3270580,襄阳市高新区长虹路9号万达写字楼1507、1508,91420600MA4896MH6J,刘梦琪,420115199508120062,襄阳市区")
		);
		
		listTransferUser.add(
				setTransferUser("12,祁守华,420984198807127514,湖北省汉川市韩集乡窑场片112号,18627187141,6214 8502 7702 9903,祁守华,招行武汉积玉桥支行,遵义,遵义市中州车汽车服务有限公司,0851-28611180,贵州省遵义市汇川区大连路航天大厦06层607号,91520303MA6DLGQ700,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("13,祁守华,420984198807127514,湖北省汉川市韩集乡窑场片112号,18627187141,6214 8327 0616 3677,祁守华,招行武汉积玉桥支行,娄底,____________________,____________________,____________________,____________________,刘梦琪,420115199508120062,____________________")
		);
		
		listTransferUser.add(
				setTransferUser("14,梅龙,420583198802040059,湖北省枝江市马家店街办民主大道31号,18507202099,6236 6826 6000 1222 884,梅龙,建行荆州北湖支行,荆州,荆州市重鑫信息服务有限公司,0716-8353099,荆州区荆沙路万达广场B座712-713号,421000000189559,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("15,李翔,420583199304200034,湖北省枝江市马家店街办迎宾大道17号,18672652458,6236 6827 5000 0446 665,李翔,中国建行恩施叶挺路支行,恩施,恩施市重鑫信息服务有限公司,18672652458,湖北省恩施市叶挺路阳光国际8栋1702室,91422801MA4899PX4B,刘梦琪,420115199508120062,____________________")
		);
		listTransferUser.add(
				setTransferUser("16,余华倩,420582199205110045,湖北省当阳市广州路锦绣江南9-203,15090926588,6214 8502 7644 3436,余华倩,招行武汉积玉桥支行,武昌,武汉中州车商务顾问有限公司青山分公司,027-62438338,武汉市武昌区中北路汉街国际总部E座1906室,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("17,黄娟,410304198111042022,武汉市江汉区中央商务区泛海国际SOHO城（一期）第2栋6层2号房,13995655393,6227 0754 0068 6727,黄娟,建行武汉中央商务区支行,武汉,武汉中州车商务顾问有限公司武汉分公司,027-83356113,湖北省武汉市江汉区泛海国际SOHO城(一期)第2幢6层2号房,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("18,鲁超,420104198106050854,乌鲁木齐市北京南路康源财富中心8楼,18672343338,6228 2708 9124 3076 078,鲁超,农业银行乌鲁木齐市分行北京南路支行,乌鲁木齐,____________________,____________________,新疆乌鲁木齐市北京南路康源财富中心8楼,____________________,刘梦琪,420115199508120062,乌鲁木齐市区")
		);
		listTransferUser.add(
				setTransferUser("19,潘高峰,420924198007097592,贵州省 铜仁市 碧江区 金滩半岛豪苑C栋二单元19楼,15871114276,6214 8389 0403 2093,潘高峰,贵阳大十字支行,铜仁,____________________,____________________,贵州省铜仁市碧江区金滩半岛豪苑C栋二单元19楼,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("20,龚启明,34282619660727613X,安徽省安庆市迎江区皖江大道北侧迎江世纪城紫峰大厦A座九层叠 1室,15391925339,6214 8355 6071 9922,龚启明,招行安庆分行,安庆,____________________,____________________,安徽省安庆市迎江区皖江大道北侧迎江世纪城紫峰大厦A座九层叠 1室,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("21,曾莉军,420984197912016037,湖北省汉川市里潭乡新正街曾坡村,13581477966,6214 8599 1258 6748,曾莉军,乌鲁木齐鲤鱼山路支行,乌鲁木齐,____________________,____________________,乌鲁木齐市人民路2号乌鲁木齐大厦12楼A座中州车贷,____________________,刘梦琪,420115199508120062,乌鲁木齐市")
		);
		listTransferUser.add(
				setTransferUser("22,金红梅,420124197910245928,武汉市新洲区仓埠街宋咀村龙湾一组9号,18086496227,6225 8802 7958 0170,金红梅,招商银行武汉分行青山支行,乌鲁木齐,____________________,____________________,新疆乌鲁木齐市新市区北京南路223号康源财富综合楼8层办公4,____________________,刘梦琪,420115199508120062,乌鲁木齐市区")
		);
//		listTransferUser.add(
//				setTransferUser("23,汪宇轩,420984200009111737,湖北省汉川市分水镇鲜鱼沟村 374号,13507299110,6222 0330 0200 1842 252,汪宇轩,工商银行乌鲁木齐光明路支行,伊犁,____________________,____________________,新疆伊犁哈萨克自治州伊宁市新华西路705号融和大厦B座1625号,____________________,刘梦琪,420115199508120062,三环")
//		);
		listTransferUser.add(
				setTransferUser("23,祁守华,420984198608037516,湖北省汉川市韩集乡窑场片112号,17762566657,6214 8302 7036 8581,祁守华,招商银行武汉光谷支行,伊犁,武汉中州车商务顾问有限公司伊犁分公司,0999-8987228,新疆伊犁州伊宁市新华西路705号融和大厦综合楼1625号,____________________,刘梦琦,420115199508120062,伊宁市")
		);
		listTransferUser.add(
				setTransferUser("24,洪灿育,342826196911213918,安徽省安庆市宿松县长铺镇长铺社区西街组30号,13955618388,6214 8379 2158 4508,洪灿育,招商银行九江八里湖支行,九江,武汉中州车商务顾问有限公司九江分公司,____________________,江西省九江市九江经济技术开发区九瑞大道九龙世纪城小区1号楼龙德大厦9F,____________________,刘梦琪,420115199508120062,三环")
		);
		listTransferUser.add(
				setTransferUser("25,林玲,420106197911130441,武汉市武昌区柴林头东区79-1号,15802756731,6214832714737546,林玲,徐东分行,克拉玛依,武汉中州车商务顾问有限公司克拉玛依分公司,0999-6264770,新疆克拉玛依市克拉玛依区新兴路恒隆广场B座5楼507号,____________________,刘梦琦,420115199508120062,克拉玛依市")
				);
	}
	
	private static Map<String,String> setTransferUser(String str){
		String[] strArr = str.split(",");
		if(strArr.length < 16){
			return null;
		}
		Map<String,String> map = new HashMap<String, String>();
		map.put("transferUserNo", strArr[0]);	// 债权人编号
		map.put("name", strArr[1]);	// 债权人姓名
		map.put("cardId", strArr[2]);	// 债权人身份证号
		map.put("address", strArr[3]);	// 债权人住所地
		map.put("mobile", strArr[4]);	// 债权人手机号
		map.put("bankNo", strArr[5]);	// 债权人银行卡号
		map.put("bankUserName", strArr[6]);	// 债权人银行卡姓名
		map.put("bankName", strArr[7]);	// 债权人银行开户行（全称）
		map.put("area", strArr[8]);	// 债权人所属区域
		map.put("companyName", strArr[9]);	// 单位名称
		map.put("companyTel", strArr[10]);	// 单位电话号码
		map.put("companyAddress", strArr[11]);	// 单位地址
		map.put("license", strArr[12]);	// 企业执照	
		map.put("trusteeName", strArr[13]);	// 委托人姓名	
		map.put("trusteeCardId", strArr[14]);	// 委托人身份证号
		map.put("area2", strArr[15]);	// 委托人所属地区
		return map;
	}
	
	public static Map<String,String> getTransferUser(String transferUserNo){
		for (int i = 0; i < listTransferUser.size(); i++) {
			if(transferUserNo.equals(listTransferUser.get(i).get("transferUserNo"))){
				return listTransferUser.get(i);
			}
		}
		return null;
	}
	
	public VehiclePledgeReport(ServletOutputStream os) {
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
			
			//合同类型
			boolean b = true;
			if(mapLoan.get("product").equals("全款车抵押") || mapLoan.get("product").equals("按揭车GPS")){
				b = false;
			}
			//利率
			int rate = getRate(limit,b);
			int monthRate = Integer.parseInt(mapLoan.get("rateByMonth"));
			
			int monthRate_wy = monthRate;
			//违约利率
			if(limit > 6){
				monthRate_wy = monthRate * 2;
			}
//			DecimalFormat df = new DecimalFormat("0.00");
//			
//			
//			int yearRate = monthRate * 12;
			
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

			//兼容2月
			if(DateUtil.getNowDate().endsWith("01")){
				
				String date4string = CommonUtil.anyRepaymentDate4string(returnDate, limit);
				String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
						Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
//				String end4month = CommonUtil.getFirstDataAndLastDateByMonth(0, 0, "yyyyMMdd")[1].substring(6, 8);
				dateEndCH = dateEndCH.substring(0,dateEndCH.length()-3) + end4month + "日";
			}
			
			String rtype = "等额本息";
			int centerRate = 2400;
			if("B".equals(refundType)){
				rtype = "先息后本";
				//centerRate = 2700;
			}
			
			//借款人姓名
			String loanUserNames = mapLoan.get("loanUserName");
			String loanUserName = loanUserNames.split("、")[0];
			//借款人身份证号
			String loanUserCardIds = mapLoan.get("loanUserCardId");
			String loanUserCardId = loanUserCardIds.split("、")[0];
			
			//验证身份证是否合法
			if(IdCardUtils.validateCard(loanUserCardId)){
				Map<String, String> transferUser = getTransferUser(transferUserNo);
				if(null == transferUser){
					document.add(createParagraph("对不起,当前合同内容有异常,请稍后再试试.", Titlefont,
							Paragraph.ALIGN_CENTER));
				}else{
					
					//添加合同内容
					if(b){
						document.add(createParagraph("车辆质押借款合同", Titlefont, Paragraph.ALIGN_CENTER));
					}else{
						document.add(createParagraph("车辆抵押借款合同", Titlefont, Paragraph.ALIGN_CENTER));
					}
					
					document.add(createParagraphChunk("编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));				
					document.add(createParagraph("甲方（借款人）：	" + loanUserNames, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + loanUserCardIds, 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("住所地：	" + mapLoan.get("loanUserAddress"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系方式：	" + mapLoan.get("loanUserMobile"), 
							textfont, Paragraph.ALIGN_LEFT));
					
//					document.add(createParagraph(new Phrase[]{createPhrase("甲方紧急联系人1：	"),
//							createPhrase(createChunk(mapLoan.get("emergencyName"))),
//							createPhrase("        与甲方关系："),
//							createPhrase(createChunk(mapLoan.get("emergencyRelation"))), 
//							createPhrase("        联系方式："),
//							createPhrase(createChunk(mapLoan.get("emergencyMobile")))} , 
//							textfont, Paragraph.ALIGN_LEFT));
//					document.add(createParagraph(new Phrase[]{createPhrase("甲方紧急联系人2：	"),
//							createPhrase(createChunk(mapLoan.get("emergencyName"))),
//							createPhrase("        与甲方关系："),
//							createPhrase(createChunk(mapLoan.get("emergencyRelation"))), 
//							createPhrase("        联系方式："),
//							createPhrase(createChunk(mapLoan.get("emergencyMobile")))} , 
//							textfont, Paragraph.ALIGN_LEFT));
//					document.add(createParagraph(new Phrase[]{createPhrase("甲方紧急联系人3：	"),
//							createPhrase(createChunk(mapLoan.get("emergencyName"))),
//							createPhrase("        与甲方关系："),
//							createPhrase(createChunk(mapLoan.get("emergencyRelation"))), 
//							createPhrase("        联系方式："),
//							createPhrase(createChunk(mapLoan.get("emergencyMobile")))} , 
//							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("乙方（出借人）：	" + transferUser.get("name"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + transferUser.get("cardId"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("住所地：	" + transferUser.get("address"),
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系方式：	" + transferUser.get("mobile"),
							textfont, Paragraph.ALIGN_LEFT));
					
					
					if(b){
						document.add(createParagraph("甲、乙双方经自愿、友好协商，就甲方以车辆作为质押物而向乙方借款事宜，一致达成以下合同：",
								textfont, Paragraph.ALIGN_LEFT));
					}else{
						document.add(createParagraph("甲、乙双方经自愿、友好协商，就甲方以车辆作为抵押物而向乙方借款事宜，一致达成以下合同：",
								textfont, Paragraph.ALIGN_LEFT));
					}
//					document.add(createParagraph("甲、乙双方经自愿、友好协商，就甲方以车辆作为质押物而向乙方借款事宜，一致达成以下合同：",
//							textfont, Paragraph.ALIGN_LEFT));
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
						
						//兼容2月
						if(DateUtil.getNowDate().endsWith("01")){
							String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
									Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
							date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
						}
						
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
					
					//质押物
					if(b){
						document.add(createParagraph("二、质押物：",
								keyfont, Paragraph.ALIGN_LEFT));
					}else{
						document.add(createParagraph("二、抵押物：",
								keyfont, Paragraph.ALIGN_LEFT));
					}
					String cars = mapLoan.get("cars");
					if(StringUtil.isBlank(cars) == false){
						String[] carArr = cars.split(",");
						for (int i = 0; i < carArr.length; i++) {
							String[] carinfoArr = carArr[i].split("\\|");
							document.add(createParagraph(new Phrase[]{createPhrase("  "),
									createPhrase(createChunk(carinfoArr[0])), 
									createPhrase(" 品牌， "),createPhrase(createChunk(carinfoArr[1])),
									createPhrase(" 型号的汽车壹辆，车辆牌号为： "),
									createPhrase(createChunk(carinfoArr[2])), createPhrase(" ，发动机号为： "),
									createPhrase(createChunk(carinfoArr[3])),createPhrase("  ，")},
									textfont, Paragraph.ALIGN_LEFT));
							document.add(createParagraph(new Phrase[]{createPhrase("车架号为：  "), createPhrase(createChunk(carinfoArr[4])),
									createPhrase("   。")},
									textfont, Paragraph.ALIGN_LEFT));
						}
					}
				
					//借款用途
					document.add(createParagraph("三、借款用途：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    甲方向乙方借款系用于： "),
							createPhrase(createChunk(mapLoan.get("loanUsed"))), 
							createPhrase("  所用。甲方承诺并保证本合同借款用途的唯一性。若甲方私自改变借款用途，乙方一经发现，有权终止借款、提前收回借款或者解除本合同。乙方采取前述措施后，甲方仍需偿还借款全额的本金和利息。甲方因私自改变借款用途所产生的一切法律后果由其自行承担。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//借款期限
					document.add(createParagraph("四、借款期限：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("    本合同借款期限从  "),
							createPhrase(createChunk(dateStartCH)),createPhrase(" 起至  "),
							createPhrase(createChunk(dateEndCH)),createPhrase(" 止，合计："),
							createPhrase(createChunk(limit+"")),createPhrase("期。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					//借款支付方式
					document.add(createParagraph("五、借款支付方式：",
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
					document.add(createParagraph("六、借款还款方式及还款要求：",
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
					document.add(createParagraph("    4）、如还款日为每月31日的，遇天数不足31天的月份，还款日为当月的最后一日；甲方未按以上要求进行还款的视为违约，按本合同约定的违约责任承担后果。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//TODO 提前还款的相关约定
					document.add(createParagraph("七、提前还款的相关约定：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、经乙方书面同意，甲方可提前归还借款本金。甲方借款时间不足一个月的，利息按一个月计付。",
							textfont, Paragraph.ALIGN_LEFT));
					if("宜昌".equals(transferUser.get("area"))||"荆州".equals(transferUser.get("area"))||"恩施".equals(transferUser.get("area"))){
						document.add(createParagraph("2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金收取方式为：借款期限在半年以内的，收取一个月利息为违约金；借款期限超过半年的，收取两个月利息为违约金。",
								textfont, Paragraph.ALIGN_LEFT));
					}else{
						
						if("青山".equals(transferUser.get("area")) || "曲靖".equals(transferUser.get("area"))
								|| "昆明".equals(transferUser.get("area")) || "楚雄".equals(transferUser.get("area"))
								|| "贵阳".equals(transferUser.get("area")) || "乌鲁木齐".equals(transferUser.get("area"))
								|| "荆门".equals(transferUser.get("area")) || "襄阳".equals(transferUser.get("area"))
								|| "娄底".equals(transferUser.get("area")) || "遵义".equals(transferUser.get("area"))
								|| "武汉".equals(transferUser.get("area")) || "________".equals(transferUser.get("area"))
								|| "安庆".equals(transferUser.get("area")) || "铜仁".equals(transferUser.get("area"))
								|| "武昌".equals(transferUser.get("area")) || "九江".equals(transferUser.get("area"))
								|| "伊犁".equals(transferUser.get("area")) || "克拉玛依".equals(transferUser.get("area"))){
							document.add(createParagraph("2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金为一个月利息。",
									textfont, Paragraph.ALIGN_LEFT));
						}else{
							document.add(createParagraph("2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金为借款本金的 " + (float)monthRate_wy/100 + "%" ,
									textfont, Paragraph.ALIGN_LEFT));
						}
					}
					
					if(b){
						//质押担保
						document.add(createParagraph("八、质押担保",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、甲方自愿以其合法享有的本合同第二条约定的质押车辆作为质押物，为甲方在本合同中约定的债务（包括借款本金、利息、违约金以及乙方实现主债权和质权等的全部费用）提供担保。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、甲方应在本合同签订当日将该质押车辆交付给乙方占有，从乙方实际收到甲方交付的该质押车辆时质权设立。在甲方清偿完毕本合同全部借款本息后，乙方将质押车辆交还甲方。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、甲方履行完毕本合同约定的全部义务前，未经乙方同意，甲方不得转让质押车辆，也不得就该车辆再设定担保物权或其他任何第三方权益。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("4、甲方需保证在本合同签订之时并且在履行完毕本合同约定的全部义务之前，没有第三人对质押车辆享有任何权利（若该质押车辆为按揭车辆，则按揭贷款的债权人除外），并且质押车辆的交强险、商业险持续有效。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("5、甲方保证自己在本合同签订之时不涉及任何诉讼或仲裁。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6、在质权存续期间，乙方应负责妥善保管质押车辆，并不得挪用，但因此产生的车辆保管费、停车费等相关费用由甲方承担。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("7、甲方应按乙方要求，对质押车辆办理财产保险，并将保险单交乙方保存。投保期限应当长于本合同约定期限。如本合同经双方同意延长期限的，甲方应办理延长投保期限的手续。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("8、甲方必须连同与抵押车辆相关的钥匙、机动车登记证书、行车证、购车发票、购置税、养路费、保险单据、户口本原件及身份证复印件交由乙方保管。",
								textfont, Paragraph.ALIGN_LEFT));
			
						
						//质押物的处置
						document.add(createParagraph("九、质押物的处置：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    若甲方不能承担本合同约定的违约责任时，甲方同意乙方有权按以下方式之一处置质押车辆：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、由乙方变卖质押车辆，变卖的价格以乙方实际收取的变卖价款为准，变卖价款在扣除变卖费用及借款本息、违约金及合同约定的费用和依法应缴纳的税费后，如有剩余部分由乙方退还甲方，如有不足，则乙方有权向甲方继续追偿。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、由乙方委托有拍卖资质的相关企业公开拍卖，拍卖价款在扣除拍卖费用及借款本息、违约金及合同约定的费用和依法应缴纳的税费后，如有剩余部分由拍卖行或乙方退还甲方，如有不足，则乙方有权向甲方继续追偿。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、甲方同意以其所欠借款本息及违约金总额作为乙方购买该车的全额购车款，将车辆出售给乙方，并且全力配合乙方办理质押车辆所有权变更登记手续，因此所产生的费均用由甲方承担。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//按揭车辆的相关规定
						document.add(createParagraph("十、按揭车辆的相关规定：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    若甲方提供的质押车辆为按揭车辆，则甲方与乙方签订的《借款合同》借款期限的最后截止日期（或甲方提前全部还款日期）若晚于甲方提供的质押车辆在银行按揭贷款或其他任何形式贷款的到期时间的，则甲方应当自该质押车辆按揭贷款全部结清后立即通知乙方并与乙方另行签订抵押担保的借款合同及相关协议，该相关合同、协议生效后本合同终止，否则乙方有权要求甲方按本合同第七条约定的方式提前还款，若甲方不能立即清偿债务的，乙方有权依本合同第九条约定的处置方式之一处置该质押车辆。",
								textfont, Paragraph.ALIGN_LEFT));
					
						//本合同的变更、终止
						document.add(createParagraph("十一、本合同的变更、终止：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、自本合同签订之日起至借款本息全部清偿时止，甲方出现下列情形（包含但不限于）：甲方本人、家庭联系人及紧急联系人工作单位、居住地址、联系方式、电子邮箱等发生变更时，甲方应当将变更后的新信息在三日内向乙方提供，双方就变更后的信息签订书面协议，该书面协议作为本合同的附件；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、甲方有下列情形之一的，乙方有权终止本合同：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    1）、甲方出现任何一期未能按时偿还借款本息的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    2）、借款期间，甲方因债务纠纷被他人提请诉讼或者仲裁，涉案金额在20万元以上的或者甲方（包括其配偶）的主要资产（包括但不限于住房、商铺、银行存款、有价证券、股权、对外投资等）被司法机关查封、扣押、冻结、执行的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    3）、借款期间乙方与甲方失去联系或者下落不明的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    4）、其它影响甲方偿还能力的事件或者严重影响乙方实现债权的情形。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、本合同签订后，乙方未按合同约定出借借款的，甲方有权解除合同。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//违约责任
						document.add(createParagraph("十二、违约责任",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、若甲方迟于本合同第一条第二款第二项约定的还款时间还款的，视为甲方逾期还款，甲方除必须按本合同还款计划表规定的金额向乙方正常偿付本息外甲方应当承担逾期还款违约金。逾期违约金每日按借款本金总额的1‰计算。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、若甲方因逾期还款导致本合同解除的，除应当承担逾期违约金外，还需承担乙方为实现债权所支出的一切费用（包括但不限于上门所需住宿费、差旅费、律师诉讼代理费等）；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、若甲方在签订合同时提供虚假资料或者故意隐瞒重要事实的、乙方一经发现有权解除合同。合同解除后三日内甲方应当一次性支付尚未清偿的借款本金及利息，同时，还应当支付（包括但不限于）违约金及其他费用。甲方行为构成犯罪的，依法由司法机关追究其刑事责任；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("4、甲方信息变更未履行通知义务导致乙方债权受损的，甲方应按借款本金总额的20%承担违约金；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("5、在本合同有效期内，有下列事项之一的，自该事项发生的次日，视为借款提前到期，乙方有权要求甲方立即清偿全部借款本息、违约金以及要求甲方承担本合同第七条约定的因提前还款而产生的相关费用，若甲方不能立即清偿债务的，乙方有权依本合同第九条约定的处置方式之一处置该质押车辆。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    1）、甲方违反本合同任何一项条款约定的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    2）、甲方在本合同期内未按时归还任意一期借款本息的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    3）、甲方提供的质押车辆有重大瑕疵并可能导致质押无效或质押车辆价值大幅降低；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    4）、甲方的财务、资产或家庭状况出现足以令乙方依法行使不安抗辩权的情形或甲方有预期违约的行为的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    5）、质押车辆为按揭车辆时，该车辆的按揭贷款的债权人对该车辆行使抵押权的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    6）、甲方死亡而无继承人，或继承人放弃继承的，或继承人拒绝履行债务的。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    7）、其他因任何原因导致车辆被查封、扣押或第三人对质押车辆主张或行使权利的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    8）、其他可能严重影响甲方归还借款本息的情形。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6、甲方在履行本合同过程中有任何违反合同义务或者诚实信用行为的，乙方有权保留将甲方违约失信相关信息在媒体或者同行业披露的权利；",
								textfont, Paragraph.ALIGN_LEFT));
					}else{
						//抵押担保
						document.add(createParagraph("八、抵押担保",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、甲方自愿以其合法享有的本合同第二条约定的抵押车辆作为抵押物，为甲方在本合同中约定的债务（包括借款本金、利息、违约金以及乙方实现主债权、抵押权等的全部费用）提供担保。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、甲方履行完毕本合同约定的全部义务前，未经乙方同意，甲方不得转让抵押车辆，也不得就该车辆再设定担保物权或其他任何第三方权益。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、甲方需保证在本合同签订之时并且在履行完毕本合同约定的全部义务之前，没有第三人对抵押车辆享有任何权利（若该抵押车辆为按揭车辆，则按揭贷款的债权人除外），并且抵押车辆的交强险、商业险持续有效。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("4、甲方保证自己在本合同签订之时不涉及任何诉讼或仲裁，并且承担车辆使用期间的全部责任、费用和损失。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("5、甲方履行完毕本合同约定的全部义务前，甲方应谨慎使用抵押车辆，若因甲方原因导致抵押车辆毁损、灭失，而使抵押车辆价值减少的，甲方应在　3　日内自行恢复抵押车辆的价值，不能恢复的，乙方有权选择以下列方式之一处理：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  1）、要求甲方另行提供合格的、同等价值并且乙方认可的抵押物或其他担保措施。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  2）、以抵押车辆毁损、灭失而获得的保险赔偿金清偿借款本金、利息、违约金及合同约定的其他费用。如保险赔偿金不足以偿还借款本金、利息、违约金及合同约定的其他费用，就不足部分，甲方需另行提供合格的、同等价值并且乙方认可的抵押物或其他担保措施；如超出了借款本金、利息、违约金及其他费用的金额，则按本合同第七条约定的方式提前还款。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  3）、宣布借款提前到期（借款的提前到期日由乙方确定），要求甲方按本合同第七条约定的方式提前还款。",
								textfont, Paragraph.ALIGN_LEFT));
			
						
						//抵押物的处置
						document.add(createParagraph("九、抵押物的处置：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    若甲方不能承担本合同约定的违约责任时，甲方同意乙方有权按以下方式之一处置抵押车辆：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、由乙方变卖抵押车辆，变卖的价格以乙方实际收取的变卖价款为准，变卖价款在扣除变卖费用及借款本息、违约金及合同约定的费用和依法应缴纳的税费后，如有剩余部分由乙方退还甲方，如有不足，则乙方有权向甲方继续追偿。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、由乙方委托有拍卖资质的相关企业公开拍卖，拍卖价款在扣除拍卖费用及借款本息、违约金及合同约定的费用和依法应缴纳的税费后，如有剩余部分由拍卖行或乙方退还甲方，如有不足，则乙方有权向甲方继续追偿。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、甲方以其所欠借款本息及违约金总额作为乙方购买该车的全额购车款，将车辆出售给乙方，并且全力配合乙方办理抵押车辆所有权变更登记手续，因此所产生的相关费用由甲方承担。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//按揭车辆的相关规定
						document.add(createParagraph("十、按揭车辆的相关规定：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    若甲方提供的抵押车辆为按揭车辆，则甲方与乙方签订的《借款合同》借款期限的最后截止日期（或甲方提前全部还款日期）若晚于甲方提供的抵押车辆在银行按揭贷款或其他任何形式贷款的到期时间的，则甲方应当自该抵押车辆按揭贷款全部结清后立即通知乙方，并且根据乙方的要求，配合乙方前往车辆抵押登记部门办理车辆抵押登记的相关手续，但由此产生的费用均由甲方承担，否则乙方有权要求甲方按本合同第七条约定的方式提前还款，并有权对抵押车辆实施停车并扣留该抵押车辆或要求甲方将该抵押车辆交付给乙方占有，若甲方不能立即清偿债务的，乙方有权依本合同第九条约定的处置方式之一处置该抵押车辆。",
								textfont, Paragraph.ALIGN_LEFT));
					
						//本合同的变更、终止
						document.add(createParagraph("十一、本合同的变更、终止：",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、自本合同签订之日起至借款本息全部清偿时止，甲方出现下列情形（包含但不限于）：甲方本人、家庭联系人及紧急联系人工作单位、居住地址、联系方式、电子邮箱等发生变更时，甲方应当将变更后的新信息在三日内向乙方提供，双方就变更后的信息签订书面协议，该书面协议作为本合同的附件；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、甲方有下列情形之一的，乙方有权终止本合同：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    1）、甲方出现任何一期未能按时偿还借款本息的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    2）、借款期间，甲方因债务纠纷被他人提请诉讼或者仲裁，涉案金额在20万元以上的或者甲方（包括其配偶）的主要资产（包括但不限于住房、商铺、银行存款、有价证券、股权、对外投资等）被司法机关查封、扣押、冻结、执行的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    3）、借款期间乙方与甲方失去联系或者下落不明的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    4）、其它影响甲方偿还能力的事件或者严重影响乙方实现债权的情形。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、本合同签订后，乙方未按合同约定出借借款的，甲方有权解除合同。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//违约责任
						document.add(createParagraph("十二、违约责任",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1、若甲方迟于本合同第一条第二款第二项约定的还款时间还款的，视为甲方逾期还款，甲方除必须按本合同还款计划表规定的金额向乙方正常偿付本息外，甲方应当承担逾期还款违约金。逾期违约金每日按借款本金总额的1‰计算；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2、若甲方因逾期还款导致本合同解除的，除应当承担逾期违约金外，还需承担乙方为实现债权所支出的一切费用（包括但不限于上门所需住宿费、差旅费、律师诉讼代理费等）；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3、若甲方在签订合同时提供虚假资料或者故意隐瞒重要事实的、乙方一经发现有权解除合同。合同解除后三日内甲方应当一次性支付尚未清偿的借款本金及利息，同时，还应当支付（包括但不限于）罚息、违约金及其他费用。甲方行为构成犯罪的，依法由司法机关追究其刑事责任；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("4、甲方信息变更未履行通知义务导致乙方债权受损的，甲方应按借款本金总额的20%承担违约金；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("5、在本合同有效期内，有下列事项之一的，自该事项发生的次日，视为借款提前到期，乙方有权要求甲方立即清偿全部借款本息、违约金以及要求甲方承担本合同第七条约定的因提前还款而产生的相关费用，并有权对抵押车辆实施停车并扣留该车辆或要求甲方将该车辆交付给乙方占有，若甲方不能立即清偿债务的，乙方有权依本合同第九条约定的处置方式之一处置该抵押车辆。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    1）、甲方违反本合同任何一项条款约定的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    2）、甲方在本合同期内未按时归还任意一期借款本息的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    3）、甲方逾期还款，经乙方3日内发出催收通知书，7日内委托律师事务所发出催收律师函，甲方在 15 日内仍不按时归还借款本金与利息的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    4）、甲方提供的抵押车辆有重大瑕疵并可能导致抵押无效或抵押车辆价值大幅降低；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    5）、甲方的财务、资产或家庭状况出现足以令乙方依法行使不安抗辩权的情形或甲方有预期违约的行为的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    6）、抵押车辆为按揭车辆时，该车辆按揭贷款的债权人对该车辆行使抵押权的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    7）、甲方死亡而无继承人，或继承人放弃继承的，或继承人拒绝履行债务的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    8）、因任何原因导致车辆被查封、扣押或第三人对抵押车辆主张或行使权利的；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    9）、其他可能严重影响甲方按期归还借款本息的情形。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6、甲方同意在抵押车辆上安装GPS全球卫星定位系统，未经乙方同意，甲方不得擅自驾驶该抵押车辆离开乙方所在的省份或直辖市。如甲方违反该约定或故意毁坏、拆除或变动GPS全球卫星定位系统（或自GPS全球卫星定位系统毁损的当日内未向乙方报告的）及有其他预期违约行为的，乙方有权宣布借款提前到期，要求甲方按本合同第七条约定的方式提前还款，并有权对抵押车辆实施停车并扣留该车辆或要求甲方将该车辆交付给乙方占有，若甲方不能立即清偿债务的，乙方有权依本合同第九条约定的处置方式之一处置该抵押车辆。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("7、甲方在履行本合同的过程中，如果擅自将抵押车辆出借、出租或质押给第三方的，甲方应授权乙方从该抵押车辆的借用人、承租人或质押权人处无偿取回该车辆，取回费用由甲方承担；如上述情形导致乙方不能取得该车辆，致使乙方实现合同权利或抵押权利可能产生障碍的，乙方有权宣布借款提前到期，甲方应立即按本合同第七条约定的方式提前还款，并且承担本合同第七条约定的因提前还款而产生的相关费用。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("8、甲方在履行本合同过程中有任何违反合同义务或者诚实信用行为的，乙方有权保留将甲方违约失信相关信息在媒体或者同行业披露的权利；",
								textfont, Paragraph.ALIGN_LEFT));
	
					}
					
					//本合同的债权转让
					document.add(createParagraph("十三、本合同的债权转让：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、乙方有权转让本合同的债权。乙方债权转让时应当通知甲方。经双方商定，债权转让通知采取电子邮件的方式进行，乙方将《债权转让通知书》发至甲方电子邮箱后即完成通知义务，视为通知到达，无须甲方回复。甲方电子邮箱为    "+ mapLoan.get("loanUserEmail") +" ；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、债权转让后，甲方应当对债权受让人继续履行本合同对乙方的还款义务，不得以未收到债权转让通知为由拒绝履行还款义务，否则，一切后果由甲方自负。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//争议解决方式
					document.add(createParagraph("十四、争议解决方式：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、甲、乙双方均确认，本合同的签订、生效、履行以不违反中华人民共和国的法律、法规为前提，如本合同中的任何一条或多条违反现行法律、法规的，则属于无效条款。但无效条款并不影响本合同其他条款的效力；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、本合同在履行过程中发生任何争议时，双方应本着互利互惠的原则友好协商解决；协商不成的，双方均有权向本协议签署地的人民法院提请诉讼。",
							textfont, Paragraph.ALIGN_LEFT));
					
					
					//合同的生效及效力
					document.add(createParagraph("十五、合同的生效及效力：",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1、本合同自双方签章（捺手印）时生效；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2、本合同及其附件的任何修改、补充均须以书面形式作出，本合同的补充协议及附件与本合同具有同等的法律效力；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3、本合同一式二份，甲、乙双方各持一份。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("（以下无正文）",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("甲方（借款人）：                                  乙方（出借人）：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("日期：" + dateStartCH + "                              日期：" + dateStartCH,
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("签署地点：" + transferUser.get("companyAddress"),
							textfont, Paragraph.ALIGN_LEFT));
				
					
					
					//换页     融资服务协议
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
						
						//兼容2月
						if(DateUtil.getNowDate().endsWith("01")){
							String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
									Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
							date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
						}
						
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
					document.add(createParagraph("8.1、甲方逾期向资金出借方偿还借款本息，应向乙方支付每日按融资借款本金金额的7‰计算的融资服务违约金。",
							textfont, Paragraph.ALIGN_LEFT));
					
					if("宜昌".equals(transferUser.get("area"))||"荆州".equals(transferUser.get("area"))||"恩施".equals(transferUser.get("area"))){
						document.add(createParagraph("8.2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金收取方式为：借款期限在半年以内的，收取一个月利息为违约金；借款期限超过半年的，收取两个月利息为违约金。",
								textfont, Paragraph.ALIGN_LEFT));
					}else{
//						document.add(createParagraph("8.2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金为借款本金的 " + (float)monthRate/100 + "%",
//								textfont, Paragraph.ALIGN_LEFT));
						
						
						if("青山".equals(transferUser.get("area")) || "曲靖".equals(transferUser.get("area"))
								|| "昆明".equals(transferUser.get("area")) || "楚雄".equals(transferUser.get("area"))
								|| "贵阳".equals(transferUser.get("area")) || "乌鲁木齐".equals(transferUser.get("area"))
								|| "荆门".equals(transferUser.get("area")) || "襄阳".equals(transferUser.get("area"))
								|| "娄底".equals(transferUser.get("area")) || "遵义".equals(transferUser.get("area"))
								|| "武昌".equals(transferUser.get("area")) || "汉口".equals(transferUser.get("area"))
								|| "武汉".equals(transferUser.get("area")) || "________".equals(transferUser.get("area"))
								|| "安庆".equals(transferUser.get("area")) || "铜仁".equals(transferUser.get("area"))
								|| "伊犁".equals(transferUser.get("area")) || "九江".equals(transferUser.get("area"))
								|| "克拉玛依".equals(transferUser.get("area"))){ 
							
							
							document.add(createParagraph("8.2、提前结清违约金：甲方有权向资金出借方要求提前结清借款，但需向乙方支付提前结清违约金；甲方严重违约的，资金出借方有权按照合同约定，要求甲方提前结清借款，同时，甲方也需向乙方支付提前结清违约金。提前结清违约金支付金额如下表：",
									textfont, Paragraph.ALIGN_LEFT));
							
							document.add(createParagraph(" ",
									textfont, Paragraph.ALIGN_LEFT));
							//创建表格
							PdfPTable table4 = createTable(2);
							table4.addCell(createCell("提前期数", keyfont, Element.ALIGN_CENTER));
							table4.addCell(createCell("违约金金额（元）", keyfont, Element.ALIGN_CENTER));
							//循环输出还款明细表格
							boolean bool = false;
							if("武昌".equals(transferUser.get("area")) || "汉口".equals(transferUser.get("area"))){
								bool = true;
							}
							if ("16".equals(transferUser.get("transferUserNo"))) {
								bool = false;
							}
							
							for (int i = 0; i < licaiMap.size(); i++) {
								if(i >= 9){
									break;
								}
								int month = licaiMap.get(i).get("month").intValue();
								double a = 0;
								switch (i) {
									case 0:
										if(bool){
											a = 1.0;
										}else{
											a = 0.9;
										}
										break;
									case 1:
										if(bool){
											a = 1.8;
										}else{
											a = 1.7;
										}
										break;
									case 2:
										if(bool){
											a = 2.5;
										}else{
											a = 2.4;
										}
										break;
									case 3:
										if(bool){
											a = 3.2;
										}else{
											a = 3.0;
										}
										break;
									case 4:
										if(bool){
											a = 3.8;
										}else{
											a = 3.5;
										}
										break;
									case 5:
										if(bool){
											a = 4.2;
										}else{
											a = 3.9;
										}
										break;
									case 6:
										if(bool){
											a = 4.5;
										}else{
											a = 4.2;
										}
										break;
									case 7:
										if(bool){
											a = 4.8;
										}else{
											a = 4.4;
										}
										break;
									default:
										if(bool){
											a = 5.0;
										}else{
											a = 4.5;
										}
										break;
								}
								if(month < 9){
									table4.addCell(createCell(month+"", keyfont));
									table4.addCell(createCell("借款本金 * " + (float)monthRate/100 + "% * " + a , textfont));
									
									//table4.addCell(createCell("￥" + new BigDecimal(loanAmount * monthRate * a /10.0/10/10/10/10/10).setScale(0, BigDecimal.ROUND_HALF_UP) , textfont));
								}else if(month == 9){
									table4.addCell(createCell("9期及9期以上", keyfont));
									table4.addCell(createCell("借款本金 * " + (float)monthRate/100 + "% * " + a , textfont));
									//table4.addCell(createCell("￥" + new BigDecimal(loanAmount * monthRate * a /10.0/10/10/10/10/10).setScale(0, BigDecimal.ROUND_HALF_UP) , textfont));
								}
							}
							document.add(table4);
							
						}else{
						
							document.add(createParagraph("8.2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金为借款本金的 " + (float)monthRate/100 + "%",
									textfont, Paragraph.ALIGN_LEFT));

						}
						
					}
					
					
					document.add(createParagraph("8.3、违约方应支付对方在依法主张权力过程中发生的诉讼费或仲裁费、公告费、评估费、拍卖费、律师费、因诉讼财产保全而提供担保财产的担保费用或折旧损失或利息（按24%的利率计算）、保险、鉴定费、催告费、拖车费、保管费、登记费等费用。",
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
	
	
					
	/*	老协议			
					//换页     中介服务协议 
					document.newPage();
					document.add(createParagraph("中介服务协议", Titlefont, Paragraph.ALIGN_CENTER));
					document.add(createParagraph("编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));
					document.add(createParagraph("委托方（以下简称甲方）：	" + transferUser.get("name"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号码：	" + transferUser.get("cardId"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系电话：	" + transferUser.get("mobile"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("家庭住址：	" + transferUser.get("address"), 
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("受托方（以下简称乙方）：	" + mapCompany.get("companyName"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("企业执照：	" + mapCompany.get("license"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("联系电话：	" + mapCompany.get("companyTel"), 
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("单位地址：	" + mapCompany.get("companyAddress"), 
							textfont, Paragraph.ALIGN_LEFT));
					
					
					//中介服务事项
					document.add(createParagraph("一、中介服务事项",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    乙方为甲方提供债权转让信息发布平台，并为甲方推荐合适的债权受让人。甲方通过与乙方所推荐的债权受让人签署债权转让相关合同、协议，实现甲方转让债权的目的。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//中介服务期限
					document.add(createParagraph("二、中介服务期限",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    自本协议生效之日起至甲方全额回购出让债权并支付相关利息、违约金（如有）之日止。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//甲方义务
					document.add(createParagraph("三、甲方义务",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、甲方应于本协议签署时，向乙方提交所转让债权的对应债务人其个人身份证复印件（或组织机构的相关主体资格证照）、工作名片或工作牌（工作证）复印件、抵押担保财产证照复印件等资料，并确保所提交资料的真实性。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、甲方应充分向乙方真实地披露所转让债权的对应债务人其个人信用征信状况，包括但不限于个人的银行欠款记录、司法诉讼查封记录、法院执行未结案记录、涉嫌经济犯罪之刑事违法记录等。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3）、甲方应在乙方通知的时限内提交签署相应合同文本时，所需的全部资料（如需要原件，甲方应提交原件查验）",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("4）、甲方应确保提交给乙方的联络方式（包括但不限于居住地址、手机、电子邮箱、固定电话、传真、紧急联络人）处于畅通状态，联络方式如有变更，应及时通知乙方和乙方所推荐资金出借方。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("5）、甲方应对所提交的上述材料或个人信息的真实性、合法性、完整性负责，如因提供资料虚假或非法等原因造成乙方或资金出借方损失的，甲方应当承担一切责任，并赔偿由此造成的一切损失。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6）、甲方理解并明白，如甲方对债权受让方违约，将会严重影响乙方在债权受让方处的声誉并影响乙方中介服务的市场质量。为此，甲方应在债权受让方要求其回购债权时无条件回购，并向债权受让方支付转让期利息。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("7）、甲方应按约定支付服务费。乙方接受委托开展中介服务工作后，一旦甲方与乙方推荐的某一（或多位）债权受让人签署债权转让相关合同、协议，并转出债权，即表示乙方已经实现中介服务，甲方应依照本合同的约定向乙方支付服务费。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//乙方服务内容和义务
					document.add(createParagraph("四、乙方服务内容和义务",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、为甲方提供债权转让平台，为其发布债权转让信息；",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、代理甲方向债权受让方提交甲方所转让债权的对应债务人其个人信息资料和财产状况资料。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("3）、了解债权转让有关的相关办理流程，代理或协助办理债权转让相关事宜。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("4）、甲方充分理解并同意，即使甲方与债权受让方已经达成债权转让相关合同、协议，甲方仍然授权乙方提供后续的中介服务，乙方提供的后续中介服务事项有：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("4.1在甲方履行债权转让相关合同、协议过程中，及时提醒甲方依照债权转让相关合同、协议的约定履行回购义务或其他义务，避免甲方不必要的违约行为发生；为此，在甲方未全额回购出让债权及支付相关利息之前，甲方授权乙方有监督权力（包括但不限于授权乙方提醒甲方及时回购、向甲方送达转让通知等权力）。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("5）、甲方承诺，乙方为方便行使上述事项中的代理职责时，可转委托。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("6）、甲方理解并明白，乙方为甲方提供中介服务以及在提供中介服务过程中行使代理权时，并不表示乙方作为保证人而向债权受让人或其他承担保证责任。甲乙双方均明确，乙方不承担任何保证担保责任。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//转让债权的基本情况
					document.add(createParagraph("五、转让债权的基本情况",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    债务人： " + loanUserNames + " ；债权金额（大写）：" + loanAmountCN + "（小写￥" + loanAmount/100 + "元）。履行期限：" + dateStartCH +" 起至  " + dateEndCH,
							textfont, Paragraph.ALIGN_LEFT));
					
					//中介服务费用及支付方式
					document.add(createParagraph("六、中介服务费用及支付方式",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、乙方促成债权转让合同成立的，甲方同意向乙方支付中介服务费。中介服务费金额为人民币（大写）：  肆仟零陆拾捌  元整（小写¥  4068.00  元整）。未促成合同成立的，乙方不得要求支付中介服务费。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、甲乙双方经协商，同意以按月分期支付方式支付中介服务费，共分 12 期，每期    339.00      元，支付时间及金额见下表：",
							textfont, Paragraph.ALIGN_LEFT));
					
					//插入表格 
					
					
					document.add(createParagraph("3）、上述中介服务费计算日期不足一个月的，按照一个月计算。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("4）、上述中介服务费支付至乙方指定账户。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//协议的变更和终止
					document.add(createParagraph("七、协议的变更和终止",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、非经双方协商一致，任何一方不得擅自变更本协议的内容。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、甲方全额回购出让债权并支付相关利息、违约金（如有）之日止，乙方即全部履行完毕本协议所承担义务。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//违约责任
					document.add(createParagraph("八、违约责任",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、若甲方逾期向乙方支付中介服务费，甲方应按剩余服务费的10%向乙方支付违约金；经乙方限期催交，甲方仍不交付的，乙方有权提前收取剩余服务费。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、违约方应支付对方在依法主张权力过程中发生的诉讼费或仲裁费、公告费、评估费、拍卖费、律师费、因诉讼财产保全而提供担保财产的担保费用或折旧损失或利息（按同期银行一年期贷款利率四倍计算）、保险、鉴定费、催告费、拖车费、保管费、登记费等费用。",
							textfont, Paragraph.ALIGN_LEFT));
					
					//争议解决
					document.add(createParagraph("九、争议解决",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("甲、乙双方因本协议及履行所发生的任何争议，双方应协商解决。经协商无法达成一致的，任何一方均有权向        人民法院提请诉讼。",
							textfont, Paragraph.ALIGN_LEFT));
	
					//其他
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("十、其他",
							keyfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("1）、本协议自双方签字盖章之日起生效。本合同一式两份，甲方双方各执一份。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("2）、本协议执行中的附件（如授权委托书、甲方与债权受让方签署的债权转让协议等）或补充性文件，是本协议的有效组成部分，对甲乙双方均有相应法律约束力。",
							textfont, Paragraph.ALIGN_LEFT));
	
					document.add(createParagraph("    （以下无正文）",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    甲方（签字、指模）：                     ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    乙方（签字、印章）：                     ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    签约日期：" + dateStartCH,
							textfont, Paragraph.ALIGN_LEFT));
	*/				
					
					
					
					//换页   个 人 授 权 委 托 书
					document.newPage();
					document.add(createParagraph("个 人 授 权 委 托 书", Titlefont, Paragraph.ALIGN_CENTER));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					
					
					//通过身份证获取信息
					String loanUserGender = IdCardUtils.getGenderCH(loanUserCardId);
					int loanUserAge = IdCardUtils.getAgeByIdCard(loanUserCardId);
					
					document.add(createParagraph(new Phrase[]{
							createPhrase("委托人：姓名"),
							createPhrase(createChunk(loanUserName)),createPhrase("    性别"),
							createPhrase(createChunk(loanUserGender)),createPhrase("    年龄"),
							createPhrase(createChunk(loanUserAge+"")),createPhrase("    身份证编号"),
							createPhrase(createChunk(loanUserCardId))},
							textfont, Paragraph.ALIGN_LEFT));
					
					
					//通过身份证获取信息
					String idCard = transferUser.get("trusteeCardId");
					String gender = IdCardUtils.getGenderCH(idCard);
					int age = IdCardUtils.getAgeByIdCard(idCard);
					
					document.add(createParagraph(new Phrase[]{
							createPhrase("受托人：姓名"),
							createPhrase(createChunk(transferUser.get("trusteeName"))),createPhrase("    性别"),
							createPhrase(createChunk(gender)),createPhrase("    年龄"),
							createPhrase(createChunk(age+"")),createPhrase("    身份证编号"),
							createPhrase(createChunk(idCard))},
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph(new Phrase[]{
							createPhrase("    兹委托受托人"),
							createPhrase(createChunk(transferUser.get("trusteeName"))),createPhrase("为我的代理人，代表我在互联网上注册电子邮箱，该电子邮箱作为"),
							createPhrase(createChunk(dateStartCH)),createPhrase("我"),
							createPhrase(createChunk(loanUserName)),createPhrase("（甲方）与 "),
							createPhrase(createChunk(transferUser.get("name"))),createPhrase("（乙方）签订的借款合同中，我接收《债权转让通知书》的邮箱。")},
							textfont, Paragraph.ALIGN_LEFT));
					
					document.add(createParagraph("    代理人在其权限范围内签署的所有文件及相关操作，我均予承认，由此在法律上产生的权利、义务均由委托人享有和承担。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("委 托 人：            ",
							textfont, Paragraph.ALIGN_RIGHT));
					document.add(createParagraph(dateStartCH+"        ",
							textfont, Paragraph.ALIGN_RIGHT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("注：委托人、受托人均需提供身份证。",
							keyfont, Paragraph.ALIGN_LEFT));
					
					
					
					//换页   还款事项提示函
					document.newPage();
					document.add(createParagraph("还款事项提示函", Titlefont, Paragraph.ALIGN_CENTER));
//					document.add(createParagraph(" ",
//							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase(createChunk(loanUserNames)),createPhrase("先生/女士您好： ")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("一、为了您更加方便、快捷的进行还款，同时为您积累良好的信用记录，请您特别注意如下事项：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("二、请您务必牢记每笔还款的还款日期和还款金额，及时进行还款，避免因逾期还款造成信用不良及由此招致的逾期罚息。还款日期按自然日计算，不因节假日顺延。若逾期还款，逾期罚息及违约金将按借款本金每天千分之八计算。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("三、为了方便我们更好的为您服务，若您变更联系方式、联系地址、工作地址等信息，请您及时告知我们。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("四、对于提前还款即一次还款，如果您决定一次性还清剩余全部款项，请您务必提前七个工作日与我们联系。我们将与您沟通并安排提前还款事宜。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("五、对于续借申请，如您需续借，请您至少提前七天联系我司。请您务必最迟于还款到期日17点前将还款金额（续借月息加本金差额）打入我司如下指定账户。若您未按时还款，我司将视为您放弃续借，我司将收回全部本金以及由于逾期还款所招致的罚息。",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("六、关于还款账户的提示：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("    接受还款的专用账户为：",
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
					document.add(createParagraph("七、关于还款期限及月还款额度：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(new Phrase[]{createPhrase("借款期限为："),
							createPhrase(createChunk(dateStartCH)),createPhrase("起至"),
							createPhrase(createChunk(dateEndCH)),createPhrase("。")},
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					
					//创建表格
					PdfPTable table4 = createTable(3);
					
					//计算还款
	//				LiCai lc = new LiCai(loanAmount, yearRate, limit);
	//				List<Map<String, Long>> lcMap = new ArrayList<Map<String,Long>>();
	//				if("A".equals(refundType)){
	//					lcMap = lc.getDengEList();
	//				}else{
	//					lcMap = lc.getDengXiList();
	//				}
	//				
	//				//创建表格
	//				table4.addCell(createCell("期数", keyfont, Element.ALIGN_CENTER));
	//				table4.addCell(createCell("还款日期", keyfont, Element.ALIGN_CENTER));
	//				table4.addCell(createCell("还款金额", keyfont, Element.ALIGN_CENTER));
	//				if(refundType.equals("B")){
	//					table4.addCell(createCell("", keyfont));
	//					table4.addCell(createCell(DateUtil.getStrFromDate(
	//							DateUtil.getDateFromString(loanDate, "yyyyMMdd"), "yyyy-MM-dd"), textfont));
	//					table4.addCell(createCell("￥" + lcMap.get(0).get("xi") /10/10 , textfont));
	//				}
	//				//循环输出还款明细表格
	//				for (int i = 0; i < lcMap.size(); i++) {
	//					int month = lcMap.get(i).get("month").intValue();
	//					table4.addCell(createCell(month + "", keyfont));
	//					String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
	//					String date4Ch = DateUtil.getStrFromDate(
	//							DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy-MM-dd");
	//					table4.addCell(createCell(date4Ch, textfont));
	//					String hkje = String.valueOf(lcMap.get(i).get("benxi") /10/10);
	//					if(lcMap.size() - i <= 1){
	//						hkje = String.valueOf((lcMap.get(i).get("benxi")+lcMap.get(i).get("balance")) /10/10);
	//					}
	//					if(refundType.equals("B") && lcMap.size() - i <= 1){
	//						hkje += "（其中" + lcMap.get(0).get("xi") /10/10 + "元利息已收取）" ;
	//					}
	//					table4.addCell(createCell("￥" + hkje , textfont));
	//				}
					
					//创建表格
					table4.addCell(createCell("期数", keyfont, Element.ALIGN_CENTER));
					table4.addCell(createCell("还款日期", keyfont, Element.ALIGN_CENTER));
					table4.addCell(createCell("还款金额", keyfont, Element.ALIGN_CENTER));
					
					//每月利息
					long lixi_month = loanAmount*monthRate/10/10/10/10;
					//利息分转换成元 并直接取整
					int lixi_month_yuan = (int)lixi_month/10/10;
					//每月本金
					long benjin_month = loanAmount/limit;
					//还款金额  =  每月利息+每月利息
					long hkje = lixi_month + benjin_month;
					int hkje_month = (int)hkje/10/10;
					String hkje_month_text = "0";
					
//					if(refundType.equals("B")){
						table4.addCell(createCell("", keyfont));
						table4.addCell(createCell(DateUtil.getStrFromDate(
								DateUtil.getDateFromString(loanDate, "yyyyMMdd"), "yyyy-MM-dd"), textfont));
						table4.addCell(createCell("￥" +  lixi_month_yuan , textfont));
//					}
					//循环输出还款明细表格
					for (int i = 0; i < limit; i++) {
						int month = i+1;
						table4.addCell(createCell(month + "", keyfont));
						String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
						String date4Ch = DateUtil.getStrFromDate(
								DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy-MM-dd");
						
						//兼容2月
						if(DateUtil.getNowDate().endsWith("01")){
							String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
									Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
							date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
						}
						
						table4.addCell(createCell(date4Ch, textfont));
						if(refundType.equals("B")){
							hkje_month_text = String.valueOf(lixi_month_yuan);
							if(limit - i <= 1){
								hkje_month_text = String.valueOf((int)Math.ceil((loanAmount+lixi_month)/10.0/10));
								hkje_month_text += "（其中" + lixi_month_yuan + "元利息已收取）" ;
							}
						}else{
							hkje_month_text = String.valueOf(hkje_month);
							if(limit - i <= 1){
								long fee = hkje*limit - hkje_month*limit*100;
								hkje_month_text = String.valueOf((int)Math.ceil((hkje+(fee<0?0:fee))/10.0/10));
								hkje_month_text += "（其中" + lixi_month_yuan + "元利息已收取）" ;
							}
						}
						table4.addCell(createCell("￥" + hkje_month_text , textfont));
					}
					document.add(table4);
					
					document.add(createParagraph("借款人签章：            ",
							textfont, Paragraph.ALIGN_RIGHT));
					document.add(createParagraph(dateStartCH + "          ",
							textfont, Paragraph.ALIGN_RIGHT));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("※温馨提示：请您于每一期还款到期日前及时还款，避免因逾期还款招致罚息。",
							keyfont, Paragraph.ALIGN_LEFT));
					
					
					
					//换页   借款人个人信息表
					document.newPage();
					document.add(createParagraph("借款人个人信息表", Titlefont, Paragraph.ALIGN_CENTER));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("姓    名："+loanUserNames,
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("身份证号："+loanUserCardIds,
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("电话号码："+mapLoan.get("loanUserMobile"),
							textfont, Paragraph.ALIGN_LEFT));
					/*
					document.add(createParagraph("学历：□ 高中或以下     □ 大专     □ 本科     □ 研究生或以上  ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("有无子女：□ 有         □ 无",
							textfont, Paragraph.ALIGN_LEFT));
					*/
					document.add(createParagraph("婚姻状况：□ 已婚       □ 离异     □ 未婚",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("有无车贷：□ 有         □ 无",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("有无房产：□ 有         □ 无",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("有无房贷：□ 有         □ 无",
							textfont, Paragraph.ALIGN_LEFT));
	
	/*				document.add(createParagraph("户籍地址：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("住宅地址：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("（质押车仅需填写以上信息）",
							textfont, Paragraph.ALIGN_LEFT));
	
					document.add(createParagraph("公司名称：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("公司地址：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("职位：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("职业类型：□ 工薪阶层          □ 私营企业主",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("公司人数：□ 10以下     □ 10-50  □ 50-100  □ 100-500  □ 500以上",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("月收入：",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("工作年限：□ 1年以下     □ 1-3年    □ 3-5年       □ 5年以上  ",
							textfont, Paragraph.ALIGN_LEFT));
					document.add(createParagraph("（提押车需填写上述所有信息）",
							textfont, Paragraph.ALIGN_LEFT));*/
					
					
					
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
					//TODO 计算中介服务费(由于中介支付申请单需要用到中介服务的算法，所以提前计算)
					long monthFee = 0;
					
					LiCai licai2 = new LiCai(loanAmount, centerRate, limit);
					List<Map<String, Long>> licaiMap2 = new ArrayList<Map<String,Long>>();
					if("A".equals(refundType)){
						licaiMap2 = licai2.getDengEList();
						monthFee = loanAmount*monthRate/10/10/10/10 + loanAmount/limit - licaiMap2.get(0).get("benxi");
					}else{
						licaiMap2 = licai2.getDengXiList();
						monthFee = loanAmount*monthRate/10/10/10/10 - loanAmount*centerRate/12/10/10/10/10;
					}
					
					BigDecimal bdFee = new BigDecimal(monthFee/10.0/10).setScale(0, BigDecimal.ROUND_HALF_UP);
					
					int totalFee = bdFee.intValue()*limit;
					String totalFeeCH = NumberToCN.number2CNMontrayUnit(new BigDecimal(totalFee));
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////			
										
					
					//TODO 借款保证担保合同书
					String dbrxms = "";
					if(StringUtil.isBlank(mapLoan.get("dbrs")) == false){
						document.newPage();
						document.add(createParagraph("借款保证担保合同书", Titlefont, Paragraph.ALIGN_CENTER));
						document.add(createParagraph("合同编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));
						document.add(createParagraph(" ",
								textfont, Paragraph.ALIGN_LEFT));
		
						String dbrs = mapLoan.get("dbrs");
						String[] dbrsArr = dbrs.split(",");
						for (int i = 0; i < dbrsArr.length; i++) {
							String[] dbrinfoArr = dbrsArr[i].split("\\|");
							String dbr_xm = dbrinfoArr[0];
							dbrxms += dbr_xm;
							if(i < dbrsArr.length-1){
								dbrxms += ",";
							}
							String dbr_sfzh = dbrinfoArr[1];
							String dbr_xb = "    ";
							if("1111".equals(dbr_sfzh)){
								dbr_sfzh = "                ";
							}else{
								if(IdCardUtils.validateCard(dbr_sfzh)){
									dbr_xb = IdCardUtils.getGenderCH(dbr_sfzh);
								}
							}
							String dbr_lxdh = dbrinfoArr[2];
							if("1111".equals(dbr_lxdh)){
								dbr_lxdh = "                ";
							}
							String dbr_zs = dbrinfoArr[3];
							if("1111".equals(dbr_zs)){
								dbr_zs = "                                    ";
							}
							
							document.add(createParagraph(new Phrase[]{createPhrase("保证人"+(i+1)+"（甲方） ："),
									createPhrase(createChunk(dbr_xm)),createPhrase("    性别："),
									createPhrase(createChunk(dbr_xb)),createPhrase("    身份证号码："),
									createPhrase(createChunk(dbr_sfzh))},
									textfont, Paragraph.ALIGN_LEFT));
							document.add(createParagraph(new Phrase[]{createPhrase("住所："),
									createPhrase(createChunk(dbr_zs)),createPhrase("    联系电话："),
									createPhrase(createChunk(dbr_lxdh))},
									textfont, Paragraph.ALIGN_LEFT));
						}
						
	/*					document.add(createParagraph(new Phrase[]{createPhrase("保证人（甲方） ："),
								createPhrase(createChunk("            ")),createPhrase("    性别："),
								createPhrase(createChunk("    ")),createPhrase("    身份证号码："),
								createPhrase(createChunk("                "))},
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(new Phrase[]{createPhrase("住所："),
								createPhrase(createChunk("                                ")),createPhrase("    联系电话："),
								createPhrase(createChunk("                "))},
								textfont, Paragraph.ALIGN_LEFT));*/
						
						document.add(createParagraph(new Phrase[]{createPhrase("债权人（乙方）："),
								createPhrase(createChunk(transferUser.get("name"))),createPhrase("    性别："),
								createPhrase(createChunk(IdCardUtils.getGenderCH(transferUser.get("cardId")))),createPhrase("    身份证号码："),
								createPhrase(createChunk(transferUser.get("cardId")))},
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(new Phrase[]{createPhrase("现住址："),
								createPhrase(createChunk(transferUser.get("address"))),createPhrase("    联系电话："),
								createPhrase(createChunk(transferUser.get("mobile")))},
								textfont, Paragraph.ALIGN_LEFT));
						
						
						document.add(createParagraph("  为确保债权人(以下称乙方)与"+loanUserNames+"（借款人）签订的"+mapLoan.get("loanNo")+"号借款合同（以下简称借款合同）的履行，甲方愿意为借款人所借款项向乙方提供保证担保。甲、乙双方根据《合同法》、《担保法》及其他有关规定，经协商一致，约定如下条款：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第一条　甲方保证担保的借款金额人民币（大写）"+loanAmountCN+"借款期限自"+dateStartCH+"至"+dateEndCH+"。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第三条　保证担保的范围：借款金额人民币（大写"+loanAmountCN+"元及利息、借款人应支付的违约金（包括罚息）、赔偿金和实现借款债权的费用（包括诉讼费、律师费等）。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第四条　保证期间：自本合同生效之日起至借款合同履行期限届满之日后两年止。借款合同展期的，以展期后所确定的合同最终履行期限之日为届满之日。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第五条　本合同的效力独立于被保证的借款合同。借款合同无效并不影响本合同的效力。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第六条　保证期间，借款合同的当事人双方协议变更借款合同除贷款利率以外的其他内容，应当事先取得本合同甲方的书面同意。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第七条　保证期间，甲方机构发生变更、撤销，甲方应提前   3   天书面通知乙方，本合同项下的全部义务由变更后的机构承担或由对甲方作出撤销决定的机构承担。如乙方认为变更后的机构不具备完全的保证能力，变更后的机构或作出撤销决定的机构有义务落实为乙方所接受的新的保证人。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第八条　保证期间，乙方有权对甲方的资金和财产状况进行监督，有权要求甲方提供其财务报表等资料，甲方应如实提供。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第九条　保证期间，甲方不得向第三方提供超出其自身负担能力的担保。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第十条　发生下列情况之一的，乙方有权要求甲方提前承担保证责任，甲方同意提前承担保证责任：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  1．甲方违反本合同第七条、第八条、第九条的约定或者发生其他严重违约行为；",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  2．《借款合同》履行期间借款人被宣告破产、被解散、擅自变更企业体制致使乙方借款债权落空、未按借款合同的约定使用贷款、卷入或即将卷入重大的诉讼或仲裁程序及其他法律纠纷、发生其他足以影响其偿债能力或缺乏偿债诚意的行为等情况。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  3、《借款合同》中约定的提前到期的情形。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第十一条　甲方不承担保证责任或者违反本合同约定的其他义务的，应向乙方支付被保证的借款合同项下借款金额30%的违约金，因此给乙方造成经济损失且违约金数额不足弥补所受损失的，还应赔偿乙方的实际经济损失。",
								textfont, Paragraph.ALIGN_LEFT));
						//document.newPage();
						document.add(createParagraph("第十二条　双方约定的其他条款：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("________________________________________________________",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("________________________________________________________",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第十三条　因本合同发生的争议，经协商不能达成一致意见，应当向合同签署地人民法院提起诉讼。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第十四条　本合同自双方签字或加盖公章后生效。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("第十五条　本合同正本一式二份，甲乙双方各执一份。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("合同签署地："+transferUser.get("companyAddress"),
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("协议双方签字盖章：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("甲方：                                  	乙方：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("日期：" + dateStartCH + "                              日期：" + dateStartCH,
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(" ",
								textfont, Paragraph.ALIGN_LEFT));
		
						document.add(createParagraph("附：1、担保人身份证复印件（正反2面）",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    2、担保人基本情况登记表（必填项）",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("  ",
								textfont, Paragraph.ALIGN_LEFT));
						
						//插入表格
						for (int i = 0; i < dbrsArr.length; i++) {
							PdfPTable table6 = createTable(6);
							String[] dbrinfoArr = dbrsArr[i].split("\\|");
							table6.addCell(createCell("担保人"+(i+1)+"姓名", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell(dbrinfoArr[0], textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("婚姻状况", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("未婚□   已婚□   离异□   丧偶□", textfontBig, Element.ALIGN_CENTER,3));
							table6.addCell(createCell("配偶姓名", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("年龄", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("配偶身份证号码", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("保证人从事行业", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("公司地址", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("公司年收入", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("公司名称", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("职务", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("个人年收入", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("固定电话", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER,2));
							table6.addCell(createCell("移动电话", textfontBig, Element.ALIGN_CENTER));
							table6.addCell(createCell("", textfontBig, Element.ALIGN_CENTER,2));
							document.add(createParagraph(" ",
									textfont, Paragraph.ALIGN_LEFT));
							document.add(table6);
						}
					}
					
					
					
					//TODO 资金支付申请单
					document.newPage();
					document.add(createParagraph("资金支付申请单（"+transferUser.get("area")+"）", Titlefont, Paragraph.ALIGN_CENTER));
					document.add(createParagraph(" ",
							textfont, Paragraph.ALIGN_LEFT));
	
					//插入表格
					PdfPTable table5 = createTable(5);
					table5.addCell(createCell("收款人姓名", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(loanUserName, textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("申请日期", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(dateStartCH, textfontBig, Element.ALIGN_CENTER,2));
					table5.addCell(createCell("合同编号", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(mapLoan.get("loanNo"), textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("付款金额", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("（大写）"+loanAmountCN+"；（小写）" + loanAmount/10/10, textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("收款银行", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(mapLoan.get("loanBankName"), textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("收款卡号", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(mapLoan.get("loanBankNo"), textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("联系电话", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(mapLoan.get("loanUserMobile"), textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("担保人", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(dbrxms, textfontBig, Element.ALIGN_LEFT,4));
					table5.addCell(createCell("利率："+(float)monthRate/10/10 + "%", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("期数："+limit, textfontBig, Element.ALIGN_CENTER));
					
					String t = "等本等息";
					if("B".equals(refundType)){
						t = "先息后本";
					}
					table5.addCell(createCell(t, textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell(mapLoan.get("product"), textfontBig, Element.ALIGN_CENTER,2));
					//----------
					table5.addCell(createCell("还款明细", headfont, Element.ALIGN_CENTER,5));
					table5.addCell(createCell("还款日期", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("理财人", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("易融恒信", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("机构", textfontBig, Element.ALIGN_CENTER));
					table5.addCell(createCell("总额", textfontBig, Element.ALIGN_CENTER));
					//循环输出还款明细表格
					//long totalAmount_month = loanAmount*monthRate/100/100;
					for (int i = 0; i < licaiMap.size(); i++) {
						int month = licaiMap.get(i).get("month").intValue();
						String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
						String date4Ch = DateUtil.getStrFromDate(
								DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy年MM月dd日");
						
						//兼容2月
						if(DateUtil.getNowDate().endsWith("01")){
							String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
									Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
							date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
						}
						
						table5.addCell(createCell(date4Ch, textfontBig));
						
						long lcr = 0;
						if(licaiMap.size() - i <= 1){
							lcr = licaiMap.get(i).get("benxi")+licaiMap.get(i).get("balance");
						}else{
							lcr = licaiMap.get(i).get("benxi");
						}
						
						int lcr_int = new BigDecimal(lcr/10.0/10).setScale(0, BigDecimal.ROUND_HALF_UP).intValue();
						
						int hktsh = 0;
						if(refundType.equals("B")){
							hktsh = lixi_month_yuan;
							if(limit - i <= 1){
								hktsh = (int)Math.ceil((loanAmount+lixi_month)/10.0/10);
							}
						}else{
							hktsh = hkje_month;
							if(limit - i <= 1){
								long fee = hkje*limit - hkje_month*limit*100;
								hktsh = (int)Math.ceil((hkje+(fee<0?0:fee))/10.0/10);
							}
						}
						
						int yrhx = hktsh - lcr_int - bdFee.intValue();
	
						table5.addCell(createCell("￥" +  lcr_int, textfontBig));
						table5.addCell(createCell("￥" + yrhx , textfontBig));
						table5.addCell(createCell("￥" + bdFee, textfontBig));
						table5.addCell(createCell("￥" + (lcr_int+yrhx+bdFee.intValue()) , textfontBig));
					}
					document.add(table5);					
					
					
					
					//------------------------- 换页     中介服务协议  ----------------------------
					
					if(!"青山".equals(transferUser.get("area"))&& !"曲靖".equals(transferUser.get("area"))
							&& !"昆明".equals(transferUser.get("area")) && !"楚雄".equals(transferUser.get("area"))
							&& !"贵阳".equals(transferUser.get("area")) && !"娄底".equals(transferUser.get("area"))
							&& !"遵义".equals(transferUser.get("area")) && !"乌鲁木齐".equals(transferUser.get("area"))
							&& !"荆门".equals(transferUser.get("area")) && !"襄阳".equals(transferUser.get("area"))
							&& !"伊犁".equals(transferUser.get("area")) && !"汉口".equals(transferUser.get("area"))
							&& !"九江".equals(transferUser.get("area")) && !"克拉玛依".equals(transferUser.get("area"))){
					
						document.newPage();
						document.add(createParagraph("中介服务协议", Titlefont, Paragraph.ALIGN_CENTER));
						document.add(createParagraph("编号:" + mapLoan.get("loanNo"), textfont, Paragraph.ALIGN_RIGHT));
						document.add(createParagraph("委托方（以下简称甲方）：	" + transferUser.get("name"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("身份证号码：	" + transferUser.get("cardId"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("联系电话：	" + transferUser.get("mobile"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("家庭住址：	" + transferUser.get("address"), 
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph("受托方（以下简称乙方）：" + transferUser.get("companyName"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("企业执照：	" + transferUser.get("license"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("联系电话：	" + transferUser.get("companyTel"), 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("单位地址：	" + transferUser.get("companyAddress"), 
								textfont, Paragraph.ALIGN_LEFT));
						
						
						document.add(createParagraph("    甲方因经营周转需要融资，委托乙方提供中介服务，并通过乙方推荐的资金出借方而得到融资资金。现甲乙双方本着自愿、平等的原则，就乙方提供中介服务有关事宜达成如下协议条款：",
								textfont, Paragraph.ALIGN_LEFT));
						
						
						//中介服务事项
						document.add(createParagraph("一、中介服务事项",
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
						document.add(createParagraph("2.6、甲方理解并明白，如甲方对资金出借方违约，将会严重影响乙方在资金出借方处的声誉并影响乙方中介服务的市场质量。为此，甲方应按期足额向资金出借方支付融资借款本息，如有逾期，甲方除向资金出借方承担违约责任外，还应依照本协议的约定向乙方承担违约责任。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2.7、甲方应按约定支付服务费。乙方接受委托开展中介服务工作后，一旦甲方与乙方推荐的某一（或多位）资金出借方签署融资借款相关合同、协议，并获得借款，即表示乙方已经实现中介服务，甲方应依照本合同的约定向乙方支付服务费。",
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
						document.add(createParagraph("3.5、甲方充分理解并同意，即使甲方与资金出借方已经达成融资借款相关合同、协议，甲方仍然授权乙方提供后续的中介服务，乙方提供的后续中介服务事项有：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3.5.1、在甲方履行融资借款相关合同、协议过程中，及时提醒甲方依照融资借款相关合同、协议的约定，履行还款义务或其他义务，避免甲方不必要的违约行为发生；为此，在甲方为全额清偿资金出借方的借款本息、违约金等之前，甲方授权乙方有监督权利（包括但不限于授权乙方提醒甲方及时还款、向甲方送达还款通知、催促甲方交付抵押物、代为保管抵押物等权利）。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3.5.2、在甲方履行融资借款相关合同、协议过程中因逾期还款而违约时，为避免资金出借方采取查封、强制执行等措施而影响甲方的个人信用或资产受限，或为避免甲方违约损失的扩大，甲方授权乙方有协商及代理的权利（包括但不限于向资金出借方申请借款延期、代理甲方在融资借款协议约定的条件下处置抵押物、在甲方面临资金出借方的诉讼过程中代理收取诉讼法律文书或代为委托诉讼代理人的权利等）。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3.6、甲方承诺，乙方为方便行使上述事项中的代理职责时，可转委托。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("3.7、甲方理解并明白，乙方为甲方提供中介服务以及在提供中介服务过程中行使代理职权时，并不表示乙方作为保证人而向资金出借方或其他方承担保证责任。甲乙双方均明确，乙方不承担任何保证担保责任。",
								textfont, Paragraph.ALIGN_LEFT));
		
						//《借款合同》的签署
						document.add(createParagraph("四、《借款合同》的签署",
								keyfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph(new Phrase[]{
								createPhrase("    甲方经乙方推荐，与借款人"),
								createPhrase(createChunk(loanUserNames)),createPhrase("于"),
								createPhrase(createChunk(dateStartCH)),createPhrase("签署了合同编号为"),
								createPhrase(createChunk(mapLoan.get("loanNo"))),createPhrase("的《借款合同》，借款本金为人民币（大写）"),
								createPhrase(createChunk(loanAmountCN)),createPhrase("，（小写￥"),
								createPhrase(createChunk(loanAmount/10/10 + "")),createPhrase("元）。")},
								textfont, Paragraph.ALIGN_LEFT));
						
						//中介服务期
						document.add(createParagraph("五、中介服务期",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    自本协议生效之日起至甲方全额清偿资金出借方的借款本息和违约金（如有）之日止。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//中介服务费用及支付方式
						document.add(createParagraph("六、中介服务费用及支付方式",
								keyfont, Paragraph.ALIGN_LEFT));
										
						document.add(createParagraph(new Phrase[]{
								createPhrase("6.1、甲方同意向资金出借方支付中介服务费。中介服务费总额为人民币（大写）："),
								createPhrase(createChunk(totalFeeCH)),createPhrase("（小写￥"),
								createPhrase(createChunk(totalFee + "")),createPhrase("元）。")},
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(new Phrase[]{
								createPhrase("6.2、甲乙双方经协商，同意以按月分期支付方式支付中介服务费，共分"),
								createPhrase(createChunk(limit+"")),createPhrase("期，每期"),
								createPhrase(createChunk(bdFee + "")),createPhrase("元（无法整除部分四舍五入），支付时间及金额见下表：")},
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph(" ",textfont, Paragraph.ALIGN_LEFT));
						//插入表格
						PdfPTable table3 = createTable(3);
						table3.addCell(createCell("项目", keyfont, Element.ALIGN_CENTER));
						table3.addCell(createCell("支付时间", keyfont, Element.ALIGN_CENTER));
						table3.addCell(createCell("中介服务费金额（元）", keyfont, Element.ALIGN_CENTER));
						//循环输出还款明细表格
						for (int i = 0; i < licaiMap.size(); i++) {
							int month = licaiMap.get(i).get("month").intValue();
							table3.addCell(createCell(month+"", keyfont));
							String date4string = CommonUtil.anyRepaymentDate4string(returnDate, month);
							String date4Ch = DateUtil.getStrFromDate(
									DateUtil.getDateFromString(date4string, "yyyyMMdd"), "yyyy年MM月dd日");
							
							//兼容2月
							if(DateUtil.getNowDate().endsWith("01")){
								String end4month = CommonUtil.getFirstDataAndLastDateByMonth(Integer.parseInt(date4string.substring(0, 4)),
										Integer.parseInt(date4string.substring(4, 6)), "yyyyMMdd")[1].substring(6, 8);
								date4Ch = date4Ch.substring(0,dateEndCH.length()-3) + end4month + "日";
							}
							
							table3.addCell(createCell(date4Ch, textfont));
							table3.addCell(createCell("￥" + bdFee , textfont));
						}
						document.add(table3);
						
						document.add(createParagraph("6.3、甲方同意，当甲方未按《借款合同》中约定的还款期限还款时，在甲方全额清偿资金出借方的应还款本息、违约金的同时，甲方还须向乙方支付中介服务违约金。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6.4、上述中介服务费计算日期不足一个月的，按照一个月计算。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6.5、支付方式：中介服务费在资金出借人实际出借给甲方之日起按月支付。支付中介服务费日期为资金出借的当日及其每月的对应日。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6.6、上述中介服务费支付至乙方指定的账户。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("6.7、乙方在协助或代理甲方办理中介服务有关事项过程中发生的所有费用由甲方承担。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//协议的变更和终止
						document.add(createParagraph("七、协议的变更和终止",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("1）、非经双方协商一致，任何一方不得擅自变更本协议的内容。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("2）、在甲方与乙方推荐的资金出借方成功签订借款相关合同、协议，并取得借款资金，至甲方全额清偿资金出借方借款本息、违约金（如有）之日止，乙方即全部履行完毕协议所承担义务。",
								textfont, Paragraph.ALIGN_LEFT));
						
						//违约责任
						document.add(createParagraph("八、违约责任",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("8.1、甲方逾期向资金出借方偿还借款本息，应向乙方支付每日按融资借款本金金额的7‰计算的中介服务违约金。",
								textfont, Paragraph.ALIGN_LEFT));
						
						if("宜昌".equals(transferUser.get("area"))||"荆州".equals(transferUser.get("area"))||"恩施".equals(transferUser.get("area"))){
							document.add(createParagraph("8.2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金收取方式为：借款期限在半年以内的，收取一个月利息为违约金；借款期限超过半年的，收取两个月利息为违约金。",
									textfont, Paragraph.ALIGN_LEFT));
						}else{
							document.add(createParagraph("8.2、如甲方在借款期间提前归还借款本金，则需向乙方支付提前还款违约金，违约金为借款本金的 " + (float)monthRate/100 + "%" ,
									textfont, Paragraph.ALIGN_LEFT));
							
//							document.add(createParagraph("8.2、提前结清违约金：甲方有权向资金出借方要求提前结清借款，但需向乙方支付提前结清违约金；甲方严重违约的，资金出借方有权按照合同约定，要求甲方提前结清借款，同时，甲方也需向乙方支付提前结清违约金。提前结清违约金支付金额如下表：",
//									textfont, Paragraph.ALIGN_LEFT));
//							
//							document.add(createParagraph(" ",
//									textfont, Paragraph.ALIGN_LEFT));
//							//创建表格
//							PdfPTable table6 = createTable(2);
//							table6.addCell(createCell("提前期数", keyfont, Element.ALIGN_CENTER));
//							table6.addCell(createCell("违约金金额（元）", keyfont, Element.ALIGN_CENTER));
//							//循环输出还款明细表格
//							for (int i = 0; i < licaiMap.size(); i++) {
//								int month = licaiMap.get(i).get("month").intValue();
//								double a = 0;
//								switch (i) {
//									case 0:
//										a = 0.9;
//										break;
//									case 1:
//										a = 1.7;
//										break;
//									case 2:
//										a = 2.4;
//										break;
//									case 3:
//										a = 3.0;
//										break;
//									case 4:
//										a = 3.5;
//										break;
//									case 5:
//										a = 3.9;
//										break;
//									case 6:
//										a = 4.2;
//										break;
//									case 7:
//										a = 4.4;
//										break;
//									default:
//										a = 4.5;
//										break;
//								}
//								table6.addCell(createCell(month+"", keyfont));
//								
//								table6.addCell(createCell("￥" + new BigDecimal(loanAmount * monthRate * a /10.0/10/10/10/10/10).setScale(0, BigDecimal.ROUND_HALF_UP) , textfont));
//							}
//							document.add(table6);

							
							
						}
						
						
						document.add(createParagraph("8.3、违约方应支付对方在依法主张权力过程中发生的诉讼费或仲裁费、公告费、评估费、拍卖费、律师费、因诉讼财产保全而提供担保财产的担保费用或折旧损失或利息（按24%的利率计算）、保险、鉴定费、催告费、拖车费、保管费、登记费等费用。" ,
								textfont, Paragraph.ALIGN_LEFT));
						
						//争议解决
						document.add(createParagraph("九、争议解决",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("		甲乙双方因本协议及履行所发生的任何争议，双方应协商解决。经协商无法达成一致，任何乙方应向本协议签署地的人民法院提起诉讼。",
								textfont, Paragraph.ALIGN_LEFT));
		
						//其他
						document.add(createParagraph("十、其他",
								keyfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("10.1、本协议自双方签字盖章之日起生效。本合同一式两份，甲方双方各执一份。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("10.2、本协议执行中的附件（如授权委托书、中介服务确认书、甲方与资金出借方签署的融资借款法律文件等）或补充性文件，是本协议的有效组成部分，对甲、乙双方均有相应法律约束力。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    甲方声明：本协议中，对免除或限制乙方责任及其条款，乙方已经作了特别提示和充分说明；甲方已全面准确理解本协议各条款及其他相关交易文件并自愿签署本协议。",
								keyfont, Paragraph.ALIGN_LEFT));
		
						document.add(createParagraph("    （以下无正文）",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    甲方（签字、指模）：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    乙方（签字、印章）：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("    签约日期：" + dateStartCH,
								textfont, Paragraph.ALIGN_LEFT));		
						document.add(createParagraph("    签署地点：" + transferUser.get("companyAddress"),
								textfont, Paragraph.ALIGN_LEFT));
					}
					
					
					if(b == false){
						//TODO (全款抵押、按揭GPS)车辆质押与出借协议书
						document.newPage();
						document.add(createParagraph("车辆质押与出借协议书", Titlefont, Paragraph.ALIGN_CENTER));
	
						
						document.add(createParagraph(new Phrase[]{createPhrase("甲方（出借人）：	"),
								createPhrase(createChunk(transferUser.get("name"))),
								createPhrase("          身份证号码：	"),
								createPhrase(createChunk(transferUser.get("cardId")))} , 
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(new Phrase[]{createPhrase("乙方（借款人）：	"),
								createPhrase(createChunk(loanUserNames)),
								createPhrase("          身份证号码：	"),
								createPhrase(createChunk(loanUserCardIds))} , 
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph(new Phrase[]{createPhrase("    作为对编号为"),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("的《借款合同》的补充，经双方友好协商，达成如下协议：")} , 
								textfont, Paragraph.ALIGN_LEFT));
						
						String carNo = "        ";
						if(StringUtil.isBlank(cars) == false){
							String[] carArr = cars.split(",");
							String[] carinfoArr = carArr[0].split("\\|");
							carNo = carinfoArr[2];
						}
						document.add(createParagraph(new Phrase[]{createPhrase("1、乙方自愿将  "),
								createPhrase(createChunk(carNo)),
								createPhrase("  汽车质押于甲方指定停车场，作为甲方权益的保障。")} , 
								textfont, Paragraph.ALIGN_LEFT));					
						document.add(createParagraph("2、考虑到乙方实际工作与生活的需要，甲方同意将该车辆出借与乙方使用，乙方应妥善保管与使用该车辆，并保证不利用该车辆向其它单位或个人借款或抵债。",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph(new Phrase[]{createPhrase("3、若乙方有严重违反编号为  "),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("  的《借款合同》或《借款承诺书》的行为时，甲方有权收回该车辆。甲方在收车过程中产生的费用由乙方承担。如客户逾期收车过程中发生的交通事故所产生的费用由乙方来承担。")} , 
								textfont, Paragraph.ALIGN_LEFT));					
						// modify by stonexk at 20170605
						if ("9".equals(transferUser.get("transferUserNo")) || "22".equals(transferUser.get("transferUserNo")) || "25".equals(transferUser.get("transferUserNo"))
								|| "21".equals(transferUser.get("transferUserNo"))) {
							document.add(createParagraph("4、收车费用标准：车辆使用费用按5元/公里计算，往返均计程；工作人员费用按500元/人/天计算；收车费用" + transferUser.get("area2") + "内10000元起步，" + transferUser.get("area2") + "外15000元起步。",
									textfont, Paragraph.ALIGN_LEFT));
						} else {
							document.add(createParagraph("4、收车费用标准：车辆使用费用按5元/公里计算，往返均计程；工作人员费用按500元/人/天计算；收车费用" + transferUser.get("area2") + "内5000元起步，" + transferUser.get("area2") + "外10000元起步。",
									textfont, Paragraph.ALIGN_LEFT));
						}
						// end modify
						
						if("曲靖".equals(transferUser.get("area"))){
							//TODO 曲靖新增
							document.add(createParagraph("5、停车费用标准：乙方车辆不论以何种原由在甲方停车场停车，所产生的停车费用一律按照50元/天计算。",
									textfont, Paragraph.ALIGN_LEFT));
						}
						
						document.add(createParagraph("甲方：                                  	乙方：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("日期：" + dateStartCH + "                              日期：" + dateStartCH,
								textfont, Paragraph.ALIGN_LEFT));
						
						
						
						//TODO 借款承诺书
						document.newPage();
						document.add(createParagraph("借款承诺书", Titlefont, Paragraph.ALIGN_CENTER));
						document.add(createParagraph(new Phrase[]{createPhrase("致：  "),
								createPhrase(createChunk(transferUser.get("name"))),
								createPhrase("  先生")} , 
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph(new Phrase[]{createPhrase("    作为对编号为"),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("的《借款合同》的补充，本人自愿承诺如下条款：")} , 
								textfont, Paragraph.ALIGN_LEFT));
	
						document.add(createParagraph(new Phrase[]{createPhrase("1、本人同意您在  "),
								createPhrase(createChunk(carNo)),
								createPhrase("  汽车上安装GPS，实时监控该车辆的行踪。本人保证妥善保管好GPS。如有设备故障，本人随时配合您对GPS进行检修或更换。")} , 
								textfont, Paragraph.ALIGN_LEFT));					
						document.add(createParagraph(new Phrase[]{createPhrase("2、本人承诺不会恶意损毁或拆卸GPS。如有违反，您有权将该行为理解为本人有逃避债务的嫌疑，并将该行为定性为诈骗，并有权向本人收取借款金额10%的违约金（不足1万元的，按1万元收取），并有权单方面提前终止编号为  "),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("  的《借款合同》。")} , 
								textfont, Paragraph.ALIGN_LEFT));						
						document.add(createParagraph(new Phrase[]{createPhrase("3、本人承诺不会私自变卖该车辆、不会利用该车辆再向其它金融机构或个人借款或抵债。如有违反，您有权将该行为理解为本人有骗贷的嫌疑，并将该行为定性为诈骗，并有权向本人收取借款金额10%的违约金（不足1万元的，按1万元收取），并有权单方面提前终止编号为  "),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("  的《借款合同》。")} , 
								textfont, Paragraph.ALIGN_LEFT));	
	
						
						document.add(createParagraph(new Phrase[]{createPhrase("4、本人承诺向您所递交的所有证照材料均通过合法渠道取得，真实有效。如有违反，您有权将该行为理解为本人有骗贷的嫌疑，并有权向本人收取借款金额10%的违约金（不足1万元的，按1万元收取），并有权单方面终止编号为  "),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("  的《借款合同》。")} , 
								textfont, Paragraph.ALIGN_LEFT));	
						document.add(createParagraph(new Phrase[]{createPhrase("5、本人承诺不会私自更换车门开启及点火启动控制装置。如有违反，您有权将该行为理解为本人有骗贷的嫌疑，并有权向本人收取借款金额10%的违约金（不足2万元的，按2万元收取），并有权单方面终止编号为  "),
								createPhrase(createChunk(mapLoan.get("loanNo"))),
								createPhrase("  的《借款合同》。")} , 
								textfont, Paragraph.ALIGN_LEFT));						
						document.add(createParagraph("6、本人保证在GPS安装完毕后现场试车。如有问题，现场协商解决。车辆一经驶离，我不会以任何理由向您提出赔偿请求。",
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph("承诺人：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("时间：" + dateStartCH,
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph("    经过合同专员的讲解与相互交流，本人对本承诺书条款已充分知情。本人保证不以“不知情”为由逃避自己应当承担的责任。",
								textfont, Paragraph.ALIGN_LEFT));
						
						document.add(createParagraph("知情人：",
								textfont, Paragraph.ALIGN_LEFT));
						document.add(createParagraph("时间：" + dateStartCH,
								textfont, Paragraph.ALIGN_LEFT));
					}
				}
			}else{
				document.add(createParagraph("借款人身份证输入错误!请认真填写!", Titlefont,
						Paragraph.ALIGN_CENTER));
			}
			document.close();
		}
	}
}



















