package com.boot.jx.account.doc;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.boot.jx.validation.ValidationAnnotations.ValidEmail;
import com.boot.jx.validation.ValidationAnnotations.ValidPhone;

public class SignupContact implements Serializable {

	private static final long serialVersionUID = 3140425278301607438L;

	@NotNull
	private String name;

	@NotNull
	@ValidEmail
	private String email;

	@NotNull
	@ValidPhone
	private String phone;

	@NotNull
	private String company;

	@NotNull
	private String role;

	private List<Object> product;

	@NotNull
	private String country;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public List<Object> getProduct() {
		return product;
	}

	public void setProduct(List<Object> product) {
		this.product = product;
	}

}
