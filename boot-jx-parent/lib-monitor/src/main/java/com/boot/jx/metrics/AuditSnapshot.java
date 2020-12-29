package com.boot.jx.metrics;

import java.io.OutputStream;

import com.codahale.metrics.Snapshot;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class AuditSnapshot extends Snapshot {

	@JsonIgnore
	Snapshot snapshot;

	public AuditSnapshot(Snapshot snapshot) {
		this.snapshot = snapshot;
	}

	@Override
	public double getValue(double quantile) {
		return snapshot.getValue(quantile);
	}

	@Override
	public long[] getValues() {
		return null;
	}

	@Override
	public int size() {
		return snapshot.size();
	}

	@Override
	public long getMax() {
		return snapshot.getMax();
	}

	@Override
	public double getMean() {
		return snapshot.getMean();
	}

	@Override
	public long getMin() {
		return snapshot.getMin();
	}

	@Override
	public double getStdDev() {
		return snapshot.getStdDev();
	}

	@Override
	public void dump(OutputStream output) {
	}

}
