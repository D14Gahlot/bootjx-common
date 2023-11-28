package com.boot.jx.contak.dto;

import java.io.Serializable;

public class CompanyMeta implements Serializable {
	private static final long serialVersionUID = -3331480179384982332L;
	public boolean verified;
	public long verifiedStamp;
	public long updateStamp;

	public boolean isVerified() {
		return verified;
	}

	public void setVerified(boolean verified) {
		this.verified = verified;
	}

	public long getVerifiedStamp() {
		return verifiedStamp;
	}

	public void setVerifiedStamp(long verifiedStamp) {
		this.verifiedStamp = verifiedStamp;
	}

	public long getUpdateStamp() {
		return updateStamp;
	}

	public void setUpdateStamp(long updateStamp) {
		this.updateStamp = updateStamp;
	}
}