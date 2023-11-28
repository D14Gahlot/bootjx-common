package com.boot.jx.account.dto;

import java.util.List;

public class AdminAgentAccountDto {
	String domain;
	List<AdminAgentAccount> adminAgentAccountDtls;
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public List<AdminAgentAccount> getAdminAgentAccountDtls() {
		return adminAgentAccountDtls;
	}
	public void setAdminAgentAccountDtls(List<AdminAgentAccount> adminAgentAccountDtls) {
		this.adminAgentAccountDtls = adminAgentAccountDtls;
	}

}
