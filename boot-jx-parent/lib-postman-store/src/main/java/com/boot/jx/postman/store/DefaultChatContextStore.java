package com.boot.jx.postman.store;

import org.springframework.stereotype.Component;

import com.boot.jx.postman.store.IChatContextStore.BasicChatContextSession;
import com.boot.jx.postman.store.IChatContextStore.BasicChatContextUser;
import com.boot.jx.scope.ThreadScoped;
import com.boot.utils.JsonUtil;

@Component
@ThreadScoped
public class DefaultChatContextStore implements IChatContextStore<BasicChatContextUser, BasicChatContextSession> {

	private BasicChatContextSession session;
	private BasicChatContextUser user;

	@Override
	public BasicChatContextUser newUser() {
		return new BasicChatContextUser();
	}

	@Override
	public BasicChatContextSession newSession() {
		return new BasicChatContextSession();
	}

	@Override
	public BasicChatContextUser getUser() {
		return user;
	}

	@Override
	public BasicChatContextSession getSession() {
		return session;
	}

	@Override
	public BasicChatContextUser loadUser(Object user) {
		if (user == null) {
			this.user = new BasicChatContextUser();
		} else {
			this.user = JsonUtil.parse(user, BasicChatContextUser.class);
		}
		return this.user;
	}

	@Override
	public BasicChatContextSession loadSession(Object session) {
		if (session == null) {
			this.session = new BasicChatContextSession();
		} else {
			this.session = JsonUtil.parse(session, BasicChatContextSession.class);
		}
		return this.session;
	}

}