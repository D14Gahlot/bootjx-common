package com.boot.jx.postman.nlp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.postman.service.CommonNLPService;
import com.boot.utils.ArgUtil;

@Component
public class CommonNLPServiceImpl implements CommonNLPService {

	@Autowired(required = false)
	private OpenNLPService openNLPService;

	@Autowired(required = false)
	private CoreNLPService coreNLPService;

	@Override
	public InboxMessage addTags(InboxMessage inboxMessage, TagDocument tags) {
		if (ArgUtil.is(openNLPService)) {
			openNLPService.addTags(inboxMessage.getMessage(), tags);
		}

		if (ArgUtil.is(coreNLPService)) {
			coreNLPService.addTags(inboxMessage.getMessage(), tags);
		}
		return null;
	}

}
