package com.boot.jx.mongo;

import com.boot.jx.mongo.CommonDocInterfaces.TimeStampIndex.UpdatedTimeStampIndexSupport;
import com.boot.utils.ArgUtil;

public class CommonMongoQueryBuilder extends CommonMongoQB<CommonMongoQueryBuilder, Object> {

    public static abstract class DocQueryBuilder<T> extends CommonMongoQB<DocQueryBuilder<T>, T> {

	protected T doc;
	protected boolean synced;
	private long updatedStamp;

	public DocQueryBuilder(T doc) {
	    this.doc = doc;
	    whereId(getId(this.doc));
	    this.synced = true;
	}

	public DocQueryBuilder(String id) {
	    this.doc = this.newDoc(id);
	    whereId(id);
	}

	public abstract T newDoc(String id);

	public abstract String getId(T doc);

	public boolean isSynced() {
	    return synced;
	}

	public void setSynced(boolean synced) {
	    this.synced = synced;
	}

	public long getUpdatedStamp() {
	    return updatedStamp;
	}

	public void setUpdatedStamp(long updatedStamp) {
	    this.updatedStamp = updatedStamp;
	}

	@Override
	public boolean isUpdatedTimeStampSupport() {
	    if (ArgUtil.is(this.doc)) {
		return this.doc instanceof UpdatedTimeStampIndexSupport;
	    }
	    return super.isUpdatedTimeStampSupport();
	}

	@SuppressWarnings("unchecked")
	public Class<T> getDocClass() {
	    if (docClass == null) {
		this.docClass = this.doc == null ? null : (Class<T>) this.doc.getClass();
	    }
	    return docClass;
	}

	public T getDoc() {
	    return doc;
	}
    }

}
