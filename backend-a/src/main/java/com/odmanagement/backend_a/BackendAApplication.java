package com.odmanagement.backend_a;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@org.springframework.scheduling.annotation.EnableAsync
public class BackendAApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendAApplication.class, args);
	}

}
