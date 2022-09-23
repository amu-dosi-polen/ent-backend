package com.amu.ent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class EntAmuBackendApplication extends SpringBootServletInitializer   {

	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    	setRegisterErrorPageFilter(false);
        return application.sources(EntAmuBackendApplication.class);
    }

	public static void main(String[] args) {
		SpringApplication.run(EntAmuBackendApplication.class, args);
	}

}
