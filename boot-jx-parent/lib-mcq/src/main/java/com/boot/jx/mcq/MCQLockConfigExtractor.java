package com.boot.jx.mcq;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Objects.requireNonNull;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAmount;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import com.boot.jx.mcq.shedlock.LockConfiguration;
import com.boot.jx.mcq.shedlock.SchedulerLock;
import com.boot.jx.mcq.shedlock.SchedulerLock.LockContext;
import com.boot.utils.ArgUtil;

public class MCQLockConfigExtractor {
	private final TemporalAmount defaultLockAtMostFor;
	private final TemporalAmount defaultLockAtLeastFor;
	private final StringValueResolver embeddedValueResolver;
	private final Logger logger = LoggerFactory.getLogger(MCQLockConfigExtractor.class);

	public MCQLockConfigExtractor(TemporalAmount defaultLockAtMostFor, TemporalAmount defaultLockAtLeastFor,
			StringValueResolver embeddedValueResolver) {
		this.defaultLockAtMostFor = requireNonNull(defaultLockAtMostFor);
		this.defaultLockAtLeastFor = requireNonNull(defaultLockAtLeastFor);
		this.embeddedValueResolver = embeddedValueResolver;
	}

	public Optional<LockConfiguration> getLockConfiguration(Runnable task) {
		if (task instanceof ScheduledMethodRunnable) {
			ScheduledMethodRunnable scheduledMethodRunnable = (ScheduledMethodRunnable) task;
			return getLockConfiguration(scheduledMethodRunnable.getTarget(), scheduledMethodRunnable.getMethod());
		} else {
			logger.debug("Unknown task type " + task);
		}
		return Optional.empty();
	}

	public Optional<LockConfiguration> getLockConfiguration(Object target, Method method) {
		SchedulerLock annotation = findAnnotation(target, method);
		if (shouldLock(annotation)) {
			Scheduled annotationScheduled = findAnnotationScheduled(target, method);
			String alterNateName = null;
			if (LockContext.BY_CLASS.equals(annotation.context())) {
				alterNateName = String.format("%s", getClassName(target));
			} else { // LockContext.BY_METHOD
				alterNateName = String.format("%s#%s", getClassName(target),
						method.getName());
			}
			return Optional.of(getLockConfiguration(annotation, annotationScheduled, alterNateName));
		} else {
			return Optional.empty();
		}
	}

	public String getClassName(Object target) {
		return target.getClass().getName().split("\\$")[0];
	}

	public LockConfiguration getLockConfiguration(SchedulerLock annotation, Scheduled annotationScheduled,
			String alterNateName) {
		Instant now = now();
		return new LockConfiguration(
				ArgUtil.nonEmpty(getName(annotation), alterNateName),
				now.plus(getLockAtMostFor(annotation, annotationScheduled)),
				now.plus(getLockAtLeastFor(annotation, annotationScheduled)),
				annotation.tenent());
	}

	private String getName(SchedulerLock annotation) {
		if (embeddedValueResolver != null) {
			return embeddedValueResolver.resolveStringValue(annotation.name());
		} else {
			return annotation.name();
		}
	}

	TemporalAmount getLockAtMostFor(SchedulerLock annotation, Scheduled annotationScheduled) {
		return getValue(
				annotation.lockMaxAge(),
				annotation.lockMaxAgeString(),
				this.defaultLockAtMostFor,
				"lockMaxAgeString");
	}

	TemporalAmount getLockAtLeastFor(SchedulerLock annotation, Scheduled annotationScheduled) {
		return getValue(
				(annotation.lockFixedDelay() < 0) ? annotationScheduled.fixedDelay() : annotation.lockFixedDelay(),
				ArgUtil.isEmpty(annotation.lockFixedDelayString()) ? annotationScheduled.fixedDelayString()
						: annotation.lockFixedDelayString(),
				this.defaultLockAtLeastFor,
				"lockFixedDelayString");
	}

	private TemporalAmount getValue(long valueFromAnnotation, String stringValueFromAnnotation,
			TemporalAmount defaultValue, final String paramName) {
		if (valueFromAnnotation >= 0) {
			return Duration.of(valueFromAnnotation, MILLIS);
		} else if (StringUtils.hasText(stringValueFromAnnotation)) {
			if (embeddedValueResolver != null) {
				stringValueFromAnnotation = embeddedValueResolver.resolveStringValue(stringValueFromAnnotation);
			}
			try {
				return Duration.of(Long.valueOf(stringValueFromAnnotation), MILLIS);
			} catch (NumberFormatException nfe) {
				try {
					return Duration.parse(stringValueFromAnnotation);
				} catch (DateTimeParseException e) {
					throw new IllegalArgumentException("Invalid " + paramName + " value \"" + stringValueFromAnnotation
							+ "\" - cannot parse into long nor duration");
				}
			}
		} else {
			return defaultValue;
		}
	}

	SchedulerLock findAnnotation(Object target, Method method) {
		SchedulerLock annotation = AnnotatedElementUtils.getMergedAnnotation(method, SchedulerLock.class);
		if (annotation != null) {
			return annotation;
		} else {
			// Try to find annotation on proxied class
			Class<?> targetClass = AopUtils.getTargetClass(target);
			if (targetClass != null && !target.getClass().equals(targetClass)) {
				try {
					Method methodOnTarget = targetClass
							.getMethod(method.getName(), method.getParameterTypes());
					return AnnotatedElementUtils.getMergedAnnotation(methodOnTarget, SchedulerLock.class);
				} catch (NoSuchMethodException e) {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	Scheduled findAnnotationScheduled(Object target, Method method) {
		Scheduled annotation = AnnotatedElementUtils.getMergedAnnotation(method, Scheduled.class);
		if (annotation != null) {
			return annotation;
		} else {
			// Try to find annotation on proxied class
			Class<?> targetClass = AopUtils.getTargetClass(target);
			if (targetClass != null && !target.getClass().equals(targetClass)) {
				try {
					Method methodOnTarget = targetClass
							.getMethod(method.getName(), method.getParameterTypes());
					return AnnotatedElementUtils.getMergedAnnotation(methodOnTarget, Scheduled.class);
				} catch (NoSuchMethodException e) {
					return null;
				}
			} else {
				return null;
			}
		}
	}

	private boolean shouldLock(SchedulerLock annotation) {
		return annotation != null;
	}
}
