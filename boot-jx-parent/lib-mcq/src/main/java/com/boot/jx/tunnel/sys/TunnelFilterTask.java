package com.boot.jx.tunnel.sys;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.jx.tunnel.CommonTunnelEvent;
import com.boot.jx.tunnel.ITunnelDefs.TunnelFilter;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@TunnelEventMapping(topic = TunnelConstants.Events.TUNNEL_TASK_MASTER_UPDATE, scheme = TunnelEventXchange.TASK_LISTNER)
public class TunnelFilterTask implements ITunnelSubscriber<CommonTunnelEvent> {

	private Logger LOGGER = LoggerFactory.getLogger(TunnelFilterTask.class);

	@Autowired
	private TunnelFilter tunnelFilter;

	@PostConstruct
	public void init() {
		LOGGER.info("TunnelFilterTask init");
	}

	@Override
	public void onMessage(String channel, CommonTunnelEvent message) {
		if (ArgUtil.is(tunnelFilter)) {
			tunnelFilter.onMasterUpdate(message);
		}
	}

}
