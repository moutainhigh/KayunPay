package com.dutiantech.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * 
 * @author Administrator
 *
 */
public class FtpUtil {

	private static Session session = null;
	private static ChannelSftp channel = null;
	private static Logger logger = Logger.getLogger(FtpUtil.class);
	
	public static ChannelSftp getConnect(String ftpHost,String ftpUserName,int ftpPort,String ftpPassword){
		/*String ftpHost = "sftp.credit2go.cn";//服务器地址
		String ftpUserName = "yironghengxin";//用户名
		int ftpPort = 5132;//端口号
		String ftpPassword = "vLUEC8CKa0EHi";//密码
*/
		
		//创建JSCH对象
		JSch jsch = new JSch();
		try{
			logger.info("sftp [ ftpHost = " + ftpHost + " ftpPort = "+ ftpPort + " ftpUserName = "+ftpUserName+"  ftpPassword = "+ftpPassword+" ]");
			session = jsch.getSession(ftpUserName,ftpHost,ftpPort);
			logger.info("Session created.");
			session.setPassword(ftpPassword);
//			session.setConfig("userauth.gssapi-with-mic", "no");
			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("UseDNS","no");
			session.connect();
			logger.debug("Session connected");
			logger.debug("Opening SFTP Channel");
			channel = (ChannelSftp) session.openChannel("sftp");//打开SFTP通道
			channel.connect();
			logger.debug("Connected successfully to ftpHost = " + ftpHost + ",as ftpUserName = "
	                 + ftpUserName + ", returning: " + channel);
			
		}catch (JSchException e){
			e.printStackTrace();
			logger.error("sftp getConnect error : " + e);
		}
		return channel;
		
	}
	
	public static void closeChannel() throws Exception{
		try{
			
				if(channel != null){
					channel.disconnect();
					
				}
				if(session != null){
					session.disconnect();
				}
			
		}catch (Exception e){
			logger.error("close sftp error",e);
			throw new Exception("close ftp error");
		}
	}
	
	
	public boolean downloadFile (String remoteFile,String remotePath,String localFile) throws Exception{
		logger.info("sftp download File remotePath :"+remotePath+File.separator+remoteFile+" to localPath : "+localFile+" !");
		OutputStream output = null;
		File file = null;
		try{
			file = new File(localFile);
			if(!checkFileExist(localFile)){
				file.createNewFile();
			}else{
				logger.error("下载失败，"+localFile  + "文件已经存在");
				return false;
			}
			output = new FileOutputStream(file);
			channel.cd(remotePath);
			
			channel.get(remoteFile,output);
			
		}catch(Exception e){
			logger.error("Download file error",e);
			throw new Exception("Download file error");
		}
		finally{
			if(output != null){
				try{
					output.close();
				}catch(IOException e){
					throw new Exception("close stream error");
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public Vector listFiles(String remotePath) throws Exception{
		Vector vector = null;
		try{
			vector = channel.ls(remotePath);
		}catch (SftpException e){
			logger.error("List file error",e);
		}
		return vector;
	}

	private boolean checkFileExist(String localPath) {
		File file = new File(localPath);
		return file.exists();
	}
	
	/**
	 * 读取文本
	 * @param path 文件目录+文件名
	 * @throws Exception
	 */
	public static List<String> writeToDat(String path){
		File file = new File(path);
		List<String> list = new ArrayList<String>();
		String[] strings = null;
		if(!file.exists()){
			return new ArrayList<String>();
		}
		try{
			BufferedReader bw = new BufferedReader(new InputStreamReader(new FileInputStream(file), "gbk"));
			String line = null;
			//存到list
			while((line = bw.readLine())!=null){
				list.add(line);
			}
			bw.close();
		}catch(IOException e){
			logger.error(e);
		}
		
		//数组长度
		strings = new String[list.size()];
		for(int i = 0;i<list.size();i++){
			String s = list.get(i);
			strings[i] = s;
		}
		
		return list;
	}
	
	//将byte数组转化成字符串
	public static String byteArrayToString(byte[] byteArray,int begin,int length){
		byte[]  aimArray= new byte[length];
		System.arraycopy(byteArray, begin, aimArray, 0, length);
		String arrayString = null;
		try {
			arrayString = new String(aimArray,"GBK");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return arrayString;
		
	}
	
	@SuppressWarnings("static-access")
	public static boolean downfileTraceFromFtp(String fileName,String ftpPath,String localPath,String ftpHost,String ftpUserName,int ftpPort,String ftpPassword){
		FtpUtil ftpUtil = new FtpUtil();
		ChannelSftp channeltest = ftpUtil.getConnect( ftpHost, ftpUserName, ftpPort, ftpPassword);
		System.out.println(channeltest.isConnected());
		File file  = new File(localPath);//目标文件的目录
		if(!file.exists()&& !file.isDirectory()){//如果目录不存在就创建目录
			boolean mkdirs = file.mkdirs();
			if(!mkdirs){
				logger.error("文件：" + fileName + "创建目录时失败");
				return false;
			}
		}
		boolean downloadFile = false;
		try {
			downloadFile = ftpUtil.downloadFile(fileName, ftpPath, localPath+"//"+fileName);//文件下载
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		//进行文件下载
		if(downloadFile){
			logger.info("文件:" + fileName + "已经下载成功" );
		}else{
			logger.error("文件：" + fileName + "下载失败");
		}
		
		try {
			ftpUtil.closeChannel();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(downloadFile){
			  return true;
		  }
		  return false;
	}

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception{
		String ftpHost = "sftp.credit2go.cn";//服务器地址
		String ftpUserName = "yironghengxin";//用户名
		int ftpPort = 5132;//端口号
		String ftpPassword = "vLUEC8CKa0EHi";//密码
		final String bankSerialNo = "3005";//银行编号
		final String productSerialNo = "0140";//产品编号
		FtpUtil ftpUtil = new FtpUtil();
		String ftpBasePath = "C2P/";
		// 文件名：$$$$-ALEVE????-YYYYMMDD   $$$$为银行编号 ：3005,????为产品编号:0140
		String nowDate = DateUtil.getNowDate();
		String fileDate  = DateUtil.delDay(nowDate, 1);//获取前一天的时间
		String dateYear =fileDate.substring(0, 4);
		String dateMonth = fileDate.substring(4,6);
		String dateDay = fileDate.substring(6,8);
		
		String ftpPath = ftpBasePath + dateYear + "/" + dateMonth + "/" + dateDay + "/";//文件在ftp上的目录
		String fileNamejx = bankSerialNo + "-ALEVE" + productSerialNo + "-" + fileDate+"1";//文件名
		String localPathjx = "D:/download/" +dateYear+"/"+dateMonth+"/"+ dateDay;//下载存储的目录
		ChannelSftp channeltest = ftpUtil.getConnect( ftpHost, ftpUserName, ftpPort, ftpPassword);
		System.out.println(channeltest.isConnected());
		File file  = new File(localPathjx);//aleve目标文件的目录
		if(!file.exists()&& !file.isDirectory()){//如果目录不存在就创建目录
			boolean mkdirs = file.mkdirs();
			if(!mkdirs){
				logger.error("文件：" + fileNamejx + "创建目录时失败");
				return;
			}
		}
		boolean downloadFile = ftpUtil.downloadFile(fileNamejx, ftpPath, localPathjx+"/"+fileNamejx);//进行文件下载
		if(downloadFile){
			logger.info("文件:" + fileNamejx + "已经下载成功" );
		}else{
			logger.error("文件：" + fileNamejx + "下载失败");
		}
		ftpUtil.closeChannel();
	}

}





















