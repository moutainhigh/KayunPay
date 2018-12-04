package com.dutiantech.util;

public class TypeUtil {

	public static String getLoanUsedType(String key){
		String value = "";
		switch (key) {
		case "A":
			value = "短期周转";
			break;
		case "B":
			value = "个人消费";
			break;
		case "C":
			value = "投资创业";
			break;
		case "D":
			value = "购车借款";
			break;
		case "E":
			value = "装修借款";
			break;
		case "F":
			value = "婚礼筹备";
			break;
		case "G":
			value = "教育培训";
			break;
		case "H":
			value = "医疗支出";
			break;
		case "J":
			value = "购房借款";
			break;
		default:
			value = "其他借款";
			break;
		}
		return value;
	}
	
	
	
}
