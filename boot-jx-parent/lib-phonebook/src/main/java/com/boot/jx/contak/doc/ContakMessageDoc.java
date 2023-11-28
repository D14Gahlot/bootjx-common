package com.boot.jx.contak.doc;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.contak.dto.ContakModel;
import com.boot.jx.contak.dto.ContakTemplate;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Document(collection = "OA_MESSAGE")
public class ContakMessageDoc extends ContakMessageTrace {
	private static final long serialVersionUID = 1281605084248923642L;

	public String title;

	public String message;

	public String otp;
	public String type; // OTP,TRAN,PROM
	public String pubKey;

	public String companyName;
	public long companyStamp;
	public String logoUrl;

	public ContakModel model;
	public String modelEncrypted;
	public ContakTemplate template;

	private List<String> tags;
	public List<Object> events;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOtp() {
		return otp;
	}

	public void setOtp(String otp) {
		this.otp = otp;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPubKey() {
		return pubKey;
	}

	public void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public ContakTemplate getTemplate() {
		return template;
	}

	public void setTemplate(ContakTemplate template) {
		this.template = template;
	}

	public ContakTemplate template() {
		if (!ArgUtil.is(this.template)) {
			this.template = new ContakTemplate();
		}
		return this.template;
	}

	public ContakModel getModel() {
		return model;
	}

	public void setModel(ContakModel model) {
		this.model = model;
	}

	public ContakModel model() {
		if (!ArgUtil.is(this.model)) {
			this.model = new ContakModel();
		}
		return this.model;
	}

	public long getCompanyStamp() {
		return companyStamp;
	}

	public void setCompanyStamp(long companyStamp) {
		this.companyStamp = companyStamp;
	}

	public List<Object> getEvents() {
		return events;
	}

	public void setEvents(List<Object> events) {
		this.events = events;
	}
}