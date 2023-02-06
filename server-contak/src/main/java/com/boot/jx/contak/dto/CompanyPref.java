package com.boot.jx.contak.dto;

import java.io.Serializable;

public class CompanyPref implements Serializable {
	private static final long serialVersionUID = -3331480179384982332L;
	public String timeZone;

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}