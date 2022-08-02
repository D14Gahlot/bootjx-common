package com.boot.jx.admin.api;

import java.util.List;
import java.util.Map;

import org.jboss.logging.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.common.doc.AppFaqDoc;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.utils.ArgUtil;

@RestController
public class AppFaqController {

	@Autowired
	private CommonMongoTemplate mongoTemplate;

	
	@RequestMapping(value = "/pub/app/faq", method = { RequestMethod.POST })
	public ApiResponse<AppFaqDoc, Object> createAppFaq(@RequestBody AppFaqDoc appFaqDoc) {
		mongoTemplate.save(appFaqDoc);
		return ApiResponse.buildResults(mongoTemplate.findAll(AppFaqDoc.class)).message("AppFaq  created");
	}
  
	@RequestMapping(value = "/pub/app/faq/parent", method = { RequestMethod.GET })
	public ApiResponse<AppFaqDoc, Object> getFaqParent(@RequestParam(required = false) String lang) {
		return ApiResponse.buildResults(getParents(lang));
	}
	
	
	@RequestMapping(value = "/pub/app/faq/parent/code", method = { RequestMethod.GET })
	public ApiResponse<AppFaqDoc, Object> getFaqByParentOrCode(
			@RequestParam(required = false) String lang,
			@RequestParam(required = false) String parent,
			@RequestParam(required = false) String code
			) {
		return ApiResponse.buildResults(getFaqByCode(lang,parent,code));
	}
	
	public List<AppFaqDoc> getParents(String lang){
		Query query = new Query();
		if(ArgUtil.is(lang)) {
			query.addCriteria(Criteria.where("parent").is("").and("translation."+lang).exists(true));
		}else {
			query.addCriteria(Criteria.where("parent").is(""));
		}
		List<AppFaqDoc> faqParentLst = mongoTemplate.find(query, AppFaqDoc.class);
	    return faqParentLst;
	}
	public List<AppFaqDoc> getFaqByCode(String lang,String parent,String code){
		Query query = new Query();
		if(ArgUtil.is(lang) && ArgUtil.is(parent)) {
			query.addCriteria(Criteria.where("parent").is(parent)
					.and("translation."+lang).exists(true));
		}else if(ArgUtil.is(parent) && ArgUtil.is(code)) {
			query.addCriteria(Criteria.where("parent").is(parent)
					.and("code").is(code));
		}else {
			query.addCriteria(Criteria.where("code").is(code));
		}
		List<AppFaqDoc> faqParentLst = mongoTemplate.find(query, AppFaqDoc.class);
	   return faqParentLst;
	}
}
