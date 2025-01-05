package com.boot.jx.xms.dto;

import java.io.Serializable;
import java.util.List;

import com.boot.jx.swagger.ApiMockModelProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public final class SessionRequestObjects {

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SessionQueueAssignment implements Serializable {
		private static final long serialVersionUID = 4064758284063588819L;
		@ApiMockModelProperty(example = "61f3810a02e14c0877fc1a32", required = true, value = "Session to be routed")
		public String sessionId;

		@ApiMockModelProperty(example = "external_bot", required = true,
				value = "Next queue where session should be routed")
		public String queue;

		@ApiMockModelProperty(example = "external_bot", required = false,
				value = "Team Code, applicable only if queue type is Agent")
		public String team;

		@ApiMockModelProperty(example = "external_bot", required = false,
				value = "Agent Code, applicable only if queue type is Agent")
		public String agent;

		@ApiMockModelProperty(example = "This small note briefing conversation so far", required = false,
				value = "Routing Note")
		public String note;

		@ApiMockModelProperty(example = "[\"dental\",\"ortho\"]", required = false,
				value = "Agent Skills, applicable only if queue type is Agent")
		public List<String> skills;

		public Object params;
	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SessionStatusClose implements Serializable {
		private static final long serialVersionUID = 6550916242164154457L;
		@ApiMockModelProperty(example = "61f3810a02e14c0877fc1a32", required = true, value = "Session to be closed")
		public String sessionId;

	}

	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ContactPrefsUpdate implements Serializable {
		private static final long serialVersionUID = 6550916242164154457L;
		@ApiMockModelProperty(example = "fb:12121212121", required = true, value = "Contact to update")
		public String contactId;

		@ApiMockModelProperty(example = "en_US", required = true, value = "Language to set")
		public String lang;

	}
}
