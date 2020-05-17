package com.amazonaws.handler;

import com.amazonaws.config.BetComponent;
import com.amazonaws.config.DaggerBetComponent;
import com.amazonaws.dao.BetDao;
import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.request.BetRequest;
import com.amazonaws.model.response.ErrorMessage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.inject.Inject;

public class CreateBetHandler implements BetRequestStreamHandler {
   private static final ErrorMessage REQUIRE_CREATOR_XREF_ERROR = new ErrorMessage("Require creatorXref to create an bet", SC_BAD_REQUEST);

   @Inject
   ObjectMapper objectMapper;
   @Inject
   BetDao betDao;

   private final BetComponent betComponent;

   public CreateBetHandler() {
       betComponent = DaggerBetComponent.builder().build();
       betComponent.inject(this);
   }

   @Override
   public void handleRequest(InputStream input, OutputStream output, Context context) throws IOException {
       final JsonNode event;
       try {
           event = objectMapper.readTree(input);
       } catch (JsonMappingException e) {
           writeInvalidJsonInStreamResponse(objectMapper, output, e.getMessage());
           return;
       }
       if (event == null) {
           writeInvalidJsonInStreamResponse(objectMapper, output, "event was null");
           return;
       }
       JsonNode createBetRequestBody = event.findValue("body");
       if (createBetRequestBody == null) {
           objectMapper.writeValue(output, new GatewayResponse<>(
                   objectMapper.writeValueAsString(new ErrorMessage("Body was null", SC_BAD_REQUEST)), APPLICATION_JSON, SC_BAD_REQUEST));
           return;
       }
       final BetRequest request;
       try {
           request = objectMapper.treeToValue(objectMapper.readTree(createBetRequestBody.asText()),
                   BetRequest.class);
       } catch (JsonParseException | JsonMappingException e) {
           objectMapper.writeValue(output,
                   new GatewayResponse<>(objectMapper.writeValueAsString(new ErrorMessage(
                           "Invalid JSON in body: " + e.getMessage(), SC_BAD_REQUEST)),
                           APPLICATION_JSON, SC_BAD_REQUEST));
           return;
       }

       if (request == null) {
           objectMapper.writeValue(output,
                   new GatewayResponse<>(objectMapper.writeValueAsString(REQUEST_WAS_NULL_ERROR),
                           APPLICATION_JSON, SC_BAD_REQUEST));
           return;
       }
       if (isNullOrEmpty(request.getCreatorXref())) {
           objectMapper.writeValue(output,
                   new GatewayResponse<>(
                           objectMapper.writeValueAsString(REQUIRE_CREATOR_XREF_ERROR),
                           APPLICATION_JSON, SC_BAD_REQUEST));
           return;
       }
       try {
           final Bet bet = betDao.createBet(request);
           objectMapper.writeValue(output, new GatewayResponse<>(objectMapper.writeValueAsString(bet),
                   APPLICATION_JSON, SC_CREATED));
       } catch (CouldNotCreateBetException e) {
           objectMapper.writeValue(output, new GatewayResponse<>(
                   objectMapper.writeValueAsString(
                           new ErrorMessage(e.getMessage(), SC_INTERNAL_SERVER_ERROR)),
                   APPLICATION_JSON, SC_INTERNAL_SERVER_ERROR));
       }
   }
}
