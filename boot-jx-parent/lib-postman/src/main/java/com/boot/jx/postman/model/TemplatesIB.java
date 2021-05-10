package com.boot.jx.postman.model;

import com.boot.jx.postman.model.PostManFile.PDFConverter;
import com.boot.jx.postman.model.ITemplates.ITemplate;

public enum TemplatesIB implements ITemplate {

	CONTACT_US("ContactForm"), SERVER_PING("server-ping-ib");

	String fileName;
	PDFConverter converter;
	String sampleJSON;
	boolean thymleaf = true;

	@Override
	public String getFileName() {
		return fileName;
	}

	TemplatesIB(String fileName, PDFConverter converter, String sampleJSON) {
		this.fileName = fileName;
		this.converter = converter;
		this.sampleJSON = sampleJSON;
		if (this.converter == PDFConverter.JASPER) {
			this.thymleaf = false;
		}
	}

	TemplatesIB(String fileName, PDFConverter converter) {
		this(fileName, converter, null);
	}

	TemplatesIB(String fileName, String sampleJSON) {
		this(fileName, null, sampleJSON);
	}

	TemplatesIB(String fileName) {
		this(fileName, null, null);
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

	public String toString() {
		return this.name();
	}

	@Override
	public String getJsonFile() {
		return "json/" + fileName;
	}

	@Override
	public String getHtmlFile() {
		return "html/" + fileName;
	}

	@Override
	public String getSMSFile() {
		return "html/" + fileName;
	}

}
