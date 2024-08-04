package com.boot.jx.tunnel.sys;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContext;
import com.boot.jx.tunnel.ChronoTask;
import com.boot.jx.tunnel.CommonTunnelEvent;
import com.boot.jx.tunnel.ITunnelDefs.TunnelFilter;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;

@Component
public class TunnelFilterManager {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelFilterManager.class);

	@Value("${bootjx.tunnel.cross.url}")
	private String crossUrl;

	@Autowired(required = false)
	private TunnelFilter tunnelFilter;

	@Autowired
	@Lazy
	private TunnelService tunnelService;

	public void postSubscriptions(List<String> topics) {
		LOGGER.info("TunnelFilterManager:postSubscriptions for tunnel Events");
		try {
			if (ArgUtil.is(tunnelFilter)) {
				if (tunnelFilter.postSubscriptions(topics)) {
					onMasterUpdate();
				}
			}
		} catch (Exception e) {
			LOGGER.error("While calling postSubscriptions", e);
		}
	}

	/**
	 * 
	 * @param <T>
	 * @param topic
	 * @param messagePayload
	 * @param context
	 * @return
	 */
	public <T> boolean beforeTaskPublish(String topic, T messagePayload, AppContext context) {
		if (ArgUtil.is(tunnelFilter)) {
			return tunnelFilter.beforeTaskPublish(topic, messagePayload, context);
		}
		return true;
	}

	@PostConstruct
	public void init() {
		LOGGER.info("TunnelFilterManager init");
	}

	@Async
	public <T> void afterTaskPublish(String topic, T messagePayload, AppContext context) {
		if (ArgUtil.is(tunnelFilter)) {
			tunnelFilter.afterTaskPublic(topic, messagePayload, context);
		}
	}

	public void onMasterUpdate() {
		tunnelService.task(TunnelConstants.Events.TUNNEL_TASK_MASTER_UPDATE, new CommonTunnelEvent());
	}

	public void onServiceInit() {
		// tunnelService.task(TunnelConstants.Events.TUNNEL_SERVICE_INIT, new
		// CommonTunnelEvent());
	}

	public ChronoTask schedule(ChronoTask chronoTask) {
		return tunnelFilter.schedule(chronoTask);
	}
}
