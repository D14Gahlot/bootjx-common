package com.boot.jx.admin.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;

public class JobsResponseDto extends TimeStampDoc {

	private String id;
	private String jobtype;
	private String jobId;
	Map<String, List<Object>> input = new HashMap<String, List<Object>>();
	Map<String, List<Object>> output = new HashMap<String, List<Object>>();
	private String isactive;
	private Long createdStamp;
	private String createBy;
	public String status;
	public TimeStampIndex time;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getIsactive() {
		return isactive;
	}

	public void setIsactive(String isactive) {
		this.isactive = isactive;
	}

	public Long getCreatedStamp() {
		return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
		this.createdStamp = createdStamp;
	}

	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public TimeStampIndex getTime() {
		return time;
	}

	public void setTime(TimeStampIndex time) {
		this.time = time;
	}

	public void setInput(Map<String, List<Object>> input) {
		this.input = input;
	}

	public Map<String, List<Object>> getInput() {
		return input;
	}

	

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Map<String, List<Object>> getOutput() {
		return output;
	}

	public void setOutput(Map<String, List<Object>> output) {
		this.output = output;
	}

	
	public String getJobtype() {
		return jobtype;
	}

	public void setJobtype(String jobtype) {
		this.jobtype = jobtype;
	}

	
}
