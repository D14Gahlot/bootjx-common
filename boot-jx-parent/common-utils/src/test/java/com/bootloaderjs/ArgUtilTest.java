package com.bootloaderjs;

import java.text.ParseException;
import java.util.regex.Pattern;

import com.boot.utils.ArgUtil;

public class ArgUtilTest { // Noncompliant

	public static final Pattern pattern = Pattern.compile("^com.amx.jax.logger.client.AuditFilter<(.*)>$");

	public static void main(String[] args) throws ParseException {
		checkAssert("=====true", ArgUtil.isEqual(null), false);
		checkAssert("=====true", ArgUtil.isEqual(null, null), true);
		checkAssert("=====false", ArgUtil.isEqual("", null, null), false);
		checkAssert("=====false", ArgUtil.isEqual(null, "", null), true);
		checkAssert("=====false", ArgUtil.isEqual(null, "x", null), true);
	}

	public static void main2(String[] args) throws ParseException {
		checkAssert("=====true", ArgUtil.isNone(0), true);
		checkAssert("=====false", ArgUtil.isNone(0L), true);
		checkAssert("=====null", ArgUtil.isNone(null), true);
		checkAssert("=====null", ArgUtil.isNone(""), true);
		checkAssert("=====null", ArgUtil.isNone(3), false);
		checkAssert("=====null", ArgUtil.isNone("3"), false);
		checkAssert("=====null", ArgUtil.isNone("0"), false);
		checkAssert("=====null", ArgUtil.isNone(false), true);
		checkAssert("=====null", ArgUtil.isNone(Boolean.FALSE), true);
	}

	public static void checkAssert(String name, Object a, Object b) throws ParseException {
		if (ArgUtil.areEqual(a, b)) {
			System.out.println(name + " : PASS");
		} else {
			System.out.println(name + " : FAIL");
		}
	}

}
