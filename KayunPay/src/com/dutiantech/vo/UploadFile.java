package com.dutiantech.vo;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.dutiantech.util.StringUtil;

public class UploadFile {

	private String name;
	private String type;
	private byte[] data;
	private String charset = "ISO-8859-1";
	private HttpServletRequest request;
	private Map<String , String> REQ_PARS = new HashMap<String , String>();

	public UploadFile(HttpServletRequest request) {
		this.request = request;
	}

	// 构造类
	class Position {

		int begin;
		int end;

		public Position(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}
	
	//获取文件名
	@SuppressWarnings("unused")
	private String getFileName(String requestBody) {
//		System.out.println(requestBody);
        String fileName = requestBody.substring(requestBody.indexOf("filename=\"") + 10);
        fileName = fileName.substring(0, fileName.indexOf("\n"));
        fileName = fileName.substring(fileName.indexOf("\n") + 1, fileName.indexOf("\""));
        return fileName;
    }
	
	//获取文件区间
	private Position getFilePosition(String contentType, String textBody)
			throws IOException {
		// 取得文件区段边界信息
		String boundaryText = contentType.substring(
				contentType.lastIndexOf("=") + 1, contentType.length());
		// 取得实际上传文件的气势与结束位置
		int pos = textBody.indexOf("filename=\"");
		pos = textBody.indexOf("\n", pos) + 1;
		pos = textBody.indexOf("\n", pos) + 1;
		pos = textBody.indexOf("\n", pos) + 1;
		int boundaryLoc = textBody.indexOf(boundaryText, pos) - 4;
		int begin = ((textBody.substring(0, pos)).getBytes("ISO-8859-1")).length;
		int end = ((textBody.substring(0, boundaryLoc)).getBytes("ISO-8859-1")).length;

		return new Position(begin, end);
	}
	
	public void make() throws IOException {
		String contentType = request.getContentType();
//		System.out.println(contentType);
		int fileLength = request.getContentLength();
		byte[] tmpData = new byte[fileLength];
		DataInputStream in = new DataInputStream(request.getInputStream());
		//String line = null ;
		in.readFully(tmpData);
		in.close();
		
		String tmpBody = new String( tmpData , charset );
		name = getFileName(tmpBody) ;
		getParams(tmpBody);
		Position pos = getFilePosition( contentType, tmpBody) ;
		data = new byte[pos.end - pos.begin ] ;
		System.arraycopy( tmpData , pos.begin , data , 0 , pos.end - pos.begin );
		
	}
	
	public Map<String , String > getParas(){
		return REQ_PARS ;
	}
	
	public String getPara(String key ){
		return REQ_PARS.get(key) ;
	}
	
	private void getParams(String body){
		//System.out.println( body );
		//获取patload区域内容
		String firstLine = body.substring( 0 , body.indexOf("\n")).trim();
		String[] ps = body.split(firstLine) ;
		for(String line : ps ){
			if( line.indexOf("Content-Disposition: form-data;") != -1 && line.indexOf("Content-Type: image/png")==-1 ){
				String[] pv = line.split("\r\n");
				String key = pv[1];
				key = key.substring( key.indexOf("\"")+1, key.lastIndexOf("\""));
				String val = pv[3] ;
				if( StringUtil.isBlank(key) == false ){
					REQ_PARS.put( key , val ) ;
				}
			}
		}
	}

	public static UploadFile makeFile(HttpServletRequest request) {
		UploadFile file = new UploadFile(request);
		try {
			file.make();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return file;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}
	/**
	 * 验证上传文件扩展名
	 * true 验证通过  false  不通过
	 * */
	public boolean verifyExtensionName(){
		String[] veName = {"jpg","png","bmp","gif","doc","wps","xls","txt","pdf","xlsx","jpeg"};
		String exName=name.substring(name.lastIndexOf(".")+1).toLowerCase();
		return Arrays.asList(veName).contains(exName);
	}
}
