package com.boot.jx.postman.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.Language;
import com.boot.jx.postman.model.ITemplates.ITemplate;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;
import com.boot.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message<T extends Message<T>> implements Serializable, MessageOptions {

	private static final long serialVersionUID = 1363933600245334964L;
	public static final String DATA_KEY = "data";
	public static final String RESULTS_KEY = "results";

	public static enum Status {
		INIT, CRTD, SENT, DLVRD, READ, NSENT, BLCKD, FAILD
	}

	public static class Priority {
		public static final int HIGHEST = 1;
		public static final int MEDIUM = 2;
		public static final int LOWEST = 3;
	}

	public static interface IChannel {
	}

	protected long timestamp;
	protected int attempt;
	protected Language lang = null;
	protected String subject;
	protected String message = null;
	protected List<String> to = null;
	protected List<Contact> contacts = null;
	private String template = null;
	private String action = null;
	private String type = null;

	private Map<String, Object> model = new HashMap<String, Object>();
	protected Map<String, Object> options = new HashMap<String, Object>();
	private MessageType messageType = null;
	private ContactType contactType;
	protected String channel;;

	private List<File> files = null;
	private List<Attachment> attachments = null;

	private String id;
	private String messageId;
	private String messageIdExt;
	private String messageIdRef;
	private String sessionId;
	private String contactId;

	private String collapseId;
	public int priority;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public ContactType getContactType() {
		return contactType;
	}

	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	@JsonIgnore
	public void setIChannel(IChannel channel) {
		this.channel = ArgUtil.parseAsString(channel);
	}

	private Status status = null;

	private List<String> lines = new ArrayList<String>();

	public Map<String, Object> getModel() {
		return model;
	}

	public void setModel(Map<String, Object> model) {
		this.model = model;
	}

	public Map<String, Object> model() {
		if (this.model == null) {
			this.model = new HashMap<String, Object>();
		}
		return model;
	}

	@SuppressWarnings("unchecked")
	@JsonIgnore
	public void setObject(Object object) {
		this.model = JsonUtil.fromJson(JsonUtil.toJson(object), Map.class);
	}

	@JsonIgnore
	public void setModelData(Object object) {
		this.getModel().put(DATA_KEY, object);
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @param subject the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String text) {
		this.message = text;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@JsonIgnore
	public void setITemplate(ITemplate template) {
		this.template = template.toString();
	}

	@JsonIgnore
	public ITemplate getITemplate() {
		return ITemplates.getTemplate(this.template);
	}

	public Language getLang() {
		return lang;
	}

	public void setLang(Language lang) {
		this.lang = lang;
	}

	public Message() {
		this.attempt = 0;
		this.timestamp = System.currentTimeMillis();
		this.status = Status.INIT;
		this.to = new ArrayList<String>();
		this.contacts = new ArrayList<Contact>();
		this.priority = 0;

	}

	public Message(ContactType contactType) {
		this();
		this.contactType = contactType;
	}

	/**
	 * @return the to
	 */
	public List<String> getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(List<String> to) {
		this.to = to;
	}

	/**
	 * @param to the to to set
	 */
	public void addTo(String... recieverIds) {
		for (String recieverId : recieverIds) {
			this.to.add(StringUtils.trim(recieverId));
		}
	}

	public void addLine(String... lines) {
		for (String line : lines) {
			this.lines.add(line);
		}
	}

	public List<String> getLines() {
		return lines;
	}

	public void setLines(List<String> lines) {
		this.lines = lines;
	}

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getAttempt() {
		return attempt;
	}

	public void setAttempt(int attempt) {
		this.attempt = attempt;
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public List<Contact> getContacts() {
		return contacts;
	}

	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}

	public void addContact(Contact... contacts) {
		for (Contact contact : contacts) {
			this.contacts.add(contact);
		}
	}

	public List<File> getFiles() {
		return files;
	}

	public void setFiles(List<File> files) {
		this.files = files;
	}

	public List<File> files() {
		if (this.files == null) {
			this.files = new ArrayList<File>();
		}
		return files;
	}

	public void addFile(File... files) {
		for (File file : files) {
			this.files().add(file);
		}
	}

	@SuppressWarnings("unchecked")
	public T to(List<String> to) {
		this.setTo(to);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T model(Map<String, Object> model) {
		this.setModel(model);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T to(String to) {
		this.addTo(to);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T subject(String subject) {
		this.setSubject(subject);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T message(String message) {
		this.setMessage(message);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T template(ITemplate template) {
		this.setITemplate(template);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T template(String template) {
		this.setTemplate(template);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T put(String key, Object value) {
		this.model().put(key, value);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T data(Object value) {
		this.getModel().put("data", value);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T file(File... files) {
		this.addFile(files);
		return (T) this;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public String getCollapseId() {
		return collapseId;
	}

	public void setCollapseId(String collapseId) {
		this.collapseId = collapseId;
	}

	public Map<String, Object> getOptions() {
		return options;
	}

	public void setOptions(Map<String, Object> options) {
		this.options = options;
	}

	@SuppressWarnings("unchecked")
	public T options(Map<String, Object> options) {
		this.options = new HashMap<String, Object>();
		return (T) this;
	}

	public Map<String, Object> options() {
		if (options == null) {
			this.options = new HashMap<String, Object>();
		}
		return this.options;
	}

	public String getMessageIdExt() {
		return messageIdExt;
	}

	public void setMessageIdExt(String messageIdExt) {
		this.messageIdExt = messageIdExt;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public String getContactId() {
		return contactId;
	}

	public void setContactId(String contactId) {
		this.contactId = contactId;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	public List<Attachment> attachments() {
		if (this.attachments == null) {
			this.attachments = new ArrayList<Attachment>();
		}
		return attachments;
	}

	@SuppressWarnings("unchecked")
	public T attachment(Attachment... attachments) {
		for (Attachment file : attachments) {
			this.attachments().add(file);
		}
		return (T) this;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

}
