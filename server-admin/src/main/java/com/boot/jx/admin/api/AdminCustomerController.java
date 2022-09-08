package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.model.ModelPatch;
import com.boot.jx.mongo.CommonMongoQB.CommonMongoQBimpl;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.store.ContactStore;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@RestController("/api/cusomter")
public class AdminCustomerController {

	@Autowired
	private ContactStore contactStore;

	// CustomerProfile
	@RequestMapping(value = "/profile", method = { RequestMethod.GET })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> getProfiles(@RequestParam(required = false) String id,
			@RequestParam(required = false, defaultValue = "0") int pageNo,
			@RequestParam(required = false, defaultValue = "25") int pageSize,
			@RequestParam(required = false) String sortBy,
			@RequestParam(required = false, defaultValue = "asc") String sortDir) {
		CommonMongoQBimpl<CustomerProfileDoc> q = CommonMongoQueryBuilder.collection(CustomerProfileDoc.class)
				.page(pageNo, pageSize);

		if (ArgUtil.is(sortBy)) {
			q = q.sortBy(sortBy, Direction.fromString(sortDir));
		}
		return ApiResponse.buildResults(contactStore.find(q));
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.POST })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> createProfiles(@RequestBody CustomerProfileDoc req) {
		contactStore.save(req);
		return ApiResponse.buildResult(req);
	}

	@RequestMapping(value = "/profile", method = { RequestMethod.PATCH })
	@JsonView(PMEnvironment.PublicProperty.class)
	public ApiResponse<CustomerProfileDoc, Object> modifyProfiles(@RequestBody ModelPatch req) {
		return ApiResponse.buildResult(contactStore.patchCustomerProfile(req));
	}

}
