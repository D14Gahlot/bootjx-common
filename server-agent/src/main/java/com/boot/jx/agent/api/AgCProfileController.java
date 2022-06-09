package com.boot.jx.agent.api;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boot.jx.api.ApiResponse;
import com.boot.jx.api.ListRequestModel;
import com.boot.jx.postman.doc.ChatContactDoc;
import com.boot.jx.postman.doc.ChatSessionDoc;
import com.boot.jx.postman.doc.QuickLabel;
import com.boot.jx.postman.dto.ContactDTO;
import com.boot.jx.postman.manager.ChatLogger;
import com.boot.jx.postman.service.ChatDTOUtil;
import com.boot.jx.postman.store.MessageStore.EVENTS;
import com.boot.jx.postman.store.SessionStore;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;

@RestController
public class AgCProfileController {

	@Autowired
	private SessionStore sessionStore;

	@Autowired
	private ChatLogger logManager;

	@RequestMapping(value = { "/api/contact/label" }, method = { RequestMethod.POST })
	public ApiResponse<ContactDTO, Object> addContactLabel(@RequestParam String sessionId,
			@RequestBody ListRequestModel<QuickLabel> labels) {
		ChatSessionDoc sessionDoc = sessionStore.getSession(sessionId);
		ChatContactDoc contact = sessionStore.getContact(sessionDoc.getContactId());

		List<String> oldList = contact.labelId();
		List<String> newList = new ArrayList<String>();
		for (QuickLabel tag : labels.getValues()) {
			newList.add(tag.getId());
		}
		newList = CollectionUtil.distinct(newList);
		contact.setLabelId(CollectionUtil.distinct(newList));
		sessionStore.save(contact);

		// LOGS
		List<String> removedItems = new ArrayList<String>(oldList);
		removedItems.removeAll(newList);
		if (ArgUtil.is(removedItems)) {
			logManager.event(sessionDoc, EVENTS.LABEL_REMOVED, removedItems.toArray(new String[0]));
		}

		List<String> addedItems = new ArrayList<String>(newList);
		addedItems.removeAll(oldList);
		if (ArgUtil.is(addedItems)) {
			logManager.event(sessionDoc, EVENTS.LABEL_ADDED, addedItems.toArray(new String[0]));
		}
		return ApiResponse.buildData(ChatDTOUtil.getContactDTO(contact));
	}

}
