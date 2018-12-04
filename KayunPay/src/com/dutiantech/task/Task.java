package com.dutiantech.task;

import java.util.HashMap;

public class Task extends HashMap<String , TaskService >{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6024404568312459686L;
	
	public static int maxTask = 100 ;	//default 
	public static Task TASK = null ;
	
	static {
		TASK = new Task() ;
	}
	
	private static boolean isOverMax(){
		return TASK.size() >= maxTask ;
	}
	
	public static void newTask( TaskService task){
		
		if( isOverMax() ){
			throw new RuntimeException("已超过最大缓存限制!");
		}
		
		String taskName = task.getName() ;
		clearTask( taskName );
		TASK.put( taskName , task ) ;
		task.start(); 
	}
	
	public static TaskService getName(String taskName){
		return TASK.get(taskName) ;
	}
	
	public static void clearTask(String taskName){
		TaskService service = TASK.get( taskName );
		if( service != null ){
			TASK.remove( taskName ) ;
			service.stopRun() ;
		} 
	}
	
}
