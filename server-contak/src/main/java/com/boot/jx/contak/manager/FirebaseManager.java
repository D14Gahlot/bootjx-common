package com.boot.jx.contak.manager;

import java.util.HashMap;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.logger.LoggerService;
import com.boot.jx.rest.RestService;
import com.boot.utils.JsonUtil;

@Component
public class FirebaseManager {
	
	private Logger LOGGER = LoggerService.getLogger(FirebaseManager.class);
	
	@Autowired
	RestService restService;
	
	
	
	
	public void sendNotification(String number, String title, String body, HashMap<String,String> data) {
		LOGGER.debug("IN FIREBASE ");
		LOGGER.info("IN FIREBASE INFO");
		HashMap< String, Object> bodyObject = new HashMap<>();
		bodyObject.put("to", "/topics/oa_"+number);
		HashMap<String ,String> notification =  new HashMap<>();
		notification.put("title", title);
		notification.put("body",body);
		bodyObject.put("notification", notification);
		bodyObject.put("data", data);
		LOGGER.info("Request for firebase  "+JsonUtil.toJson(bodyObject));
		
		String response = restService.ajax("https://fcm.googleapis.com/fcm/send")
		.header("Authorization","key=AAAA13CquBk:APA91bFwITuk9RjqKHP_5b0yR1YacM7cVAK6VUpWIfSTXh5ySYB9egacKwfs8qFk238pZG_cQI1PxtOpmuFYGY32EF1LD9NMHCXk8b0o75n0Y4RVQmRRDMRjM835YA4E7XFxx_QoHrPL")
		.header("Content-Type", "application/json")
		.post(JsonUtil.toJson(bodyObject)).asString();
		
//		if (LOGGER.isDebugEnabled()) {
			LOGGER.info("Response for firebase "+response);
//		}
	}
}
