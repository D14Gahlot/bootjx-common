package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiFieldError;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ApiResponseUtil;
import com.boot.jx.common.doc.UserActivityLogDoc;
import com.boot.jx.dict.ContactType;
import com.boot.jx.mongo.CommonDocInterfaces.AuditActivityDoc;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.mongo.CommonMongoTemplate.PaginatedQuery;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.MessageDoc;
import com.boot.jx.postman.doc.MessageDoc.MessageDocLogs;
import com.boot.jx.postman.doc.MessageHold;
import com.boot.jx.postman.doc.MessageHold.MESSAGE_QUEUE_TYPE;
import com.boot.jx.postman.doc.config.ChannelConfigDupsDoc;
import com.boot.jx.postman.doc.config.ChannelConfigLogger;
import com.boot.jx.postman.doc.tpo.PayloadDumpCollection;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.utils.PostManUtil;
import com.boot.model.MapModel;
import com.boot.model.UtilityModels.PublicJsonProperty;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AdminObjectsController {

	@Autowired
	private CommonMongoTemplate comonMongoTemplate;

	@Autowired
	private MessageStore messageStore;

	public <T> List<T> getPaginatedBulk(Class<T> docClass, String collectionName, int pageNo, int pageSize,
			String sortBy, String sortDir, MapModel extraParams) {
		return comonMongoTemplate.getPages(PaginatedQuery.select(docClass, collectionName).pageNo(pageNo)
				.pageSize(pageSize).pageSize(pageSize).sortBy(sortBy).sortDir(sortDir).extraParams(extraParams))
				.getResults();
	}

	public <T> List<T> getPaginatedBulk(Class<T> docClass, String collectionName, int pageNo, int pageSize,
			String sortBy, String sortDir) {
		return getPaginatedBulk(docClass, collectionName, pageNo, pageSize, sortBy, sortDir, MapModel.createInstance());
	}

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

		return ApiResponse.buildResults(comonMongoTemplate.find(q));
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
		return ApiResponse.buildResults(comonMongoTemplate.find(q));
	}

	@RequestMapping(value = "/api/objects/change_logs", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<AuditActivityDoc, Object> getChangeLogs(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String collection, @RequestParam(required = false) String createdBy) {
		MQB<AuditActivityDoc> q = MongoQueryBuilder.select(AuditActivityDoc.class, "ZACTIVITY_LOGS").page(pageNo,
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
		return ApiResponse.buildResults(comonMongoTemplate.find(q));
	}

	@RequestMapping(value = "/api/objects/payload_dump", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<PayloadDumpCollection, Object> getPayLoadDump(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String type) {
		return ApiResponse.buildResults(
				getPaginatedBulk(PayloadDumpCollection.class, "PAYLOAD_DUMP", pageNo, pageSize, sortBy, sortDir));
	}

	@RequestMapping(value = { "/api/objects/channel_setup_logs" }, method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<ChannelConfigLogger, Object> channelSetupLogs(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String domain,
			@RequestParam(required = false) String lane,
			@RequestParam(required = false, defaultValue = "false") boolean local) {

		MapModel extparams = MapModel.createInstance();
		if (!local && !Tenants.isDefault(AppContextUtil.getTenant())) {
			extparams.put("domain", AppContextUtil.getTenant());
			AppContextUtil.switchTenant(Tenants.getDefault());
		}

		return ApiResponse.buildResults(
				getPaginatedBulk(ChannelConfigLogger.class, "TEMP_CONFIG_CHANNEL", pageNo, pageSize, sortBy, sortDir));
	}

	@RequestMapping(value = { "/api/objects/archive/channel" }, method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<ChannelConfigDupsDoc, Object> channelArchive(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String domain,
			@RequestParam(required = false) String lane) {
		return ApiResponse.buildResults(
				getPaginatedBulk(ChannelConfigDupsDoc.class, "DUPS_CONFIG_CHANNEL", pageNo, pageSize, sortBy, sortDir));
	}

	@RequestMapping(value = { "/api/objects/messages/{messageQueueType}" }, method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<MessageHold, Object> queuedMessages(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String domain,
			@RequestParam(required = false) String lane, @PathVariable MESSAGE_QUEUE_TYPE messageQueueType) {
		return ApiResponse.buildResults(
				getPaginatedBulk(MessageHold.class, "MESSAGE_" + messageQueueType, pageNo, pageSize, sortBy, sortDir));
	}

	@RequestMapping(value = "/api/objects/messages", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<MessageDoc, Object> allMessages(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String sessionId,
			@RequestParam(required = false, value = "contact.contactType") ContactType contactType,
			@RequestParam(required = false) String channelType, @RequestParam(required = false) String channelId,
			@RequestParam(required = false) String domain, @RequestParam(required = false) String lane) {

		if (ArgUtil.is(sessionId)) {
			ChatSessionDoc chatSessionDoc = comonMongoTemplate.findById(sessionId, ChatSessionDoc.class);
			if (ArgUtil.is(chatSessionDoc)) {
				contactType = ContactType.valueOf(chatSessionDoc.getContactType());
			}
		} else if (ArgUtil.is(channelId)) {
			Contactable c = PostManUtil.parseChannelId(channelId);
			contactType = c.type();
		}

		if (!ArgUtil.is(contactType)) {
			ApiResponseUtil.throwInputException(new ApiFieldError().field("contactType").codeKey("INVALID_CONTACT_TYPE")
					.description("Invalid Contact Type " + contactType));
		}

		return ApiResponse.buildResults(getPaginatedBulk(MessageDoc.class, MessageStore.getCollectionName(contactType),
				pageNo, pageSize, sortBy, sortDir));
	}

	@RequestMapping(value = "/api/objects/sessions", method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<ChatSessionDoc, Object> getSession(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "createdStamp") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String type) {
		return ApiResponse.buildResults(
				getPaginatedBulk(ChatSessionDoc.class, "CHAT_SESSION", pageNo, pageSize, sortBy, sortDir));
	}
}
