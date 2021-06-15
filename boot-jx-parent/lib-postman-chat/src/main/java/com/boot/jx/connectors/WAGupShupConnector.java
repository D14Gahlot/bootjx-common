package com.boot.jx.connectors;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorHandler;
import com.boot.jx.chat.ConnectorHandlerFactory.ConnectorMapping;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.client.TmplClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.TemplateReply;
import com.boot.jx.postman.gupshup.GupShupClientChat;
import com.boot.jx.postman.gupshup.GupShupClientNotify;
import com.boot.jx.postman.gupshup.GupShupConfigClient;
import com.boot.jx.postman.gupshup.GupShupDeliveryResp;
import com.boot.jx.postman.gupshup.GupShupDeliveryResp.GupShupDeliveryDto;
import com.boot.jx.postman.gupshup.GupShupInbound;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.IMessage.SessionMessage;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.JsonUtil;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = "GUPSHUPW")
public class WAGupShupConnector implements ConnectorHandler {

	@Autowired
	private GupShupClientChat gupShupChatClient;

	@Autowired
	private GupShupClientNotify gupShupNotifyClient;

	@Autowired
	protected GupShupConfigClient gupShupConfig;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private TmplClient tmplClient;

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Override
	public void send(ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		outboxMessage.setChannel(chatContactDoc.getChannelType());
		outboxMessage.setLane(chatContactDoc.getLane());
		String to = CollectionUtil.getOne(outboxMessage.getTo());
		gupShupNotifyClient.send(outboxMessage);
	}

	@Override
	public void reply(SessionMessage inboxMessage, OutboxMessage outboxMessage) {
		try {
			outboxMessage.setLane(inboxMessage.getLane());
			if (ArgUtil.is(outboxMessage.getTemplate())) {
				TemplateReply templateReply = mongoTemplate.findById(outboxMessage.getTemplate(), TemplateReply.class);
				if (ArgUtil.is(templateReply)) {
					if ("image".equalsIgnoreCase(templateReply.getType())) {
						outboxMessage.attachment(
								new Attachment().mediaURL(templateReply.getUrl()).mediaType(FileType.IMAGE.toString()));
						outboxMessage = gupShupChatClient.send(outboxMessage);
					}
				} else {
					tmplClient.process(outboxMessage);
					outboxMessage = gupShupChatClient.send(outboxMessage);
				}
			} else {
				outboxMessage = gupShupChatClient.send(outboxMessage);
			}
			outboxMessage.updateStatus(Message.Status.SENT);
		} catch (Exception e) {
			outboxMessage.logs().add(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public InboxMessage assignToAgent(InboxMessage inboxMessage) {
		return inboxMessage;
	}

	@Override
	public boolean initSession(ChatContactDoc contact, ChatSessionDoc session, InboxMessage inboxMessage) {
		if (ArgUtil.is(inboxMessage.getOriginalMessage())) {
			GupShupInbound dm = JsonUtil.parse(inboxMessage.getOriginalMessage(), GupShupInbound.class);
			contact.setName(dm.getName());
			contact.setPhone(dm.getMobile());
		}
		return true;
	}

	public InboxMessage toInboxMessage(GupShupInbound inbound) {
		InboxMessage inboxMessage = new InboxMessage();
		inboxMessage.setContactType(ContactType.WHATSAPP);
		inboxMessage.setChannel("GUPSHUPW");
		inboxMessage.setFrom(inbound.getMobile());
		inboxMessage.setFromName(inbound.getName());
		inboxMessage.setMessage(inbound.getText());
		inboxMessage.setTo(inbound.getWaNumber());
		inboxMessage.setMessageIdExt(inbound.getReplyId());
		inboxMessage.setLane(inbound.getWaNumber());
		inboxMessage.setCsid(inbound.getMobile());

		if (ArgUtil.is(inbound.getImage())) {
			CommonFile srcFile = new CommonFile().url(inbound.getImage().getUrl() + inbound.getImage().getSignature())
					.fileType(FileType.IMAGE).format(FileFormat.from(inbound.getImage().getMimeType()));

			CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
					PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());

			inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
					.mediaSrc(srcFile.getUrl()).mediaCaption(inbound.getImage().getCaption()));
		} else if (ArgUtil.is(inbound.getDocument())) {
			CommonFile srcFile = new CommonFile()
					.url(inbound.getDocument().getUrl() + inbound.getDocument().getSignature())
					.fileType(FileType.DOCUMENT).format(FileFormat.from(inbound.getDocument().getMimeType()));

			CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
					PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());

			inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
					.mediaSrc(srcFile.getUrl()).mediaCaption(inbound.getDocument().getCaption()));
		}

		return inboxMessage;
	}

	@Override
	public void send(OutboxMessage outboxMessage) {
		// TODO Auto-generated method stub
	}

	public List<MessageReport> updateDeliveryStatus(GupShupDeliveryResp status) {
		List<MessageReport> batch = new LinkedList<MessageReport>();
		for (GupShupDeliveryDto gupShupDelivery : status.getResponse()) {
			MessageReport report = new MessageReport();
			report.setContactType(ContactType.WHATSAPP);
			report.setTimestamp(gupShupDelivery.getEventTs());

			report.setMessageIdExt(gupShupDelivery.getExternalId());
			String[] x = gupShupDelivery.getExternalId().split("-");
			if (x.length == 2) {
				report.setMessageId(x[1]);
			}
			if ("SENT".equals(gupShupDelivery.getEventType())) {
				report.setStatus(Status.SENTX);
			} else if ("DELIVERED".equals(gupShupDelivery.getEventType())) {
				report.setStatus(Status.DLVRD);
			} else if ("READ".equals(gupShupDelivery.getEventType())) {
				report.setStatus(Status.READ);
			} else if ("FAILED".equals(gupShupDelivery.getEventType())) {
				report.setStatus(Status.FAILD);
				if ("BLOCKED_FOR_USER".equalsIgnoreCase(gupShupDelivery.getCause())) {
					report.setStatus(Status.BLCKD);
				}
				report.setReason(gupShupDelivery.getCause());
			}
			batch.add(report);
		}
		return batch;
	}

}
