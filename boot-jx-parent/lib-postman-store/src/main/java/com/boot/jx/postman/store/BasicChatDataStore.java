package com.boot.jx.postman.store;

import java.util.HashMap;
import java.util.Map;

import com.boot.utils.JsonUtil;

public class BasicChatDataStore {

    public static class BasicChatUserData {
	private Map<String, Object> data;

	public Map<String, Object> getData() {
	    return data;
	}

	public void setData(Map<String, Object> data) {
	    this.data = data;
	}

	public Map<String, Object> data() {
	    if (this.data == null) {
		this.data = new HashMap<String, Object>();
	    }
	    return data;
	}

	public void put(String key, Object value) {
	    this.data().put(key, value);
	}

	public Object get(String key) {
	    return this.data().get(key);
	}
    }

    public static class BasicChatSessionData {
	private Map<String, Object> data;

	public BasicChatSessionData() {
	    super();
	    this.data = new HashMap<String, Object>();
	}

	public Map<String, Object> getData() {
	    return data;
	}

	public void setData(Map<String, Object> data) {
	    this.data = data;
	}

	public Map<String, Object> data() {
	    if (this.data == null) {
		this.data = new HashMap<String, Object>();
	    }
	    return data;
	}

	public void put(String key, Object value) {
	    this.data().put(key, value);
	}

	public Object get(String key) {
	    return this.data().get(key);
	}
    }

    private BasicChatSessionData session;
    private BasicChatUserData user;

    public BasicChatUserData newUserData() {
	return new BasicChatUserData();
    }

    public BasicChatSessionData newSessionData() {
	return new BasicChatSessionData();
    }

    public BasicChatUserData getUserData() {
	return user;
    }

    public BasicChatSessionData getSessionData() {
	return session;
    }

    public BasicChatUserData loadUserData(Object user) {
	if (user == null) {
	    this.user = new BasicChatUserData();
	} else {
	    this.user = JsonUtil.parse(user, BasicChatUserData.class);
	}
	return this.user;
    }

    public BasicChatSessionData loadSessionData(Object session) {
	if (session == null) {
	    this.session = new BasicChatSessionData();
	} else {
	    this.session = JsonUtil.parse(session, BasicChatSessionData.class);
	}
	return this.session;
    }

}