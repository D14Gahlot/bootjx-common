package com.boot.jx.contak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class OtpService {

    @Autowired
    MongoTemplate mongoTemplate;


}
