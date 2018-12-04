package com.dutiantech.task;
import java.util.Calendar;
import java.util.Date;

import com.dutiantech.Log;
import com.dutiantech.util.DateUtil;
import com.dutiantech.util.HttpRequestUtil;

/**
 * 	定时服务
 * 		默认3000ms检查一次
 * @author Five
 *
 */
public class DayService extends TaskService {
	
	private String callurl ;
	private long millis = 3000 ;
	private Calendar taskTime = Calendar.getInstance();
	private int taskHour = 0 ;
	private int taskMin = 0 ;
	private int lastRunDay = -1 ;
	
	/**
	 * 
	 * @param callurl	呼叫服务的url
	 * @param dateStr	日批次时间，24小时制	HH:mm，作用时间到小时分钟
	 * @param getName() 服务名称
	 */
	public DayService(String callurl , String dateStr , String serviceName ){
		super( serviceName ) ;
		this.callurl = callurl ;
		//this.getName() = getName() ;
		Date tmpDate = DateUtil.getDateFromString(dateStr, "HH:mm") ;
		taskTime.setTime(tmpDate);
		taskHour = taskTime.get( Calendar.HOUR_OF_DAY ) ;
		taskMin = taskTime.get( Calendar.MINUTE ) ;
	}
	
	private void work(){
		String result = HttpRequestUtil.sendGet(callurl, getParamToString() ) ;
		Log.info("【" + getName() + "】执行结果：" + result );
	}
	
	public void run(){
		Log.info("开启服务：" + getName() );
		int count = 0;
		//loop...
		while( isRun ){
			millis = 3*1000 ;	//set default time 
			//兼容异常情况
			try{
				Calendar nowTime = Calendar.getInstance() ;
				int nowHour = nowTime.get( Calendar.HOUR_OF_DAY ) ;
				int nowMin = nowTime.get( Calendar.MINUTE ) ;
				if( lastRunDay != nowTime.get( Calendar.DAY_OF_YEAR )){	//run once
					//check hour
					if( nowHour == taskHour ){
						if( nowMin == taskMin ){
							lastRunDay = nowTime.get( Calendar.DAY_OF_YEAR ) ;
							Log.info("执行日切服务：" + getName() );
							work() ;
						}else{
						}
					}else{
						//减少CPU工作频率
						millis = 60*1000 ;
					}
				}
			}catch(Exception e){
				Log.info( getName() + " 服务出现异常：" + e.getLocalizedMessage() );
			}finally{
			}

			try {
				count ++ ;
				Log.info("【" + getName() + "】 运行次数：" + count +"， 无任务，休眠：" + millis/1000 + "秒！");
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
		}
	}
	
}
