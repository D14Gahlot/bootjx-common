package com.boot.jx.postman.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.PostManException;
import com.boot.jx.postman.model.Email;
import com.boot.jx.postman.model.ITemplates.TemplateDefaultEnum;
import com.boot.jx.postman.model.MessageCategoryType;
import com.boot.jx.postman.model.SupportEmail;
import com.boot.jx.scope.tnt.TenantScoped;
import com.boot.jx.scope.tnt.TenantValue;
import com.boot.utils.Utils;

/**
 * The Class SupportService.
 */
@Component
@TenantScoped
public class SupportService {

	/** The support contact to. */
	@TenantValue("${support.contact.to}")
	private String supportContactTo;

	/** The support contact from. */
	@TenantValue("${support.contact.from}")
	private String supportContactFrom;

	/** The support contact subject. */
	@TenantValue("${support.contact.subject}")
	private String supportContactSubject;

	/** The support SAO subject. */
	@TenantValue("${support.soa.to}")
	private String supportSAOSubject;

	/** The support IT subject. */
	@TenantValue("${support.it.to}")
	private String supportITSubject;

	/**
	 * Creates the contact us email.
	 *
	 * @param email the email
	 * @return the email
	 * @throws PostManException the post man exception
	 */
	public Email createContactUsEmail(SupportEmail email) throws PostManException {

		Map<String, String> map = new HashMap<String, String>();
		map.put("name", email.getVisitorName());
		map.put("cphone", email.getVisitorPhone());
		map.put("cemail", email.getVisitorEmail());
		map.put("message", email.getVisitorMessage());
		map.put("identity", email.getIdentity());
		map.put("lines", Utils.concatenate(email.getLines(), " \n "));

		email.setFrom(this.supportContactFrom);
		email.setReplyToEmail(email.getVisitorEmail());
		email.addAllTo(supportContactTo);
		email.getModel().put("data", map);
		email.setSubject(supportContactSubject);
		email.setITemplate(TemplateDefaultEnum.CONTACT_US);
		email.setHtml(true);

		return email;
	}

	/**
	 * Filter message type.
	 *
	 * @param email the email
	 * @return the email
	 */
	public Email filterMessageType(Email email) {
		if (email.categoryType() == null) {
		} else if (email.categoryType() == MessageCategoryType.SOA) {
			email.addAllTo(supportSAOSubject);
		} else if (email.categoryType() == MessageCategoryType.IT) {
			email.addAllTo(supportITSubject);
		}
		return email;
	}

}
