package com.boot.jx.chat;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.iterators.ArrayListIterator;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.boot.common.ScopedBeanFactory;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;

@Component
public class ConnectorHandlerFactory extends ScopedBeanFactory<String, ConnectorHandler> {

	private static final long serialVersionUID = 4007091611441725719L;

	public interface ConnectorHandler {
		public void sendReply(InboxMessage inboxMessage, OutboxMessage outboxMessage);

		public void assignToAgent(InboxMessage inboxMessage, String deptName);
	}

	public interface DefaultConnector extends ConnectorHandler {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Lazy
	public @interface ConnectorMapping {
		ContactType[] value();

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
			for (ContactType contactType : annotation.value()) {
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
