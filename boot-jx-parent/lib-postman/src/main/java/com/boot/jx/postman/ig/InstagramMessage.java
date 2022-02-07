package com.boot.jx.postman.ig;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstagramMessage implements Serializable {
	private static final long serialVersionUID = 260395496229953084L;
	private String mid;
	private Long seq;
	private String text;
	private boolean is_echo;
	private boolean is_deleted;

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public Long getSeq() {
		return seq;
	}

	public void setSeq(Long seq) {
		this.seq = seq;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public boolean isIs_echo() {
		return is_echo;
	}

	public void setIs_echo(boolean is_echo) {
		this.is_echo = is_echo;
	}
	
	public boolean isIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted(boolean is_deleted) {
		this.is_deleted = is_deleted;
	}
	
}
