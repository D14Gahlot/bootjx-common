package com.boot.jx.metrics;

import com.codahale.metrics.Metered;
import com.codahale.metrics.Sampling;
import com.codahale.metrics.Snapshot;
import com.codahale.metrics.Timer;

public class AuditTimer implements Metered, Sampling {

	private Timer timer;
	private Snapshot snapshot;

	public AuditTimer(Timer timer) {
		this.timer = timer;
		this.snapshot = new AuditSnapshot(timer.getSnapshot());
	}

	@Override
	public Snapshot getSnapshot() {
		return snapshot;
	}

	@Override
	public long getCount() {
		return timer.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return timer.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return timer.getFiveMinuteRate();
	}

	@Override
	public double getMeanRate() {
		return timer.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return timer.getOneMinuteRate();
	}

}
