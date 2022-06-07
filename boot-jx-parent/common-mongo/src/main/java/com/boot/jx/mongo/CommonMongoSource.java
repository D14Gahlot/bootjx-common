package com.boot.jx.mongo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.AppContextUtil;
import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.scope.tnt.TenantValue;
import com.boot.jx.scope.tnt.Tenants;
import com.boot.jx.scope.tnt.Tenants.TenantResolver;
import com.boot.utils.ArgUtil;
import com.boot.utils.StringUtils;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;

@Component
@TenantScoped
public class CommonMongoSource {

	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@TenantValue("${spring.data.mongodb.database}")
	String dataSourceDatabase;

	@TenantValue("${spring.data.mongodb.uri}")
	String dataSourceUrl;

	@Value("${spring.data.mongodb.uri}")
	String globalDataSourceUrl;

	@Value("${spring.data.mongodb.prefix}")
	String globalDBProfix;

	@TenantValue("${spring.data.mongodb.username}")
	String dataSourceUsername;

	@TenantValue("${spring.data.mongodb.password}")
	String dataSourcePassword;

	@Autowired(required = false)
	TenantResolver tenantResolver;

	public String getDataSourceUrl() {
		return dataSourceUrl;
	}

	public String getDataSourceUsername() {
		return dataSourceUsername;
	}

	public String getDataSourcePassword() {
		return dataSourcePassword;
	}

	MongoTemplate mongoTemplate;
	MongoDbFactory mongoDbFactory;

	boolean ready = false;

	private static Object lock = new Object();

	public MongoDbFactory getMongoDbFactory(String dataSourceUrl) {
		String tnt = AppContextUtil.getTenant();
		String dbtnt = ArgUtil.is(tenantResolver) ? tenantResolver.getDBName(tnt) : tnt;
		MongoClientURI mongoClientURI = new MongoClientURI(dataSourceUrl);
		String dataBaseName = (!ArgUtil.areEqual(StringUtils.trim(dataSourceUrl), StringUtils.trim(globalDataSourceUrl))
				|| Tenants.isDefault(tnt)) ? mongoClientURI.getDatabase() : (globalDBProfix + "_" + dbtnt);
		LOGGER.info("MONGODB: {}:{}:{}", dataBaseName, Tenants.isDefault(tnt), dbtnt);
		return new SimpleMongoDbFactory(new MongoClient(mongoClientURI), dataBaseName);
	}

	public MongoDbFactory getMongoDbFactory() {
		if (mongoDbFactory == null && ArgUtil.is(dataSourceUrl)) {
			mongoDbFactory = getMongoDbFactory(dataSourceUrl);
			LOGGER.debug("mongoTemplate was NULL So created One");
			ready = true;
		}
		return mongoDbFactory;
	}

	public MongoTemplate getMongoTemplate() {
		if (mongoTemplate == null) {
			synchronized (lock) {
				LOGGER.debug("mongoTemplate is NULL So creating One {} {}", getDataSourceUrl(),
						getDataSourceUsername());
				mongoDbFactory = getMongoDbFactory();
				if (ArgUtil.is(mongoDbFactory)) {
					mongoTemplate = new MongoTemplate(mongoDbFactory);
					LOGGER.debug("mongoTemplate was NULL So created One");
					ready = true;
				} else {
					LOGGER.error("mongoDbFactory was NULL So cannot create One");
				}
			}
		}
		return mongoTemplate;
	}

	public boolean isReady() {
		return ready;
	}

	public String getDataSourceDatabase() {
		return dataSourceDatabase;
	}

	public void setDataSourceDatabase(String dataSourceDatabase) {
		this.dataSourceDatabase = dataSourceDatabase;
	}

	public String getGlobalDataSourceUrl() {
		return globalDataSourceUrl;
	}

	public void setGlobalDataSourceUrl(String globalDataSourceUrl) {
		this.globalDataSourceUrl = globalDataSourceUrl;
	}

	public String getGlobalDBProfix() {
		return globalDBProfix;
	}

	public void setGlobalDBProfix(String globalDBProfix) {
		this.globalDBProfix = globalDBProfix;
	}

	public void setDataSourceUrl(String dataSourceUrl) {
		this.dataSourceUrl = dataSourceUrl;
	}

	public void setDataSourceUsername(String dataSourceUsername) {
		this.dataSourceUsername = dataSourceUsername;
	}

	public void setDataSourcePassword(String dataSourcePassword) {
		this.dataSourcePassword = dataSourcePassword;
	}

}
