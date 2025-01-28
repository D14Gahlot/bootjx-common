package com.boot.jx.postman.pbook;

import java.io.Serializable;

import com.boot.model.UtilityModels.JsonIgnoreUnknown;
import com.boot.model.UtilityModels.UniqueIndex;
import com.boot.utils.ArgUtil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PBDate implements Serializable, Comparable<PBDate>, JsonIgnoreUnknown, UniqueIndex<PBDate> {

	private static final long serialVersionUID = -3845469973718851720L;
	
	public String uuid;
	public String date;
	public String type;
	public String label;
	public long stamp;
	public long stampLocal;
	public String timeZone;
	
	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public long getStamp() {
		return stamp;
	}
	public void setStamp(long stamp) {
		this.stamp = stamp;
	}
	public long getStampLocal() {
		return stampLocal;
	}
	public void setStampLocal(long stampLocal) {
		this.stampLocal = stampLocal;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	@Override
	public String uuid() {
		return this.uuid;
	}

	@Override
	public String uuid(String uuid) {
		if (ArgUtil.not(this.uuid)) {
			this.uuid = uuid;
		}
		return this.uuid;
	}
	@Override
	public PBDate update(PBDate fromObject) {
		this.date =fromObject.getDate();
		this.stamp =fromObject.getStamp();
		this.stampLocal =fromObject.getStampLocal();
		this.timeZone=fromObject.getTimeZone();
		return this;
	}
	
	@Override
	public int compareTo(PBDate o) {
		if (o == null) {
			return 1;
		}
		return this.toString().compareTo(o.toString());
	}
	
	
}
