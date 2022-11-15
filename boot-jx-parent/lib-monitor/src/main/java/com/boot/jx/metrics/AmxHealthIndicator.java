package com.boot.jx.metrics;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import com.boot.jx.def.IndicatorListner;
import com.boot.jx.def.IndicatorListner.GaugeIndicator;
import com.boot.utils.ArgUtil;

//import io.prometheus.client.Gauge;

@Component
public class AmxHealthIndicator extends AbstractHealthIndicator {

	@Autowired(required = false)
	List<IndicatorListner> listners;

//	private static final Gauge statusGauge = Gauge.build()
//			.name("amx_gauge")
//			.labelNames("status")
//			.help("Amx Custom Gauge Values").register();

	@Override
	protected void doHealthCheck(Health.Builder builder) throws Exception {
		// Use the builder to build the health status details that should be reported.
		// If you throw an exception, the status will be DOWN with the exception
		// message.
		builder.up();
		GaugeIndicator gaugeIndicator = new GaugeIndicator();
		if (!ArgUtil.isEmpty(listners)) {
			for (IndicatorListner eachListner : listners) {
				Map<String, Object> x = eachListner.getIndicators(gaugeIndicator);
				for (Entry<String, Object> indicatorListner : x.entrySet()) {
					builder.withDetail(indicatorListner.getKey(), indicatorListner.getValue());
				}
			}
//			
//			for (Entry<String, Double> gauge : gaugeIndicator.toMap().entrySet()) {
//				statusGauge.labels(gauge.getKey()).set(gauge.getValue());
//			}

		}
	}
}
