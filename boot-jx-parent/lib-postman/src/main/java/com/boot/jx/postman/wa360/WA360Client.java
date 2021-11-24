package com.boot.jx.postman.wa360;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.dict.FileType;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpClientException;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.fb.FacebookMessageResp;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.wa360.WA360Constants.OutBoundWrapperPaths;
import com.boot.jx.postman.wa360.WA360Constants.TmplComponent;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;

@Component
public class WA360Client {

    @Autowired
    private RestService restService;

    public OutboxMessage send(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	StringJoiner msgIds = new StringJoiner(",");

	if (ArgUtil.is(outboxMessage.getTemplateExt())) {
	    MapModel resp = sendTemplate(channelConfig, outboxMessage);
	    msgIds.add(getMessageId(resp));
	} else {
	    boolean isList = false;
	    boolean isButton = false;
	    List<TmplElement> buttons = null;
	    if (outboxMessage.options().containsKey("buttons")) {
		buttons = new MapModel(outboxMessage.options()).entry("buttons").asList(TmplElement.class);
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
	}

	outboxMessage.setMessageIdExt(msgIds.toString());
	return outboxMessage;
    }

    private MapModel sendTemplate(ChannelConfig channelConfig, OutboxMessage outboxMessage) {
	MapModel req = MapModel.createInstance().put("recipient_type", "individual").put("to",
		outboxMessage.contact().getCsid());

	MapModel extTemplate = MapModel.from(outboxMessage.getTemplateExt().getTemplate());
	MapModel model = MapModel.from(outboxMessage.getModel());
	MapModel varMap = MapModel.from(outboxMessage.getTemplateExt().getVarMap());

	req.put(OutBoundWrapperPaths.MESSAGE_TYPE, "template");
	req.put(OutBoundWrapperPaths.TEMPLATE_NAMESPACE, extTemplate.get("namespace"));
	req.put(OutBoundWrapperPaths.TEMPLATE_NAME, extTemplate.get("name"));
	req.put(OutBoundWrapperPaths.TEMPLATE_LANGUAGE_CODE, extTemplate.get("language"));
	req.put(OutBoundWrapperPaths.TEMPLATE_LANGUAGE_POLICY, "deterministic");

	MapModel components = MapModel.createInstance();
	List<Map<String, Object>> extTemplateComponents = extTemplate.keyEntry("components").asListOfMap();

	for (Map<String, Object> extTemplateComponent : extTemplateComponents) {
	    String extTemplateComponentType = (String) extTemplateComponent.get("type");
	    if ("HEADER".equals(extTemplateComponentType)) {
		TmplComponent headerComponentReq = TmplComponent.createInstance().header();
		String extTemplateComponentFormat = (String) extTemplateComponent.get("format");
		if ("TEXT".equals(extTemplateComponentFormat)) {
		    if (varMap.containsKey("header")) {
			List<Map<String, Object>> headerParametersTemp = varMap.entry("header").asListOfMap();
			for (Map<String, Object> headerParameter : headerParametersTemp) {
			    String path = (String) headerParameter.get("path");
			    headerComponentReq.parameter("text", model.pathEntry(path).asString());
			}
			components.add(headerComponentReq.build().map());
		    }
		} else if (ArgUtil.is(outboxMessage.getAttachments())) {
		    String lowerFormat = extTemplateComponentFormat.toLowerCase();
		    WA360OutBoundMedia media = createMedia(lowerFormat, outboxMessage.getAttachments().get(0));
		    headerComponentReq.parameter(lowerFormat, media);
		    components.add(headerComponentReq.build().map());
		}

	    } else if ("BODY".equals(extTemplateComponentType)) {
		if (varMap.containsKey("body")) {
		    List<Map<String, Object>> bodyParametersTemp = varMap.entry("body").asListOfMap();
		    TmplComponent bodyComponent = TmplComponent.createInstance().body();
		    for (Map<String, Object> bodyParameter : bodyParametersTemp) {
			String path = (String) bodyParameter.get("path");
			bodyComponent.parameter("text", model.pathEntry(path).asString());
		    }
		    components.add(bodyComponent.build().map());
		}
	    } else if ("BUTTONS".equals(extTemplateComponentType)) {
		if (varMap.containsKey("buttons")) {

		    List<Map<String, Object>> extTemplateComponentButtons = MapModel.from(extTemplateComponent)
			    .keyEntry("buttons").asListOfMap();
		    List<Map<String, Object>> buttonsParametersVars = varMap.entry("buttons").asListOfMap();

		    for (int i = 0; i < extTemplateComponentButtons.size(); i++) {
			Map<String, Object> extTemplateComponentButton = extTemplateComponentButtons.get(i);
			Map<String, Object> buttonParameterVar = buttonsParametersVars.get(i);
			String buttonType = (String) extTemplateComponentButton.get("type");
			if ("URL".equals(buttonType)) {
			    if (buttonParameterVar.containsKey("path")) {
				String path = (String) buttonParameterVar.get("path");
				TmplComponent buttonComponent = TmplComponent.createInstance().button("url", i);
				buttonComponent.parameter("text", model.pathEntry(path).asString());
				components.add(buttonComponent.build().map());
			    }
			} else if ("QUICK_REPLY".equals(buttonType)) {
			    if (buttonParameterVar.containsKey("path")) {
				String path = (String) buttonParameterVar.get("path");
				TmplComponent buttonComponent = TmplComponent.createInstance().button("quick_reply", i);
				buttonComponent.parameter("payLoad", model.pathEntry(path).asString());
				components.add(buttonComponent.build().map());
			    }

			}
		    }

		}
	    }

	}

	req.put(OutBoundWrapperPaths.TEMPLATE_COMPONENTS, components.list());
	return send(req, channelConfig);
    }

    private WA360OutBoundMedia createMedia(String mediaType, Attachment attachment) {
	WA360OutBoundMedia wa360OutBoundMedia = new WA360OutBoundMedia();
	wa360OutBoundMedia.setCaption(ArgUtil.nonEmpty(attachment.getMediaCaption()));
	wa360OutBoundMedia.setLink(attachment.getMediaURL());
	wa360OutBoundMedia.setFilename(attachment.getMediaName());
	if (mediaType.equalsIgnoreCase("image")) {
	    wa360OutBoundMedia.setFilename(null);
	}
	return wa360OutBoundMedia;
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
	try {
	    MapModel resp = restService.ajax(WA360Constants.BASE_URL).path("v1/messages")
		    .header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey()).post(req.toMap())
		    .asMapModel();
	    return resp;
	} catch (ApiHttpClientException e) {
	    return MapModel.from(e.getResponse().getBody());
	}
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

    public MapModel createTemplates(ChannelConfig channelConfig, MapModel req) {
	try {
	    MapModel resp = restService.ajax(WA360Constants.BASE_URL).path("v1/configs/templates")
		    .header(WA360Constants.D360_API_KEY, channelConfig.getWa360d().getApiKey()).post(req.toMap())
		    .asMapModel();

	    return resp;
	} catch (HttpStatusCodeException | ApiHttpException e) {
	    if (e instanceof HttpStatusCodeException)
		ApiResponseUtil.addError(((HttpStatusCodeException) e).getResponseBodyAsString());
	    else
		ApiResponseUtil.addError(((ApiHttpException) e));
	    throw e;
	}

    }

}
