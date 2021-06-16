package com.boot.jx.admin.api;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.boot.jx.AppContextUtil;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.aws.AWSFileStore;
import com.boot.jx.postman.doc.QuickAction;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.doc.QuickReply;
import com.boot.jx.postman.doc.QuickMedia;
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

		QuickReply newVersion = new QuickReply();
		if (ArgUtil.is(id)) {
			QuickReply oldVersion = mongoTemplate.findById(id, QuickReply.class);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
				newVersion.setId(id);
			}
		}

		newVersion.setCategory(category);
		newVersion.setTitle(title);
		newVersion.setTemplate(template);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickReply.class)).data(newVersion)
				.message("QuickReply created");
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

		QuickAction newVersion = new QuickAction();
		if (ArgUtil.is(id)) {
			QuickAction oldVersion = mongoTemplate.findById(id, QuickAction.class);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
				newVersion.setId(id);
			}
		}

		newVersion.setCategory(category);
		newVersion.setTitle(title);
		newVersion.setAction(code);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickAction.class)).data(newVersion)
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
		QuickLabel newVersion = new QuickLabel();
		if (ArgUtil.is(id)) {
			QuickLabel oldVersion = mongoTemplate.findById(id, QuickLabel.class);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
				newVersion.setId(id);
			}
		}

		newVersion.setCategory(category);
		newVersion.setTitle(title);
		newVersion.setCode(code);
		mongoTemplate.save(newVersion);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickLabel.class)).data(newVersion)
				.message("QuickLabel created");
	}

	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.GET })
	public ApiResponse<QuickMedia, Object> listQuickMedia() {
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class));
	}

	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.DELETE })
	public ApiResponse<QuickMedia, Object> deleteQuickMedia(@RequestParam String id) {
		QuickMedia qr = new QuickMedia();
		qr.setName(id);
		mongoTemplate.remove(qr);
		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class)).data(qr)
				.message("Quick Media deleted");
	}

	@Autowired
	AWSFileStore fileStore;

	@RequestMapping(value = "/api/tmpl/quickmedia", method = { RequestMethod.POST })
	public ApiResponse<QuickMedia, Object> createQuickMedia(@RequestParam(required = false) String name,
			@RequestParam String category, @RequestParam String title, @RequestParam(required = false) String url,
			@RequestParam(name = "file", required = false) MultipartFile file) {

		if (ArgUtil.isEmpty(url) && ArgUtil.is(file)) {
			url = fileStore
					.upload1(file, String.format("%s/quickmedia/%s", AppContextUtil.getTenant(), UUID.randomUUID()),
							file.getOriginalFilename())
					.getUrl();
		} else if (ArgUtil.isEmpty(url)) {
			throw new IllegalStateException("Cannot upload empty file");
		}
		QuickMedia newVersion = new QuickMedia();
		if (ArgUtil.is(name)) {
			QuickMedia oldVersion = mongoTemplate.findById(name, QuickMedia.class);
			if (ArgUtil.is(oldVersion)) {
				newVersion.oldVersion(oldVersion);
				newVersion.setName(name);
			}
		}

		newVersion.setTitle(title);
		newVersion.setType("IMAGE");
		newVersion.setCategory(category);
		newVersion.setUrl(url);

		mongoTemplate.save(newVersion);

		return ApiResponse.buildResults(mongoTemplate.findAll(QuickMedia.class)).data(newVersion)
				.message("Quick Media created");
	}
}
