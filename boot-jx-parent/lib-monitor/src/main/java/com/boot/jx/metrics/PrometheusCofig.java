package com.boot.jx.metrics;

import org.springframework.context.annotation.Configuration;

import io.prometheus.client.spring.boot.EnablePrometheusEndpoint;
import io.prometheus.client.spring.boot.EnableSpringBootMetricsCollector;

@Configuration
@EnablePrometheusEndpoint
@EnableSpringBootMetricsCollector
public class PrometheusCofig {

}
