package com.boot.jx.xms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.boot.jx.http.ApiRequest;
import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;

public class XmsConstants {

    public static final String INBOUND_WEBHOOKS_DESCRIPTION = "Webhooks can be used for:\n"
	    + "* Inbound Message Notifications: Use it to get a notification you when you have received a message.\n"
	    + "* Message Status Notifications: Monitor the status of sent messages.\n"
	    + "Webhook must be implemented by client and should be available on public internet";

    public static final String SESSION_MNGMNT_DESCRIPTION = "Session API can be used:\n"
	    + "* to fetch all session messages.\n"
	    + "A session usually starts with first messages and stays active for 24 hours by default.";

    public static final String X_API_KEY = "x-api-key";

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @ApiRequest(authenticateTenant = true)
    @ApiMockParams({
//	    @ApiMockParam(name = "x-consumer-key", value = "Consumer Key", defaultValue = "abcvernorx",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-consumer-token", value = "Consumer Token", defaultValue = "kje34kipdghkjwww",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-access-key", value = "Access Key", defaultValue = "agent1",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-access-token", value = "Access Token", defaultValue = "s1agenttoken$$$",
//		    paramType = MockParamType.HEADER),
	    @ApiMockParam(name = X_API_KEY, value = "API Key", defaultValue = "thisduMmykEy4maTD0NTUsEit",
		    paramType = MockParamType.HEADER) })
    public @interface ApiClientParams {

    }

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @ApiMockParams({
//	    @ApiMockParam(name = "x-verify-key", value = "Callback Verification Key", defaultValue = "abcvernorx",
//		    paramType = MockParamType.HEADER),
	    @ApiMockParam(name = "x-verify-token", value = "Callback Verification Token",
		    defaultValue = "s1agenttoken$$$", paramType = MockParamType.HEADER) })
    public @interface ApiCallbacktParams {

    }

}
