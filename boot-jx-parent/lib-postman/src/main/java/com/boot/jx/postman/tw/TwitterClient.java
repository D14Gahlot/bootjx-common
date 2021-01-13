package com.boot.jx.postman.tw;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.fb.FacebookMessageResponse;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

@Component
@PropertySource("classpath:application-twitter.properties")
@EnableEncryptableProperties
public class TwitterClient {

	@Value("${twitter4j.oauth.consumer-key}")
	String consumerKey;

	@Value("${twitter4j.oauth.consumer-secret}")
	String consumerKeySecret;

	@Value("${twitter4j.oauth.access-token}")
	String accessToken;

	@Value("${twitter4j.oauth.access-token-secret}")
	String accessTokenSecret;
	
	Twitter twitter =null;
	

	private static final Logger LOGGER = LoggerFactory.getLogger(TwitterClient.class);

	@Autowired
	RestService restService;

	@Autowired
	private Environment environment;

	@PostConstruct
	public void init() {
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerKeySecret)
				.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		setTwitter(twitter);
	}

	public String registerWebhook(String token, String challenge, String lane) {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		String verifyToken = environment.getProperty("twitter." + lane + ".verifyToken");
		if (token != null && !token.isEmpty() && token.equals(verifyToken)) {
			return challenge;
		} else {
			return "Wrong Token";
		}
	}

	public void sendReply(String id, String text, String lane) throws NumberFormatException, TwitterException {
		lane = ArgUtil.nonEmpty(lane, "default").toLowerCase();
		//String accessToken = environment.getProperty("facebook.lane." + lane + ".accessToken");
		//FacebookMessageResponse response = new FacebookMessageResponse();
		//response.setMessageType("text");
		//response.getRecipient().put("id", id);
		//response.getMessage().put("text", text);
		//String result = restService.ajax("https://graph.facebook.com/v2.6/me/messages?access_token=" + accessToken)
		//		.post(response).asString();
		 twitter.sendDirectMessage(Long.parseLong(id), text);
		LOGGER.info("Message result to {} : {}", id);

	}
	public Twitter getTwitter() {
		return twitter;
	}

	public void setTwitter(Twitter twitter) {
		this.twitter = twitter;
	}
}