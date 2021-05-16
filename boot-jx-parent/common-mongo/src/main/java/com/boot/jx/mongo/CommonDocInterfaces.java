package com.boot.jx.mongo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;

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

	public static interface IDocument {
	}

	public static abstract class ADocumentDTO<T extends ADocumentDTO<T>> implements IDocument, Serializable {
		private static final long serialVersionUID = 5315474201666739759L;

		@SuppressWarnings("unchecked")
		public T importFrom(IDocument entity) {

			if (ArgUtil.is(entity)) {
				EntityDtoUtil.entityToDto(entity, this);
			}

			return (T) this;
		}

		public List<T> importFrom(List<? extends IDocument> entityList) {
			List<T> list = new ArrayList<T>();
			for (IDocument entity : entityList) {
				T dto = this.newInstance().importFrom(entity);
				list.add(dto);
			}
			return list;
		}

		protected abstract ADocumentDTO<T> newInstance();
	}

}
