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
import com.boot.jx.http.CommonHttpRequest.ApiRequestDetail;
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

	public static final String USE_DEFAULT_DB = "USE_DEFAULT_DB";
	public static final String USE_NO_DB = "USE_NO_DB";

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

	private static Object lock = new Object();
	MongoDbFactory mongoDbFactory;
	MongoTemplate mongoTemplate;

	private static Object lockNoDb = new Object();
	static MongoDbFactory mongoDbFactoryNoDb;
	static MongoTemplate mongoTemplateNoDb;

	private static Object lockDefault = new Object();
	static MongoDbFactory mongoDbFactoryDefault;
	static MongoTemplate mongoTemplateDefault;

	boolean ready = false;

	private boolean hasRule(String useNoDb) {
		ApiRequestDetail apiDetails = AppContextUtil.getApiRequestDetail();
		return ArgUtil.is(apiDetails) && apiDetails.hasRule(useNoDb);
	}

	public MongoDbFactory getMongoDbFactory(String dataSourceUrl) {
		String tnt = AppContextUtil.getTenant();
		String dbtnt = ArgUtil.is(tenantResolver) ? tenantResolver.getDBName(tnt) : tnt;
		MongoClientURI mongoClientURI = new MongoClientURI(dataSourceUrl);

		String dataBaseName = (globalDBProfix + "_" + dbtnt);
		if (hasRule(USE_NO_DB)) {
			// dataBaseName = "nodb";
			dataBaseName = mongoClientURI.getDatabase();
		} else if ((!ArgUtil.areEqual(StringUtils.trim(dataSourceUrl), StringUtils.trim(globalDataSourceUrl))
				|| Tenants.isDefault(tnt) || (hasRule(USE_DEFAULT_DB)))) {
			dataBaseName = mongoClientURI.getDatabase();
		}
		LOGGER.info("MONGODB: {}:{}:{}", dataBaseName, Tenants.isDefault(tnt), dbtnt);
		return new SimpleMongoDbFactory(new MongoClient(mongoClientURI), dataBaseName);
	}

	public MongoDbFactory getMongoDbFactory() {
		if (hasRule(USE_NO_DB)) {
			if (mongoDbFactoryNoDb == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactoryNoDb = getMongoDbFactory(dataSourceUrl);
				LOGGER.warn("mongoDbFactoryNoDb was NULL So created One");
			}
			return mongoDbFactoryNoDb;
		} else if (hasRule(USE_DEFAULT_DB)) {
			if (mongoDbFactoryDefault == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactoryDefault = getMongoDbFactory(dataSourceUrl);
				LOGGER.warn("mongoDbFactoryNoDb was NULL So created One");
			}
			return mongoDbFactoryDefault;
		} else {
			if (mongoDbFactory == null && ArgUtil.is(dataSourceUrl)) {
				mongoDbFactory = getMongoDbFactory(dataSourceUrl);
				LOGGER.warn("mongoTemplate was NULL So created One");
				ready = true;
			}
			return mongoDbFactory;
		}
	}

	public MongoTemplate getMongoTemplate() {

		if (hasRule(USE_NO_DB)) {
			if (mongoTemplateNoDb == null) {
				synchronized (lockNoDb) {
					LOGGER.info("mongoTemplateNoDb is NULL So creating One {} {}", getDataSourceUrl(),
							getDataSourceUsername());
					mongoDbFactoryNoDb = getMongoDbFactory();
					if (ArgUtil.is(mongoDbFactoryNoDb)) {
						mongoTemplateNoDb = new MongoTemplate(mongoDbFactoryNoDb);
						LOGGER.debug("mongoTemplateNoDb was NULL So created One");
					} else {
						LOGGER.error("mongoDbFactoryNoDb was NULL So cannot create One");
					}
				}
			} else {
				LOGGER.error("mongoDbFactoryNoDb = {}", mongoDbFactoryNoDb.getDb().getName());
			}
			return mongoTemplateNoDb;
		} else if (hasRule(USE_DEFAULT_DB)) {
			if (mongoTemplateDefault == null) {
				synchronized (lockDefault) {
					LOGGER.info("mongoTemplate is NULL So creating One {} {}", getDataSourceUrl(),
							getDataSourceUsername());
					mongoDbFactoryDefault = getMongoDbFactory();
					if (ArgUtil.is(mongoDbFactoryDefault)) {
						mongoTemplateDefault = new MongoTemplate(mongoDbFactoryDefault);
						LOGGER.debug("mongoTemplateDefault was NULL So created One");
						ready = true;
					} else {
						LOGGER.error("mongoDbFactoryDefault was NULL So cannot create One");
					}
				}
			} else {
				LOGGER.error("mongoDbFactoryDefault = {}", mongoTemplateNoDb.getDb().getName());
			}
			return mongoTemplateDefault;
		} else {
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
			} else {
				LOGGER.debug("mongoDbFactory = {}", mongoDbFactory.getDb().getName());
			}
			return mongoTemplate;
		}
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
