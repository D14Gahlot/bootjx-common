package com.boot.jx.postman.client;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpServerException;
import com.boot.jx.postman.model.ext.SessionBoundEvent;
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.ChronoScheduler;
import com.boot.jx.tunnel.ITunnelService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class CommonServiceClient {
	private Logger LOGGER = LoggerFactory.getLogger(CommonServiceClient.class);

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Value("${bootjx.tunnel.scheduler}")
	private String scheduler;

	@Value("${mry.chrono.url}")
	private String cronoJobUrl;

	@Value("${bootjx.tunnel.calender}")
	private String calenderApiUrl;

	@Value("${app.proxy.token}")
	private String appProxyToken;

	@Autowired
	RestService restService;

	@Autowired
	AppConfig appConfig;

	@Autowired
	private ITunnelService tunnelService;

	@Async
	@Retryable(value = ApiHttpServerException.class, maxAttempts = 3, backoff = @Backoff(delay = 3000))
	public void publishDomainCreatedEvent(String version) {
		Map<String, Object> domainCreatedInfo = MapModel.createInstance() //
				.put("domain", AppContextUtil.getTenant()) //
				.put("env", AppContextUtil.getEnv()) //
				.put("version", version) //
				.toMap();
		tunnelService.task("DOMAIN_CREATED", domainCreatedInfo);
		restService.ajax(cronoJobUrl).path("/api/v1/on/domain/created")
				.post(null).asNone();
	}

	@Async
	@Retryable(value = ApiHttpServerException.class, maxAttempts = 3, backoff = @Backoff(delay = 3000))
	public void publishSessionBoundEvent(SessionBoundEvent event) {
		if (ArgUtil.is(event.getTriggerType(),
				SessionBoundEvent.TRIGGER_TYPE.STATUS)) {
			restService.ajax(cronoJobUrl)
					.path("/session-event-timer/api/v1/message/status")
					.post(null).asNone();
		} else if (ArgUtil.is(event.getTriggerType(),
				SessionBoundEvent.TRIGGER_TYPE.MESSAGE)) {
			restService.ajax(cronoJobUrl)
					.path("/session-event-timer/api/v1/message/in-out")
					.post(null).asNone();
		}
	}

	@SuppressWarnings("unchecked")
	public List<HashMap<String, Object>> getScheduleStatus(String schedule) {
		try {
			Map<String, Object> response = restService.ajax(calenderApiUrl)
					.queryParam("code", schedule)
					.header("app-proxy-token", appProxyToken)
					.header("x-agent-code", "lt").get().asMap();

			@SuppressWarnings("unchecked")
			List<HashMap<String, Object>> apiResponse = (List<HashMap<String, Object>>) response
					.get("results");

			if (apiResponse != null && !apiResponse.isEmpty()) {
				LOGGER.info("Response of Api", apiResponse);
			} else {
				LOGGER.warn("No resposne found");
			}

			return apiResponse != null ? apiResponse : Collections.emptyList();
		} catch (Exception e) {
			LOGGER.error("Error: ", e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	public ChronoScheduler schedule(ChronoScheduler chronoTask) {
		if (ArgUtil.is(scheduler)) {
			MapModel resp = null;
			if (ArgUtil.is(chronoTask.getTopic())
					&& chronoTask.getTopic().equalsIgnoreCase("CANCELLED")) {
				String cancelUrl = null;
				try {
					cancelUrl = cronoJobUrl
							+ "/scheduler/api/v1/job/tunnel/cancel";
					String instanceId = null;
					Map<String, Object> data = new HashMap<>();
					if (ArgUtil.is(chronoTask.getData())) {
						instanceId = (String) chronoTask.getData().get("jobId");
						data.put("instanceId", instanceId);

						resp = restService.ajax(cancelUrl).postJson(data)
								.asMapModel();
						LOGGER.info(
								"Res schedule -cancel:" + JsonUtil.toJson(resp)
										+ "\n cancelUrl :" + cancelUrl);
						if (resp != null && resp.get("status") != null) {
							Map<String, Object> dataMap = (Map<String, Object>) resp
									.get("status");
							String key = (String) dataMap.get("key");
							int code = (int) dataMap.get("code");
							if (key.equalsIgnoreCase("SUCCESS")
									|| code == 200) {
								return chronoTask;
							}
						}
					}
					return null;
				} catch (Exception e) {
					LOGGER.error("Error while cancelling schdulers", e);
				}
			}
		}
		return chronoTask;
	}

}
