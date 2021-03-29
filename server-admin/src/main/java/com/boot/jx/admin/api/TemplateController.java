package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.utils.ArgUtil;

@RestController
public class TemplateController {

	@Autowired
	MongoTemplate mongoTemplate;

	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.POST })
	public List<QuickReply> mapSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		QuickReply sr = new QuickReply();
		sr.setTemplate(template);
		sr.setMessage(message);
		mongoTemplate.save(sr);
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<QuickReply> listSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.GET })
	public ApiResponse<QuickReply, Object> listQuickReply() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class));
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.DELETE })
	public ApiResponse<QuickReply, Object> deleteQuickReply(@RequestParam String id) {
		QuickReply qr = new QuickReply();
		qr.setId(id);
		mongoTemplate.remove(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class)).data(qr).message("QuickReply deleted");
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.POST })
	public ApiResponse<QuickReply, Object> createQuickReply(@RequestParam(required = false) String id,
			@RequestParam String category, @RequestParam String title,
			@RequestParam(required = false) String template) {
		QuickReply qr = null;
		if (ArgUtil.is(id)) {
			qr = mongoTemplate.findById(id, QuickReply.class);
		}

		if (!ArgUtil.is(qr)) {
			qr = new QuickReply();
		}

		qr.setCategory(category);
		qr.setTitle(title);
		qr.setTemplate(template);
		mongoTemplate.save(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class)).data(qr).message("QuickReply created");
	}

	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.GET })
	public ApiResponse<QuickAction, Object> listQuickAction() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class));
	}

	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.DELETE })
	public ApiResponse<QuickAction, Object> deleteQuickAction(@RequestParam String id) {
		QuickAction qr = new QuickAction();
		qr.setId(id);
		mongoTemplate.remove(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class)).data(qr)
				.message("QuickAction deleted");
	}

	@RequestMapping(value = "/api/tmpl/quickaxn", method = { RequestMethod.POST })
	public ApiResponse<QuickAction, Object> createQuickAction(@RequestParam(required = false) String id,
			@RequestParam String category, @RequestParam String title, String code) {
		QuickAction qr = null;
		if (ArgUtil.is(id)) {
			qr = mongoTemplate.findById(id, QuickAction.class);
		}

		if (!ArgUtil.is(qr)) {
			qr = new QuickAction();
		}

		qr.setCategory(category);
		qr.setTitle(title);
		qr.setAction(code);
		mongoTemplate.save(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class)).data(qr)
				.message("QuickAction created");
	}

	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.GET })
	public ApiResponse<QuickLabel, Object> listQuickTag() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class));
	}

	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.DELETE })
	public ApiResponse<QuickLabel, Object> deleteQuickTag(@RequestParam String id) {
		QuickLabel qr = new QuickLabel();
		qr.setId(id);
		mongoTemplate.remove(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class)).data(qr).message("QuickLabel deleted");
	}

	@RequestMapping(value = "/api/tmpl/quicklabels", method = { RequestMethod.POST })
	public ApiResponse<QuickLabel, Object> createQuickTag(@RequestParam(required = false) String id,
			@RequestParam String category, @RequestParam String title, String code) {
		QuickLabel qr = null;
		if (ArgUtil.is(id)) {
			qr = mongoTemplate.findById(id, QuickLabel.class);
		}

		if (!ArgUtil.is(qr)) {
			qr = new QuickLabel();
		}

		qr.setCategory(category);
		qr.setTitle(title);
		qr.setCode(code);
		mongoTemplate.save(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class)).data(qr).message("QuickLabel created");
	}
}
