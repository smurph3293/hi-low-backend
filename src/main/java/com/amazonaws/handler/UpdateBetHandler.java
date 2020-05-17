package com.amazonaws.handler;

import com.amazonaws.config.BetComponent;
import com.amazonaws.config.DaggerBetComponent;
import com.amazonaws.dao.BetDao;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToUpdateException;
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

public class UpdateBetHandler implements BetRequestStreamHandler {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    BetDao betDao;
    private final BetComponent betComponent;

    public UpdateBetHandler() {
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
        final JsonNode pathParameterMap = event.findValue("pathParameters");
        final String betXref = Optional.ofNullable(pathParameterMap)
                .map(mapNode -> mapNode.get("betXref"))
                .map(JsonNode::asText)
                .orElse(null);
        if (isNullOrEmpty(betXref)) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(BET_XREF_WAS_NOT_SET),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        JsonNode updateBetRequestBody = event.findValue("body");
        if (updateBetRequestBody == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Body was null",
                                            SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        final BetRequest request;
        try {
            request = objectMapper.readValue(
                    updateBetRequestBody.asText(), BetRequest.class);
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
        try {
            Bet updatedBet = betDao.updateBet(request);
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(updatedBet),
                    APPLICATION_JSON, SC_OK));
        } catch (UnableToUpdateException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_CONFLICT)),
                    APPLICATION_JSON, SC_CONFLICT));
        } catch (TableDoesNotExistException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_BAD_REQUEST)),
                    APPLICATION_JSON, SC_BAD_REQUEST));
        } catch (IllegalArgumentException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_BAD_REQUEST)),
                    APPLICATION_JSON, SC_BAD_REQUEST));
        } catch (IllegalStateException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(
                            new ErrorMessage(e.getMessage(), SC_INTERNAL_SERVER_ERROR)),
                    APPLICATION_JSON, SC_INTERNAL_SERVER_ERROR));
        }
    }
}
