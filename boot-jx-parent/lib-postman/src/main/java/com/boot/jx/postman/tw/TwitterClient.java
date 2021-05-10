package com.boot.jx.postman.tw;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.FileType;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostmanPackages.MessageClient;
import com.boot.jx.postman.client.ExtUtilService;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.DirectMessageLocalImpl;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.UploadedMedia;
import twitter4j.conf.ConfigurationBuilder;

@Component
@PropertySource("classpath:application-twitter.properties")
@EnableEncryptableProperties
public class TwitterClient implements MessageClient {

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterClient.class);
	private static Map<String, TwitterClientContext> CLIENTS = Collections
			.synchronizedMap(new HashMap<String, TwitterClientContext>());

	@Value("${postman.twitter.default.lane}")
	private String defaultLane;

	@Value("${postman.twitter.webhook.url}")
	private String webhookUrl;

	@Value("${postman.twitter.webhook.path}")
	private String webhookPath;

	@Autowired
	private PMEnvironment environment;

	@Autowired
	private ExtUtilService extUtilService;

	public TwitterClientContext getContext(String lane) {
		// lane = ArgUtil.nonEmpty(lane, defaultLane);

		if (ArgUtil.isEmpty(lane)) {
			throw new PostManException("No lane " + lane);
		}

		TwitterClientContext ctx = CLIENTS.get(lane);

		if (ArgUtil.isEmpty(ctx)) {
			TwitterConfig config = environment.get().twitter(lane);
			if (!ArgUtil.is(config)) {
				throw new PostManException("No Config for lane " + lane);
			}

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(config.getConsumerKey())
					.setOAuthConsumerSecret(config.getConsumerSecret()).setOAuthAccessToken(config.getAccessToken())
					.setOAuthAccessTokenSecret(config.getAccessTokenSecret());
			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter tw = tf.getInstance();
			ctx = new TwitterClientContext(tw);
			if (ArgUtil.is(config.getEnvName())) {
				WebhookManager manager = new WebhookManager(tw.getConfiguration(), config.getEnvName());
				ctx.setWebhookManager(manager);
			}
			CLIENTS.put(lane, ctx);
		}
		return ctx;
	}

	public DirectMessage sendReply(String id, String text, String lane) throws NumberFormatException, TwitterException {
		Twitter twitter = getContext(lane).getTwitter();
		DirectMessageLocalImpl x = new DirectMessageLocalImpl(twitter.sendDirectMessage(Long.parseLong(id), text));
		LOGGER.debug("Message result to {} : {}", id, x.getId());
		return x;
	}

	public DirectMessage sendReply(String id, String text, String mediaId, String lane)
			throws NumberFormatException, TwitterException {
		Twitter twitter = getContext(lane).getTwitter();
		DirectMessageLocalImpl x = new DirectMessageLocalImpl(
				twitter.sendDirectMessage(Long.parseLong(id), text, Long.parseLong(mediaId)));
		LOGGER.debug("Message result to {} : {}", id, x.getId());
		return x;
	}

	public String uploadMedia(String lane, String url, String title)
			throws IOException, MalformedURLException, TwitterException {
		Twitter twitter = getContext(lane).getTwitter();
		InputStream media = new java.net.URL(url).openStream();
		UploadedMedia uploadedMedia = twitter.uploadMedia(title, media);
		return ArgUtil.parseAsString(uploadedMedia.getMediaId());
	}

	public OutboxMessage send(OutboxMessage message) {
		String to = CollectionUtil.getOne(message.getTo());
		String lane = message.getLane();

		StringJoiner msgIds = new StringJoiner(",");

		StringJoiner sj = new StringJoiner("\n");
		sj.add(message.getMessage());

		try {
			if (ArgUtil.is(message.getAttachments())) {
				for (Attachment attachment : message.getAttachments()) {
					if (ArgUtil.is(attachment.getMediaURL())) {
						if (ArgUtil.areEqual(attachment.getMediaType(), FileType.IMAGE.toString())) {
							String mediaId = uploadMedia(lane, attachment.getMediaURL(), attachment.getMediaCaption());
							attachment.mediaId(mediaId);
							DirectMessage resp = sendReply(to,
									ArgUtil.nonEmpty(attachment.getMediaCaption(), message.getSubject()), mediaId,
									lane);
							if (ArgUtil.is(resp.getId()))
								msgIds.add(ArgUtil.parseAsString(resp.getId()));
						} else {
							sj.add(extUtilService.tinyUrl(attachment.getMediaURL()));
						}
					}
				}
			}

			if (sj.length() > 0) {
				DirectMessage resp = sendReply(to, sj.toString(), lane);
				if (ArgUtil.is(resp.getId()))
					msgIds.add(ArgUtil.parseAsString(resp.getId()));
			}
			message.setMessageIdExt(msgIds.toString());
		} catch (IOException | TwitterException e) {
			throw new PostManException(e.getMessage());
		}
		return message;
	}

	public DirectMessageList pollDirectMessagesReceived(String lane) throws TwitterException {
		TwitterClientContext ctx = getContext(lane);
		return ctx.getDirectMessagesReceived(5);
	}

	public StatusCode registerWebhook(String lane, String callbackURL) {
		// lane = ArgUtil.nonEmpty(lane, defaultLane);
		TwitterClientContext ctx = getContext(lane);
		LOGGER.info("RegisterWebHook " + callbackURL + webhookPath + "/" + lane);
		return ctx.registerWebhook(callbackURL + webhookPath + "/" + lane);
	}

	public StatusCode registerWebhook(String lane) {
		TwitterConfig config = environment.get().twitter(lane);
		if (ArgUtil.isEmpty(config)) {
			LOGGER.info("No Config " + lane);
		}

		if (ArgUtil.is(config.getWebhookUrl())) {
			return registerWebhook(lane, config.getWebhookUrl());
		}
		return null;
	}

	/**
	 * Registers is only once if valid webhook is not registered
	 * 
	 * @param lane
	 * @return
	 */
	public StatusCode registerWebhookOnce(String lane) {
		TwitterClientContext ctx = getContext(lane);
		WebhookInfo webhookInfo = ctx.getWebhookInfo();
		if (ArgUtil.isEmpty(webhookInfo) || !webhookInfo.isValid()) {
			return registerWebhook(lane);
		}
		return null;
	}

	public Map<String, String> verifyCRC(String lane, String crcToken)
			throws InvalidKeyException, NoSuchAlgorithmException {
		TwitterClientContext ctx = getContext(lane);
		Map<String, String> map = new HashMap<String, String>();
		map.put("response_token",
				"sha256=" + new HashBuilder().secret(ctx.getTwitter().getConfiguration().getOAuthConsumerSecret())
						.message(crcToken).toHashHmac("HmacSHA256").hash());
		return map;

	}

}