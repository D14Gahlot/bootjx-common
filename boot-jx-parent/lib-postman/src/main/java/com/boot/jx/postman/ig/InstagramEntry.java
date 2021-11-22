package com.boot.jx.postman.ig;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InstagramEntry implements Serializable {
	private static final long serialVersionUID = 4844872478399699245L;
	private String id;
	private Long time;
	private List<InstagramMessaging> messaging = new ArrayList<>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public List<InstagramMessaging> getMessaging() {
		return messaging;
	}

	public void setMessaging(List<InstagramMessaging> messaging) {
		this.messaging = messaging;
	}
}
