package com.boot.jx.postman.model;

import java.io.Serializable;

public class MessageSession implements Serializable {
	private static final long serialVersionUID = -5472557093277982501L;
	private String assignedToDept;
	private String assignedToAgent;
	private String mode;

	public String getAssignedToDept() {
		return assignedToDept;
	}

	public void setAssignedToDept(String assignedToDept) {
		this.assignedToDept = assignedToDept;
	}

	public String getAssignedToAgent() {
		return assignedToAgent;
	}

	public void setAssignedToAgent(String assignedToAgent) {
		this.assignedToAgent = assignedToAgent;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
}
