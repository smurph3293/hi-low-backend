package com.hilow.hilowbackend;

import com.hilow.hilowbackend.controller.BetController;
import com.hilow.hilowbackend.controller.CommentController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
//@EnableJpaAuditing
@Import({ BetController.class, CommentController.class})
public class HiLowBackendApplication {
	// silence console logging
	@Value("${logging.level.root:OFF}")
	String message = "";

	public static void main(String[] args) {
		SpringApplication.run(HiLowBackendApplication.class, args);
	}

}
