package com.boot.jx.postman.model;

import com.boot.jx.postman.model.PostManFile.PDFConverter;
import com.boot.jx.postman.model.ITemplates.ITemplate;
import com.boot.jx.postman.model.Notipy.ChannelType;

public enum TemplatesMX implements ITemplate {

	CONTACT_US("ContactForm"),

	SERVER_PING("server-ping"),
	// Default add enums above this
	DEFAULT("default");

	String fileName;
	PDFConverter converter;
	String sampleJSON;
	boolean thymleaf = true;
	ChannelType channel = null;

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public String getHtmlFile() {
		return "html/" + getFileName();
	}

	@Override
	public String getSMSFile() {
		return "html/sms/" + getFileName();
	}

	@Override
	public String getJsonFile() {
		return "json/" + getFileName();
	}

	TemplatesMX(String fileName, PDFConverter converter, String sampleJSON, ChannelType channel) {
		this.fileName = fileName;
		this.converter = converter;
		this.sampleJSON = sampleJSON;
		if (this.converter == PDFConverter.JASPER) {
			this.thymleaf = false;
		}
		this.channel = channel;
	}

	TemplatesMX(String fileName, PDFConverter converter, String sampleJSON) {
		this(fileName, converter, sampleJSON, null);
	}

	TemplatesMX(String fileName, PDFConverter converter) {
		this(fileName, converter, null, null);
	}

	TemplatesMX(String fileName, ChannelType channel) {
		this(fileName, null, null, channel);
	}

	TemplatesMX(String fileName) {
		this(fileName, null, null, null);
	}

	@Override
	public PDFConverter getConverter() {
		return converter;
	}

	@Override
	public String getSampleJSON() {
		if (sampleJSON == null) {
			return this.fileName + ".json";
		}
		return sampleJSON;
	}

	@Override
	public boolean isThymleaf() {
		return thymleaf;
	}

	@Override
	public ChannelType getChannel() {
		return channel;
	}

	public String toString() {
		return this.name();
	}

}
