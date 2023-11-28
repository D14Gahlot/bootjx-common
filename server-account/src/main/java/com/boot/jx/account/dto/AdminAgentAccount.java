package com.boot.jx.account.dto;

public class AdminAgentAccount {
	
	private String agent_code;
	private String agent_name;
	private String agent_email;
	private String agent_number;
	private boolean admin;
	public String getAgent_code() {
		return agent_code;
	}
	public void setAgent_code(String agent_code) {
		this.agent_code = agent_code;
	}
	public String getAgent_name() {
		return agent_name;
	}
	public void setAgent_name(String agent_name) {
		this.agent_name = agent_name;
	}
	public String getAgent_email() {
		return agent_email;
	}
	public void setAgent_email(String agent_email) {
		this.agent_email = agent_email;
	}
	public String getAgent_number() {
		return agent_number;
	}
	public void setAgent_number(String agent_number) {
		this.agent_number = agent_number;
	}
	public boolean isAdmin() {
		return admin;
	}
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
}
