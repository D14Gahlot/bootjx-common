package com.boot.jx.postman.pbook;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PBPhone implements Serializable, Comparable<PBPhone> {

	private static final long serialVersionUID = 1772318013635615811L;
	public String phone;
	public String type;
	public String label;

	public String country;
	public String countryCallingCode;
	public String nationalNumber;
	public String ext;

	@Override
	public String toString() {
		return countryCallingCode + nationalNumber + ext;
	}

	@Override
	public int compareTo(PBPhone o) {
		if (o == null) {
			return 1;
		}
		return this.toString().compareTo(o.toString());
	}

	// Social
	public String whatsAppId;

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getCountryCallingCode() {
		return countryCallingCode;
	}

	public void setCountryCallingCode(String countryCallingCode) {
		this.countryCallingCode = countryCallingCode;
	}

	public String getNationalNumber() {
		return nationalNumber;
	}

	public void setNationalNumber(String nationalNumber) {
		this.nationalNumber = nationalNumber;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	public String getWhatsAppId() {
		return whatsAppId;
	}

	public void setWhatsAppId(String whatsAppId) {
		this.whatsAppId = whatsAppId;
	}

	public PBPhone phone(String phone) {
		this.phone = phone;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		PBPhone that = (PBPhone) o;
		if ((this.countryCallingCode != that.countryCallingCode) || (this.nationalNumber != that.nationalNumber)
				|| (this.ext != that.ext)) {
			return false;
		}
		return true;
	}

}
