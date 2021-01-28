package com.boot.jx.admin.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.postman.doc.MediaReply;
import com.boot.jx.postman.doc.SmartReply;
import com.boot.utils.UniqueID;

@RestController
public class TemplateController {

	@Autowired
	MongoTemplate mongoTemplate;

	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.POST })
	public List<SmartReply> mapSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		SmartReply sr = new SmartReply();
		sr.id().setCategory(category);
		sr.id().setSubject(subject);
		sr.setTemplate(template);
		sr.setMessage(message);
		sr.setUniqueId(UniqueID.generateString());
		mongoTemplate.save(sr);
		return mongoTemplate.findAll(SmartReply.class);
	}

	@RequestMapping(value = "/category/map/smart_reply", method = { RequestMethod.GET })
	public List<SmartReply> listSmartReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		return mongoTemplate.findAll(SmartReply.class);
	}

	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.POST })
	public List<MediaReply> mapMediaReply(@RequestParam String gallery, @RequestParam String subject,
			@RequestParam(required = false) String mediaurl, @RequestParam(required = false) String message) {
		MediaReply sr = new MediaReply();
		sr.id().setGallery(gallery);
		sr.id().setSubject(subject);
		sr.setMessage(message);
		sr.setMedia(mediaurl);
		sr.setUniqueId(UniqueID.generateString());
		mongoTemplate.save(sr);
		return mongoTemplate.findAll(MediaReply.class);
	}

	@RequestMapping(value = "/gallery/map/media_reply", method = { RequestMethod.GET })
	public List<MediaReply> listMediaReply(@RequestParam String category, @RequestParam String subject,
			@RequestParam(required = false) String template, @RequestParam(required = false) String message) {
		return mongoTemplate.findAll(MediaReply.class);
	}

}
