package com.boot.jx.tunnel.sys;

import com.boot.jx.tunnel.ITunnelEventsDict;

public enum SysTunnelEventsDict implements ITunnelEventsDict {

	TEST_TOPIC(Names.TEST_TOPIC),
	SHARED_CONFIG_UPDATE(Names.SHARED_CONFIG_UPDATE);

	public static final class Names {
		public static final String SHARED_CONFIG_UPDATE = "SHARED_CONFIG_UPDATE";
		public static final String TEST_TOPIC = "TEST_TOPIC";
	}

	String eventName;

	SysTunnelEventsDict(String eventName) {
		this.eventName = eventName;
	}

	@Override
	public String getEventCode() {
		return this.eventName;
	}
}
