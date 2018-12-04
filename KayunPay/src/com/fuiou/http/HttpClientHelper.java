package com.fuiou.http;



import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class HttpClientHelper{
    
    public static String doHttp(String urlStr,String charSet,Object parameters, String timeOut) throws Exception{
    	String responseString="";
    	PostMethod xmlpost;
 	    int statusCode = 0;
 	    HttpClient httpclient = new HttpClient();
// 	    httpclient.setConnectionTimeout(new Integer(timeOut).intValue());
// 	    httpclient.getParams().setConnectionManagerTimeout(new Long(timeOut));
 	   httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(new Integer(timeOut).intValue());
 	   httpclient.getHttpConnectionManager().getParams().setSoTimeout(new Integer(timeOut).intValue());
 	    xmlpost = new PostMethod(urlStr);
 	    httpclient.getParams().setParameter(
 	    		HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
        try{
        	//组合请求参数
        	List<NameValuePair> list=new ArrayList<NameValuePair>();
			Method[] ms=parameters.getClass().getMethods();
			for(int i=0;i<ms.length;i++){
				Method m=ms[i];
				String name=m.getName();
				if(name.startsWith("get")){
					String param=name.substring(3,name.length());
					param=param.substring(0,1).toLowerCase()+param.substring(1,param.length());
					if(param.equals("class")){
						continue;
					}
					Object value="";
					try {
						value = m.invoke(parameters, null);
//						LogWriter.error("=====>name="+m.getName()+",value="+value);
						NameValuePair nvp=new NameValuePair(param,(value==null?"":value.toString()));
						list.add(nvp);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						throw e;
					}
				}
			}
			NameValuePair[] nvps=new NameValuePair[list.size()];
        	xmlpost.setRequestBody(list.toArray(nvps)); 
        	statusCode = httpclient.executeMethod(xmlpost);
	    	responseString = xmlpost.getResponseBodyAsString();
//        	statusCode=HttpURLConnection.HTTP_OK;
//	    	responseString = "";
            if(statusCode<HttpURLConnection.HTTP_OK || statusCode>=HttpURLConnection.HTTP_MULT_CHOICE){
                throw new Exception("请求接口失败，失败码[ "+ statusCode +" ]");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        } finally{
        	xmlpost.releaseConnection();
        	httpclient.getHttpConnectionManager().closeIdleConnections(0);
        }
        return responseString;
    }
    
    public static String doHttp(String urlStr,String charSet,Map parameters, String timeOut) throws Exception{
    	String responseString="";
    	PostMethod xmlpost;
 	    int statusCode = 0;
 	    HttpClient httpclient = new HttpClient();
// 	    httpclient.setConnectionTimeout(new Integer(timeOut).intValue());
// 	   httpclient.getParams().setConnectionManagerTimeout(new Long(timeOut));
 	   httpclient.getHttpConnectionManager().getParams().setConnectionTimeout(new Integer(timeOut).intValue());
 	   httpclient.getHttpConnectionManager().getParams().setSoTimeout(new Integer(timeOut).intValue());
 	    xmlpost = new PostMethod(urlStr);
 	    httpclient.getParams().setParameter(
 	    		HttpMethodParams.HTTP_CONTENT_CHARSET, charSet);
        try{
        	//组合请求参数
        	List<NameValuePair> list=new ArrayList<NameValuePair>();
			Iterator it=parameters.keySet().iterator();
			while(it.hasNext()){
				String key=(String)it.next();
				NameValuePair nvp=new NameValuePair(key,(parameters.get(key)==null?"":parameters.get(key).toString()));
				list.add(nvp);
			}
			
			NameValuePair[] nvps=new NameValuePair[list.size()];
        	xmlpost.setRequestBody(list.toArray(nvps)); 
        	statusCode = httpclient.executeMethod(xmlpost);
	    	responseString = xmlpost.getResponseBodyAsString();
            if(statusCode<HttpURLConnection.HTTP_OK || statusCode>=HttpURLConnection.HTTP_MULT_CHOICE){
                throw new Exception("请求接口失败，失败码[ "+ statusCode +" ]");
            }
        }catch(Exception e){
            e.printStackTrace();
            throw e;
        } finally{
        	xmlpost.releaseConnection();
        	httpclient.getHttpConnectionManager().closeIdleConnections(0);
        }
        return responseString;
    }

}
