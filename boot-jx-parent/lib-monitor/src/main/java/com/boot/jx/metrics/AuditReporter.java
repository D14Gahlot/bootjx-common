package com.boot.jx.metrics;

import java.util.SortedMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.boot.jx.logger.AbstractEvent.EventMarker;
import com.boot.jx.logger.AuditService;
import com.boot.jx.logger.client.AuditServiceClient;
import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;

/**
 * A reporter which outputs measurements to a {@link AuditService}, like
 * {@code System.out}.
 */
public class AuditReporter extends ScheduledReporter {

	private static final Marker metermarker = MarkerFactory.getMarker(EventMarker.METER.toString());

	/**
	 * Returns a new {@link Builder} for {@link ConsoleReporter}.
	 *
	 * @param registry the registry to report
	 * @return a {@link Builder} instance for a {@link ConsoleReporter}
	 */
	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	/**
	 * A builder for {@link ConsoleReporter} instances. Defaults to using the
	 * default locale and time zone, writing to {@code System.out}, converting rates
	 * to events/second, converting durations to milliseconds, and not filtering
	 * metrics.
	 */
	public static class Builder {
		private final MetricRegistry registry;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
		}

		/**
		 * Convert rates to the given time unit.
		 *
		 * @param rateUnit a unit of time
		 * @return {@code this}
		 */
		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		/**
		 * Convert durations to the given time unit.
		 *
		 * @param durationUnit a unit of time
		 * @return {@code this}
		 */
		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		/**
		 * Only report metrics which match the given filter.
		 *
		 * @param filter a {@link MetricFilter}
		 * @return {@code this}
		 */
		public Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		/**
		 * Builds a {@link ConsoleReporter} with the given properties.
		 *
		 * @return a {@link ConsoleReporter}
		 */
		public AuditReporter build() {
			return new AuditReporter(registry, rateUnit, durationUnit, filter);
		}
	}

	private AuditReporter(MetricRegistry registry, TimeUnit rateUnit, TimeUnit durationUnit, MetricFilter filter) {
		super(registry, "console-reporter", filter, rateUnit, durationUnit);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		AuditServiceClient.logAbstractEvent(metermarker,
				new ActuatorEvent(gauges, counters, histograms, meters, timers), false);
	}

}
