package com.boot.jx.contak.doc;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.mongo.CommonDocInterfaces.IDocument;
import com.boot.utils.ArgUtil;

@Document(collection = "CONTAK_USER")
@TypeAlias("ContakUser")
public class ContakUserDoc implements IDocument, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;

	private String name;
	private String email;
	private String phone;

	private Set<String> role;
	private ContakUserMeta meta;
	private boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Set<String> getRole() {
		return role;
	}

	public void setRole(Set<String> role) {
		this.role = role;
	}

	public Set<String> role() {
		if (!ArgUtil.is(role)) {
			this.role = new TreeSet<String>();
		}
		return this.role;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ContakUserMeta getMeta() {
		return meta;
	}

	public void setMeta(ContakUserMeta meta) {
		this.meta = meta;
	}

	public ContakUserMeta meta() {
		if (!ArgUtil.is(meta)) {
			this.meta = new ContakUserMeta();
		}
		return this.meta;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
