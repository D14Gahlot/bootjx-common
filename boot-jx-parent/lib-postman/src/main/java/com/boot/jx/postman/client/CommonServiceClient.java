package com.boot.jx.postman.client;

import java.util.HashMap;
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
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.ChronoScheduler;
import com.boot.jx.tunnel.ITunnelService;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
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
	
	
	public java.util.Map<String, Object> getScheduleStatus(String schedule){
		 RestTemplate restTemplate = new RestTemplate();

	        String url = UriComponentsBuilder
	                .fromHttpUrl("https://demo.mehery.xyz/nexus/calendar/api/v1/orgSchedule/status"+schedule)
	                .encode()
	                .toUriString();
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("app-proxy-token", "iwPHDr0GZTuriUsijvf6g70AOFlPak541Y2fJQpSUhp8vYtT04gXQFSBCggkjFSR");
	        headers.set("x-agent-code", "lt");

	        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);

	        Map<String, Object> responseMap = new HashMap<>();
	        try {
	            ResponseEntity<String> response = restTemplate.exchange(
	                    url,
	                    HttpMethod.GET,
	                    requestEntity,
	                    String.class
	            );

	            LOGGER.info("Response", response.getBody());

	            ObjectMapper objectMapper = new ObjectMapper();
	            JsonNode rootNode = objectMapper.readTree(response.getBody());
	            rootNode.fields().forEachRemaining(entry -> {
	                responseMap.put(entry.getKey(), entry.getValue());
	            });

	        } catch (HttpClientErrorException e) {
	            LOGGER.error("Error", e.getResponseBodyAsString());
	            responseMap.put("error", e.getResponseBodyAsString());
	        } catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

	        return responseMap;
	    }
	
	
	
	public ChronoScheduler schedule(ChronoScheduler chronoTask) {
		if (ArgUtil.is(scheduler)) {
			MapModel resp =null;
			if(ArgUtil.is(chronoTask.getTopic()) && chronoTask.getTopic().equalsIgnoreCase("CANCELLED")) {
				String cancelUrl=null;
				try {
					cancelUrl= cronoJobUrl+"/scheduler/api/v1/job/tunnel/cancel";
					String instanceId = null;
					Map<String, Object> data =new HashMap<>();
					if(ArgUtil.is(chronoTask.getData())) {
						instanceId = (String)chronoTask.getData().get("jobId");
						data.put("instanceId", instanceId);
					
				    resp = restService.ajax(cancelUrl).postJson(data).asMapModel();
				    LOGGER.info("Res schedule -cancel:"+JsonUtil.toJson(resp)+"\n cancelUrl :"+cancelUrl);
				    if (resp != null && resp.get("status") != null) {
				    	Map<String, Object> dataMap = (Map<String, Object>) resp.get("status");
				    	String key =(String)dataMap.get("key");
				    	int code =(int)dataMap.get("code"); 
				    	if(key.equalsIgnoreCase("SUCCESS") || code==200) {
				    		 return chronoTask;
				    	}
				    }
				    }
				    return null;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return chronoTask;
	}
	

}
