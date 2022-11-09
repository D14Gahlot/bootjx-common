package com.boot.jx.admin.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WabaSummaryDto {
	String tenant;
	String month;
	Map<Object,Long> summaryCount;
	Map<Object,List<ContactTypeCountDto>> map;
	Map<String, Map<String, Long>> dateWiseSummaryCount;
	
	Map<Object,Map<Object,Object>> dateWiseCountMap = new HashMap<>();
	
}
