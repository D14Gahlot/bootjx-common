package com.boot.jx.mcq;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.types.Expiration;

import com.boot.jx.mcq.shedlock.LockConfiguration;

public class MCQLockProvider {

	private MCQLocker mcq;

	public static Candidate getCandidate(LockConfiguration lockConfiguration) {
		long maxExpiration = Math.max(0,
				getExpiration(lockConfiguration.getLockAtMostUntil()).getExpirationTimeInMilliseconds());
		long minExpiration = Math.max(1000,
				getExpiration(lockConfiguration.getLockAtLeastUntil()).getExpirationTimeInMilliseconds());

		if (maxExpiration == 0L) {
			maxExpiration = minExpiration + 120000;
		}

		Candidate candidate = new Candidate()
				.fixedDelay(Math.min(minExpiration, maxExpiration))
				.maxAge(Math.max(minExpiration, maxExpiration))
				.queue(lockConfiguration.getName());

		return candidate;
	}

	public static Expiration getExpiration(Instant until) {
		return Expiration.from(getMsUntil(until), TimeUnit.MILLISECONDS);
	}

	public static long getMsUntil(Instant until) {
		return Duration.between(Instant.now(), until).toMillis();
	}

}
