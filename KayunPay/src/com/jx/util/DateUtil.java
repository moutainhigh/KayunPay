package com.jx.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * 
 * @author Administrator
 *
 */
public class DateUtil {
	/**
	 * 获取年月日
	 * */
	public static String getDate() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		return sf.format(new Date());
	}

	/**
	 * 获取时间
	 * */
	public static String getTime() {
		SimpleDateFormat sf = new SimpleDateFormat("HHmmss");
		return sf.format(new Date());
	}
    
    /**
     * 获取随机数字
     * @param len
     * @return
     */
	public static String getRandomStr(int len){
    	double d1 = Math.pow(10, len-1);
    	double d2 = d1*Math.random()/2;
    	long n = new Double(d1+d2).longValue();
    	return String.valueOf(n);
    	
    }
    
	public static String getDateAndTime() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sf.format(new Date());
	}

	/**
	 * 
	 * @param startDate
	 *            yyyyMMdd
	 * @param duration
	 *            Integer
	 * @return
	 */
	public static String getDate(String startDate, Integer duration) {

		Calendar cal = Calendar.getInstance();

		Integer year = Integer.parseInt(startDate.substring(0, 4));
		Integer month = Integer.parseInt(startDate.substring(4, 6));
		Integer date = Integer.parseInt(startDate.substring(6, 8));

		cal.set(year, month-1, date);

		cal.add(Calendar.DATE,  duration);
		return String.valueOf(cal.get(Calendar.YEAR)) + format(cal.get(Calendar.MONTH)+1,2) + format(cal.get(Calendar.DATE),2);

	}

	private static String format(int i, int len) {
		StringBuilder sb = new StringBuilder("");
		String perfix = "0";
		String srcStr = String.valueOf(i);
		if (srcStr.length() < len) {
			for (int k = len - srcStr.length(); k < len; k++) {
				sb.append(perfix);
			}
		}
		sb.append(srcStr);
		return sb.toString();
	}

	/**
	 * @计算历史时间距今小时数
	 * @author zhaoyong
	 * @since 2015/09/01
	 * @param (String)date 历史时间
	 * @return (long) 距今小时数 。异常-1。
	 * 
	 * */
	public static long fromNowHours(String date) {
		String dateStart = date;
		long hours = -1;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = null;
		Date d2 = new Date();
		try {
			d1 = format.parse(dateStart);
			// 毫秒ms
			long diff = d2.getTime() - d1.getTime();
			//long diffSeconds = diff / 1000 % 60;
			//long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);

			hours = diffDays * 24 + diffHours;
		} catch (Exception e) {
			System.out.println("Date formate error.");
		}
		return hours;
	}

	/**
	 * @计算历史时间距今分钟数
	 * @author zhaoyong
	 * @since 2015/11/27
	 * @param (String)date 历史时间
	 * @return (long) 距今小时数 。异常-1。
	 * 
	 * */
	public static long fromNowMinutes(String date) {
		String dateStart = date;
		long minutes = -1;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = null;
		Date d2 = new Date();
		try {
			d1 = format.parse(dateStart);
			// 毫秒ms
			long diff = d2.getTime() - d1.getTime();
			//long diffSeconds = diff / 1000 % 60;
			long diffMinutes = diff / (60 * 1000) % 60;
			long diffHours = diff / (60 * 60 * 1000) % 24;
			long diffDays = diff / (24 * 60 * 60 * 1000);

			minutes = diffDays * 24 + diffHours * 60 + diffMinutes;
		} catch (Exception e) {
			System.out.println("Date formate error.");
		}
		return minutes;
	}

	/**
	 * 获取延迟后的时间
	 * 
	 * @param date
	 *            需要处理的时间
	 * @param timeTypeEnum
	 *            时间类型
	 * @param delta
	 *            需要顺延的时间变量
	 * @return Date
	 * */
	public static Date getContinueDate(Date date, TimeTypeEnum timeTypeEnum, int delta) {
		if (null == date) {
			return null;
		}

		// 向后顺延相应的天数
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		if (timeTypeEnum == TimeTypeEnum.DAY) {
			calendar.add(Calendar.DATE, delta); // 把日期往后增加N天， 正数往后推,负数往前移动
		} else if (timeTypeEnum == TimeTypeEnum.HOUR) {
			calendar.add(Calendar.HOUR, delta);// 把日期往后增加N小时， 正数往后推,负数往前移动
		} else if (timeTypeEnum == TimeTypeEnum.MINUTE) {
			calendar.add(Calendar.MINUTE, delta);// 把日期往后增加N分钟， 正数往后推,负数往前移动
		} else if (timeTypeEnum == TimeTypeEnum.MONTH) {
			calendar.add(Calendar.MONTH, delta); // 把日期往后增加N个月， 正数往后推,负数往前移动
		} else if (timeTypeEnum == TimeTypeEnum.YEAR) {
			calendar.add(Calendar.YEAR, delta); // 把日期往后增加N年， 正数往后推,负数往前移动
		}

		return calendar.getTime();
	}

	
	/**
	 * 比较两个时间前后
	 * */
	public static int compareDate(String date1, String date2) {

		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		try {
			Date dt1 = df.parse(date1);
			Date dt2 = df.parse(date2);
			if (dt1.getTime() > dt2.getTime()) {
//				System.out.println("dt1 在dt2后");
				return 1;
			} else if (dt1.getTime() < dt2.getTime()) {
//				System.out.println("dt1在dt2前");
				return -1;
			} else {
				return 0;
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return 0;
	}

	/**
	 * 时间类型枚举类
	 * 
	 * */
	public static enum TimeTypeEnum {

		YEAR(1, "年"), MONTH(2, "月"), DAY(3, "日"), HOUR(4, "小时"), MINUTE(5, "分"), SECOND(6, "秒");

		private int timeType;
		private String desc;

		private TimeTypeEnum(int timeType, String desc) {
			this.timeType = timeType;
			this.desc = desc;
		}

		public int getTimeType() {
			return timeType;
		}

		public void setTimeType(int timeType) {
			this.timeType = timeType;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

	}

	public static void main(String[] args) {
		System.out.println("==============================");
		System.out.println("距今hours:" + DateUtil.fromNowHours("2015-11-27 3:50:00"));
		System.out.println("距今minutes:" + DateUtil.fromNowMinutes("2015-11-27 10:50:00"));

		System.out.println("==============================");
		SimpleDateFormat sf = new SimpleDateFormat("YYYY-MM-DD HH:mm:ss");
		System.out.println("当前时间:" + sf.format(new Date()));
		System.out.println("10分钟之后时间:" + sf.format(DateUtil.getContinueDate(new Date(), TimeTypeEnum.MINUTE, 10)));
	}
}
