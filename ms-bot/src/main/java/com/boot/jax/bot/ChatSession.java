package com.boot.jax.bot;

import java.io.Serializable;
import java.math.BigDecimal;

public class ChatSession implements Serializable {

	private static final long serialVersionUID = -5282578895568965400L;

	Boolean valid;

	public Boolean isValid() {
		return valid;
	}

	public void setValid(Boolean valid) {
		this.valid = valid;
	}

	public String whatsappExists;

	public String whatsappVerified;

	public String civilExists;

	public String civilMatch;
	
	public BigDecimal countryId;

	public BigDecimal getCountryId() {
		return countryId;
	}

	public void setCountryId(BigDecimal countryId) {
		this.countryId = countryId;
	}

	public String getWhatsappExists() {
		return whatsappExists;
	}

	public void setWhatsappExists(String whatsappExists) {
		this.whatsappExists = whatsappExists;
	}

	public String getWhatsappVerified() {
		return whatsappVerified;
	}

	public void setWhatsappVerified(String whatsappVerified) {
		this.whatsappVerified = whatsappVerified;
	}

	public String getCivilExists() {
		return civilExists;
	}

	public void setCivilExists(String civilExists) {
		this.civilExists = civilExists;
	}

	public String getCivilMatch() {
		return civilMatch;
	}

	public void setCivilMatch(String civilMatch) {
		this.civilMatch = civilMatch;
	}


}
