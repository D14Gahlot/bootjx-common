package com.boot.jx.mongo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.boot.jx.model.AuditableEntity;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.boot.utils.TimeUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class CommonDocInterfaces {

    public static interface MongoQueryBuilder<T> {
	public boolean isUpdatedTimeStampSupport();

	public void updatedStamp();

	public Update getUpdate();

	public void setUpdate(Update object);

	public Query getQuery();

	public Class<T> getDocClass();
    }

    public static interface Patchable<T extends Patchable<T>> {
	public T patch();
    }

    public static interface PatchableIndexed<T extends PatchableIndexed<T, I>, I> extends Patchable<T> {
	public T newInstance();

	public void savePatch(T patch);

	public T fetchPatch();

	default public T patch() {
	    if (fetchPatch() == null) {
		T patch = newInstance();
		this.savePatch(patch);
		patch.id(this.id());
	    }
	    return (T) fetchPatch();
	}

	public void id(I id);

	public I id();
    }

    public static abstract class APatchableIndexed<T extends APatchableIndexed<T, I>, I>
	    implements PatchableIndexed<T, I> {
	@JsonIgnore
	private T patch;

	@Override
	public void savePatch(T patch) {
	    this.patch = patch;
	}

	@Override
	public T fetchPatch() {
	    return this.patch;
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static interface OldDocVersion<T extends OldDocVersion<T>> {

	public void setOldVersions(List<T> arrayList);

	public List<T> getOldVersions();

	public default void oldVersion(T oldVersion) {
	    if (ArgUtil.is(oldVersion.getOldVersions())) {
		this.setOldVersions(oldVersion.getOldVersions());
	    } else {
		this.setOldVersions(new ArrayList<T>());
	    }
	    oldVersion.setOldVersions(null);
	    this.getOldVersions().add(oldVersion);
	}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static interface DocVersion extends OldDocVersion<DocVersion> {

    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static interface IDocument {
    }

    public interface ADocumentDTO<T extends ADocumentDTO<T>> extends IDocument, Serializable {

	@SuppressWarnings("unchecked")
	default public T importFrom(IDocument entity) {

	    if (ArgUtil.is(entity)) {
		EntityDtoUtil.entityToDto(entity, this);
	    }

	    return (T) this;
	}

	default public List<T> importFrom(List<? extends IDocument> entityList) {
	    List<T> list = new ArrayList<T>();
	    for (IDocument entity : entityList) {
		T dto = this.newInstance().importFrom(entity);
		list.add(dto);
	    }
	    return list;
	}

	ADocumentDTO<T> newInstance();
    }

    @Document(collection = "TRASH")
    public static class TrashDocument implements AuditableEntity, Serializable {
	private static final long serialVersionUID = -8573412950623297045L;
	@Id
	private String id;
	private Object doc;
	private String createdBy;
	private Long createdStamp;

	public String getId() {
	    return id;
	}

	public void setId(String id) {
	    this.id = id;
	}

	public Object getDoc() {
	    return doc;
	}

	public void setDoc(Object doc) {
	    this.doc = doc;
	}

	public String getCreatedBy() {
	    return createdBy;
	}

	public void setCreatedBy(String createdBy) {
	    this.createdBy = createdBy;
	}

	public Long getCreatedStamp() {
	    return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
	    this.createdStamp = createdStamp;
	}

	public TrashDocument doc(Object doc) {
	    this.doc = doc;
	    return this;
	}

    }

    public static class BasicDocument<T extends BasicDocument<T>>
	    implements OldDocVersion<T>, IDocument, AuditableEntity, Serializable {

	private static final long serialVersionUID = 3330736275464700381L;
	private String createdBy;
	private Long createdStamp;

	@Field("oldVersions")
	private List<T> oldVersions;

	@Override
	public void setOldVersions(List<T> oldVersions) {
	    this.oldVersions = oldVersions;
	}

	@Override
	public List<T> getOldVersions() {
	    return this.oldVersions;
	}

	public String getCreatedBy() {
	    return createdBy;
	}

	public void setCreatedBy(String createdBy) {
	    this.createdBy = createdBy;
	}

	public Long getCreatedStamp() {
	    return createdStamp;
	}

	public void setCreatedStamp(Long createdStamp) {
	    this.createdStamp = createdStamp;
	}

    }

    public static class TimeStamp implements Serializable {

	private static final long serialVersionUID = 9114924334759684396L;
	private long stamp;
	@Indexed
	private long hour;
	@Indexed
	private long day;
	@Indexed
	private long week;

	public long getStamp() {
	    return stamp;
	}

	public void setStamp(long stamp) {
	    this.stamp = stamp;
	}

	public long getHour() {
	    return hour;
	}

	public void setHour(long hour) {
	    this.hour = hour;
	}

	public long getDay() {
	    return day;
	}

	public void setDay(long day) {
	    this.day = day;
	}

	public long getWeek() {
	    return week;
	}

	public void setWeek(long week) {
	    this.week = week;
	}

	public static TimeStamp from(long stamp) {
	    TimeStamp timeStamp = new TimeStamp();
	    timeStamp.setHour(stamp / TimeUtils.Constants.MILLIS_IN_HOUR);
	    timeStamp.setDay(stamp / TimeUtils.Constants.MILLIS_IN_DAY);
	    timeStamp.setWeek(stamp / TimeUtils.Constants.MILLIS_IN_WEEK);
	    return timeStamp;
	}

	public static TimeStamp now() {
	    return from(System.currentTimeMillis());
	}

	public interface UpdatedTimeStampSupport {
	    public TimeStamp getUpdated();

	    public void setUpdated(TimeStamp updated);
	}

	public static class UpdatedTimeStampDoc implements UpdatedTimeStampSupport {
	    private TimeStamp updated;

	    public TimeStamp getUpdated() {
		return updated;
	    }

	    public void setUpdated(TimeStamp updated) {
		this.updated = updated;
	    }
	}
    }

}
