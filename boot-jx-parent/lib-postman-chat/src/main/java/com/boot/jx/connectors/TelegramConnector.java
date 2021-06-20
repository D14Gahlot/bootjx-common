package com.boot.jx.connectors;

import java.util.Comparator;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickMedia;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.tg.TelegramClient;
import com.boot.jx.postman.tg.TelegramModels.TGFile;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.TELEGRAM)
public class TelegramConnector implements ConnectorHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(TelegramConnector.class);

	@Autowired
	private TelegramClient telegramClient;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	public void send(OutboxMessage outboxMessage) {
		try {
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				QuickMedia mediaReply = mongoTemplate.findById(outboxMessage.getTemplate(), QuickMedia.class);
				if (ArgUtil.is(mediaReply)) {
					if ("image".equalsIgnoreCase(mediaReply.getType())) {
						outboxMessage.attachment(new Attachment().mediaURL(mediaReply.getUrl())
								.mediaType(FileType.IMAGE.toString()).mediaCaption(mediaReply.getTitle()));
						telegramClient.send(outboxMessage);
					}
				} else {
					tmplClient.process(outboxMessage);
					telegramClient.send(outboxMessage);
				}
			} else {
				telegramClient.send(outboxMessage);
			}
			outboxMessage.updateStatus(OutboxMessage.Status.SENT);
		} catch (Exception e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(e.getMessage());
			LOGGER.error("SEND ERROR", e);
		}

	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	public InboxMessage toInboxMessage(String lane, Update update) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setOriginalMessage(update);
		inboxMessage.contact().setContactType(ContactType.TELEGRAM.toString());
		inboxMessage.contact().setLane(lane);
		inboxMessage.setFrom(ArgUtil.parseAsString(update.getMessage().getChatId()));
		inboxMessage.contact().setCsid(ArgUtil.parseAsString(update.getMessage().getChatId()));

		if (ArgUtil.is(update.getMessage())) {
			inboxMessage.setMessageIdExt(
					String.format("%s-%s", update.getMessage().getChatId(), update.getMessage().getMessageId()));

			inboxMessage.setMessage(update.getMessage().getText());
			if (ArgUtil.is(update.getMessage().getPhoto())) {
				Optional<PhotoSize> photo = update.getMessage().getPhoto().stream()
						.max(Comparator.comparing(PhotoSize::getWidth));

				if (photo.isPresent()) {
					TGFile file = telegramClient.getFile(lane, photo.get().getFileId());
					/**
					 * Telegram Does not provide Image, so explicitly set Image File Type
					 */
					CommonFile srcFile = new CommonFile().url(file.getFileUrl()).fileType(FileType.IMAGE);
					CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
							PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());

					inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
							.mediaCaption(update.getMessage().getCaption()));
				}
			} else if (ArgUtil.is(update.getMessage().getDocument())) {
				TGFile file = telegramClient.getFile(lane, update.getMessage().getDocument().getFileId());
				/**
				 * Telegram Does not provide Image, so explicitly set Image File Type
				 */
				CommonFile srcFile = new CommonFile().url(file.getFileUrl()).fileType(FileType.DOCUMENT);
				CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
						PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());

				inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
						.mediaCaption(update.getMessage().getCaption()));
			}

		}
		return inboxMessage;
	}

	@Override
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {

		Update update = JsonUtil.parse(inboxMessage.getOriginalMessage(), Update.class);

		if (ArgUtil.is(update) && ArgUtil.is(update.getMessage()) && ArgUtil.is(update.getMessage().getFrom())
				&& ArgUtil.is(update.getMessage().getContact())) {

			if (ArgUtil.isEqual(update.getMessage().getFrom().getId(), update.getMessage().getContact().getUserID())) {
				contact.setName(update.getMessage().getFrom().getFirstName() + " "
						+ update.getMessage().getFrom().getLastName());
				contact.setPhone(update.getMessage().getContact().getPhoneNumber());

			}

		}

		if (ArgUtil.isEmpty(contact.getPhone())) {
			telegramClient.promptShareNumber(inboxMessage.getFrom(),
					"Confirm that you would like to share your contact number and continue, by clicking on the button below",
					inboxMessage.contact().getLane());
			return false;
		}

		return true;
	}

}
