package com.dutiantech.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;


public class HttpRequestor {
    
    private String charset = "utf-8";
    private Integer connectTimeout = null;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;
    private Map<String , String> headers = new HashMap<String , String>();
    
    public String doGet(String url){
    	try{
    		return doGet(url, new HashMap<String , Object>());
    	}catch(Exception e){
    		e.printStackTrace( );
        	return null ;
    	}
    }
    
    /**
     * Do GET request
     * @param url
     * @return
     * @throws Exception
     * @throws IOException
     */
    public String doGet(String url, Map<String, Object> parameterMap) throws Exception {
        
    	String reuqestParams = makeRequestParams(parameterMap) ;
    	if( reuqestParams.length() > 0 ){
    		if( url.indexOf("?") > 0 ){
    			reuqestParams = "&" + reuqestParams ;
    		}
    		url = url + reuqestParams ;
    	}
    	
        URL localURL = new URL(url);
        
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        
        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        
        if (httpURLConnection.getResponseCode() >= 300) {
            throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
        }
        
        try {
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            
        } finally {
            
            if (reader != null) {
                reader.close();
            }
            
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            
        }

        return resultBuffer.toString();
    }
    
    
    /**
     * 	post请求，自定义写入数据
     * @param url
     * @param reqBody
     * @return
     * @throws Exception
     */
    public String doPostWithHttps(String url , String reqBody ) throws Exception{

        URL localURL = new URL(url);
        
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(reqBody.length()));
        httpURLConnection.setRequestProperty("Charset", "utf-8");
        
        if(httpURLConnection instanceof HttpsURLConnection){
    		SSLContext sc = SSLContext.getInstance("SSL");
    		sc.init(null, new TrustManager[]{new TrustAnyTrustManager()}, new java.security.SecureRandom());  
            ((HttpsURLConnection) httpURLConnection).setSSLSocketFactory(sc.getSocketFactory());  
            ((HttpsURLConnection) httpURLConnection).setHostnameVerifier(new TrustAnyHostnameVerifier());
    	}

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
            
            outputStreamWriter.write( reqBody );
            outputStreamWriter.flush();
            
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            
        } finally {
            
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            
            if (outputStream != null) {
                outputStream.close();
            }
            
            if (reader != null) {
                reader.close();
            }
            
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            
        }

        return resultBuffer.toString();
    }
    
    
    /**
     * 	post请求，自定义写入数据
     * @param url
     * @param reqBody
     * @return
     * @throws Exception
     */
    public String doPost(String url , String reqBody ) throws Exception{

        URL localURL = new URL(url);
        
        URLConnection connection = openConnection(localURL);
        HttpURLConnection httpURLConnection = (HttpURLConnection)connection;
        
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setRequestProperty("Accept-Charset", charset);
        httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        httpURLConnection.setRequestProperty("Content-Length", String.valueOf(reqBody.length()));
        httpURLConnection.setRequestProperty("Charset", "utf-8");

        OutputStream outputStream = null;
        OutputStreamWriter outputStreamWriter = null;
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader reader = null;
        StringBuffer resultBuffer = new StringBuffer();
        String tempLine = null;
        
        try {
            outputStream = httpURLConnection.getOutputStream();
            outputStreamWriter = new OutputStreamWriter(outputStream,"UTF-8");
            
            outputStreamWriter.write( reqBody );
            outputStreamWriter.flush();
            
            if (httpURLConnection.getResponseCode() >= 300) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            
            inputStream = httpURLConnection.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream);
            reader = new BufferedReader(inputStreamReader);
            
            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            
        } finally {
            
            if (outputStreamWriter != null) {
                outputStreamWriter.close();
            }
            
            if (outputStream != null) {
                outputStream.close();
            }
            
            if (reader != null) {
                reader.close();
            }
            
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            
            if (inputStream != null) {
                inputStream.close();
            }
            
        }

        return resultBuffer.toString();
    }
    
    /**
     * Do POST request
     * @param url
     * @param parameterMap
     * @return
     * @throws Exception 
     */
    public String doPost(String url, Map<String, Object> parameterMap) throws Exception {
        
        /* Translate parameter map to parameter date string */
        String requestParams = makeRequestParams(parameterMap) ;

        return doPost(url, requestParams) ;
    }

    private URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            connection = localURL.openConnection(proxy);
        } else {
            connection = localURL.openConnection();
        }
        
        //设置请求头
        if( headers.size() > 0 ){
        	Iterator<String> its = headers.keySet().iterator();
        	while( its.hasNext() ){
        		String key = its.next() ;
        		connection.setRequestProperty(key, headers.get(key));
        	}
        }
        
        return connection;
    }
    
    public void setHeader(String key , String value ){
    	headers.put(key, value) ;
    }
    
    /**
     * Render request according setting
     * @param request
     
    private void renderRequest(URLConnection connection) {
        
        if (connectTimeout != null) {
            connection.setConnectTimeout(connectTimeout);
        }
        
        if (socketTimeout != null) {
            connection.setReadTimeout(socketTimeout);
        }
        
    }*/
    
    public String makeRequestParams(Map<String, Object> parameterMap){
        StringBuffer parameterBuffer = new StringBuffer();
    	if (parameterMap != null) {
            Iterator<String> iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String)iterator.next();
                if (parameterMap.get(key) != null) {
                    value = String.valueOf( parameterMap.get(key));
                } else {
                    value = "";
                }
                
                parameterBuffer.append(key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
    	return parameterBuffer.toString() ;
    }
    
    
    /*
     * Getter & Setter
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }
    
}