package com.boot.jx.mongo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.boot.utils.ArgUtil;
import com.boot.utils.EntityDtoUtil;

public class CommonDocInterfaces {

	public static interface Patchable<T> {

		public T patch();
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
