package com.boot.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class MapUtils {

			
	public  static Map<Object, Long> mapMerge(Map<String, Long> dateRanMap1,Map<Object, Long> dateRanMap2) {
		Map<Object, Long> dateRanMap3 = new HashMap<>(dateRanMap1);
		 dateRanMap2.forEach((key, value) -> dateRanMap3.merge(key, value, (v1, v2) -> v1+v2));
		 return dateRanMap3;
	}
	
	public static Map<Object, Map<Object, Long>> defaultValue(Map<String, Map<String, Long>> dayWiseCountMap, List<String> channelLst,Map<Object, Long> dateRanMap,String tnt) {
		Map<Object, Map<Object, Long>> dayWiseMap = new HashMap<>();
		for (Map.Entry<String, Map<String, Long>> keyValue : dayWiseCountMap.entrySet()) {
			Map<Object, Long> dateWiseCnt = new HashMap<>();
			String key = keyValue.getKey();
			Map<String, Long> keyMap = dayWiseCountMap.get(key);
			if (ArgUtil.is(keyMap)) {
				dateWiseCnt = mapMerge(keyMap,dateRanMap);
				dayWiseMap.put(key, dateWiseCnt);
			}
		}
		for (String channel : channelLst) {
			String  tnt_channel=tnt + "_" + channel;
			if(!dayWiseMap.containsKey(tnt_channel)) {
				dayWiseMap.put(tnt_channel, dateRanMap);
			}
			
		}
		return dayWiseMap;
		
	}
}
