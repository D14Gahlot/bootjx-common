package com.boot.jx.postman.client;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpServerException;
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.ITunnelService;
import com.boot.model.MapModel;

@Component
public class CommonServiceClient {

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Value("${bootjx.tunnel.scheduler}")
	private String scheduler;

	@Value("${mry.chrono.url}")
	private String cronoJobUrl;

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
		restService.ajax(cronoJobUrl).path("/api/v1/on/domain/created").post(null).asNone();

	}

}
