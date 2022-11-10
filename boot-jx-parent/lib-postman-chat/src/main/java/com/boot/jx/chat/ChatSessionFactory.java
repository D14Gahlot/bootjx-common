package com.boot.jx.chat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMConstants.CHAT_MODE;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.model.MessageDefinitions.IMessage;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.ChatSessionQuery;
import com.boot.jx.postman.store.MessageContext;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;

@Component
public class ChatSessionFactory {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChatSessionFactory.class);

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private MessageStore messageStore;

	@Autowired
	private PMDomainConfig pmDomainConfig;

	@Autowired
	private MessageContext messageContext;

	@Autowired
	private ChatUtility chatUtility;

	public ChatSessionDoc getChatSessionByContactId(String contactId, String ticketHash) {

		if (!ArgUtil.is(contactId)) {
			return null;
		}

		ChatSessionDoc chatSessionDoc = null;

		if (ArgUtil.is(ticketHash)) {
			chatSessionDoc = sessionStore.getSessionPrimeByTicketHash(contactId, ticketHash);

		} else {
			ChatContactDoc chatContactDoc = sessionStore.getContact(contactId);
			if (ArgUtil.is(chatContactDoc)) {
				String sessionId = chatContactDoc.getSessionId();
				if(ArgUtil.is(sessionId)){
					chatSessionDoc = sessionStore.getSession(sessionId);					
				}
			}
		}

		if (sessionStore.isSessionValid(chatSessionDoc)) {
			return chatSessionDoc;
		}

		return null;
	}

	public ChatSessionDoc getChatSession(String sessionId, String ticketHash) {

		if (!ArgUtil.is(sessionId)) {
			return null;
		}

		ChatSessionDoc chatSessionDoc = sessionStore.getSession(sessionId);

		if (sessionStore.isSessionValid(chatSessionDoc)) {
			return chatSessionDoc;
		}

		if (!ArgUtil.is(chatSessionDoc)) {
			return null;
		}

		return getChatSessionByContactId(chatSessionDoc.getContactId(), ticketHash);
	}

	public ChatSessionDoc getChatSession(IMessage sessionMessage) {

		String ticketHash = sessionMessage.session().getTicketHash();

		// SESSION FIND BY SESSION_ID
		ChatSessionDoc chatSessionDoc = getChatSession(sessionMessage.getSessionId(), ticketHash);

		if (ArgUtil.is(chatSessionDoc)) {
			return chatSessionDoc;
		}

		Contactable contact = PostManUtil.getContactMeta(sessionMessage.contact());

		if (!ArgUtil.is(contact.getContactId())) {
			// CONTACT CONNANOT BE FOUND
			if (ArgUtil.is(sessionMessage.getSessionId())) {
				chatSessionDoc = sessionStore.getSession(sessionMessage.getSessionId());
				if (ArgUtil.is(chatSessionDoc)) {
					contact.copyFrom(chatSessionDoc.contact());
					contact.setContactId(chatSessionDoc.getContactId());
				}
			}
			if (!ArgUtil.is(contact.getContactId())) {
				return null;
			}
		}

		// CONTACT FIND BY SESSION_ID
		ChatContactDoc chatContactDoc = sessionStore.getContact(contact.getContactId());

		// CONTACT CREATION
		if (ArgUtil.isEmpty(chatContactDoc)) {
			// System.out.println("CONTACT CREATION");
			ChatContactQuery chatContactQuery = new ChatContactQuery(contact.getContactId());
			chatContactQuery.update(contact);
			chatContactQuery.updateCreatedStamp();
			// chatContactDoc = sessionStore.save(chatContactQuery.getDoc());
			chatContactDoc = sessionStore.save(chatContactQuery);
		}

		// SESSION FiND BY CONTACT_ID
		chatSessionDoc = getChatSessionByContactId(contact.getContactId(), ticketHash);

		if (ArgUtil.is(chatSessionDoc)) {
			return chatSessionDoc;
		}

		// Try with Reply Id
		if (ArgUtil.is(sessionMessage.getReplyIdExt()) && PostManUtil.IS_TRACK_BY_REPLY_ID(contact.getChannelType())) {
			MessageDoc prev = messageStore.findOneByMessageIdExt(sessionMessage.getReplyIdExt(),
					contact.getContactType());
			if (ArgUtil.is(prev) && ArgUtil.is(prev.getSessionId())) {
				// SESSION FIND BY SESSION_ID - Try Again
				chatSessionDoc = getChatSession(prev.getSessionId(), ticketHash);
				if (ArgUtil.is(chatSessionDoc)) {
					return chatSessionDoc;
				}
			}
		}

		sessionStore.inactiveAllPreviousSessions(contact.getContactId(), ticketHash);

		// SESSION CREATION
		// System.out.println("SESSION CREATION");
		chatSessionDoc = new ChatSessionDoc();
		chatSessionDoc.setContactId(contact.getContactId());
		chatSessionDoc.setContactType(sessionMessage.contact().getContactType());
		chatSessionDoc.setChannel(sessionMessage.contact().getChannelType());
		chatSessionDoc.setLane(sessionMessage.contact().getLane());
		chatSessionDoc
				.setMode(ArgUtil.nonEmpty(sessionMessage.session().getMode(), sessionMessage.route().getSendMode()));
		chatSessionDoc.setActive(true);
		chatSessionDoc.setPrimary(true);
		chatSessionDoc.contact().setName(chatContactDoc.getName());
		chatSessionDoc.contact().copyFrom(chatContactDoc);
		chatSessionDoc.setTicketHash(sessionMessage.session().getTicketHash());
		chatSessionDoc.setSubject(sessionMessage.getSubject());
		chatSessionDoc.setRoutingId(PostManUtil.ROUTING_ID("",
				ArgUtil.nonEmpty(sessionMessage.route().getQueueCode(), sessionMessage.session().getQueue())));
		sessionMessage.session().setFirstMessage(true);
		return sessionStore.saveSession(chatSessionDoc);
	}

	public ChatSessionDoc linkSession(ChatSessionDoc chatSessionDoc, IMessage inboxMessage) {
		if (!ArgUtil.is(chatSessionDoc)) {
			return null;
		}

		if (PostManUtil.isInBound(inboxMessage)) {
			chatSessionDoc.setLastInComingStamp(inboxMessage.getTimestamp());
			// Query Update for Session
			ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);
			chatSessionDocQuery.setLastInComingStamp(chatSessionDoc.getLastInComingStamp());

			if (ArgUtil.isEmptyValue(chatSessionDoc.getFirstInComingStamp())) {
				chatSessionDocQuery.setFirstInComingStamp(inboxMessage.getTimestamp());
			}
			// Assign Queue
			if (!chatUtility.inQueue(chatSessionDoc)) {
				String defaultQueue = inboxMessage.route().getQueueCode();
				if (ArgUtil.is(defaultQueue)) {
					chatSessionDocQuery.setQueue(defaultQueue);
					chatSessionDocQuery.setMode(inboxMessage.route().getSendMode());
				} else {
					defaultQueue = pmDomainConfig.getDefaultInboundQueue(inboxMessage.contact());
					chatSessionDocQuery.setQueue(defaultQueue);
				}
			}

			sessionStore.updateFirst(chatSessionDocQuery);

			// Query Update for Contact
			ChatContactQuery chatContactQuery = new ChatContactQuery(chatSessionDoc.getContactId());
			chatContactQuery.setLastInBoundStamp(inboxMessage.getTimestamp());
			chatContactQuery.setSessionId(chatSessionDoc.getSessionId());
			chatContactQuery.update(inboxMessage.contact());
			sessionStore.updateFirst(chatContactQuery);
		} else if (PostManUtil.isOutBound(inboxMessage)) {
			ChatSessionQuery chatSessionDocQuery = new ChatSessionQuery(chatSessionDoc);

			// Assign Queue
			if (!chatUtility.inQueue(chatSessionDoc)) {

				String defaultQueue = inboxMessage.route().getQueueCode();
				if (ArgUtil.is(defaultQueue)) {
					chatSessionDocQuery.setQueue(defaultQueue);
					chatSessionDocQuery.setMode(inboxMessage.route().getSendMode());
				} else {
					defaultQueue = pmDomainConfig.getDefaultInboundQueue(inboxMessage.contact(),
							CHAT_MODE.from(inboxMessage.route().getSendMode()));
					chatSessionDocQuery.setQueue(defaultQueue);
				}
			}

			sessionStore.updateFirst(chatSessionDocQuery);

			// Query Update for Contact
			ChatContactQuery chatContactQuery = new ChatContactQuery(chatSessionDoc.getContactId());
			chatContactQuery.setSessionId(chatSessionDoc.getSessionId());
			sessionStore.updateFirst(chatContactQuery);
		}
		sessionStore.updateMessageFromSession(chatSessionDoc, inboxMessage);
		return chatSessionDoc;
	}

	public ChatSessionDoc linkSession(IMessage inboxMessage) {
		ChatSessionDoc session = getChatSession(inboxMessage);
		return linkSession(session, inboxMessage);
	}

	public void push(MessageDoc msgDoc, IMessage iMessage) {
		if (PostManUtil.isInBound(msgDoc.getType()) || PostManUtil.isOutBound(msgDoc.getType())) {
			try {
				ChatSessionQuery chatSessionDocQuery = messageContext.session();
				long now = System.currentTimeMillis();

				if (PostManUtil.isInBound(msgDoc.getType())) {

					ChatSessionDoc session = chatSessionDocQuery.getDoc();
					if (!session.stamps().containsKey(ChatSessionDoc.FIRST_INBOUND_STAMP)) {
						chatSessionDocQuery.set(ChatSessionDoc.FIRST_INBOUND_STAMP, now);
					}

					String FIRST_INBOUND_STAMP_MODE = ChatSessionDoc.FIRST_INBOUND_STAMP + "_" + session.getMode();
					if (!session.stamps().containsKey(FIRST_INBOUND_STAMP_MODE)) {
						chatSessionDocQuery.set(FIRST_INBOUND_STAMP_MODE, now);
					}
					chatSessionDocQuery.set(ChatSessionDoc.LAST_INBOUND_STAMP, now);
					chatSessionDocQuery.set(ChatSessionDoc.LAST_INBOUND_STAMP + "_" + session.getMode(), now);

					chatSessionDocQuery.setLastInBoundMsg(msgDoc, iMessage.contact().getContactType());

				} else if (PostManUtil.isOutBound(msgDoc.getType())) {
					ChatSessionDoc session = chatSessionDocQuery.getDoc();
					if (!session.stamps().containsKey(ChatSessionDoc.FIRST_OUTBOUND_STAMP)) {
						chatSessionDocQuery.set(ChatSessionDoc.FIRST_OUTBOUND_STAMP, now);
					}

					String FIRST_INBOUND_STAMP_MODE = ChatSessionDoc.FIRST_OUTBOUND_STAMP + "_" + session.getMode();
					if (!session.stamps().containsKey(FIRST_INBOUND_STAMP_MODE)) {
						chatSessionDocQuery.set(FIRST_INBOUND_STAMP_MODE, now);
					}
					chatSessionDocQuery.set(ChatSessionDoc.LAST_OUTBOUND_STAMP, now);
					chatSessionDocQuery.set(ChatSessionDoc.LAST_OUTBOUND_STAMP + "_" + session.getMode(), now);

					chatSessionDocQuery.setLastOutBoundMsg(msgDoc, iMessage.contact().getContactType());
				}

				chatSessionDocQuery.setLastMsg(msgDoc, iMessage.contact().getContactType());
				sessionStore.updateFirst(chatSessionDocQuery);
			} catch (Exception e) {
				LOGGER.error("SessionStore.push", e);
			}
		}
	}

}
