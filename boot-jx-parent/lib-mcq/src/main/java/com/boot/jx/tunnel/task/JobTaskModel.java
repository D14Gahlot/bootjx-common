package com.boot.jx.tunnel.task;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.AppContextUtil;
import com.boot.jx.tunnel.ChronoScheduler;
import com.boot.jx.tunnel.ITunnelDefs.Schedulable;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

public abstract class JobTaskModel<T> implements Serializable, Schedulable {

	public JobTaskModel() {
		super();
		this.tenant = AppContextUtil.getTenant();
	}

	private static final long serialVersionUID = -8178126816683098712L;

	public static enum JOB_STATUS_TYPES {
		UNREADABLE;
	}

	public static enum JOB_STATUS {
		CREATED, READING, READING_DONE, EXECUTING, RESOLVED, TALLY, CLOSED(JOB_STATUS_TYPES.UNREADABLE),
		COMPLETED(JOB_STATUS_TYPES.UNREADABLE), CANCELLED(JOB_STATUS_TYPES.UNREADABLE),
		STOPPED(JOB_STATUS_TYPES.UNREADABLE);

		boolean readable;

		JOB_STATUS() {
			this.readable = true;
		}

		JOB_STATUS(JOB_STATUS_TYPES prop0, JOB_STATUS_TYPES... props) {
			this();
			set(prop0);
			for (JOB_STATUS_TYPES job_STATUS_TYPES : props) {
				set(job_STATUS_TYPES);
			}
		}

		private void set(JOB_STATUS_TYPES job_STATUS_TYPES) {
			switch (job_STATUS_TYPES) {
			case UNREADABLE:
				this.readable = false;
				break;
			default:
				break;
			}
		}

		public boolean isReadable() {
			return readable;
		}

	}

	private String tenant;
	private String jobId;
	private String batchId;
	private Long version;
	private ChronoScheduler scheduler;

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

	@SuppressWarnings("unchecked")
	public T scheduler(ChronoScheduler scheduler) {
		this.scheduler = scheduler;
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
		private Map<String, Long> stamps;

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

		public Map<String, Long> getStamps() {
			return stamps;
		}

		public void setStamps(Map<String, Long> stamps) {
			this.stamps = stamps;
		}

		public Map<String, Long> stamps() {
			if (stamps == null)
				stamps = new HashMap<String, Long>();
			return stamps;
		}

		public void updateStatus(JOB_STATUS status) {
			long now = System.currentTimeMillis();
			String statusStr = ArgUtil.parseAsString(status);
			this.status = status;
			this.stamps().put(statusStr, now);
			switch (status) {
			case CREATED:
				this.openStamp = now;
				break;
			case RESOLVED:
				this.resolveStamp = now;
				break;
			case CLOSED:
				this.closeStamp = now;
				break;
			case TALLY:
				this.tallyStamp = now;
				break;
			default:
				break;
			}
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
			return String.format("%s-%s-%s-%s", this.getTenant(), this.getJobId(), this.getTaskId(), this.getVersion());
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
		taslet.setVersion(job.getVersion());
		return taslet;
	}

	public static BatchJob newBatchJob() {
		return new BatchJob();
	}

	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public ChronoScheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(ChronoScheduler scheduler) {
		this.scheduler = scheduler;
	}
}
