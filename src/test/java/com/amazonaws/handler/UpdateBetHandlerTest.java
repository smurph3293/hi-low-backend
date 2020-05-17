package com.amazonaws.handler;

import com.amazonaws.services.lambda.runtime.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class UpdateBetHandlerTest {
    private UpdateBetHandler sut = new UpdateBetHandler();

    @Test
    public void handleRequest_whenUpdateBetInputStreamEmpty_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        sut.handleRequest(new ByteArrayInputStream(new byte[0]), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Invalid JSON"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamHasNoMappedBetIdPathParam_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { }}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("bet_id was not set"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamHasNoBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Body was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamHasNullBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }, \"body\": \"null\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Request was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamHasWrongTypeForBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }, \"body\": \"1\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Invalid JSON"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamHasEmptyBodyDict_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }, \"body\": \"{}\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("creatorId was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamOnlyHasCustomer_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }, \"body\": \"{\\\"creatorId\\\": \\\"customer\\\"}\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("preTaxAmount was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenUpdateBetInputStreamDoesNotHavePostTaxAmount_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { \"bet_id\" : \"a\" }, \"body\": \"{\\\"creatorId\\\": \\\"customer\\\", \\\"preTaxAmount\\\": 1}\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("postTaxAmount was null"));
        assertTrue(os.toString().contains("400"));
    }
}
