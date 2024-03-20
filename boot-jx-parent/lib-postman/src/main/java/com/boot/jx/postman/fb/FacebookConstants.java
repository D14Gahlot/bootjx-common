package com.boot.jx.postman.fb;

import com.boot.jx.postman.model.MessageCategoryType;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonPath;

public class FacebookConstants {

	public static final String D360_API_KEY = "D360-API-KEY";
	public static final String BASE_URL = "https://waba.360dialog.io";

	public static String MEDIA_URL(String mediaId) {
		return BASE_URL + "/v1/media/" + mediaId;
	}

	public static class InBoundWrapperPaths {
		public static final JsonPath STORY_ID = new JsonPath("story/id");
		public static final JsonPath STORY_URL = new JsonPath("story/url");
	}

	public static class MessageTypes {
		public static final String CONFIRMED_EVENT_UPDATE = "CONFIRMED_EVENT_UPDATE";
		public static final String POST_PURCHASE_UPDATE = "POST_PURCHASE_UPDATE";
		public static final String ACCOUNT_UPDATE = "ACCOUNT_UPDATE";
		public static final String CUSTOMER_FEEDBACK = "CUSTOMER_FEEDBACK";
		public static final String HUMAN_AGENT = "HUMAN_AGENT";
	}

	public static class WABAPaths {
		public static final JsonPath DISPLAY_PHONE_NUMBER = new JsonPath("value/metadata/display_phone_number");
		public static final JsonPath PHONE_NUMBER_ID = new JsonPath("value/metadata/phone_number_id");
	}

	public static String MESSAGE_TAG(String messageType) {

		if (!ArgUtil.is(messageType)) {
			return null;
		}

		switch (messageType) {
		case MessageCategoryType.AUTO_REPLY:
			return null;

		// ACCOUNT_UPDATE
		case MessageCategoryType.ACCOUNT_UPDATE:
		case MessageCategoryType.ALERT_UPDATE:
			return MessageTypes.ACCOUNT_UPDATE;

		// CONFIRMED_EVENT_UPDATE
		case MessageCategoryType.RESERVATION_UPDATE:
		case MessageCategoryType.APPOINTMENT_UPDATE:
			return MessageTypes.CONFIRMED_EVENT_UPDATE;

		// POST_PURCHASE_UPDATE
		case MessageCategoryType.PAYMENT_UPDATE:
		case MessageCategoryType.PERSONAL_FINANCE_UPDATE:
		case MessageCategoryType.SHIPPING_UPDATE:
		case MessageCategoryType.TICKET_UPDATE:
		case MessageCategoryType.TRANSPORTATION_UPDATE:
			return MessageTypes.POST_PURCHASE_UPDATE;

		case MessageCategoryType.CUSTOMER_FEEDBACK:
			return MessageTypes.CUSTOMER_FEEDBACK;

		case MessageCategoryType.ISSUE_RESOLUTION:
		case MessageCategoryType.HUMAN_AGENT:
			return MessageTypes.HUMAN_AGENT;
		}

		return null;
	}
}
