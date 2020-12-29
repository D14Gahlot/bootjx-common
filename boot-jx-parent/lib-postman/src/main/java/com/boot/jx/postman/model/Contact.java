package com.boot.jx.postman.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.boot.jx.dict.Language;
import com.boot.jx.dict.Tenant;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Contact implements java.io.Serializable {

	private static final long serialVersionUID = -2229330167964350550L;
	Tenant tenant;
	String country;
	String userid;
	String prefix;
	String mobile;
	String email;
	Language lang;
	List<Map<String, Object>> filter;
	List<Map<String, String>> keymap;

	public Contact() {
		super();
		this.filter = new ArrayList<Map<String, Object>>();
		this.keymap = new ArrayList<Map<String, String>>();
	}

	public Language getLang() {
		return lang;
	}

	public void setLang(Language lang) {
		this.lang = lang;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public Contact prefix(String prefix) {
		this.prefix = prefix;
		return this;
	}

	public Contact mobile(String mobile) {
		this.mobile = mobile;
		return this;
	}

	public Contact email(String email) {
		this.email = email;
		return this;
	}

	public List<Map<String, String>> getKeymap() {
		return keymap;
	}

	public void setKeymap(List<Map<String, String>> keymap) {
		this.keymap = keymap;
	}

	public List<Map<String, Object>> getFilter() {
		return filter;
	}

	public void setFilter(List<Map<String, Object>> filter) {
		this.filter = filter;
	}

	public Contact or(Map<String, Object> or) {
		this.filter.add(or);
		return this;
	}

}
