package com.boot.jx.postman.doc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.Patchable;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.TagDocument;

@Document(collection = MessageDoc.COLLECTION_NAME)
@TypeAlias("MessageDoc")
public class MessageDoc implements Serializable, Patchable<MessageDoc> {
	private static final long serialVersionUID = -7003453286628859075L;
	public static final String COLLECTION_NAME = "MESSAGE";

	@Id
	private String messageId;

	@Indexed
	private String messageIdExt;
	private String messageIdRef;

	@Indexed
	private String sessionId;

	private String collapseId;
	private long timestamp;
	private String type;
	private String template;
	private String action;
	private String handler;
	private String message;
	private String status;
	private ContactDoc contact;
	private String agent;
	private TagDocument tags;
	private Map<String, Object> model;
	private List<Attachment> attachments;

	private String quickReplyId;
	private String mediaReplyId;

	private Map<String, Long> stamps;
	public List<String> logs;

	@Indexed
	private String contactId;

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public String getCollapseId() {
		return collapseId;
	}

	public void setCollapseId(String collapseId) {
		this.collapseId = collapseId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ContactDoc getContact() {
		return contact;
	}

	public void setContact(ContactDoc contact) {
		this.contact = contact;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getAgent() {
		return this.agent;
	}

	public void setAgent(String agent) {
		this.agent = agent;
	}

	public TagDocument getTags() {
		return tags;
	}

	public void setTags(TagDocument tags) {
		this.tags = tags;
	}

	public String getMediaReplyId() {
		return mediaReplyId;
	}

	public void setMediaReplyId(String mediaReplyId) {
		this.mediaReplyId = mediaReplyId;
	}

	public String getQuickReplyId() {
		return quickReplyId;
	}

	public void setQuickReplyId(String quickReplyId) {
		this.quickReplyId = quickReplyId;
	}

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	@Override
	public MessageDoc patch() {
		MessageDoc patch = new MessageDoc();
		patch.setMessageId(this.getMessageId());
		return patch;
	}

	public List<String> getLogs() {
		return logs;
	}

	public void setLogs(List<String> logs) {
		this.logs = logs;
	}

	public List<String> logs() {
		if (this.logs == null) {
			this.logs = new ArrayList<String>();
		}
		return this.logs;
	}

	public String getMessageIdExt() {
		return messageIdExt;
	}

	public void setMessageIdExt(String messageIdExt) {
		this.messageIdExt = messageIdExt;
	}

	public String getMessageIdRef() {
		return messageIdRef;
	}

	public void setMessageIdRef(String messageIdRef) {
		this.messageIdRef = messageIdRef;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Long> getStamps() {
		return stamps;
	}

	public void setStamps(Map<String, Long> stamps) {
		this.stamps = stamps;
	}

	public Map<String, Long> stamps() {
		if (stamps == null)
			stamps = new HashMap<String, Long>();
		return stamps;
	}
}
