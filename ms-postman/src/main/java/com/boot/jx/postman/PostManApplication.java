package com.boot.jx.postman;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import com.cloudinary.Cloudinary;

/**
 * The Class PostManApplication.
 */
@SpringBootApplication
@ComponentScan(basePackages = { "com.boot.jx" })
@EnableAsync(proxyTargetClass = true)
public class PostManApplication {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(PostManApplication.class, args);
	}


}
