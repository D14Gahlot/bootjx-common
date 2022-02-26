package com.boot.jx.postman;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonView;

public interface ClientApp extends Serializable {

    public static final String APP_TYPE_WEBHOOK = "WEBHOOK";
    public static final String APP_TYPE_AGENT = "AGENT";
    public static final String APP_TYPE_BOT = "BOT";

    public String getId();

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    public String getKey();

    public String getKeyName();

    public String getQueue();

    public String getKeyVersion();

    public String getAppType();

    public String getWebhook();

    public String getForward();

    public Map<String, Object> getProps();

    public Map<String, Object> props();

    @JsonView(PMEnvironment.ProtectedProperty.class)
    public Map<String, Object> getSecret();

    public Map<String, Object> secret();

}
