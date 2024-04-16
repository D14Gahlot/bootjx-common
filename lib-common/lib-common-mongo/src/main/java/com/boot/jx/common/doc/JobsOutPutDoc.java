package com.boot.jx.common.doc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;

@Document(collection="JOBS_OUTPUT")
@TypeAlias("JobsOutPutDoc")
public class JobsOutPutDoc {

	@Id
	private String id;
	private String  jobtype;
	List<Map<String, Object>> outputLst = new ArrayList<>();
	private String isactive;
	private Long createdStamp;
	private String createBy;
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
	

	public String getJobid() {
		return jobid;
	}
	public void setJobid(String jobid) {
		this.jobid = jobid;
	}
	public List<Map<String, Object>> getOutputLst() {
		return outputLst;
	}
	public void setOutputLst(List<Map<String, Object>> outputLst) {
		this.outputLst = outputLst;
	}
	
}
