package com.dutiantech;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import com.alibaba.fastjson.JSONObject;
import com.dutian.sohusdk.Mail;
import com.dutian.sohusdk.Sender;
import com.dutiantech.controller.pay.SafeUtil;
import com.dutiantech.plugins.HttpRequestor;
import com.dutiantech.service.BaseService;
import com.dutiantech.util.CommonUtil;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.FileOperate;
import com.dutiantech.util.StringUtil;

public class Test {
	
	@SuppressWarnings("unchecked")
	public static <T> T getIns(Class<? extends BaseService> cls){
		T bs = null ;
		try {
			bs = (T) cls.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return bs;
	}
	
	public static boolean checkSign4lianlianWithRSA( Map<String, String> response ){
		String pubKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCSS/DiwdCf/aZsxxcacDnooGph3d2JOj5GXWi+q3gznZauZjkNP8SKl3J2liP0O6rU/Y/29+IUe+GTMhMOFJuZm1htAtKiu5ekW0GlBMWxf4FPkYlQkPE0FtaoMP3gYfh+OwI+fIRrpW3ySn3mScnc6Z700nU/VYrRkfcSCbSnRwIDAQAB";
		String signValue = response.get("sign") ;
		response.remove("sign") ;
		
		Iterator<String> keys = response.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = response.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		
		return SafeUtil.checksign(pubKey, buff.toString(), signValue) ;
	}
	
	public static void unbind(String userCode){
		try {
			Map<String , String > requestInfo = new TreeMap<String , String>();
			requestInfo.put("oid_partner", "201505261000341507" );
			requestInfo.put("sign_type", "RSA");
			requestInfo.put("user_id", userCode);
			requestInfo.put("offset", "0");
			requestInfo.put("pay_type", "D");
			sign4lianlianWithRSA( requestInfo );
			HttpRequestor http = new HttpRequestor() ;
			String responseBody = http.doPost("https://yintong.com.cn/queryapi/bankcardbindlist.htm", JSONObject.toJSONString(requestInfo) ) ;
			@SuppressWarnings("unchecked")
			Map<String , Object > responseData = JSONObject.parseObject(responseBody , TreeMap.class ) ;
			String resultCode = (String) responseData.get("ret_code");
			System.out.println(resultCode);
			System.out.println(JSONObject.toJSONString(responseData,true));
//			if( "0000".equals(resultCode) == true ){
//				System.out.println("已签约，开始解绑...");
//				JSONArray zz = (JSONArray) responseData.get("agreement_list");
//				for (int i = 0; i < zz.size(); i++) {
//					JSONObject xx = (JSONObject) zz.get(i);
//					Map<String , String > requestInfo1 = new TreeMap<String , String>();
//					requestInfo1.put("oid_partner", "201505261000341507" );
//					requestInfo1.put("sign_type", "RSA");
//					requestInfo1.put("user_id", userCode);
//					requestInfo1.put("offset", "0");
//					requestInfo1.put("no_agree",xx.getString("no_agree"));
//					requestInfo1.put("pay_type", "D");
//					sign4lianlianWithRSA( requestInfo1 );
//					System.out.println(JSONObject.toJSONString(requestInfo1));
//					String responseBody1 = http.doPost("https://yintong.com.cn/traderapi/bankcardunbind.htm", JSONObject.toJSONString(requestInfo1) ) ;
//					@SuppressWarnings("unchecked")
//					Map<String , Object > responseData1 = JSONObject.parseObject(responseBody1 , TreeMap.class ) ;
//					String resultCode1 = (String) responseData1.get("ret_code");
//					if(resultCode1.equals("0000")){
//						System.out.println("解绑成功!");
//						System.out.println(JSONObject.toJSONString(responseData1,true));
//					}
//				}
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void xxx(){
		//发他妈的邮件给一起好
		Map<String, String> params = new HashMap<String, String>();
		params.put("countDate", "2016年6月14日");
		//沉淀资金
		params.put("cdzj","0.1万");
		//可用余额
		params.put("kyye","0.05万");
		//冻结余额
		params.put("djye","0.05万");
		//充值总额
		params.put("czze","0.1万");
		//人工充值
		params.put("rgcz","0.05万");
		//连连支付充值
		params.put("llzfcz","0.05万");
		//今日成交总额
		params.put("jrcjze","0.1万");
		//提现总额
		params.put("txze","0.1万");
		//人工提现
		params.put("rgtx","0.05万");
		//连连支付提现
		params.put("llzftx","0.05万");
		//应还总额
		params.put("yhze","12万");
		//待还总额
		params.put("dhze","0.00万");
		//应还本金
		params.put("yhbj","11万");
		//待还本金
		params.put("dhbj","0.00万");
		//应还利息
		params.put("yhlx","1万");
		//待还利息
		params.put("dhlx","0.00万");
		//实际还款总额
		params.put("sjhkze", "12万");
		//实际还款本金
		params.put("sjhkbj","11万");
		//实际还款利息
		params.put("sjhklx","1万");
		//提前还款本金
		params.put("tqhkbj","1万");
		//累计成交金额
		params.put("ljcjze","100万");
		//累计赚取收益
		params.put("ljzq","50万");
		//1-3月标成交金额
		params.put("m13","1万");
		//4-6月标成交金额
		params.put("m46","2万");
		//7-12月标成交金额
		params.put("m712","3万");
		//13-18月标成交金额
		params.put("m1318","4万");
		//质押宝成交金额
		params.put("pa","5万");
		//车稳赢成交金额
		params.put("pb","6万");
		//房稳赚成交金额
		params.put("pc","7万");
		//新手标成交金额
		params.put("noob","8万");
		//本日债权转让承接金额
		params.put("jrzqzr","9万");
		//累计债权转让承接金额
		params.put("ljzqzr","10万");
		
		Sender sender = new Sender();
		String tmp_toEmails = "6134642@qq.com,88288291@qq.com,2215567@qq.com,649143182@qq.com";
		String[] toEmails = tmp_toEmails.split(",");
		for (int i = 0; i < toEmails.length; i++) {
			Mail mail = new Mail();
			mail.setTo(toEmails[i]);
			mail.setParams(params);
			sender.putMail(mail);
		}
		sender.setTemplate_invoke_name("22170_wow_99");
		System.out.println(sender.sendMail());
	}
	
	public static void sign4lianlianWithRSA(Map<String, String> params){
		Iterator<String> keys = params.keySet().iterator() ;
		StringBuffer buff = new StringBuffer() ;
		while(keys.hasNext() ) {
			String key = keys.next() ;
			String value = params.get(key) ;
			if( StringUtil.isBlank(value) == false ){
				if(buff.length() > 0 ){
					buff.append("&");
				}
				buff.append(key + "=" + value );
			}else{
				//remove 
				//params.remove(key) ;
			}
		}
		String tmpString = buff.toString() ;
		String priKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBALy9wZnzma2QAABvGkyJ6BbysYLDGVGltM/KLDzMZYtGynyP9dhn4JoAY/iWGW3uUbQTtFxe1pYIsBZ0yzTOYi8PkdfPKjG8hCOccd4/md5NPjNP46EO8nsf0aZhip7cZBWAMyEtsupiFwPnaPBXV0GPf/wWSdC6FZArxcJmlAdRAgMBAAECgYAQHPcZSJW3HpMRdmq9XAynYOLMshFISihMkQsDycNlh46j4bpwVjXzj9L5Fv9mxjDNed0tAZ+6QVWfJStv/6qcClz/QWfrx7ZAxodEEf0Vrx7KoKkYmW2FlhD11/vhUFPb91n5ksw+BHwAfQIU9axN51EOB+B1Zx3aVIq3jMWdfQJBAOT5um5JtrAFdt/Uugw4KZab0A7Rx0mzNTot3ELeWBBxiGFkcvnO3V3sEuppbxuvHOZ0LBIe6YxL3muT6Y701SMCQQDTBGOMYcay551fgA87s7bPC7pCgDM+WwAAhNFCmnbUfuohgnSy6l+d53Qq9LVEkSXSUbXkqmjqA0eUre5HcJr7AkAzR24mBuIf94lQxV5JIEbIEOr+dqKP8c9o0R5z50GHpTVqwkkxgs92mkj+MFCOvZ/WSIwaHswk/FS6eOykdFZLAkBVkrUQC+K5UIYYYWVMD8A1zIq3RygAxISGsVXvTZac6+7ksfPDTpqB/Ye1l9EewkH1PZ+m4Jh1NelEWRiFLhwbAkEAlseBPMr2THrXgRbti//ADxJdqO6uICAoVnj+u5bjfjXMIKeF3Nz07hY2aASAVkEVw9ELI7PcT/iS+vcYfKrBbA==";
		String signValue = SafeUtil.sign( priKey , tmpString ) ;
		if( signValue != null ){
			params.put("sign", signValue ) ;
		}
	}
	
	public static boolean isCanClear(int nowDay){
		Integer[] days = getClearDay() ;
		for( int day : days ){
			if( day == nowDay ){
				return true ;
			}
		}
		return false ;
	}
	
	private static Integer[] getClearDay(){
		Calendar nowCal = Calendar.getInstance() ;
		int nowDay = nowCal.get( Calendar.DAY_OF_MONTH ) ;
//		nowCal.set(Calendar.DAY_OF_MONTH , -1 );
//		int lastDay = nowCal.get( Calendar.DAY_OF_MONTH );
		Integer[] days = null ;
		int lastDay = nowCal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);
		System.out.println(lastDay);
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
	
//	//修改手机号
//	public static void main(String[] args) throws Exception {
//		String passwd = CommonUtil.encryptPasswd("123456");
//		String authcode = CommonUtil.buildLoginAuthCode("13307310546", "123456");
//		String mobile = CommonUtil.encryptUserMobile("13307310546");
//		
//		System.out.println("passwd:" + passwd);
//		System.out.println("authcode:" + authcode);
//		System.out.println("mobile:" + mobile);
//	}

	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		try {
//			String wxMsgUrl = "http://wxapi.yrhx.com/weixin/service/sendTempMsg";
////			String wxMsgUrl = "http://192.168.2.155/weixin/service/sendTempMsg";
//			TemplateMessage msgxxx = new TemplateMessage() ;
//			msgxxx.setTemplateId("KOU41Tac7axGmgzxgRBQM5cd81Rge1yNznaMb_aQdGA");
//			msgxxx.setTouser("oFLoGuFbL00xJ8F5UCcvMusKY2CM");
//			msgxxx.setData("keyword1", "易融恒信");
//			msgxxx.setData("keyword2", "13800000000");
//			msgxxx.setData("keyword3", "123456");
//			msgxxx.setData("keyword4", DateUtil.getStrFromNowDate("yyyy-MM-dd HH:mm:ss"));
//			msgxxx.setData("remark", "您正在使用提现功能，请注意验证码保密。");
//			String result = HttpRequestUtil.sendGet(wxMsgUrl, "appid=wx377e1b9c96a05ce6&body="+URLEncoder.encode( msgxxx.toJSONString(),"UTF-8"));
//			System.out.println( result );
//			System.out.println(String.valueOf( 10000000/10.00/10.00 ));
//			unbind("09172a912f98fd37eff281c068e0ca47");
//			String mobile=  "13365555020";
//			String content = "亲爱的江南小福犬，您收到2笔回款，总金额129.38元。【易融恒信】";
//			String mobile=  "18717129832";
//			String content = "亲爱的laosunaqing，您收到13笔回款，总金额1038.37元。【易融恒信】";
//			SMSService smsService = new SMSService();
//			int x = smsService.sendSms(mobile, content);
//			System.out.println(x);
//			String msgInfo = "测试短信   【易融恒信】";
//			SMSClient.sendSms("18717129832", msgInfo);
//			long a = CommonUtil.f_000(720400, 1, 2130, 1, "A")[1];
//			long b = CommonUtil.f_000(706600, 1, 2130, 1, "A")[1];
//			long c = CommonUtil.f_000(611500, 1, 2130, 1, "B")[1];
//			long d = CommonUtil.f_000(3145600, 1, 2130, 1, "B")[1];
//			long e = CommonUtil.f_000(509500, 1, 2130, 1, "A")[1];
//			System.out.println("a:"+a);
//			System.out.println("b:"+b);
//			System.out.println("c:"+c);
//			System.out.println("d:"+d);
//			System.out.println("e:"+e);
//			System.out.println("总共："+(a+b+c+d+e));
//			System.out.println(UIDUtil.generate());
		} catch (Exception e) {
			e.printStackTrace();
		}

//		String userCardId = CommonUtil.decryptUserCardId("1a911d1859cd155e35a0210a2b3ea986e75c7eb93e2855aa");
//		userCardId = "***"+userCardId.substring(userCardId.length()-4, userCardId.length());
//		System.out.println(userCardId);
//		System.out.println(UIDUtil.generate());
//		String month = "3";
//		month = month.length() == 1 ? "0"+month : month;
//		System.out.println(month);
		
//		FileOperate fo = new FileOperate();
//		String[] mobile = fo.readTxtLine("E://mobile.txt", "");
//		StringBuffer sb = new StringBuffer();
//		for (String string : mobile) {
//			String str = CommonUtil.decryptUserCardId(string);
//			if (str.length() > 12) {
//				String m = IdentityUtil.getAstro(CommonUtil.decryptUserCardId(string));
//				sb.append(m).append("\r\n");
//			} else {
//				sb.append("").append("\r\n");
//			}
//		}
//		fo.createFile("E://DeryptMobile.txt", sb.toString());
		
		
		FileOperate fo = new FileOperate();
//		String[] loanInfo = fo.readTxtLine("E://mobile.txt", "");
//		StringBuffer sb = new StringBuffer();
//		for (String string : loanInfo) {
//			String[] arr = string.split("@@");
////			System.out.println(arr[1] + "@@" + arr[2] + "@@" + arr[3] + "@@" + arr[4]);
//			long[] zonglixi = CommonUtil.f_004(Long.parseLong(arr[1]), Integer.parseInt(arr[2]), Integer.parseInt(arr[3]), String.valueOf(arr[4]));
//			System.out.println(zonglixi[1]);
//			sb.append(zonglixi[1]).append("\r\n");
//			string = CommonUtil.decryptUserMobile(string);
//			string = CommonUtil.decryptUserCardId(string);
//			if (!StringUtil.isNumeric(string)) {
//				string = "";
//			}
//			sb.append(string).append("\r\n");
//			string = IdentityUtil.getAstro(string);
//			int age;
//			if (!StringUtil.isBlank(string) && string.length() > 10) {
//				age = IdentityUtil.getAge(string);
//			} else {
//				age = 0;
//			}
//			sb.append(string).append("\r\n");
//		}
//		fo.createFile("E://DeryptMobile.txt", sb.toString());
		
//		long[] zonglixi = CommonUtil.f_004( loanAmount , rateByYear , loanTimeLimit , refundType) ;
//		long[] zonglixi = CommonUtil.f_004(8141100 , 1700 , 12 , refundType.A.val());
//		System.out.println(zonglixi[0]);
//		System.out.println(zonglixi[1]);
//		long[] lixi = CommonUtil.f_001(8141100, 12, 1700, 1);
//		System.out.println(lixi[0]);
//		System.out.println(lixi[1]);
//		long points = CommonUtil.f_005(13700 , 1 , "B") ;
//		System.out.println(points);
//		System.out.println(CommonUtil.encryptUserCardId("05005565"));
//		System.out.println(CommonUtil.decryptUserCardId("da0b24aeadc9bea01e200e032d277112a346adc4a21fcaa8"));
//		System.out.println(CommonUtil.isBirth("40d7aee7d9a31c28e21bb0a9d95d5ce0e7867e2b297b309e"));
//		System.out.println(CommonUtil.decryptUserMobile("2a40699fa6c8b3c0d6a7fbaf55f2f19c"));
//		System.out.println(CommonUtil.genMchntSsn());
//		System.out.println(IdentityUtil.getAstro(CommonUtil.decryptUserCardId("b7849a22fb9407431028cf83dc37f3e70f8e01dc3f819b09")));
//		System.out.println(CommonUtil.encryptUserMobile("17180200974"));
//		System.out.println(UIDUtil.generate());
//		System.out.println(DateUtil.getNowDateTime());
//		System.out.println(DateUtil.getNowDateTime(-1000 * 60 * 10));
//		System.out.println(DateUtil.getDateStrFromDateTime(new Date(System.currentTimeMillis() + 15*60*1000)));
//		System.out.println(CommonUtil.encryptUserCardId("420323197809140069"));
//		System.out.println(CommonUtil.genMchntSsn());
//		String loginAuth = MD5Code.md5( "13986264294" + "stonexk0112" ) ;
//		System.out.println(loginAuth);
//		System.out.println("18995514237".substring("18995514237".length() - 6, "18995514237".length()));
//		int limit = 12;
//		for (int i = 1; i <= limit; i++) {
//			long[] recyAmount = CommonUtil.f_000(8141100, limit, 17, i, "A");
//			System.out.println(recyAmount[0] + "::::" + recyAmount[1]);
//		}
//		System.out.println(String.format("%016d", 1400));
//		FileOperate file = new FileOperate();
//		String[] content = file.readTxtLine("E:\\3005-BIDRESP-301002-20180523", "GBK");
//		System.out.println(content.length);
//		for (int i = 0; i < content.length; i++) {
////			String line = content[i].split(" ")[0];
////			String uid = content[i].split("\t")[1];
////			String name = content[i].substring(80, 148);
////			String authCode = content[i].substring(150);
////			System.out.println(line + "::" + uid + "::" + name + "::" + authCode);
//			byte[] rspCode = content[i].getBytes();
//			System.arraycopy(content[i], 0, rspCode, 0, 160);
//		}
		
//		String[] queryTxCode = new String[]{"accountIdQuery", "accountDetailsQuery2", "accountQueryByMobile",
//				"balanceQuery", "batchVoucherDetailsQuery", "freezeAmtQuery", "creditDetailsQuery",
//				"cardBindDetailsQuery", "debtDetailsQuery", "mobileMaintainace", 
//				"transactionStatusQuery", "batchQuery", "batchDetailsQuery", "creditInvestQuery", "bidApplyQuery",
//				"creditAuthQuery", "corprationQuery", "frzDetailsQuery", "passwordSetQuery", "balanceFreezeQuery",
//				"fundTransQuery", "creditInvesDetailsQuery", "termsAuthQuery"};
//		Arrays.sort(queryTxCode);
//		System.out.println(Arrays.binarySearch(queryTxCode, "batchLendPay"));
//
//		System.out.println(DateUtil.differentMinuteByMillisecond("20180527120000", "20180529173232", "yyyyMMddHHmmss"));
//		List<String> test = new ArrayList<String>();
//		test.add("aaaaaa");
//		test.add("bbbbbb");
//		System.out.println(StringUtil.getMoneyYuan(14400));
		
//		JSONArray ja = JSONArray.parseArray("[{\"amount\":0,\"isDel\":\"Y\",\"rate\":250,\"code\":\"267c2d635d4641ea98820e1c7a6be68c\",\"type\":\"C\"}]");
//		for (int i = 0; i < ja.size(); i++) {
//			JSONObject jsonObj = ja.getJSONObject(i);
//			System.out.println(jsonObj.getString("code"));
//			System.out.println(jsonObj.getInteger("rate"));
//		}
//		System.out.println((CommonUtil.f_000(677400, 24, 1750, 6, "A")[1]*5/100));
//		return CommonUtil.f_000(payAmount, loanTimeLimit, rateByYear+rewardRateByYear, loanTimeLimit-loanRecyCount, refundType)[1]*5/100;
//		System.out.println( CommonUtil.f_005( 33600 , 24 , "A"));
		
		
		/************************************************************************************/
		
//		String str = "[{\"amount\":0,\"code\":\"8fd1fc11939943e59ad9fb5cb63cd6d8\",\"rate\":100,\"type\":\"C\",\"isDel\":\"N\"}]";
//		JSONArray jsonArray = JSONArray.parseArray(str);
//		JSONObject jsonObject = jsonArray.getJSONObject(0);
//		System.out.println(jsonObject.get("rate"));
		
		int loanTimeLimit = 12;
		for (int j = 1; j <= loanTimeLimit; j++) {
//			long[] amount = CommonUtil.f_000(8141100, loanTimeLimit, 1350 + 350, j, "A");
			int i = loanTimeLimit - j + 1;
			i = loanTimeLimit - i + 1;
			long[] amount = CommonUtil.f_000(1000000, 12, 1250, i, "B");
			System.out.println("Principal:" + amount[0] + "=== Interest:" + amount[1]);
		}
//		System.out.println("42010619870112361x".contains("x"));
//		System.out.println(DateUtil.subDate("20181018", 1, "DAY"));
//		NumberFormat numberFormat = NumberFormat.getInstance();
//		numberFormat.setMaximumFractionDigits(2);
//		System.out.println(numberFormat.format((float)1 / (float)2 * 100) + "%");
//		System.out.println(DateUtil.isValidDate("201801", "yyyyMMdd"));
//		System.out.println(DateUtil.parseDateTime(DateUtil.getDateFromString("201801", "yyyyMMdd"), "yyyy-MM-dd"));
	}	
}
