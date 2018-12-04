package com.dutiantech.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapSortUtil {
	
	/**
	 * Map值为Long，按值进行排序
	 * @param oldMap	Map
	 * @param type		0升序，非0降序
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map sortMapByLongVal(Map oldMap, final int type) {
		ArrayList<Map.Entry<String, Long>> list = new ArrayList<Map.Entry<String, Long>>(oldMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Long>>() {  
			@Override  
			public int compare(Entry<java.lang.String, Long> arg0,
					Entry<java.lang.String, Long> arg1) {
				long tmpVal = arg0.getValue().longValue()-arg1.getValue().longValue();
				if(type==0){
					if(tmpVal > 0)
						return 1;
					else if(tmpVal == 0)
						return 0;
					else
						return -1;
				}else{
					if(tmpVal > 0)
						return -1;
					else if(tmpVal == 0)
						return 0;
					else
						return 1;
				}
			}
		});
		Map newMap = new LinkedHashMap();
		for (int i = 0; i < list.size(); i++) {
			newMap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return newMap;  
	}

}
