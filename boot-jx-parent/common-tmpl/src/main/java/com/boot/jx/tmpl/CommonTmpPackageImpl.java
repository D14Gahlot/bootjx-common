package com.boot.jx.tmpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PostmanPackages.ICommonTmplPackage;
import com.boot.jx.postman.model.PostManFile;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

@Component
public class CommonTmpPackageImpl implements ICommonTmplPackage {

	@Autowired
	private TemplateService templateService;

	public static final Handlebars HANDLEBARS = new Handlebars();

	@Override
	public PostManFile process(PostManFile file, ContactType contactType) {
		return templateService.process(file, contactType);
	}

	@Override
	public String process(String templateContent, Object contact) {
		try {
			Template template = HANDLEBARS.compileInline(templateContent);
			return template.apply(contact);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return templateContent;
	}

}
