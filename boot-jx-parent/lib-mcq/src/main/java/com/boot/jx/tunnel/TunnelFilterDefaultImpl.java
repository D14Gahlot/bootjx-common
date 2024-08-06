package com.boot.jx.tunnel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContext;
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.ITunnelDefs.TunnelFilter;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Service
@ConditionalOnProperty(name = "bootjx.tunnel.filter", havingValue = "default")
public class TunnelFilterDefaultImpl implements TunnelFilter {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelFilterDefaultImpl.class);

	Map<String, String> myTopics = null;

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Value("${bootjx.tunnel.scheduler}")
	private String scheduler;

	@Autowired
	private RestService restService;

	@Autowired
	private AppConfig appConfig;

	@Override
	public boolean postSubscriptions(List<String> topics) {
		for (String topic : topics) {
			LOGGER.info("TunnelFilterDefaultImpl postSubscriptions:{}", topic);
		}
		return false;
	}

	@Override
	public <T> boolean beforeTaskPublish(String topic, T messagePayload, AppContext context) {
		return true;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("TunnelFilterDefaultImpl init");
	}

	@Override
	public <T> void afterTaskPublic(String topic, T messagePayload, AppContext context) {
		if (myTopics.containsKey(topic) && ArgUtil.is(crossUrl)) {
			TunnelMessage<Object> t = new TunnelMessage<Object>(new HashMap<String, Object>());
			t.setAppType(appConfig.getAppType());
			t.setContext(context);
			t.setTopic(topic);
			t.setData(messagePayload);
			restService.ajax(crossUrl).postJson(t).asNone();
		}
	}

	@Override
	public void onMasterUpdate(TunnelEvent message) {
		LOGGER.info("======onMasterUpdate==={}", JsonUtil.toJson(message));
		this.myTopics = null;
	}

	@Override
	public ChronoTask schedule(ChronoTask chronoTask) {
		if (ArgUtil.is(scheduler)) {
			MapModel resp = restService.ajax(scheduler).postJson(chronoTask).asMapModel();
			MapPathEntry id = resp.keyEntry("id");
			if (id.exists()) {
				chronoTask.setTaskId(id.asString());
			}
		}
		return chronoTask;
	}
}
