package com.boot.jx.postman.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.async.ExecutorConfig;
import com.boot.jx.dict.ContactType;
import com.boot.jx.logger.AuditEvent;
import com.boot.jx.logger.AuditService;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.audit.PMGaugeEvent;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.File;
import com.boot.jx.postman.model.Message.Status;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.tmpl.TemplateUtils;
import com.boot.jx.tunnel.TunnelMessage;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;
import com.boot.utils.CryptoUtil;
import com.boot.utils.TimeUtils;
import com.boot.utils.Utils;

/**
 * The Class EmailService.
 */
@TenantScoped
@Component
public class EmailService {

	static final String FAILED_EMAIL_QUEUE = "FAILED_EMAIL_QUEUE";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

	public static final Pattern pattern = Pattern.compile("^(.*)<(.*)>$");
	public static final Pattern PATTERN_CID = Pattern.compile("src=\"cid:(.*?)\"");
	public static final Pattern PATTERN_SUBJECT = Pattern.compile("<title>(.*?)</title>");

	/** The template utils. */
	@Autowired
	private TemplateUtils templateUtils;

	/** The file service. */
	@Autowired
	private FileService fileService;

	/** The mail sender. */
	private JavaMailSender mailSender;

	/** The default mail sender. */
	@Autowired
	private JavaMailSender defaultMailSender;

	/** The post man config. */
	@Autowired
	private PostManConfig postManConfig;

	@Autowired
	private ContactCleanerService contactService;

	/** The slack service. */
	@Autowired
	private SlackService slackService;

	/** The audit service. */
	@Autowired
	private AuditService auditService;

	@Autowired
	private AppConfig appConfig;

	@Autowired(required = false)
	RedissonClient redisson;

	public static final int RESEND_INTERVAL = 1 * 60 * 1000;

	/**
	 * Gets the mail sender.
	 *
	 * @return the mail sender
	 */
	public JavaMailSender getMailSender() {
		if (mailSender == null) {
			if (postManConfig.getTenant() != null) {
				LOGGER.info("Using {} mailSender", postManConfig.getTenant());
				JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
				javaMailSender.setHost(postManConfig.getMailHost());
				javaMailSender.setPort(postManConfig.getMailPort());
				javaMailSender.setUsername(postManConfig.getMailUsername());
				javaMailSender.setPassword(postManConfig.getMailPassword());
				Properties mailProp = new Properties();
				mailProp.put("mail.smtp.auth", postManConfig.isMailSmtpAuth());
				mailProp.put("mail.smtp.starttls.enable", postManConfig.isMailSmtpTls());
				javaMailSender.setJavaMailProperties(mailProp);
				javaMailSender.setProtocol(postManConfig.getMailProtocol());
				javaMailSender.setDefaultEncoding(postManConfig.getMailDefaultEncoding());
				this.mailSender = javaMailSender;
			} else {
				LOGGER.info("Using Default mailSender");
				this.mailSender = defaultMailSender;
				// this.mailFrom = postManConfig.getMailDefaultSender();
			}
		}
		return this.mailSender;
	}

	/**
	 * Send email.
	 *
	 * @param email the email
	 * @return the email
	 * @throws PostManException the post man exception
	 */
	@Async(ExecutorConfig.EXECUTER_GOLD)
	public Email sendEmail(Email email) throws PostManException {

		PMGaugeEvent pMGaugeEvent = new PMGaugeEvent(PMGaugeEvent.Type.SEND_EMAIL);
		String to = null;
		Email emailClone = null;
		try {
			emailClone = email.clone();
		} catch (CloneNotSupportedException e1) {
			LOGGER.error("Clonning exception {} Email to {}", email.getTemplate(), Utils.commaConcat(email.getTo()));
		}
		try {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Sending {} Email to {}", email.getTemplate(), Utils.commaConcat(email.getTo()));
			}

			to = email.getTo() != null ? email.getTo().get(0) : null;

			if (ArgUtil.isEmpty(to)) {
				email.setStatus(Status.NOT_SENT);
				auditService.log(pMGaugeEvent.set(AuditEvent.Result.REJECTED).set(email));
			} else if (contactService.isEmailBlackListed(to)) {
				email.setStatus(Status.BLOCKED);
				auditService.log(pMGaugeEvent.set(AuditEvent.Result.REJECTED).set(email));
			} else {
				if (email.getTemplate() != null) {
					File file = new File();
					file.setTemplate(email.getTemplate());
					file.setModel(email.getModel());
					file.setLang(email.getLang());

					email.setMessage(fileService.create(file, ContactType.EMAIL).getContent());

					if (ArgUtil.isEmptyString(email.getSubject())) {
						email.setSubject(file.getTitle());
					}
				}

				if (email.getFiles() != null && email.getFiles().size() > 0) {
					for (File file : email.getFiles()) {
						if (file.getLang() == null) {
							file.setLang(email.getLang());
						}
						fileService.create(file);
					}
				}
				this.send(email);
				email.setStatus(Status.SENT);
				auditService.log(pMGaugeEvent.set(AuditEvent.Result.DONE).set(email));
			}

		} catch (Throwable e) {
			auditService.excep(pMGaugeEvent.set(AuditEvent.Result.ERROR).set(email), LOGGER, e);
			// slackService.sendException(to, e);
		}

		if (!ArgUtil.isEmpty(emailClone) && !Status.SENT.equals(email.getStatus())
				&& !Status.NOT_SENT.equals(email.getStatus())
				&& !Status.BLOCKED.equals(email.getStatus()) && redisson != null) {
			AppContext context = AppContextUtil.getContext();
			TunnelMessage<Email> tunnelMessage = new TunnelMessage<Email>(emailClone, context);
			RQueue<TunnelMessage<Email>> emailQueue = redisson
					.getQueue(FAILED_EMAIL_QUEUE + "_" +
							TimeUtils.getRotationNumber(RESEND_INTERVAL, 0x1)
							+ "_" + postManConfig.getEmailRetryPush());
			emailQueue.add(tunnelMessage);
		}

		return email;
	}

	/**
	 * Send.
	 *
	 * @param eParams the e params
	 * @return the email
	 * @throws MessagingException the messaging exception
	 * @throws IOException        Signals that an I/O exception has occurred.
	 */
	private Email send(Email email) throws MessagingException, IOException {
		String tos = null;

		if (email.isHtml()) {
			tos = String.join(",", sendHtmlMail(email));
		} else {
			tos = String.join(",", sendPlainTextMail(email));
		}

		if (!appConfig.isProdMode() && !ArgUtil.isEmpty(email.getITemplate())
				&& !ArgUtil.isEmpty(email.getITemplate().getChannel())) {
			Notipy msg = new Notipy();
			msg.setSubject(email.getSubject());
			msg.setAuthor(String.format("%s = %s", email.getTo().get(0), tos));
			msg.setMessage(String.format("%s", email.getModel().toString()));
			msg.setIChannel(email.getITemplate().getChannel());
			msg.addField("ENV", appConfig.getAppEnv());
			msg.addField("TRACE-ID", AppContextUtil.getTraceId());
			msg.addField("TEMPLATE", email.getITemplate().toString());
			msg.setColor("#" + CryptoUtil.toHex(6, email.getITemplate().toString()));
			slackService.sendNotification(msg);
		}

		return email;
	}

	private InternetAddress getInternetAddress(String email) throws UnsupportedEncodingException {
		String fromEmail = null;
		String fromTitle = null;

		Matcher matcher = pattern.matcher(email);
		if (matcher.find()) {
			fromEmail = matcher.group(2);
			fromTitle = matcher.group(1);
		} else {
			fromEmail = email;
			fromTitle = email;
		}
		return new InternetAddress(fromEmail, fromTitle);
	}

	/**
	 * Send html mail.
	 *
	 * @param eParams the e params
	 * @return
	 * @throws MessagingException the messaging exception
	 * @throws IOException        Signals that an I/O exception has occurred.
	 */
	private String[] sendHtmlMail(Email eParams) throws MessagingException, IOException {

		boolean isHtml = true;

		MimeMessage message = getMailSender().createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

		String[] tos = contactService.getEmail(eParams.getTo());

		if (ArgUtil.isEmpty(tos) || tos.length == 0) {
			return tos;
		}

		helper.setTo(tos);
		// helper.setTo(emailsTo.toArray(new String[emailsTo.size()]));
		// helper.setReplyTo(eParams.getFrom());

		if (eParams.getFrom() == null || Constants.DEFAULT_STRING.equals(eParams.getFrom())) {
			eParams.setFrom(postManConfig.getMailFrom());
		}

		InternetAddress fromInternetAddress = getInternetAddress(eParams.getFrom());

		if (eParams.getReplyTo() == null || Constants.DEFAULT_STRING.equals(eParams.getReplyTo())) {
			eParams.setReplyTo(eParams.getFrom());
		}

		helper.setFrom(fromInternetAddress);
		helper.setReplyTo(eParams.getReplyTo());

		String messageStr = eParams.getMessage();

		String subject = eParams.getSubject();
		if (ArgUtil.isEmptyString(subject)) {
			Matcher mtitle = PATTERN_SUBJECT.matcher(messageStr);

			if (mtitle.find()) {
				subject = mtitle.group(1);
			}
			subject = ArgUtil.isEmptyString(subject) ? "No Subject" : subject;
		}
		helper.setSubject(subject);
		helper.setText(eParams.getMessage(), isHtml);

		if (eParams.getCc().size() > 0) {
			helper.setCc(eParams.getCc().toArray(new String[eParams.getCc().size()]));
		}

		Matcher m = PATTERN_CID.matcher(messageStr);
		while (m.find()) {
			String contentId = m.group(1);
			helper.addInline(contentId, templateUtils.readAsResource(contentId));
		}

		if (eParams.getFiles() != null && eParams.getFiles().size() > 0) {
			for (File file : eParams.getFiles()) {
				helper.addAttachment(file.getName(), fileService.toDataSource(file));
			}
		}

		getMailSender().send(message);

		return tos;
	}

	/**
	 * Send plain text mail.
	 *
	 * @param eParams the e params
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	private String[] sendPlainTextMail(Email eParams) throws UnsupportedEncodingException {
		SimpleMailMessage mailMessage = new SimpleMailMessage();

		String[] tos = contactService.getEmail(eParams.getTo());
		// InternetAddress fromInternetAddress = getInternetAddress(eParams.getFrom());

		if (ArgUtil.isEmpty(tos) || tos.length == 0) {
			return tos;
		}

		// eParams.getTo().toArray(new String[eParams.getTo().size()]);
		mailMessage.setTo(tos);
		mailMessage.setReplyTo(eParams.getFrom());
		mailMessage.setFrom(eParams.getFrom());
		mailMessage.setSubject(eParams.getSubject());
		mailMessage.setText(eParams.getMessage());

		if (eParams.getCc().size() > 0) {
			mailMessage.setCc(eParams.getCc().toArray(new String[eParams.getCc().size()]));
		}

		getMailSender().send(mailMessage);
		return tos;
	}

}
