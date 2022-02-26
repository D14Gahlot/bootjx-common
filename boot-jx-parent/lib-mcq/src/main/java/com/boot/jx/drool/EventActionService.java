package com.boot.jx.drool;

import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.drools.DAction;
import com.boot.jx.drools.DEvent;
import com.boot.jx.drools.DEvent.State;
import com.boot.jx.drools.DEvent.Status;
import com.boot.jx.drools.DEventActionHandler;
import com.boot.jx.drools.DEventActionService;
import com.boot.jx.drools.DEventCallbackHandler;
import com.boot.jx.rest.RestService;
import com.boot.utils.ArgUtil;

@Component
public class EventActionService implements DEventActionService {

    @Value("${jax.drools.url:nourl}")
    private String droolUrl;

    @Autowired
    RestService restService;

    @Autowired
    EventActionBeanFactory eventActionBeanFactory;

    @Autowired
    EventCallbackBeanFactory eventCallbackBeanFactory;

    @SuppressWarnings("unchecked")
    private <T extends DEvent> void process(String eventName, T event, String path) {
	try {
	    event.setState(State.SENT);
	    DEvent dEvent = restService.ajax(droolUrl).path(path).pathParam(Param.EVENT_NAME, eventName).post(event)
		    .as(DEvent.class);
	    event.setState(State.RECEIVED);
	    event.setActions(dEvent.getActions());
	    event.setStatus(Status.SUCCESS);
	} catch (Exception e) {
	    event.setStatus(Status.FAILED);
	}

	if (ArgUtil.is(event.getActions())) {
	    for (Entry<String, DAction> item : event.getActions().entrySet()) {
		DEventActionHandler<T> dEventActionHandler = (DEventActionHandler<T>) eventActionBeanFactory
			.get(item.getKey());
		if (ArgUtil.is(dEventActionHandler)) {
		    dEventActionHandler.handle(event, item.getValue());
		}
	    }
	}
	event.setState(State.PROCESSED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DEvent> void beforeEvent(String eventName, T event) {
	event.setEventName(eventName);
	event.setEventType(DEvent.Types.BEFORE);
	process(eventName, event, Path.BEFORE_EVENT_PATH);
	DEventCallbackHandler<T> dEventCallbackHandler = eventCallbackBeanFactory.get(eventName);
	if (ArgUtil.is(dEventCallbackHandler)) {
	    dEventCallbackHandler.beforeEventCallback(event);
	}
	event.setState(State.COMPLETED);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends DEvent> void onEvent(String eventName, T event) {
	event.setEventName(eventName);
	event.setEventType(DEvent.Types.ON);
	process(eventName, event, Path.ON_EVENT_PATH);
	DEventCallbackHandler<T> dEventCallbackHandler = eventCallbackBeanFactory.get(eventName);
	if (ArgUtil.is(dEventCallbackHandler)) {
	    dEventCallbackHandler.onEventCallback(event);
	}
	event.setState(State.COMPLETED);
    }

    @Override
    @SuppressWarnings("unchecked")
    @Async
    public <T extends DEvent> void afterEvent(String eventName, T event) {
	event.setEventName(eventName);
	event.setEventType(DEvent.Types.AFTER);
	process(eventName, event, Path.AFTER_EVENT_PATH);
	DEventCallbackHandler<T> dEventCallbackHandler = eventCallbackBeanFactory.get(eventName);
	if (ArgUtil.is(dEventCallbackHandler)) {
	    dEventCallbackHandler.afterEventCallback(event);
	}
	event.setState(State.COMPLETED);
    }
}
