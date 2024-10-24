package com.boot.jx.connectors;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.auth.AuthStateManager.AuthState;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.FileType;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.model.CommonFile;
import com.boot.jx.model.CommonFileStream;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.PMConstants.MESSAGE_COMPOSE_TYPE;
import com.boot.jx.postman.PMConstants.MESSAGE_FORMAT_TYPE;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.client.PMFileStoreClient;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.config.ChannelConfigLogger;
import com.boot.jx.postman.doc.tpo.WABAFlows;
import com.boot.jx.postman.fb.FacebookConstants;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.MessageReport;
import com.boot.jx.postman.model.MessageReport.MessageReportError;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBDate;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBLocation;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBSocial;
import com.boot.jx.postman.pbook.PBVCard;
import com.boot.jx.postman.pbook.PBWebsite;
import com.boot.jx.postman.pbook.PBWork;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ConnectorMapping;
import com.boot.jx.postman.plugin.WacfbPlugin;
import com.boot.jx.postman.plugin.WacfbPlugin.WACFBConfigDetails;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.postman.query.WABAConversationQuery;
import com.boot.jx.postman.wa360.WA360Constants;
import com.boot.jx.postman.wa360.WA360Constants.InBoundWrapperPaths;
import com.boot.jx.postman.wacfb.WacfbClient;
import com.boot.jx.postman.wacfb.WacfbInboundMedia;
import com.boot.jx.rest.RestService;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.model.MapModel.MapPathEntry;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.boot.utils.PhoneUtil;
import com.boot.utils.Random;
import com.boot.utils.Urly;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
@ConnectorMapping(contactType = ContactType.WHATSAPP, channel = CHANNEL_TYPE.WACFB)
public class WacfbConnector extends AbstractConnector<WACFBConfigDetails, WacfbPlugin> {

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(WacfbConnector.class);
	@Autowired
	private RestService restService;

	@Autowired
	private PMFileStoreClient pmFileStoreClient;

	@Autowired
	private WacfbClient wacfbClient;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Autowired
	private CommonMongoTemplate commonMongoTemplate;

	@Override
	public List<ChannelConfig> onRegister(ChannelConfig setup, ChannelConfigLogger channelConfigTemp, AuthState state) {
		List<ChannelConfig> channels = new ArrayList<ChannelConfig>();
		try {
			MapModel resp = MapModel.from(channelConfigTemp.getResp());

			MapModel accessToken = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path("oauth/access_token")
					.field("client_id", setup.getWacfb().getMasterAppId())
					.field("client_secret", setup.getWacfb().getMasterAppSecret())
					.field("code", resp.pathEntry("authResponse.code").asString()).submit().asMapModel();

			channelConfigTemp.log("oauth/access_token", accessToken.toMap());

			String userAccessToken = accessToken.keyEntry("access_token").asString();
			String assignedWaBaId = resp.pathEntry("_.waba_id").asString();
			String phoneNumberId = resp.pathEntry("_.phone_number_id").asString();

			if (!ArgUtil.is(assignedWaBaId)) {
				MapModel debugToken = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path("debug_token")
						.queryParam("input_token", userAccessToken)
						.authBearer(setup.getWacfb().getMasterAppId() + "|" + setup.getWacfb().getMasterAppSecret())
						.get().asMapModel();
				channelConfigTemp.log("/debug_token", debugToken.toMap());
				assignedWaBaId = debugToken.pathEntry("/data/granular_scopes/[0]/target_ids/[0]").asString();
			}

			String verificationPin = ArgUtil.nonEmpty(setup.getWacfb().getVerificationPin(), Random.randomNumeric(6));

			if (ArgUtil.is(phoneNumberId)) {
				MapModel phoneMap = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(phoneNumberId)
						.authBearer(userAccessToken).get().asMapModel();
				
				Map<String, Object> meta = setup.meta();				
				if (ArgUtil.is(phoneMap)) {
					meta.put("status ", phoneMap.toObject());
					}
				setup.setMeta(meta);
		 			
				channelConfigTemp.log("/phone_number_by_id", phoneMap.toMap());

				ChannelConfig channel = new ChannelConfig();
				channel.setWacfb(new WACFBConfigDetails());
				channel.getWacfb().setAccessToken(userAccessToken);
				channel.getWacfb().setNumber(PhoneUtil.phone(phoneMap.keyEntry("display_phone_number").asString()));
				channel.getWacfb().setPhoneNumberId(phoneMap.keyEntry("id").asString());
				channel.getWacfb().setVerificationPin(verificationPin);
				channel.getWacfb().setVerifyToken(setup.getWacfb().getMasterAppVerifyToken());
				channel.getWacfb().setWabaId(assignedWaBaId);
				channel.getWacfb().setMasterAppId(setup.getWacfb().getMasterAppId());
				channel.getWacfb().setMasterAppConfigId(setup.getWacfb().getMasterAppConfigId());
				channel.setName(phoneMap.keyEntry("verified_name").asString());
				channels.add(channel);
       
				MapModel subscribeResp = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(assignedWaBaId)
						.path("/subscribed_apps").authBearer(setup.getWacfb().getMasterSUAccessToken()).post()
						.asMapModel();

				channelConfigTemp.log("/subscribed_apps", subscribeResp.toMap());

				boolean isRegistered = false;
				boolean isPinSet = false;
				try {
					MapModel setPinResp = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(phoneNumberId)
							.authBearer(userAccessToken)
							.postJson(MapModel.createInstance().put("pin", verificationPin).toMap()).asMapModel();
					channelConfigTemp.log("/setPin", setPinResp.toMap());
					isPinSet = true;
				} catch (ApiHttpException e) {
					channelConfigTemp.log("/setPin", MapModel.from(e.getResponse().getBody()).toMap());
					MapModel registerResp = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(phoneNumberId)
							.path("/register").authBearer(userAccessToken).postJson(MapModel.createInstance()
									.put("pin", verificationPin).put("messaging_product", "whatsapp").toMap())
							.asMapModel();
					isRegistered = true;
					channelConfigTemp.log("/register", registerResp.toMap());
				}

				if (!isRegistered) {
					MapModel registerResp = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(phoneNumberId)
							.path("/register").authBearer(userAccessToken).postJson(MapModel.createInstance()
									.put("pin", verificationPin).put("messaging_product", "whatsapp").toMap())
							.asMapModel();
					channelConfigTemp.log("/register", registerResp.toMap());
				}
				if (!isPinSet) {
					MapModel setPinResp = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(phoneNumberId)
							.authBearer(userAccessToken)
							.postJson(MapModel.createInstance().put("pin", verificationPin).toMap()).asMapModel();
					channelConfigTemp.log("/setPin", setPinResp.toMap());
				}

			} else {
				MapModel phoneNumbers = restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(assignedWaBaId)
						.path("/phone_numbers").authBearer(userAccessToken).get().asMapModel();
				
				channelConfigTemp.log("/phone_numbers", phoneNumbers.toMap());
				final String assignedWaBaIdFinal = assignedWaBaId;

				phoneNumbers.keyEntry("data").asListOfMap().forEach(phone -> {
					MapModel phoneMap = MapModel.from(phone);
					ChannelConfig channel = new ChannelConfig();
					channel.setWacfb(new WACFBConfigDetails());
					channel.getWacfb().setAccessToken(userAccessToken);
					channel.getWacfb().setNumber(PhoneUtil.phone(phoneMap.keyEntry("display_phone_number").asString()));
					channel.getWacfb().setPhoneNumberId(phoneMap.keyEntry("id").asString());
					// channel.getWacfb().setVerificationPin(verificationPin);
					channel.getWacfb().setWabaId(assignedWaBaIdFinal);
					channel.getWacfb().setMasterAppId(setup.getWacfb().getMasterAppId());
					channel.getWacfb().setMasterAppConfigId(setup.getWacfb().getMasterAppConfigId());
					channel.setName(phoneMap.keyEntry("verified_name").asString());
					channels.add(channel);
				});
			}

		} catch (ApiHttpException e) {
			channelConfigTemp.log("exception", MapModel.from(e.getResponse().getBody()).toMap());
		}
		commonMongoTemplate.save(channelConfigTemp);
		return channels;
	}

	@Override
	public void onChannelUpdate(ChannelConfig channelConfig, ChannelConfigLogger channelConfigLogger) {
		String webhookUrl = null;
		try {
			webhookUrl = pmClientConfig.getWebhookUrl(channelConfig, null, null);
			MapModel webhook = MapModel.createInstance().put("override_callback_uri", webhookUrl).put("verify_token",
					channelConfig.getWacfb().getVerifyToken());

			restService.ajax(WA360Constants.META_WA_CLOUD_URL).path(channelConfig.getWacfb().getWabaId())
					.path("/subscribed_apps").authBearer(channelConfig.getWacfb().getAccessToken())
					.postJson(webhook.toMap()).asMapModel();
					
		}
				
		 catch (Exception e) {
			logManager.error("While Setting " + webhookUrl, e);
		}
	
	}

	public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		ChatContactQuery contactQuery = messageContext.contact();
		ChatContactDoc chatContactDoc = contactQuery.getDoc();
		if (chatContactDoc.phone() == null) {
			contactQuery.setPhone(inboxMessage.contact().phone());
		}
		if (chatContactDoc.getPhoneVerified() == null) {
			contactQuery.setPhoneVerified(true);
		}
		String user_input_type = this.context().session().getEntry("session_init_user_input_type").asString();
		if (ArgUtil.is(user_input_type)) {
			if (user_input_type.equals("name")) {
				contactQuery.setInfoName(inboxMessage.getMessage());
			}
			if (user_input_type.equals("email")) {
				String email = inboxMessage.getMessage();
				if (isValidEmail(email)) {
					contactQuery.setInfoEmail(email);
				} else {
					return (OutboxMessage) inboxMessage.replyMessage("Please enter a valid email address.");
				}
			}
			if (user_input_type.equals("phone")) {
				contactQuery.setInfoPhone(inboxMessage.getMessage());
			}

		}
		ChannelConfig channel = getChannelConfig(inboxMessage);

		if (ArgUtil.is(channel.getWacfb())) {
			if (channel.getWacfb().isPromptName()) {
				if (ArgUtil.isEmpty(chatContactDoc.info().getName())) {
					this.context().session().put("session_init_user_input_type", "name");
					return (OutboxMessage) inboxMessage.replyMessage("Please enter your name");
				}

			}

			if (channel.getWacfb().isPromptEmail()) {
				if (ArgUtil.isEmpty(chatContactDoc.info().getEmail())) {
					this.context().session().put("session_init_user_input_type", "email");
					return (OutboxMessage) inboxMessage.replyMessage("Please enter your email");
				}

			}

			if (channel.getWacfb().isPromptPhone()) {
				if (ArgUtil.isEmpty(chatContactDoc.info().getPhone())) {
					this.context().session().put("session_init_user_input_type", "phone");
					return (OutboxMessage) inboxMessage.replyMessage("Please enter your phone");
				}

			}
		}

		return null;
	}
	private boolean isValidEmail(String email) {
		String emailRegex = "^[A-Za-z0-9+_]+@[A-Za-z0-9-]+.[A-Za-z]{3,6}";
	    Pattern pattern = Pattern.compile(emailRegex);
	    return pattern.matcher(email).matches();
	}

	@Override
	protected CustomerProfileDoc findProfile(ChatContactDoc chatContactDoc) {
		return contactStore.findProfileByPhone(chatContactDoc.phone());
	}

	public InboxMessage toInboxMessage(ChannelConfig channelConfig, MapModel map) {

		// Create Default Message from Channel
		InboxMessage inboxMessage = this.createInboxMessage(channelConfig);

		// Set Contact info
		String contactNumber = map.entry(InBoundWrapperPaths.CONTACT_NUMBER).asString();
		String contactName = map.entry(InBoundWrapperPaths.CONTACT_NAME).asString();

		inboxMessage.contact().setCsid(contactNumber);
		inboxMessage.contact().setName(contactName);
		inboxMessage.contact().phone(contactNumber);

		// Set Additional info
		inboxMessage.setFrom(contactNumber);
		inboxMessage.setFromName(contactName);
		inboxMessage.to().add(channelConfig.getLane());

		// Extract Message Details
		inboxMessage.setMessageIdExt(map.entry(InBoundWrapperPaths.MESSAGE_ID).asString());

		String messageType = map.entry(InBoundWrapperPaths.MESSAGE_TYPE).asString();

		if ("text".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.TEXT);
			inboxMessage.setMessage(map.entry(InBoundWrapperPaths.MESSAGE_TEXT).asString());
		} else if ("interactive".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.TEXT);
			String interactiveType = map.entry(InBoundWrapperPaths.INTERACTIVE_TYPE).asString();
			String replyId = null;
			if ("button_reply".equals(interactiveType)) {
				replyId = map.entry(InBoundWrapperPaths.INTERACTIVE_BUTTON_ID).asString();
				inboxMessage.form().put("reply_id", replyId);
				inboxMessage.form().put("reply_title",
						map.entry(InBoundWrapperPaths.INTERACTIVE_BUTTON_REPLY).asString());
			} else if ("list_reply".equals(interactiveType)) {
				replyId = map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_ID).asString();
				inboxMessage.form().put("reply_id", replyId);
				inboxMessage.form().put("reply_title",
						map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_REPLY).asString());
				inboxMessage.form().put("reply_desc", map.entry(InBoundWrapperPaths.INTERACTIVE_LIST_DESC).asString());
			} else if ("nfm_reply".equals(interactiveType)) {
				String responseJsonString = map.entry(InBoundWrapperPaths.INTERACTIVE_NFM_REPLY_RESPONSE_JSON)
						.asString();
				Map<String, Object> replyJsonMap = JsonUtil.fromJsonToMap(responseJsonString);
				String flow_token = replyJsonMap.get("flow_token").toString();
				String id = String.format("%s/%s", channelConfig.getWacfb().getWabaId(), flow_token);
				WABAFlows flow = commonMongoTemplate.findById(id, WABAFlows.class);
				if (flow != null) {
					inboxMessage.form().put("field_meta", flow.getFieldMeta());
				}
				inboxMessage.form().put("reply_json", replyJsonMap);
				inboxMessage.form().put("reply_title",
						map.entry(InBoundWrapperPaths.INTERACTIVE_NFM_REPLY_BODY).asString());

				List<Map<String, Object>> formattedObjects = new ArrayList<>();
				List<Map<String, Object>> fieldMetaList = (List<Map<String, Object>>) inboxMessage.form()
						.get("field_meta");
				if (fieldMetaList != null) {
					for (Map<String, Object> fieldMeta : fieldMetaList) {
						String label = (String) fieldMeta.get("label");
						String key = (String) fieldMeta.get("key");
						String type = (String) fieldMeta.get("type");
						String text = replyJsonMap.containsKey(key) ? replyJsonMap.get(key).toString() : "N/A";
						if (!"N/A".equals(text) && fieldMeta.containsKey("data-source")) {
							String dataSource = (String) fieldMeta.get("data-source");
							Pattern pattern = Pattern.compile("\\{id=(.*?), title=(.*?)\\}");
							Matcher matcher = pattern.matcher(dataSource);
							while (matcher.find()) {
								String idFromDataSource = matcher.group(1);
								String title = matcher.group(2);

								if (idFromDataSource.equals(text)) {
									text = title;
									break;
								}
							}
						}
						Map<String, Object> formattedObject = new HashMap<>();
						formattedObject.put("key", key);
						formattedObject.put("label", label);
						formattedObject.put("type", type);
						formattedObject.put("text", text);
						formattedObjects.add(formattedObject);
					}
				}

				inboxMessage.form().put("reply_json_Map", formattedObjects.toString());
			}
			inboxMessage.setMessage(ArgUtil.parseAsString(inboxMessage.form().get("reply_title"), Constants.BLANK));
		} else if ("button".equals(messageType)) {
			inboxMessage.form().put("reply_title", map.entry(InBoundWrapperPaths.SIMPLE_BUTTON_REPLY).asString());
			String reply_payload = map.entry(InBoundWrapperPaths.SIMPLE_BUTTON_PAYLOAD).asString();
			inboxMessage.form().put("reply_payload", reply_payload);
			if (ArgUtil.is(reply_payload) && reply_payload.startsWith("reply_id:")) {
				String reply_id = reply_payload.replaceFirst("reply_id:", "");
				inboxMessage.form().put("reply_id", reply_id);
			}
			inboxMessage.setMessage(ArgUtil.parseAsString(inboxMessage.form().get("reply_title"), Constants.BLANK));
		} else if ("image".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.IMAGE);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.IMAGE, FileType.IMAGE);
		} else if ("document".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.DOCUMENT);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.DOCUMENT, FileType.DOCUMENT);
		} else if ("audio".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.AUDIO);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.AUDIO, FileType.AUDIO);
		} else if ("voice".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.VOICE);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.VOICE, FileType.AUDIO);
		} else if ("video".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.VIDEO);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.VIDEO, FileType.VIDEO);
		} else if ("sticker".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.STICKER);
			formatMedia(inboxMessage, map, channelConfig, InBoundWrapperPaths.STICKER, FileType.IMAGE);
		} else if ("contacts".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.CONTACTS);
			extractContacts(map, inboxMessage);
		} else if ("location".equals(messageType)) {
			inboxMessage.setFormatType(MESSAGE_FORMAT_TYPE.LOCATION);
			PBLocation pbLocation = new PBLocation();
			pbLocation.setName(map.pathEntry("messages/[0]/location/name").asString());
			pbLocation.setAddress(map.pathEntry("messages/[0]/location/address").asString());
			pbLocation.setLatitude(map.pathEntry("messages/[0]/location/latitude").asString());
			pbLocation.setLongitude(map.pathEntry("messages/[0]/location/longitude").asString());
			pbLocation.setUrl(map.pathEntry("messages/[0]/location/url").asString());
			inboxMessage.vccards().add(new PBVCard().locations(pbLocation));
		}

		inboxMessage.setFormatSubType(messageType);

		String replyIdExt = map.entry(InBoundWrapperPaths.CONTEXT_ID).asString();
		if (ArgUtil.is(replyIdExt)) {
			inboxMessage.setReplyIdExt(replyIdExt);
		}

		inboxMessage.setOriginalMessage(map.map());

		return inboxMessage;
	}

	private void extractContacts(MapModel map, InboxMessage inboxMessage) {
		List<Map<String, Object>> cards = map.entry(InBoundWrapperPaths.VCARDS).asListOfMap();
		for (Map<String, Object> card : cards) {
			MapModel cardMap = MapModel.from(card);

			PBVCard pbContact = new PBVCard();
			PBName pbName = new PBName();
			pbName.setFirstName(cardMap.pathEntry("/name/first_name").asString());
			pbName.setLastName(cardMap.pathEntry("/name/last_name").asString());
			pbName.setFormattedName(cardMap.pathEntry("/name/formatted_name").asString());
			pbContact.setName(pbName);

			List<Map<String, String>> phones = cardMap.keyEntry("phones").asListOfMapT();
			for (Map<String, String> phone : phones) {
				PBPhone pbPhone = new PBPhone();
				pbPhone.setType(phone.get("type"));
				pbPhone.setWhatsAppId(phone.get("wa_id"));
				pbPhone.setPhone(phone.get("phone"));
				pbContact.phones().add(pbPhone);
			}

			List<Map<String, String>> addresses = cardMap.keyEntry("addresses").asListOfMapT();
			for (Map<String, String> address : addresses) {
				PBAddress pbAddress = new PBAddress();
				pbAddress.setType(address.get("type"));
				pbAddress.setCity(address.get("city"));
				pbAddress.setCountry(address.get("country"));
				pbAddress.setCountryCode(address.get("country_code"));
				pbAddress.setState(address.get("state"));
				pbAddress.setStreet(address.get("street"));
				pbAddress.setZip(address.get("zip"));
				pbContact.addresses().add(pbAddress);
			}

			List<Map<String, String>> emails = cardMap.keyEntry("emails").asListOfMapT();
			for (Map<String, String> email : emails) {
				PBEmail pbEmail = new PBEmail();
				pbEmail.setType(email.get("type"));
				pbEmail.setEmail(email.get("email"));
				pbContact.emails().add(pbEmail);
			}

			List<Map<String, String>> ims = cardMap.keyEntry("ims").asListOfMapT();
			for (Map<String, String> im : ims) {
				PBSocial pbSocial = new PBSocial();
				pbSocial.setService(im.get("service"));
				pbSocial.setUserid(im.get("user_id"));
				pbContact.ims().add(pbSocial);
			}

			List<Map<String, String>> urls = cardMap.keyEntry("urls").asListOfMapT();
			for (Map<String, String> url : urls) {
				PBWebsite pnWebsite = new PBWebsite();
				pnWebsite.setUrl(url.get("url"));
				pnWebsite.setType(url.get("type"));
				pbContact.urls().add(pnWebsite);
			}

			MapPathEntry workCompany = cardMap.pathEntry("/org/company");
			MapPathEntry workDepartment = cardMap.pathEntry("/org/department");
			MapPathEntry workTitle = cardMap.pathEntry("/org/title");

			if (workCompany.exists() || workDepartment.exists() || workTitle.exists()) {
				PBWork pbWork = new PBWork();
				pbWork.setCompany(workCompany.asString());
				pbWork.setDepartment(workDepartment.asString());
				pbWork.setTitle(workTitle.asString());
				pbContact.work().add(pbWork);
			}

			MapPathEntry birthday = cardMap.keyEntry("birthday");
			if (birthday.exists()) {
				PBDate pbDate = new PBDate();
				pbDate.setType("birthday");
				pbDate.setDate(birthday.asString());
				pbContact.dates().add(pbDate);
			}

			inboxMessage.vccards().add(pbContact);
		}
	}

	private void formatMedia(InboxMessage inboxMessage, MapModel map, ChannelConfig channelConfig, JsonPath path,
			FileType fileType) {
		try {

			WacfbInboundMedia media = map.entry(path).as(WacfbInboundMedia.class);

			String mediaUrl = wacfbClient.getMediaUrl(channelConfig, WA360Constants.META_WA_CLOUD_URL(media.getId()));
			// System.out.println("mediaUrl " + mediaUrl);

			CommonFileStream srcFile = new CommonFileStream().url(mediaUrl).fileType(fileType)
					.format(FileFormat.from(media.getMimeType())).authBearer(channelConfig.getWacfb().getAccessToken())
					.name(ArgUtil.nonEmpty(media.getFilename(), media.getCaption()));

			CommonFile dstFile = pmFileStoreClient.uploadSessionFileAsync(srcFile,
					PostManUtil.createContactId(inboxMessage), inboxMessage.getMessageIdExt());
			inboxMessage.attachment(new Attachment().mediaURL(dstFile.getUrl()).mediaType(dstFile.getFileType())
					.mediaSrc(srcFile.getUrl()).mediaCaption(media.getCaption()).mediaName(media.getFilename())
					.mediaMimeType(media.getMimeType()));
		} catch (IOException e) {
			logManager.error(inboxMessage, e);
		}
	}

	@Override
	public CommonFile reloadMedia(ChannelConfig channelConfig, MessageDoc msg, Attachment attachment)
			throws FileNotFoundException, IOException {
		String mediaUrl = wacfbClient.getMediaUrl(channelConfig, attachment.getMediaSrc());

		CommonFileStream srcFile = new CommonFileStream().url(mediaUrl)
				// .fileType(attachment.getMediaType())
				.format(FileFormat.from(attachment.getMediaMimeType()))
				.header(WA360Constants.D360_CLOUD_API_KEY, channelConfig.getWa360dc().getApiKey())
				.name(ArgUtil.nonEmpty(attachment.getMediaName(), attachment.getMediaCaption(),
						attachment.getMediaMimeType(), "File"));

		File fileb = Urly.parse(attachment.getMediaURL()).toFile();

		CommonFile dstFile = new CommonFile().url(attachment.getMediaURL()).path(fileb.getParent())
				.fileType(ArgUtil.parseAsEnumT(attachment.getMediaType(), FileType.class));
		return pmFileStoreClient.commitSessionFileSync(srcFile, dstFile);
	}

	@Override
	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {

			boolean isValidContact = true;
			if (outboxMessage.messageMetaWrapper().composeTypeIs(MESSAGE_COMPOSE_TYPE.SEND_CODE)) {
				isValidContact = optin(channelConfig, chatContactDoc);
			}
			if (isValidContact) {
				wacfbClient.send(channelConfig, outboxMessage);
				outboxMessage.updateStatus(OutboxMessage.Status.SENT);

			} else {
				outboxMessage.logs().add(String.format("Invalid Contact for %s", chatContactDoc));
				outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			}

		} catch (AmxApiException e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(((AmxApiException) e).getErrorKey());
			outboxMessage.logs().add(e.getMessage());
		}
	}

	private MessageReport toMessageReport(ChannelConfig channelConfig, MapModel requestMap) {
		MessageReport report = this.createMessageReport(channelConfig);
		String csid = requestMap.path(WA360Constants.InBoundWrapperPaths.STATUS_RECIPIENT).asString();
		if (!ArgUtil.is(csid)) {
			csid = requestMap.getString("recipient_id");
		}
		report.contact().setCsid(csid);
		report.setChangeStamp(requestMap.getLong("timestamp", 0L) * 1000);
		report.setMessageIdExt(requestMap.getString("id"));

		String status = requestMap.getString("status");

		if ("sent".equals(status)) {
			report.setStatus(Status.SENTX);
		} else if ("delivered".equals(status)) {
			report.setStatus(Status.DLVRD);
		} else if ("read".equals(status)) {
			report.setStatus(Status.READ);
		} else if ("deleted".equals(status)) {
			report.setStatus(Status.DELTD);
		} else if ("failed".equals(status)) {
			report.setStatus(Status.FAILD);

			String errorCode = requestMap.pathEntry("errors/[0]/code").asString();
			if (ArgUtil.areEqual(errorCode, "470")) {
				report.setStatus(Status.CCWIN);
			} else if (ArgUtil.areEqual(errorCode, "471")) {
				report.setStatus(Status.LIMIT);
			}
			report.setReason("Code:" + errorCode);

			List<MessageReportError> errors = requestMap.keyEntry("errors")
					.asList(MessageReport.MessageReportError.class);
			if (ArgUtil.is(errors)) {
				report.setErrors(errors);
			}
		}

		return report;
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		LOGGER.info("IN message {DR}" + requestMap);

		List<Map<String, Object>> changes = requestMap.path(FacebookConstants.WABAPaths.CHANGES).asListOfMap();

		changes.forEach(change -> {
			MapModel changeMap = MapModel.from(change).keyEntry("value").asMapModel();

			if (changeMap.containsKey("messages")) {
				messageBoxEvent.addInboxMessage(toInboxMessage(channelConfig, changeMap));
			} else if (changeMap.containsKey("statuses")) {
				List<Map<String, Object>> statusMaps = changeMap.keyEntry("statuses").asListOfMap();
				for (Map<String, Object> statusMap : statusMaps) {
					MapModel statusModel = MapModel.from(statusMap);
					MessageReport reprt = toMessageReport(channelConfig, statusModel);
					messageBoxEvent.addMessageReport(reprt);
					if (Status.SENTX.equals(reprt.getStatus())) {
						Map<String, Object> conversation = statusModel.keyEntry("conversation").asMap();
						if (ArgUtil.is(conversation)) {
							String id = String.format("%s_%s", channelConfig.getChannelId(), conversation.get("id"));
							WABAConversationQuery query = new WABAConversationQuery(id);
							query.setContact(reprt.contact());
							query.setConversation(conversation);
							query.setPricing(statusModel.keyEntry("pricing").asMap());
							query.set("meta.to_country", getCountryCode(reprt.contact().getCsid()));
							commonMongoTemplate.upsert(query);
							reprt.setTpMeta(
									MapModel.createInstance().put("ccwExpiry", conversation.get("expiration_timestamp"))
											.put("wabaConvesationId", id).toMap());

						}
					}
				}
			}
		});
		return messageBoxEvent;
	}

	public String getCountryCode(String phone) {
		String defaultRegion = environment.keyEntry("postman.phonebook.region").asString("IN");
		PhoneNumber phoneNumber;
		try {
			phoneNumber = PHONE_NUMBER_UTIL.parse("+" + phone, defaultRegion);
			return PHONE_NUMBER_UTIL.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
		} catch (NumberParseException e) {
			return defaultRegion;
		}
	}

	// @Override
	/*
	 * public boolean optin(ChannelConfig channelConfig, ChatContactDoc
	 * chatContactDoc) {
	 * 
	 * if (ArgUtil.isEmptyValue(chatContactDoc.getLastOptInStamp())) { String
	 * defaultRegion =
	 * environment.keyEntry("postman.phonebook.region").asString("IN"); String phone
	 * = chatContactDoc.getPhone(); try { phone = phone.replace(" ",
	 * "").replaceAll("^[\\+0\\s]+(?!$)", "").trim(); PhoneNumber phoneNumber =
	 * PHONE_NUMBER_UTIL.parse("+" + phone, defaultRegion); phone =
	 * String.format("+%s%s", phoneNumber.getCountryCode(),
	 * phoneNumber.getNationalNumber()); } catch (NumberParseException e) { phone =
	 * String.format("+%s", phone); }
	 * 
	 * MapModel resp = waClient.fetchContact(phone, channelConfig); String waId
	 * =resp.getString("wa_id"); String input = null;// resp.getString("input");
	 * String status = "valid";// resp.getString("status");
	 * 
	 * if ("valid".equals(status)) { ChatContactQuery chatContactQuery = new
	 * ChatContactQuery(chatContactDoc); chatContactQuery.updateLastOptInStamp();
	 * commonMongoTemplate.updateFirst(chatContactQuery); return true; } }
	 * 
	 * //return ArgUtil.isEmptyValue(chatContactDoc.getLastOptInStamp()); return
	 * true; }
	 */

}
