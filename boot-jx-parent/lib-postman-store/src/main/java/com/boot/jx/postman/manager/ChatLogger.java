package com.boot.jx.postman.manager;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.exception.AmxApiError;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpServerException;
import com.boot.jx.logger.AuditDetailProvider;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.MessageDoc.MessageDocLogs;
import com.boot.jx.postman.doc.MessageDocAbstract;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageDefinitions.IMessageExtended;
import com.boot.jx.postman.model.MessageDefinitions.LogMessage;
import com.boot.jx.postman.model.MessageDefinitions.LoggableEntity;
import com.boot.jx.postman.model.MessageDefinitions.SessionInfo;
import com.boot.jx.postman.model.MessageDefinitions.TraceMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;

@Component
public class ChatLogger {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatLogger.class);

	@Autowired(required = false)
	private AuditDetailProvider auditDetailProvider;

	public String getCurrenUser() {
		return ArgUtil.is(auditDetailProvider) ? auditDetailProvider.getAuditUser() : "_SYSTEM_";
	}

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private MessageContext messageContext;

	@Autowired
	private AppConfig appConfig;

	public MessageDoc note(ChatSessionDoc sessionDoc, OutboxMessage outboxMessage) {
		outboxMessage.contact().setContactType(sessionDoc.getContactType());
		outboxMessage.contact().setChannelType(sessionDoc.getChannel());
		outboxMessage.contact().setLane(sessionDoc.getLane());
		outboxMessage.contact().setContactId(sessionDoc.getContactId());
		outboxMessage.contact().copyFrom(sessionDoc.getContact());

		outboxMessage.setSessionId(sessionDoc.getSessionId());
		outboxMessage.setType("N");
		return messageStore.note(outboxMessage, getCurrenUser());
	}

	public MessageDoc event(SessionInfo inboxMessage, String actorAgent, EVENTS eventName, Object... logMessage) {
		MessageDoc doc = new MessageDoc();
		doc.setContactId(PostManUtil.createContactId(inboxMessage.contact()));
		doc.setType("L");
		doc.setTimestamp(System.currentTimeMillis());
		if (ArgUtil.is(logMessage)) {
			for (Object string : logMessage) {
				doc.logs().add(string);
			}
		}
		doc.setAction(ArgUtil.parseAsString(eventName));
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setAgent(ArgUtil.nonEmpty(actorAgent, AppContextUtil.getActorId()));
		messageStore.save(doc, inboxMessage.contact().type());
		return doc;
	}

	public MessageDoc event(SessionInfo inboxMessage, EVENTS event, Object... logs) {
		return event(inboxMessage, inboxMessage.session().getAgent(), event, logs);
	}

	public MessageDoc event(ChatSessionDoc sessionDoc, String auditAgent, EVENTS event, Object... logs) {
		IMessageExtended inboxMessage = sessionStore.toSessionMessage(sessionDoc);
		return event(inboxMessage, auditAgent, event, logs);
	}

	public MessageDoc event(ChatSessionDoc sessionDoc, EVENTS event, Object... logs) {
		return event(sessionDoc, getCurrenUser(), event, logs);
	}

	public void error(LogMessage inboxMessage, Throwable e) {
		this.error(inboxMessage, null, e);
	}

	public void error(LogMessage inboxMessage, Status status, Throwable e) {
		MessageDocLogs doc = new MessageDocLogs();
		doc.setSessionId(inboxMessage.getSessionId());
		doc.setMessageId(inboxMessage.getMessageId());
		doc.setMessageIdExt(inboxMessage.getMessageIdExt());
		doc.setMessageIdRef(inboxMessage.getMessageIdRef());
		doc.setContactId(PostManUtil.createContactId(inboxMessage.contact()));
		doc.setType("E");
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(e.getMessage());
		doc.setStatus(ArgUtil.parseAsString(status));

		toLogs(e, doc);

		messageStore.save(doc);
		inboxMessage.logs().add(e.getMessage());
		inboxMessage.logs().add("trail:" + doc.getMessageId());
	}

	public void error(InBoundEvent inBoundEvent, Throwable e) {
		MessageDocLogs doc = new MessageDocLogs();
		doc.setSessionId(inBoundEvent.sessionId);
		doc.setContactId(inBoundEvent.contactId);
		doc.setType("E");
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(e.getMessage());
		doc.setQueue(inBoundEvent.session().getQueue());

		toLogs(e, doc);
		messageStore.save(doc);
	}

	private void toLogs(Throwable e, MessageDocLogs doc) {
		StackTraceElement[] traces = e.getStackTrace();

		if (traces.length > 0 && traces[0].toString().length() > 0) {
			for (StackTraceElement trace : traces) {
				doc.logs().add(trace.toString());
			}
		}

		if (e instanceof ApiHttpServerException || e instanceof ApiHttpException) {
			AmxApiError r = ((ApiHttpException) e).getResponse();
			doc.setHttpResp(MapModel.from(r.getBody()).toMap());
			doc.setHttpStatusCode(r.getRawStatusCode());
		}

		List<ApiFieldError> errors = ApiResponseUtil.getErrors();
		if (ArgUtil.is(errors)) {
			doc.trace().add(errors);
		}

	}

	public void error(Throwable e) {
		if (ArgUtil.is(messageContext.getMessage())) {
			this.error(messageContext.getMessage(), e);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.error(messageContext.getInBoundEvent(), e);
		} else {
			MessageDocLogs doc = new MessageDocLogs();
			doc.setType("E");
			doc.setTimestamp(System.currentTimeMillis());
			doc.setTraceId(AppContextUtil.getTraceId());
			doc.setMessage(e.getMessage());
			messageStore.save(doc);
		}
	}

	private void log(MessageDocAbstract doc, String message, Object[] debugMessage) {
		doc.setTimestamp(System.currentTimeMillis());
		doc.setTraceId(AppContextUtil.getTraceId());
		doc.setMessage(message);
		if (ArgUtil.is(debugMessage)) {
			for (int i = 0; i < debugMessage.length; i++) {
				doc.logs().add(ArgUtil.parseAsString(debugMessage[i]));
			}
		}
		messageStore.save(doc);
	}

	private MessageDocAbstract messageDoc(String type, LoggableEntity inBoundEvent) {
		MessageDocLogs doc = new MessageDocLogs();
		if (ArgUtil.is(inBoundEvent)) {
			doc.setSessionId(inBoundEvent.getSessionId());
			doc.setContactId(inBoundEvent.getContactId());
		}
		return doc;
	}

	private MessageDocAbstract messageDoc(String type, LogMessage message) {
		MessageDocAbstract doc = new MessageDocLogs();
		doc.setType(type);
		if (ArgUtil.is(message)) {
			doc.setSessionId(message.getSessionId());
			doc.setMessageId(message.getMessageId());
			doc.setMessageIdExt(message.getMessageIdExt());
			doc.setMessageIdRef(message.getMessageIdRef());
			doc.setContactId(PostManUtil.createContactId(message.contact()));
		}
		return doc;
	}

	public void debug(String message, Object... debugMessage) {
		if (!LOGGER.isDebugEnabled())
			return;
		if (ArgUtil.is(messageContext.getMessage())) {
			this.log(messageDoc("D", messageContext.getMessage()), message, debugMessage);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.log(messageDoc("D", messageContext.getInBoundEvent()), message, debugMessage);
		} else {
			this.log(messageDoc("D", new InBoundEvent()), message, debugMessage);
		}
	}

	public void debug(InBoundEvent assignEvent, String message, Object... debugMessage) {
		if (!LOGGER.isDebugEnabled())
			return;
		this.log(messageDoc("D", assignEvent), message, debugMessage);
	}

	public void warn(String message, Object... debugMessage) {
		if (!LOGGER.isWarnEnabled())
			return;
		if (ArgUtil.is(messageContext.getMessage())) {
			this.log(messageDoc("W", messageContext.getMessage()), message, debugMessage);
		} else if (ArgUtil.is(messageContext.getInBoundEvent())) {
			this.log(messageDoc("W", messageContext.getInBoundEvent()), message, debugMessage);
		} else {
			this.log(messageDoc("W", new InBoundEvent()), message, debugMessage);
		}
	}

	public void addTrace(TraceMessage inboxMessage, Object... msg) {
		if (msg == null || msg.length == 0 || inboxMessage == null) {
			return;
		}
		Object[] result = new Object[msg.length + 1];
		result[0] = appConfig.getAppInstanceType();
		System.arraycopy(msg, 0, result, 1, msg.length);
		CommonMongoQueryBuilder builder = new CommonMongoQueryBuilder();

		if (ArgUtil.is(inboxMessage.id())) {
			builder.whereIdSafe(inboxMessage.id());
			inboxMessage.trace().add(result);
			builder.update().push("trace", result);
			messageStore.updateFirst(builder.getQuery(), builder.getUpdate(), MessageDoc.class,
					MessageStore.getCollectionName(inboxMessage.contact().type()));
		} else {
			inboxMessage.trace().add(result);
		}
	}

}
