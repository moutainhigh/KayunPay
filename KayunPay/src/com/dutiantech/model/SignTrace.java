package com.dutiantech.model;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.plugin.activerecord.Model;

public class SignTrace extends Model<SignTrace> {
	
	public static Map<Integer, Integer> POINT_MAP = new HashMap<Integer, Integer>();
	static {
		POINT_MAP.put(7, 2500);
		POINT_MAP.put(12, 5000);
		POINT_MAP.put(20, 8800);
	}
	
	private static final long serialVersionUID = -2680295761436973353L;
	
	public static final SignTrace signTraceDao = new SignTrace();
	
}
