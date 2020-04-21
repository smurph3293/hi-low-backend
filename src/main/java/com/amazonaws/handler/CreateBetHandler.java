package com.amazonaws.handler;

import com.amazonaws.config.BetComponent;
import com.amazonaws.config.DaggerBetComponent;
import com.amazonaws.dao.BetDao;
import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.request.CreateBetRequest;
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
    private static final ErrorMessage REQUIRE_CUSTOMER_ID_ERROR
            = new ErrorMessage("Require customerId to create an bet", SC_BAD_REQUEST);
    private static final ErrorMessage REQUIRE_PRETAX_AMOUNT_ERROR
            = new ErrorMessage("Require preTaxAmount to create an bet",
            SC_BAD_REQUEST);
    private static final ErrorMessage REQUIRE_POST_TAX_AMOUNT_ERROR
            = new ErrorMessage("Require postTaxAmount to create an bet",
            SC_BAD_REQUEST);

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
    public void handleRequest(InputStream input, OutputStream output,
                              Context context) throws IOException {
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
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Body was null",
                                            SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        final CreateBetRequest request;
        try {
            request = objectMapper.treeToValue(
                    objectMapper.readTree(createBetRequestBody.asText()),
                    CreateBetRequest.class);
        } catch (JsonParseException | JsonMappingException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Invalid JSON in body: "
                                            + e.getMessage(), SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        if (request == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(REQUEST_WAS_NULL_ERROR),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        if (isNullOrEmpty(request.getCustomerId())) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(REQUIRE_CUSTOMER_ID_ERROR),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        if (request.getPreTaxAmount() == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(REQUIRE_PRETAX_AMOUNT_ERROR),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        if (request.getPostTaxAmount() == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(REQUIRE_POST_TAX_AMOUNT_ERROR),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        try {
            final Bet bet = betDao.createBet(request);
            objectMapper.writeValue(output,
                    new GatewayResponse<>(objectMapper.writeValueAsString(bet),
                            APPLICATION_JSON, SC_CREATED)); //TODO redirect with a 303
        } catch (CouldNotCreateBetException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage(e.getMessage(),
                                            SC_INTERNAL_SERVER_ERROR)),
                            APPLICATION_JSON, SC_INTERNAL_SERVER_ERROR));
        }
    }
}
