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
    private StringCacheBox http2sessionUIdMap;

//    public static final Map<String, String> http2stompUIdMap = Collections
//	    .synchronizedMap(new HashMap<String, String>());

    @Autowired
    private StringCacheBox http2stompUIdMap;

    /*
     * Map for <wsSessionID, httpSessionId>
     */
//    public static final Map<String, String> ws2httpMap = Collections.synchronizedMap(new HashMap<String, String>());

    @Autowired
    private StringCacheBox ws2httpMap;

    /*
     * Map for <stompUID, stompSession>
     */
    @Autowired(required = false)
    StompSessionCache stompSessionCache;

    public static String getMSInstanceId() {
	return AppParam.APP_INSTANCE_ID.getValue();
    }

    public String createSessionMapping(String wsSessionID, String httpSessionId, String sessionUID) {
	if (ArgUtil.isEmpty(sessionUID)) {
	    sessionUID = http2sessionUIdMap.get(httpSessionId);
	    if (ArgUtil.isEmpty(sessionUID)) {
		sessionUID = String.format("%s-%s-%s", getMSInstanceId(), httpSessionId, wsSessionID);
		http2sessionUIdMap.put(httpSessionId, sessionUID);
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
	return http2sessionUIdMap.get(httpSessionId);
    }

    public void delinkWs2Http(String httpSessionId, String wsSessionID) {
	ws2httpMap.remove(wsSessionID);
	boolean isExists = false;
	for (Entry<String, String> entry : ws2httpMap.readAllEntrySet()) {
	    if (entry.getValue().equals(httpSessionId)) {
		isExists = true;
	    }
	}
	if (!isExists) {
	    http2sessionUIdMap.remove(httpSessionId);
	}
    }

    /**
     * 
     * @param stompUID      - only one session with one stompUID can exists, if you
     *                      want to support multiple, change accordingly
     * @param httpSessionId
     */
    public void mapHTTPSession(String stompUID, String httpSessionId, String... tags) {
	StompSession stompSession = new StompSession();
	stompSession.setPrefix(getMSInstanceId());
	stompSession.setHttpSessionId(httpSessionId);

	if (tags != null && tags.length > 0) {
	    String[] etags = new String[tags.length];
	    for (int i = 0; i < tags.length; i++) {
		etags[i] = createTagId(tags[i]);
	    }
	    stompSession.setTags(etags);
	}
	stompSession.setTenantToken(createTagId(AppContextUtil.getTenant()));
	http2stompUIdMap.put(httpSessionId, stompUID);
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

    public void registerUser(String stompUID, String... tags) {
	mapHTTPSession(stompUID, AppContextUtil.getSessionId(true), tags);
    }

    public StompSession getStompSession(String stompUID) {
	return stompSessionCache.get(stompUID);
    }

    public StompSession getStompSessionByHttpSessionId(String httpSessionId) {
	if (ArgUtil.is(httpSessionId)) {
	    String stompUID = http2stompUIdMap.get(httpSessionId);
	    if (ArgUtil.is(stompUID)) {
		return stompSessionCache.get(stompUID);
	    }
	} else {
	    LOGGER.error("httpSessionId cannot be null");
	}
	return null;
    }

    public String createTagId(String tag) {
	return tag + "-" + CryptoUtil.getHashBuilder().message(tag).secret("SOME_SECRET_TO_B_CHANGED_LATER")
		.toHmacSHA256().hash();
    }

}
