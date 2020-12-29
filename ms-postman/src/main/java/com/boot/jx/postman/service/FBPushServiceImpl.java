package com.boot.jx.postman.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.boot.jx.api.AmxApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditEvent.Result;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.logger.client.AuditServiceClient;
import com.boot.jx.postman.IPushNotifyService;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.events.UserMessageEvent;
import com.boot.jx.postman.model.Contact;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.rest.RestService;
import com.boot.jx.tunnel.TunnelService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.boot.utils.MapBuilder;
import com.boot.utils.MapBuilder.BuilderMap;

/**
 * The Class FBPushServiceImpl.
 */
@Service
public class FBPushServiceImpl implements IPushNotifyService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerService.getLogger(FBPushServiceImpl.class);

	/** The server key. */
	@Value("${fcm.server.key}")
	String serverKey;

	/** The server key. */
	@Value("${fcm.api.key}")
	String apiKey;

	@Value("${android.app.domain}")
	String androidPackageName;

	@Value("${ios.app.domain}")
	String iosPackageName;

	@Value("${domain.uri.prefix}")
	String domainUriPrefix;

	@Value("${company.app.url}")
	String companyAppUrl;

	/** The rest service. */
	@Autowired
	RestService restService;

	@Autowired
	SlackService slackService;

	/** The audit service client. */
	@Autowired
	AuditServiceClient auditServiceClient;

	@Autowired
	private FileService fileService;

	@Autowired
	private TunnelService tunnelService;

	/** The Constant MAIN_TOPIC. */
	private static final JsonPath MAIN_TOPIC = new JsonPath("/to");
	private static final JsonPath MAIN_CONDITION = new JsonPath("/condition");

	/** The Constant DATA_TITLE. */
	private static final JsonPath DATA_TITLE = new JsonPath("/data/data/title");
	private static final JsonPath DATA_TAG = new JsonPath("/data/data/tag");

	/** The Constant DATA_IS_BG. */
	private static final JsonPath DATA_IS_BG = new JsonPath("/data/data/is_background");

	/** The Constant DATA_MESSAGE. */
	private static final JsonPath DATA_MESSAGE = new JsonPath("/data/data/message");

	/** The Constant DATA_IMAGE. */
	private static final JsonPath DATA_IMAGE = new JsonPath("/data/data/image");

	/** The Constant DATA_PAYLOAD. */
	private static final JsonPath DATA_PAYLOAD = new JsonPath("/data/data/payload");

	/** The Constant DATA_TIMESTAMP. */
	private static final JsonPath DATA_TIMESTAMP = new JsonPath("/data/data/timestamp");

	/** The Constant NOTFY_TITLE. */
	private static final JsonPath NOTFY_TITLE = new JsonPath("/notification/title");
	private static final JsonPath NOTFY_TAG = new JsonPath("/notification/tag");

	/** The Constant NOTFY_SOUND. */
	private static final JsonPath NOTFY_SOUND = new JsonPath("/notification/sound");

	/** The Constant NOTFY_MESSAGE. */
	private static final JsonPath NOTFY_MESSAGE = new JsonPath("/notification/body");

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.amx.jax.postman.FBPushService#sendDirect(com.amx.jax.postman.model.
	 * PushMessage)
	 */
	@Async
	@Override
	public AmxApiResponse<PushMessage, Object> sendDirect(PushMessage msg) {
		try {

			if (ArgUtil.isEmpty(msg.getTo()) && ArgUtil.isEmpty(msg.getContacts())) {
				throw new PostManException(PostManException.ErrorCode.NO_RECIPIENT_DEFINED);
			}

			if (msg.getTemplate() != null) {
				File file = new File();
				file.setTemplate(msg.getTemplate());
				file.setModel(msg.getModel());
				file.setLang(msg.getLang());
				file.setType(File.Type.JSON);

				@SuppressWarnings("unchecked")
				Map<String, Object> map = JsonUtil.fromJson(fileService.create(file, ContactType.FBPUSH).getContent(),
						Map.class);
				msg.setModel(map);

				String message = ArgUtil.parseAsString(map.get("_message"));
				if (!ArgUtil.isEmptyString(message)) {
					msg.setMessage(message);
					map.remove("_message");
				}

				String subject = ArgUtil.parseAsString(map.get("_subject"));
				if (!ArgUtil.isEmptyString(subject)) {
					msg.setSubject(subject);
					map.remove("_subject");
				}

				String image = ArgUtil.parseAsString(map.get("_image"));
				if (!ArgUtil.isEmptyString(image)) {
					msg.setImage(image);
					map.remove("_image");
				}

				String link = ArgUtil.parseAsString(map.get("url"));
				if (!ArgUtil.isEmptyString(link)) {
					msg.setLink(link);
					// map.remove("url");
				}

			}

			UserMessageEvent userMessageEvent = new UserMessageEvent();
			userMessageEvent.setTo(msg.getTo());
			userMessageEvent.setSubject(msg.getSubject());
			userMessageEvent.setMessage(msg.getMessage());
			userMessageEvent.setImage(msg.getImage());
			userMessageEvent.setLink(msg.getLink());
			userMessageEvent.setTemplate(msg.getTemplate());
			userMessageEvent.setContactType(msg.getContactType());
			userMessageEvent.setTimestamp(msg.getTimestamp());
			userMessageEvent.setContacts(msg.getContacts());

			String topic = null;
			String androidTopicStr = null;
			String iosTopicStr = null;
			String webTopicStr = null;

			int totalConditions = 0;
			if (msg.getContacts().size() > 0) {
				StringJoiner orCondition = new StringJoiner(") || (");
				int totalOrConditions = 0;
				for (Contact singleContact : msg.getContacts()) {
					for (Map<String, Object> singleFilter : singleContact.getFilter()) {
						StringJoiner andCondition = new StringJoiner(PushMessage.CONDITION_SEPRATOR_AND);
						for (Entry<String, Object> entry : singleFilter.entrySet()) {
							andCondition.add(
									"'" + PushMessage.topic(entry.getKey(), entry.getValue())
											+ "%sx%' in topics");
							totalConditions++;
						}
						totalConditions++;
						totalOrConditions++;
						orCondition.add(andCondition.toString());
					}
				}
				String orConditionStr = orCondition.toString();
				if (totalOrConditions > 1) {
					orConditionStr = "(" + orConditionStr + ")";
				}
				androidTopicStr = orConditionStr.toString().replaceAll("%sx%", "_and");
				iosTopicStr = orConditionStr.toString().replaceAll("%sx%", "_ios");
				webTopicStr = orConditionStr.toString().replaceAll("%sx%", "_web");
				topic = orConditionStr.toString().replaceAll("%sx%", "");
				
				msg.setCondition(true);
				msg.addTo(topic);
				
			} else {
				topic = msg.getTo().get(0);
				
				StringBuilder androidTopic = new StringBuilder();
				StringBuilder iosTopic = new StringBuilder();
				StringBuilder webTopic = new StringBuilder();

				if (msg.getTo().size() > 1) {
					String seperator = Constants.BLANK;
					for (String singleTopicPath : msg.getTo()) {
						String singleTopic = singleTopicPath.toLowerCase().replace(PushMessage.TOPICS_PREFIX,
								Constants.BLANK);
						androidTopic.append(seperator + "'" + singleTopic + "_and'" + " in topics ");
						iosTopic.append(seperator + "'" + singleTopic + "_ios'" + " in topics ");
						webTopic.append(seperator + "'" + singleTopic + "_web'" + " in topics ");
						seperator = PushMessage.CONDITION_SEPRATOR;
					}
					msg.setCondition(true);
				} else {
					String topicLower = topic.toLowerCase();
					androidTopic.append(topicLower + "_and");
					iosTopic.append(topicLower + "_ios");
					webTopic.append(topicLower + "_web");
				}

				androidTopicStr = androidTopic.toString();
				iosTopicStr = iosTopic.toString();
				webTopicStr = webTopic.toString();
			}

			boolean isMessageSent = false;
			if (!ArgUtil.isEmptyString(topic) || (totalConditions > 0 && totalConditions < 4)) {
				if (msg.getMessage() != null) {
					this.send(PMGaugeEvent.Type.NOTIFCATION_ANDROID, androidTopicStr, msg, msg.getMessage());
					this.send(PMGaugeEvent.Type.NOTIFCATION_IOS, iosTopicStr, msg, msg.getMessage());
					this.send(PMGaugeEvent.Type.NOTIFCATION_WEB, webTopicStr, msg, msg.getMessage());
					userMessageEvent.setMessage(msg.getMessage());
					tunnelService.task(userMessageEvent);
					isMessageSent = true;
				}
				if (msg.getLines() != null) {
					for (String message : msg.getLines()) {
						this.send(PMGaugeEvent.Type.NOTIFCATION_ANDROID, androidTopicStr, msg, message);
						this.send(PMGaugeEvent.Type.NOTIFCATION_IOS, iosTopicStr, msg, message);
						this.send(PMGaugeEvent.Type.NOTIFCATION_WEB, webTopicStr, msg, message);
						userMessageEvent.setMessage(message);
						isMessageSent = true;
						tunnelService.task(userMessageEvent);
					}
				}
			}

			if (!isMessageSent) {
				if (totalConditions > 5) {
					throw new PostManException(PostManException.ErrorCode.TOO_MANY_RECIPIENT);
				}
				throw new PostManException(PostManException.ErrorCode.NO_MESSAGE_DEFINED);
			}

			// tunnelService.task(userMessageEvent);
			if (!ArgUtil.isEmpty(msg.getITemplate())
					&& !ArgUtil.isEmpty(msg.getITemplate().getChannel())) {
				Notipy noti = new Notipy();
				noti.setSubject(msg.getSubject());
				noti.setAuthor(String.format("Topic = %s", msg.getTo().get(0)));
				noti.setMessage(msg.getMessage());
				noti.setLines(msg.getLines());
				noti.setIChannel(msg.getITemplate().getChannel());
				noti.addField("TEMPLATE", msg.getITemplate().toString());
				noti.setColor("#" + CryptoUtil.toHex(6, msg.getITemplate().toString()));
				slackService.sendNotification(noti);
			}

		} catch (PostManException e) {
			auditServiceClient.log(
					new PMGaugeEvent(PMGaugeEvent.Type.NOTIFCATION).result(Result.FAIL, e).set(msg, msg.getMessage(),
							null));
		} catch (Exception e) {
			auditServiceClient.excep(new PMGaugeEvent(PMGaugeEvent.Type.NOTIFCATION).set(msg, msg.getMessage(), null),
					LOGGER, e);
		}

		return AmxApiResponse.build(msg);
	}

	/**
	 * Send.
	 *
	 * @param type    the type
	 * @param topic   the topic
	 * @param msg     the msg
	 * @param message the message
	 */
	@Async
	private void send(PMGaugeEvent.Type type, String topic, PushMessage msg, String message) {
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(type);
		try {
			String response = null;
			if (type == PMGaugeEvent.Type.NOTIFCATION_ANDROID) {
				response = this.sendAndroid(topic, msg, message);
			} else if (type == PMGaugeEvent.Type.NOTIFCATION_IOS) {
				response = this.sendIOS(topic, msg, message);
			} else if (type == PMGaugeEvent.Type.NOTIFCATION_WEB) {
				return;
				// response = this.sendWeb(topic, msg, message);
			} else {
				throw new PostManException(PostManException.ErrorCode.NO_CHANNEL_DEFINED);
			}
			auditServiceClient.log(pMGaugeEvent.set(Result.DONE).set(msg, message, response));
		} catch (PostManException e) {
			auditServiceClient.log(pMGaugeEvent.set(Result.FAIL).set(msg, message, null));
		} catch (Exception e) {
			auditServiceClient.excep(pMGaugeEvent.set(msg, message, null), LOGGER, e);
		}

	}

	/**
	 * Send android.
	 *
	 * @param topic   the topic
	 * @param msg     the msg
	 * @param message the message
	 * @return the string
	 */
	private String sendAndroid(String topic, PushMessage msg, String message) {
		BuilderMap fields = MapBuilder.map()

				.put(DATA_IS_BG, true).put(DATA_TITLE, msg.getSubject()).put(DATA_MESSAGE, message)
				.put(DATA_IMAGE, msg.getImage()).put(DATA_PAYLOAD, msg.getModel())
				.put(DATA_TIMESTAMP, System.currentTimeMillis())
				.put(DATA_TAG, ArgUtil.nonEmpty(msg.getCollapseId(),msg.getId()));

		fields.put(msg.isCondition() ? MAIN_CONDITION : MAIN_TOPIC, topic);

		return restService.ajax("https://fcm.googleapis.com/fcm/send").header("Authorization", "key=" + serverKey)
				.header("Content-Type", "application/json").post(fields.toMap()).asString();
	}

	/**
	 * Send IOS.
	 *
	 * @param topic   the topic
	 * @param msg     the msg
	 * @param message the message
	 * @return the string
	 */
	private String sendIOS(String topic, PushMessage msg, String message) {
		BuilderMap fields = MapBuilder.map()

				.put(DATA_IS_BG, true).put(DATA_TITLE, msg.getSubject()).put(DATA_MESSAGE, message)
				.put(DATA_IMAGE, msg.getImage()).put(DATA_PAYLOAD, msg.getModel())
				.put(DATA_TIMESTAMP, System.currentTimeMillis())
				.put(DATA_TAG, ArgUtil.nonEmpty(msg.getCollapseId(),msg.getId()))

				.put(NOTFY_TITLE, msg.getSubject()).put(NOTFY_MESSAGE, message).put(NOTFY_SOUND, "default")
				.put(NOTFY_TAG, ArgUtil.nonEmpty(msg.getCollapseId(),msg.getId()));

		fields.put(msg.isCondition() ? MAIN_CONDITION : MAIN_TOPIC, topic);

		return restService.ajax("https://fcm.googleapis.com/fcm/send").header("Authorization", "key=" + serverKey)
				.header("Content-Type", "application/json").post(fields.toMap()).asString();

	}

	/**
	 * Send web.
	 *
	 * @param topic   the topic
	 * @param msg     the msg
	 * @param message the message
	 * @return the string
	 */
	private String sendWeb(String topic, PushMessage msg, String message) {
		BuilderMap fields = MapBuilder.map()

				.put(DATA_IS_BG, true).put(DATA_TITLE, msg.getSubject()).put(DATA_MESSAGE, message)
				.put(DATA_IMAGE, msg.getImage()).put(DATA_PAYLOAD, msg.getModel())
				.put(DATA_TIMESTAMP, System.currentTimeMillis())

				.put(NOTFY_TITLE, msg.getSubject()).put(NOTFY_MESSAGE, message).put(NOTFY_SOUND, "default");

		fields.put(msg.isCondition() ? MAIN_CONDITION : MAIN_TOPIC, topic);

		return restService.ajax("https://fcm.googleapis.com/fcm/send").header("Authorization", "key=" + serverKey)
				.header("Content-Type", "application/json").post(fields.toMap()).asString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.amx.jax.postman.FBPushService#subscribe(java.lang.String,
	 * java.lang.String)
	 */
	@Override
	public AmxApiResponse<String, Object> subscribe(String token, String topic) {
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent();
		pMGaugeEvent.setType(PMGaugeEvent.Type.NOTIFCATION_SUBSCRIPTION);
		try {
			String response = restService.ajax("https://iid.googleapis.com/iid/v1/" + token + "/rel/topics/" + topic)
					.header("Authorization", "key=" + serverKey).header("Content-Type", "application/json").post()
					.asString();
			pMGaugeEvent.setResponseText(response);
			auditServiceClient.gauge(pMGaugeEvent);
		} catch (Exception e) {
			auditServiceClient.excep(pMGaugeEvent, LOGGER, e);
			// Slack Exception Handling should be for specific cases
			// slackService.sendException(topic, e);
		}
		return AmxApiResponse.build(token);
	}

	@Override
	public String shortLink(String relativeUrl) {
		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent();
		pMGaugeEvent.setType(PMGaugeEvent.Type.NOTIFCATION_SUBSCRIPTION);
		String response = "";
		try {
			Map<String, Object> androidInfo = new HashMap<String, Object>();
			androidInfo.put("androidPackageName", androidPackageName);
			Map<String, Object> iosInfo = new HashMap<String, Object>();
			iosInfo.put("iosBundleId", iosPackageName);

			Map<String, Object> dynamicLinkInfo = new HashMap<String, Object>();
			dynamicLinkInfo.put("domainUriPrefix", domainUriPrefix);
			dynamicLinkInfo.put("link", companyAppUrl + relativeUrl);
			dynamicLinkInfo.put("androidInfo", androidInfo);
			dynamicLinkInfo.put("iosInfo", iosInfo);

			Map<String, Object> suffix = new HashMap<String, Object>();

			suffix.put("option", "SHORT");

			Map<String, Object> fields = new HashMap<String, Object>();

			fields.put("dynamicLinkInfo", dynamicLinkInfo);
			fields.put("suffix", suffix);
			response = restService.ajax("https://firebasedynamiclinks.googleapis.com/v1/shortLinks?key=" + apiKey)
					.header("Content-Type", "application/json").post(fields)
					.asString();
			pMGaugeEvent.setResponseText(response);
			auditServiceClient.gauge(pMGaugeEvent);
		} catch (Exception e) {
			auditServiceClient.excep(pMGaugeEvent, LOGGER, e);
		}
		Map<String, Object> responseMap;
		try {
			responseMap = jsonToMap(new JSONObject(response));
			if (responseMap.containsKey("shortLink")) {
				return responseMap.get("shortLink").toString();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}

	public Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
		Map<String, Object> retMap = new HashMap<String, Object>();

		if (json != JSONObject.NULL) {
			retMap = toMap(json);
		}
		return retMap;
	}

	public Map<String, Object> toMap(JSONObject object) throws JSONException {
		Map<String, Object> map = new HashMap<String, Object>();

		Iterator<String> keysItr = object.keys();
		while (keysItr.hasNext()) {
			String key = keysItr.next();
			Object value = object.get(key);

			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			map.put(key, value);
		}
		return map;
	}

	public List<Object> toList(JSONArray array) throws JSONException {
		List<Object> list = new ArrayList<Object>();
		for (int i = 0; i < array.length(); i++) {
			Object value = array.get(i);
			if (value instanceof JSONArray) {
				value = toList((JSONArray) value);
			}

			else if (value instanceof JSONObject) {
				value = toMap((JSONObject) value);
			}
			list.add(value);
		}
		return list;
	}

	@Override
	public AmxApiResponse<PushMessage, Object> send(List<PushMessage> msgs) throws PostManException {
		return AmxApiResponse.buildList(msgs);
	}

}
