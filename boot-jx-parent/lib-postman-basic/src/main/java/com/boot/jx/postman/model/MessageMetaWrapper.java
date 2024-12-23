package com.boot.jx.postman.model;

import java.util.Map;

import com.boot.model.MapModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageMetaWrapper extends MapModel {

	public MessageMetaWrapper(Map<String, Object> meta) {
		super(meta);
	}

	/**
	 * 
	 * @return
	 */
	public MessageMetaWrapper isPush(boolean isPush) {
		this.put("isPush", isPush);
		return this;
	}

	public MessageMetaWrapper isTemplateExt(boolean isTemplateExt) {
		this.put("isTemplateExt", isTemplateExt);
		return this;
	}

	/**
	 * Value can differ from channel to channel
	 * 
	 * @param sendType
	 * @return
	 */
	public MessageMetaWrapper sendType(String sendType) {
		this.put("sendType", sendType);
		return this;
	}

	// method mediatemplecount(String )
	public MessageMetaWrapper mediaTemplate(String mediaTemplate) {
		this.put("ismediaTemplate", mediaTemplate);
		return this;
	}

	public String MediaType() {
		return this.getString("mediaTemplate");
	}

	public MessageMetaWrapper composeType(String composeType) {
		this.put("composeType", composeType);
		return this;
	}

	public MessageMetaWrapper categoryType(String categoryType) {
		this.put("categoryType", categoryType);
		return this;
	}

	public MapPathEntry categoryType() {
		return this.keyEntry("categoryType");
	}

	public MessageMetaWrapper categorySubType(String categorySubType) {
		this.put("categorySubType", categorySubType);
		return this;
	}

	public MapPathEntry categorySubType() {
		return this.keyEntry("categorySubType");
	}

	public boolean composeTypeIs(String composeType) {
		return this.keyEntry("composeType").is(composeType);
	}

	public boolean isTemplateExt() {
		return this.keyEntry("isTemplateExt").asBoolean();
	}

	public String sendType() {
		return this.getString("sendType");
	}

	public static MessageMetaWrapper from(Map<String, Object> meta) {
		return new MessageMetaWrapper(meta);
	}

}
