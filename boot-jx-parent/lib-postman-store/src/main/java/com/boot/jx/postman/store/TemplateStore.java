package com.boot.jx.postman.store;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.model.CommonTemplateMeta;
import com.boot.jx.mongo.CommonMongoQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.PostmanPackages.TemplateResolver;
import com.boot.jx.postman.doc.HSMTemplateDoc;
import com.boot.jx.postman.model.ITemplates.BasicTemplate;
import com.boot.utils.ArgUtil;

@Component
public class TemplateStore implements TemplateResolver {

	@Autowired
	protected CommonMongoTemplate commonMongoTemplate;

	@Override
	public BasicTemplate get(String templateId) {
		HSMTemplateDoc x = commonMongoTemplate.findById(templateId, HSMTemplateDoc.class);
		return x;
	}

	public BasicTemplate resolve(CommonTemplateMeta template) {
		if (ArgUtil.is(template.getId())) {
			return get(template.getId());
		} else if (ArgUtil.is(template.getCode())) {
			List<HSMTemplateDoc> temps = commonMongoTemplate
					.find(CommonMongoQueryBuilder.collection(HSMTemplateDoc.class).where("code", template.getCode()));
			if (ArgUtil.is(temps)) {
				HSMTemplateDoc resolvedTemplate = null;
				if (temps.size() > 1) {
					for (HSMTemplateDoc hsmTemplate3rdParty : temps) {
						if (ArgUtil.areEqual(hsmTemplate3rdParty.getLang(), template.getLang())) {
							resolvedTemplate = hsmTemplate3rdParty;
							break;
						} else if (ArgUtil.is(hsmTemplate3rdParty.getLang())) {
							resolvedTemplate = hsmTemplate3rdParty;
						}
					}
				} else {
					resolvedTemplate = temps.get(0);
				}
				return resolvedTemplate;
			}
		}
		return null;
	}

	@Override
	public BasicTemplate get(CommonTemplateMeta template) {
		BasicTemplate basicTemplate = resolve(template);
		if (ArgUtil.is(basicTemplate)) {
			template.setCode(basicTemplate.getCode());
			template.setId(basicTemplate.getId());
		}
		return basicTemplate;
	}

}
