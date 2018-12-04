package com.jx.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.fuiou.http.HttpClientHelper;
import com.fuiou.util.ConfigReader;

public class WebUtils {

	public static String sendHttp(String url, Object parameters) throws Exception {
		String outStr = "";
		try {
			String charSet = "UTF-8";
			String timeOut = ConfigReader.getConfig("TimeOut");
			outStr = HttpClientHelper.doHttp(url, charSet, parameters, timeOut);
			if (outStr == null) {
				throw new Exception("请求接口失败!");
			}
			System.out.println("接口返回=" + outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("请求接口失败!");
		}
		return outStr;
	}

	public static String sendHttp(String url, Map parameters) throws Exception {
		String outStr = "";
		try {
			String charSet = "UTF-8";
			String timeOut = ConfigReader.getConfig("TimeOut");
			outStr = HttpClientHelper.doHttp(url, charSet, parameters, timeOut);
			if (outStr == null) {
				throw new Exception("请求接口失败!");
			}
			System.out.println("接口返回=" + outStr);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("请求接口失败!");
		}
		return outStr;
	}

	public static String genForwardHtml(String url, Map<String, String> parameters, String charset) {
		StringBuffer returnHtml = new StringBuffer("");
		if (!"".equals(url)) {
			returnHtml.append("<html>");
			String head = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + charset
					+ "\" pageEncoding=\"" + charset + "\" />";
			returnHtml.append(head);
			returnHtml.append("<title>loading</title>");
			returnHtml.append("<style type=\"text/css\">");
			returnHtml.append(
					"body{margin:200px auto;font-family: \"宋体\", Arial;font-size: 12px;color: #369;text-align: center;}");
			returnHtml.append("#1{height:auto; width:78px; margin:0 auto;}");
			returnHtml.append("#2{height:auto; width:153px; margin:0 auto;}");
			returnHtml.append("vertical-align: bottom;}");
			returnHtml.append("</style>");
			returnHtml.append("</head>");
			returnHtml.append("<body>");
			returnHtml.append("<div id=\"3\">交易处理中...</div>");
			returnHtml.append("<form name=\"forwardForm\" action=\"").append(url).append("\" method=\"POST\">");
			System.out.println("WebUtils genForwardHtml::url=" + url);
			Iterator keyIterator = parameters.keySet().iterator();
			while (keyIterator.hasNext()) {
				Object key = keyIterator.next();
				returnHtml.append("  <input type=\"hidden\" name=\"").append(key.toString()).append("\" value=\"")
						.append((String) parameters.get(key)).append("\"/>");
				System.out.println("WebUtils genForwardHtml::" + key.toString() + "=" + parameters.get(key));
			}
			returnHtml.append("</form>");
			returnHtml.append("<SCRIPT LANGUAGE=\"Javascript\">");
			returnHtml.append("    document.forwardForm.submit();");
			returnHtml.append("</SCRIPT>");
			returnHtml.append("</body>");
			returnHtml.append("</html>");
		}
		return returnHtml.toString();
	}

	/**
	 * 页面提示弹窗 WJW
	 * 
	 * @param promptStr
	 *            提示文字
	 * @param url
	 *            跳转链接
	 * @param charset
	 *            编码
	 * @return
	 */
	public static void writePromptHtml(String promptStr, String url, String charset, HttpServletResponse response) {
		StringBuffer returnHtml = new StringBuffer("");
		returnHtml.append("<html>");
		String head = "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=" + charset
				+ "\" pageEncoding=\"" + charset + "\" />";
		returnHtml.append(head);
		returnHtml.append("<title>loading</title>");
		returnHtml.append("</head>");
		returnHtml.append("<body>");
		returnHtml.append("<SCRIPT LANGUAGE=\"Javascript\">");
		returnHtml.append("alert('" + promptStr + "');window.location.href='" + url + "';");
		returnHtml.append("</SCRIPT>");
		returnHtml.append("</body>");
		returnHtml.append("</html>");

		OutputStream out;
		try {
			out = response.getOutputStream();
			out.write(returnHtml.toString().getBytes("utf-8"));
			out.flush();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}