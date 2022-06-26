package com.boot.jx.inbound;

import java.io.IOException;
import java.util.Calendar;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMessage;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;

import org.apache.commons.mail.util.MimeMessageParser;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.chat.ChatStatusService;
import com.boot.jx.connectors.EmailConnector;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.utils.ArgUtil;
import com.boot.utils.CloseUtil;

@EnableScheduling
@Component
public class InBoundPoller extends ATaskLimiter {

	public static final String TASK_EMAIL_POLLER = "EMAIL_POLLER";

	private static final Logger LOGGER = LoggerService.getLogger(InBoundPoller.class);

	@Autowired
	private AppConfig appConfig;

	@Autowired
	private PMEnvironment env;

	@Autowired
	private EmailConnector emailConnector;

	@Autowired
	private InBoundService inBoundService;

	@Autowired
	private ChatStatusService inBoundStatusService;

	@Override
	public String getVersion() {
		return "6";
	}

	@Override
	public boolean isWorker() {
		return ArgUtil.isEqual(appConfig.getAppType(), "POSTMAN", "ADMIN", "ACCOUNT");
	}

	@Override
	public void doTask(TunnelTask task) {
		if (ArgUtil.is(task.getName(), TASK_EMAIL_POLLER)) {
			String channelId = task.data().getString("channelId");
			ChannelConfig channel = env.local().channel(channelId);

			if (!ArgUtil.is(channel)) {
				LOGGER.warn("No Channel found {}", channelId);
				return;
			}

			Session session = emailConnector.getMailSession(channel);

			if (!ArgUtil.is(session)) {
				LOGGER.warn("No Session Created for {}", channelId);
				return;
			}

			MessageBoxEvent messageBoxEvent = new MessageBoxEvent();
			Folder folder = null;
			Store store = null;
			try {

				// Get a Store object and connect to the current host
				store = session.getStore("pop3s");
				LOGGER.debug("store.connect {} {} {}", channel.getEmail().getPop3Host(),
						channel.getEmail().getPop3User(), channel.getEmail().getPop3Pass());
				store.connect(channel.getEmail().getPop3Host(), channel.getEmail().getPop3User(),
						channel.getEmail().getPop3Pass());
				// change the user and password accordingly

				folder = store.getFolder("inbox");
				if (!folder.exists()) {
					LOGGER.warn("Inbox not found for task {} {}", task.getId(), channelId);
					return;
				}

				folder.open(Folder.READ_WRITE);

				Calendar c = Calendar.getInstance();
				c.add(Calendar.HOUR, -1);

				SearchTerm newerThan = new SentDateTerm(ComparisonTerm.GT, c.getTime());
				Message[] messages = folder.search(newerThan);

				LOGGER.debug("Inbox Messages {} = {}", task.getId(), messages.length);

				if (messages.length != 0) {

					for (int i = 0, n = messages.length; i < n; i++) {

						try {
							Message message = messages[i];
							try {
								MimeMessageParser email = new MimeMessageParser((MimeMessage) message).parse();
								InboxMessage msg = emailConnector.toInboxMessage(channel, email);
								if (ArgUtil.is(msg)) {
									messageBoxEvent.addInboxMessage(msg);
								}

							} catch (MessagingException | IOException e) {
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							message.setFlag(Flags.Flag.DELETED, true);
						} catch (MessagingException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

					}
				}
				// close the store and folder objects
				closeAll(folder, store);
			} catch (MessagingException e2) {
				LOGGER.error("For Channel {} {} {}", channel.getEmail().getPop3Host(), channel.getEmail().getPop3Port(),
						channel.getEmail().getPop3User());
				LOGGER.error("Eexception==e2", e2);
			} catch (Exception e3) {
				LOGGER.error("For Channel {} {} {}", channel.getEmail().getPop3Host(), channel.getEmail().getPop3Port(),
						channel.getEmail().getPop3User());
				LOGGER.error("Eexception==e3", e3);
			} finally {
				closeAll(folder, store);
			}

			if (ArgUtil.is(messageBoxEvent.getInboxMessages())) {
				emailConnector.beforeReceiveInboxMessage(messageBoxEvent.getInboxMessages());
				messageBoxEvent.getInboxMessages().forEach(inboxMessage -> {
					emailConnector.prompt(inboxMessage);
					inBoundService.pushMessageToInvokeAsync(inboxMessage);
				});
				emailConnector.onReceiveInboxMessage(messageBoxEvent.getInboxMessages());
			} else if (ArgUtil.is(messageBoxEvent.getMessageReports())) {
				emailConnector.onMessageReports(messageBoxEvent.getMessageReports());
				inBoundStatusService.update(messageBoxEvent.getMessageReports());
			}

		}
	}

	private void closeAll(Folder folder, Store store) {
		if (folder != null && folder.isOpen()) {
			try {
				folder.close(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (store != null) {
			try {
				store.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
