package com.hilow.hilowbackend;

import com.hilow.hilowbackend.controller.BetController;
import com.hilow.hilowbackend.controller.CommentController;
import com.hilow.hilowbackend.controller.UserController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
//@EnableJpaAuditing
@Import({ BetController.class, CommentController.class, UserController.class})
public class HiLowBackendApplication {
	// silence console logging
	@Value("${logging.level.root:OFF}")
	String message = "";

	public static void main(String[] args) {
		SpringApplication.run(HiLowBackendApplication.class, args);
	}

}
