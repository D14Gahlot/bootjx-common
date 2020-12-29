package com.boot.jx.postman.service;

import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.AppConfig;
import com.boot.jx.AppContext;
import com.boot.jx.AppContextUtil;
import com.boot.jx.async.ExecutorConfig;
import com.boot.jx.postman.PostManConfig;
import com.boot.jx.postman.model.ExceptionReport;
import com.boot.jx.postman.model.Notipy;
import com.boot.jx.postman.model.Notipy.ChannelType;
import com.boot.jx.postman.model.Notipy.Workspace;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

/**
 * The Class SlackService.
 */
@Component
public class SlackService {

	/** The logger. */
	private Logger LOGGER = LoggerFactory.getLogger(getClass());

	/** The exception channel code. */
	@Value("${slack.exception.channel}")
	private String exceptionChannelCode;

	/** The app config. */
	@Autowired
	AppConfig appConfig;

	/** The post man config. */
	@Autowired
	PostManConfig postManConfig;

	/** The rest service. */
	@Autowired
	RestService restService;

	/**
	 * Send.
	 *
	 * @param message the message
	 * @param channel the channel
	 * @return the string
	 */
	private String send(Map<String, Object> message, ChannelType channel) {
		return restService.ajax("https://slack.com/api/chat.postMessage")
				.header("Authorization",
						(channel.getWorkspace() == Workspace.ALMEX)
								? "Bearer xoxp-253198866083-252757085313-290617557616-ba4ac4b1a235baae2fe2ac930213d171"
								: "Bearer xoxp-359453932565-359453932869-364186637029-e1548faa24b4292ae44026ea32985586")
				.postJson(message).asString();
	}

	/**
	 * Send notification.
	 *
	 * @param msg the msg
	 * @return the notipy
	 */
	public Notipy sendNotification(Notipy msg) {

		Map<String, Object> message = new HashMap<String, Object>();
		// message.put("text", msg.getSubject());
		ChannelType channelType = ArgUtil.parseAsEnumT(msg.getChannel(), ChannelType.class);
		message.put("channel", postManConfig.getChannelCode(channelType));
		// message.put("text", msg.getSubject());

		Map<String, Object> attachment = new HashMap<String, Object>();
		attachment.put("title", msg.getSubject());
		StringBuilder tracetext = new StringBuilder();
		tracetext.append(msg.getMessage());
		if (msg.getLines().size() > 0 && msg.getLines().get(0).toString().length() > 0) {
			for (String line : msg.getLines()) {
				tracetext.append("\n" + line);
			}
		}
		attachment.put("text", tracetext.toString());
		attachment.put("author_name", ArgUtil.parseAsString(msg.getAuthor()));
		attachment.put("color", msg.getColor());
		attachment.put("fields", msg.getFields());
		message.put("attachments", Collections.singletonList(attachment));

		String response = send(message, ArgUtil.parseAsEnumT(msg.getChannel(), ChannelType.class));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.info("Slack Sent", response);
		}
		return msg;
	}

	/**
	 * Send exception.
	 *
	 * @param title the title
	 * @param e     the e
	 * @return the exception report
	 */
	@Async(ExecutorConfig.EXECUTER_BRONZE)
	public ExceptionReport sendException(String title, Exception e) {
		return this.sendException(title, new ExceptionReport(e));
	}

	/**
	 * Send exception.
	 *
	 * @param title the title
	 * @param e     the e
	 * @return the exception report
	 */
	@Async(ExecutorConfig.EXECUTER_BRONZE)
	public ExceptionReport sendException(String title, ExceptionReport e) {
		return this.sendException(appConfig.getAppName(), title, e.getClass().getName(), e);
	}

	/**
	 * Send exception.
	 *
	 * @param appname   the appname
	 * @param title     the title
	 * @param exception the exception
	 * @param e         the e
	 * @return the exception report
	 */
	@Async(ExecutorConfig.EXECUTER_BRONZE)
	public ExceptionReport sendException(String appname, String title, String exception, ExceptionReport e) {

		if (appConfig.isDebug()) {
			LOGGER.error("Slack-Notify-Exception ", e);
			return e;
		}

		AppContext context = AppContextUtil.getContext();
		try {
			StackTraceElement[] traces = e.getStackTrace();

			Map<String, Object> message = new HashMap<>();
			message.put("text", title);

			if (appConfig.isProdMode()) {
				message.put("channel", postManConfig.getExceptionChannelCode());
			} else {
				message.put("channel", exceptionChannelCode);
			}

			List<Map<String, String>> attachments = new LinkedList<Map<String, String>>();

			Map<String, String> attachmentApp = new HashMap<String, String>();
			attachmentApp.put("title", "App Detail");
			attachmentApp.put("text", String.format("%s ->> %s", appname, appConfig.getAppName()));
			attachmentApp.put("color", "danger");
			attachments.add(attachmentApp);

			Map<String, String> attachmentTrace = new HashMap<String, String>();
			attachmentTrace.put("title", "RequestTrace");
			attachmentTrace.put("text", String.format("TraceId = %s-%s \n Tranx = %s", context.getTenant(),
					context.getTraceId(), context.getTranxId()));
			attachmentTrace.put("color", "danger");
			attachments.add(attachmentTrace);

			Map<String, String> attachmentTitle = new HashMap<String, String>();
			attachmentTitle.put("title", "Exception Details");
			attachmentTitle.put("text", String.format("Type = %s \n Message = %s", exception,
					URLEncoder.encode(ArgUtil.parseAsString(e.getMessage(), Constants.BLANK), "UTF-8")));
			attachmentTitle.put("color", "danger");
			attachments.add(attachmentTitle);

			if (traces.length > 0 && traces[0].toString().length() > 0) {
				Map<String, String> attachment = new HashMap<>();

				attachment.put("title", "Stack Trace");

				StringBuilder tracetext = new StringBuilder();
				for (StackTraceElement trace : traces) {
					tracetext.append("\n" + trace.toString());
				}

				attachment.put("text", tracetext.toString());
				attachment.put("color", "danger");
				// message.put("attachments", Collections.singletonList(attachment));
				attachments.add(attachment);
			}

			message.put("attachments", attachments);

			send(message, ChannelType.DEFAULT);

		} catch (Exception e1) {
			LOGGER.error("NestedException ", e1);
		}
		return e;
	}

}
