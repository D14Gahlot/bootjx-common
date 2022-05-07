package com.boot.jx.connectors;

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;

import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.email.EmailReplyParser;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider;
import com.boot.jx.postman.plugin.EmailPlugin;
import com.boot.jx.postman.plugin.EmailPlugin.EmailConfigDetails;
import com.boot.jx.postman.wa360.WA360Client;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
@ConnectorMapping(contactType = ContactType.EMAIL, channel = CHANNEL_TYPE.EMAIL)
public class EmailConnector extends AbstractConnector<EmailConfigDetails, EmailPlugin> {

	@Override
	public EmailPlugin getPlugin() {
		return ChannelPluginProvider.EMAIL;
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(EmailConnector.class);
	@Autowired
	private RestService restService;

	@Autowired
	private WA360Client wa360Client;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig) {
		String webhookUrl = pmClientConfig.getWebhookUrl(channelConfig);
		restService.ajax(WA360Constants.BASE_URL).path("v1/configs/webhook")
				.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey())
				.post(MapModel.createInstance().put("url", webhookUrl).toMap()).asMap();
	}

	public Session getMailSession(ChannelConfig channel) {

		EmailConfigDetails emailConfig = channel.getEmail();

		if (!ArgUtil.is(emailConfig)) {
			LOGGER.warn("No Email Config found {}", channel.getChannelId());
			return null;
		}

		Properties properties = new Properties();
		properties.put("mail.store.protocol", ArgUtil.nonEmpty(emailConfig.getProtocol(), "pop3"));
		properties.put("mail.pop3s.host", emailConfig.getPop3Host());
		properties.put("mail.pop3s.port", ArgUtil.nonEmpty(emailConfig.getPop3Port(), "995"));
		properties.put("mail.pop3.starttls.enable", emailConfig.isPop3StartTls());
		properties.put("mail.smtp.auth", emailConfig.isSmtpAuth());
		properties.put("mail.smtp.starttls.enable", emailConfig.isSmtpStartTls());
		properties.put("mail.smtp.host", emailConfig.getSmtpHost());
		properties.put("mail.smtp.port", emailConfig.getSmtpPort());
		return Session.getDefaultInstance(properties);
	}

	public InboxMessage toInboxMessage(ChannelConfig channelConfig, MimeMessageParser email) throws Exception {

		// Create Default Message from Channel
		InboxMessage inboxMessage = this.createInboxMessage(channelConfig);
		// Set Contact info
		InternetAddress from = null;
		try {
			from = CollectionUtil
					.first(InternetAddress.parse(InternetAddress.toString(email.getMimeMessage().getFrom())));
		} catch (MessagingException e) {
			e.printStackTrace();
		}

		if (!ArgUtil.is(from)) {
			return null;
		}

		inboxMessage.contact().setCsid(from.getAddress());
		inboxMessage.contact().setName(from.getPersonal());
		inboxMessage.contact().setEmail(from.getAddress());

		// Set Additional info
		inboxMessage.setFrom(from.getAddress());
		inboxMessage.setFromName(from.getPersonal());
		inboxMessage.to().add(channelConfig.getLane());

		// Extract Message Details
		inboxMessage.setMessageIdExt(CollectionUtil.first(email.getMimeMessage().getHeader("Message-ID")));
		inboxMessage.setReplyIdExt(CollectionUtil.first(email.getMimeMessage().getHeader("In-Reply-To")));
		inboxMessage.setSubject(email.getSubject());
		inboxMessage.setMessage(EmailReplyParser.parseReply(email.getPlainContent()));

		return inboxMessage;
	}

	@Override
	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
			template(channelConfig, chatContactDoc, outboxMessage); // TODO:- This is common for all connector, make it
			// generic

			Session session = getMailSession(channelConfig);

			if (!ArgUtil.is(session)) {
				outboxMessage.logs().add(String.format("No Session Created for %s", chatContactDoc));
				outboxMessage.updateStatus(OutboxMessage.Status.NSENT);
				return;
			}

			MimeMessage replyMessage = new MimeMessage(session);
			replyMessage.setFrom(new InternetAddress(channelConfig.getEmail().getSmtpUser(), channelConfig.getName()));
			replyMessage.addRecipient(RecipientType.TO, new InternetAddress(outboxMessage.contact().getCsid()));
			replyMessage.setSubject(ArgUtil.nonEmpty(outboxMessage.getSubject(), channelConfig.getName()));
			replyMessage.setText(outboxMessage.getMessage());
			Transport t = session.getTransport("smtp");
			try {
				// connect to the smpt server using transport instance
				// change the user and password accordingly
				t.connect(channelConfig.getEmail().getSmtpUser(), channelConfig.getEmail().getSmtpPass());
				t.sendMessage(replyMessage, replyMessage.getAllRecipients());
			} finally {
				t.close();
			}

		} catch (Exception e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			String log = null;
			if (e instanceof AmxApiException) {
				outboxMessage.logs().add(((AmxApiException) e).getErrorKey());
			}
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		}
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		return messageBoxEvent;
	}

}
