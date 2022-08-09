package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBWebsite;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "CUSTOMER_PROFILE")
public class CustomerProfileDoc implements Serializable {
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