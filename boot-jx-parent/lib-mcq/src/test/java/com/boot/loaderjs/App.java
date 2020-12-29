package com.boot.loaderjs;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.nustaq.serialization.FSTConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.dict.UserClient.AppType;
import com.boot.jx.dict.UserClient.DeviceType;
import com.boot.jx.dict.UserClient.UserDeviceClient;
import com.boot.jx.tunnel.DBEvent;
import com.boot.jx.tunnel.TunnelMessage;
import com.boot.loaderjs.App;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;

public class App { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^\\$\\{(.*)\\}$");

	private static Logger LOGGER = LoggerFactory.getLogger(App.class);

	/**
	 * This is just a test method
	 * 
	 * @param args
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws UnsupportedEncodingException
	 */
	public static void main(String[] args)
			throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {
		AppContext context = AppContextUtil.getContext();

		UserDeviceClient client = new UserDeviceClient();

		client.setIp("0:0:0:0:0:0:0:1");
		client.setFingerprint("38b3dd46de1d7df8303132bba73ca1e6");
		client.setDeviceType(DeviceType.COMPUTER);
		client.setAppType(AppType.WEB);
		context.setClient(client);
		context.setTraceId("TST-1d59nub55kbgg-1d59nub5827sx");
		context.setTranxId("TST-1d59nub55kbgg-1d59nub5827sx");

		DBEvent e = new DBEvent();
		e.setData(new HashMap<String, String>());
		e.setDescription("string");
		e.setEventCode("DATAUPD_CUSTOMER");
		e.setPriority("string");
		e.setText("string");

		TunnelMessage<DBEvent> message = new TunnelMessage<DBEvent>(
				e, context);
		message.setTopic("DATAUPD_CUSTOMER");

		String messageJson = JsonUtil.toJson(message);
		LOGGER.info("J====== {}", messageJson);
		TunnelMessage<DBEvent> message2 = JsonUtil.fromJson(messageJson,
				new TypeReference<TunnelMessage<DBEvent>>() {
				});
		LOGGER.info("J====== {}", JsonUtil.toJson(message2));

		FSTConfiguration conf = FSTConfiguration.createJsonConfiguration();
		byte[] bytes = conf.asByteArray(message);
		String messageJson2 = new String(bytes, "UTF-8");
		LOGGER.info("F====== {}", messageJson2);
		TunnelMessage<DBEvent> message3 = (TunnelMessage<DBEvent>) conf.asObject(bytes);

		LOGGER.info("F====== {}", JsonUtil.toJson(message3));

	}
}
