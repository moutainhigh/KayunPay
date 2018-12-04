package com.dutiantech.util;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import com.dutiantech.exception.BaseBizRunTimeException;

public class DateUtil {
	
	public static void main(String[] args) throws ParseException{
		
	}
	
	public static String getLastDate(String ftString){
		SimpleDateFormat dft = new SimpleDateFormat( ftString );
		Date beginDate = new Date();
		Calendar date = Calendar.getInstance();
		date.setTime(beginDate);
		date.set(Calendar.DATE, date.get(Calendar.DATE) - 1);
		return dft.format( date.getTime() ) ;
	}
	
	/**
	 * 计算当前时间与第二天00:00:00的时间差
	 * */
	public static long getTodayToTomTime(){
		Calendar curDate = Calendar.getInstance();
		Calendar tommorowDate = new GregorianCalendar(curDate
		.get(Calendar.YEAR), curDate.get(Calendar.MONTH),
		curDate.get(Calendar.DATE) + 1, 0, 0, 0);
		long times = tommorowDate.getTimeInMillis() - curDate
				.getTimeInMillis();
		
		return times;
	}
	 
	/**
	 * 判断日期str1与日期str2的大小
	 * @param fmStr	时间格式
	 * @param str1	
	 * @param str2
	 * @return	
	 * 		-1 -> str1小于str2;
	 * 		0 -> str1等于str2;
	 * 		1 -> str1大于str2;
	 * 		2 -> 日期格式错误
	 */
	public static int compareDateByStr(String fmStr , String str1 , String str2) {
		try{
			SimpleDateFormat format = new SimpleDateFormat(fmStr);
			Date d1 = format.parse( str1 );
			Date d2 = format.parse( str2 );
			return d1.compareTo(d2) ;
		}catch(Exception e){
			throw new BaseBizRunTimeException("E9", "日期解析异常", null  ) ;
		}
	}
	
	@SuppressWarnings("unused")
	public static int getCurWeek(Date begin,Date end){
		SimpleDateFormat fmt=new SimpleDateFormat("yyyy-MM-dd");
		try {
			Calendar cal=Calendar.getInstance();
			Calendar cal2=Calendar.getInstance();
			cal2.setTime(end);
			cal.setTime(begin);
			int week=cal2.get(Calendar.WEEK_OF_YEAR);
			int week_init=cal.get(Calendar.WEEK_OF_YEAR);
			if(cal2.get(Calendar.YEAR)-cal.get(Calendar.YEAR)>0){
				Date yearBegin=fmt.parse(cal2.get(Calendar.YEAR)+"-01-01");
				int weekend=cal2.get(Calendar.DAY_OF_WEEK);
				cal2.setTime(yearBegin);
				cal2.add(Calendar.DATE, -7);
				week=cal2.get(Calendar.WEEK_OF_YEAR)+week;
			}
			return week-week_init;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static Date getDateFromString(String dateStr) {
		return getDateFromString(dateStr, null);
	}
	
	public static Date getDateFromString(String dateStr, String pattern) {
		if (pattern == null || "".equals(pattern)) {
			pattern = "yyyy-MM-dd HH:mm:ss";
		}
		SimpleDateFormat format = new SimpleDateFormat(pattern);
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static String getStrFromNowDate(String pattern){
		return getStrFromDate(new Date(), pattern) ;
	}
	
	public static String getStrFromDate(Date date, String pattern) {
	     java.text.DateFormat df = new java.text.SimpleDateFormat(pattern); 
	     String s = df.format(date); 
	     return s;
	}
	
	public static String getLongStrFromDate(Date date) {
		return getStrFromDate(date, "yyyy-MM-dd HH:mm:ss");
	}
	
	public static String getDateStrFromDateTime(Date date) {
		return getStrFromDate(date, "yyyyMMddHHmmss");
	}
	
	public static String getDateStrFromDate(Date date) {
		return getStrFromDate(date, "yyyy-MM-dd");
	}
	public static String getWeekOfDate(Date dt) {
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        return weekDays[w];
    }
	
	/**
	 * ��ʾ����
	 * 
	 * @param date
	 * @return
	 */
	public static String showDate(Date date) {
		return parseDateTime(date, "yyyy-MM-dd");
	}

	/**
	 * ��ʾ���ں�ʱ��
	 * 
	 * @param date
	 * @return
	 */
	public static String showDateTime(Date date) {
		return parseDateTime(date, "yyyyMMddHHmmss");
	}

	/**
	 * ��ʽ������
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 */
	public static String parseDateTime(Date date, String pattern) {
		if (date == null)
			return "";
		return new SimpleDateFormat(pattern).format(date);
	}

	public static String getNowDate(){
		Date date = new Date();
		return parseDateTime(date , "yyyyMMdd");
		
	}
	
	public static String getNowDateYandM(){
		Date date = new Date();
		return parseDateTime(date , "yyyy-MM-");
		
	}
	
	public static String getNowTime(){
		Date date = new Date();
		return parseDateTime(date , "HHmmss");
		
	}

	public static String getNowDateTime(long exValue) {
		Date date = new Date();
		date = new Date( date.getTime() + exValue );
		return showDateTime(date);
	}
	
	public static String getNowDateTime() {
		Date date = new Date();
		return showDateTime(date);
	}

	public static Timestamp getTimestamp() {
		return new Timestamp(new Date().getTime());
	}

	public static java.sql.Date getJavaSqlDate() {
		return new java.sql.Date(new Date().getTime());
	}

	/**
	 * 根据身份证号获取生日
	 * @param identityCard
	 * @return
	 */
	public static java.sql.Date getBirthFromIdentityCard(String identityCard) {
		if (identityCard == null) {
			return null;
		}
		int length = identityCard.length();

		if (length == 15) {
			identityCard = identityCard.substring(0, 6) + "19"
					+ identityCard.substring(6) + "x";

		}

		if (identityCard.length() == 18) {
			return java.sql.Date.valueOf(identityCard.substring(6, 10) + "-"
					+ identityCard.substring(10, 12) + "-"
					+ identityCard.substring(12, 14));
		} else {
			return null;
		}

	}

	/**
	 * 通过给定时间得到这段时间星期1到星期天之间具体日期
	 * 
	 * @param args
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static Date[] getWeekBetween(Date date) throws ParseException {
		long time = date.getTime();
		int day = date.getDay();
		long startTime = 0, endTime = 0;
		int start = 0, end = 0;
		Date startDate = null, endDate = null;
		Date[] dates = new Date[2];
		switch (day) {
		case 0:
			start -= 6;
			end = 0;
			break;
		case 1:
			start = 0;
			end += 6;
			break;
		case 2:
			start -= 1;
			end += 5;
			break;
		case 3:
			start -= 2;
			end += 4;
			break;
		case 4:
			start -= 3;
			end += 3;
			break;
		case 5:
			start -= 4;
			end += 2;
			break;
		case 6:
			start -= 5;
			end += 1;
			break;
		default:
			break;
		}
		startTime = time / 1000 + start * (60 * 60 * 24);
		endTime = time / 1000 + end * (60 * 60 * 24);
		startDate = new Date(startTime * 1000);
		endDate = new Date(endTime * 1000);
		dates[0] = startDate;
		dates[1] = endDate;
		return dates;
	}

	
	/**
	 * 根据给定时间得到这个月头和尾的具体时间
	 * @param args
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static Date[] getMonthBetween(Date date) throws ParseException {
		
		Date firstDate=null,lastDate=null;
		Date[] dates = new Date[2];
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.YEAR,date.getYear()+1900);
		calendar.set(Calendar.MONTH,date.getMonth());
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.SECOND,0);
		calendar.set(Calendar.MINUTE, 0);
		int maxDay=calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		calendar.set(Calendar.DATE,1);		
		firstDate=calendar.getTime();
		calendar.set(Calendar.DATE,maxDay--);
		lastDate=calendar.getTime();
		dates[0]=firstDate;	
		dates[1]=lastDate;	
		return dates;
	}
	
	/**
	 * 根据给定时间得到0点到24点时间
	 * @param args
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static Date[] getDayBetween(Date date) throws ParseException {
		
		Date firstDate=null,lastDate=null;
		Date[] dates = new Date[2];
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.YEAR,date.getYear()+1900);
		calendar.set(Calendar.MONTH,date.getMonth());
		calendar.set(Calendar.DATE, date.getDate());
		calendar.set(Calendar.HOUR,-12);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		firstDate=calendar.getTime();
		calendar.set(Calendar.HOUR, 24);
		lastDate=calendar.getTime();
		dates[0]=firstDate;	
		dates[1]=lastDate;	
		return dates;
	}
	
	/**
	 * 得出一个月具体时间
	 * @param args
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static List<Date> getMonthDay(Date date)
	{
		List<Date> list=new ArrayList<Date>();
		Calendar calendar=Calendar.getInstance();
		calendar.set(Calendar.YEAR,date.getYear()+1900);
		calendar.set(Calendar.MONTH,date.getMonth());
		int maxDay=calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
		for(int i=0;i<maxDay;i++)
		{
			calendar.set(Calendar.DATE, i+1);
			list.add(calendar.getTime());
		}	
		return list;
	}
	
	
	/**
	 * 通过年月日比较2日期是否相等
	 * @return
	 */
	public static boolean compareEqualByYMD(Date first,Date last)
	{
		return first.compareTo(last)==0;
	}
	
	/**
	 * 将数字星期转化为中文
	 * 
	 * @param args
	 * @throws ParseException
	 */
	@SuppressWarnings("deprecation")
	public static String getDay2Chinese(Date date) throws ParseException {
		int day = date.getDay();
		
		String rtn = "";
		
		switch (day) {
		case 0:
			rtn = "日";
			break;
		case 1:
			rtn = "一";
			break;
		case 2:
			rtn = "二";
			break;
		case 3:
			rtn = "三";
			break;
		case 4:
			rtn = "四";
			break;
		case 5:
			rtn = "五";
			break;
		case 6:
			rtn = "六";
			break;
		default:
			break;
		}
		
		return rtn;
	}
	
	/**
	 * 获取某天添加N天后的日期
	 * @param date		给定时间（yyyyMMdd）
	 * @param count		需要累加天数
	 * @return
	 * @author shiqingsong 
	 */
	public static String addDay(String date , Integer count){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date returnDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
			cal.set(Calendar.DATE, cal.get(Calendar.DATE) + count);
			returnDate = cal.getTime();
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return sdf.format(returnDate);
	}
	
	/**
	 * 获取某天减去N天后的日期
	 * @param date		给定时间（yyyyMMdd）
	 * @param count		需要累加天数
	 * @return
	 * @author shiqingsong 
	 */
	public static String subDay(String date , Integer count){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date returnDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
			cal.set(Calendar.DATE, cal.get(Calendar.DATE) - count);
			returnDate = cal.getTime();
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return sdf.format(returnDate);
	}
	
	/**
	 * 获取某天减去N天后的日期
	 * @param date		给定时间（yyyyMMdd）
	 * @param count		需要减去天数
	 * @return
	 * @author shiqingsong 
	 */
	public static String delDay(String date , Integer count){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date returnDate = null;
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(date));
			cal.set(Calendar.DATE, cal.get(Calendar.DATE) - count);
			returnDate = cal.getTime();
		} catch (ParseException e) {
			//e.printStackTrace();
		}
		return sdf.format(returnDate);
	}
	
	/**
	 * 判断是否是时间内
	 * @param sourceTime	时间区间 eg: "08:00-12:00" 8至12点
	 * @param curTime	当前时间
	 * @return
	 */
	public static boolean isInTime(String sourceTime, String curTime) {
	    if (sourceTime == null || !sourceTime.contains("-") || !sourceTime.contains(":")) {
	        throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
	    }
	    if (curTime == null || !curTime.contains(":")) {
	        throw new IllegalArgumentException("Illegal Argument arg:" + curTime);
	    }
	    String[] args = sourceTime.split("-");
	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
	    try {
	        long now = sdf.parse(curTime).getTime();
	        long start = sdf.parse(args[0]).getTime();
	        long end = sdf.parse(args[1]).getTime();
	        if (args[1].equals("00:00")) {
	            args[1] = "24:00";
	        }
	        if (end < start) {
	            if (now >= end && now < start) {
	                return false;
	            } else {
	                return true;
	            }
	        } 
	        else {
	            if (now >= start && now < end) {
	                return true;
	            } else {
	                return false;
	            }
	        }
	    } catch (ParseException e) {
	        e.printStackTrace();
	        throw new IllegalArgumentException("Illegal Argument arg:" + sourceTime);
	    }

	}
	/**
	 * yyyyMMdd 改为yyyy-MM-dd
	 * */
	public static String chenageDay(String date){
		String dateString = "";
		dateString+=date.substring(0, 4)+"-";
		dateString+=date.substring(4, 6)+"-";
		dateString+=date.substring(6, 8);
		return dateString;
	}
	/**
	 * 获取某个月的天数
	 * */
	public static int getDaysByYearMonth(String date) {  
		int year=Integer.parseInt(date.substring(0,4));
		int month=Integer.parseInt(date.substring(4,6));
		Calendar a = Calendar.getInstance();  
		a.set(Calendar.YEAR, year);  
		a.set(Calendar.MONTH, month - 1);  
		a.set(Calendar.DATE, 1);  
		a.roll(Calendar.DATE, -1);  
		int maxDate = a.get(Calendar.DATE);  
		return maxDate;  
	} 

	/**
	 * 时间增加月份
	 * @param args
	 * @throws ParseException 
	 * @throws Exception
	 */
	public static String addMonth(String date,int month){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		Date now;
		Calendar calendar = Calendar.getInstance();
		try {
			now = sdf.parse(date);
			calendar.setTime(now);
			calendar.add(Calendar.MONTH, month);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return sdf.format(calendar.getTime());
		}
	
	/**
	 * 判断时间是否在给定时间范围内(不包含临界时间)   20171101  WCF
	 * @param strInputTime	判定时间(yyyyMMddHHmmss)
	 * @param strBeginTime	开始时间(yyyyMMddHHmmss)
	 * @param strEndTime	结束时间(yyyyMMddHHmmss)
	 * @return
	 */
	public static boolean isInDateTime(String strInputTime, String strBeginTime, String strEndTime) {
		Date nowTime = DateUtil.getDateFromString(strInputTime, "yyyyMMddHHmmss");
		Date beginTime = DateUtil.getDateFromString(strBeginTime, "yyyyMMddHHmmss");
		Date endTime = DateUtil.getDateFromString(strEndTime, "yyyyMMddHHmmss");
		
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(beginTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }
	
	/**
	 * 计算两个日期的时间差
	 * @param date1	开始时间
	 * @param date2	结束时间
	 * @param pattern	时间格式
	 * @return
	 */
	public static int differentDaysByMillisecond(String date1, String date2, String pattern) {
		Date d1 = DateUtil.getDateFromString(date1, pattern);
		Date d2 = DateUtil.getDateFromString(date2, pattern);
        int days = (int) ((d2.getTime() - d1.getTime()) / (1000*3600*24));
        return days;
    }
}




