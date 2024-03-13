package com.boot.jx.common.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RestService.Ajax;

@Component
public class RestHookService {

	RestTemplate restTemplate;

	public RestTemplate restTemplate() {
		if (restTemplate == null) {
			CloseableHttpClient httpClient = HttpClients.custom().setMaxConnTotal(100) // Maximum total connections
					.setMaxConnPerRoute(10) // Maximum connections per route (per host)
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			requestFactory.setConnectTimeout(3000); // 5 seconds
			requestFactory.setReadTimeout(5000); // 10 seconds

			restTemplate = new RestTemplate(requestFactory);
			restService.getLocalRestTemplate(restTemplate);
		}

		return restTemplate;
	}

	@Autowired
	RestService restService;

	public Ajax ajax(String url) {
		return restService.ajax(restTemplate(), url);
	}

}
