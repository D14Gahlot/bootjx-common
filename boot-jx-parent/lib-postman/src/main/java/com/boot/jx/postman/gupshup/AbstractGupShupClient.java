package com.boot.jx.postman.gupshup;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.postman.gupshup.GupShupConstants.SessionType;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageOptions.WAMessageOptions;
import com.boot.jx.rest.RestService;
import com.boot.jx.rest.RestService.Ajax;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil;
import com.boot.utils.JsonUtil;

public abstract class AbstractGupShupClient {

	@Autowired
	protected GupShupConfig gupShupConfig;

	@Autowired
	protected RestService restService;

	public abstract SessionType getSessionType();

	public boolean getIsHSM() {
		return false;
	}

	private Ajax ajax(GupShupReq req, boolean encrypt) {
		Ajax ajax = restService.ajax(gupShupConfig.getGupShupApiUrl()).path("/GatewayAPI/rest");

		if (getSessionType() == SessionType.NOTIFICATION) {
			ajax.field("userid", gupShupConfig.getGupShupNotifyId());
			req.password(gupShupConfig.getGupShupNotifyPass());
		} else {
			ajax.field("userid", gupShupConfig.getGupShupChatId());
			req.password(gupShupConfig.getGupShupChatPass());
		}
		if (encrypt) {
			ajax.field("encrdata",
					CryptoUtil.getEncoder().obzect(req.password(gupShupConfig.getGupShupChatPass())).encodeBase64()
							.toString());
		} else {
			Map<String, Object> reqMap = JsonUtil.toMap(req);
			for (Entry<String, Object> entrySet : reqMap.entrySet()) {
				ajax.field(entrySet.getKey(), entrySet.getValue());
			}
		}
		return ajax;
	}

	private GupShupResp post(GupShupReq req, boolean encrypt) {
		Ajax ajax = ajax(req, encrypt);
		return ajax.postForm().as(GupShupResp.class);
	}

	public GupShupResp uploadDocument(String phoneNumber, MultipartFile file) throws IOException {
		return ajax(new GupShupReq(GupShupConstants.Method.UploadMedia)
				.sendTo(phoneNumber),
				false).field("media_type", GupShupConstants.MessageType.DOCUMENT)
						.field("media_file", file)
						.postForm().as(GupShupResp.class);
	}

	protected GupShupResp post(GupShupReq req) {
		return post(req, false);
	}

	public GupShupResp optIn(String phoneNumber) {
		GupShupReq gupShupReq = new GupShupReq(GupShupConstants.Method.OPT_IN).phoneNumber(phoneNumber);
		gupShupReq.setChannel("WHATSAPP");
		return post(gupShupReq);
	}

	public GupShupResp optOut(String phoneNumber) {
		return post(
				new GupShupReq(GupShupConstants.Method.OPT_OUT).phoneNumber(phoneNumber));
	}

	public GupShupResp sendMessage(String phoneNumber, String message) {
		return post(
				new GupShupReq(GupShupConstants.Method.SendMessage)
						.sendTo(phoneNumber).messageType(GupShupConstants.MessageType.TEXT)
						.message(CryptoUtil.getEncoder().message(message).toString()));
	}

	public GupShupResp sendImageURL(String phoneNumber, String media_url, String caption) {
		return post(
				new GupShupReq(GupShupConstants.Method.SendMediaMessage)
						.sendTo(phoneNumber).messageType(GupShupConstants.MessageType.IMAGE).hsm(getIsHSM())
						.dataEncoding(GupShupConstants.DataEncoding.TEXT)
						.mediaURL(media_url)
						.caption(CryptoUtil.getEncoder().message(caption).encodeURL().toString()));
	}

	public GupShupResp sendDocumentURL(String phoneNumber, String media_url, String caption) {
		return post(
				new GupShupReq(GupShupConstants.Method.SendMediaMessage)
						.sendTo(phoneNumber).messageType(GupShupConstants.MessageType.DOCUMENT).hsm(getIsHSM())
						.mediaURL(media_url)
						.caption(CryptoUtil.getEncoder().message(caption).encodeURL().toString()));
	}

	public GupShupResp sendDocument(String phoneNumber, MultipartFile file, String caption) throws IOException {
		GupShupResp media = uploadDocument(phoneNumber, file);
		return post(
				new GupShupReq(GupShupConstants.Method.SendMediaMessage)
						.sendTo(phoneNumber).messageType(GupShupConstants.MessageType.DOCUMENT).hsm(getIsHSM())
						.dataEncoding(GupShupConstants.DataEncoding.TEXT)
						.mediaId(media.getResponse().getId())
						.caption(CryptoUtil.getEncoder().message(caption).encodeURL().toString()));
	}

	public GupShupResp sendMessage(Message<?> message) {
		String phoneNumber = CollectionUtil.getOne(message.getTo());
		if (ArgUtil.is(message.getFiles())
				&& ArgUtil.is(message.getFiles().get(0))
				&& ArgUtil.is(message.getFiles().get(0).getUrl())) {
			File file = message.getFiles().get(0);

			if (ArgUtil.is(file.getType()) && ArgUtil.isEqual(file.getType().getFormatType(), File.Format.IMAGE)) {
				return sendImageURL(phoneNumber, file.getUrl(), message.getMessage());
			}
			return sendDocumentURL(phoneNumber, file.getUrl(), message.getMessage());
		}

		GupShupReq gupShupReq = new GupShupReq(GupShupConstants.Method.SendMessage).sendTo(phoneNumber)
				.messageType(GupShupConstants.MessageType.TEXT)
				.message(CryptoUtil.getEncoder().message(message.getMessage()).toString());

		if (message instanceof WAMessageOptions) {
			WAMessageOptions waMessageOptions = (WAMessageOptions) message;
			if (waMessageOptions.isQRButtons()) {
				gupShupReq.isTemplate(true);
				gupShupReq.messageType(GupShupConstants.MessageType.HSM);
			}
		}
		return post(gupShupReq);
	}

}
