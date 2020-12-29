package com.boot.jx.drool;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.common.HandlerBeanFactory;
import com.boot.jx.drools.DEventCallbackHandler;

@SuppressWarnings("rawtypes")
@Component
public class EventCallbackBeanFactory extends HandlerBeanFactory<DEventCallbackHandler> {

	private static final long serialVersionUID = 2959267620648739583L;

	public EventCallbackBeanFactory(@Autowired(required = false) List<DEventCallbackHandler> libs) {
		super(libs);
	}

}
