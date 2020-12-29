package com.boot.jx.postman.api;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.LocaleResolver;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.api.AmxApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.dict.Tenant;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManUrls;
import com.boot.jx.postman.client.GeoLocationClient;
import com.boot.jx.postman.client.PostManClient;
import com.boot.jx.postman.client.PushNotifyClient;
import com.boot.jx.postman.model.DefaultMessage;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.ExceptionReport;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.GeoLocation;
import com.boot.jx.postman.model.ITemplates;
import com.boot.jx.postman.model.ITemplates.ITemplate;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.postman.model.TemplatesMX;
import com.boot.jx.postman.service.PostManServiceImpl;
import com.boot.jx.tmpl.TemplateModelCache;
import com.boot.jx.tmpl.TemplateService;
import com.boot.utils.ArgUtil;
import com.boot.utils.IoUtils;
import com.boot.utils.JsonPath;
import com.boot.utils.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ibm.icu.text.Transliterator;

/**
 * The Class PostManControllerTest.
 */
@Controller
@RequestMapping("test/")
public class PostManControllerTest {

	/** The response. */
	@Autowired
	private HttpServletResponse response;

	/** The postman url. */
	@Value("${jax.postman.url}")
	private String postmanUrl;

	/** The post man client. */
	@Autowired
	PostManClient postManClient;

	/** The fb push client. */
	@Autowired
	PushNotifyClient pushNotifyClient;

	/** The post man service impl. */
	@Autowired
	PostManServiceImpl postManServiceImpl;

	/** The context. */
	@Autowired
	private ApplicationContext context;

	/** The request. */
	@Autowired
	private HttpServletRequest request;

	/** The message source. */
	@Autowired
	private MessageSource messageSource;

	/** The locale resolver. */
	@Autowired
	private LocaleResolver localeResolver;

	/** The geo location client. */
	@Autowired
	GeoLocationClient geoLocationClient;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PostManControllerTest.class);

	/**
	 * Notify slack.
	 *
	 * @return the message
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "exception", method = RequestMethod.GET)
	public Message notifySlack() {

		try {
			throw new Exception("Some Error");
		} catch (Exception e) {
			postManClient.notifyException("My Error", new ExceptionReport(e));
		}
		return null;
	}

	/**
	 * Location.
	 *
	 * @return the geo location
	 * @throws PostManException the post man exception
	 */
	@ResponseBody
	@RequestMapping(value = PostManUrls.GEO_LOC, method = RequestMethod.GET)
	public GeoLocation location() throws PostManException {
		String remoteAddr = null;
		if (request != null) {
			remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
		}
		return geoLocationClient.getLocation(remoteAddr);
	}

	/**
	 * Prints the.
	 *
	 * @param tnt the tnt
	 * @return the message
	 * @throws PostManException the post man exception
	 */
	@ResponseBody
	@RequestMapping(value = PostManUrls.PROCESS_TEMPLATE + "/print", method = RequestMethod.GET)
	public Message<?> print(@RequestParam Tenant tnt) throws PostManException {

		Message<?> msg = new DefaultMessage();
		final String ENG_TO_DEV = "Latin-Devanagari";
		Transliterator toDevnagiri = Transliterator.getInstance(ENG_TO_DEV);
		String devnagiri = toDevnagiri.transliterate("lalit");
		msg.setMessage(devnagiri);
		return msg;
	}

	/**
	 * Fb push.
	 *
	 * @param msg the msg
	 * @return the push message
	 * @throws PostManException     the post man exception
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException   the execution exception
	 */
	@ResponseBody
	@RequestMapping(value = PostManUrls.NOTIFY_PUSH, method = RequestMethod.POST)
	public PushMessage fbPush(@RequestBody PushMessage msg)
			throws PostManException, InterruptedException, ExecutionException {
		pushNotifyClient.sendDirect(msg);
		return msg;
	}

	/**
	 * Fb push.
	 *
	 * @param token the token
	 * @param topic the topic
	 * @return the string
	 * @throws PostManException     the post man exception
	 * @throws InterruptedException the interrupted exception
	 * @throws ExecutionException   the execution exception
	 */
	@RequestMapping(value = PostManUrls.NOTIFY_PUSH_SUBSCRIBE, method = RequestMethod.POST)
	@ResponseBody
	public String fbPush(@RequestParam String token, @PathVariable String topic)
			throws PostManException, InterruptedException, ExecutionException {
		pushNotifyClient.subscribe(token, topic);
		return topic;
	}

	/**
	 * Process template.
	 *
	 * @param template the template
	 * @param ext      the ext
	 * @param email    the email
	 * @param data     the data
	 * @param tnt      the tnt
	 * @param lib      the lib
	 * @return the string
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws PostManException the post man exception
	 */
	@ResponseBody
	@RequestMapping(value = PostManUrls.PROCESS_TEMPLATE + "/{template}.{ext}", method = RequestMethod.GET)
	public String processTemplate(@PathVariable("template") TemplatesMX template, @PathVariable("ext") String ext,
			@RequestParam(name = "email", required = false) String email,
			@RequestBody(required = false) Map<String, Object> data, @RequestParam(required = false) Tenant tnt,
			@RequestParam(required = false) File.PDFConverter lib,
			@RequestParam(required = false) TemplatesMX attachment)
			throws IOException, /* DocumentException, */ PostManException {

		Map<String, Object> map = readJsonWithObjectMapper("templates/dummy/" + template.getSampleJSON());
		map.put("traceId", AppContextUtil.getTraceId());

		new JsonPath("/data/remittanceApplList/[0]/firstName").save(map, AppContextUtil.getTraceId());

		// LOGGER.info("====={}", messageSource.getMessage("sender.details", null,
		// localeResolver.resolveLocale(request)));

		postManClient.setLang(localeResolver.resolveLocale(request).toString());

		File file = new File();
		file.setModel(map);
		file.setITemplate(template);
		file.setConverter(lib);

		if ("pdf".equals(ext)) {
			file.setType(File.Type.PDF);
			file = postManClient.processTemplate(file).getResult();
			// file = postManClient.processTemplate(template, map, File.Type.PDF);
			file.create(response, false);
			return null;
		} else if ("json".equals(ext)) {
			file.setType(File.Type.JSON);
			file = postManClient.processTemplate(file).getResult();
			return file.getContent();
		} else if ("html".equals(ext)) {
			AmxApiResponse<File, Object> resp = postManClient.processTemplate(file);
			file = resp.getResult();
			if (email != null) {
				Email eml = new Email();
				// eml.setSubject("Email Template : " + template);
				eml.setFrom("amxjax@gmail.com");
				eml.addTo(email);
				eml.setITemplate(template);
				eml.setHtml(true);
				eml.setModel(map);
				// this.readImageWithObjectMapper(null);

				File file2 = new File();

				if (ArgUtil.isEmpty(attachment)) {
					file2.setITemplate(template);
					file2.setModel(map);
				} else {
					file2.setITemplate(attachment);
					Map<String, Object> map2 = readJsonWithObjectMapper(
							"templates/dummy/" + attachment.getSampleJSON());
					file2.setModel(map2);
					file2.setType(File.Type.PDF);
					file2.setConverter(lib);
					eml.addFile(file2);
				}

				postManClient.sendEmailAsync(eml);
			}
			return file.getContent();
		} else {
			return JsonUtil.toJson(map);
		}

	}

	@Autowired
	TemplateService templateService;

	@Autowired
	private TemplateModelCache templateModelCache;

	@Autowired
	private AppConfig appConfig;

	@ResponseBody
	@RequestMapping(value = PostManUrls.PROCESS_TEMPLATE + "/file/{template}.{contactType}", method = RequestMethod.GET)
	public String processTemplate(@PathVariable("contactType") ContactType contactType,
			@PathVariable("template") String template, @RequestParam(defaultValue = "HTML") File.Type type)
			throws IOException {
		ITemplate temp = ITemplates.getTemplate(template);

		Map<String, Object> map = null;

		if (!appConfig.isProdMode() && appConfig.isCache()) {
			// map = templateModelCache.get(temp.getSampleJSON());
		}

		if (map == null) {
			map = readJsonWithObjectMapper("templates/dummy/" + temp.getSampleJSON());
		}

		postManClient.setLang(localeResolver.resolveLocale(request).toString());

		File file = new File();
		file.setModel(map);
		file.setITemplate(temp);
		// file.setConverter(lib);
		if (File.Type.PDF.equals(type)) {
			file.setType(File.Type.PDF);
			file = postManClient.processTemplate(file).getResult();
			// file = postManClient.processTemplate(template, map, File.Type.PDF);
			file.create(response, false);
			return file.getContent();
		}
		return templateService.process(file, contactType).getContent();
	}

	/**
	 * Read json with object mapper.
	 *
	 * @param jsonFile the json file
	 * @return the map
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@SuppressWarnings("unchecked")
	public Map<String, Object> readJsonWithObjectMapper(String jsonFile) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		// return objectMapper.readValue(new File(jsonFile), Map.class);
		return objectMapper.readValue(context.getResource("classpath:" + jsonFile).getInputStream(), Map.class);
	}

	/**
	 * Read image with object mapper.
	 *
	 * @param jsonFile the json file
	 * @return the input stream source
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public InputStreamSource readImageWithObjectMapper(String jsonFile) throws IOException {
		// return objectMapper.readValue(new File(jsonFile), Map.class);
		InputStreamSource imageSource = new ByteArrayResource(
				IoUtils.toByteArray(context.getResource("classpath:images/logo.png").getInputStream()));
		return imageSource;

	}
}
