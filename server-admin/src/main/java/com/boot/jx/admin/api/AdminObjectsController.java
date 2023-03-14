package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.UserActivityLogDoc;
import com.boot.jx.mongo.CommonDocInterfaces.AuditActivityDoc;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.MessageDoc.MessageDocLogs;
import com.boot.jx.postman.store.MessageStore;
import com.boot.model.UtilityModels.PublicJsonProperty;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AdminObjectsController {

	@Autowired
	private MessageStore messageStore;

	@RequestMapping(value = "/api/objects/logs", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<MessageDocLogs, Object> getLogs(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortDir) {
		MongoQueryBuilder<MessageDocLogs> q = MongoQueryBuilder.collection(MessageDocLogs.class).page(pageNo, pageSize);

		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		return ApiResponse.buildResults(messageStore.find(q));
	}

	@RequestMapping(value = "/api/objects/user_activities", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<UserActivityLogDoc, Object> getActivityLogs(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdAt.stamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String appType, @RequestParam(required = false) String user) {
		MongoQueryBuilder<UserActivityLogDoc> q = MongoQueryBuilder.collection(UserActivityLogDoc.class).page(pageNo,
				pageSize);

		if (ArgUtil.is(appType)) {
			q.where("appType").is(appType);
		}
		if (ArgUtil.is(user)) {
			q.where("user").is(user);
		}

		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		return ApiResponse.buildResults(messageStore.find(q));
	}

	@RequestMapping(value = "/api/objects/change_logs", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<AuditActivityDoc, Object> getChangeLogs(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String collection, @RequestParam(required = false) String createdBy) {
		MongoQueryBuilder<AuditActivityDoc> q = MongoQueryBuilder.collection(AuditActivityDoc.class).page(pageNo,
				pageSize);

		if (ArgUtil.is(collection)) {
			q.where("collection").is(collection);
		}
		if (ArgUtil.is(createdBy)) {
			q.where("createdBy").is(createdBy);
		}

		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		return ApiResponse.buildResults(messageStore.find(q));
	}
}
