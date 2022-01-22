package com.boot.jx.tunnel.sys;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.boot.jx.AppConfigPackage;
import com.boot.jx.tunnel.DBEvent;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.TunnelService;

@TunnelEventMapping(topic = SysTunnelEventsDict.Names.SHARED_CONFIG_UPDATE)
public class SharedConfigManager implements ITunnelSubscriber<DBEvent> {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    TunnelService tunnelService;

    @Autowired
    AppConfigPackage appConfigPackage;

    @Override
    public void onMessage(String channel, DBEvent brokerEvent) {
	appConfigPackage.clear(brokerEvent.getData());
    }

    public void clear() {
	DBEvent e = new DBEvent();
	e.setEventCode(SysTunnelEventsDict.Names.SHARED_CONFIG_UPDATE);
	appConfigPackage.clear(e.getData());
	tunnelService.shout(SysTunnelEventsDict.Names.SHARED_CONFIG_UPDATE, e);
    }

}
