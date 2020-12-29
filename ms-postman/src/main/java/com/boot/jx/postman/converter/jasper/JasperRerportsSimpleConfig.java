package com.boot.jx.postman.converter.jasper;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The Class JasperRerportsSimpleConfig.
 * 
 * @author lalittanwar
 *
 */
@Configuration
public class JasperRerportsSimpleConfig {

	/**
	 * Report filler.
	 *
	 * @return the simple report filler
	 */
	@Bean
	public SimpleReportFiller reportFiller() {
		return new SimpleReportFiller();
	}

	/**
	 * Report exporter.
	 *
	 * @return the simple report exporter
	 */
	@Bean
	public SimpleReportExporter reportExporter() {
		return new SimpleReportExporter();
	}

}