package com.boot.jx.chat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.common.ScopedBeanFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Component
public class ConnectorHandlerFactory extends ScopedBeanFactory<String, ConnectorHandler> {

	private static final long serialVersionUID = 4007091611441725719L;

	public interface ConnectorHandler {
		default public void reply(InboxMessage inboxMessage, OutboxMessage outboxMessage) {
			outboxMessage.addTo(inboxMessage.getFrom());
			outboxMessage.setLane(inboxMessage.getLane());
			this.send(outboxMessage);
		}

		default public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
			outboxMessage.addTo(chatContactDoc.getCsid());
			outboxMessage.setLane(chatContactDoc.getLane());
			this.send(outboxMessage);
		}

		public InboxMessage assignToAgent(InboxMessage inboxMessage);

		default public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
			return true;
		}

		default public void message(String messageType, ChatContactDoc chatContactDoc, InboxMessage inboxMessage,
				OutboxMessage outboxMessage) {
			try {
				switch (messageType) {
				case "SEND":
					this.send(chatContactDoc, outboxMessage);
					break;
				case "REPLY":
					this.reply(inboxMessage, outboxMessage);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				outboxMessage.setStatus(Message.Status.SENT_ERR);
				outboxMessage.logs().add(e.getMessage());
			}

		}

		void send(OutboxMessage outboxMessage);

	}

	public interface DefaultConnector extends ConnectorHandler {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Lazy
	public @interface ConnectorMapping {
		ContactType[] contactType();

		String[] channel() default "DEFAULT";
	}

	public ConnectorHandlerFactory(List<ConnectorHandler> libs) {
		super(libs);
	}

	@Override
	public String[] getKeys(ConnectorHandler lib) {
		ConnectorMapping annotation = lib.getClass().getAnnotation(ConnectorMapping.class);
		List<String> zoom = new ArrayList<String>();
		if (annotation != null) {
			for (ContactType contactType : annotation.contactType()) {
				for (String channel : annotation.channel()) {
					zoom.add(String.format("%s_%s", contactType, channel));
				}
			}
			return zoom.toArray(new String[0]);
		}
		return null;
	}

	public ConnectorHandler get(ContactType contactType, String channel) {
		String precisedKey = String.format("%s_%s", contactType, channel);
		ConnectorHandler x = this.get(precisedKey);
		if (ArgUtil.is(x)) {
			return x;
		}
		precisedKey = String.format("%s_DEFAULT", contactType);
		return this.get(precisedKey);
	}

}
