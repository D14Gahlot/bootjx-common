package com.boot.jx.cache.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.cache.test.RedisSampleTxCacheBox.RedisSampleData;
import com.boot.jx.tunnel.ITunnelSubscriber;
import com.boot.jx.tunnel.TunnelEventMapping;
import com.boot.jx.tunnel.sys.SysTunnelEventsDict;
import com.boot.utils.JsonUtil;

@TunnelEventMapping(topic = SysTunnelEventsDict.Names.TEST_TOPIC)
public class RedisSampleSubscriber implements ITunnelSubscriber<RedisSampleData> {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Override
	public void onMessage(String channel, RedisSampleData msg) {
		LOGGER.info("======onMessage1==={} ====  {}", channel,
				JsonUtil.toJson(msg));
	}

}
