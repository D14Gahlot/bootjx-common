package com.boot.jx.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import com.boot.jx.admin.model.Agent;
import com.boot.jx.admin.repository.IAgentRepository;

/**
 * The Class WebApplication.
@EnableTransactionManagement
@EnableCaching
 */
@ServletComponentScan
@SpringBootApplication
@ComponentScan("com.boot.jx")
@EnableAsync(proxyTargetClass = true)
@EnableCaching
public class Admin extends SpringBootServletInitializer {

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(Admin.class, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.springframework.boot.web.support.SpringBootServletInitializer#configure(
	 * org.springframework.boot.builder.SpringApplicationBuilder)
	 */
	protected SpringApplicationBuilder configure(SpringApplicationBuilder applicationBuilder) {
		return applicationBuilder.sources(Admin.class);
	}
	
	@Autowired
	IAgentRepository agentRepository;
	
	 public void run(String... args) {
		 	Agent ag = new Agent();
		 	ag.setAgent_name("MeherY");
		 	ag.setAgent_department("TEST");
		 	
		 	agentRepository.save(ag);
	        System.out.println("\nfindAll()");
	        agentRepository.findAll().forEach(x -> System.out.println(x));

	        System.out.println("\nfindById(1L)");
	        //agentRepository.findById(1l).ifPresent(x -> System.out.println(x));

	        System.out.println("\nfindByName('Node')");
	        //agentRepository.findByName("Node").forEach(x -> System.out.println(x));

	    }

}
