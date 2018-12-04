package com.dutiantech.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.dutiantech.CACHED;
import com.dutiantech.config.AdminConfig;

public class LoggerUtil {
	
	/**
	 * 初始化Logger对象属性
	 * @param fileName		日志文件名
	 * @param logger		logger对象
	 */
	public static void initLogger(String fileName,Logger logger){
		
		String logPath = (String) CACHED.get("S1.taskLogsPath");
		String lsp = System.getProperty("file.separator");
		if(StringUtil.isBlank(logPath)){
			logPath = "/data/taskLogs";
		}
		
		if( AdminConfig.isDevMode == true ){
			logPath = "D://log";
		}
		
		logPath = logPath + lsp + fileName;
		File file = new File(logPath.toString());
		if (file.exists() == false )
			file.mkdir();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH");
		logPath = logPath + lsp + fileName + sdf.format(new Date())+".log";
		//FileHander也会jetty默认日志输出，这里ConsoleHandler就不要了
//		ConsoleHandler consoleHandler =new ConsoleHandler(); 
//		consoleHandler.setLevel(Level.OFF); 
//		logger.addHandler(consoleHandler);
		FileHandler fileHandler;
		try {
			fileHandler = new FileHandler(logPath.toString(),true);
			fileHandler.setLevel(Level.ALL);
			fileHandler.setFormatter(new LogFormater());
			logger.addHandler(fileHandler);
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

}

class LogFormater extends Formatter{
	@Override
	public String format(LogRecord record) {
		StringBuffer logInfo = new StringBuffer("");
		logInfo.append(record.getSourceClassName());
		logInfo.append(System.getProperty("line.separator"));
		long millis = record.getMillis();
		Date x = new Date(Long.valueOf(millis));
		String dateTime = DateUtil.getStrFromDate(x, "yyyy-MM-dd HH:mm:ss");
		logInfo.append(dateTime);
		logInfo.append("[").append(record.getLevel()).append("]:").append(record.getMessage());
		logInfo.append(System.getProperty("line.separator"));
		
		return logInfo.toString();
	} 
}
