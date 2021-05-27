package com.boot.jx.postman.tg;

import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

public class TelegramModels {

	@JsonInclude(Include.NON_NULL)
	@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static class TGSendPhoto extends SendPhoto {

	}

	@JsonInclude(Include.NON_NULL)
	@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static class TGSendDocument extends SendDocument {

	}

	@JsonInclude(Include.NON_NULL)
	@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static class TGMessage extends org.telegram.telegrambots.meta.api.objects.Message {
		private static final long serialVersionUID = 1L;
	}

	@JsonInclude(Include.NON_NULL)
	@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static class TGFile extends org.telegram.telegrambots.meta.api.objects.File {
		private static final long serialVersionUID = 1L;
		String fileUrl;

		public String getFileUrl() {
			return fileUrl;
		}

		public void setFileUrl(String fileUrl) {
			this.fileUrl = fileUrl;
		}

		public TGFile updateFileUrl(String botToken) {
			this.fileUrl = this.getFileUrl(botToken);
			return this;
		}

	}

	@JsonInclude(Include.NON_NULL)
	@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
	public static class TGGetFile extends org.telegram.telegrambots.meta.api.methods.GetFile {

	}
}
