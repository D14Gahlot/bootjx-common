package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.admin.dto.ProfileSearchQuery;
import com.boot.jx.admin.service.CustomerProfileService;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonMongoQB.MongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.store.ContactStore;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController("Profile Controller")
@RequestMapping("/api/cusomter")
public class AdminCustomerController {

	@Autowired
	private ContactStore contactStore;
	
	@Autowired
	CustomerProfileService cusProfileService;

	// CustomerProfile
	@RequestMapping(value = "/profile", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, ChatContactDoc> getProfiles(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false, defaultValue = "created") String sortBy,
			@RequestParam(required = false, defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String contactId,

			@RequestParam(required = false, value = "search.name") String searchName,
			@RequestParam(required = false, value = "search.code") String searchCode,
			@RequestParam(required = false, value = "search.phones") String searchPhone,
			@RequestParam(required = false, value = "search.emails") String searchEmail) {
		if (ArgUtil.is(contactId)) {
			return ApiResponse.buildResults(contactStore.findProfileByContactId(contactId),
					contactStore.findById(contactId, ChatContactDoc.class));
		}
		MongoQueryBuilder<CustomerProfileDoc> q = MongoQueryBuilder.collection(CustomerProfileDoc.class).page(pageNo,
				pageSize);
		if (ArgUtil.is(id)) {
			q = q.whereId(id);
		}

		q.search("name.formattedName", searchName).search("code", searchCode).search("emails.email", searchEmail)
				.search("phones.phone", searchPhone);

		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		return ApiResponse.buildResults(contactStore.find(q), null);
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> createProfiles(@RequestBody CustomerProfileDoc req) {
		contactStore.save(req);
		return ApiResponse.buildResult(req);
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.DELETE })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> deleteProfiles(@RequestParam String id) {
		CustomerProfileDoc req = new CustomerProfileDoc();
		req.setId(id);
		contactStore.remove(req);
		return ApiResponse.buildResult(req);
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.PATCH })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> modifyProfiles(@RequestBody ModelPatches req) {
		contactStore.checkDuplicate(req);
		return ApiResponse.buildResult(contactStore.patchCustomerProfile(req));
	}

	@RequestMapping(value = "/profile/link", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChatContactDoc, Object> linkProfile(@RequestParam String profileId,
			@RequestParam String contactId) {
		return ApiResponse.buildResult(contactStore.linkProfile(contactId, profileId));
	}

	@RequestMapping(value = "/profile/link", method = { RequestMethod.DELETE })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<ChatContactDoc, Object> linkProfile(@RequestParam String contactId) {
		return ApiResponse.buildResult(contactStore.delinkProfile(contactId));
	}

	@RequestMapping(value = "/profile/create", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> createprofile(@RequestBody CustomerProfileDoc req) {
		return ApiResponse.buildResult(contactStore.createprofile(req));
	}
	
	@RequestMapping(value = "/profile/search", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> getProfiles(@RequestBody ProfileSearchQuery searchQry) {
		List<CustomerProfileDoc> docs = cusProfileService.getProfileSearch(searchQry);
		return ApiResponse.buildResults(docs);
	}

}
