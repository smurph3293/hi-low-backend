package com.amazonaws.handler;

import com.amazonaws.config.DaggerOrderComponent;
import com.amazonaws.config.OrderComponent;
import com.amazonaws.dao.OrderDao;
import com.amazonaws.exception.OrderDoesNotExistException;
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

public class DeleteBetHandler implements OrderRequestStreamHandler {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    OrderDao orderDao;
    private final OrderComponent orderComponent;

    public DeleteBetHandler() {
        orderComponent = DaggerOrderComponent.builder().build();
        orderComponent.inject(this);
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
        final String orderId = Optional.ofNullable(pathParameterMap)
                .map(mapNode -> mapNode.get("order_id"))
                .map(JsonNode::asText)
                .orElse(null);

        if (isNullOrEmpty(orderId)) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(ORDER_ID_WAS_NOT_SET),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }
        try {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(orderDao.deleteOrder(orderId)),
                    APPLICATION_JSON, SC_OK));
        } catch (OrderDoesNotExistException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(new ErrorMessage(e.getMessage(),
                            SC_NOT_FOUND)),
                    APPLICATION_JSON, SC_NOT_FOUND));
        } catch (UnableToDeleteException e) {
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(new ErrorMessage(e.getMessage(),
                            SC_CONFLICT)),
                    APPLICATION_JSON, SC_CONFLICT));
        }
    }
}

