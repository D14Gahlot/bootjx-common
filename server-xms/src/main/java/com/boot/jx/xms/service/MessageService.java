package com.boot.jx.xms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.chat.ChatService;
import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.store.SessionStore;
import com.boot.jx.xms.dto.OutBoundMsgBasic.OutBoundMsg;
import com.boot.jx.xms.dto.OutBoundReciept;
import com.boot.utils.ArgUtil;

@Component
public class MessageService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ChatService chatService;

    @Autowired
    private SessionStore sessionStore;

    @Autowired
    private PMEnvironment pmEnvironment;

    public OutBoundReciept send(OutBoundMsg message) {
	ChannelConfig channel = pmEnvironment.config().channel(message.getChannelId());

	if (!ArgUtil.is(channel)) {
	    ApiResponseUtil.throwInputException(new ApiFieldError().field("channelId").obzect("OutBoundMsg")
		    .codeKey("CHANNEL_NOT_FOUND").description("Channel : " + message.getChannelId() + " is Not Setup"));
	}

	OutboxMessage outboxMessage = new OutboxMessage();

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

	outboxMessage.contact().type(channel.getContactType());
	outboxMessage.contact().setChannelType(channel.getChannelType());
	outboxMessage.contact().setLane(channel.getLane());

	outboxMessage.contact().copyFrom(message.getToContact());

	ChatSessionDoc chatSessionDoc = sessionStore.linkSession(outboxMessage);
	if (ArgUtil.is(chatSessionDoc)) {
	    chatService.initSession(outboxMessage, chatSessionDoc);
	    chatService.send(chatSessionDoc, outboxMessage);
	} else {
	    ApiResponseUtil.throwInputException(
		    new ApiFieldError().field("to").obzect("OutBoundMsg").codeKey("INSUFFICIENT_CONTACT_DETAILS")
			    .description("Session Cannot be initialized for given contact"));
	}
	String messageId = outboxMessage.getMessageId();
	return new OutBoundReciept().id(messageId);
    }

}
