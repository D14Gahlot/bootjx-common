package com.boot.jx.metrics;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.codahale.metrics.MetricRegistry;
import com.ryantenney.metrics.spring.config.annotation.EnableMetrics;
import com.ryantenney.metrics.spring.config.annotation.MetricsConfigurerAdapter;

@Configuration
@EnableMetrics(proxyTargetClass = true)
@PropertySource("classpath:monitor.properties")
@ConditionalOnProperty("app.monitor")
public class MetricsConfig extends MetricsConfigurerAdapter {

	@Override
	public void configureReporters(MetricRegistry metricRegistry) {
		// registerReporter allows the MetricsConfigurerAdapter to
		// shut down the reporter when the Spring context is closed
		//registerReporter(AuditReporter.forRegistry(metricRegistry).build()).start(15, TimeUnit.MINUTES);
	}

	// @Bean
	// public PoolingHttpClientConnectionManager
	// poolingHttpClientConnectionManager() {
	// // this will be used to demonstrate using a Gauge. in a real service we would
	// // externalize the
	// // configuration and use create a CloseableHttpClient that uses this pool
	// // manager, etc.
	// PoolingHttpClientConnectionManager connMgr = new
	// PoolingHttpClientConnectionManager();
	// connMgr.setMaxTotal(200);
	// connMgr.setDefaultMaxPerRoute(20);
	// return connMgr;
	// }
}
