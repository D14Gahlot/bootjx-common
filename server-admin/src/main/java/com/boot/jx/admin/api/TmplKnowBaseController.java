package com.boot.jx.admin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.model.ModelPatch.ModelPatches;
import com.boot.jx.mongo.CommonMongoQB.MQB;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.KnowBase;
import com.boot.jx.postman.doc.KnowBaseQA;
import com.boot.utils.ArgUtil;
import com.boot.utils.UniqueID;
import com.mongodb.client.result.UpdateResult;

@RestController
public class TmplKnowBaseController {
	@Autowired
	private CommonMongoTemplate mongoTemplate;

	// KnowledgeBase
	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.GET })
	public ApiResponse<KnowBase, Object> listKnowBase(@RequestParam(required = false) String parentId) {
		if (ArgUtil.is(parentId)) {
			return ApiResponse
					.buildResults(mongoTemplate.find(MQB.collection(KnowBase.class).where("parentId", parentId)))
					.meta(mongoTemplate.findById(parentId, KnowBase.class));
		} else {
			return ApiResponse.buildResults(mongoTemplate.find(MQB.collection(KnowBase.class).without("parentId")));
		}
	}

	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.DELETE })
	public ApiResponse<KnowBase, Object> deleteKnowBase(@RequestParam String id) {
		KnowBase qr = mongoTemplate.removeAndAudit(id, KnowBase.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(KnowBase.class)).data(qr).message("KnowBase deleted");
	}

	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.PATCH })
	public ApiResponse<KnowBase, Object> patchKnowBase(@RequestBody ModelPatches req)
			throws InstantiationException, IllegalAccessException {
		UpdateResult qr = mongoTemplate.patch(req, KnowBase.class);
		return ApiResponse.buildResults(mongoTemplate.findById(req.getId(), KnowBase.class))
				.message("KnowBase updated");
	}

	@RequestMapping(value = "/api/tmpl/knowbase", method = { RequestMethod.POST })
	public ApiResponse<KnowBase, Object> createKnowBase(@RequestBody KnowBase req) {
		KnowBase newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new KnowBase());
		newVersion.setParentId(req.getParentId());
		newVersion.setCode(req.getCode());
		newVersion.setType(req.getType());
		newVersion.setCategory(req.getCategory());

		newVersion.setTitle(req.getTitle());
		newVersion.setStartnote(req.getStartnote());
		newVersion.setContent(req.getContent());
		newVersion.setEndnote(req.getEndnote());

		if (ArgUtil.is(newVersion.getParentId())) { // It is a Page
			KnowBase parent = mongoTemplate.findById(newVersion.getParentId(), KnowBase.class);
			if (ArgUtil.is(parent)) {
				newVersion.setCode(parent.getCode());
				newVersion.setType(parent.getType());
				newVersion.setCategory(parent.getCategory());
			}
		} else {
			if (!ArgUtil.is(newVersion.getCode())) { // Add code
				newVersion.setCode(UniqueID.generateString62());
			}

			if (ArgUtil.is(newVersion.getId())) { // Update Knowledge requires all pages to be updated
				mongoTemplate.updateMulti(MQB.collection(KnowBase.class).where("parentId", newVersion.getId())//
						.set("code", newVersion.getCode())//
						.set("type", newVersion.getType())//
						.set("category", newVersion.getCategory()));
			}

		}
		mongoTemplate.saveAndAudit(newVersion, ArgUtil.is(newVersion.getId()));
		return listKnowBase(newVersion.getParentId()).message("KnowBase Saved");
	}

	// KnowledgeBaseQA
	@RequestMapping(value = "/api/tmpl/kbqa", method = { RequestMethod.GET })
	public ApiResponse<KnowBaseQA, Object> listKnowBaseQA(@RequestParam(required = false) String parentId,
			@RequestParam(required = false) String pageId) {
		if (ArgUtil.is(pageId)) {
			return ApiResponse
					.buildResults(mongoTemplate.find(MQB.collection(KnowBaseQA.class).where("pageId", pageId)))
					.meta(mongoTemplate.findById(pageId, KnowBaseQA.class));
		} else if (ArgUtil.is(parentId)) {
			return ApiResponse
					.buildResults(mongoTemplate.find(MQB.collection(KnowBaseQA.class).where("parentId", parentId)))
					.meta(mongoTemplate.findById(parentId, KnowBaseQA.class));
		} else {
			return ApiResponse.buildResults(mongoTemplate.find(MQB.collection(KnowBaseQA.class).without("parentId")));
		}
	}

	@RequestMapping(value = "/api/tmpl/kbqa", method = { RequestMethod.DELETE })
	public ApiResponse<KnowBaseQA, Object> deleteKnowBaseQA(@RequestParam String id) {
		KnowBaseQA qr = mongoTemplate.removeAndAudit(id, KnowBaseQA.class);
		return ApiResponse.buildResults(mongoTemplate.findAll(KnowBaseQA.class)).data(qr).message("KnowBaseQA deleted");
	}

	@RequestMapping(value = "/api/tmpl/kbqa", method = { RequestMethod.PATCH })
	public ApiResponse<KnowBaseQA, Object> patchKnowBaseQA(@RequestBody ModelPatches req)
			throws InstantiationException, IllegalAccessException {
		UpdateResult qr = mongoTemplate.patch(req, KnowBaseQA.class);
		return ApiResponse.buildResults(mongoTemplate.findById(req.getId(), KnowBaseQA.class))
				.message("KnowBaseQA updated");
	}

	@RequestMapping(value = "/api/tmpl/kbqa", method = { RequestMethod.POST })
	public ApiResponse<KnowBaseQA, Object> createKnowBaseQA(@RequestBody KnowBaseQA req) {
		KnowBaseQA newVersion = mongoTemplate.findByIdOrDefault(req.getId(), new KnowBaseQA());
		newVersion.setParentId(req.getParentId());
		newVersion.setPageId(req.getPageId());
		newVersion.setQuestion(req.getQuestion());
		newVersion.setAnswer(req.getAnswer());
		// mongoTemplate.saveAndAudit(newVersion, ArgUtil.is(newVersion.getId()));
		return listKnowBaseQA(newVersion.getParentId(), newVersion.getPageId()).message("KnowBaseQA Saved");
	}
}