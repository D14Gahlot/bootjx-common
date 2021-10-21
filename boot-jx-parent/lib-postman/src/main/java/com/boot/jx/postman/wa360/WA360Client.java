package com.boot.jx.postman.wa360;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMConfiguration;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Constants.OutBoundWrapperPaths;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;

@Component
public class WA360Client {

    @Autowired
    private PMEnvironment environment;

    @Autowired
    private RestService restService;

    public OutboxMessage send(OutboxMessage outboxMessage) {
	PMConfiguration config = environment.config();
	String channelId = PostManUtil.CHANNEL_ID(outboxMessage.contact());
	ChannelConfig channelConfig = config.channels(channelId);
	StringJoiner msgIds = new StringJoiner(",");

	boolean isList = false;
	boolean isButton = false;
	List<TmplElement> buttons = null;
	if (outboxMessage.options().containsKey("buttons")) {
	    buttons = new MapModel(outboxMessage.options()).entry("buttons").asList(new TmplElement());
	    isList = (buttons.size() > 3);
	    isButton = (buttons.size() > 0) && (buttons.size() < 4);
	}

	if (isList) {
	    MapModel resp = sendList(channelConfig, outboxMessage, buttons);
	    msgIds.add(getMessageId(resp));
	} else if (isButton) {
	    MapModel resp = sendButton(channelConfig, outboxMessage, buttons);
	    msgIds.add(getMessageId(resp));
	} else {
	    if (ArgUtil.is(outboxMessage.getMessage())) {
		MapModel resp = sendText(channelConfig, outboxMessage);
		msgIds.add(getMessageId(resp));
	    }
	    if (ArgUtil.is(outboxMessage.getAttachments())) {
		for (Attachment attachment : outboxMessage.getAttachments()) {
		    MapModel resp = sendMedia(channelConfig, outboxMessage, attachment);
		    msgIds.add(getMessageId(resp));
		}
	    }
	}

	outboxMessage.setMessageIdExt(msgIds.toString());
	return outboxMessage;
    }

    private MapModel sendText(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	MapModel req = MapModel.createInstance().put("recipient_type", "individual").put("to",
		outboxMessage.contact().getCsid());

	req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "text");
	req.put(OutBoundWrapperPaths.MESSAGE_TEXT, outboxMessage.getMessage());

	return send(req, channelConfig);
    }

    private MapModel sendMedia(ChannelConfig channelConfig, OutboxMessage outboxMessage, Attachment attachment) {
	MapModel req = MapModel.createInstance().put("recipient_type", "individual").put("to",
		outboxMessage.contact().getCsid());

	WA360OutBoundMedia wa360OutBoundMedia = new WA360OutBoundMedia();
	wa360OutBoundMedia.setCaption(ArgUtil.nonEmpty(attachment.getMediaCaption(), outboxMessage.getSubject()));
	wa360OutBoundMedia.setLink(attachment.getMediaURL());
	wa360OutBoundMedia.setFilename(attachment.getMediaName());

	if (ArgUtil.areEqual(attachment.getMediaType(), FileType.IMAGE.toString())) {
	    req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "image");
	    wa360OutBoundMedia.setFilename(null);
	    req.put("image", wa360OutBoundMedia);
	} else if (ArgUtil.areEqual(attachment.getMediaType(), FileType.VIDEO.toString())) {
	    req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "video");
	    req.put("video", wa360OutBoundMedia);
	} else if (ArgUtil.areEqual(attachment.getMediaType(), FileType.AUDIO.toString())) {
	    req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "audio");
	    req.put("audio", wa360OutBoundMedia);
	} else {
	    req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "document");
	    req.put("document", wa360OutBoundMedia);
	}

	return send(req, channelConfig);
    }

    private MapModel sendList(ChannelConfig channelConfig, OutboxMessage outboxMessage, List<TmplElement> buttons) {
	MapModel req = MapModel.createInstance().put("recipient_type", "individual").put("to",
		outboxMessage.contact().getCsid());

	req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "interactive");

	req.put(new JsonPath("/interactive/type"), "list");

	req.put(OutBoundWrapperPaths.INTERACTIVE_HEADER_TYPE, "text");
	req.put(OutBoundWrapperPaths.INTERACTIVE_HEADER_TEXT,
		ArgUtil.parseAsString(outboxMessage.getSubject(), Constants.BLANK));
	req.put(OutBoundWrapperPaths.INTERACTIVE_BODY_TEXT, outboxMessage.getMessage());
	req.put(OutBoundWrapperPaths.INTERACTIVE_FOOTER_TEXT,
		ArgUtil.parseAsString(outboxMessage.getFooter(), Constants.BLANK));
	req.put(OutBoundWrapperPaths.INTERACTIVE_ACTION_BUTTON, "menu");

	List<Object> sections = new ArrayList<Object>();
	Map<String, Object> section = null;
	List<Object> rows = null;

	for (TmplElement button : buttons) {
	    if (section == null) {
		section = new HashMap<String, Object>();
		sections.add(section);
	    }
	    if (rows == null) {
		rows = new ArrayList<Object>();
		// section.put("title", "Section Title");
		section.put("rows", rows);
	    }

	    Map<String, Object> row = new HashMap<String, Object>();
	    row.put("id", button.getName());
	    row.put("title", button.getLabel());
	    row.put("description", button.getType());
	    rows.add(row);

	    if (rows.size() > 9) {
		section = null;
		rows = null;
	    }
	}
	req.put(OutBoundWrapperPaths.INTERACTIVE_ACTION_SECTIONS, sections);
	return send(req, channelConfig);
    }

    private MapModel sendButton(ChannelConfig channelConfig, OutboxMessage outboxMessage, List<TmplElement> buttons) {
	MapModel req = MapModel.createInstance().put("recipient_type", "individual").put("to",
		outboxMessage.contact().getCsid());

	req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "interactive");
	req.put(new JsonPath("/interactive/type"), "button");

	MapModel intr = MapModel.createInstance();
	if (ArgUtil.is(outboxMessage.getAttachments())) {
	    Attachment attachment = outboxMessage.getAttachments().get(0);
	    WA360OutBoundMedia wa360OutBoundMedia = new WA360OutBoundMedia();
	    // wa360OutBoundMedia.setCaption(ArgUtil.nonEmpty(attachment.getMediaCaption(),
	    // outboxMessage.getSubject()));
	    wa360OutBoundMedia.setLink(attachment.getMediaURL());
	    wa360OutBoundMedia.setFilename(attachment.getMediaName());
	    if (ArgUtil.areEqual(attachment.getMediaType(), FileType.IMAGE.toString())) {
		intr.put(OutBoundWrapperPaths.MESSAGE_TYPE, "image");
		wa360OutBoundMedia.setFilename(null);
		intr.put("image", wa360OutBoundMedia);
	    } else if (ArgUtil.areEqual(attachment.getMediaType(), FileType.VIDEO.toString())) {
		intr.put(OutBoundWrapperPaths.MESSAGE_TYPE, "video");
		intr.put("video", wa360OutBoundMedia);
	    } else {
		intr.put(OutBoundWrapperPaths.MESSAGE_TYPE, "document");
		intr.put("document", wa360OutBoundMedia);
	    }
	} else {
	    intr.put(OutBoundWrapperPaths.INTERACTIVE_HEADER_TYPE, "text");
	    intr.put(OutBoundWrapperPaths.INTERACTIVE_HEADER_TEXT,
		    ArgUtil.nonEmpty(outboxMessage.getSubject(), Constants.BLANK));
	}
	req.put(new JsonPath("interactive/header"), intr.toMap());

	req.put(OutBoundWrapperPaths.INTERACTIVE_BODY_TEXT, outboxMessage.getMessage());
	req.put(OutBoundWrapperPaths.INTERACTIVE_FOOTER_TEXT,
		ArgUtil.parseAsString(outboxMessage.getFooter(), Constants.BLANK));
	req.put(OutBoundWrapperPaths.INTERACTIVE_ACTION_BUTTON, "menu");

	List<Object> rows = new ArrayList<Object>();
	for (TmplElement button : buttons) {
	    rows.add(MapModel.createInstance().put("type", "reply")
		    .put(OutBoundWrapperPaths.INTERACTIVE_ACTION_REPLY_ID, button.getName())
		    .put(OutBoundWrapperPaths.INTERACTIVE_ACTION_REPLY_TITLE, button.getLabel()).toMap());
	}
	req.put(OutBoundWrapperPaths.INTERACTIVE_ACTION_BUTTONS, rows);
	return send(req, channelConfig);
    }

    private MapModel send(MapModel req, ChannelConfig channelConfig) {
	MapModel resp = restService.ajax(WA360Constants.BASE_URL).path("v1/messages")
		.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey()).post(req.toMap())
		.asMapModel();
	return resp;
    }

    private String getMessageId(MapModel resp) {
	String id = resp.entry(OutBoundWrapperPaths.RESPONSE_MSG_ID).asString();
	String errorCode = resp.entry(OutBoundWrapperPaths.RESPONSE_ERROR_CODE).asString();
	if (ArgUtil.is(errorCode)) {
	    String errorTitle = resp.entry(OutBoundWrapperPaths.RESPONSE_ERROR_TITLE).asString();
	    String errorDetails = resp.entry(OutBoundWrapperPaths.RESPONSE_ERROR_DETAILS).asString();
	    throw new PostManException(String.format("%s : %s / %s / %s ", id, errorCode, errorTitle, errorDetails));
	}
	return id;
    }

    public MapModel fetchTemplates(ChannelConfig channelConfig) {
	MapModel resp = restService.ajax(WA360Constants.BASE_URL).path("v1/configs/templates")
		.header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey()).get().asMapModel();
	return resp;
    }

}
