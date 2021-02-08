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
			@ApiMockParam(name = "consumer-key", value = "Consumer Key", defaultValue = "abcvernorx", paramType = MockParamType.HEADER),
			@ApiMockParam(name = "consumer-token", value = "Consumer Token", defaultValue = "kje34kipdghkjwww", paramType = MockParamType.HEADER),
			@ApiMockParam(name = "access-key", value = "Access Key", defaultValue = "agent1", paramType = MockParamType.HEADER),
			@ApiMockParam(name = "access-token", value = "Access Token", defaultValue = "s1agenttoken$$$", paramType = MockParamType.HEADER) })
	public @interface ApiClientParams {

	}

}
