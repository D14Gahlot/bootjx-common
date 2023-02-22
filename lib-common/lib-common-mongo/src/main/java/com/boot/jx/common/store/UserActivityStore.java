package com.boot.jx.common.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.common.doc.UserActivityLogDoc;
import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;

@Component
public class UserActivityStore extends CommonMongoTemplateAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserActivityStore.class);

	@Autowired
	private AppConfig appConfig;

	public UserActivityLogDoc log(UserActivityLogDoc doc) {
		doc.setCreatedAt(TimeStampIndex.now());
		doc.setAppType(appConfig.getAppType());
		save(doc);
		return doc;
	}

	public UserActivityLogDoc log(String user, String activity) {
		UserActivityLogDoc doc = new UserActivityLogDoc();
		doc.setActivity(activity);
		return log(doc);
	}

}
