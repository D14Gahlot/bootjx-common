package com.boot.jx.postman.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.swagger.ApiMockModelProperty;

@Document(collection = "CHAT_SESSION")
public class ChatSession implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	private String sessionId;

	@ApiMockModelProperty(example = "wa919930104050", required = false)
	private String contactId;

	private String assignedTo;

}
