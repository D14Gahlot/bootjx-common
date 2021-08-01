package com.boot.jx.xms.dto;

import com.boot.jx.postman.model.MessageDefinitions.Contact;
import com.boot.jx.swagger.ApiMockModelProperty;

public class OutBoundContact implements Contact {

    private static final long serialVersionUID = -4577777772782792298L;

    @ApiMockModelProperty(value = "Name of to be used will, override the name in message")
    String name;

    @ApiMockModelProperty(value = "Phone in caseo of SMS/WHATSAPP")
    String phone;

    @ApiMockModelProperty(value = "Email Address for email message.")
    String email;

    public String getName() {
	return name;
    }

    public void setName(String name) {
	this.name = name;
    }

    public String getPhone() {
	return phone;
    }

    public void setPhone(String phone) {
	this.phone = phone;
    }

    public String getEmail() {
	return email;
    }

    public void setEmail(String email) {
	this.email = email;
    }

}