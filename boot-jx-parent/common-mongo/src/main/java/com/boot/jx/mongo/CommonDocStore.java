package com.boot.jx.mongo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.convert.MongoConverter;

public class CommonDocStore {

    @Autowired
    protected MongoConverter mongoConverter;

    @Autowired
    protected MongoTemplate mongoTemplate;

}
