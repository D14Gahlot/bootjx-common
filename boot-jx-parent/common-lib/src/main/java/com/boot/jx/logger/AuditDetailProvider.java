package com.boot.jx.logger;

import com.boot.jx.model.AuditCreateEntity;
import com.boot.jx.model.AuditCreateEntity.AuditUpdateEntity;

public interface AuditDetailProvider {

    @Deprecated
    public default AuditActor getActor() {
	return null;
    };

    public String getAuditUser();

    public default <T extends AuditCreateEntity> T auditCreate(T entity) {
	entity.setCreatedBy(getAuditUser());
	entity.setCreatedStamp(System.currentTimeMillis());
	return entity;
    }

    public default <T extends AuditUpdateEntity> T auditUpdate(T entity) {
	entity.setUpdatedBy(getAuditUser());
	entity.setUpdatedStamp(System.currentTimeMillis());
	return entity;
    }

}
