package com.boot.jx.contak.doc;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import com.boot.jx.contak.dto.CompanyDoc;
import com.boot.jx.mongo.CommonDocInterfaces.IDocument;

@Document(collection = "CONTAK_MEMBERSHIP")
@TypeAlias("ContakMembership")
public class ContakMembershipDoc implements IDocument, Serializable {

	private static final long serialVersionUID = -3354844112176554561L;

	@Id
	private String id;

	private String userId;
	private String companyId;

	private ContakUserDoc user;
	private CompanyDoc company;

	private String membershipType;

	private boolean active;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public ContakUserDoc getUser() {
		return user;
	}

	public void setUser(ContakUserDoc user) {
		this.user = user;
	}

	public CompanyDoc getCompany() {
		return company;
	}

	public void setCompany(CompanyDoc company) {
		this.company = company;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getCompanyId() {
		return companyId;
	}

	public void setCompanyId(String companyId) {
		this.companyId = companyId;
	}

	public String getMembershipType() {
		return membershipType;
	}

	public void setMembershipType(String membershipType) {
		this.membershipType = membershipType;
	}

}
