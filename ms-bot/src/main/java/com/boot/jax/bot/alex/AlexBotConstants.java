package com.boot.jax.bot.alex;

public class AlexBotConstants {

	public static class KEY {
		public static final String VERIFY_CIVIL_ID = "verify/identity/v1";
		public static final String LINK_CIVIL_ID = "link/identity/v1";
		public static final String JUST_CIVIL_ID = "link/identity/v2";
		public static final String PING = "account/ping";
		public static final String MENU = "account/menu";
		public static final String ROUTE_NUMBER = "route/number/v1";
		public static final String SERVICE_SELECTOR = "account/service/select";

		public static final String CONFIRM_GENDER = "confirm/gender/menu";
		public static final String CONFIRM_GENDER_ONSELECT = "confirm/gender/onselect";

		public static final String MENU_SELECTOR = "account/menu/options";
		public static final String CUSTOMER_MENU = "account/customer/menu";
		public static final String NON_CUSTOMER_MENU = "account/noncustomer/menu";
		public static final String CUSTOMER_MENU_OPTIONS = "account/customer/menu/options";
		public static final String NON_CUSTOMER_MENU_OPTIONS = "account/noncustomer/menu/options";
		public static final String VERIFIED_MOBILE_NUMBER = "account/verifiedmobno";
		public static final String MOBILE_NUMBER = "account/mobileno";
		public static final String ENTER_MOBILE_NUMBER = "account/entermobno";
		public static final String NEED_CIVIL_ID = "account/needcivilid";
		public static final String CIVIL_ID = "account/civilid";
		public static final String CIVIL_ID_EXISTS = "account/civilidexists";
		public static final String CIVIL_ID_MATCH = "account/civilidmatch";
		public static final String INITIATE = "initiate/mobile";
		public static final String NON_CUST_MENU_ON_SELECT = "account/noncust/menu/onselect";
		public static final String CUST_MENU_ON_SELECT = "account/cust/menu/onselect";
		public static final String MORE_YES_EXIT = "account/exchangerate";
		public static final String COUNTRY_YES_EXIT = "account/exchangerate/country";
		public static final String CURRENCY_FOR_COUNTRY = "account/exchangerate/country/currency";
		public static final String YES_EXIT = "account/end";

	}

	public static class MessageTemplates {

		public static final String NON_CUST_MENU = "menu-non-customer";

		public static final String CUST_MENU = "menu-customer";

		public static final String GREETINGS_CUST_MENU = "greetings-customer-menu";
		
		public static final String GREETINGS_NON_CUST_MENU = "greetings-non-customer-menu";

		public static final String SELECT_AGAIN_CUST = "select-again-cust-menu";
		
		public static final String SELECT_AGAIN_NON_CUST = "select-again-non-cust-menu";
		
		public static final String STATUS_NEED_ANY_HELP = "status-need-any-help";
		
		public static final String STATUS_UNAVAILABLE = "status-unavailable";
		
		public static final String RATE_NEED_ANY_HELP = "rate-need-any-help";
		
		public static final String VALID_RATE_NEED_ANY_HELP = "valid-rate-need-any-help";

		public static final String RATE_UNAVAILABLE = "rate-unavailable";
		
		public static final String RATE_UNAVAILABLE_ASK_COUNTRY = "rate-unavailable-country";
		
		public static final String SELECT_CURRENCY_FOR_COUNTRY = "select-currency-for-country";
		
		public static final String RATE_INVALID = "rate-invalid";
		
		public static final String COUNTRY_INVALID = "country-invalid";
		
		public static final String COUNTRY_RATE_UNAVAILABLE = "country-rate-unavailable";
		
		public static final String COUNTRY_RATE_INVALID = "country-rate-invalid";
		
		public static final String JUST_ASK_COUNTRY = "just-ask-country";
		
		public static final String NEED_ANY_HELP = "need-any-help";

		public static final String AGENT = "redirect-to-agent";

		public static final String CIVIL_ID = "Please enter your Civil Id.";

		public static final String THANKS_CIVIL_ID = "Thank You for sharing your Civil Id.";

		public static final String UPDATE_PHONE_NUMBER = "no-link-found";

		public static final String WELCOME_REGISTER = "register";
		
		public static final String NEW_REGISTER = "new-register";

		public static final String FINAL_EXIT = "final-exit";

		public static final String RECEIPT_UNAVAILABLE = "receipt-unavailable";		
		
		public static final String RECEIPT_NEED_ANY_HELP = "receipt-need-any-help";	

	}

}
