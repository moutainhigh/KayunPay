package com.fuiou.util;

import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class Object2Xml {
	
//	private static XStream xstream = null;
	
	public static String object2xml(Object object,String name){
		XStream xstream = new XStream(new DomDriver());
		xstream.alias(name, object.getClass());
		return xstream.toXML(object);
	}

	public static Object xml2object(String xml,String name,Class<?> className){
		XStream xstream = new XStream(new DomDriver());
		xstream.alias(name, className);
		return xstream.fromXML(xml);
	}
	
	public static Object xml2objectAsAttr(String xml,Map<String,Class<?>> map){
		XStream xstream = new XStream(new DomDriver());
		for(Map.Entry<String, Class<?>> entry:map.entrySet()){
			xstream.alias(entry.getKey(), entry.getValue());
		}
		return xstream.fromXML(xml);
	}
	
	public static String object2xmlAsAttr(Object obj,Map<String,Class<?>> map){
		XStream xstream = new XStream(new DomDriver());
		for(Map.Entry<String, Class<?>> entry:map.entrySet()){
			xstream.alias(entry.getKey(), entry.getValue());
		}
		return xstream.toXML(obj);
	}
	
	public static String getByTag(String xml ,String tag){
	    if(xml==null||xml.equals("")||tag==null||tag.equals(""))
	    	return "";
		int beg = xml.indexOf("<"+tag+">");
		if(beg<0)return "";
		int end = xml.indexOf("</"+tag+">");
		if(end<0) return "";
		return xml.substring(beg+2+tag.length(),end);
  }	
	
	/**
	 *解析xml成javabean(list) 
	 * @param xml 需解析的xml字符
	 * @param map key:xml节点标签  ,value:xmljavabean类
	 * @param key javabean类里面的list名字
	 * @return
	 */
	public static Object xml2List(String xml,Map<String,Class<?>> map,String key){
		XStream xstream = new XStream(new DomDriver());
		for(Map.Entry<String, Class<?>> entry:map.entrySet()){
			xstream.alias(entry.getKey(), entry.getValue());
		}
		xstream.addImplicitCollection(map.get(key),key);
		return xstream.fromXML(xml);
	}
	public static void main(String[] args) {
//		String str="<opResultSet><opResult><txn_tm>交易时间</txn_tm><mchnt_txn_ssn>交易流水号</mchnt_txn_ssn><out_cust_nm>付款方企业名</out_cust_nm>" +
//				  "<in_cust_nm>收款方企业名</in_cust_nm><amt>交易金额</amt><balance>当时交易后转出账号的余额</balance><busi_nm>业务名称</busi_nm>" +
//				  "<contract_no>合同号</contract_no><fy_date>富友清算日</fy_date></opResult>" +
//				  "<opResult><txn_tm>交易时间</txn_tm><mchnt_txn_ssn>交易流水号</mchnt_txn_ssn><out_cust_nm>付款方企业名</out_cust_nm>" +
//				  "<in_cust_nm>收款方企业名</in_cust_nm><amt>交易金额</amt><balance>当时交易后转出账号的余额</balance><busi_nm>业务名称</busi_nm>" +
//				  "<contract_no>合同号</contract_no><fy_date>富友清算日</fy_date></opResult>" +
//				  "</opResultSet>";
//		Map<String , Class<?>> map=new HashMap<String, Class<?>>();
//		map.put("opResultSet", OpResultSet.class);
//		map.put("opResult", OpResult.class);
//		OpResultSet opResultSet = (OpResultSet) Object2Xml.xml2List(str, map,"opResultSet");
//		System.out.println(opResultSet.getOpResultSet().size());
//		Map<String , Class<?>> map1=new HashMap<String, Class<?>>();
//		map1.put("opResultSet", OpResultSet.class);
//		map1.put("opResult", OpResult.class);
//		String aaa = Object2Xml.object2xmlAsAttr(opResultSet, map1);
//		System.out.println(aaa);
	}

}
