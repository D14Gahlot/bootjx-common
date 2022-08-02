package com.boot.jx.xms.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.AmxResponseSchemes.ApiResultsMetaCompactResponse;
import com.boot.jx.api.ApiResponse;
import com.boot.jx.mongo.CommonMongoTemplate;
import com.boot.jx.postman.model.ext.InBoundEvent;
import com.boot.jx.postman.query.ChatContactQuery;
import com.boot.jx.xms.XmsConstants;
import com.boot.jx.xms.XmsConstants.XMSClientAuth;
import com.boot.jx.xms.dto.SessionRequestObjects.ContactPrefsUpdate;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;

@Api(tags = "Contact Management", description = XmsConstants.CONTACT_MNGMNT_DESCRIPTION)
@RestController
public class ContactApiV1 {

	@Autowired
	public CommonMongoTemplate commonMongoTemplate;

	@ApiOperation(value = "Contact Preferences", notes = "${swagger.ContactApiV1.setLangPrefs.description}",
			authorizations = @Authorization("X_API_KEY"))
	@XMSClientAuth
	@RequestMapping(value = "/api/v1/contact/prefs/lang", method = { RequestMethod.POST })
	public ApiResultsMetaCompactResponse<InBoundEvent, Object> sessionRouting(@RequestBody ContactPrefsUpdate req) {
		commonMongoTemplate.updateFirst(new ChatContactQuery(req.contactId).setLang(req.lang));
		InBoundEvent event = new InBoundEvent().eventCode(InBoundEvent.CONTACT_UPDATE);
		event.contactId = req.contactId;
		return ApiResponse.buildResults(event);
	}

}
