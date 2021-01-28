package com.boot.jx.postman.dms;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.scope.tnt.TenantValue;
import com.boot.utils.ArgUtil;
import com.cloudinary.Cloudinary;

/**
 * The Class GeoServiceController.
 */
@Component
@TenantScoped
public class DMService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DMService.class);

	@TenantValue("${cloudinary.name}")
	private String cloudinaryName;

	@TenantValue("${cloudinary.api.key}")
	private String cloudinaryApiKey;

	@TenantValue("${cloudinary.api.secret}")
	private String cloudinaryApiSecret;

	private Cloudinary cloudinary;

	public Cloudinary cloudinary() {
		if (!ArgUtil.is(this.cloudinary)) {
			Map<String, Object> config = new HashMap<String, Object>();
			config.put("cloud_name", cloudinaryName);
			config.put("api_key", cloudinaryApiKey);
			config.put("api_secret", cloudinaryApiSecret);
			this.cloudinary = new Cloudinary(config);
		}
		return this.cloudinary;
	}

	public String getCloudinaryApiSecret() {
		return cloudinaryApiSecret;
	}

	public String getCloudinaryName() {
		return cloudinaryName;
	}

	public String getCloudinaryApiKey() {
		return cloudinaryApiKey;
	}

}
