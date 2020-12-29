package com.boot.jx.mcq;

import static java.time.temporal.ChronoUnit.MILLIS;

import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.util.StringValueResolver;

import com.boot.jx.mcq.shedlock.LockConfiguration;
import com.boot.jx.mcq.shedlock.LockableRunnable;

@Configuration
public class MCQSchedulerConfig implements SchedulingConfigurer, EmbeddedValueResolverAware {

	private final int POOL_SIZE = 10;

	public static final String EXECUTER_TASK = "task";

	@Autowired
	MCQLocker locker;

	private StringValueResolver embeddedValueResolver;

	@Override
	public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
		ThreadPoolTaskScheduler threadPoolTaskScheduler = new CustomTaskScheduler(
				new MCQLockConfigExtractor(
						Duration.of(10000, MILLIS), Duration.of(10000, MILLIS),
						embeddedValueResolver),
				locker);
		threadPoolTaskScheduler.setPoolSize(POOL_SIZE);
		threadPoolTaskScheduler.setThreadNamePrefix(EXECUTER_TASK + "-");
		threadPoolTaskScheduler.initialize();
		scheduledTaskRegistrar.setTaskScheduler(threadPoolTaskScheduler);
	}

	public static class CustomTaskScheduler extends ThreadPoolTaskScheduler {

		private static final long serialVersionUID = 495107613921116994L;

		private MCQLockConfigExtractor configExtractor;
		private MCQLocker locker;

		public CustomTaskScheduler(MCQLockConfigExtractor configExtractor, MCQLocker locker) {
			this.configExtractor = configExtractor;
			this.locker = locker;
		}

		@Override
		public ScheduledFuture<?> schedule(Runnable task, Date startTime) {
			Optional<LockConfiguration> optionalConfig = configExtractor.getLockConfiguration(task);
			if (optionalConfig.isPresent()) {
				Candidate candidate = MCQLockProvider.getCandidate(optionalConfig.get());
				return super.schedule(
						new LockableRunnable(task, locker, candidate),
						startTime);
			}
			return super.schedule(task, startTime);
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long period) {
			Optional<LockConfiguration> optionalConfig = configExtractor.getLockConfiguration(task);
			if (optionalConfig.isPresent()) {
				Candidate candidate = MCQLockProvider.getCandidate(optionalConfig.get());
				return super.scheduleAtFixedRate(
						new LockableRunnable(task, locker, candidate),
						period);
			}
			return super.scheduleAtFixedRate(task, period);
		}

		@Override
		public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, Date startTime, long period) {
			Optional<LockConfiguration> optionalConfig = configExtractor.getLockConfiguration(task);
			if (optionalConfig.isPresent()) {
				Candidate candidate = MCQLockProvider.getCandidate(optionalConfig.get());
				return super.scheduleAtFixedRate(
						new LockableRunnable(task, locker, candidate),
						period);
			}
			return super.scheduleAtFixedRate(task, startTime, period);
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, long period) {
			Optional<LockConfiguration> optionalConfig = configExtractor.getLockConfiguration(task);
			if (optionalConfig.isPresent()) {
				Candidate candidate = MCQLockProvider.getCandidate(optionalConfig.get());
				return super.scheduleWithFixedDelay(
						new LockableRunnable(task, locker, candidate),
						period);
			}
			return super.scheduleWithFixedDelay(task, period);
		}

		@Override
		public ScheduledFuture<?> scheduleWithFixedDelay(Runnable task, Date startTime, long period) {
			Optional<LockConfiguration> optionalConfig = configExtractor.getLockConfiguration(task);
			if (optionalConfig.isPresent()) {
				Candidate candidate = MCQLockProvider.getCandidate(optionalConfig.get());
				return super.scheduleWithFixedDelay(
						new LockableRunnable(task, locker, candidate),
						period);
			}
			return super.scheduleWithFixedDelay(task, startTime, period);
		}
	}

	@Override
	public void setEmbeddedValueResolver(StringValueResolver resolver) {
		this.embeddedValueResolver = resolver;
	}
}
