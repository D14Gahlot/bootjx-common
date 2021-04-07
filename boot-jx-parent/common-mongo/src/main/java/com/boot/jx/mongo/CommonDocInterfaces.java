package com.boot.jx.mongo;

import java.util.ArrayList;
import java.util.List;

import com.boot.utils.ArgUtil;

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
}
