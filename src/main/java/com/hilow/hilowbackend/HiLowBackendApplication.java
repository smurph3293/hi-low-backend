package com.hilow.hilowbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HiLowBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(HiLowBackendApplication.class, args);
	}

}
