package com.boot.utils;

import java.io.Serializable;
import java.security.NoSuchAlgorithmException;

public class OTPUtils {

	public static class OTPDetails implements Serializable {
		public OTPDetails() {
			super();
			this.timestamp = System.currentTimeMillis();
		}

		private static final long serialVersionUID = -2043308399129271937L;
		private long timestamp;
		private String id;
		private String key;
		private String hash;
		private String otp;
		private String prefix;

		public long getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(long timestamp) {
			this.timestamp = timestamp;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getOtp() {
			return otp;
		}

		public void setOtp(String otp) {
			this.otp = otp;
		}

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public boolean isValid(String otp) {
			try {
				return this.getHash().equals(CryptoUtil.getSHA1Hash(otp));
			} catch (NoSuchAlgorithmException e) {
				return false;
			}
		}

	}

	public static String genrateUniqueKey(String authid, String context) {
		return String.join("#", authid, context);
	}

	public static OTPDetails getBasicOTP(String id, String authid, String context) {
		OTPDetails details = new OTPDetails();
		details.setId(id);
		details.setKey(genrateUniqueKey(authid, context));
		String saltedDetailString = String.join("#", id, authid, context);
		details.setPrefix(CryptoUtil.toAlpha(3, saltedDetailString).toString().toUpperCase());
		details.setOtp(CryptoUtil.toNumeric(6, saltedDetailString).toString().toUpperCase());
		try {
			details.setHash(CryptoUtil.getSHA1Hash(details.getOtp()));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return details;
	}

	public static OTPDetails genrateBasicOTP(String authid, String context) {
		return getBasicOTP(Random.randomAlphaNumeric(16), authid, context);
	}

	public static boolean validateBasicOTP(String id, String authid, String context, String otp) {
		return getBasicOTP(id, authid, context).isValid(otp);
	}

}
