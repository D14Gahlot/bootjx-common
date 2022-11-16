package com.boot.jx.account.doc;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;

@Component
public class AccountStore extends CommonMongoTemplateAbstract<AccountStore> {

	public static final MapModel NODOMAIN = MapModel.createInstance().put("domain").put("account").put("partner")
			.put("front").put("cpanel").put("admin").put("agent").put("docs");

	public boolean isValidDomainName(String domain) {
		if (!ArgUtil.is(domain) || domain.length() < 4) {
			return false;
		}
		domain = StringUtils.toLowerCase(domain);
		return !NODOMAIN.containsKey(domain);
	}

	public <T> List<T> findByKey(String key, Object value, Class<T> clazz) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where(key).is(value));
		List<T> docs = find(query2, clazz);
		return docs;
	}

	public <T> T findOneByKey(String key, Object value, Class<T> clazz) {
		Query query2 = new Query();
		query2.addCriteria(Criteria.where(key).is(value));
		T doc = findOne(query2, clazz);
		return doc;
	}

	public BusinessUserDoc findUserByEmail(String email) {
		return findOneByKey("contact.email", email, BusinessUserDoc.class);
	}

	public List<BusinessUserDoc> findAllUsersByDomainId(String domainId) {
		return findByKey("domains.$id", new ObjectId(domainId), BusinessUserDoc.class);
	}

	public DomainDoc findDomainByName(String domain) {
		DomainDoc x = findOneByKey("domain", domain, DomainDoc.class);
		if (ArgUtil.is(x)) {
			return x;
		}
		return findOneByKey("domainAlias", domain, DomainDoc.class);
	}

	public List<DomainDoc> findAllDomainByServer(String server) {
		return findByKey("server", server, DomainDoc.class);
	}

	public DomainLicenseDoc findDomainLicenseByName(String domain) {
		return findOneByKey("domain", domain, DomainLicenseDoc.class);
	}

}
