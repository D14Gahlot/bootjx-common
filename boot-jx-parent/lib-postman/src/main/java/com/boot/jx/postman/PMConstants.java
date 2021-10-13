package com.boot.jx.postman;

import com.boot.jx.dict.ContactType;
import com.boot.utils.ArgUtil;

public class PMConstants {

    public final class DEFAULT {

	public static final String SYSTEM = "__SYSTEM__";
	public static final String NO_DEPT = "__DEPT__";
	public static final String NO_USER = "__USER__";

    }

    public final class USER_ROLE {
	public static final String BUSINESS_USER = "BUSINESS_USER";
	public static final String DUPER_USER = "DUPER_USER";
	public static final String ADMIN = "ADMIN";
	public static final String AGENT = "AGENT";
    }

    public static class MESSAGE_BOUND_TYPE {
	public static final String INBOUND = "I";
	public static final String INBOUND_IMPORTED = "Ii";

	public static final String OUTBOUND = "O";
	public static final String OUTBOUND_IMPORTED = "Oi";
    }

    public final class CHANNEL_TYPE {
	public static final String WA_GUPSHUP_LEGACY = "GUPSHUPW";
	public static final String TELEGRAM = "tg";
	public static final String TWITTER = "tw";
	public static final String FACEBOOK = "fb";
	public static final String WA_GUPSHUP = "wags";
	public static final String WA_360D = "wa360";
	public static final String WEB = "web";
    }

    public enum CHANNEL_TYPE_ENUM {
	tg, tw, fb, wags, wa360, web
    }

    public static enum CHAT_STATUS {
	OPEN, UNASSIGNED, URGENT, ONHOLD, ATTENTION, EXPIRED, RESOLVED, CLOSED;
    }

    public static enum CHAT_MODE {
	AGENT, BOT;
    }

    public static class ASSIGNMENT_RULE {
	public static final String MANUAL = "MANUAL";
	public static final String ROUND_ROBIN = "ROUND_ROBIN";
	public static final String STRICT_DEFAULT = "STRICT_DEFAULT";
    }

    public static class CHAT_SESSION_STICKY {
	public static final String NONE = "NONE";
	public static final String ONAVAILABLE = "ONAVAILABLE";
	public static final String STRICT = "STRICT"; // TODO:-
    }

    public static String CHANNEL_TYPE(String contactType, String channel) {
	CHANNEL_TYPE_ENUM channelEnum = ArgUtil.parseAsEnumT(channel, CHANNEL_TYPE_ENUM.class, null);
	if (ArgUtil.is(channelEnum)) {
	    return channel;
	}
	if (ContactType.FACEBOOK.toString().equals(contactType)) {
	    return CHANNEL_TYPE.FACEBOOK;
	} else if (ContactType.TWITTER.toString().equals(contactType)) {
	    return CHANNEL_TYPE.TWITTER;
	} else if (ContactType.TELEGRAM.toString().equals(contactType)) {
	    return CHANNEL_TYPE.TELEGRAM;
	} else if (ContactType.WEBSITE.toString().equals(contactType)) {
	    return CHANNEL_TYPE.WEB;
	} else if (ContactType.WHATSAPP.toString().equals(contactType)) {
	    if (CHANNEL_TYPE.WA_GUPSHUP_LEGACY.equals(channel)) {
		return CHANNEL_TYPE.WA_GUPSHUP;
	    }
	}

	return null;
    }

    public final class PostManUrls {

	private PostManUrls() {
	}

	public static final String SEND_MESSAGE_BOX = "/postman/messagebox/send";
	public static final String SEND_SMS = "/postman/sms/send";
	public static final String SEND_EMAIL = "/postman/email/send";
	public static final String SEND_EMAIL_BULK = "/postman/email/sendBulk";
	public static final String SEND_EMAIL_DB = "/postman/email/";
	public static final String SEND_EMAIL_OLD = "/email/api/send/transaction/email";
	public static final String SEND_EMAIL_SUPPORT = "/postman/email/support";
	public static final String NOTIFY_SLACK = "/postman/slack/notify";
	public static final String NOTIFY_PUSH = "/postman/push/notify";
	public static final String NOTIFY_PUSH_BULK = "/postman/push/bulk_notify";
	public static final String NOTIFY_PUSH_SUBSCRIBE = "/postman/subscribe/{topic}";
	public static final String NOTIFY_SLACK_EXCEP = "/postman/slack/exception";
	public static final String NOTIFY_SLACK_EXCEP_REPORT = "/postman/slack/excep_report";
	public static final String PROCESS_TEMPLATE = "/postman/template/process";
	public static final String PROCESS_TEMPLATE_FILE = "/postman/template/file";
	public static final String PROCESS_TEMPLATE_FILE_LOCAL = "local";
	public static final String WHATS_APP_SEND = "/postman/whatsapp/send";
	public static final String WHATS_APP_SEND_BULK = "/postman/whatsapp/send_bulk";
	public static final String WHATS_APP_RESEND = "/postman/whatsapp/resend";
	public static final String WHATS_APP_STATUS = "/postman/whatsapp/status";
	public static final String WHATS_APP_STATS = "/postman/whatsapp/stats";
	public static final String WHATS_APP_POLL = "/postman/whatsapp/poll";
	public static final String SHORT_LINK = "/postman/shortlink";

	public static final String GEO_LOC = "/geo/location";
	public static final String EVENT_PUBLISH = "/event/publish/{event}/{id}";

	public static final String LIST_TENANT = "/postman/list/tenant";
	public static final String LIST_NATIONS = "/postman/list/nations";
	public static final String LIST_BRANCHES = "/postman/list/branches";

	public static final String DOC_UPLOAD_URL = "/upload/url";
	public static final String DOC_UPLOAD_FILE = "/upload/file";
	public static final String DOC_VALIDATE_ID = "/validate/id";
	public static final String DOC_IMAGE_BY_ID = "/image/{image_id}.{ext}";
	public static final String DOC_VIEW_BY_ID = "/view/{image_id}.{ext}";
	public static final String DOC_VIEW_BY_ID_JSON = "/view/{image_id}.json";
	public static final String DOC_URL_BY_ID = "/url/{image_id}.{ext}";
	public static final String DOC_SCAN_ID = "/scan/id";

    }

}
