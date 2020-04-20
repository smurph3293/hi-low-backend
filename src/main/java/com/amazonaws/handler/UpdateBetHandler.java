package com.amazonaws.handler;

import com.amazonaws.config.DaggerOrderComponent;
import com.amazonaws.config.OrderComponent;
import com.amazonaws.dao.OrderDao;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Order;
import com.amazonaws.model.request.UpdateOrderRequest;
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

public class UpdateBetHandler implements OrderRequestStreamHandler {
    @Inject
    ObjectMapper objectMapper;
    @Inject
    OrderDao orderDao;
    private final OrderComponent orderComponent;

    public UpdateBetHandler() {
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

        JsonNode updateOrderRequestBody = event.findValue("body");
        if (updateOrderRequestBody == null) {
            objectMapper.writeValue(output,
                    new GatewayResponse<>(
                            objectMapper.writeValueAsString(
                                    new ErrorMessage("Body was null",
                                            SC_BAD_REQUEST)),
                            APPLICATION_JSON, SC_BAD_REQUEST));
            return;
        }

        final UpdateOrderRequest request;
        try {
            request = objectMapper.readValue(
                    updateOrderRequestBody.asText(), UpdateOrderRequest.class);
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
            Order updatedOrder = orderDao.updateOrder(
                    Order.builder().orderId(orderId)
                            .customerId(request.getCustomerId())
                            .version(request.getVersion())
                            .preTaxAmount(request.getPreTaxAmount())
                            .postTaxAmount(request.getPostTaxAmount())
                            .build());
            objectMapper.writeValue(output, new GatewayResponse<>(
                    objectMapper.writeValueAsString(updatedOrder),
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
