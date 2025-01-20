package com.boot.jx.common.api;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.logger.LoggerService;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.MongoUtils;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;

import io.swagger.annotations.ApiOperation;

@RestController
public class CommonMonogController {

	private static final Logger LOGGER = LoggerService.getLogger(CommonMonogController.class);

	@Autowired
	CommonMongoTemplate commonMongoTemplate;

	@ApiOperation(value = "Only for test")
	@RequestMapping(value = { "/pub/mongo/clean" }, method = RequestMethod.GET)
	public ApiResponse<String, Object> cleanMongoCollection(@RequestParam String collectionName)
			throws URISyntaxException, MalformedURLException {
		Class<?> targetClass = ChatSessionDoc.class;

		switch (collectionName) {
		case "CHAT_SESSION":
			targetClass = ChatSessionDoc.class;
			break;
		default:
			if (collectionName.indexOf("MESSAGE_") == 0)
				targetClass = MessageDoc.class;
			break;
		}

		return ApiResponse.buildResults(MongoUtils.cleanupIndexes(commonMongoTemplate, collectionName, targetClass));
	}

}
