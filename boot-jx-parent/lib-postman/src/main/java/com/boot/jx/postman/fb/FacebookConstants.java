package com.boot.jx.postman.fb;

public class FacebookConstants {

    public static class InBoundWrapperPaths {

    }

    public static class OutBoundWrapperPaths {

    }

    public static final String D360_API_KEY = "D360-API-KEY";
    public static final String BASE_URL = "https://waba.360dialog.io";

    public static String MEDIA_URL(String mediaId) {
	return BASE_URL + "/v1/media/" + mediaId;
    }
}
