package com.boot.jx.phonebook.model;

import org.springframework.data.mongodb.core.index.Indexed;

public class PBPhone {

    @Indexed
    public String phone;
    public String type;
    public String label;

    public String country;
    public String countryCallingCode;
    public String nationalNumber;
    public String ext;

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

}
