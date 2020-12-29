package com.boot.jx.postman.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import com.boot.utils.ArgUtil;
import com.boot.utils.Constants;

/**
 * The Class EmailTemplate.
 */
public class EmailTemplate {

	/** The template id. */
	private String templateId;

	/** The template. */
	private String template;

	/** The replacement params. */
	private Map<String, String> replacementParams;

	/**
	 * Instantiates a new email template.
	 *
	 * @param templateId
	 *            the template id
	 */
	public EmailTemplate(String templateId) {
		this.templateId = templateId;
		try {
			this.template = loadTemplate(templateId);
		} catch (Exception e) {
			this.template = Constants.BLANK;
		}
	}

	/**
	 * Load template.
	 *
	 * @param templateId
	 *            the template id
	 * @return the string
	 * @throws Exception
	 *             the exception
	 */
	private String loadTemplate(String templateId) throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		File file = new File(classLoader.getResource("templates/" + templateId).getFile());
		String content = Constants.BLANK;
		try {
			content = new String(Files.readAllBytes(file.toPath()));
		} catch (IOException e) {
			throw new Exception("Could not read template with ID = " + templateId);
		}
		return content;
	}

	/**
	 * Gets the template.
	 *
	 * @param replacements
	 *            the replacements
	 * @return the template
	 */
	public String getTemplate(Map<String, String> replacements) {
		String cTemplate = this.getTemplate();

		if (!ArgUtil.isEmpty(cTemplate)) {
			for (Map.Entry<String, String> entry : replacements.entrySet()) {
				cTemplate = cTemplate.replace("{{" + entry.getKey() + "}}", entry.getValue());
			}
		}

		return cTemplate;
	}

	/**
	 * Gets the template id.
	 *
	 * @return the templateId
	 */
	public String getTemplateId() {
		return templateId;
	}

	/**
	 * Sets the template id.
	 *
	 * @param templateId
	 *            the templateId to set
	 */
	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	/**
	 * Gets the template.
	 *
	 * @return the template
	 */
	public String getTemplate() {
		return template;
	}

	/**
	 * Sets the template.
	 *
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	/**
	 * Gets the replacement params.
	 *
	 * @return the replacementParams
	 */
	public Map<String, String> getReplacementParams() {
		return replacementParams;
	}

	/**
	 * Sets the replacement params.
	 *
	 * @param replacementParams
	 *            the replacementParams to set
	 */
	public void setReplacementParams(Map<String, String> replacementParams) {
		this.replacementParams = replacementParams;
	}

}