package com.boot.jx.tunnel.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.model.MapModel;

public abstract class JobTaskModel<T> implements Serializable {

	private static final long serialVersionUID = -8178126816683098712L;
	private String tenant;
	private String jobId;
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
		return taslet;
	}

	public static BatchJob newBatchJob() {
		return new BatchJob();
	}

}
