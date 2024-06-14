package com.boot.jx.postman.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.dict.ContactType;
import com.boot.jx.model.CommonFile;
import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.PostmanPackages.ICommonTmplPackage;
import com.boot.jx.postman.PostmanPackages.Text2Media;
import com.boot.jx.postman.model.Attachment;
import com.boot.jx.postman.model.OutboxMessage;
import com.boot.jx.postman.model.PostManFile;
import com.boot.jx.postman.model.TmplElement;
import com.boot.jx.rest.RestService;
import com.boot.model.MapModel;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@Component
public class TmplClient {

	public static class PATH {
		public static final String TMPL_FILE_PROCESS = "/tmpl/file/process";
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(TmplClient.class);

	@Autowired
	private RestService restService;

	@Autowired
	private PostManClient postManClient;

	@Autowired(required = false)
	private ICommonTmplPackage iCommonTmplPackage;

	@Autowired
	protected Text2Media text2Media;

	public ApiResponse<CommonFile, Object> process(CommonFile file, ContactType contactType) throws PostManException {
		if (ArgUtil.is(iCommonTmplPackage)) {
			return ApiResponse.buildResult(iCommonTmplPackage.process(file, contactType));
		}
		return restService.ajax(postManClient.getPostmapURL()).path(PATH.TMPL_FILE_PROCESS)
				.queryParam("contactType", contactType).queryParam(PostManClient.PARAM_LANG, postManClient.getLang())
				.contentTypeJson().acceptJson().post(file)
				.as(new ParameterizedTypeReference<ApiResponse<CommonFile, Object>>() {
				});
	}

	public OutboxMessage process(OutboxMessage outboxMessage) {
		CommonFile file = new PostManFile();

		file.setModel(outboxMessage.getModel());
		file.setTemplate(outboxMessage.getHsm());
		file = this.process(file, outboxMessage.contact().type()).getResult();

		outboxMessage.setMessage(file.getContent());

		Object categoryType = file.meta().get("categoryType");

		if (ArgUtil.is(categoryType)) {
			outboxMessage.meta().put("categoryType", categoryType);
		}

		if (!ArgUtil.is(outboxMessage.getSubject())) {
			outboxMessage.setSubject(ArgUtil.parseAsString(file.getOptions().get("subject"), file.getTitle()));
		}

		Map<String, Object> options = new HashMap<String, Object>();
		List<TmplElement> buttons = new ArrayList<TmplElement>();
		List<TmplElement> inputs = new ArrayList<TmplElement>();

		MapModel optionsModel = MapModel.from(file.options());
		List<Map<String, Object>> buttonsModel = optionsModel.keyEntry("buttons").asListOfMap();

		for (Map<String, Object> map : buttonsModel) {
			MapModel buttonMapModel = MapModel.from(map);
			buttons.add(new TmplElement().code(buttonMapModel.getString("key")).label(buttonMapModel.getString("label"))
					.desc(buttonMapModel.getString("desc")).type(buttonMapModel.getString("type"))
					.url(buttonMapModel.getString("url")).phone(buttonMapModel.getString("phone_number")));
		}

		for (Entry<String, Object> entry : file.getOptions().entrySet()) {
			if (entry.getKey().indexOf("form-input-") == 0) {
				String[] params = ArgUtil.parseAsString(entry.getValue()).split("\\|");
				inputs.add(new TmplElement().code(entry.getKey().replace("form-input-", ""))
						.label(CollectionUtil.get(params, 0)).type(CollectionUtil.get(params, 1)));
			} else if (entry.getKey().indexOf("actions-button-") == 0) {
				String[] params = ArgUtil.parseAsString(entry.getValue()).split("\\|");
				buttons.add(new TmplElement().code(entry.getKey().replace("actions-button-", ""))
						.label(CollectionUtil.get(params, 0)).type(CollectionUtil.get(params, 1)));
			} else {
				options.put(entry.getKey(), entry.getValue());
			}
		}
		options.put("inputs", inputs);
		options.put("buttons", buttons);

		Attachment defaultAttachment = optionsModel.keyEntry("attachment").as(Attachment.class);
		if (ArgUtil.is(defaultAttachment) && outboxMessage.attachments().size() == 0) {
			outboxMessage.attachments().add(defaultAttachment);
		}

		Attachment backgroundVoice = optionsModel.keyEntry("bg_voice").as(Attachment.class);
		// TODO:-@lalit to review
		if (ArgUtil.is(backgroundVoice) && outboxMessage.getContact().type().equals(ContactType.WEBSITE)) {
			outboxMessage.attachments().add(backgroundVoice);
		}

		if (ArgUtil.is(outboxMessage.attachments())) {
			try {
				for (Attachment attach : outboxMessage.getAttachments()) {
					if (ArgUtil.is(attach.getMediaTemplate())) {
						String attachFileStr = process(attach.getMediaTemplate(), outboxMessage.getModel());
						attach.setMediaTemplate(attachFileStr);
						attach.setMediaURL(text2Media.toImage(attachFileStr));
					}
				}
			} catch (Exception e) {
				outboxMessage.logs().add("MediaTemplateException : " + e.getMessage());
				LOGGER.error("MediaTemplateException", e);
			}
		}

		outboxMessage.options().putAll(options);
		return outboxMessage;
	}

	public String process(String template, Object model) {
		if (ArgUtil.is(iCommonTmplPackage)) {
			return iCommonTmplPackage.process(template, model);
		}
		return template;
	}

}
