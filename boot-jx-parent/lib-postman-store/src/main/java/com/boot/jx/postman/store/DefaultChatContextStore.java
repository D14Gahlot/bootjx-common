package com.boot.jx.postman.store;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.scope.ThreadScoped;
import com.boot.utils.JsonUtil;

@Component
@ThreadScoped
public class DefaultChatContextStore implements IChatContextStore<Map<String, Object>, Map<String, Object>> {

	private Map<String, Object> session;
	private Map<String, Object> user;

	@Override
	public Map<String, Object> newUser() {
		return new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> newSession() {
		return new HashMap<String, Object>();
	}

	@Override
	public Map<String, Object> getUser() {
		return user;
	}

	@Override
	public Map<String, Object> getSession() {
		return session;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> loadUser(Object user) {
		if (user == null) {
			this.user = new HashMap<String, Object>();
		} else if (user instanceof Map) {
			this.user = (Map<String, Object>) user;
		} else if (user instanceof String) {
			this.user = JsonUtil.fromJsonToMap((String) user);
		} else {
			this.user = JsonUtil.toMap(user);
		}
		return this.user;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> loadSession(Object session) {
		if (session == null) {
			this.session = new HashMap<String, Object>();
		} else if (session instanceof Map) {
			this.session = (Map<String, Object>) session;
		} else if (session instanceof String) {
			this.session = JsonUtil.fromJsonToMap((String) session);
		} else {
			this.session = JsonUtil.toMap(session);
		}
		return this.session;
	}

}