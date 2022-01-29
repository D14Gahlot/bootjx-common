package com.boot.jx.cache;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppParam;
import com.boot.jx.cache.test.RedisSampleCacheBox;
import com.boot.jx.def.IndicatorListner;
import com.boot.jx.tunnel.TunnelSubscriberFactory;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class MCQIndicator implements IndicatorListner {

	@Autowired
	TunnelSubscriberFactory tunnelSubscriberFactory;
	public static final Map<String, Object> statusMap = Collections.synchronizedMap(new HashMap<String, Object>());

	@Autowired
	RedisSampleCacheBox sampleTestCacheBox;

	@Autowired
	AppConfig appConfig;

	public static String TIMER = null;

	@Override
	public Map<String, Object> getIndicators(GaugeIndicator gauge) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("tunnel.listner", MCQIndicator.getStatus());

		Map<String, Object> cacheMap = new HashMap<String, Object>();
		cacheMap.put("status", "UP");
		

		if (ArgUtil.isEmpty(TIMER)) {
			TIMER = appConfig.getSpringAppName() + System.currentTimeMillis();
		}

		try {
			sampleTestCacheBox.get(TIMER);
			cacheMap.put("GET", "PASS");
			gauge.set("redis_get", GaugeIndicator.UP);
		} catch (Exception e) {
			cacheMap.put("GET", "FAIL");
			cacheMap.put("status", "DOWN");
			gauge.set("redis_get", GaugeIndicator.DOWN);
		}

		try {
			sampleTestCacheBox.remove(TIMER);
			cacheMap.put("DEL", "PASS");
			gauge.set("redis_del", GaugeIndicator.UP);
		} catch (Exception e) {
			cacheMap.put("DEL", "FAIL");
			cacheMap.put("status", "DOWN");
			gauge.set("redis_del", GaugeIndicator.DOWN);
		}

		TIMER = appConfig.getSpringAppName() + System.currentTimeMillis();

		try {
			sampleTestCacheBox.put(TIMER, "PASS");
			cacheMap.put("PUT", "PASS");
			gauge.set("redis_put", GaugeIndicator.UP);
		} catch (Exception e) {
			cacheMap.put("PUT", "FAIL");
			cacheMap.put("status", "DOWN");
			gauge.set("redis_put", GaugeIndicator.DOWN);
		}

		try {
			for (Entry<String, Object> iterable_element : statusMap.entrySet()) {
				String newKey = "tunnel_listner_" + iterable_element.getKey().replaceAll(".", "_") + "_ago";
				String strValue = ArgUtil.parseAsString(iterable_element.getValue());
				if (ArgUtil.is(strValue)) {
					Date d = TunnelSubscriberFactory.TS_FORMAT.parse(strValue);
					gauge.set(newKey, new Long(TimeUtils.timeSince(d.getTime())).doubleValue());
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		map.put("redis.cache", cacheMap);
		return map;
	}

	public static void messageException(String channel) {
		statusMap.put("channel." + channel + ".excep.ts", TunnelSubscriberFactory.TS_FORMAT.format(new Date()));
	}

	public static void messageIgnored(String channel) {
		statusMap.put("channel." + channel + ".ignrd.ts", TunnelSubscriberFactory.TS_FORMAT.format(new Date()));
	}

	public static void messageProcessed(String channel) {
		statusMap.put("channel." + channel + ".prcsd.ts", TunnelSubscriberFactory.TS_FORMAT.format(new Date()));
	}

	public static void messageRcvd(String channel) {
		statusMap.put("channel." + channel + ".rcvd.ts", TunnelSubscriberFactory.TS_FORMAT.format(new Date()));
	}

	public static Map<String, Object> getStatus() {
		return statusMap;
	}

	public static String messageSubscribed(String channel) {
		statusMap.put("channel." + channel + ".sub.ts", TunnelSubscriberFactory.TS_FORMAT.format(new Date()));
		return channel;
	}

}
