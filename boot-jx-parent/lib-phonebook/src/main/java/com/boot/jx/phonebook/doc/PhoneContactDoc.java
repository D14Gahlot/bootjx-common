package com.boot.jx.phonebook.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.phonebook.model.PBAddress;
import com.boot.jx.phonebook.model.PBEmail;
import com.boot.jx.phonebook.model.PBName;
import com.boot.jx.phonebook.model.PBPhone;
import com.boot.jx.phonebook.model.PBWebsite;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "PHONE_CONTACT")
public class PhoneContactDoc implements Serializable {
    private static final long serialVersionUID = 1281605084248923642L;

    @Id
    public String id;

    public String userId;

    public PBName name;

    public List<PBPhone> phones;
    public List<PBEmail> emails;
    public List<PBAddress> aaddresses;
    public List<PBWebsite> urls;

}