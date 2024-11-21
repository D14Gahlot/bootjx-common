package com.boot.jx.postman.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageDefinitions.IMessageLoggable;
import com.boot.jx.postman.model.MessageOptions.WAMessageOptions;
import com.boot.jx.tunnel.ChronoScheduler;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutboxMessage extends Message<OutboxMessage>
		implements WAMessageOptions, IMessageLoggable, IMessageExtended {

	private static final long serialVersionUID = 3115992767625612005L;

	public static final OutboxMessage NO_MESSAGE = new OutboxMessage();

	private BigDecimal queue;
	private MessageSession session;
	private MessageRouter route;
	private MessagePrompt prompt;
	private List<Object> logs;
	private List<Object> trace;
	/** csv refernce key **/
	private String referenceKey;
	/** **/
	private Map<String, Object> rawMessageFormat;
	/** group key **/
	private String groupId;
	private String campaignTitle;
	private String groupName;
	private ChronoScheduler scheduler;
	/** for resend/cancel schedular API **/
	private String bulkSessionId;
	/** use /resend with a flag 'cancelExisting' */
	public boolean cancelExisting;
	private List<String> groups;
	private List<String> filters;

	public OutboxMessage(ContactType contactType) {
		super(contactType);
		this.updateStatus(Status.SCHLD);
	}

	public OutboxMessage() {
		super();
	}

	public BigDecimal getQueue() {
		return queue;
	}

	public void setQueue(BigDecimal queue) {
		this.queue = queue;
	}

	public MessageSession getSession() {
		return session;
	}

	public void setSession(MessageSession session) {
		this.session = session;
	}

	@Override
	public MessageSession session() {
		if (session == null) {
			this.session = new MessageSession();
		}
		return this.session;
	}

	public List<Object> getLogs() {
		return logs;
	}

	public void setLogs(List<Object> logs) {
		this.logs = logs;
	}

	public List<Object> logs() {
		if (this.logs == null) {
			this.logs = new ArrayList<Object>();
		}
		return this.logs;
	}

	public String getCsid() {
		return this.contact().getCsid();
	}

	public void setCsid(String csid) {
		this.contact().setCsid(csid);
	}

	public MessagePrompt getPrompt() {
		return prompt;
	}

	public void setPrompt(MessagePrompt prompt) {
		this.prompt = prompt;
	}

	public OutboxMessage prompt(MessagePrompt prompt) {
		this.prompt = prompt;
		return this;
	}

	public MessageRouter getRoute() {
		return route;
	}

	public void setRoute(MessageRouter route) {
		this.route = route;
	}

	@Override
	public MessageRouter route() {
		if (route == null) {
			this.route = new MessageRouter();
		}
		return this.route;
	}

	@Override
	public String getFrom() {
		return null;
	}

	@Override
	public String getFromName() {
		return null;
	}

	@Override
	public Message<?> replyMessage(String message) {
		return null;
	}

	public List<Object> trace() {
		if (this.trace == null) {
			this.trace = new ArrayList<Object>();
		}
		return this.trace;
	}

	public List<Object> getTrace() {
		return trace;
	}

	public void setTrace(List<Object> trace) {
		this.trace = trace;
	}

	public String getReferenceKey() {
		return referenceKey;
	}

	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}

	public Map<String, Object> getRawMessageFormat() {
		return rawMessageFormat;
	}

	public void setRawMessageFormat(Map<String, Object> rawMessageFormat) {
		this.rawMessageFormat = rawMessageFormat;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCampaignTitle() {
		return campaignTitle;
	}

	public void setCampaignTitle(String campaignTitle) {
		this.campaignTitle = campaignTitle;
	}

	public ChronoScheduler getScheduler() {
		return scheduler;
	}

	public void setScheduler(ChronoScheduler scheduler) {
		this.scheduler = scheduler;
	}

	public String getBulkSessionId() {
		return bulkSessionId;
	}

	public void setBulkSessionId(String bulkSessionId) {
		this.bulkSessionId = bulkSessionId;
	}

	public boolean getCancelExisting() {
		return cancelExisting;
	}

	public void setCancelExisting(boolean cancelExisting) {
		this.cancelExisting = cancelExisting;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}

}
