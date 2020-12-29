package com.boot.jx.postman.cache;

import org.springframework.stereotype.Component;

import com.boot.jx.cache.CacheBox;

/**
 * The Class LoggedInUsers.
 */
@Component
public class ContactsCache extends CacheBox<String> {

	/**
	 * Instantiates a new logged in users.
	 */
	public ContactsCache() {
		super("ContactsCache");
	}

}
