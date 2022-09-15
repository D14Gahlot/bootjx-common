package com.boot.jx.postman.store;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.boot.jx.model.ModelPatch;
import com.boot.jx.model.ModelPatch.ModelPatchCommand;
import com.boot.jx.mongo.CommonMongoQueryBuilder.SimpleDocQueryBuilder;
import com.boot.jx.mongo.CommonMongoTemplateAbstract;
import com.boot.jx.postman.PMEnvironment;
import com.boot.jx.postman.PMEnvironment.PMClientConfig;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.CustomerProfileDoc;
import com.boot.jx.postman.model.MessageDefinitions.Contactable;
import com.boot.jx.postman.pbook.PBAddress;
import com.boot.jx.postman.pbook.PBEmail;
import com.boot.jx.postman.pbook.PBName;
import com.boot.jx.postman.pbook.PBPhone;
import com.boot.jx.postman.pbook.PBWebsite;
import com.boot.jx.utils.PostManUtil;
import com.boot.utils.ArgUtil;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

@Component
public class ContactStore extends CommonMongoTemplateAbstract {

	private static final Logger LOGGER = LoggerFactory.getLogger(ContactStore.class);

	public static final PhoneNumberUtil PHONE_NUMBER_UTIL = PhoneNumberUtil.getInstance();

	@Autowired
	public PMClientConfig pmClientConfig;

	@Autowired
	protected PMEnvironment environment;

	public ChatContactDoc findContact(Contactable contactMeta) {
		Contactable contact = PostManUtil.getContactMeta(contactMeta);
		if (ArgUtil.isEmpty(contact.getContactId())) {
			return null;
		}
		return findById(contact.getContactId(), ChatContactDoc.class);
	}

	public List<ChatContactDoc> searchContacts(String search, String lane) {
		// TODO:-- Optimize Search
		// Query query =
		// TextQuery.queryText(TextCriteria.forDefaultLanguage().matching(search)).sortByScore()

		Criteria c = Criteria.where("lane").is(lane); // Lane should be fixed

		if (ArgUtil.is(search)) {
			c = c.orOperator(
					// Check all fields
					Criteria.where("name").regex("" + search + "", "i"),
					Criteria.where("phone").regex("" + search + "", "i"),
					Criteria.where("email").regex("" + search + "", "i"));
		}
		Query query = new Query()
				// New Criteria
				.addCriteria(c);
		return find(query, ChatContactDoc.class);

	}

	public static <T> Set<T> patch(ModelPatchCommand command, Set<T> items, T item) {

		switch (command) {
		case ADD:
			items.add(item);
			break;
		case REMOVE:
			items.remove(item);
			break;
		case DELETE:
			return null;
		default:
			break;
		}
		return items;
	}

	public PBPhone parsePhone(PBPhone pbPhone) {
		String defaultRegion = environment.keyEntry("postman.phonebook.region").asString("IN");
		if (ArgUtil.not(pbPhone.phone)) {
			pbPhone.phone = String.format("+%s%s", pbPhone.countryCallingCode, pbPhone.nationalNumber);
		}
		pbPhone.phone = pbPhone.phone.replace(" ", "").replaceAll("^[\\+0\\s]+(?!$)", "").trim();
		try {
			PhoneNumber phoneNumber = PHONE_NUMBER_UTIL.parse("+" + pbPhone.phone, defaultRegion);
			pbPhone.nationalNumber = ArgUtil.parseAsString(phoneNumber.getNationalNumber());
			pbPhone.countryCallingCode = ArgUtil.parseAsString(phoneNumber.getCountryCode());
			pbPhone.phone = String.format("+%s%s", phoneNumber.getCountryCode(), phoneNumber.getNationalNumber());
			pbPhone.country = PHONE_NUMBER_UTIL.getRegionCodeForCountryCode(phoneNumber.getCountryCode());
		} catch (NumberParseException e) {

		}

		return pbPhone;
	}

	public CustomerProfileDoc patchCustomerProfile(ModelPatch req) {
		CustomerProfileDoc doc = findById(req.getId(), CustomerProfileDoc.class);
		SimpleDocQueryBuilder qb = SimpleDocQueryBuilder.doc(doc);

		switch (req.getField()) {
		case "email":
		case "emails":
			PBEmail email = req.value().as(PBEmail.class);
			qb.set("emails", patch(req.getCommand(), doc.emails(), email));
			break;
		case "phone":
		case "phones":
			PBPhone phone = parsePhone(req.value().as(PBPhone.class));
			qb.set("phones", patch(req.getCommand(), doc.phones(), phone));
			break;
		case "address":
		case "addresses":
			PBAddress address = req.value().as(PBAddress.class);
			qb.set("addresses", patch(req.getCommand(), doc.addresses(), address));
			break;
		case "url":
		case "urls":
			PBWebsite url = req.value().as(PBWebsite.class);
			qb.set("urls", patch(req.getCommand(), doc.urls(), url));
			break;
		case "name":
			PBName name = req.value().as(PBName.class);
			qb.set("name", name);
			break;
		case "code":
			qb.set("code", req.value().asString());
			break;
		case "rmCode":
			qb.set("rmCode", req.value().asString());
			break;
		default:
			break;
		}
		update(qb);
		return findById(req.getId(), CustomerProfileDoc.class);
	}

}
