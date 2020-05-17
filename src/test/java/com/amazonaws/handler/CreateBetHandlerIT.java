package com.amazonaws.handler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.TestContext;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

public class CreateBetHandlerIT extends BetHandlerTestBase {

    private CreateBetHandler sut = new CreateBetHandler();
    private GetBetHandler getBet = new GetBetHandler();
    private UpdateBetHandler updateBet = new UpdateBetHandler();

    @Test
    public void handleRequest_whenCreateBetInputStreamOk_puts200InOutputStream() throws IOException {
        Context ctxt = TestContext.builder().build();
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        String input = "{\"body\": \"{\\\"creatorId\\\": \\\"foo\\\", \\\"preTaxAmount\\\": 3, \\\"postTaxAmount\\\": 10}\"}";

        sut.handleRequest(new ByteArrayInputStream(input.getBytes()), os, ctxt);
        Item outputWrapper = Item.fromJSON(os.toString());
        assertTrue(outputWrapper.hasAttribute("headers"));
        Map<String, Object> headers = outputWrapper.getMap("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertTrue(outputWrapper.hasAttribute("statusCode"));
        assertEquals(201, outputWrapper.getInt("statusCode"));
        assertTrue(outputWrapper.hasAttribute("body"));
        String bodyString = outputWrapper.getString("body");
        assertNotNull(bodyString);
        Item body = Item.fromJSON(bodyString);
        verifyBetItem(body, 1, "3");

        //now that we verified the created bet, lets see if we can get it anew
        os = new ByteArrayOutputStream();
        String betId = body.getString("betId");

        getBet.handleRequest(new ByteArrayInputStream(("{\"pathParameters\": { \"bet_id\": \"" + betId + "\"}}").getBytes()), os, ctxt);

        outputWrapper = Item.fromJSON(os.toString());
        assertTrue(outputWrapper.hasAttribute("headers"));
        headers = outputWrapper.getMap("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertTrue(outputWrapper.hasAttribute("statusCode"));
        assertEquals(200, outputWrapper.getInt("statusCode"));
        assertTrue(outputWrapper.hasAttribute("body"));
        bodyString = outputWrapper.getString("body");
        assertNotNull(bodyString);
        body = Item.fromJSON(bodyString);
        verifyBetItem(body, 1, "3");

        //update the bet with invalid arguments (try to change the version from 1 to 2 and the preTaxAmount from 3 to 4)
        os = new ByteArrayOutputStream();
        updateBet.handleRequest(
                new ByteArrayInputStream(("{\"pathParameters\": { \"bet_id\": \"" + betId + "\"}, "
                        + "\"body\": \"{\\\"creatorId\\\": \\\"foo\\\", \\\"preTaxAmount\\\": 4, \\\"postTaxAmount\\\": 10, \\\"version\\\": 2}\"}").getBytes()),
                os, ctxt);
        assertTrue(os.toString().contains("409")); //SC_CONFLICT

        //update the bet with invalid arguments (try to change the pretax amount from 3 to 4)
        os = new ByteArrayOutputStream();
        updateBet.handleRequest(
                new ByteArrayInputStream(("{\"pathParameters\": { \"bet_id\": \"" + betId + "\"}, "
                        + "\"body\": \"{\\\"creatorId\\\": \\\"foo\\\", \\\"preTaxAmount\\\": 4, \\\"postTaxAmount\\\": 10, \\\"version\\\": 1}\"}").getBytes()),
                os, ctxt);
        outputWrapper = Item.fromJSON(os.toString());
        assertTrue(outputWrapper.hasAttribute("headers"));
        headers = outputWrapper.getMap("headers");
        assertNotNull(headers);
        assertEquals(1, headers.size());
        assertTrue(headers.containsKey("Content-Type"));
        assertEquals("application/json", headers.get("Content-Type"));
        assertTrue(outputWrapper.hasAttribute("statusCode"));
        assertEquals(200, outputWrapper.getInt("statusCode"));
        assertTrue(outputWrapper.hasAttribute("body"));
        bodyString = outputWrapper.getString("body");
        assertNotNull(bodyString);
        body = Item.fromJSON(bodyString);
        verifyBetItem(body, 2, "4");

        assertTrue(os.toString().contains("200")); //SC_OK
    }

    private void verifyBetItem(Item body, long expectedVersion, String expectedPreTaxAmount) {
        assertTrue(body.hasAttribute("betId"));
        String betId = body.getString("betId");
        assertNotNull(betId);
        assertTrue(betId.contains("-"));
        assertTrue(body.hasAttribute("creatorId"));
        String creatorId = body.getString("creatorId");
        assertEquals("foo", creatorId);
        assertTrue(body.hasAttribute("preTaxAmount"));
        BigDecimal preTaxAmount = body.getNumber("preTaxAmount");
        assertEquals(new BigDecimal(expectedPreTaxAmount), preTaxAmount);
        assertTrue(body.hasAttribute("postTaxAmount"));
        BigDecimal postTaxAmount = body.getNumber("postTaxAmount");
        assertEquals(new BigDecimal("10"), postTaxAmount);
        assertTrue(body.hasAttribute("version"));
        long version = body.getLong("version");
        assertEquals(expectedVersion, version);
    }
}
