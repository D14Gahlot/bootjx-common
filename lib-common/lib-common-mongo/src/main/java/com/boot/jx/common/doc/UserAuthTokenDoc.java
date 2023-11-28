package com.boot.jx.common.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.common.dto.UserAuthToken;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Document(collection = "USER_LOGIN_TOKEN")
@TypeAlias("UserAuthToken")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserAuthTokenDoc extends UserAuthToken {
	private static final long serialVersionUID = 8544996516050512567L;

	@Id
	public String getTokenId() {
		return this.tokenId;
	}

}
