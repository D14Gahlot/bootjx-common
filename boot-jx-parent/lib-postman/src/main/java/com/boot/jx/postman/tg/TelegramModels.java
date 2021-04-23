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

}
