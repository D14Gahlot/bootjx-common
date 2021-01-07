package com.boot.jx.postman.store;

import java.util.HashMap;
import java.util.Map;

import com.boot.jx.postman.store.IChatContextStore.BasicChatContextSession;
import com.boot.jx.postman.store.IChatContextStore.BasicChatContextUser;

public interface IChatContextStore<U extends BasicChatContextUser, S extends BasicChatContextSession> {

	public static class BasicChatContextUser {
		private String firstName;
		private String lastName;
		private String profilePic;
		private String gender;
		private Map<String, Object> data;

		public String getFirstName() {
			return firstName;
		}

		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public void setLastName(String lastName) {
			this.lastName = lastName;
		}

		public String getProfilePic() {
			return profilePic;
		}

		public void setProfilePic(String profilePic) {
			this.profilePic = profilePic;
		}

		public String getGender() {
			return gender;
		}

		public void setGender(String gender) {
			this.gender = gender;
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public Map<String, Object> data() {
			if (this.data == null) {
				this.data = new HashMap<String, Object>();
			}
			return data;
		}

		public void put(String key, Object value) {
			this.data().put(key, value);
		}

		public Object get(String key) {
			return this.data().get(key);
		}
	}

	public static class BasicChatContextSession {
		private Map<String, Object> data;

		public BasicChatContextSession() {
			super();
			this.data = new HashMap<String, Object>();
		}

		public Map<String, Object> getData() {
			return data;
		}

		public void setData(Map<String, Object> data) {
			this.data = data;
		}

		public Map<String, Object> data() {
			if (this.data == null) {
				this.data = new HashMap<String, Object>();
			}
			return data;
		}

		public void put(String key, Object value) {
			this.data().put(key, value);
		}

		public Object get(String key) {
			return this.data().get(key);
		}
	}

	public static interface ChatContextStore<U, S>
			extends IChatContextStore<BasicChatContextUser, BasicChatContextSession> {
	}

	public U newUser();

	public S newSession();

	public U getUser();

	public S getSession();

	public U loadUser(Object user);

	public S loadSession(Object session);

}
