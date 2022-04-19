package com.boot.jx.tmpl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PostmanPackages.ICommonTmplPackage;
import com.boot.jx.postman.PostmanPackages.TemplateResolver;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

@Component
public class CommonTmpPackageImpl implements ICommonTmplPackage {

	@Autowired
	private TemplateService templateService;

	@Autowired(required = false)
	private TemplateResolver templateResolver;

	public static final Handlebars HANDLEBARS = new Handlebars();

	@Override
	public CommonFile process(CommonFile file, ContactType contactType) {
		if (ArgUtil.is(templateResolver) && ArgUtil.is(file.getTemplate())) {
			BasicTemplate basicTemplate = templateResolver.get(file.getTemplate());
			if (ArgUtil.is(basicTemplate)) {
				String content = this.process(basicTemplate.getTemplate(), file.getModel());
				file.options().putAll(basicTemplate.options());
				if (ArgUtil.is(content)) {
					String[] x = content.split("---options---");
					file.setContent(x[0]);
					if (x.length > 1 && ArgUtil.is(x[1])) {
						file.options().putAll(StringUtils.toMap(x[1]));
					}
					String categoryType = basicTemplate.getCategoryType();
					if (ArgUtil.is(categoryType)) {
						file.meta().put("categoryType", categoryType);
					}

				}
				return file;
			}
		}
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
