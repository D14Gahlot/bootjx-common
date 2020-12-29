package com.boot.jx.stomp;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.boot.jx.AppContextUtil;
import com.boot.jx.stomp.StompSessionCache.StompSession;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;

@Component
@Service
public class StompTunnelSessionManager {

	/*
	 * Map for <httpSessionId, stompUID>
	 */
	public static final Map<String, String> http2stompUIdMap = Collections
			.synchronizedMap(new HashMap<String, String>());

	/*
	 * Map for <wsSessionID, httpSessionId>
	 */
	public static final Map<String, String> ws2httpMap = Collections.synchronizedMap(new HashMap<String, String>());

	/*
	 * Map for <stompUID, stompSession>
	 */
	@Autowired(required = false)
	StompSessionCache stompSessionCache;

	public static String getSystemPrefix() {
		return UniqueID.PREF;
	}

	public String createSessionMapping(String wsSessionID, String httpSessionId, String sessionUID) {
		if (ArgUtil.isEmpty(sessionUID)) {
			sessionUID = http2stompUIdMap.get(httpSessionId);
			if (ArgUtil.isEmpty(sessionUID)) {
				sessionUID = String.format("%s-%s-%s", getSystemPrefix(), httpSessionId, wsSessionID);
				http2stompUIdMap.put(httpSessionId, sessionUID);
			}
		}
		ws2httpMap.put(wsSessionID, httpSessionId);
		return sessionUID;
	}

	/**
	 * Returns SessionUID for httpSessionId
	 * 
	 * @param httpSessionId
	 * @return
	 */
	public String getSessionUId(String httpSessionId) {
		return http2stompUIdMap.get(httpSessionId);
	}

	public void delinkWs2Http(String httpSessionId, String wsSessionID) {
		ws2httpMap.remove(wsSessionID);
		boolean isExists = false;
		for (Entry<String, String> entry : ws2httpMap.entrySet()) {
			if (entry.getValue().equals(httpSessionId)) {
				isExists = true;
			}
		}
		if (!isExists) {
			http2stompUIdMap.remove(httpSessionId);
		}
	}

	/**
	 * 
	 * @param stompUID      - only one session with one stompUID can exists, if you
	 *                      want to support multiple, change accordingly
	 * @param httpSessionId
	 */
	public void mapHTTPSession(String stompUID, String httpSessionId) {
		StompSession stompSession = new StompSession();
		stompSession.setPrefix(getSystemPrefix());
		stompSession.setHttpSessionId(httpSessionId);
		stompSessionCache.put(stompUID, stompSession);
	}

	/**
	 * Create Stomp Session for User, Prefer with prefix E:21,C:1212,T:3435
	 * 
	 * @param stompUID
	 */
	public void registerUser(String stompUID) {
		mapHTTPSession(stompUID, AppContextUtil.getSessionId(true));
	}

	public StompSession getStompSession(String stompUID) {
		return stompSessionCache.get(stompUID);
	}

}
