package com.boot.jx.admin.dto;

public class PeakLoadDto {
	Object timestamp;
	long total;
	long epochStamp;
	
	public long getTotal() {
		return total;
	}
	public void setTotal(long total) {
		this.total = total;
	}
	public Object getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Object timestamp) {
		this.timestamp = timestamp;
	}
	
	public long getEpochStamp() {
		return epochStamp;
	}
	public void setEpochStamp(long epochStamp) {
		this.epochStamp = epochStamp;
	}
}
