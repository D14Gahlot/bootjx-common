package com.boot.jx.tunnel.sys;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppConfigPackage;
import com.boot.jx.AppConfigPackage.AppSharedConfigChange;
import com.boot.jx.tunnel.DBEvent;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelEventXchange;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.JsonUtil;

@TunnelEventMapping(topic = TunnelConstants.Events.SHARED_CONFIG_UPDATE, scheme = TunnelEventXchange.SHOUT_LISTNER,
		integrity = false)
public class SharedConfigManager implements ITunnelSubscriber<DBEvent> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	TunnelService tunnelService;

	@Autowired
	AppConfigPackage appConfigPackage;

	@Override
	public void onMessage(String channel, DBEvent brokerEvent) {
		Map<String, String> data = brokerEvent.getData();

		if (data != null) {
			AppSharedConfigChange change = JsonUtil.toObject(JsonUtil.toJsonMap(data), AppSharedConfigChange.class);
			appConfigPackage.clear(change);
		}
		appConfigPackage.clear(new AppSharedConfigChange());
	}

	public void clear(AppSharedConfigChange change) {
		DBEvent e = new DBEvent();
		e.setData(JsonUtil.toStringMap(change));
		e.setEventCode(TunnelConstants.Events.SHARED_CONFIG_UPDATE);
		appConfigPackage.clear(change);
		tunnelService.shout(TunnelConstants.Events.SHARED_CONFIG_UPDATE, e);
	}

	public void clear() {
		DBEvent e = new DBEvent();
		e.setEventCode(TunnelConstants.Events.SHARED_CONFIG_UPDATE);
		appConfigPackage.clear(new AppSharedConfigChange());
		tunnelService.shout(TunnelConstants.Events.SHARED_CONFIG_UPDATE, e);
	}

}
