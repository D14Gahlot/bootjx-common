package com.boot.jax.bot.alex;


public class ExchangRateResponseModel {

	public static String localCurrencyCode;
	public static String foreignCurrencyCode;
	public static Long localCurrencyAmount = (long) 10;
	public static Long foreignCurrencyAmount;
	
	public static String getLocalCurrencyCode() {
		return localCurrencyCode;
	}
	public static void setLocalCurrencyCode(String localCurrencyCode) {
		ExchangRateResponseModel.localCurrencyCode = localCurrencyCode;
	}
	public static String getForeignCurrencyCode() {
		return foreignCurrencyCode;
	}
	public static void setForeignCurrencyCode(String foreignCurrencyCode) {
		ExchangRateResponseModel.foreignCurrencyCode = foreignCurrencyCode;
	}
	public static Long getLocalCurrencyAmount() {
		return localCurrencyAmount;
	}
	public static void setLocalCurrencyAmount(Long localCurrencyAmount) {
		ExchangRateResponseModel.localCurrencyAmount = localCurrencyAmount;
	}
	public static Long getForeignCurrencyAmount() {
		return foreignCurrencyAmount;
	}
	public static void setForeignCurrencyAmount(Long foreignCurrencyAmount) {
		ExchangRateResponseModel.foreignCurrencyAmount = foreignCurrencyAmount;
	}

}
