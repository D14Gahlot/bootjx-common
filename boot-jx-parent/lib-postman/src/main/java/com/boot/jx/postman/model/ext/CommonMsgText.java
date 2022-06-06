package com.boot.jx.postman.model.ext;

import com.boot.jx.swagger.ApiMockModelProperty;

public interface CommonMsgText {

	@ApiMockModelProperty(example = "your-text-message-content", value = "Message Text",
			notes = "Contains the text of the message, which can contain URLs and formatting.")
	public String getBody();

	public class InBoundMsgText extends CommonMsg implements CommonMsgText {
		private String body;

		@Override
		public String getBody() {
			return this.body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}

	public class OutBoundMsgText implements CommonMsgText {
		private String body;

		@Override
		public String getBody() {
			return this.body;
		}

		public void setBody(String body) {
			this.body = body;
		}
	}

}