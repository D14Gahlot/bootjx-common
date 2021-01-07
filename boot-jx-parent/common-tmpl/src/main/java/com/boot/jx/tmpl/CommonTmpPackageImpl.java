package com.boot.jx.tmpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.PostmanPackages.ICommonTmplPackage;
import com.boot.jx.postman.model.File;

@Component
public class CommonTmpPackageImpl implements ICommonTmplPackage {

	@Autowired
	private TemplateService templateService;

	@Override
	public File process(File file, ContactType contactType) {
		return templateService.process(file, contactType);
	}

}
