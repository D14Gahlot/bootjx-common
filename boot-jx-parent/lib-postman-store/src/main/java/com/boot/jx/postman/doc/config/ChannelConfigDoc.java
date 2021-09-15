package com.boot.jx.postman.doc.config;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.postman.plugin.ChannelConfig;

@Document(collection = "CONFIG_CHANNEL")
@TypeAlias("ConfigChannel")
public class ChannelConfigDoc extends ChannelConfig {

    private static final long serialVersionUID = -6368905475787041196L;

    @Id
    private String id;

    public String getId() {
	return id;
    }

    public void setId(String id) {
	this.id = id;
    }

}
