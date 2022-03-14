package com.boot.jx.account.doc;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;

@Component
public class DomainSummaryMetaStore extends CommonMongoTemplateAbstract {

	   public <T> List<T> findByKey(String key, String value, Class<T> clazz) {
			Query query2 = new Query();
			query2.addCriteria(Criteria.where(key).is(value));
			List<T> docs = find(query2, clazz);
			return docs;
		    }

		    public <T> T findOneByKey(String key, String value, Class<T> clazz) {
			Query query2 = new Query();
			query2.addCriteria(Criteria.where(key).is(value));
			T doc = findOne(query2, clazz);
			return doc;
		    }
		    
		    public <T> T findOneByMultiKey(String key1, String value1,String key2,String value2, Class<T> clazz) {
				Query query2 = new Query();
				query2.addCriteria(Criteria.where(key1).is(value1).and(key2).is(value2));
				T doc = findOne(query2, clazz);
				return doc;
			    }
		
		    
	
	 public DomainSummaryMetaDoc findDomainByName(String domain) {
			return findOneByKey("domain", domain, DomainSummaryMetaDoc.class);
      }
	 
	 
	 public DomainSummaryMessageDoc findDomainAndByName(String domain,String dtMonth) {
			return findOneByMultiKey("domain", domain,"date",dtMonth, DomainSummaryMessageDoc.class);
   }
}
