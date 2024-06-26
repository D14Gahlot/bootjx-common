package com.boot.jx.connectors;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.dict.ContactType;
import com.boot.jx.exception.AmxApiException;
import com.boot.jx.exception.ApiHttpExceptions.ApiHttpException;
import com.boot.jx.http.CommonHttpRequest;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.postman.PMConstants.CHANNEL_TYPE;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.doc.config.ChannelConfigTempDoc;
import com.boot.jx.postman.model.AuthStateManager.AuthState;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.MessageBoxEvent;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.jx.postman.plugin.ChannelPluginProvider.ConnectorMapping;
import com.boot.jx.postman.plugin.OutlookPlugin;
import com.boot.jx.postman.plugin.OutlookPlugin.OutlookConfigDetails;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.Urly;

@Component
@ConnectorMapping(contactType = ContactType.EMAIL, channel = CHANNEL_TYPE.OUTLOOK)
public class OutlookConnector extends AbstractConnector<OutlookConfigDetails, OutlookPlugin> {

	private static Logger LOGGER = LoggerService.getLogger(OutlookConnector.class);
	private static final String AUTHORITY = "https://login.microsoftonline.com";
	private static final String AUTHORIZE_URL = AUTHORITY + "/common/oauth2/v2.0/authorize";
	private static final String AUTHORIZE_TOKEN = AUTHORITY + "/common/oauth2/v2.0/token";

	@Autowired
	private RestService restService;

	@Autowired
	private CommonHttpRequest commonHttpRequest;

	@Autowired
	private AppConfig appConfig;

	@Override
	public String createAuthUrl(ChannelConfig setup, ChannelConfigTempDoc channelConfigTemp, AuthState state)
			throws URISyntaxException, MalformedURLException {
		String redirectUri = String.format("%s%s/ext/setup/channel/callback/outlook", commonHttpRequest.getServerHost(),
				appConfig.getAppPrefix(), environment.keyEntry("mry.prop.service.server").asString());
		/// &state=fooobar&scope=r_liteprofile%20r_emailaddress%20w_member_social
		state.setRedirectUrl(redirectUri);

		return Urly.parse(AUTHORIZE_URL).queryParam("response_type", "code") //
				.queryParam("client_id", setup.getOutlook().getMasterClientId()) //
				.queryParam("redirect_uri", redirectUri).queryParam("state", state.toString()) // State
				.queryParam("nonce", state.getNonce()) //
				.queryParam("scope", "offline_access user.read mail.read mail.send") //
				.queryParam("response_mode", "form_post") //
				.getURL();

	}

	public List<ChannelConfig> onRegister(ChannelConfig setup, ChannelConfigTempDoc channelConfigTemp,
			AuthState state) {
		List<ChannelConfig> channels = new ArrayList<ChannelConfig>();
		try {

			MapModel resp = MapModel.from(channelConfigTemp.getResp());
			MapModel tokenResponse = restService.ajax(AUTHORIZE_TOKEN)//
					.field("grant_type", "authorization_code")//
					.field("code", resp.pathEntry("authResponse.code").asString())//
					.field("client_id", setup.getOutlook().getMasterClientId())//
					.field("client_secret", setup.getOutlook().getMasterClientSecret())//
					.field("redirect_uri", state.getRedirectUrl()).submit().asMapModel();

			channelConfigTemp.log("oauth2/v2.0/token", tokenResponse.toMap());

			String accessToken = tokenResponse.keyEntry("access_token").asString();
			String refreshToken = tokenResponse.keyEntry("refresh_token").asString();

			MapModel profileResponse = restService.ajax("https://graph.microsoft.com/v1.0/me")
					.header("Authorization", "Bearer " + accessToken).get().asMapModel();

			channelConfigTemp.log("/me", profileResponse.toMap());

			ChannelConfig channel = new ChannelConfig();
			channel.setOutlook(new OutlookConfigDetails());
			channel.getOutlook().setAccessToken(accessToken);
			channel.getOutlook().setRefreshToken(refreshToken);
			channel.getOutlook().setEmail(profileResponse.keyEntry("mail").orKeyEntry("userPrincipalName").asString());
			channel.getOutlook().setMasterClientId(setup.getOutlook().getMasterClientId());
			channel.setName(profileResponse.keyEntry("displayName").asString());
			channels.add(channel);
		} catch (ApiHttpException e) {
			channelConfigTemp.log("exception", MapModel.from(e.getResponse().getBody()).toMap());
		}
		commonMongoTemplate.save(channelConfigTemp);
		return channels;
	}

	@Override
	public void onSend(ChannelConfig channelConfig, ChatContactDoc chatContactDoc, OutboxMessage outboxMessage) {
		try {
			template(channelConfig, chatContactDoc, outboxMessage); // TODO:- This is common for all connector, make it
			// generic
			outboxMessage.updateStatus(OutboxMessage.Status.SENT);
		} catch (AmxApiException e) {
			outboxMessage.updateStatus(OutboxMessage.Status.SENT_ERR);
			outboxMessage.logs().add(((AmxApiException) e).getErrorKey());
		}
	}

	@Override
	public CustomerProfileDoc findProfile(ChatContactDoc chatContactDoc) {
		return contactStore.findProfileByPhone(chatContactDoc.getPhone());
	}

	@Override
	public OutboxMessage initSession(ChatSessionDoc session, InboxMessage inboxMessage) {
		return null;
	}

	public InboxMessage toInboxMessage(ChannelConfig channelConfig, Map<String, Object> dataMap) {
		InboxMessage inboxMessage = this.createInboxMessage(channelConfig);
		return inboxMessage;
	}

	@Override
	public MessageBoxEvent inboundMessageBoxEvent(ChannelConfig channelConfig, MapModel requestMap,
			MessageBoxEvent messageBoxEvent) {
		return messageBoxEvent.addInboxMessage(toInboxMessage(channelConfig, requestMap.toMap()));
	}

}
