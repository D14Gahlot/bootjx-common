package com.amx.tests;

import com.boot.utils.Constants;
import com.boot.utils.StringUtils;

public class PostmanBasicTest {

	public static void main(String[] arg) {
		System.out.println(new PostmanBasicTest().slugifyFileName("sds/dsd sas.abc.png"));
	}

	public String slugifyFileName(String fileName) {
		String fileExtension = Constants.BLANK;
		String fileBaseName = String.format("%s", fileName);
		int index = fileBaseName.lastIndexOf('.');

		if (index > 0) {
			fileExtension = "." + fileBaseName.substring(index + 1);
			fileBaseName = fileBaseName.substring(0, index);
		}
		String fileNameNow = StringUtils.slugify(String.format("%s", fileBaseName)) + fileExtension;
		return fileNameNow;
	}
}
