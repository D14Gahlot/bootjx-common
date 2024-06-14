package com.boot.jx.admin.dto;

import java.util.List;

public class GroupReqDto {	
	
	String groupId;
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
	public List<GroupSessionDto> getSession() {
		return session;
	}
	public void setSession(List<GroupSessionDto> session) {
		this.session = session;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	String groupName;
	List<GroupSessionDto> session;
	boolean isActive;
 
}
 
class GroupSessionDto{
	
	
	String phone;
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getContactType() {
		return contactType;
	}
	public void setContactType(String contactType) {
		this.contactType = contactType;
	}
	String contactType;
	
}
