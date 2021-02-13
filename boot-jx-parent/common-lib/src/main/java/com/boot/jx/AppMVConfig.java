package com.boot.jx;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.boot.jx.filter.AppRequestInterceptor;
import com.boot.jx.filter.StringToEnumIgnoringCaseConverterFactory;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AppMVConfig extends WebMvcConfigurerAdapter {

	@Autowired
	private AppRequestInterceptor appRequestInterceptor;
	/** Pattern relative to templates base used to match text templates. */
	public static final String TEXT_TEMPLATES_RESOLVE_PATTERN = "text/*";
	/** Pattern relative to templates base used to match JSON templates. */
	public static final String JSON_TEMPLATES_RESOLVE_PATTERN = "json/*";
	/** Pattern relative to templates base used to match JSON templates. */
	public static final String JS_TEMPLATES_RESOLVE_PATTERN = "js/*";
	/** Pattern relative to templates base used to match XML templates. */
	public static final String XML_TEMPLATES_RESOLVE_PATTERN = "xml/*";
	public static final String TEMPLATES_BASE = "classpath:/templates/";

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(appRequestInterceptor);
	}

	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverterFactory(new StringToEnumIgnoringCaseConverterFactory());
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
		registry.addResourceHandler("/swagger-ui.html").addResourceLocations(getStaticLocations());
	}

	private String[] getStaticLocations() {

		String[] result = new String[5];
		result[0] = "/";
		result[1] = "classpath:/META-INF/resources/";
		result[2] = "classpath:/resources/";
		result[3] = "classpath:/static/";
		result[4] = "classpath:/public/";

		return result;
	}
}
