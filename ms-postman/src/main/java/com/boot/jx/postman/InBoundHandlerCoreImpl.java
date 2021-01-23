package com.boot.jx.postman;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.jx.inbound.InBoundHandler;
import com.boot.jx.postman.model.InboxMessage;
import com.boot.jx.postman.model.TagDocument;
import com.boot.jx.postman.nlp.CoreNLPService;
import com.boot.jx.postman.nlp.OpenNLPService;
import com.boot.utils.ArgUtil;
import com.boot.utils.CollectionUtil;
import com.sun.xml.bind.v2.runtime.reflect.ListIterator;

@Component
public class InBoundHandlerCoreImpl implements InBoundHandler {

	@Autowired
	private OpenNLPService openNLPService;

	@Autowired
	private CoreNLPService coreNLPService;

	@Override
	public InboxMessage onHandle(InboxMessage inboxMessage) {

		if (ArgUtil.is(inboxMessage.getMessage())) {
			try {
				if (!ArgUtil.is(inboxMessage.getTags())) {
					inboxMessage.setTags(new TagDocument());
				}
				openNLPService.addTags(inboxMessage.getMessage(), inboxMessage.getTags());
				coreNLPService.addTags(inboxMessage.getMessage(), inboxMessage.getTags());

				if (ArgUtil.is(inboxMessage.getTags().getCategories())) {
					inboxMessage.getTags()
							.setCategories(CollectionUtil.distinct(inboxMessage.getTags().getCategories()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getPersons())) {
					inboxMessage.getTags().setPersons(CollectionUtil.distinct(inboxMessage.getTags().getPersons()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getCountries())) {
					inboxMessage.getTags().setCountries(CollectionUtil.distinct(inboxMessage.getTags().getCountries()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getCities())) {
					inboxMessage.getTags().setCities(CollectionUtil.distinct(inboxMessage.getTags().getCities()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getLocations())) {
					inboxMessage.getTags().setLocations(CollectionUtil.distinct(inboxMessage.getTags().getLocations()));

					java.util.ListIterator<String> iter = inboxMessage.getTags().getLocations().listIterator();

					while (iter.hasNext()) {
						String location = iter.next();
						if (ArgUtil.is(inboxMessage.getTags().getCountries())
								&& inboxMessage.getTags().getCountries().contains(location)) {
							iter.remove();
							continue;
						}
						if (ArgUtil.is(inboxMessage.getTags().getCities())
								&& inboxMessage.getTags().getCities().contains(location)) {
							iter.remove();
							continue;
						}
					}
				}

				if (ArgUtil.is(inboxMessage.getTags().getOrganizations())) {
					inboxMessage.getTags()
							.setOrganizations(CollectionUtil.distinct(inboxMessage.getTags().getOrganizations()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getOrganizations())) {
					inboxMessage.getTags()
							.setOrganizations(CollectionUtil.distinct(inboxMessage.getTags().getOrganizations()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getOrganizations())) {
					inboxMessage.getTags()
							.setOrganizations(CollectionUtil.distinct(inboxMessage.getTags().getOrganizations()));
				}

				if (ArgUtil.is(inboxMessage.getTags().getSentiments())) {
					int scoe = inboxMessage.getTags().getSentimentScore();
					for (String sentiment : inboxMessage.getTags().getSentiments()) {
						switch (sentiment) {
						case "Very positive":
							scoe = +2;
							break;
						case "Positive":
							scoe = +1;
							break;
						case "Very negative":
							scoe = -2;
							break;
						case "Negative":
							scoe = -1;
							break;
						default:
							break;
						}
					}
					inboxMessage.getTags().setSentimentScore(scoe);
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return inboxMessage;
	}

}
