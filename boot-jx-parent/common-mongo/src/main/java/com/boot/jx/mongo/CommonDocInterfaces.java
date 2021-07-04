package com.boot.jx.mongo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import com.boot.jx.model.AuditableEntity;
import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class CommonDocInterfaces {

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

	public static class BasicDocument implements DocVersion, IDocument {

		@Field("oldVersions")
		private List<DocVersion> oldVersions;

		@Override
		public void setOldVersions(List<DocVersion> oldVersions) {
			this.oldVersions = oldVersions;
		}

		@Override
		public List<DocVersion> getOldVersions() {
			return this.oldVersions;
		}

	}

}
