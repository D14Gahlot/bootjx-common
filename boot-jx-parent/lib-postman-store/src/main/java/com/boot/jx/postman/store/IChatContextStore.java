package com.boot.jx.postman.store;

public interface IChatContextStore<U, S> {

	public static interface ChatContextStore<U, S> extends IChatContextStore<U, S> {
	}

	public U newUser();

	public S newSession();

	public U getUser();

	public S getSession();

	public U loadUser(Object user);

	public S loadSession(Object session);

}
