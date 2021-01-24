/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.boot.jx.mcq.shedlock;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.boot.jx.scope.tnt.Tenants;

@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface SchedulerLock {

	public static enum LockContext {
		BY_UNIQUE, BY_CLASS, BY_METHOD
	}

	/**
	 * Lock name.
	 */
	String name() default "";

	LockContext context() default LockContext.BY_METHOD;

	String tenent() default Tenants.NONE_STR;

	/**
	 * How long (in ms) the lock should be kept in case the machine which obtained
	 * the lock died before releasing it. This is just a fallback, under normal
	 * circumstances the lock is released as soon the tasks finishes. Negative value
	 * means default
	 *
	 * Ignored when using ZooKeeper and other lock providers which are able to
	 * detect dead node.
	 */
	long lockMaxAge() default -1;

	/**
	 * Lock at most for as string. Can be either number in ms or formatted as
	 * described in {@link java.time.Duration#parse(CharSequence)}
	 */
	String lockMaxAgeString() default "";

	/**
	 * The lock will be held at least for X millis. Can be used if you really need
	 * to execute the task at most once in given period of time. If the duration of
	 * the task is shorter than clock difference between nodes, the task can be
	 * theoretically executed more than once (one node after another). By setting
	 * this parameter, you can make sure that the lock will be kept at least for
	 * given period of time.
	 */
	long lockFixedDelay() default -1;

	/**
	 * Lock at least for as string. Can be either number in ms or formatted as
	 * described in {@link java.time.Duration#parse(CharSequence)}
	 */
	String lockFixedDelayString() default "";
}
