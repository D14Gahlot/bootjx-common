package com.boot.jx.postman.service;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContextUtil;
import com.boot.jx.AppParam;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.async.ExecutorConfig;
import com.boot.jx.dict.FileFormat;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostManService;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.ExceptionReport;
import com.boot.jx.postman.model.ITemplates.TemplateDefaultEnum;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.postman.model.PostManFile;
import com.boot.jx.postman.model.PushMessage;
import com.boot.jx.postman.model.SMS;
import com.boot.jx.postman.model.SupportEmail;
import com.boot.jx.postman.model.WAMessage;
import com.boot.utils.ContextUtil;

/**
 * The Class PostManServiceImpl.
 */
@Component
public class PostManServiceImpl implements PostManService {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(PostManServiceImpl.class);

	/** The support service. */
	@Autowired
	private SupportService supportService;

	/** The email service. */
	@Autowired
	private EmailService emailService;

	/** The sms service. */
	@Autowired
	private SMService smsService;

	@Autowired
	private FBPushServiceImpl fbPushService;

	@Autowired
	private WhatsAppService whatsAppService;

	/** The slack service. */
	@Autowired
	private SlackService slackService;

	/** The file service. */
	@Autowired
	private FileService fileService;

	/** The app config. */
	@Autowired
	private AppConfig appConfig;

	public void setLang(String lang) {
		ContextUtil.map().put(PARAM_LANG, lang);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#sendEmail(com.amx.jax.postman.model.Email)
	 */
	@Override
	public ApiResponse<Email, Object> sendEmail(Email email) throws PostManException {
		return ApiResponse.build(emailService.sendEmail(supportService.filterMessageType(email)));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#sendEmailAsync(com.amx.jax.postman.model.
	 * Email)
	 */
	@Override

	public ApiResponse<Email, Object> sendEmailAsync(Email email) throws PostManException {
		return this.sendEmail(email);
	}

	@Override
	public ApiResponse<Email, Object> sendEmailBulk(List<Email> emailList) {
		for (Email email : emailList) {
			this.sendEmail(email);
		}
		return ApiResponse.buildList(emailList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#processTemplate(com.amx.jax.postman.model.
	 * File)
	 */
	public ApiResponse<PostManFile, Object> processTemplate(PostManFile file) {
		return ApiResponse.build(fileService.create(file));
	}

	/**
	 * Process template.
	 *
	 * @param template the template
	 * @param map      the map
	 * @param fileType the file type
	 * @return the file
	 */
	public CommonFile processTemplate(TemplateDefaultEnum template, Map<String, Object> map, FileFormat fileType) {
		PostManFile file = new PostManFile();
		file.setITemplate(template);
		file.setFileFormat(fileType);
		file.setModel(map);
		return this.processTemplate(file).getResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#sendSMS(com.amx.jax.postman.model.SMS)
	 */
	@Override
	public ApiResponse<SMS, Object> sendSMS(SMS sms) throws PostManException {

		if (AppParam.DEBUG_INFO.isEnabled()) {
			LOGGER.info("{}:START", "sendSMS");
		}
		this.smsService.sendSMS(sms);

		if (AppParam.DEBUG_INFO.isEnabled()) {
			LOGGER.info("{}:END", "sendSMS");
		}
		return ApiResponse.build(sms);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#notifySlack(com.amx.jax.postman.model.
	 * Notipy)
	 */
	@Override
	@Async(ExecutorConfig.EXECUTER_BRONZE)
	public ApiResponse<Notipy, Object> notifySlack(Notipy msg) throws PostManException {
		try {
			return ApiResponse.build(slackService.sendNotification(msg));
		} catch (Exception e) {
			throw new PostManException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#notifyException(com.amx.jax.postman.model.
	 * ExceptionReport)
	 */
	@Override
	public ApiResponse<ExceptionReport, Object> notifyException(ExceptionReport e) {
		return this.notifyException(appConfig.getAppName(), e.getTitle(), e.getException(), e);
	}

	/**
	 * Notify exception.
	 *
	 * @param appname   the appname
	 * @param title     the title
	 * @param exception the exception
	 * @param e         the e
	 * @return the exception report
	 */
	public ApiResponse<ExceptionReport, Object> notifyException(String appname, String title, String exception,
			ExceptionReport e) {
		return ApiResponse.build(slackService.sendException(appname, title, exception, e));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.amx.jax.postman.PostManService#notifyException(java.lang.String,
	 * java.lang.Exception)
	 */
	@Override
	public ApiResponse<ExceptionReport, Object> notifyException(String title, Exception exc) {
		return this.notifyException(new ExceptionReport(title, exc));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#sendSMSAsync(com.amx.jax.postman.model.
	 * SMS)
	 */
	@Override
	@Async(ExecutorConfig.EXECUTER_PLATINUM)
	public ApiResponse<SMS, Object> sendSMSAsync(SMS sms) throws PostManException {
		if (AppParam.DEBUG_INFO.isEnabled()) {
			LOGGER.info("{}:START", "sendSMSAsync");
		}
		return this.sendSMS(sms);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.amx.jax.postman.PostManService#sendEmailToSupprt(com.amx.jax.postman.
	 * model.SupportEmail)
	 */
	@Override
	@Async(ExecutorConfig.EXECUTER_BRONZE)
	public ApiResponse<Email, Object> sendEmailToSupprt(SupportEmail supportEmail) throws PostManException {
		Email email = this.sendEmail(supportService.createContactUsEmail(supportEmail)).getResult();
		Notipy msg = new Notipy();
		msg.setMessage(supportEmail.getSubject());
		msg.addLine("Tenant : " + AppContextUtil.getTenant());
		msg.addLine("VisitorName : " + supportEmail.getVisitorName());
		msg.addLine("VisitorEmail : " + supportEmail.getVisitorEmail());
		msg.addLine("VisitorPhone : " + supportEmail.getVisitorPhone());
		msg.addLine("VisitorMessage : " + supportEmail.getVisitorMessage());
		msg.setSubject(supportEmail.getSubject());
		msg.setIChannel(Notipy.ChannelType.INQUIRY);
		this.notifySlack(msg);
		return ApiResponse.build(email);
	}

	@Override
	public ApiResponse<MessageBox, Object> send(MessageBox messageBox) {

		LOGGER.debug("messageBox with Ex{} Sx{} Wx{} Tx{} Px{}", messageBox.getEmailBucket().size(),
				messageBox.getSmsBucket().size(), messageBox.getWaBucket().size(), messageBox.getTgBucket().size(),
				messageBox.getPushBucket().size());

		for (Email email : messageBox.getEmailBucket()) {
			this.sendEmail(email);
		}

		for (SMS sms : messageBox.getSmsBucket()) {
			this.sendSMS(sms);
		}

		for (WAMessage waMessage : messageBox.getWaBucket()) {
			whatsAppService.send(waMessage);
		}

		for (PushMessage pushMessage : messageBox.getPushBucket()) {
			fbPushService.sendDirect(pushMessage);
		}

		return ApiResponse.build(messageBox);
	}

}
