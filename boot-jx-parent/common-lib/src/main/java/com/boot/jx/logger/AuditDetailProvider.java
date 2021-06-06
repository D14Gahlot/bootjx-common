package com.boot.jx.logger;

import java.io.Serializable;

public interface AuditDetailProvider extends Serializable {

	@Deprecated
	public default AuditActor getActor() {
		return null;
	};

	public String getAuditUser();
}
