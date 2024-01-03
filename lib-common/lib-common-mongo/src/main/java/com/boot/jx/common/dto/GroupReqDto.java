package com.boot.jx.common.dto;

import java.util.List;

public class GroupReqDto {
	String groupId;
	String groupName;
	List<GroupSessionDto> sessions;
	boolean isActive;
	public String getGroupId() {
		return groupId;
	}
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<GroupSessionDto> getSessions() {
		return sessions;
	}
	public void setSessions(List<GroupSessionDto> sessions) {
		this.sessions = sessions;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
}

