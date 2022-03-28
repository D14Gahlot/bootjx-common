package com.boot.jx.postman.fb;

import com.boot.utils.JsonPath;

public class FacebookConstants {

    public static class InBoundWrapperPaths {
	public static final JsonPath STORY_ID = new JsonPath("story/id");
	public static final JsonPath STORY_URL = new JsonPath("story/url");
    }

    public static class OutBoundWrapperPaths {

    }

    public static final String D360_API_KEY = "D360-API-KEY";
    public static final String BASE_URL = "https://waba.360dialog.io";

    public static String MEDIA_URL(String mediaId) {
	return BASE_URL + "/v1/media/" + mediaId;
    }
}
