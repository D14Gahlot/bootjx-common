package com.boot.jx.tunnel.sys;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppSharedConfig;
import com.boot.jx.tunnel.DBEvent;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.utils.ArgUtil;

@TunnelEventMapping(topic = SysTunnelEventsDict.Names.SHARED_CONFIG_UPDATE)
public class SharedConfigSubscriber implements ITunnelSubscriber<DBEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired(required = false)
	private List<AppSharedConfig> listAppSharedConfig;

	@Override
	public void onMessage(String channel, DBEvent brokerEvent) {
		if (ArgUtil.is(listAppSharedConfig)) {
			for (AppSharedConfig appSharedConfig : listAppSharedConfig) {
				appSharedConfig.clear(brokerEvent.getData());
			}
		}
	}

}
