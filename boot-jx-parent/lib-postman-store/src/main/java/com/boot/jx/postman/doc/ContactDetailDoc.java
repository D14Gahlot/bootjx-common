package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.Map;

import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.utils.ArgUtil;

public class ContactDetailDoc implements Serializable, Contactable {
	private static final long serialVersionUID = -6046846959629225232L;
	private String email;
	private String userid;
	private String mobile;
	private String phone;
	private String name;

	private String contactType;
	private String channel;
	private String lane;
	private String csid;
	private String contactId;

	private Map<String, Object> filter;

	public String getPhone() {
		return ArgUtil.nonEmpty(this.phone, this.mobile);
	}

	public void setPhone(String phone) {
		this.phone = phone;
		this.mobile = phone;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Map<String, Object> getFilter() {
		return filter;
	}

	public void setFilter(Map<String, Object> filter) {
		this.filter = filter;
	}

	public String getContactType() {
		return contactType;
	}

	public void setContactType(String contactType) {
		this.contactType = contactType;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Deprecated
	public String getMobile() {
		return ArgUtil.nonEmpty(this.phone, this.mobile);
	}

	@Deprecated
	public void setMobile(String mobile) {
		this.mobile = mobile;
		this.phone = mobile;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getCsid() {
		return csid;
	}

	public void setCsid(String csid) {
		this.csid = csid;
	}

	public String getLane() {
		return lane;
	}

	public void setLane(String lane) {
		this.lane = lane;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
