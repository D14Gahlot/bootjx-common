package com.boot.jx.tunnel.task;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import com.boot.jx.AppContextUtil;
import com.boot.jx.cache.CacheBox;
import com.boot.jx.def.ICacheBox;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mcq.Candidate;
import com.boot.jx.mcq.MCQLocker;
import com.boot.jx.tunnel.TunnelQueue;
import com.boot.jx.tunnel.TunnelService;
import com.boot.jx.tunnel.task.JobTaskModel.BatchJob;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;
import com.boot.utils.UniqueID;

public abstract class QueuedTaskExecuter {

	public static Logger LOGGER = LoggerService.getLogger(QueuedTaskExecuter.class);

	private static Map<String, Candidate> LOCK_MAP = Collections.synchronizedMap(new HashMap<String, Candidate>());

	private String jobName;

	private String getJobName() {
		if (this.jobName == null) {
			this.jobName = ClazzUtil.getUltimateClassName(this);
		}
		return this.jobName;
	}

	private Long fixedDelay;

	public Long fixedDelay() {
		if (fixedDelay == null || fixedDelay == 0L) {
			Class<?> c = ClazzUtil.getUltimateClass(this);
			for (Method method : c.getMethods()) {
				if (method.isAnnotationPresent(Scheduled.class) && method.getName().equals("reader")) {
					Scheduled annot = ClazzUtil.findMethodAnnotation(c, method, Scheduled.class);
					fixedDelay = annot.fixedDelay();
				}
			}
		}
		return fixedDelay;
	}

	@Autowired
	private TunnelService tunnelService;

	@Autowired(required = false)
	private RedissonClient redisson;

	private CacheBox<BatchJob> jobs;

	private CacheBox<String> cache;

	private TunnelQueue<Tasklet> queue;

	private TunnelQueue<BatchJob> batch;

	public TunnelQueue<Tasklet> queue() {
		if (queue == null) {
			this.queue = tunnelService.getQueue("QTE-TASK-Q2-" + getJobName());
		}
		return this.queue;
	}

	public ICacheBox<String> map() {
		if (cache == null) {
			this.cache = CacheBox.getInstance("QTE-TASK-M-" + getJobName(), redisson);
		}
		return this.cache;
	}

	public TunnelQueue<BatchJob> batch() {
		if (batch == null) {
			this.batch = tunnelService.getQueue("QTE-BATCH-Q2-" + getJobName());
		}
		return this.batch;
	}

	public ICacheBox<BatchJob> jobs() {
		if (jobs == null) {
			this.jobs = CacheBox.getInstance("QTE-BATCH-M" + getJobName(), redisson);
		}
		return this.jobs;
	}

	@Autowired
	private MCQLocker mcq;

	private Candidate lock() {
		String tenant = AppContextUtil.getTenant();
		if (!LOCK_MAP.containsKey(tenant + "." + getJobName())) {
			LOCK_MAP.put(tenant, new Candidate().fixedDelay(fixedDelay()).maxAge(fixedDelay() * 30)
					.queue("QTE-ELECTION-" + getJobName()));
		}
		return LOCK_MAP.get(tenant);
	}

	public void registerJob(BatchJob batchJob) {
		try {
			batchJob.setTenant(AppContextUtil.getTenant());
			batch().add(batchJob);
			jobs().put(batchJob.jobUUID(), batchJob);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerJob(String jobId) {
		BatchJob job = new BatchJob();
		job.setJobId(jobId);
		registerJob(job);
	}

	@Scheduled(fixedDelay = 1000)
	public void reader() {
		read();
	}

	protected void read() {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return;
		}

		BatchJob currentBatchJob = batch().poll();
		if (ArgUtil.is(currentBatchJob)) {
			AppContextUtil.setTenant(currentBatchJob.getTenant());
			String sessionId = UniqueID.generateString();
			AppContextUtil.setSessionId(sessionId);
			AppContextUtil.getTraceId(true, true);
			AppContextUtil.resetTraceTime();
			AppContextUtil.init();

			BatchJob nextBatchJob = this.read(currentBatchJob);

			if (ArgUtil.is(nextBatchJob))
				batch().add(nextBatchJob);
		}
	}

	/**
	 * This method needs implementation. Within function body of
	 * {@link #read(BatchJob)}, read your tasklets for current batch and push them
	 * one by one to executer using {{@link #push(Tasklet)},
	 * 
	 * @param batchJob
	 * @return
	 */
	public abstract BatchJob read(BatchJob batchJob);

	public String push(Tasklet tasklet) {
		if (!ArgUtil.is(tasklet.getTenant())) {
			tasklet.setTenant(AppContextUtil.getTenant());
		}
		String taskUUID = tasklet.taskUUID();
		String ackId = map().get(taskUUID);
		if (ArgUtil.is(ackId)) {
			return ackId;
		}
		ackId = UniqueID.generateString62();
		tasklet.setAckId(ackId);

		map().put(taskUUID, tasklet.getAckId());
		queue().add(tasklet);
		return ackId;
	}

	@Scheduled(fixedDelay = 1000)
	public void scheduler() {
		this.execute();
	}

	public abstract void execute(BatchJob taskJob, Tasklet tasklet);

	public void execute() {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return;
		}
		Tasklet tasklet = queue().poll();
		if (ArgUtil.is(tasklet)) {
			String taskUUID = tasklet.taskUUID();
			String ackId = map().get(taskUUID);
			if (ArgUtil.isEmpty(ackId) || ackId.equals("DONE") || !ackId.equals(tasklet.getAckId())) {
				return;
			}

			BatchJob taskJob = jobs().get(tasklet.jobUUID());

			try {
				if (ArgUtil.is(taskJob)) {
					AppContextUtil.setTenant(tasklet.getTenant());
					String sessionId = UniqueID.generateString();
					AppContextUtil.setSessionId(sessionId);
					AppContextUtil.getTraceId(true, true);
					AppContextUtil.resetTraceTime();
					AppContextUtil.init();
					this.execute(taskJob, tasklet);
					map().put(taskUUID, "DONE");
				} else {
					map().fastRemove(taskUUID);
				}

			} catch (Exception e) {
				LOGGER.error("For Tasklet:" + tasklet.getTaskId(), e);
			}
		}
	}

	public void execute(int count) {
		for (int i = 0; i < count; i++) {
			this.execute();
		}
	}

}
