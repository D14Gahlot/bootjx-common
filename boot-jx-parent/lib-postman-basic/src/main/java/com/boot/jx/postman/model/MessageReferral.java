package com.boot.jx.postman.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageReferral implements Serializable {
	private static final long serialVersionUID = 1875887497925865671L;

	String messageId;
	String messageIdExt;
	String bulkId;

	String sourceUrl;
	String sourceId;

	@ApiMockModelProperty(example = "feedback", value = "Category of Referral Source",
			allowableValues = "message,social,ads,campaign")
	String sourceCategory;

	@ApiMockModelProperty(example = "feedback", value = "Type of Referral Source",
			allowableValues = "inbound,outbound,feedback,story,post")
	String sourceType;

	String title;
	String body;

	String mediaType;
	String mediaUrl;
	String thumbUrl;
	String imageUrl;

	private Map<String, Object> info;

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public void setSourceUrl(String sourceUrl) {
		this.sourceUrl = sourceUrl;
	}

	public String getSourceId() {
		return sourceId;
	}

	public void setSourceId(String sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public Map<String, Object> info() {
		if (this.info == null) {
			this.info = new HashMap<String, Object>();
		}
		return this.info;
	}

	@Override
	public String toString() {
		return "MessageReferral{ \n" + "sourceUrl=" + sourceUrl + ", sourceId=" + sourceId + ", sourceType="
				+ sourceType + ", body=" + body + "}";
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMediaType() {
		return mediaType;
	}

	public void setMediaType(String mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public Map<String, Object> getInfo() {
		return info;
	}

	public void setInfo(Map<String, Object> info) {
		this.info = info;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getMessageIdExt() {
		return messageIdExt;
	}

	public void setMessageIdExt(String messageIdExt) {
		this.messageIdExt = messageIdExt;
	}

	public String getSourceCategory() {
		return sourceCategory;
	}

	public void setSourceCategory(String sourceCategory) {
		this.sourceCategory = sourceCategory;
	}

	public String getBulkId() {
		return bulkId;
	}

	public void setBulkId(String bulkId) {
		this.bulkId = bulkId;
	}
}
