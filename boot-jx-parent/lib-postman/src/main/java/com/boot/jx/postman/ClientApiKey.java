package com.boot.jx.postman;

import java.io.Serializable;

import com.boot.utils.Random;
import com.boot.utils.UniqueID;
import com.fasterxml.jackson.annotation.JsonView;

public interface ClientApiKey extends Serializable {

    @JsonView(PMEnvironment.OneTimeVisibleProperty.class)
    public String getKey();

    public String getKeyName();

    public String getKeyVersion();

    public static String generateApiKey() {
	return String.format("%s%s", UniqueID.generateString(), Random.randomAlphaNumeric(10));
    }

}
