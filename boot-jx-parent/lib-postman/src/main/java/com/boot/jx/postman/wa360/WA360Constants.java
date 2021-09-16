package com.boot.jx.postman.wa360;

import com.boot.utils.JsonPath;

public class WA360Constants {

    public static class InBoundWrapperPaths {

	public static final JsonPath CONTACT_NAME = new JsonPath("contacts/[0]/profile/name");
	public static final JsonPath CONTACT_NUMBER = new JsonPath("contacts/[0]/wa_id");
	public static final JsonPath MESSAGE_TYPE = new JsonPath("messages/[0]/type");
	public static final JsonPath MESSAGE_ID = new JsonPath("messages/[0]/id");
	public static final JsonPath MESSAGE_TEXT = new JsonPath("messages/[0]/text/body");

	public static final JsonPath INTERACTIVE_TYPE = new JsonPath("messages/[0]/interactive/type");
	public static final JsonPath INTERACTIVE_LIST_REPLY = new JsonPath("messages/[0]/interactive/list_reply/title");
	public static final JsonPath INTERACTIVE_LIST_ID = new JsonPath("messages/[0]/interactive/list_reply/id");
	public static final JsonPath INTERACTIVE_LIST_DESC = new JsonPath(
		"messages/[0]/interactive/list_reply/description");
	public static final JsonPath INTERACTIVE_BUTTON_REPLY = new JsonPath(
		"messages/[0]/interactive/button_reply/title");
	public static final JsonPath INTERACTIVE_BUTTON_ID = new JsonPath("messages/[0]/interactive/button_reply/id");

	public static final JsonPath IMAGE = new JsonPath("messages/[0]/image");
	public static final JsonPath IMAGE_ID = new JsonPath("messages/[0]/image/id");
	public static final JsonPath IMAGE_TYPE = new JsonPath("messages/[0]/image/mime_type");
	public static final JsonPath IMAGE_CAPTION = new JsonPath("messages/[0]/image/caption");

	public static final JsonPath DOCUMENT = new JsonPath("messages/[0]/document");
	public static final JsonPath DOCUMENT_ID = new JsonPath("messages/[0]/document/id");
	public static final JsonPath DOCUMENT_TYPE = new JsonPath("messages/[0]/document/mime_type");
	public static final JsonPath DOCUMENT_NAME = new JsonPath("messages/[0]/document/filename");
	public static final JsonPath DOCUMENT_CAPTION = new JsonPath("messages/[0]/document/caption");

	public static final JsonPath AUDIO = new JsonPath("messages/[0]/audio");
	public static final JsonPath AUDIO_ID = new JsonPath("messages/[0]/audio/id");
	public static final JsonPath AUDIO_TYPE = new JsonPath("messages/[0]/audio/mime_type");
	public static final JsonPath AUDIO_CAPTION = new JsonPath("messages/[0]/audio/caption");

	public static final JsonPath VOICE = new JsonPath("messages/[0]/voice");
	public static final JsonPath VIDEO = new JsonPath("messages/[0]/video");
	public static final JsonPath STICKER = new JsonPath("messages/[0]/sticker");

    }

    public static class OutBoundWrapperPaths {
	public static final String MESSAGE_TYPE = "type";
	public static final JsonPath MESSAGE_TEXT = new JsonPath("text/body");

	public static final JsonPath INTERACTIVE_HEADER_TYPE = new JsonPath("interactive/header/type");
	public static final JsonPath INTERACTIVE_HEADER_TEXT = new JsonPath("interactive/header/text");
	public static final JsonPath INTERACTIVE_BODY_TEXT = new JsonPath("interactive/body/text");
	public static final JsonPath INTERACTIVE_FOOTER_TEXT = new JsonPath("interactive/footer/text");
	public static final JsonPath INTERACTIVE_ACTION_BUTTON = new JsonPath("interactive/action/button");
	public static final JsonPath INTERACTIVE_ACTION_BUTTONS = new JsonPath("interactive/action/buttons");
	public static final JsonPath INTERACTIVE_ACTION_SECTIONS = new JsonPath("interactive/action/sections");

	public static final JsonPath INTERACTIVE_ACTION_REPLY_ID = new JsonPath("reply/id");
	public static final JsonPath INTERACTIVE_ACTION_REPLY_TITLE = new JsonPath("reply/title");

	public static final JsonPath RESPONSE_MSG_ID = new JsonPath("messages/[0]/id");
	public static final JsonPath RESPONSE_ERROR_CODE = new JsonPath("errors/[0]/code");
	public static final JsonPath RESPONSE_ERROR_TITLE = new JsonPath("errors/[0]/title");
	public static final JsonPath RESPONSE_ERROR_DETAILS = new JsonPath("errors/[0]/details");
    }

    public static final String D360_API_KEY = "D360-API-KEY";
    public static final String BASE_URL = "https://waba.360dialog.io";

    public static String MEDIA_URL(String mediaId) {
	return BASE_URL + "/v1/media/" + mediaId;
    }
}
