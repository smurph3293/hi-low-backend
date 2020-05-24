package com.hilow.hilowbackend;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hilow.hilowbackend.model.Bet;
import com.hilow.hilowbackend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
public class HiLowBackendApplicationTests {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.registerModule(new JavaTimeModule());

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void canCreateBet() throws Exception {
		User user1 = new User();
		user1.setUserName("user1");
		MvcResult userResult = this.mockMvc.perform(
				post("/users")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody(user1)))
				//.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.userName").value("user1"))
				.andReturn();
		User userEntity = parseResponse(userResult, User.class);
		Bet bet1 = new Bet();
		bet1.setTitle("bet1");
		bet1.setCreator(userEntity);
		//bet1.setCommissionerXref(user1);
		bet1.setConditionsDeadline(new Date());
		bet1.setPunishmentDeadline(new Date());
		this.mockMvc.perform(
				post("/bets")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody(bet1)))
				//.andDo(print())
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("bet1"))
				.andExpect(jsonPath("$.creator.userName").value("user1"));
	}

	@Test
	public void canGetCreatedBet() throws Exception {
		this.mockMvc.perform(
				get("/bets"))
				//.andDo(print())
				.andExpect(status().isOk());
	}

	public static String requestBody(Object request) {
		try {
			return MAPPER.writeValueAsString(request);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T parseResponse(MvcResult result, Class<T> responseClass) {
		try {
			String contentAsString = result.getResponse().getContentAsString();
			return MAPPER.readValue(contentAsString, responseClass);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
