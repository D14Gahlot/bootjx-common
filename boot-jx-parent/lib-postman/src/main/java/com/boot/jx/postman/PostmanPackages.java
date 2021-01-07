package com.boot.jx.postman;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.File;

public class PostmanPackages {

	public static interface ICommonTmplPackage {
		public File process(File file, ContactType contactType);
	}
}
