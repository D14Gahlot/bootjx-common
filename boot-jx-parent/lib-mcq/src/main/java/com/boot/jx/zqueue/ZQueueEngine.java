package com.boot.jx.zqueue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.boot.jx.tunnel.ITunnelDefs.TunnelTask;
import com.boot.jx.tunnel.ZQueueDefs.ZQMethodWrapper;
import com.boot.jx.tunnel.ZQueueDefs.ZQueueElement;
import com.boot.jx.tunnel.ZQueueDefs.ZQueueStore;
import com.boot.jx.tunnel.ZQueueDefs.Zqueuelized;
import com.boot.jx.tunnel.ZQueueDefs.Zqueuelizer;
import com.boot.jx.tunnel.task.ATaskLimiter;
import com.boot.utils.ArgUtil;
import com.boot.utils.ClazzUtil;

@Component
public class ZQueueEngine extends ATaskLimiter {

	@Autowired(required = false)
	List<Zqueuelizer> zqueuelizers;

	private final Map<String, Zqueuelizer> filtersMap = new HashMap<String, Zqueuelizer>();
	private final Map<String, ZQMethodWrapper> methodNameMap = new HashMap<String, ZQMethodWrapper>();

	@PostConstruct
	public void init() {
		if (ArgUtil.isEmpty(zqueuelizers)) {
			return;
		}
		for (Zqueuelizer zqueuelizer : zqueuelizers) {
			Class<?> c = AopProxyUtils.ultimateTargetClass(zqueuelizer);
			String controllerName = c.getName();
			Zqueuelized zqueuelizedDefault = ClazzUtil.getAnnotation(c, Zqueuelized.class);
			filtersMap.put("controllerName#" + controllerName, zqueuelizer);
			Method[] methods = c.getMethods();
			for (Method method : methods) {
				if (method.isAnnotationPresent(Zqueuelized.class)) {
					Zqueuelized zqueuelized = method.getAnnotation(Zqueuelized.class);
					ZQMethodWrapper methodWrapper = new ZQMethodWrapper();
					methodWrapper.setMethod(method);
					methodWrapper.setController(controllerName);
					methodWrapper.setDelay(zqueuelized.delay());
					methodNameMap.put(zqueuelized.value(), methodWrapper);
				} else if ("zqueuelized".equals(method.getName()) && zqueuelizedDefault != null) {
					ZQMethodWrapper methodWrapper = new ZQMethodWrapper();
					methodWrapper.setMethod(method);
					methodWrapper.setController(controllerName);
					methodWrapper.setDelay(zqueuelizedDefault.delay());
					methodNameMap.put(zqueuelizedDefault.value(), methodWrapper);
				}
			}
		}
	}

	@Autowired(required = false)
	private ZQueueStore zQStore;

	@Autowired(required = false)
	private ZQueueImpl zQueue;

	@Override
	public boolean doTask(TunnelTask task) {
		ZQMethodWrapper matchedMethod = methodNameMap.get(task.getName());
		if (ArgUtil.is(matchedMethod)) {
			Method method = matchedMethod.getMethod();
			Zqueuelizer controller = filtersMap.get("controllerName#" + matchedMethod.getController());
			List<Class<?>> prmTyps = Arrays.asList(method.getParameterTypes());
			if (prmTyps.contains(ZQueueElement.class)) {
				try {
					if (ArgUtil.is(zQStore)) {
						List<? extends ZQueueElement> elemtns = zQStore.dequeueAll(task.getName(), task.getId());
						if (ArgUtil.is(elemtns) && elemtns.size() > 0) {
							for (ZQueueElement elemtn : elemtns) {
								method.invoke(controller, elemtn);
							}
							zQueue.pushBackAsync(task);
							return false;
						}
					}
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	@Async
	public void throttleQ(TunnelTask task) {
		if (ArgUtil.isEmptyValue(task.getInterval())) {
			ZQMethodWrapper matchedMethod = methodNameMap.get(task.getName());
			if (ArgUtil.is(matchedMethod)) {
				this.throttle(task.intervalMillis(matchedMethod.getDelay()));
				return;// exit
			}

		}
		this.throttle(task);
	}

}
