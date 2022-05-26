package com.boot.jx.stomp;

import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.cache.CacheBox.StringCacheBox;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

@Component
@Service
public class StompTunnelSessionManager {

	private static final Logger LOGGER = LoggerFactory.getLogger(StompTunnelSessionManager.class);

	/*
	 * Map for <httpSessionId, stompUID>
	 */
//    public static final Map<String, String> http2sessionUIdMap = Collections
//	    .synchronizedMap(new HashMap<String, String>());
//    
	@Autowired
	private StringCacheBox http2GSessionIdMap;

//    public static final Map<String, String> http2stompUIdMap = Collections
//	    .synchronizedMap(new HashMap<String, String>());

	@Autowired
	private StringCacheBox http2stompUIdMap;

	/*
	 * Map for <wsSessionID, httpSessionId>
	 */
//    public static final Map<String, String> ws2httpMap = Collections.synchronizedMap(new HashMap<String, String>());

	@Autowired
	private StringCacheBox ws2xSessionMap;

	@Autowired
	private StringCacheBox ws2jSessionMap;

	/*
	 * Map for <stompUID, stompSession>
	 */
	@Autowired(required = false)
	StompSessionCache stompSessionCache;

	public static String getMSInstanceId() {
		return AppParam.APP_INSTANCE_ID.getValue();
	}

	public String createSessionMapping(String wsSessionID, String xSessionId, String jSessionId, String gSessionId) {
		if (ArgUtil.isEmpty(gSessionId)) {

			gSessionId = http2GSessionIdMap.getSafe(xSessionId);

			if (ArgUtil.isEmpty(gSessionId)) {
				gSessionId = http2GSessionIdMap.getSafe(jSessionId);
			}

			if (ArgUtil.isEmpty(gSessionId)) {
				gSessionId = String.format("%s-%s-%s", getMSInstanceId(), xSessionId, wsSessionID);
				http2GSessionIdMap.putSafe(xSessionId, gSessionId);
				http2GSessionIdMap.putSafe(jSessionId, gSessionId);
			}

		}
		ws2xSessionMap.put(wsSessionID, xSessionId);
		ws2jSessionMap.put(wsSessionID, jSessionId);
		return gSessionId;
	}

	/**
	 * Returns SessionUID for httpSessionId
	 * 
	 * @param xSessionId
	 * @return
	 */
	public String getSessionUId(String xSessionId, String jSessionId) {
		String sessionUId = http2GSessionIdMap.get(xSessionId);
		if (!ArgUtil.is(sessionUId)) {
			return http2GSessionIdMap.getSafe(jSessionId);
		}
		return sessionUId;
	}

	public void delinkWs2Http(String xSessionId, String jSessionId, String wsSessionID) {
		ws2xSessionMap.remove(wsSessionID);
		boolean isExists = false;

		if (ArgUtil.is(xSessionId)) {
			for (Entry<String, String> entry : ws2xSessionMap.readAllEntrySet()) {
				if (entry.getValue().equals(xSessionId)) {
					isExists = true;
				}
			}
			if (!isExists) {
				http2GSessionIdMap.remove(xSessionId);
			}
		}

		if (ArgUtil.is(jSessionId)) {
			ws2jSessionMap.remove(wsSessionID);
			isExists = false;
			for (Entry<String, String> entry : ws2jSessionMap.readAllEntrySet()) {
				if (entry.getValue().equals(jSessionId)) {
					isExists = true;
				}
			}
			if (!isExists) {
				http2GSessionIdMap.remove(jSessionId);
			}
		}
	}

	/**
	 * 
	 * @param stompUID   - only one session with one stompUID can exists, if you
	 *                   want to support multiple, change accordingly
	 * @param xSessionId
	 */
	public void mapHTTPSession(String stompUID, String xSessionId, String jSessionId, String... tags) {
		StompSession stompSession = new StompSession();
		stompSession.setPrefix(getMSInstanceId());
		stompSession.setXsessionId(xSessionId);
		stompSession.setJsessionId(jSessionId);

		if (tags != null && tags.length > 0) {
			String[] etags = new String[tags.length];
			for (int i = 0; i < tags.length; i++) {
				etags[i] = createTagId(tags[i]);
			}
			stompSession.setTags(etags);
		}
		stompSession.setTenantToken(createTagId(AppContextUtil.getTenant()));

		http2stompUIdMap.putSafe(xSessionId, stompUID);
		http2stompUIdMap.putSafe(jSessionId, stompUID);
		stompSessionCache.putSafe(stompUID, stompSession);
	}

	/**
	 * Create Stomp Session for User, Prefer with prefix E:21,C:1212,T:3435
	 * 
	 * @param stompUID
	 */
	public void registerUser(String stompUID) {
		mapHTTPSession(stompUID, AppContextUtil.getSessionId(true), AppContextUtil.getJSessionId());
	}

	public void registerUser(String stompUID, String... tags) {
		mapHTTPSession(stompUID, AppContextUtil.getSessionId(true), AppContextUtil.getJSessionId(), tags);
	}

	public StompSession getStompSession(String stompUID) {
		return stompSessionCache.get(stompUID);
	}

	public StompSession getStompSessionByHttpSessionId(String xSessionId, String jSessionId) {
		boolean bothIdEmpty = true;
		if (ArgUtil.is(xSessionId)) {
			bothIdEmpty = false;
			String stompUID = http2stompUIdMap.get(xSessionId);
			if (ArgUtil.is(stompUID)) {
				return stompSessionCache.get(stompUID);
			}
		}

		if (ArgUtil.is(jSessionId)) {
			bothIdEmpty = false;
			String stompUID = http2stompUIdMap.get(jSessionId);
			if (ArgUtil.is(stompUID)) {
				return stompSessionCache.get(stompUID);
			}
		}

		if (bothIdEmpty) {
			LOGGER.error("xSessionId & jSessionId both Null");
		}
		return null;
	}

	public String createTagId(String tag) {
		return tag + "-" + CryptoUtil.getHashBuilder().message(tag).secret("SOME_SECRET_TO_B_CHANGED_LATER")
				.toHmacSHA256().hash();
	}

}
