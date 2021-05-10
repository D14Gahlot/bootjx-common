package com.boot.jx.postman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;

@Component
public class PMEnvironment {

	public static interface PMEnvironmentProvider {
		public ConnectorConfig get();
	}

	@Autowired(required = false)
	private PMEnvironmentProvider provider;

	public ConnectorConfig get() {
		if (ArgUtil.is(provider)) {
			return provider.get();
		}
		return null;
	}

}
