package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.BusinessUserDoc;
import com.boot.jx.common.models.AppAuthModels;
import com.boot.jx.dict.ContactType;
import com.boot.jx.http.ApiRequest;
import com.boot.jx.mongo.CommonMongoStore.PaginatedQuery;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PMConstants;
import com.boot.jx.postman.PMEnvironment.SummaryView;
import com.boot.jx.postman.doc.config.ChannelConfigDupsDoc;
import com.boot.jx.postman.doc.config.ChannelConfigLogger;
import com.boot.jx.postman.store.MessageStore;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.model.MapModel;
import com.boot.model.UtilityModels.PublicJsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

@RestController
public class AdminSuperMasterController {

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

		return ApiResponse
				.buildResults(
						comonMongoTemplate
								.getPages(PaginatedQuery.select(ChannelConfigLogger.class, "TEMP_CONFIG_CHANNEL")
										.pageNo(pageNo).pageSize(pageSize).sortBy(sortBy).sortDir(sortDir))
								.getResults());

	}

	@ApiRequest(rules = { AppAuthModels.ACCESS_RULES.CAN_ACCESS_ALL_DOMAINS })
	@RequestMapping(value = { "/api/objects/archive/channel" }, method = { RequestMethod.GET })
	@JsonView(PublicJsonProperty.class)
	public ApiResponse<ChannelConfigDupsDoc, Object> channelArchive(@RequestParam(required = false) String id,
			@RequestParam(required = false) ContactType contactType, @RequestParam(required = false) String channelType,
			@RequestParam(required = false) String channelId, @RequestParam(required = false) String domain,
			@RequestParam(required = false) String lane) {
		return ApiResponse.buildResults(comonMongoTemplate
				.getPages(PaginatedQuery.select(ChannelConfigDupsDoc.class, "DUPS_CONFIG_CHANNEL").count())
				.getResults());
	}

	@ApiRequest(rules = { AppAuthModels.ACCESS_RULES.CAN_ACCESS_ALL_DOMAINS })
	@RequestMapping(value = { "/api/objects/partner/users" }, method = { RequestMethod.GET })
	@JsonView({ PublicJsonProperty.class, SummaryView.class })
	public ApiResponse<BusinessUserDoc, Object> partnerUsers() {
		return ApiResponse.buildResults(comonMongoTemplate
				.getPages(PaginatedQuery.select(BusinessUserDoc.class, "DOMAIN_USER").skipDBRef().count())
				.getResults());
	}

}
