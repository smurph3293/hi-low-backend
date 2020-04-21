package com.amazonaws.handler;

import com.amazonaws.config.BetComponent;
import com.amazonaws.config.DaggerBetComponent;
import com.amazonaws.dao.BetDao;
import com.amazonaws.model.BetPage;
import com.amazonaws.model.response.GatewayResponse;
import com.amazonaws.model.response.GetBetsResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import javax.inject.Inject;

public class GetBetsHandler implements BetRequestStreamHandler {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    BetDao betDao;
    private final BetComponent betComponent;

    public GetBetsHandler() {
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
        final JsonNode queryParameterMap = event.findValue("queryParameters");
        final String exclusiveStartKeyQueryParameter = Optional.ofNullable(queryParameterMap)
                .map(mapNode -> mapNode.get("exclusive_start_key").asText())
                .orElse(null);

        BetPage page = betDao.getBets(exclusiveStartKeyQueryParameter);
        //TODO handle exceptions
        objectMapper.writeValue(output, new GatewayResponse<>(
                objectMapper.writeValueAsString(
                        new GetBetsResponse(page.getLastEvaluatedKey(), page.getBets())),
                APPLICATION_JSON, SC_OK));
    }
}
