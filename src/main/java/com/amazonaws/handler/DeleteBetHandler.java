package com.amazonaws.handler;

import com.amazonaws.config.BetComponent;
import com.amazonaws.config.DaggerBetComponent;
import com.amazonaws.dao.BetDao;
import com.amazonaws.exception.BetDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.model.response.ErrorMessage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.inject.Inject;

public class DeleteBetHandler implements BetRequestStreamHandler {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    BetDao betDao;
    private final BetComponent betComponent;

    public DeleteBetHandler() {
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

        final JsonNode pathParameterMap = event.findValue("pathParameters");
        final String betXref = Optional.ofNullable(pathParameterMap).map(mapNode -> mapNode.get("betXref"))
                .map(JsonNode::asText).orElse(null);

        if (isNullOrEmpty(betXref)) {
            objectMapper.writeValue(output, new GatewayResponse<>(objectMapper.writeValueAsString(BET_XREF_WAS_NOT_SET),
                    APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        try {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(betDao.deleteBet(betXref)), APPLICATION_JSON, SC_OK));
        } catch (BetDoesNotExistException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(new ErrorMessage(e.getMessage(), SC_NOT_FOUND)),
                            APPLICATION_JSON, SC_NOT_FOUND));
        } catch (UnableToDeleteException e) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(new ErrorMessage(e.getMessage(), SC_CONFLICT)),
                            APPLICATION_JSON, SC_CONFLICT));
        }
    }
}
