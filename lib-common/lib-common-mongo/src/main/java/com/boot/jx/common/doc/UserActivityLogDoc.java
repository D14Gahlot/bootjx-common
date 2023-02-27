package com.boot.jx.common.doc;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;

@Document(collection = "USER_ACTIVITY_LOGS")
@TypeAlias("UserActivityLogs")
public class UserActivityLogDoc {

	@Id
	private String id;

	@Indexed
	private String userType;

	@Indexed
	private String user;

	@Indexed
	private String activity;

	private Map<String, Object> data;

	public TimeStampIndex createdAt;

	@Indexed
	private String sessionId;

	@Indexed
	private String appType;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAppType() {
		return appType;
	}

	public void setAppType(String appType) {
		this.appType = appType;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public TimeStampIndex getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(TimeStampIndex createdAt) {
		this.createdAt = createdAt;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
