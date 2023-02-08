package com.boot.jx.contak.dto;

import java.io.Serializable;
import java.util.List;

public class CompanyPref implements Serializable {
	private static final long serialVersionUID = -3331480179384982332L;
	public String timeZone;
	public List<String> allowedIPAddresses;

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public List<String> getAllowedIPAddresses() {
		return allowedIPAddresses;
	}

	public void setAllowedIPAddresses(List<String> allowedIps) {
		this.allowedIPAddresses = allowedIps;
	}
}