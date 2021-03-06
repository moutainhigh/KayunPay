package com.dutiantech.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dutiantech.config.AdminConfig;
import com.dutiantech.plugins.Memcached;

public class CommonUtil {
	
	public static String DESKEY = "selangshiwo.com";
	public static String PWDKEY = "hao@2013@yrhx";
	public static String MCHNT_CD="0005210F0412335"; //正式商户号
	//public static String MCHNT_CD="0002900F0353030"; 
	public static String OUTCUSTNO="13886192153";//人工充值的划拨用户（吴总）
//	public static String ADDRESS="http://59.175.148.70:9500";//外网地址 前台 备机
	public static String VER="0.44";//存管版本号
	public static String fUIOUACCOUNT="08J41233562b";//正式存管商户帐号
//	public static String fUIOUACCOUNT="18112345678";
	public static String PAY_INTERFACE = "fuiou";
	public static String payCpnyID = "00003000";// 第三方支付公司ID(报备使用)  生产:00003000     测试:000001  by WCF
	public static String reportPath = File.separator + "home" + File.separator + "report" + File.separator;//存管文件路径    WCF
	
//	public static String ADDRESS = "http://www.yrhx.com";//外网地址 前台
	public static String ADDRESS = "http://www.yrhx.com";
	public static String APPBACK_ADDRESS = "app://yrhx";
	public static String PC_URL = "http://www.yrhx.com";
	public static String APP_URL = "http://app.yrhx.com";
	public static String NIUX_URL = "http://niux.yrhx.com:8899";
	public static String CALLBACK_URL = "http://115.159.95.166:8899";

	public static String YISTAGE_URL = "http://118.25.53.62";	//旅游分期服务器地址
	
	public static boolean fuiouPort = false;	// 富友存管接口开关
	public static boolean jxPort = true;	// 江西银行存管接口开关
	
	//短信相关配置
	/**
	 * 绑定银行卡
	 */
	public static String SMS_MSG_BINDCARD = "[code]是您的验证码！您正在使用绑定银行卡功能，若非本人操作，请忽略！【易融恒信】";
	/**
	 * 提现
	 */
	public static String SMS_MSG_TX = "[code]是您的验证码，本次申请提现：[money]元。【易融恒信】";
	/**
	 * 注册
	 */
	public static String SMS_MSG_REGISTER = "[code]是您的验证码，欢迎您注册易融恒信金融信息服务平台。【易融恒信】";
	
	/**
	 * 注册（返利投）
	 */
	public static String SMS_MSG_REGISTER_FLT = "尊敬的[name]，您好，感谢您注册易融恒信金融信息服务平台。您的帐号为[mobile]，初始密码为[pwd]，请尽快修改密码。【易融恒信】";
	
	/**
	 * 修改支付密码
	 */
	public static String SMS_MSG_PAYPWD = "[code]是您的验证码，您正在使用修改支付密码功能,若非本人操作，请忽略！【易融恒信】";
	/**
	 * 实名认证
	 */
	public static String SMS_MSG_CERTIFICATION = "[code]是您的验证码，您正在使用实名认证功能，若非本人操作，请忽略！【易融恒信】";
	
	/**
	 * 申请提现
	 */
	public static String SMS_MSG_WITHDRAW = "[code]是您的验证码，您正在使用提现功能，若非本人操作，请忽略！【易融恒信】";
	
	/**
	 * 找回密码
	 */
	public static String SMS_MSG_FINDPWD = "[code]是您的验证码，您正在使用找回密码功能，若非本人操作，请忽略！【易融恒信】";

	/**
	 * 通过邮箱绑定手机号
	 */
	public static final String SMS_MSG_BINDEMAIL = "[code]是您的验证码，您正在使用绑定手机功能，若非本人操作，请忽略！【易融恒信】";

	/**
	 * 手机验证码登录
	 */
	public static String SMS_MSG_LOGIN = "[code]是您的验证码，您正在使用短信验证码登录功能，若非本人操作，请忽略！【易融恒信】";

	/**
	 * 手机承接债权
	 */
	public static String SMS_MSG_TRANSFER = "[code]是您的验证码，您正在使用承接债权功能，若非本人操作，请忽略！【易融恒信】";
	
	/**
	 * 手机承接债权
	 */
	public static String SMS_MSG_RECHARGE = "[code]是您的验证码，您正在使用充值功能，若非本人操作，请忽略！【易融恒信】";

	/**
	 * 回款短信
	 */
	public static String SMS_MSG_MONEY = "亲爱的[payName]，您收到[loanCount]笔回款，总金额[money]元；其中正常回款[normalLoanCount]笔，金额[normalMoney]元,提前还款[earlyLoanCount]笔，金额[earlyMoney]元。";
	
	/**
	 * 充值到账短信
	 */
	public static String SMS_MSG_RECHARAGE_ONLINE = "[userName]您好，您通过(线上充值)充值([payAmount]元)已经通过，收取手续费(0)。";

	/**
	 * 手动投标短信--满标审核后
	 */
	public static String SMS_MSG_MLOAN = "[userName]您已成功投资，借款标(编号：[loanNo]，金额：[payAmount]元)，满标后开始计息。";
	
	/**
	 * 自动投标短信--满标审核后
	 */
	public static String SMS_MSG_AUTOLOAN = "[userName]您已自动投资，借款标(编号：[loanNo]，金额：[payAmount]元)，满标后开始计息。";
	
	/**
	 * 流标短信
	 */
	public static String SMS_MSG_LIUBIAO = "[userName]您好，借款标(编号：[loanNo]，金额：[payAmount]元)，此标已进行流标处理，投资金额已返还至您的账户，请注意查收。";
	
	/**
	 * 活动现金抵用券发放通知
	 */
	public static String SMS_MSG_TICKET = "[huoDongName],奖励现金抵用券[ticketAmount]元，请尽快使用以免过期作废。";
	
	/**
	 * 提现申请审核后短信通知
	 */
	public static String SMS_MSG_WITHDRAW_ONLINE = "[userName]您好，您申请提现([payAmount])元已审核通过，收取手续费([fee])元。已进入银行转账流程，请注意查收。";
	
	/**
	 * 生日当天投资送积分短信通知
	 */
	public static String SMS_MSG_BIRTHDAYLOAN = "尊敬的[userName]，您好！在这个特殊的日子里，易融恒信易家人祝您生日快乐，幸福安康！[tempInfo]";
	
	/**
	 * 修改手机号
	 */
	public static String SMS_MSG_PHONE = "[code]是您的验证码，您正在使用修改手机号功能，若非本人操作，请忽略！【易融恒信】";
	////////////////////////////////////////////////

	/**
	 * 通知借款人--债权变更后
	 */
	public static String SMS_SMG_TRANSFER_CHANGE = "[loanName]，您好，([transferName],[transferCardId])将持有的《易融恒信借款协议》(编号：[loanNo])项下的债权[transAmount]元已全额转让至([carryOnName]，[carryOnCardId])，与此转让债权相关的其他权利义务一并转让。特此短信通知！[nowDate]。";
	
//	/**
//	 * 微信授权路径_code
//	 */
//	public static String OAUTH_CODE_URL = "http://weixin.yrhx.com/weixin/oauth2GetCode";
	
	/**
	 * 微信授权路径_accessToken
	 */
	public static String OAUTH_ACCESSTOKEN_URL = "http://wxapi.yrhx.com/weixin/api/oauth4weixinByCode";
	
	
	
	/**
	 * 验证还款中的标，今天是否还款日
	 * @param date 放款日期 yyMMdd
	 * @return
	 */
	public static boolean validateLentDate(String date) throws ParseException{
		java.util.Calendar zz = java.util.Calendar.getInstance();
		int lastDay = zz.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);//当月最后一天
		int nowDay = zz.get(java.util.Calendar.DAY_OF_MONTH);
		java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd");
		java.util.Calendar effectDate = java.util.Calendar.getInstance();
		effectDate.setTime(df.parse(date));
		int effectDay = effectDate.get(java.util.Calendar.DAY_OF_MONTH);//审核放款日
		if(effectDay == nowDay){
			return true;
		}
		if(effectDay > lastDay){
			if(nowDay==lastDay){
				return true;
			}
		}
		return false;
	}

	/**
	 * 生成loginAuthCode
	 * @param userMobile	明文手机号
	 * @param passwd		明文密码
	 * @return
	 * @throws Exception 
	 */
	public static String buildLoginAuthCode(String userMobile, String passwd) throws Exception{
		if(StringUtil.isBlank(userMobile) || StringUtil.isBlank(passwd)){
			return null;
		}else{
			return MD5Code.md5( userMobile+ CommonUtil.getSourcePwd(passwd) );
		}
		
	}
	
	/**
	 * 密码加密
	 * @param passwd
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String encryptPasswd(String passwd) throws UnsupportedEncodingException{
		if(StringUtil.isBlank(passwd)){
			return null;
		}
		try {
			return  MD5Code.md5(MD5Code.crypt(MD5Code.crypt(MD5Code.SHA1(passwd+PWDKEY))));
		} catch (Exception e) {
			return null;
		}
	}
	/**
	 * 手机号加密
	 * @param userMobile
	 * @return
	 * @throws Exception
	 */
	public static String encryptUserMobile(String userMobile) throws Exception{
		if(StringUtil.isBlank(userMobile)){
			return null;
		}
		return DESUtil.encode4string(userMobile, DESKEY);
	}
	/**
	 * 手机号解密
	 * @param EnUserMobile 密文手机号
	 * @return
	 * @throws Exception
	 */
	public static String decryptUserMobile(String userMobile) throws Exception{
		if(StringUtil.isBlank(userMobile)){
			return null;
		}
		return DESUtil.decode4string(userMobile, DESKEY);
	}
	
	/**
	 * 身份证加密
	 * @param userCardId	身份证 明文
	 * @return
	 * @throws Exception
	 */
	public static String encryptUserCardId(String userCardId) throws Exception{
		if(StringUtil.isBlank(userCardId)){
			return null;
		}
		return DESUtil.encode4string(userCardId, DESKEY);
	}
	/**
	 * 身份证解密
	 * @param userCardId 身份证密文
	 * @return
	 * @throws Exception
	 */
	public static String decryptUserCardId(String userCardId) throws Exception{
		if(StringUtil.isBlank(userCardId)){
			return null;
		}
		return DESUtil.decode4string(userCardId, DESKEY);
	}
	
	/**
	 * 计算时间差，返回天数
	 * @param date1		最近的新的时间
	 * @param date2		过去的时间
	 * @param format	上面两个参数时间的完整格式化，如：yyyyMMdd HHmmss
	 * @return
	 */
	public static long compareDateTime(String date1, String date2, String format){
		DateFormat df = new SimpleDateFormat(format);
		try{
			Date d1 = df.parse(date1);
			Date d2 = df.parse(date2);
			long diff = d1.getTime() - d2.getTime();
			long days = diff / (1000 * 60 * 60 * 24);
			return days;
		} catch (Exception e) {
			return -1;
		}
	}
	
	/**
	 * 验证满标是不是过了1分钟
	 * @param date1		最近的新的时间
	 * @param date2		过去的时间
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean vilidataManbiao(String date1, String date2){
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			java.util.Date now = df.parse(date1);
			java.util.Date date= df.parse(date2);
			long l=now.getTime()-date.getTime();
			long day=l/(24*60*60*1000);
			long hour=(l/(60*60*1000)-day*24);
			long min=((l/(60*1000))-day*24*60-hour*60);
			long s=(l/1000-day*24*60*60-hour*60*60-min*60);
			System.out.println("相差"+day+"天"+hour+"小时"+min+"分钟");
			if(day>0 || hour>0){
				return true;
			}
			if(min>=1){
				return true;
			}
			
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * 验证两个日期时间相差等于10分钟
	 * @param date1		最近的新的时间
	 * @param date2		过去的时间
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean validateMinTime(String date1, String date2){
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			java.util.Date now = df.parse(date1);
			java.util.Date date= df.parse(date2);
			long l=now.getTime()-date.getTime();
			long day=l/(24*60*60*1000);
			long hour=(l/(60*60*1000)-day*24);
			long min=((l/(60*1000))-day*24*60-hour*60);
			long s=(l/1000-day*24*60*60-hour*60*60-min*60);
//			System.out.println("相差"+day+"天"+hour+"小时"+min+"分钟");
			if(min>=10){
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * 验证两个日期时间相差分钟
	 * @param date1	最近的新的时间
	 * @param date2	过去的时间
	 * @param space	相差时间（分钟）
	 * @return
	 */
	@SuppressWarnings("unused")
	public static boolean validateMinTime(String date1, String date2, int space) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			java.util.Date now = df.parse(date1);
			java.util.Date date= df.parse(date2);
			long l=now.getTime()-date.getTime();
			long day=l/(24*60*60*1000);
			long hour=(l/(60*60*1000)-day*24);
			long min=((l/(60*1000))-day*24*60-hour*60);
			long s=(l/1000-day*24*60*60-hour*60*60-min*60);
			if(min >= space){
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}
	
	/**
	 * 	根据类型获取某一个月的本金利息
	 * @param amount		投资金额
	 * @param limit			标的期限
	 * @param rate			年化利率
	 * @param countLimit	第几期
	 * @param rt			还款方式：A=等额本息，B=先息后本
	 * @return
	 */
	public static long[] f_000(long amount,int limit, int rate, int countLimit , String rt){
		if( "A".equals(rt) == true ){
			return f_001(amount, limit, rate, countLimit);
		}else if( "B".equals(rt) == true ){
			return f_002(amount, limit, rate, countLimit);
		}
		return new long[]{ 0 , 0};
	}
	
	/**
	 * 按月等额本息
	 * @param amount	金额 单位分
	 * @param limit		总期数
	 * @param rate		年利率
	 * @param countLimit 第几期
	 * @return int数组 [本金,利息]
	 */
	public static long[] f_001(long amount,int limit, int rate, int countLimit){
		LiCai tmp = new LiCai(Math.round(amount), Math.round(rate), limit);
		Map<String,Long> map = tmp.getDengE4month(countLimit);
		long x = (long) Math.rint(map.get("ben"));
		long y = (long) Math.rint(map.get("xi"));
		return new long[]{x,y};
	}
	/**
	 * 按月等额本息
	 * @param amount	金额 单位分
	 * @param limit		总期数
	 * @param rate		年利率
	 * @param countLimit 第几期
	 * @return int数组 [本金,利息]
	 */
	public static long[] f_0014loan(long amount,int limit, int rate, int countLimit){
		LiCai tmp = new LiCai(Math.round(amount), Math.round(rate), limit);
		Map<String,Long> map = tmp.getDengE4month4loan(countLimit);
		long x = (long) Math.rint(map.get("ben"));
		long y = (long) Math.rint(map.get("xi"));
		return new long[]{x,y};
	}
	
	/**
	 * 按月付息，到期还本
	 * @param amount
	 * @param rate
	 * @param limit
	 * @param countLimit	第几期
	 * @return
	 */
	public static long[] f_002(long amount,int limit, int rate, int countLimit){
		LiCai tmp = new LiCai(Math.round(amount), Math.round(rate), limit);
		Map<String,Long> x = tmp.getDengXi4month(countLimit);
		return new long[]{x.get("ben"),x.get("xi")};
	}
	
	/**
	 * 计算总利息
	 * @param amount	金额
	 * @param rate		年利率
	 * @param limit		期数(月)
	 * @param ltype		借款类型，A - 等额本息	B - 先息后本
	 * @return
	 */
	@SuppressWarnings("unused")
	public static long[] f_004(long amount, int rate, int limit , String ltype){
//		double rateByMonth = (rate / 12.00)/10000.00;
//		return (int) Math.rint(amount * rateByMonth * limit);
		LiCai tmp = new LiCai(Math.round(amount), Math.round(rate), limit );
		long[] benxi = new long[2] ;
		benxi[0] = amount ;
		if( "A".equals(ltype) ){
			//long bx = (long)tmp.dengebenxi()*limit ;
			//xi = bx - amount ;
			//xi = (long)tmp.dengebenxi() ;
			List<Map<String , Long>> list = tmp.getDengEList() ;
			//int len = list.size() ;
			long ben = 0 ;
			long tx = 0 ;
			for( int i = 0 ; i < list.size() ; i ++ ){
				Map<String , Long> m = list.get(i) ;
				ben += m.get("ben");
				tx += m.get("xi");
			}
//			benxi[0] = ben ;
			benxi[1] = tx ;
		}else if( "B".equals(ltype) ){
			benxi[1] = (long) (tmp.dengxi()*limit) ;
		}
//		Map<String,Long> map = tmp.getDengE4month(limit);
		
		return benxi ;
	}
	
	/**
	 * 	计算剩余未还的总本金和总利息
	 * @param amount
	 * @param rate
	 * @param limit
	 * @param reciedCount
	 * @param type
	 * @return
	 */
	public static long[] f_005(long amount , int rate , int limit , int reciedCount , String type ){
		
		LiCai tmp = new LiCai( amount , rate, limit );
		long[] benxi = new long[2] ;
		if( limit <= reciedCount ){
			return benxi ;
		}

		benxi[0] = 0 ;
		benxi[1] = 0 ;
		if( "A".equals(type) == true ){
			List<Map<String , Long>> list = tmp.getDengEList() ;
			for( int i = (reciedCount) ; i < limit ; i ++ ){
				Map<String , Long> m = list.get(i) ;
//				ben += m.get("ben");
//				tx += m.get("xi");
				benxi[0] += m.get("ben") ;
				benxi[1] += m.get("xi") ;
			}
		}else{
			benxi[0] = amount ;
			benxi[1] = (long) (tmp.dengxi()*(limit-reciedCount))  ;
		}
		
		return benxi ;
	}
	
	
	/**
	 * 	计算可得积分
	 * @param amount
	 * @param limit
	 * @param refundType	
	 * 	A - 等额本息
	 * 	B - 先息后本
	 * @return
	 */
	public static long f_005(long amount , int limit , String refundType ){
		double[] pm = POINTS_MAP.get(limit ) ;
		double point = 0 ;
		double am = pm[0] ;//单位分
		if( "A".equals(refundType) ){
			point = pm[1] ;
		}else if( "B".equals(refundType)) {
			point = pm[2] ;
		}
		point = amount/am*point ;
		return (long)point ; 
	}
	
	/**
	 * 	计算未还本金和利息
	 * @param amount
	 * @param rate
	 * @param limit
	 * @param recyCount	已还期数
	 * @param refundType
	 * @return
	 */
	public static long[] f_006(long amount , int rate , int limit , int recyCount , String refundType ){
		
		long lastPrincipal = 0 ;	//剩余本金
		long lastInterest = 0 ;		//剩余利息
		while( recyCount < limit ){
			
			long[] benxi = f_000(amount, limit, rate, (recyCount+1) , refundType) ;
			lastPrincipal += benxi[0] ;
			lastInterest += benxi[1] ;
			recyCount ++ ;
		}
		
		return new long[]{ lastPrincipal , lastInterest } ;
	}
	
	public static void main(String[] args) {
		List<Map<String,String>> x = getAnyDateFromMonthLength(5,2016,3,"yyyyMMdd",0);
		for (int i = 0; i < x.size(); i++) {
			System.out.println(x.get(i).get("start"));
			System.out.println(x.get(i).get("start").substring(0,6));
			System.out.println(x.get(i).get("end"));
			System.out.println("------------------------");
		}
		
		List<String> y = getAnyDateFromDayLength(12, 0, 0, 0,"yyyyMMdd",0);
		
		for (int i = 0; i < y.size(); i++) {
			System.out.println(y.get(i));
			System.out.println("------------------------");
		}
		
	}
	
	/**
	 * 以指定的年月份为基准，获取前后limit个月，每个月的开始日期和结束日期
	 * @param limit		获取几个月
	 * @param year		指定基准年份 0默认本年
	 * @param month		指定基准月份 0默认本月
	 * @param format	格式化
	 * @param type		0计算基准年月份过去的limit个月，1计算基准年月份以后的limit个月
	 * @return	List<Map<"开始日期","结束日期">>
	 */
	public static List<Map<String,String>> getAnyDateFromMonthLength(int limit,int year,int month,String format,int type){
		if(StringUtil.isBlank(format)){
			format = "yyyyMMdd";
		}
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		java.util.Calendar date = java.util.Calendar.getInstance();
		if(year>0)
			date.set(java.util.Calendar.YEAR, year);
		
		if(month>0)
			date.set(java.util.Calendar.MONTH, month-1);
		date.set(java.util.Calendar.DAY_OF_MONTH, 1);
		String lastDate1 = DateUtil.getStrFromDate(date.getTime(), format);
		date.set(java.util.Calendar.DAY_OF_MONTH, date.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
		String lastDate2 = DateUtil.getStrFromDate(date.getTime(), format);
		
		Map<String,String> index_date = new HashMap<String, String>();
		index_date.put("start", lastDate1);
		index_date.put("end", lastDate2);
		result.add(index_date);
		for (int i = 0; i < limit-1; i++) {
			if(type==0)
				date.set(java.util.Calendar.MONTH, date.get(java.util.Calendar.MONTH)-1);
			else if(type==1)
				date.set(java.util.Calendar.MONTH, date.get(java.util.Calendar.MONTH)+1);
			date.set(java.util.Calendar.DAY_OF_MONTH,1);
			String tmp1 = DateUtil.getStrFromDate(date.getTime(), format);
			date.set(java.util.Calendar.DAY_OF_MONTH, date.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
			String tmp2 = DateUtil.getStrFromDate(date.getTime(), format);
			Map<String,String> tmp = new HashMap<String, String>();
			tmp.put("start", tmp1);
			tmp.put("end", tmp2);
			result.add(tmp);
		}
		return result;
	}
	
	/**
	 * 根据基准日期，计算前后limit天的日期数据
	 * @param limit	获取几天
	 * @param year	基准日期年份，0默认本年
	 * @param month	基准日期月份，0默认本月
	 * @param day	基准日期日，0默认当天
	 * @param type	0减少，获取过去的，1增加，获取未来的
	 * @param format 格式化时间
	 * @return List<String>
	 */
	public static List<String> getAnyDateFromDayLength(int limit,int year,int month,int day,String format,int type){
		if(StringUtil.isBlank(format)){
			format = "yyyyMMdd";
		}
		List<String> result = new ArrayList<String>();
		java.util.Calendar date = java.util.Calendar.getInstance();
		
		if(year>0)
			date.set(java.util.Calendar.YEAR,year);
		if(month>0)
			date.set(java.util.Calendar.MONTH,month-1);
		if(day>0)
			date.set(java.util.Calendar.DAY_OF_MONTH,day);
		String index_date = DateUtil.getStrFromDate(date.getTime(), format);
		result.add(index_date);
		for (int i = 0; i < limit-1; i++) {
			if(type==0)
				date.set(java.util.Calendar.DAY_OF_MONTH,date.get(java.util.Calendar.DAY_OF_MONTH)-1);
			else if(type==1)
				date.set(java.util.Calendar.DAY_OF_MONTH,date.get(java.util.Calendar.DAY_OF_MONTH)+1);
			
			String tmp_date = DateUtil.getStrFromDate(date.getTime(), format);
			result.add(tmp_date);
		}
		return result;
	}
	
	/**
	 * 获取某月的第一天日期和最后一天日期
	 * @param year	0的话默认本年，大于0就是指定年份
	 * @param month 0的话就默认本月，大于0就是指定月份
	 * @param format 返回时间字符的格式化
	 * @return
	 */
	public static String[] getFirstDataAndLastDateByMonth(int year,int month,String format){
		try {
			if(StringUtil.isBlank(format)){
				format = "yyyyMMdd";
			}
			java.util.Calendar date = java.util.Calendar.getInstance();
			month = month -1;
			if(month>-1){
				date.set(java.util.Calendar.MONTH, month);
			}
			if(year>0){
				date.set(java.util.Calendar.YEAR, year);
			}
			date.set(java.util.Calendar.DAY_OF_MONTH, 1);
			
			Date firstDate = date.getTime();
			
			date.set(java.util.Calendar.DAY_OF_MONTH, date.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
			
			Date lastDate = date.getTime();
			
			String firstDateStr = DateUtil.getStrFromDate(firstDate, format);
			
			String lastDateStr = DateUtil.getStrFromDate(lastDate, format);
			
			return new String[]{firstDateStr,lastDateStr};
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
     * 某期还款年月日日期
     * @param date	放款时间 yyyyMMdd
     * @param limit	第几期还款
     * @return
     */
	public static String anyRepaymentDate4string(String date,int limit){
		java.util.Calendar cal = anyRepaymentDate(date,limit) ;
		Date d = cal.getTime() ;
		return DateUtil.getStrFromDate(d, "yyyyMMdd");
	}
	
	/**
     * 某期还款年月日日期
     * @param date	放款时间 yyyyMMdd
     * @param limit	第几期还款
     * @return
     */
	public static java.util.Calendar anyRepaymentDate(String effectDate,int limit){
		try {
			java.text.DateFormat df = new java.text.SimpleDateFormat("yyyyMMdd");
			java.util.Calendar beginDate = java.util.Calendar.getInstance();
			beginDate.setTime(df.parse(effectDate));
			int beginDate_index_year = beginDate.get(java.util.Calendar.YEAR);//放款年份
			int beginDate_index_month = beginDate.get(java.util.Calendar.MONTH);//放款月份
			int beginDate_index_day = beginDate.get(java.util.Calendar.DAY_OF_MONTH);//放款日
			
			java.util.Calendar tmp = java.util.Calendar.getInstance();
			tmp.set(java.util.Calendar.YEAR, beginDate_index_year);
			tmp.set(java.util.Calendar.MONTH, beginDate_index_month);
			tmp.set(java.util.Calendar.DAY_OF_MONTH, 1);
			tmp.set(java.util.Calendar.MONTH, beginDate_index_month+limit);
			
			int tmp_lastDay = tmp.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);//当月最后一天几号
			//检查放款日是否大于当前日期
			if( beginDate_index_day > tmp_lastDay ){
				tmp.set(java.util.Calendar.DAY_OF_MONTH, tmp_lastDay);
			}else{
				tmp.set(java.util.Calendar.DAY_OF_MONTH, beginDate_index_day);
			}
			return tmp;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获取原始密码
	 * @param uPwd
	 * @return
	 */
	public static String getSourcePwd(String uPwd){
		String oldPwd = "";
		try {
			oldPwd = MD5Code.crypt(MD5Code.crypt(MD5Code.SHA1(uPwd + PWDKEY)));
		} catch (UnsupportedEncodingException e) {
			//e.printStackTrace();
		}
		return oldPwd;
	}
	
	/**
	 * 短信码验证
	 * @param key
	 * @param msgMac
	 * @return
	 */
	public static boolean validateSMS(String key , String msgMac){
		String errorKey = key + "_ERRCOUNT";
		Object obj = Memcached.get(key);
		if( AdminConfig.isDevMode == true  ){
			return true ;
		}
		
		if(obj != null && obj.equals(msgMac)){
			//删除短信验证码
			Memcached.delete(key);
			Memcached.delete(errorKey);
			return true;
		}else{
			Object errCount = Memcached.get(errorKey);
			if(null != errCount){
				Long errc = Long.parseLong(errCount.toString());
				if(errc > 10){
					Memcached.delete(key);
					Memcached.delete(errorKey);
				}else{
					Memcached.incr(errorKey, 1);
				}
			}else{
				Memcached.storeCounter(errorKey,1);
			}
			return false;
		}
	}
	
	/**
	 * 验证支付密码
	 * @param userCode
	 * @param payPwd
	 * @param dbPayPwd
	 * @return
	 */
	public static boolean validatePwd(String userCode,String payPwd,String dbPayPwd){
		//验证支付密码   容错5次
		String newPayPwd = "";
		try {
			newPayPwd = CommonUtil.encryptPasswd(payPwd);
		} catch (UnsupportedEncodingException e) {
			return false;
		}
		if(StringUtil.isBlank(dbPayPwd)){
			return false;
		}
		if(dbPayPwd.equals(newPayPwd) == false){
			Object object = Memcached.get("SMS_MSG_WITHDRAW_PWDERROR_" + userCode);
			if(object != null){
				Integer errorCount = Integer.parseInt(object.toString());
				if(errorCount > 5){
					return false;
				}
				Memcached.incr("SMS_MSG_WITHDRAW_PWDERROR_" + userCode, 1);
			}else{
				Memcached.storeCounter("SMS_MSG_WITHDRAW_PWDERROR_" + userCode, 1);
			}
			return false;
		}
		return true;
	}
	
	/**
	 * 验证支付密码
	 * @param userCode
	 * @param payPwd
	 * @param dbPayPwd
	 * @author WCF  20170727
	 * @return
	 */
	public static int validatePwd4App(String userCode,String payPwd,String dbPayPwd){
		//验证支付密码   容错5次
		String newPayPwd = "";
		try {
			newPayPwd = CommonUtil.encryptPasswd(payPwd);
		} catch (UnsupportedEncodingException e) {
			return 01;
		}
		if(StringUtil.isBlank(dbPayPwd)){
			return 02;
		}
		if(dbPayPwd.equals(newPayPwd) == false){
			Object object = Memcached.get("SMS_MSG_WITHDRAW_PWDERROR_" + userCode);
			if(object != null){
				Integer errorCount = Integer.parseInt(object.toString());
				if(errorCount > 5){
					return 03;
				}
				Memcached.incr("SMS_MSG_WITHDRAW_PWDERROR_" + userCode, 1);
			}else{
				Memcached.storeCounter("SMS_MSG_WITHDRAW_PWDERROR_" + userCode, 1);
			}
			return 04;
		}
		return 00;
	}
	
	/** 
     * 手机号验证 
     * @param  str 
     * @return 验证通过返回true 
     */  
	public static boolean isMobile(String str) {   
        Pattern p = null;  
        Matcher m = null;  
        boolean b = false;   
        if(StringUtil.isBlank(str) == false){
	        p = Pattern.compile("^[1][3,4,5,8,7,9][0-9]{9}$"); // 验证手机号  
	        m = p.matcher(str);  
	        b = m.matches();   
        }
        return b;  
    }
	
	 /** 
     * 电话号码验证 
     * @param  str 
     * @return 验证通过返回true 
     */  
    public static boolean isPhone(String str) {   
        Pattern p1 = null,p2 = null;  
        Matcher m = null;  
        boolean b = false;    
        p1 = Pattern.compile("^[0][1-9]{2,3}-[0-9]{5,10}$");  // 验证带区号的  
        p2 = Pattern.compile("^[1-9]{1}[0-9]{5,8}$");         // 验证没有区号的  
        if(str.length() >9)  
        {   m = p1.matcher(str);  
            b = m.matches();    
        }else{  
            m = p2.matcher(str);  
            b = m.matches();   
        }    
        return b;  
    }

    
    /**
     * 生成随机数字
     * @param length	长度
     * @return
     */
    public static String getMathNumber(int length){
    	
    	String str = "";

    	for(int i = 0; i < length; i++){

    		str += (int)(Math.random()*10);

    	};

    	return str;
    }
    
    /**
     * 加减乘除运算
     * @param num1		主数
     * @param num2		被数
     * @param type		jia jian cheng chu quyu取余
     * @param point		四舍五入保留几位
     * @return
     */
    public static BigDecimal yunsuan(String num1, String num2, String type,int point){
		BigDecimal bignum1 = new BigDecimal(num1);
		BigDecimal bignum2 = new BigDecimal(num2);
		BigDecimal result = null;
		
		switch (type) {
			case "jia":
				result =  bignum1.add(bignum2).setScale(point,BigDecimal.ROUND_HALF_UP);
				break;
			case "jian":
				result =  bignum1.subtract(bignum2).setScale(point,BigDecimal.ROUND_HALF_UP);
				break;
			case "cheng":
				result =  bignum1.multiply(bignum2).setScale(point,BigDecimal.ROUND_HALF_UP);
				break;
			case "chu":
				result =  bignum1.divide(bignum2,point,BigDecimal.ROUND_HALF_UP);
				break;
			case "quyu":
				result =  bignum1.divideAndRemainder(bignum2)[1];
				break;
		}
		return result;
	}
    
    /**
     * 每月返佣金额
     * @param payAmount			投资金额
     * @param loanTimeLimit		总期数
     * @param recyLimit			第N期还款
     * @param refundType		还款方式
     * @return
     */
    public static long fanyong(long payAmount, int loanTimeLimit, int recyLimit, String refundType){
    	long fanyong = 0;
    	double tmp1 = CommonUtil.yunsuan(loanTimeLimit+"", "12", "chu", 6).doubleValue();//(投资期限 除以 12)
		double tmp2 = CommonUtil.yunsuan(payAmount+"", tmp1+"", "cheng", 6).doubleValue();//年化交易额=本金 乘以 (投资期限 除以 12)
		double tmp3 = CommonUtil.yunsuan(tmp2+"","1000", "chu", 6).doubleValue();//年化交易额 除以1000
		long tmp4 = CommonUtil.yunsuan(tmp3+"", "5", "cheng", 0).longValue();//年化交易额 乘以 5
		if(refundType.equals("B")){//先息后本
			if(recyLimit == loanTimeLimit ){
				fanyong = tmp4;
			}else{
				return 0;
			}
		}else if(refundType.equals("A")){//等额本息
			long _a = CommonUtil.yunsuan(tmp4+"", loanTimeLimit+"", "chu", 0).longValue();
			if(recyLimit == loanTimeLimit ){//最后一期或提前还款
				fanyong = tmp4 - (_a * (recyLimit -1)) ;
			}else if(recyLimit < loanTimeLimit){
				fanyong = _a;
			}
		}
		return fanyong;
    }
    
    /**
     * 总计剩余返佣金额
     * @param payAmount			投资金额
     * @param loanTimeLimit		总期数
     * @param recyLimit			第N期还款
     * @param refundType		还款方式
     * @return
     */
    public static long fanyong_sy(long payAmount, int loanTimeLimit, int recyLimit, String refundType){
    	long fanyong_sy = 0;
    	for (int i = recyLimit; i <= loanTimeLimit; i++) {
    		fanyong_sy = fanyong_sy +fanyong(payAmount, loanTimeLimit, recyLimit, refundType);
		}
    	return fanyong_sy;
    }
	
	public static Map<Integer , double[]> POINTS_MAP = new HashMap<Integer , double[]>();
	static{
		POINTS_MAP.put( 1 , new double[]{100, 1	, 1 });	
		POINTS_MAP.put( 2 , new double[]{100, 1.3 , 1.7 });
		POINTS_MAP.put( 3 , new double[]{100, 1.6 , 2.4 });
		POINTS_MAP.put( 4 , new double[]{100, 1.9 , 3.1 });
		POINTS_MAP.put( 5 , new double[]{100, 2.2 , 3.8 });
		POINTS_MAP.put( 6 , new double[]{100, 2.5 , 4.5 });
		POINTS_MAP.put( 7 , new double[]{100, 2.8 , 5.2 });
		POINTS_MAP.put( 8 , new double[]{100, 3.1 , 5.9 });
		POINTS_MAP.put( 9 , new double[]{100, 3.4 , 6.6 });
		POINTS_MAP.put( 10 , new double[]{100, 3.7 , 7.3 });
		POINTS_MAP.put( 11 , new double[]{100, 4 , 8 });
		POINTS_MAP.put( 12 , new double[]{100, 4.3 , 8.7 });
		POINTS_MAP.put( 13 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 14 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 15 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 16 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 17 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 18 , new double[]{100, 6.1 , 12.9 });
		POINTS_MAP.put( 19 , new double[]{100, 7.9 , 17.1 });
		POINTS_MAP.put( 20 , new double[]{100, 7.9 , 17.1 });
		POINTS_MAP.put( 21 , new double[]{100, 7.9 , 17.1 });
		POINTS_MAP.put( 22 , new double[]{100, 7.9 , 17.1 });
		POINTS_MAP.put( 23 , new double[]{100, 7.9 , 17.1 });
		POINTS_MAP.put( 24 , new double[]{100, 7.9 , 17.1 });
	}
	
	// \b 是单词边界(连着的两个(字母字符 与 非字母字符) 之间的逻辑上的间隔),    
    // 字符串在编译时会被转码一次,所以是 "\\b"    
    // \B 是单词内部逻辑间隔(连着的两个字母字符之间的逻辑上的间隔)    
    static String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"    
            +"|windows (phone|ce)|blackberry"    
            +"|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"    
            +"|laystation portable)|nokia|fennec|htc[-_]"    
            +"|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";    
    static String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser"    
            +"|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";    
      
    //移动设备正则匹配：手机端、平板  
    static Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);    
    static Pattern tablePat = Pattern.compile(tableReg, Pattern.CASE_INSENSITIVE);    
        
    /** 
     * 检测是否是移动设备访问 
     *  
     * @Title: check 
     * @Date : 2014-7-7 下午01:29:07 
     * @param userAgent 浏览器标识 
     * @return true:移动设备接入，false:pc端接入 
     */  
    public static boolean check(String userAgent){    
        if(null == userAgent){    
            userAgent = "";    
        }    
        // 匹配    
        Matcher matcherPhone = phonePat.matcher(userAgent);    
        Matcher matcherTable = tablePat.matcher(userAgent);    
        if(matcherPhone.find() || matcherTable.find()){    
            return true;    
        } else {    
            return false;    
        }    
    }  
    /**
     * 生成20位流水号
     * */
	public static String genMchntSsn(){
		int ranm=(int)((Math.random()*9+1)*100000);
		return DateUtil.getNowDateTime()+ranm;
	}
	/**
	 * 	生成20位UID
	 * @return
	 */
	public static String genShortUID(){
		UUID uuid = UUID.randomUUID();
		StringBuffer sb = new StringBuffer();
		sb.append(digits(uuid.getMostSignificantBits() >> 32, 8));
		sb.append(digits(uuid.getMostSignificantBits() >> 16, 4));
		sb.append(digits(uuid.getMostSignificantBits(), 4));
		sb.append(digits(uuid.getLeastSignificantBits() >> 48, 4));
		sb.append(digits(uuid.getLeastSignificantBits(), 12));
		int split = xxx();
		return sb.toString().substring(0,split)+new Random().nextInt(10)+sb.toString().substring(split,sb.toString().length());
	}
	
	public static int xxx(){
		int x = 0;
		x = new Random().nextInt(19);
		if(x > 0){
			return x;
		}else{
			x = xxx();
		}
		return x;
	}
	
	private static String digits(long val, int digits) {  
		long hi = 1L << (digits * 4);  
		return Number.toString(hi | (val & (hi - 1)), Number.MAX_RADIX).substring(1);  
	} 
	
	
	/**
	 * 查询省市代码
	 * */
	public static String[] checkCity(String province){
		String city="";
		String cityCode="";
		if(province.equals("重庆市")||province.equals("重庆")){
			city="重庆市";cityCode="6530";
		}else if(province.equals("天津市")||province.equals("天津")){
			city="天津市";cityCode="1100";
		}else if(province.equals("上海市")||province.equals("上海")){
			city="上海";cityCode="2900";
		}else if(province.equals("北京市")||province.equals("北京")){
			city="北京市";cityCode="1000";
		}else if(province.equals("河北省")||province.equals("河北")){
			city="石家庄市";cityCode="1210";
		}else if(province.equals("山西省")||province.equals("山西")){
			city="太原市";cityCode="1610";
		}else if(province.equals("内蒙古自治区")||province.equals("内蒙古")){
			city="呼和浩特市";cityCode="1910";
		}else if(province.equals("辽宁省")||province.equals("辽宁")){
			city="沈阳市";cityCode="2210";
		}else if(province.equals("黑龙江省")||province.equals("黑龙江")){
			city="哈尔滨市";cityCode="2610";
		}else if(province.equals("吉林省")||province.equals("吉林")){
			city="长春市";cityCode="2410";
		}else if(province.equals("江苏省")||province.equals("江苏")){
			city="南京";cityCode="3010";
		}else if(province.equals("浙江省")||province.equals("浙江")){
			city="杭州市";cityCode="3310";
		}else if(province.equals("安徽省")||province.equals("安徽")){
			city="合肥市";cityCode="3610";
		}else if(province.equals("福建省")||province.equals("福建")){
			city="福州市";cityCode="3910";
		}else if(province.equals("江西省")||province.equals("江西")){
			city="南昌";cityCode="4210";
		}else if(province.equals("山东省")||province.equals("山东")){
			city="济南市";cityCode="4510";
		}else if(province.equals("河南省")||province.equals("河南")){
			city="郑州市";cityCode="4910";
		}else if(province.equals("湖北省")||province.equals("湖北")){
			city="武汉市";cityCode="5210";
		}else if(province.equals("湖南省")||province.equals("湖南")){
			city="长沙";cityCode="5510";
		}else if(province.equals("广东省")||province.equals("广东")){
			city="广州";cityCode="5810";
		}else if(province.equals("广西壮族自治区")||province.equals("广西")){
			city="南宁市";cityCode="6110";
		}else if(province.equals("海南省")||province.equals("海南")){
			city="海口";cityCode="6410";
		}else if(province.equals("四川省")||province.equals("四川")){
			city="成都市";cityCode="6510";
		}else if(province.equals("贵州省")||province.equals("贵州")){
			city="贵阳市";cityCode="7510";
		}else if(province.equals("云南省")||province.equals("云南")){
			city="昆明市";cityCode="7310";
		}else if(province.equals("西藏自治区")||province.equals("西藏")){
			city="拉萨市";cityCode="7700";
		}else if(province.equals("陕西省")||province.equals("陕西")){
			city="西安市";cityCode="7910";
		}else if(province.equals("甘肃省")||province.equals("甘肃")){
			city="兰州市";cityCode="8210";
		}else if(province.equals("宁夏回族自治区")||province.equals("宁夏")){
			city="银川市";cityCode="8710";
		}else if(province.equals("青海省")||province.equals("青海")){
			city="西宁市";cityCode="8510";
		}else if(province.equals("新疆维吾尔自治区")||province.equals("新疆")){
			city="乌鲁木齐";cityCode="8810";
		}else{
			return null;
		}
		String[] cc={city,cityCode};
		return cc;
	}
	
	public static String checkBankCode(String bankName){
		String bankCode="";
		if("中国工商银行".equals(bankName)){
			bankCode="0102";
		}else if("中国农业银行".equals(bankName)){
			bankCode="0103";
		}else if("中国银行".equals(bankName)){
			bankCode="0104";
		}else if("中国建设银行".equals(bankName)){
			bankCode="0105";
		}else if("交通银行".equals(bankName)){
			bankCode="0301";
		}else if("中信银行".equals(bankName)){
			bankCode="0302";
		}else if("中国光大银行".equals(bankName)){
			bankCode="0303";
		}else if("华夏银行".equals(bankName)){
			bankCode="0304";
		}else if("中国民生银行".equals(bankName)){
			bankCode="0305";
		}else if("广东发展银行".equals(bankName)){
			bankCode="0306";
		}else if("平安银行股份有限公司".equals(bankName)){
			bankCode="0307";
		}else if("招商银行".equals(bankName)){
			bankCode="0308";
		}else if("兴业银行".equals(bankName)){
			bankCode="0309";
		}else if("上海浦东发展银行".equals(bankName)){
			bankCode="0310";
		}else if("中国邮政储蓄银行股份有限公司".equals(bankName)){
			bankCode="0403";
		}
		return bankCode;
	}
	
	/**
	 * 
	 * 判断投资当天是否是该用户的生日
	 * */
	public static boolean isBirth(String userCardId){
		boolean flag=false;
		String cardNo="";
		try {
			if(null==userCardId ||"".equals(userCardId)) {
				return flag;
			}
			cardNo=CommonUtil.decryptUserCardId(userCardId);
			if(cardNo.length()==0){
				return flag;
			}
			String cardnSub=cardNo.substring(10,14);
			String nowDay=DateUtil.getNowDate().substring(4,8);
			if(cardnSub.equals(nowDay)){
				flag=true;
			}
			
		} catch (Exception e) {
			return false;
		}
		return flag;
	}
	/**
	 * 生成8位随机兑换码 ws
	 * */
	public static String getRedeemCode(){
		StringBuffer sb = new StringBuffer();
		for(int i = 0; i < 5; i++) {  
            sb.append((char) (Math.random() * 26 + 'A'));
        }
		Random rm=new Random();
		sb.append(rm.nextInt(10));
		sb.append(rm.nextInt(10));
		sb.append((char) (Math.random() * 26 + 'A'));
		return sb.toString();
	}
	
	/**
	 * 获取验证码
	 * */
	public static String getVerifiCode() {
		String val = "";
		Random random = new Random();
		for (int i = 0; i <= 3; i++) {
			String strOrNum = random.nextInt(2) % 2 == 0 ? "str" : "num";
			// 随机输出是字母还是数字
			if ("str".equalsIgnoreCase(strOrNum)) {
				// 随机输出是大写字母还是小写字母
				int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
				val += (char) (random.nextInt(26) + temp);
			} else if ("num".equalsIgnoreCase(strOrNum)) {
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;

	}
	/**
	 * 获取验证码  随机码
	 * n 获取位数
	 * */
	public static String getVerifiCode(int n) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < n; i++) {
			String strOrNum = random.nextInt(2) % 2 == 0 ? "str" : "num";
			// 随机输出是字母还是数字
			if ("str".equalsIgnoreCase(strOrNum)) {
				// 随机输出是大写字母还是小写字母
				int temp = random.nextInt(2) % 2 == 0 ? 65 : 97;
				val += (char) (random.nextInt(26) + temp);
			} else if ("num".equalsIgnoreCase(strOrNum)) {
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}
	/**
	 * 验证图形验证码
	 * 
	 * @param key
	 * @param msgMac
	 * @return
	 */
	public static boolean validateCode(String key, String msgMac) {
		Object obj = Memcached.get(key);
		if (AdminConfig.isDevMode == true) {
			return true;
		}
		if (obj != null && String.valueOf(obj).equalsIgnoreCase(msgMac)) {
			// 删除图形验证码
			Memcached.delete(key);
			return true;
		} else {
			return false;
		}
	}
}