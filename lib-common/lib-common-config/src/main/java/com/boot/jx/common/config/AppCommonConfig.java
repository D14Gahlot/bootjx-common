package com.boot.jx.common.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.TimeUtils;

@Component
public class AppCommonConfig {

	@Autowired
	private SessionStore sessionStore;

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("chatSessionTimeout", TimeUtils.toMillis(sessionStore.getChatSessionTimeout()));
		map.put("timestamp", System.currentTimeMillis());
		return map;
	}
}
