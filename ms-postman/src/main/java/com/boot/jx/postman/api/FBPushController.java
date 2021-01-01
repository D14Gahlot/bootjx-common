package com.boot.jx.postman.api;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.IPushNotifyService;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.postman.service.FBPushServiceImpl;

/**
 * The Class FBPushController.
 */
@RestController
public class FBPushController implements IPushNotifyService {

	private static final Logger LOGGER = LoggerFactory.getLogger(FBPushController.class);

	@Autowired
	FBPushServiceImpl fBPushService;

	/**
	 * Fb push.
	 *
	 * @param msg
	 *            the msg
	 * @return the push message
	 * @throws PostManException
	 *             the post man exception
	 * @throws InterruptedException
	 *             the interrupted exception
	 * @throws ExecutionException
	 *             the execution exception
	 */
	@Override
	@RequestMapping(value = PostManUrls.NOTIFY_PUSH, method = RequestMethod.POST)
	public ApiResponse<PushMessage, Object> sendDirect(@RequestBody PushMessage msg) throws PostManException {
		return fBPushService.sendDirect(msg);
	}

	@Override
	@RequestMapping(value = PostManUrls.NOTIFY_PUSH_BULK, method = RequestMethod.POST)
	public ApiResponse<PushMessage, Object> send(@RequestBody List<PushMessage> msgs) throws PostManException {
		for (PushMessage pushMessage : msgs) {
			fBPushService.sendDirect(pushMessage);
		}
		return fBPushService.send(msgs);
	}

	/**
	 * Fb push.
	 *
	 * @param token
	 *            the token
	 * @param topic
	 *            the topic
	 * @return the string
	 * @throws PostManException
	 *             the post man exception
	 */
	@Override
	@RequestMapping(value = PostManUrls.NOTIFY_PUSH_SUBSCRIBE, method = RequestMethod.POST)
	public ApiResponse<String, Object> subscribe(@RequestParam String token, @PathVariable String topic)
			throws PostManException {
		return fBPushService.subscribe(token, topic);
	}
	
	
	/**
	 * Fb push.
	 *
	 * @param token
	 *            the token
	 * @param topic
	 *            the topic
	 * @return the string
	 * @throws PostManException
	 *             the post man exception
	 */
	@Override
	@RequestMapping(value = PostManUrls.SHORT_LINK, method = RequestMethod.POST)
	public String shortLink(@RequestParam String relativeUrl)
			throws PostManException {
		return fBPushService.shortLink(relativeUrl);
	}
	

}
