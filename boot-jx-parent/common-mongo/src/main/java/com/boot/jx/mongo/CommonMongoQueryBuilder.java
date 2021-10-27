package com.boot.jx.mongo;

public class CommonMongoQueryBuilder extends CommonMongoQB<CommonMongoQueryBuilder, Object> {

    public static abstract class DocQueryBuilder<T> extends CommonMongoQB<DocQueryBuilder<T>, T> {
	protected T doc;
	protected boolean synced;
	private long updatedStamp;

	@SuppressWarnings("unchecked")
	public DocQueryBuilder(T doc) {
	    this.doc = doc;
	    whereId(getId(this.doc));
	    this.docClass = (Class<T>) this.doc.getClass();
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

	public void updatedStamp() {
	    this.set("updatedStamp", System.currentTimeMillis());
	}
    }

}
