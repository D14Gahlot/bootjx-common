package com.boot.jx.tunnel;

import java.util.List;

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

@Service
@ConditionalOnProperty(name = "bootjx.tunnel.filter", havingValue = "default")
public class TunnelFilterDefaultImpl implements TunnelFilter {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelFilterDefaultImpl.class);

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Autowired
	RestService restService;

	@Autowired
	AppConfig appConfig;

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

	}

	@Override
	public void onMasterUpdate(TunnelEvent message) {
		// TODO Auto-generated method stub

	}

	@Override
	public ChronoTask schedule(ChronoTask chronoTask) {
		return chronoTask;
	}
}
