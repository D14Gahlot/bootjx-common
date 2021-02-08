package com.boot.jx.tmpl.custom;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;

import com.google.common.collect.Sets;

public class ThymeleafDatabaseResourceResolver extends StringTemplateResolver {
	private final static String PREFIX = "";

	@Autowired
	ThymeleafTemplateDao thymeleaftemplateDao;

	public ThymeleafDatabaseResourceResolver() {
		setResolvablePatterns(Sets.newHashSet(PREFIX + "*"));
	}

	@Override
	protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate,
			String template, Map<String, Object> templateResolutionAttributes) {

		// ThymeleafTemplate is our internal object that contains the content.
		// You should change this to match you're set up.

		ThymeleafTemplate thymeleafTemplate = thymeleaftemplateDao.findByTemplateName(template);
		if (thymeleafTemplate != null) {
			return super.computeTemplateResource(configuration, ownerTemplate, thymeleafTemplate.getContent(),
					templateResolutionAttributes);
		}
		return null;
	}
}
