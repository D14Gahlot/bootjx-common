package com.boot.jx.common.config;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.boot.jx.rest.AjaxRestService;
import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils.TimePeriod;

@Component
public class RestHookService implements AjaxRestService {

	@Value("${app.webhook.connect.timeout}")
	private TimePeriod webhookConnectTimout;

	@Value("${app.webhook.read.timeout}")
	private TimePeriod webhookReadTimout;

	RestTemplate restTemplate;

	public RestTemplate restTemplate() {
		if (restTemplate == null) {
			CloseableHttpClient httpClient = HttpClients.custom().setMaxConnTotal(100) // Maximum total connections
					.setMaxConnPerRoute(10) // Maximum connections per route (per host)
					.build();
			HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
			requestFactory.setHttpClient(httpClient);
			requestFactory.setConnectTimeout(ArgUtil.parseAsInteger(webhookConnectTimout.toMillis())); // 3-5 seconds
			requestFactory.setReadTimeout(ArgUtil.parseAsInteger(webhookReadTimout.toMillis())); // 5 seconds
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
