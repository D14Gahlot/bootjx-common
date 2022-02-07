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

}
