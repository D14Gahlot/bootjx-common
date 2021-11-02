package com.boot.jx.tunnel.task;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.redisson.api.RAtomicLong;
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
import com.boot.jx.tunnel.task.JobTaskModel.JOB_STATUS;
import com.boot.jx.tunnel.task.JobTaskModel.Tasklet;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;
import com.boot.utils.TimeUtils;
import com.boot.utils.UniqueID;

public abstract class QueuedTaskExecuter {

	public static final long JOB_RESOLVE_EXPIRY = 1000 * 60 * 15;
	public static final long JOB_TALLY_TIMEOUT = JOB_RESOLVE_EXPIRY / 10;

	public static Logger LOGGER = LoggerService.getLogger(QueuedTaskExecuter.class);

	private static Map<String, Candidate> LOCK_MAP = Collections.synchronizedMap(new HashMap<String, Candidate>());

	private String jobName;

	private String getJobName() {
		if (this.jobName == null) {
			this.jobName = ClazzUtil.getUltimateClassName(this) + "V5";
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

	private CacheBox<BatchJob> jobStatus;

	private CacheBox<String> taskStatus;

	private TunnelQueue<Tasklet> taskQueue;

	private TunnelQueue<BatchJob> jobQueue;

	public TunnelQueue<Tasklet> taskQueue() {
		if (taskQueue == null) {
			this.taskQueue = tunnelService.getQueue("QTE-TASK-PENDING-Q-" + getJobName());
		}
		return this.taskQueue;
	}

	public ICacheBox<String> taskStatus() {
		if (taskStatus == null) {
			this.taskStatus = CacheBox.getInstance("QTE-TASK-M-" + getJobName(), redisson);
		}
		return this.taskStatus;
	}

	public TunnelQueue<BatchJob> jobQueue() {
		if (jobQueue == null) {
			this.jobQueue = tunnelService.getQueue("QTE-BATCH-Q-" + getJobName());
		}
		return this.jobQueue;
	}

	public ICacheBox<BatchJob> jobStatus() {
		if (jobStatus == null) {
			this.jobStatus = CacheBox.getInstance("QTE-BATCH-M" + getJobName(), redisson);
		}
		return this.jobStatus;
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
			batchJob.setStatus(JOB_STATUS.CREATED);
			batchJob.setOpenStamp(System.currentTimeMillis());
			jobQueue().add(batchJob);
			jobStatus().put(batchJob.jobUUID(), batchJob);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void registerJob(String jobId) {
		BatchJob job = new BatchJob();
		job.setJobId(jobId);
		registerJob(job);
	}

	@Scheduled(fixedDelay = 2000)
	public void reader() {
		read();
	}

	protected void read() {
		if (redisson == null) {
			LOGGER.error("No Redissson Client Instance Available");
			return;
		}

		BatchJob currentBatchJob = jobQueue().poll();
		if (ArgUtil.is(currentBatchJob)) {
			
			
			AppContextUtil.setTenant(currentBatchJob.getTenant());
			String sessionId = UniqueID.generateString();
			AppContextUtil.setSessionId(sessionId);
			AppContextUtil.getTraceId(true, true);
			AppContextUtil.resetTraceTime();
			AppContextUtil.init();

			RAtomicLong pushedTaskCounter = redisson.getAtomicLong("PUSHED." + currentBatchJob.jobUUID());
			RAtomicLong pushedDoneCounter = redisson.getAtomicLong("DONE." + currentBatchJob.jobUUID());
			currentBatchJob.setPushedTaskCount(Math.max(1, pushedTaskCounter.get()));
			currentBatchJob.setDoneTaskCount(pushedDoneCounter.get());

			// Moving to next Step
			if (JOB_STATUS.CREATED == currentBatchJob.getStatus()) {
				currentBatchJob.setStatus(JOB_STATUS.READING);
			} else if (JOB_STATUS.READING_DONE == currentBatchJob.getStatus()) {
				currentBatchJob.setStatus(JOB_STATUS.EXECUTING);
			} else if (JOB_STATUS.CLOSED == currentBatchJob.getStatus()) {
				currentBatchJob.setStatus(JOB_STATUS.COMPLETED);
			}

			try {
				// Reading & Writing Steps
				if (JOB_STATUS.READING == currentBatchJob.getStatus()) {
					currentBatchJob.setBatchId(UniqueID.generateString62());
					long minDoneCount = currentBatchJob.getPushedTaskCount() / 2;
					if (currentBatchJob.getDoneTaskCount() >= minDoneCount) {

						try {
							boolean readCompleted = this.read(currentBatchJob);
							if (readCompleted) {
								currentBatchJob.setStatus(JOB_STATUS.READING_DONE);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				} else if (JOB_STATUS.EXECUTING == currentBatchJob.getStatus()
						|| JOB_STATUS.RESOLVED == currentBatchJob.getStatus()) {

					// Calculate Progress
					long percent = (currentBatchJob.getDoneTaskCount() * 100) / currentBatchJob.getPushedTaskCount();
					long currentProgress = (percent - currentBatchJob.getDonePercent());
					currentBatchJob.setDonePercent(percent);

					// JOB GOT RESOLVED
					boolean justGotResolved = false;
					if (JOB_STATUS.RESOLVED != currentBatchJob.getStatus() && currentBatchJob.getDonePercent() == 100) {
						currentBatchJob.setStatus(JOB_STATUS.RESOLVED);
						currentBatchJob.setResolveStamp(System.currentTimeMillis());
						justGotResolved = true;
					}

					// JOB is RESOLVED/TALLY =-------> Tally with Method
					if (justGotResolved
							// Tally Timeout has happened
							|| TimeUtils.isExpired(currentBatchJob.getTallyStamp(), JOB_TALLY_TIMEOUT)
							// Current Progress is more than 10%
							|| (currentProgress > 0L)) {
						boolean tallyCompleted = false;
						try {
							tallyCompleted = this.tally(currentBatchJob);
						} catch (Exception e) {
							e.printStackTrace();
						}

						currentBatchJob.setTallyStamp(System.currentTimeMillis());
						if (tallyCompleted || (currentBatchJob.getDonePercent() == 100
								&& TimeUtils.isExpired(currentBatchJob.getResolveStamp(), JOB_RESOLVE_EXPIRY))) {
							currentBatchJob.setStatus(JOB_STATUS.CLOSED);
						}

					}
				}
			} catch (Exception e) {
				LOGGER.error("READING OR TALLY ERROR", e);
			}

			LOGGER.debug("{} {} ... {}% = {}/{}", currentBatchJob.jobUUID(), currentBatchJob.getStatus(),
					currentBatchJob.getDonePercent(), currentBatchJob.getDoneTaskCount(),
					currentBatchJob.getPushedTaskCount());

			// Check if Final Step of continue
			if (JOB_STATUS.COMPLETED == currentBatchJob.getStatus()) {
				jobStatus().remove(currentBatchJob.jobUUID());
			} else {
				jobStatus().put(currentBatchJob.jobUUID(), currentBatchJob);
				jobQueue().add(currentBatchJob);
			}

		}
	}

	/**
	 * This method needs implementation. Within function body of
	 * {@link #read(BatchJob)}, read your tasklets for current batch and push them
	 * one by one to executer using {{@link #push(Tasklet)},
	 * 
	 * 
	 * @param currentBatchJob
	 * @return - return true when reading is completed otherwise false to continue
	 *         reading
	 */
	public abstract boolean read(BatchJob currentBatchJob);

	/**
	 * 
	 * Can be used to track overall status of job
	 * 
	 * @param currentBatchJob
	 * @return - return true when tally is completed otherwise false to continue
	 *         tallying
	 */
	public abstract boolean tally(BatchJob currentBatchJob);

	public String push(Tasklet tasklet) {
		if (!ArgUtil.is(tasklet.getTenant())) {
			tasklet.setTenant(AppContextUtil.getTenant());
		}
		String taskUUID = tasklet.taskUUID();
		String ackId = taskStatus().get(taskUUID);
		if (ArgUtil.is(ackId)) {
			return ackId;
		}
		ackId = UniqueID.generateString62();
		tasklet.setAckId(ackId);

		taskStatus().put(taskUUID, tasklet.getAckId());
		taskQueue().add(tasklet);

		RAtomicLong counter = redisson.getAtomicLong("PUSHED." + tasklet.jobUUID());
		counter.incrementAndGet();

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
		Tasklet tasklet = taskQueue().poll();
		if (ArgUtil.is(tasklet)) {
			String taskUUID = tasklet.taskUUID();
			String ackId = taskStatus().get(taskUUID);
			if (ArgUtil.isEmpty(ackId) || !ackId.equals(tasklet.getAckId())) {
				LOGGER.debug("Skipping Task {} for AckMisMatch {} {}", tasklet.getTaskId(), ackId, tasklet.getAckId());
				return;
			}

			BatchJob taskJob = jobStatus().get(tasklet.jobUUID());

			if (ArgUtil.is(taskJob)) {
				AppContextUtil.setTenant(tasklet.getTenant());
				String sessionId = UniqueID.generateString();
				AppContextUtil.setSessionId(sessionId);
				AppContextUtil.getTraceId(true, true);
				AppContextUtil.resetTraceTime();
				AppContextUtil.init();

				try {
					this.execute(taskJob, tasklet);
					taskStatus().put(taskUUID, "SUCCESS");
				} catch (Exception e) {
					LOGGER.error("For Tasklet:" + tasklet.getTaskId(), e);
					taskStatus().put(taskUUID, "FAILED");
				} finally {
					RAtomicLong counter = redisson.getAtomicLong("DONE." + tasklet.jobUUID());
					counter.incrementAndGet();
					LOGGER.debug("Completed Task {} for NoJob {} {} ", tasklet.getTaskId(), tasklet.jobUUID(),
							taskJob.getStatus());
				}
			} else {
				LOGGER.debug("Skipping Task {} for NoJob {}", tasklet.getTaskId(), tasklet.jobUUID());
			}

			if (ArgUtil.isEmpty(taskJob) || ArgUtil.isEmpty(taskJob.getStatus())
					|| taskJob.getStatus().ordinal() > JOB_STATUS.READING.ordinal()) {
				LOGGER.debug("Removing Task {} for Job {} {}", tasklet.getTaskId());
				taskStatus().fastRemove(taskUUID);
			}

		}
	}

	public void execute(int count) {
		for (int i = 0; i < count; i++) {
			this.execute();
		}
	}

}
