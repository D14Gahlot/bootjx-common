package com.boot.jx.postman;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.dict.ContactType;
import com.boot.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonView;

@Component
public class PMEnvironment {

	public static interface PMEnvironmentProvider {
		public PMConfiguration config();
	}

	public static interface PMConnectorConfig extends Serializable {

		public static interface Public {

		}

		public static interface Protected extends Public {

		}

		@JsonView(PMConnectorConfig.Public.class)
		public String getLane();

		@JsonView(PMConnectorConfig.Public.class)
		public default String getChannel() {
			return null;
		}

		@JsonView(PMConnectorConfig.Public.class)
		public boolean isPushAllowed();

		@JsonView(PMConnectorConfig.Public.class)
		public boolean isPushOnlyApproved();

		@JsonView(PMConnectorConfig.Public.class)
		public boolean isPushFreeTextAllowed();

		@JsonView(PMConnectorConfig.Public.class)
		public boolean isPushToNewContactAllowed();

		@JsonView(PMConnectorConfig.Public.class)
		public ContactType getContactType();
	}

	public static class PMConfigurationObject extends MapEntry implements Serializable {

		private static final long serialVersionUID = 2678154770516185408L;
		String key;
		String description;

		public PMConfigurationObject(String key, Object value) {
			super(value);
			this.key = key;
		}

		public PMConfigurationObject() {
			super(null);
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

	}

	@Autowired(required = false)
	private PMEnvironmentProvider provider;

	public PMConfiguration config() {
		PMConfiguration config = null;
		if (ArgUtil.is(provider)) {
			config = provider.config();
		}
		if (config == null) {
			config = new PMConfiguration();
		}
		return config;
	}

	@Autowired
	AppConfig appConfig;

	public PMConfigurationObject get(String key) {
		PMConfigurationObject config = this.config().map().get(key);
		if (ArgUtil.isEmpty(config)) {
			String value = appConfig.prop(key);
			config = new PMConfigurationObject(key, value);
			this.config().map().put(key, config);
		}
		return config;
	}

}
