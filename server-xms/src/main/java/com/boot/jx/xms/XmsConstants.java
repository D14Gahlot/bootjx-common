package com.boot.jx.xms;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.boot.jx.swagger.ApiMockParam;
import com.boot.jx.swagger.ApiMockParams;
import com.boot.jx.swagger.MockParamBuilder.MockParamType;

public class XmsConstants {

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @ApiMockParams({
//	    @ApiMockParam(name = "x-consumer-key", value = "Consumer Key", defaultValue = "abcvernorx",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-consumer-token", value = "Consumer Token", defaultValue = "kje34kipdghkjwww",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-access-key", value = "Access Key", defaultValue = "agent1",
//		    paramType = MockParamType.HEADER),
//	    @ApiMockParam(name = "x-access-token", value = "Access Token", defaultValue = "s1agenttoken$$$",
//		    paramType = MockParamType.HEADER),
	    @ApiMockParam(name = "x-api-key", value = "API Key", defaultValue = "x-api-key$$$",
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
