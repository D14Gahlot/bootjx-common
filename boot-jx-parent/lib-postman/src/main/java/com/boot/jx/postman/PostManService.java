package com.boot.jx.postman;

import java.util.List;

import com.boot.jx.api.AmxApiResponse;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.ExceptionReport;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.MessageBox;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.postman.model.SMS;
import com.boot.jx.postman.model.SupportEmail;

public interface PostManService {

	public static final String PARAM_LANG = "lang";
	public static final String PARAM_ASYNC = "async";

	public AmxApiResponse<Email, Object> sendEmail(Email email) throws PostManException;

	public AmxApiResponse<Email, Object> sendEmailToSupprt(SupportEmail email) throws PostManException;

	public AmxApiResponse<SMS, Object> sendSMS(SMS sms) throws PostManException;

	public AmxApiResponse<Notipy, Object> notifySlack(Notipy msg) throws PostManException;

	public AmxApiResponse<ExceptionReport, Object> notifyException(ExceptionReport e);

	public AmxApiResponse<ExceptionReport, Object> notifyException(String title, Exception exc);

	public AmxApiResponse<Email, Object> sendEmailAsync(Email email) throws PostManException;

	public AmxApiResponse<SMS, Object> sendSMSAsync(SMS sms) throws PostManException;

	public AmxApiResponse<File, Object> processTemplate(File file) throws PostManException;

	public AmxApiResponse<Email, Object> sendEmailBulk(List<Email> emailList);

	public AmxApiResponse<MessageBox, Object> send(MessageBox messageBox);

}
