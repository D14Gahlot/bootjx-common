package com.boot.jax.bot.alex;

import org.springframework.stereotype.Component;

import com.boot.jx.scope.TenantScoped;
import com.boot.jx.scope.TenantValue;

/**
 * The Class PostManConfig.
 */
@TenantScoped
@Component
public class BotConfig {

	@TenantValue("${company.app.url}")
	private String companyAppUrl;
	
	@TenantValue("${company.app.download.url}")
	private String companyAppDownloadUrl;

	@TenantValue("${company.name}")
	private String companyName;

	@TenantValue("${company.website.url}")
	private String companyWebSiteUrl;

	@TenantValue("${company.idtype}")
	private String companyIDType;
	
	@TenantValue("${app.default.currency.quotename}")
	private String tenantCurrencyQuoteName;

	public String getTenantCurrencyQuoteName() {
		return tenantCurrencyQuoteName;
	}

	public void setTenantCurrencyQuoteName(String tenantCurrencyQuoteName) {
		this.tenantCurrencyQuoteName = tenantCurrencyQuoteName;
	}

	public String getCompanyName() {
		return companyName;
	}

	public String getCompanyWebSiteUrl() {
		return companyWebSiteUrl;
	}

	public String getCompanyIDType() {
		return companyIDType;
	}

	public String getCompanyAppUrl() {
		return companyAppUrl;
	}
	
	public String getCompanyAppDownloadUrl() {
		return companyAppDownloadUrl;
	}

}
