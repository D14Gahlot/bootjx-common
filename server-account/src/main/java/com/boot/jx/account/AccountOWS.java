package com.boot.jx.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * The Class WebApplication.
 * 
 * @EnableTransactionManagement
 * @EnableCaching
 */
@ServletComponentScan
@SpringBootApplication
@ComponentScan("com.boot.jx")
@EnableAsync(proxyTargetClass = true)
@EnableCaching
@EnableMongoRepositories("com.boot.jx")
public class AccountOWS extends SpringBootServletInitializer {

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
	SpringApplication.run(AccountOWS.class, args);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.boot.web.support.SpringBootServletInitializer#configure(
     * org.springframework.boot.builder.SpringApplicationBuilder)
     */
    protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
	return applicationBuilder.sources(AccountOWS.class);
    }

    public void run(String... args) {
	System.out.println("\nfindById(1L)");
	System.out.println("\nfindByName('Node')");
    }

}
