package com.boot.jx.outbound;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.chat.ChatSessionFactory;
import com.boot.jx.chat.ChatSessionService;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMConstants.MESSAGE_SENDER_TYPE;
import com.boot.jx.postman.PMContextUtil;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.MessageDefinitions.ContactID;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard.OutBoundMsgContactAddress;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard.OutBoundMsgContactEmail;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard.OutBoundMsgContactPhone;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard.OutBoundMsgContactSocial;
import com.boot.jx.postman.model.outbound.CommonMsgContactCard.OutBoundMsgContactUrl;
import com.boot.jx.postman.model.outbound.OutBoundMsgBasic.OutBoundMsg;
import com.boot.jx.postman.model.outbound.OutBoundReciept;
import com.boot.jx.postman.pbook.PBDate;
import com.boot.jx.postman.pbook.PBLocation;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBSocial;
import com.boot.jx.postman.pbook.PBVCard;
import com.boot.jx.postman.pbook.PBWebsite;
import com.boot.jx.postman.pbook.PBWork;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;

@Component
public class MessageService {

	@Autowired
	private ChatService chatService;

	@Autowired
	private ChatSessionService chatSessionService;

	@Autowired
	private ChatSessionFactory chatSessionFactory;

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private MessageStore messageStore;

	public OutBoundReciept send(OutBoundMsg message) {

		if (!ArgUtil.is(message.getChannelId())) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("channelId").obzect("OutBoundMsg")
					.codeKey("CHANNEL_MISSING").description("channelId is Missing"));
		}

		ChannelConfig channel = pmEnvironment.config().channel(message.getChannelId());

		if (!ArgUtil.is(channel)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("channelId").obzect("OutBoundMsg")
					.codeKey("CHANNEL_NOT_FOUND").description("Channel : " + message.getChannelId() + " is Not Setup"));
		}

		if (channel.isDisabled() || channel.isDeleted()) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("channelId").obzect("OutBoundMsg")
					.codeKey("CHANNEL_DISABLED").description("Channel : " + message.getChannelId() + " is Disabled"));
		}

		if (ArgUtil.is(message.getMessageIdResend())) {
			MessageDoc resendMsg = messageStore.findByMessageId(message.getMessageIdResend(), channel.getContactType());

			if (!ArgUtil.is(resendMsg)) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("messageIdResend").obzect("OutBoundMsg")
						.codeKey("MESSAGE_NOT_FOUND").description("Message cannot be Resent"));
			}
			OutboxMessage outboxmessage = ChatDTOUtil.toOutboxMessage(resendMsg);
			outboxmessage.setMessageIdResend(message.getMessageIdResend());
			return send(channel, resendMsg.getContact(), outboxmessage);
		} else {
			return send(message, channel);
		}
	}

	public OutBoundReciept send(OutBoundMsg message, ChannelConfig channel) {

		if (!ArgUtil.is(message.getType())) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("type").obzect("OutBoundMsg")
					.codeKey("TYPE_MISSING").description("Message Type is missing")
					.possibleValues("text", "template", "audio", "video", "image", "document"));
		}

		OutboxMessage outboxMessage = new OutboxMessage();
		outboxMessage.setFormatType(message.getType());

		if ("text".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getText()) || !ArgUtil.is(message.getText().getBody())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("text").obzect("OutBoundMsg")
						.codeKey("TEXT_DETAILS_MISSING").description("Text Body is missing"));
			}
			outboxMessage.setMessage(message.getText().getBody());
		}

		if ("template".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getTemplate())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("template").obzect("OutBoundMsg")
						.codeKey("TEMPLATE_DETAILS_MISSING").description("Template details is missing"));
			}

			if (!ArgUtil.is(message.getTemplate().getId()) && !ArgUtil.is(message.getTemplate().getCode())) {
				ApiResponseUtil.throwInputException(
						new ApiFieldError().field("template").obzect("OutBoundMsg").codeKey("TEMPLATE_DETAILS_MISSING")
								.description("Either template.id or template.code is required"));
			}

			outboxMessage.setHsm(message.getTemplate());
			outboxMessage.setModelData(message.getTemplate().data());
		}

		if ("document".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getDocument())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("document").obzect("OutBoundMsg")
						.codeKey("DOCUMENT_DETAILS_MISSING").description("Document details is missing"));
			}
		}

		if ("image".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getImage())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("image").obzect("OutBoundMsg")
						.codeKey("IMAGE_DETAILS_MISSING").description("Image details is missing"));
			}
		}

		if ("video".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getVideo())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("video").obzect("OutBoundMsg")
						.codeKey("VIDEO_DETAILS_MISSING").description("Video details is missing"));
			}
		}

		if ("audio".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getAudio())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("audio").obzect("OutBoundMsg")
						.codeKey("AUDIO_DETAILS_MISSING").description("Audio details is missing"));
			}
		}

		if ("location".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getLocation())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("location").obzect("OutBoundMsg")
						.codeKey("LOCATION_DETAILS_MISSING").description("Location details is missing"));
			}
		}

		if ("contacts".equalsIgnoreCase(message.getType())) {
			if (!ArgUtil.is(message.getContacts())) {
				ApiResponseUtil.throwInputException(new ApiFieldError().field("contacts").obzect("OutBoundMsg")
						.codeKey("CONTACTS_DETAILS_MISSING").description("Contacts details is missing"));
			}
		}

		if (ArgUtil.is(message.getDocument())) {
			outboxMessage.attachment(new Attachment().mediaURL(message.getDocument().getLink())
					.mediaName(message.getDocument().getFilename()).mediaCaption(message.getDocument().getCaption())
					.mediaType(FileType.DOCUMENT.toString()));
		}

		if (ArgUtil.is(message.getImage())) {
			outboxMessage.attachment(
					new Attachment().mediaURL(message.getImage().getLink()).mediaName(message.getImage().getFilename())
							.mediaCaption(message.getImage().getCaption()).mediaType(FileType.IMAGE.toString()));
		}
		if (ArgUtil.is(message.getVideo())) {
			outboxMessage.attachment(
					new Attachment().mediaURL(message.getVideo().getLink()).mediaName(message.getVideo().getFilename())
							.mediaCaption(message.getVideo().getCaption()).mediaType(FileType.VIDEO.toString()));
		}

		if (ArgUtil.is(message.getAudio())) {
			outboxMessage.attachment(
					new Attachment().mediaURL(message.getAudio().getLink()).mediaName(message.getAudio().getFilename())
							.mediaCaption(message.getAudio().getCaption()).mediaType(FileType.AUDIO.toString()));
		}

		if (ArgUtil.is(message.getLocation())) {
			PBLocation pbLocation = new PBLocation();
			pbLocation.setName(message.getLocation().name);
			pbLocation.setAddress(message.getLocation().address);
			pbLocation.setLatitude(message.getLocation().latitude);
			pbLocation.setLongitude(message.getLocation().longitude);
			pbLocation.setUrl(message.getLocation().url);
			outboxMessage.vccards().add(new PBVCard().locations(pbLocation));
		}

		if (ArgUtil.is(message.getContacts())) {
			for (CommonMsgContactCard contact : message.getContacts()) {
				PBVCard pbVCard = new PBVCard();

				if (ArgUtil.is(contact.name)) {
					PBName pbName = new PBName();
					pbName.setFirstName(contact.name.first_name);
					pbName.setLastName(contact.name.last_name);
					pbName.setFormattedName(contact.name.formatted_name);
					pbVCard.setName(pbName);
				}

				if (ArgUtil.is(contact.emails)) {
					for (OutBoundMsgContactEmail email : contact.emails) {
						pbVCard.emails().add(email.toEmail());
					}
				}
				if (ArgUtil.is(contact.phones)) {
					for (OutBoundMsgContactPhone phone : contact.phones) {
						pbVCard.phones().add(phone.toPhone());
					}
				}

				if (ArgUtil.is(contact.addresses)) {
					for (OutBoundMsgContactAddress address : contact.addresses) {
						pbVCard.addresses().add(address.toAddress());
					}
				}

				if (ArgUtil.is(contact.birthday)) {
					PBDate pbDate = new PBDate();
					pbDate.setType("birthday");
					pbDate.setDate(contact.birthday);
					pbVCard.dates().add(pbDate);
				}

				if (ArgUtil.is(contact.org)) {
					PBWork pbWork = new PBWork();
					pbWork.setCompany(contact.org.company);
					pbWork.setDepartment(contact.org.department);
					pbWork.setTitle(contact.org.title);
					pbVCard.work().add(pbWork);
				}

				if (ArgUtil.is(contact.urls)) {
					for (OutBoundMsgContactUrl url : contact.urls) {
						PBWebsite pnWebsite = new PBWebsite();
						pnWebsite.setUrl(url.url);
						pnWebsite.setType(url.type);
						pbVCard.urls().add(pnWebsite);
					}
				}
				if (ArgUtil.is(contact.ims)) {
					for (OutBoundMsgContactSocial ims : contact.ims) {
						PBSocial pnWebsite = new PBSocial();
						pnWebsite.setService(ims.service);
						pnWebsite.setUserid(ims.userid);
						pbVCard.ims().add(pnWebsite);
					}
				}
				outboxMessage.vccards().add(pbVCard);
			}
		}

		if (ArgUtil.is(message.getOptions())) {
			if (ArgUtil.is(message.getOptions().buttons)) {
				outboxMessage.option("buttons", message.getOptions().buttons);
			}
			if (ArgUtil.is(message.getOptions().buttonDisplayScheme)) {
				outboxMessage.option("is_list", "list".equalsIgnoreCase(message.getOptions().buttonDisplayScheme));
			}
		}

		return send(channel, message.getToContact(), outboxMessage);
	}

	public OutBoundReciept send(String channelId, OutboxMessage outboxMessage) {
		ChannelConfig channel = pmEnvironment.config().channel(channelId);

		return send(channel, outboxMessage.getContact(), outboxMessage);
	}

	private OutBoundReciept send(ChannelConfig channel, ContactID contact, OutboxMessage outboxMessage) {
		ClientApp clientApp = PMContextUtil.clientApp();

		outboxMessage.contact().type(channel.getContactType());
		outboxMessage.contact().setChannelType(channel.getChannelType());
		outboxMessage.contact().setLane(channel.getLane());

		if (ArgUtil.is(contact.phone()) && contact.phone().startsWith("+")) {
			contact.phone(StringUtils.removeSpaces(contact.phone().replaceFirst("\\+", "")));
		}

		outboxMessage.contact().copyFrom(contact);

		outboxMessage.route().setQueueCode(clientApp.getQueue());
		outboxMessage.route().setSendMode(clientApp.getAppMode());
		outboxMessage.route().setSenderApp(clientApp.getAppType());
		outboxMessage.route()
				.setSenderType(ArgUtil.parseAsString(clientApp.props().get("sender_type"), MESSAGE_SENDER_TYPE.API));

		ChatSessionDoc chatSessionDoc = chatSessionFactory.linkSession(outboxMessage);

		if (ArgUtil.is(chatSessionDoc)) {
			chatSessionService.initSession(outboxMessage, chatSessionDoc);
			chatService.send(chatSessionDoc, outboxMessage);
		} else {
			ApiResponseUtil.throwInputException(
					new ApiFieldError().field("to").obzect("OutBoundMsg").codeKey("INSUFFICIENT_CONTACT_DETAILS")
							.description("Session Cannot be initialized for given contact"));
		}
		String messageId = outboxMessage.getMessageId();
		OutBoundReciept outBoundReciept = new OutBoundReciept();
		outBoundReciept.setId(messageId);
		outBoundReciept.setTemplateCode(outboxMessage.templateCode());
		outBoundReciept.setTemplateId(outboxMessage.getHsm().getId());
		outBoundReciept.setType(ArgUtil.nonEmpty(outboxMessage.getType(), "O"));
		return outBoundReciept;
		// return new OutBoundReciept().id(messageId);
	}

	public OutBoundReciept resend(OutBoundMsg message) {

		return send(message);
	}

}
