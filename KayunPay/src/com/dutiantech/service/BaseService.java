package com.dutiantech.service;

import java.util.ArrayList;
import java.util.List;

import com.dutiantech.util.StringUtil;

public class BaseService {

	/**
	 * 	该字段只适用与数据库中为字符的字段
	 */
	protected List<String> keys = new ArrayList<String>();
	
//	/**	
//	 * 	模糊查询，因为是拼接sql，所以必须有强限制防止注入
//	 * 		限制一 不允许有空格出现
//	 * 		限制二 过滤0x，暂不实现
//	 * 		限制三 不允许出现'，+ 等
//	 * 		限制四 全key检索的长度大于1,小于10，大于10会被截取
//	 * @param value
//	 * @return
//	 */
//	public String getFuzzyQueryConditions(String value){
//		value = tranSql(value);
//		value.replace(" ", "");
//		if( value.length() == 0 ){
//			return "";
//		}
//		
//		if( value.length() > 10 ){
//			value = value.substring(0 , 10 ) ;
//		}
//		
//		StringBuffer buff = new StringBuffer();
//		//buff.append(" and 1=1 ");
//		int tmp = 1;
//		for(String key : keys){
//			if(tmp==1)
//				buff.append(key + " like '%" + value + "%' ");
//			else
//				buff.append( "or " + key + " like '%" + value + "%' ");
//			tmp++;
//		}
//		
//		return buff.toString();
//	}
	
//	private String tranSql(String key ){
//		return key.replaceAll(".*([';]+|(--)+).*", "");
//	}
	
	protected String makeSql4WhereHasWhere(StringBuffer buff ){
		if( buff.length() > 0 ){
			return " and " + buff.toString();
		}else{
			return "" ;
		}
	}
	
	protected String makeSql4Where(StringBuffer buff ){
		if( buff.length() > 0 ){
			return " where " + buff.toString();
		}else{
			return "" ;
		}
	}
	
	protected void makeExp4like(StringBuffer buff , String key , String value ,String ex ,List<Object> ps ) {
		if( StringUtil.isBlank(value) == false ){
			String tpl = " %s %s like ? ";
			if( buff.length() == 0 ){
				buff.append(String.format(tpl, "" , key ));
			}else{
				buff.append(String.format(tpl, ex , key ));
			}
			ps.add("%" + value + "%");
		}
	}
	
	protected void makeExp4like( StringBuffer buff , String key , String value ,List<Object> ps){
		makeExp4like(buff, key, value ,"and", ps);
	}
	
	/**
	 * 
	 * @param buff
	 * @param ps
	 * @param key
	 * @param ex1		< = 
	 * @param value
	 * @param ex2	and or
	 */
	protected void makeExp(StringBuffer buff ,List<Object> ps, String key , String ex1 , String value ,String ex2){
		
		if(StringUtil.isBlank( value ) == false ){
			String tpl = " %s %s %s ? ";
			if( buff.length() == 0 ){
				buff.append(String.format(tpl, "" , key , ex1 ));
			}else{
				buff.append(String.format(tpl, ex2 , key , ex1 ));
			}
			ps.add(value) ;
		}
	}
	
	protected void makeExp4In(StringBuffer buff ,List<Object> ps, String key , String[] values, String ex2){
		if(!StringUtil.isBlank(values[0].trim()) && values.length > 0){
			if(buff.length()>=3){
				buff.append(" ").append(ex2).append(" ");
			}
			buff.append(key).append(" in (");
			for (int i = 0; i < values.length; i++) {
				ps.add(values[i]);
				buff.append("?");
				if(i<values.length-1){
					buff.append(",");
				}
			}
			buff.append(") ");
		}
	}
	
	/**
	 * 
	 * @param buff
	 * @param ps
	 * @param key
	 * @param values
	 * @param ex2		第一个
	 * @param ex3		里面的 循环的
	 */
	protected void makeExp4AnyLike(StringBuffer buff ,List<Object> ps, String[] key , String values, String ex2, String ex3){
		if(!StringUtil.isBlank(values)){
			if(buff.length()>=3){
				buff.append(" ").append(ex2);
			}
			if(key.length>1){
				buff.append(" (");
			}
			for (int i = 0; i < key.length; i++) {
				ps.add("%"+values+"%");
				if(i>0){
					buff.append(" ").append(ex3).append(" ");
				}
				buff.append(key[i]).append(" like ? ");
			}
			if(key.length>1){
				buff.append(" )");
			}
		}
	}
	
}
