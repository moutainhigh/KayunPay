package com.dutiantech.task;
import com.dutiantech.Log;
import com.dutiantech.util.HttpRequestUtil;

/**
 * 	被动呼叫服务，通过定制任务的方式实现响应式服务触发，如果是分步式部署的服务器，建议控制服务启动的数量。
 * 		最后是只有一个service
 * 	
 * @author Five
 *
 */
public class CallService extends TaskService {
	
	private String callurl ;
	private long millis = 100 ;
	
	/**
	 * @param callurl	呼叫服务的url
	 * @param timeout	呼叫服务间隔时间，单位ms，建议100ms
	 */
	public CallService(String callurl , long millis , String serviceName ){
		super( serviceName ) ;
		this.callurl = callurl ;
		this.millis = millis ;
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
			Log.info("【"+ getName() + "】 运行次数：" + count );
			//兼容异常情况
			try{
				work() ;
			}catch(Exception e){
				Log.info( getName() + " 服务出现异常：" + e.getLocalizedMessage() );
			}finally{
			}

			try {
				count ++ ;
				Thread.sleep(millis);
			} catch (InterruptedException e) {
			}
		}
	}
	
}
