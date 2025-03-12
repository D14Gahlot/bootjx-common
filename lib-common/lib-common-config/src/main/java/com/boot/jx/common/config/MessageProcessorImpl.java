package com.boot.jx.common.config;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.MessageProcessor;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.postman.plugin.ChannelConfig;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

@Component
public class MessageProcessorImpl implements MessageProcessor {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Autowired
	private PMClientConfig pmClientConfig;

	@Override
	public OutboxMessage beforeSend(OutboxMessage outboxMessage, ChannelConfig channelConfig) {
		PMConfigurationObject trackMessage = pmEnvironment.local().prefsEntry(CONFIG_SETUP_KEY.POSTMAN_TRACK_MESSAGE);
		if (trackMessage.exists() && trackMessage.asBoolean()) {
			String trackMessageUrl = pmEnvironment.config().prefsEntry(CONFIG_SETUP_KEY.POSTMAN_TRACK_MESSAGE_URL)
					.asString(pmClientConfig.getWebhookBase(channelConfig, "nexus"));

			MapModel options = MapModel.from(outboxMessage.options());

			List<TmplElement> allbuttons = options.entry("buttons").asList(TmplElement.class);// null

			String channelShortCode = ArgUtil
					.parseAsEnumT(outboxMessage.contact().getContactType(), ContactType.WEBSITE, ContactType.class)
					.getShortCode();

			for (TmplElement button : allbuttons) {
				try {
					if (ArgUtil.areEqual(button.getType(), TmplElement.TYPES.URL)) {
						button.setShorturl(String.format("%s/link/short/%s/%s/%s?tnt=%s", trackMessageUrl,
								channelShortCode, outboxMessage.getMessageId(), CryptoUtil.getMD5Hash(button.getUrl()),
								AppContextUtil.getTenant()));;
					}
				} catch (NoSuchAlgorithmException e) {
					outboxMessage.logs().add("ShortUrl not created");
				}
			}
		}
		return outboxMessage;
	}

}
