package com.boot.jx.agent.api;

import java.util.List;

import com.boot.jx.postman.PMConstants.CHAT_STATUS;
import com.boot.jx.postman.doc.QuickTag;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class ControllerRequestDTOs {
	public static class ChatTagUpdateRequest {
		public String sessionId;
		public CHAT_STATUS status;
		public List<QuickTag> tags;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SessionSearchRequest {
		public String text;
		public List<CHAT_STATUS> status;
		public List<QuickTag> tags;
		public long fromStamp;
		public long toStamp;
		public long limit;
	}

}
