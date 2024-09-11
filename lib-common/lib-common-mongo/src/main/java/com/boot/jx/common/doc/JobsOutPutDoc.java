package com.boot.jx.common.doc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;

@Document(collection = "JOBS_OUTPUT")
@TypeAlias("JobsOutPutDoc")
public class JobsOutPutDoc extends TimeStampDoc {

	@Id
	private String id;
	private String jobType;
	Map<String, List<Object>> output = new HashMap<String, List<Object>>();
	private String isactive;
	public String status;
	public TimeStampIndex time;
	public String jobId;

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

	
	public Map<String, List<Object>> getOutput() {
		return output;
	}

	public void setOutput(Map<String, List<Object>> output) {
		this.output = output;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

}
