package com.boot.jx.common.config;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.MessageProcessor;
import com.boot.jx.postman.PMEnvironment.PMConfigurationObject;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.TmplElement;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CryptoUtil;

@Component
public class MessageProcessorImpl implements MessageProcessor {

	@Autowired
	private PMEnvironment pmEnvironment;

	@Override
	public OutboxMessage beforeSend(OutboxMessage outboxMessage) {
		PMConfigurationObject trackMessage = pmEnvironment.local().prefsEntry(CONFIG_SETUP_KEY.POSTMAN_TRACK_MESSAGE);
		if (trackMessage.exists() && trackMessage.asBoolean()) {
			PMConfigurationObject trackMessageUrl = pmEnvironment.config()
					.prefsEntry(CONFIG_SETUP_KEY.POSTMAN_TRACK_MESSAGE_URL);

			if (trackMessageUrl.exists()) {
				MapModel options = MapModel.from(outboxMessage.options());

				List<TmplElement> allbuttons = options.entry("buttons").asList(TmplElement.class);// null

				for (TmplElement button : allbuttons) {
					try {
						if (ArgUtil.areEqual(button.getType(), TmplElement.TYPES.URL)) {
							button.setShorturl(String.format("%s/nexus/link/short/wa/%s/%s", trackMessageUrl.asString(),
									outboxMessage.getMessageId(), CryptoUtil.getMD5Hash(button.getUrl())));;
						}
					} catch (NoSuchAlgorithmException e) {
						outboxMessage.logs().add("ShortUrl not created");
					}
				}
			}
		}
		return outboxMessage;
	}

}
