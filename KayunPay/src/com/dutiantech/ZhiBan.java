package com.dutiantech;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ZhiBan {

	
	public static void main(String[] args) {
		String[] players = new String[]{"吴成","陈康","郑贤","青松","陈长义","张衡"};
		String[] mobiles = new String[]{"13469992252","18062593920","18717129832","15871727350","15527609321","18627095267"};
		DateFormat df = new SimpleDateFormat("MM月dd日") ;
		int month = 4 ;
		
		//now date 
		Calendar cal = Calendar.getInstance() ;
		cal.set( Calendar.MONTH , month -1 );		//设置月
		
		int maxMonthDay = cal.getActualMaximum( Calendar.DAY_OF_MONTH ) ;
		
		int c1 = 0 ;	//平时计数
		int c2 = 0 ;	//周末计数
//		int c3 = 0 ;	//累计上午休息2小时次数
//		int c4 = 0 ;	//累计休息1天次数
		
		int curDay = 1 ; 
		while( curDay <= maxMonthDay ){
			cal.set( Calendar.DAY_OF_MONTH , curDay );	//设置日期
			int dayOfWeek = cal.get( Calendar.DAY_OF_WEEK  ) ;
			dayOfWeek = dayOfWeek - 1 ;
			if( dayOfWeek == 0 )
				dayOfWeek = 7 ;
			String dw = "";
			switch (dayOfWeek) {
			case 1:
				dw = "星期一";
				break;
			case 2:
				dw = "星期二";
				break;
			case 3:
				dw = "星期三";
				break;
			case 4:
				dw = "星期四";
				break;
			case 5:
				dw = "星期五";
				break;
			case 6:
				dw = "星期六";
				break;
			case 7:
				dw = "星期天";
				break;
			}
			String nowDate = df.format( cal.getTime() ) ;
			if( dayOfWeek < 6 ){
				//周一到周五
				String play = players[c1] ;
				String mobile = mobiles[c1] ;
				System.out.println( String.format("[%s][%s][%s]-值晚班\t\t联系电话:[%s]", nowDate , dw , play ,mobile));
				c1 ++ ;
			}else{
				//周六,周日
				String play = players[c2] ;
				String mobile = mobiles[c2] ;
				System.err.println( String.format("[%s][%s][%s]-值全天班\t\t联系电话:[%s]", nowDate , dw , play  ,mobile));
				c2 ++ ;
			}
			
			if( (c1) == players.length ){
				c1 = 0 ;
			}
			
			if( (c2) == players.length ){
				c2 = 0 ;
			}
			
			curDay ++ ;
		}
		
//		cal.set( Calendar.DAY_OF_MONTH , 16 );
		System.out.println(String.format("末班索引：[c1=%d][c2=%d]", c1 , c2 ));
		
	}
}
