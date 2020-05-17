package com.amazonaws.handler;

import com.amazonaws.services.lambda.runtime.TestContext;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class DeleteBetHandlerTest {
    private DeleteBetHandler sut = new DeleteBetHandler();

    @Test
    public void handleRequest_whenDeleteBetInputStreamEmpty_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        sut.handleRequest(new ByteArrayInputStream(new byte[0]), os, TestContext.builder().build());
        assertTrue(os.toString().contains("Invalid JSON"));
        assertTrue(os.toString().contains("400"));
    }

    @Test
    @Ignore
    public void handleRequest_whenDeleteBetInputStreamHasNoMappedBetIdPathParam_puts400InOutputStream() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String input = "{\"pathParameters\": { }}";
        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, TestContext.builder().build());
        assertTrue(os.toString().contains("bet_id was not set"));
        assertTrue(os.toString().contains("400"));
    }
}
