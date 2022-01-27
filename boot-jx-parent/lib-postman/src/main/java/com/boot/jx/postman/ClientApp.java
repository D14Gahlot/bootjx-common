package com.boot.jx.postman;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonView;

public interface ClientApp extends Serializable {

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    public String getKey();

    public String getKeyName();

    public String getQueue();

    public String getKeyVersion();

    public String getAppType();

    public String getWebhook();

}
