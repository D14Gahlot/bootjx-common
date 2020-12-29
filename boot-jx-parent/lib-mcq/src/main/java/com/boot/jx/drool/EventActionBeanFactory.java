package com.boot.jx.drool;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.boot.common.HandlerBeanFactory;
import com.boot.jx.drools.DEventActionHandler;

@Component
public class EventActionBeanFactory extends HandlerBeanFactory<DEventActionHandler<?>> {

	private static final long serialVersionUID = 2959267620648739583L;

	public EventActionBeanFactory(@Autowired(required = false) List<DEventActionHandler<?>> libs) {
		super(libs);
	}

}
