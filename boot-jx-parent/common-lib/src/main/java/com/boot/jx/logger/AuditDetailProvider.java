package com.boot.jx.logger;

import java.io.Serializable;

import com.boot.jx.model.AuditableEntity;

public interface AuditDetailProvider extends Serializable {

    @Deprecated
    public default AuditActor getActor() {
	return null;
    };

    public String getAuditUser();

    public default <T extends AuditableEntity> T audit(T entity) {
	entity.setCreatedBy(getAuditUser());
	entity.setCreatedStamp(System.currentTimeMillis());
	return entity;
    }

}
