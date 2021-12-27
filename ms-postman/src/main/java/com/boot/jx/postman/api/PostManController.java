package com.boot.jx.postman.api;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.AppParam;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.dict.Language;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PMConstants.PostManUrls;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.ExceptionReport;
import com.boot.jx.postman.model.ITemplates.ITemplate;
import com.boot.jx.postman.model.Message;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.postman.model.PostManFile;
import com.boot.jx.postman.model.SMS;
import com.boot.jx.postman.model.SupportEmail;
import com.boot.jx.postman.service.PostManServiceImpl;
import com.boot.utils.ArgUtil;
import com.boot.utils.JsonUtil;

/**
 * The Class PostManController.
 */
@RestController
public class PostManController {

    /** The Constant LOGGER. */
    private static final Logger LOGGER = LoggerFactory.getLogger(PostManController.class);

    /** The post man service. */
    @Autowired
    private PostManServiceImpl postManService;

    /** The request. */
    @Autowired
    private HttpServletRequest request;

    /** The post man config. */
    @Autowired
    private PostManConfig postManConfig;

    /**
     * Gets the lang.
     *
     * @return the lang
     */
    public String getLang(CommonFile file) {
	if (ArgUtil.isEmpty(file) || ArgUtil.isEmpty(file.template().getLang())) {
	    String langString = request.getParameter(PostManServiceImpl.PARAM_LANG);// localeResolver.resolveLocale(request).toString();
	    Language lang = ArgUtil.parseAsEnumT(langString, postManConfig.getTenantLang(), Language.class);
	    file.lang(lang);
	}
	return file.template().getLang();
    }

    @SuppressWarnings("rawtypes")
    private String getLang(Message sms) {
	if (ArgUtil.isEmpty(sms) || ArgUtil.isEmpty(sms.template().getLang())) {
	    String langString = request.getParameter(PostManServiceImpl.PARAM_LANG);// localeResolver.resolveLocale(request).toString();
	    Language lang = ArgUtil.parseAsEnumT(langString, postManConfig.getTenantLang(), Language.class);
	    sms.lang(lang);
	}
	return sms.template().getLang();
    }

    /**
     * Process template.
     *
     * @param template the template
     * @param data     the data
     * @param fileName the file name
     * @param fileType the file type
     * @return the file
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = PostManUrls.PROCESS_TEMPLATE, method = RequestMethod.POST)
    public ApiResponse<PostManFile, Object> processTemplate(@RequestParam ITemplate template,
	    @RequestParam(required = false) String data, @RequestParam(required = false) String fileName,
	    @RequestParam(required = false) FileFormat fileType) {

	PostManFile file = new PostManFile();
	getLang(file);

	file.setITemplate(template);
	file.setFileFormat(fileType);
	file.setModel(JsonUtil.fromJson(data, Map.class));
	return postManService.processTemplate(file);

    }

    /**
     * Process template file.
     *
     * @param file the file
     * @return the file
     */
    @RequestMapping(value = { PostManUrls.PROCESS_TEMPLATE_FILE, PostManUrls.PROCESS_TEMPLATE_FILE_LOCAL },
	    method = RequestMethod.POST)
    public CommonFile processTemplateFile(@RequestBody PostManFile file) {
	getLang(file);
	return postManService.processTemplate(file).getResult();
    }

    /**
     * Send SMS.
     *
     * @param sms   the sms
     * @param async the async
     * @return the sms
     * @throws PostManException the post man exception
     */
    @RequestMapping(value = PostManUrls.SEND_SMS, method = RequestMethod.POST)
    public ApiResponse<SMS, Object> sendSMS(@RequestBody SMS sms,
	    @RequestParam(required = false, defaultValue = "false") Boolean async) throws PostManException {

	if (AppParam.DEBUG_INFO.isEnabled()) {
	    LOGGER.info("{}:START", "sendSMS");
	}

	getLang(sms);

	if (async == true) {
	    return postManService.sendSMSAsync(sms);
	} else {
	    return postManService.sendSMS(sms);
	}
    }

    @RequestMapping(value = PostManUrls.SEND_SMS, method = RequestMethod.GET)
    public ApiResponse<SMS, Object> sendSMSGet(@RequestParam String to, @RequestParam String message)
	    throws PostManException {
	SMS sms = new SMS();
	sms.addTo(to);
	sms.setMessage(message);
	return postManService.sendSMSAsync(sms);
    }

    @RequestMapping(value = PostManUrls.SEND_EMAIL, method = RequestMethod.POST)
    public ApiResponse<Email, Object> sendEmail(@RequestBody Email email,
	    @RequestParam(required = false, defaultValue = "false") Boolean async) throws PostManException {

	getLang(email);

	if (LOGGER.isDebugEnabled()) {
	    LOGGER.debug("SEND_EMAIL   {} ", JsonUtil.toJson(email));
	}

	if (async == true) {
	    return postManService.sendEmailAsync(email);
	} else {
	    return postManService.sendEmail(email);
	}
    }

    @RequestMapping(value = PostManUrls.SEND_EMAIL_BULK, method = RequestMethod.POST)
    public ApiResponse<Email, Object> sendEmailBulk(@RequestBody List<Email> emailList) throws PostManException {
	return postManService.sendEmailBulk(emailList);
    }

    @RequestMapping(value = PostManUrls.SEND_MESSAGE_BOX, method = RequestMethod.POST)
    public ApiResponse<MessageBox, Object> send(@RequestBody MessageBox messageBox) throws PostManException {
	return postManService.send(messageBox);
    }

    /**
     * Send email.
     *
     * @param email the email
     * @return the email
     * @throws PostManException the post man exception
     */
    @RequestMapping(value = PostManUrls.SEND_EMAIL_SUPPORT, method = RequestMethod.POST)
    public ApiResponse<Email, Object> sendEmail(@RequestBody SupportEmail email) throws PostManException {
	getLang(email);
	return postManService.sendEmailToSupprt(email);
    }

    /**
     * Notify slack.
     *
     * @param msg the msg
     * @return the message
     * @throws PostManException the post man exception
     */
    @RequestMapping(value = PostManUrls.NOTIFY_SLACK, method = RequestMethod.POST)
    public ApiResponse<Notipy, Object> notifySlack(@RequestBody Notipy msg) throws PostManException {
	return postManService.notifySlack(msg);
    }

    /**
     * Notify slack.
     *
     * @param eMsg      the e msg
     * @param title     the title
     * @param appname   the appname
     * @param exception the exception
     * @return the exception
     * @throws PostManException the post man exception
     */
    @RequestMapping(value = PostManUrls.NOTIFY_SLACK_EXCEP, method = RequestMethod.POST)
    public ApiResponse<ExceptionReport, Object> notifySlack(@RequestBody Exception eMsg,
	    @RequestParam(required = false) String title, @RequestParam(required = false) String appname,
	    @RequestParam(required = false) String exception) throws PostManException {
	return postManService.notifyException(appname, title, exception, new ExceptionReport(eMsg));
    }

    @RequestMapping(value = PostManUrls.NOTIFY_SLACK_EXCEP_REPORT, method = RequestMethod.POST)
    public ApiResponse<ExceptionReport, Object> notifySlackReport(@RequestBody ExceptionReport eMsg,
	    @RequestParam(required = false) String title, @RequestParam(required = false) String appname,
	    @RequestParam(required = false) String exception) throws PostManException {
	if (eMsg.getEmail() != null) {
	    postManService.sendEmail(eMsg.getEmail());
	}
	return postManService.notifyException(appname, title, exception, eMsg);
    }

}
