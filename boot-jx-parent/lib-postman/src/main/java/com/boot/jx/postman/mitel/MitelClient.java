package com.boot.jx.postman.mitel;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.ClientApp;
import com.boot.jx.postman.PMEnvironment.PMDomainConfig;
import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.TimeUtils;

@Component
public class MitelClient {

    public static final long TOKEN_EXPIRY = TimeUtils.toMillis("10min");

    @Autowired
    PMDomainConfig pmDomainConfig;

    @Autowired
    private RestService restService;

    public String getToken(ClientApp defaultClient) {

	String accessToken = ArgUtil.parseAsString(defaultClient.secret().get("accessToken"));
	Long accessTokenStamp = ArgUtil.parseAsLong(defaultClient.secret().get("accessTokenStamp"), 0L);

	if (ArgUtil.is(accessTokenStamp) && TimeUtils.isExpired(accessTokenStamp, TOKEN_EXPIRY)) {
	    return accessToken;
	}

	defaultClient.secret().put("accessTokenStamp", System.currentTimeMillis());

	String endPoint = ArgUtil.parseAsString(defaultClient.props().get("end_point"));
	String grantType = ArgUtil.parseAsString(defaultClient.props().get("grant_type"), "client_credentials");

	Ajax ajax = restService.ajax(endPoint).path("/AuthorizationServer/Token").field("grant_type", grantType);

	if (ArgUtil.areEqual(grantType, "password")) {
	    String username = ArgUtil.parseAsString(defaultClient.props().get("username"));
	    String password = ArgUtil.parseAsString(defaultClient.secret().get("password"));
	    ajax.field("username", username).field("password", password);
	} else {
	    String clientId = ArgUtil.parseAsString(defaultClient.props().get("client_id"));
	    String clientSecret = ArgUtil.parseAsString(defaultClient.secret().get("client_secret"));
	    ajax.field("client_id", clientId).field("client_secret", clientSecret);
	}

	accessToken = ajax.asMapModel().keyEntry("access_token").asString();

	defaultClient.secret().put("accessToken", accessToken);
	defaultClient.secret().put("accessTokenStamp", System.currentTimeMillis());
	return accessToken;

    }

    public void send(ClientApp defaultClient, String sessionId, String contactId) {
	String accessToken = getToken(defaultClient);
	String endPoint = ArgUtil.parseAsString(defaultClient.props().get("end_point"));
	String queue = ArgUtil.parseAsString(defaultClient.props().get("queue"));
	String from = ArgUtil.parseAsString(defaultClient.props().get("from"), contactId);
	String to = ArgUtil.parseAsString(defaultClient.props().get("to"));

	String url = String.format("%s/agent/plug/chat/%s/%s/%s/hide/CHATBOX", pmDomainConfig.getDomainUrl(), contactId,
		sessionId, contactId);
	restService.ajax(endPoint).path("/MiccSdk/api/v1/openmedia").header("Authorization", "Bearer " + accessToken)
		.post(MapModel.createInstance().put("targetUri", url).put("targetUriEmbedded", true)
			.put("previewUrl", url).put("historyUrl", url).put("queue", queue).put("from", from)
			.put("to", to).put("subject", contactId).toMap())
		.asNone();
    }

}
