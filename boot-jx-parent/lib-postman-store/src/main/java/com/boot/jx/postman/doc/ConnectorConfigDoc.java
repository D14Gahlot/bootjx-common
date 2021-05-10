package com.boot.jx.postman.doc;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.ConnectorConfig;

@Document(collection = "CONNECTOR_CONFIG")
@TypeAlias("ConfigDoc")
public class ConnectorConfigDoc extends ConnectorConfig {

	private static final long serialVersionUID = 7942286016346691701L;

	@Id
	private String tenant;

	public String getTenant() {
		return tenant;
	}

	public void setTenant(String tenant) {
		this.tenant = tenant;
	}

}
