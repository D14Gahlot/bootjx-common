package com.boot.jx.admin.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.postman.doc.QuickReply;

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
	public List<QuickReply> listQuickReply() {
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.DELETE })
	public List<QuickReply> deleteQuickReply(@RequestParam String id) {
		QuickReply qr = new QuickReply();
		qr.setId(id);
		mongoTemplate.remove(qr);
		return mongoTemplate.findAll(QuickReply.class);
	}

	@RequestMapping(value = "/api/tmpl/quickreps", method = { RequestMethod.POST })
	public List<QuickReply> createQuickReply(@RequestParam String category, @RequestParam String title,
			@RequestParam(required = false) String template) {
		return mongoTemplate.findAll(QuickReply.class);
	}

}
