package com.boot.utils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.aop.framework.AopProxyUtils;

public class ClazzUtil {

	private final static Map<String, Class<?>> cache = new HashMap<String, Class<?>>();

	@SuppressWarnings("unchecked")
	public static <T> Class<T> fromName(String clzName) {
		Class<?> clz = cache.get(clzName);
		if (clz != null)
			return (Class<T>) clz;
		try {
			clz = Class.forName(clzName);
			cache.put(clzName, clz);
		} catch (Exception e) {
		}
		return (Class<T>) clz;
	}

	public static Pattern getGenericTypePattern(Class<?> clazz) {
		return Pattern.compile(
				"^" + clazz.getName() + "<(.*)>$");
	}

	public static String getClassName(Object target) {
		return target.getClass().getName().split("\\$")[0];
	}

	public static String getSimpleClassName(Object target) {
		return target.getClass().getSimpleName().split("\\$")[0];
	}

	public static <A extends Annotation> A getAnnotation(Class<?> clazz, Class<A> annotationClass) {
		final A annotation = clazz.getAnnotation(annotationClass);
		if (annotation == null && (clazz.isSynthetic())) { // OK, this is probably proxy
			return getAnnotation(clazz.getSuperclass(), annotationClass);
		} else {
			return annotation;
		}
	}

	public static <A extends Annotation> A getAnnotationFromBean(Object candidate, Class<A> annotationClass) {
		Class<?> c = AopProxyUtils.ultimateTargetClass(candidate);
		return getAnnotation(c, annotationClass);
	}
}
