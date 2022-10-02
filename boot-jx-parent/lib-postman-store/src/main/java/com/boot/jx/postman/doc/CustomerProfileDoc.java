package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.SimpleDocument;
import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBWebsite;
import com.boot.model.UtilityModels.JsonIgnoreNull;
import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "CUSTOMER_PROFILE")
public class CustomerProfileDoc implements Serializable, SimpleDocument, JsonIgnoreUnknown, JsonIgnoreNull {
	private static final long serialVersionUID = 1281605084248923642L;

	@Id
	public String id;

	@Indexed(sparse = true, unique = true)
	public String code;

	public PBName name;

	public Set<PBPhone> phones;
	public Set<PBEmail> emails;
	public Set<PBAddress> addresses;
	public Set<PBWebsite> urls;

	public String rmCode;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<PBWebsite> urls() {
		if (urls == null) {
			this.urls = new TreeSet<PBWebsite>();
		}
		return this.urls;
	}

	public Set<PBAddress> addresses() {
		if (addresses == null) {
			this.addresses = new TreeSet<PBAddress>();
		}
		return this.addresses;
	}

	public Set<PBPhone> phones() {
		if (phones == null) {
			this.phones = new TreeSet<PBPhone>();
		}
		return this.phones;
	}

	public Set<PBEmail> emails() {
		if (emails == null) {
			this.emails = new TreeSet<PBEmail>();
		}
		return this.emails;
	}

}