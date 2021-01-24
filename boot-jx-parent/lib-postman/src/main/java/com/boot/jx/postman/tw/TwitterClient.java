package com.boot.jx.postman.tw;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil.HashBuilder;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import twitter4j.DirectMessageList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Component
@PropertySource("classpath:application-twitter.properties")
@EnableEncryptableProperties
public class TwitterClient {

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
	private Environment environment;

	private String getProperty(String lane, String property) {
		String accessToken = environment.getProperty("postman.twitter.lane." + lane + "." + property);
		return accessToken;
	}

	public TwitterClientContext getContext(String lane) {
		lane = ArgUtil.nonEmpty(lane, defaultLane).toLowerCase();
		TwitterClientContext ctx = CLIENTS.get(lane);
		if (ArgUtil.isEmpty(ctx)) {
			String consumerKey = getProperty(lane, "consumer-key");
			String consumerKeySecret = getProperty(lane, "consumer-secret");
			String accessToken = getProperty(lane, "access-token");
			String accessTokenSecret = getProperty(lane, "access-token-secret");
			String envName = getProperty(lane, "env_name");

			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerKeySecret)
					.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);
			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter tw = tf.getInstance();
			ctx = new TwitterClientContext(tw);
			if (ArgUtil.is(envName)) {
				WebhookManager manager = new WebhookManager(tw.getConfiguration(), envName);
				ctx.setWebhookManager(manager);
			}
			CLIENTS.put(lane, ctx);
		}
		return ctx;
	}

	public void sendReply(String id, String text, String lane) throws NumberFormatException, TwitterException {
		Twitter twitter = getContext(lane).getTwitter();
		twitter.sendDirectMessage(Long.parseLong(id), text);
		LOGGER.info("Message result to {} : {}", id);
	}

	public DirectMessageList pollDirectMessagesReceived(String lane) throws TwitterException {
		TwitterClientContext ctx = getContext(lane);
		return ctx.getDirectMessagesReceived(5);
	}

	public StatusCode registerWebhook(String lane, String callbackURL) {
		lane = ArgUtil.nonEmpty(lane, defaultLane).toLowerCase();
		TwitterClientContext ctx = getContext(lane);
		return ctx.registerWebhook(callbackURL + webhookPath + "/" + lane);
	}

	public StatusCode registerWebhook(String lane) {
		if (ArgUtil.is(webhookUrl)) {
			return registerWebhook(lane, webhookUrl);
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