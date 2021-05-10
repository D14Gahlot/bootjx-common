package com.boot.jx.postman;

import com.boot.jx.dict.ContactType;
import com.boot.jx.postman.model.PostManFile;

public class PostmanPackages {

	public static interface ICommonTmplPackage {
		public PostManFile process(PostManFile file, ContactType contactType);
	}
}
