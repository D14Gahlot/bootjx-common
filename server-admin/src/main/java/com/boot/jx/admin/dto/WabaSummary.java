package com.boot.jx.admin.dto;
import java.util.List;
import java.util.Map;

public class WabaSummary {
	List<WabaSummaryDocDto> wabaSummaryCount;
	Map<String,Long> mediaSummaryCount;
	public List<WabaSummaryDocDto> getWabaSummaryCount() {
		return wabaSummaryCount;
	}
	public void setWabaSummaryCount(List<WabaSummaryDocDto> wabaSummaryCount) {
		this.wabaSummaryCount = wabaSummaryCount;
	}
	public Map<String, Long> getMediaSummaryCount() {
		return mediaSummaryCount;
	}
	public void setMediaSummaryCount(Map<String, Long> mediaSummaryCount) {
		this.mediaSummaryCount = mediaSummaryCount;
	}
}
