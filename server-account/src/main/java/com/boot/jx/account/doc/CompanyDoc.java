package com.boot.jx.account.doc;

import java.io.Serializable;

import com.boot.jx.phonebook.model.PBAddress;
import com.boot.jx.phonebook.model.PBEmail;
import com.boot.jx.phonebook.model.PBPhone;
import com.boot.jx.phonebook.model.PBTax;
import com.boot.jx.phonebook.model.PBWebsite;

public class CompanyDoc implements Serializable {

    private static final long serialVersionUID = -3354844112176554561L;

    private String businessName;
    private String businessType;
    private String websiteUrl;
    private String businessAbout;
    //Type --Company , Billing
    private PBAddress address;
    private PBPhone phone;
    private PBPhone phoneAlt;
    private PBEmail email;
    private PBEmail emailAlt;
    private PBTax tax;
    private PBWebsite website;
    

    public String getBusinessName() {
	return businessName;
    }

    public void setBusinessName(String businessName) {
	this.businessName = businessName;
    }

    public String getBusinessType() {
	return businessType;
    }

    public void setBusinessType(String businessType) {
	this.businessType = businessType;
    }

    public String getWebsiteUrl() {
	return websiteUrl;
    }

    public void setWebsiteUrl(String websiteUrl) {
	this.websiteUrl = websiteUrl;
    }


    public String getBusinessAbout() {
	return businessAbout;
    }

    public void setBusinessAbout(String businessAbout) {
	this.businessAbout = businessAbout;
    }

	public PBAddress getAddress() {
		return address;
	}

	public void setAddress(PBAddress address) {
		this.address = address;
	}

	public PBPhone getPhone() {
		return phone;
	}

	public void setPhone(PBPhone phone) {
		this.phone = phone;
	}

	public PBPhone getPhoneAlt() {
		return phoneAlt;
	}

	public void setPhoneAlt(PBPhone phoneAlt) {
		this.phoneAlt = phoneAlt;
	}

	public PBEmail getEmail() {
		return email;
	}

	public void setEmail(PBEmail email) {
		this.email = email;
	}

	public PBEmail getEmailAlt() {
		return emailAlt;
	}

	public void setEmailAlt(PBEmail emailAlt) {
		this.emailAlt = emailAlt;
	}

	public PBTax getTax() {
		return tax;
	}

	public void setTax(PBTax tax) {
		this.tax = tax;
	}

	public PBWebsite getWebsite() {
		return website;
	}

	public void setWebsite(PBWebsite website) {
		this.website = website;
	}

   
	

	
	

	
}
