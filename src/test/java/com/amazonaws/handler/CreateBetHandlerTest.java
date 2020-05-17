package com.amazonaws.handler;

import com.amazonaws.services.lambda.runtime.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class CreateBetHandlerTest {
    private CreateBetHandler sut = new CreateBetHandler();

    @Test
    public void handleRequest_whenCreateBetInputStreamEmpty_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        sut.handleRequest(new ByteArrayInputStream(new byte[0]), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Invalid JSON"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    public void handleRequest_whenCreateBetInputStreamHasNoBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Body was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    public void handleRequest_whenCreateBetInputStreamHasNullBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"body\": \"null\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Request was null"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    public void handleRequest_whenCreateBetInputStreamHasWrongTypeForBody_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"body\": \"1\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Invalid JSON"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenCreateBetInputStreamHasEmptyBodyDict_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"body\": \"{}\"}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Require creatorId to create an bet"));
        assertTrue(os.toString().contains("400"));
    }
}
