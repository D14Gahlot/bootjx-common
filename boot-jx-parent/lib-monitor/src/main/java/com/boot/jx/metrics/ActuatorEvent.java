package com.boot.jx.metrics;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import com.boot.jx.logger.AbstractEvent;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

@SuppressWarnings("rawtypes")
public class ActuatorEvent extends AbstractEvent {

	private static final long serialVersionUID = -6503726300332663101L;
	private SortedMap<String, Gauge> gauges;
	private SortedMap<String, Counter> counters;
	private SortedMap<String, Histogram> histograms;
	private SortedMap<String, Meter> meters;
	private SortedMap<String, AuditTimer> timers;

	public ActuatorEvent(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		this.setGauges(gauges);
		this.counters = counters;
		this.histograms = histograms;
		this.meters = meters;
		this.timers = cleanTimers(timers);
	}

	public SortedMap<String, Gauge> getGauges() {
		return gauges;
	}

	public void setGauges(SortedMap<String, Gauge> gauges2) {
		this.gauges = gauges2;
	}

	public SortedMap<String, Counter> getCounters() {
		return counters;
	}

	public void setCounters(SortedMap<String, Counter> counters) {
		this.counters = counters;
	}

	public SortedMap<String, Histogram> getHistograms() {
		return histograms;
	}

	public void setHistograms(SortedMap<String, Histogram> histograms) {
		this.histograms = histograms;
	}

	public SortedMap<String, Meter> getMeters() {
		return meters;
	}

	public void setMeters(SortedMap<String, Meter> meters) {
		this.meters = meters;
	}

	public SortedMap<String, AuditTimer> getTimers() {
		return timers;
	}

	public void setTimers(SortedMap<String, AuditTimer> timers) {
		this.timers = timers;
	}

	public static SortedMap<String, AuditTimer> cleanTimers(SortedMap<String, Timer> timers) {
		SortedMap<String, AuditTimer> newtimers = new TreeMap<String, AuditTimer>();
		for (Map.Entry<String, Timer> timerEntry : timers.entrySet()) {
			newtimers.put(timerEntry.getKey(), new AuditTimer(timerEntry.getValue()));
		}
		return newtimers;
	}

	@Override
	public void clean() {
		// TODO Auto-generated method stub
	}
}
