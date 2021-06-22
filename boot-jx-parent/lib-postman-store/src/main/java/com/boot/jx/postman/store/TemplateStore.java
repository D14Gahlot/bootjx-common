package com.boot.jx.postman.store;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.PostmanPackages.TemplateResolver;
import com.boot.jx.postman.doc.HSMTemplate;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;

@Component
public class TemplateStore implements TemplateResolver {

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public BasicTemplate get(String templateId) {
		HSMTemplate x = mongoTemplate.findById(templateId, HSMTemplate.class);
		return x;
	}

}
