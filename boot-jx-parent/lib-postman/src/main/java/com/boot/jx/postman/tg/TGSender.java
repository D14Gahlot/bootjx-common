package com.boot.jx.postman.tg;

import java.io.Serializable;
import java.util.List;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.groupadministration.SetChatPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendAnimation;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.send.SendVideoNote;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.methods.stickers.AddStickerToSet;
import org.telegram.telegrambots.meta.api.methods.stickers.CreateNewStickerSet;
import org.telegram.telegrambots.meta.api.methods.stickers.UploadStickerFile;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageMedia;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.bots.AbsSender;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.updateshandlers.SentCallback;

public class TGSender extends AbsSender {

	@Override
	public Message execute(SendDocument sendDocument) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendPhoto sendPhoto) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendVideo sendVideo) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendVideoNote sendVideoNote) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendSticker sendSticker) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendAudio sendAudio) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendVoice sendVoice) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Message> execute(SendMediaGroup sendMediaGroup) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean execute(SetChatPhoto setChatPhoto) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean execute(AddStickerToSet addStickerToSet) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Boolean execute(CreateNewStickerSet createNewStickerSet) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File execute(UploadStickerFile uploadStickerFile) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Serializable execute(EditMessageMedia editMessageMedia) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Message execute(SendAnimation sendAnimation) throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected <T extends Serializable, Method extends BotApiMethod<T>, Callback extends SentCallback<T>> void sendApiMethodAsync(
			Method method, Callback callback) {
		// TODO Auto-generated method stub

	}

	@Override
	protected <T extends Serializable, Method extends BotApiMethod<T>> T sendApiMethod(Method method)
			throws TelegramApiException {
		// TODO Auto-generated method stub
		return null;
	}

}
