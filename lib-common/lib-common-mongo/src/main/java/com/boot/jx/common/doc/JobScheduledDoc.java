package com.boot.jx.common.doc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.TimeStampDoc;

@Document(collection = "JOBS_SCHEDULED")
@TypeAlias("JobScheduledDoc")
public class JobScheduledDoc extends TimeStampDoc {

	@Id
	private String id;
	private String jobtype;
	Map<String, List<Object>> input = new HashMap<String, List<Object>>();
	private String isactive;
	public String status;
	public TimeStampIndex time;
	private String instanceId;

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

	public Map<String, List<Object>> getInput() {
		return input;
	}

	public void setInput(Map<String, List<Object>> input) {
		this.input = input;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

}
