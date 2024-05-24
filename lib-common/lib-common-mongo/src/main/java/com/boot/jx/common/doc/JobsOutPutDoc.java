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
	private String jobtype;
	Map<String, List<Object>> output = new HashMap<String, List<Object>>();
	private String isactive;
	public String status;
	public TimeStampIndex time;
	public String jobid;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getJobtype() {
		return jobtype;
	}

	public void setJobtype(String jobtype) {
		this.jobtype = jobtype;
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

	public String getJobid() {
		return jobid;
	}

	public void setJobid(String jobid) {
		this.jobid = jobid;
	}

	public Map<String, List<Object>> getOutput() {
		return output;
	}

	public void setOutput(Map<String, List<Object>> output) {
		this.output = output;
	}

}
