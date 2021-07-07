package com.boot.jx.tunnel.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.model.MapModel;

public abstract class JobTaskModel<T> implements Serializable {

	private static final long serialVersionUID = -8178126816683098712L;

	public static enum JOB_STATUS {
		CREATED, READING, READING_DONE, EXECUTING, RESOLVED, TALLY, CLOSED, COMPLETED
	}

	private String tenant;
	private String jobId;
	private String batchId;

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	private Map<String, Object> data;

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	@SuppressWarnings("unchecked")
	public T jobId(String jobId) {
		this.jobId = jobId;
		return (T) this;
	}

	public MapModel data() {
		if (this.data == null) {
			this.data = new HashMap<String, Object>();
		}
		return new MapModel(data);
	}

	@SuppressWarnings("unchecked")
	public T data(String key, Object value) {
		this.data().put(key, value);
		return (T) this;
	}

	public String jobUUID() {
		return String.format("%s-%s", this.getTenant(), this.getJobId());
	}

	public static class BatchJob extends JobTaskModel<BatchJob> {
		private static final long serialVersionUID = 8313215764559747000L;
		private JOB_STATUS status;
		private long pushedTaskCount;
		private long doneTaskCount;
		private long donePercent;
		private long openStamp;
		private long resolveStamp;
		private long closeStamp;
		private long tallyStamp;

		public JOB_STATUS getStatus() {
			return status;
		}

		public void setStatus(JOB_STATUS status) {
			this.status = status;
		}

		public long getPushedTaskCount() {
			return pushedTaskCount;
		}

		public void setPushedTaskCount(long pushedTaskCount) {
			this.pushedTaskCount = pushedTaskCount;
		}

		public long getDoneTaskCount() {
			return doneTaskCount;
		}

		public void setDoneTaskCount(long doneTaskCount) {
			this.doneTaskCount = doneTaskCount;
		}

		public long getDonePercent() {
			return donePercent;
		}

		public void setDonePercent(long donePercent) {
			this.donePercent = donePercent;
		}

		public long getOpenStamp() {
			return openStamp;
		}

		public void setOpenStamp(long startStamp) {
			this.openStamp = startStamp;
		}

		public long getResolveStamp() {
			return resolveStamp;
		}

		public void setResolveStamp(long completeStamp) {
			this.resolveStamp = completeStamp;
		}

		public long getTallyStamp() {
			return tallyStamp;
		}

		public void setTallyStamp(long tallyStamp) {
			this.tallyStamp = tallyStamp;
		}

		public long getCloseStamp() {
			return closeStamp;
		}

		public void setCloseStamp(long closeStamp) {
			this.closeStamp = closeStamp;
		}

	}

	public static class Tasklet extends JobTaskModel<Tasklet> {
		private static final long serialVersionUID = -7195990852420923324L;
		private String ackId;
		private String taskId;

		public String getTaskId() {
			return taskId;
		}

		public void setTaskId(String taskId) {
			this.taskId = taskId;
		}

		public String getAckId() {
			return ackId;
		}

		public void setAckId(String ackId) {
			this.ackId = ackId;
		}

		public String taskUUID() {
			return String.format("%s-%s-%s", this.getTenant(), this.getJobId(), this.getTaskId());
		}

		public Tasklet taskId(String taskId) {
			this.taskId = taskId;
			return this;
		}

	}

	public static Tasklet newTasklet(BatchJob job) {
		Tasklet taslet = new Tasklet();
		taslet.setTenant(job.getTenant());
		taslet.setJobId(job.getJobId());
		taslet.setBatchId(job.getBatchId());
		return taslet;
	}

	public static BatchJob newBatchJob() {
		return new BatchJob();
	}

}
