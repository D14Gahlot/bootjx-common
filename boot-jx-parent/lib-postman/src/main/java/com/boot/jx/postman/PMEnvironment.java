package com.boot.jx.postman;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.utils.ArgUtil;

@Component
public class PMEnvironment {

	public static interface PMEnvironmentProvider {
		public PMConfiguration config();
	}

	@Autowired(required = false)
	private PMEnvironmentProvider provider;

	public PMConfiguration config() {
		if (ArgUtil.is(provider)) {
			return provider.config();
		}
		return null;
	}

}
