package com.boot.jx.postman;

import java.io.Serializable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.model.MapModel.MapEntry;
import com.boot.utils.ArgUtil;

@Component
public class PMEnvironment {

	public static interface PMEnvironmentProvider {
		public PMConfiguration config();
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

}
