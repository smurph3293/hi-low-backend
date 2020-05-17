package com.amazonaws.dao;

import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.exception.BetDoesNotExistException;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.request.BetRequest;
import org.junit.Ignore;
import org.junit.Test;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BetDaoTest {
    private static final String BET_XREF = "some bet xref";
    private DynamoDbClient dynamoDb = mock(DynamoDbClient.class);
    private BetDao sut = new BetDao(dynamoDb, "table_name", 10);

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void createBet_whenRequestNull_throwsIllegalArgumentException() {
        sut.createBet(null);
    }

    //test CRUD when table does not exist
    @Test(expected = TableDoesNotExistException.class)
    @Ignore
    public void createBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).putItem(any(PutItemRequest.class));
        sut.createBet(BetRequest.builder().creatorXref("me").build());
    }

    @Test(expected = TableDoesNotExistException.class)
    @Ignore
    public void getBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = TableDoesNotExistException.class)
    @Ignore
    public void updateBet_whenTableDoesNotExistOnLoadItem_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).updateItem(any(UpdateItemRequest.class));
        sut.updateBet(BetRequest.builder()
                .xref(BET_XREF)
                .build());
    }

    @Test(expected = TableDoesNotExistException.class)
    @Ignore
    public void updateBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).updateItem(any(UpdateItemRequest.class));
        sut.updateBet(BetRequest.builder()
                .xref(BET_XREF)
                .build());
    }

    @Test(expected = TableDoesNotExistException.class)
    @Ignore
    public void deleteBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betXref", AttributeValue.builder().s(BET_XREF).build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(GetItemResponse.builder().item(betItem).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_XREF);
    }

    //conditional failure tests
    @Test(expected = CouldNotCreateBetException.class)
    @Ignore
    public void createBet_whenAlreadyExists_throwsCouldNotCreateBetException() {
        doThrow(ConditionalCheckFailedException.builder().build()).when(dynamoDb).putItem(any(PutItemRequest.class));
        sut.createBet(BetRequest.builder().creatorXref("me").build());
    }

    @Test(expected = UnableToDeleteException.class)
    @Ignore
    public void deleteBet_whenVersionMismatch_throwsUnableToDeleteException() {
        doThrow(ConditionalCheckFailedException.builder().build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void deleteBet_whenDeleteItemReturnsNull_throwsIllegalStateException() {
        doReturn(null).when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void deleteBet_whenDeleteItemReturnsNoAttributes_throwsIllegalStateException() {
        doReturn(DeleteItemResponse.builder().build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void deleteBet_whenDeleteItemReturnsEmptyAttributes_throwsIllegalStateException() {
        doReturn(DeleteItemResponse.builder().attributes(new HashMap<>()).build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_XREF);
    }

    @Test
    @Ignore
    public void deleteBet_whenDeleteItemReturnsOkBetItem_returnsDeletedBet() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betXref", AttributeValue.builder().s(BET_XREF).build());
        betItem.put("creatorId", AttributeValue.builder().s("customer").build());
        betItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        betItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(DeleteItemResponse.builder().attributes(betItem).build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        Bet deleted = sut.deleteBet(BET_XREF);
        assertNotNull(deleted);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void updateBet_whenBetIsNull_throwsIllegalArgumentException() {
        sut.updateBet(null);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void updateBet_whenAllNotSet_throwsIllegalArgumentException() {
        BetRequest postBet = new BetRequest();
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    @Ignore
    public void updateBet_whenCreatorIdSetButEmpty_throwsIllegalArgumentException() {
        BetRequest postBet = new BetRequest();
        postBet.setXref("s");
        postBet.setCreatorXref("");
        sut.updateBet(postBet);
    }

    @Test
    @Ignore
    public void updateBet_whenAllSet_returnsUpdate() {
        Map<String, AttributeValue> createdItem = new HashMap<>();
        createdItem.put("betXref", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        createdItem.put("creatorId", AttributeValue.builder().s("customer").build());
        createdItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        createdItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        createdItem.put("version", AttributeValue.builder().n("1").build());

        doReturn(UpdateItemResponse.builder().attributes(createdItem).build())
                .when(dynamoDb).updateItem(any(UpdateItemRequest.class));

        BetRequest postBet = new BetRequest();
        postBet.setXref(createdItem.get("betXref").s());
        Bet bet = sut.updateBet(postBet);
        assertEquals(createdItem.get("betXref").s(), bet.getXref());
    }

    //positive functional tests
    @Test
    @Ignore
    public void createBet_whenBetDoesNotExist_createsBetWithPopulatedbetXref() {
        Map<String, AttributeValue> createdItem = new HashMap<>();
        createdItem.put("betXref", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        createdItem.put("creatorId", AttributeValue.builder().s("customer").build());
        createdItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        createdItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        createdItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(PutItemResponse.builder().attributes(createdItem).build()).when(dynamoDb).putItem(any(PutItemRequest.class));

        Bet bet = sut.createBet(BetRequest.builder()
                .creatorXref("customer").build());
        assertNotNull(bet.getVersion());
        //for a new item, object mapper sets version to 1
        assertEquals(1L, bet.getVersion().longValue());
        assertEquals("customer", bet.getCreatorXref());
        assertNotNull(bet.getXref());
        assertNotNull(UUID.fromString(bet.getXref()));
    }

    @Test(expected = BetDoesNotExistException.class)
    @Ignore
    public void getBet_whenBetDoesNotExist_throwsBetDoesNotExist() {
        doReturn(GetItemResponse.builder().item(null).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = BetDoesNotExistException.class)
    @Ignore
    public void getBet_whenGetItemReturnsEmptyHashMap_throwsIllegalStateException() {
        doReturn(GetItemResponse.builder().item(new HashMap<>()).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithbetXrefWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithUnsetbetXrefAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithEmptybetXref_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithCreatorIdWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithUnsetCreatorIdAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithEmptyCreatorId_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithPreTaxWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithUnsetPreTaxAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithInvalidPreTax_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithPostTaxWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithUnsetPostTaxAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithInvalidPostTax_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithVersionOfWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().ss("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithUnsetVersionAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test(expected = IllegalStateException.class)
    @Ignore
    public void getBet_whenGetItemReturnsHashMapWithInvalidVersion_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betXref", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_XREF);
    }

    @Test
    @Ignore
    public void getBet_whenBetExists_returnsBet() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betXref", AttributeValue.builder().s(BET_XREF).build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        betItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        betItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        betItem.put("creatorId", AttributeValue.builder().s("customer").build());
        doReturn(GetItemResponse.builder().item(betItem).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        Bet bet = sut.getBet(BET_XREF);
        assertEquals(BET_XREF, bet.getXref());
        assertEquals(1L, bet.getVersion().longValue());
        assertEquals("customer", bet.getCreatorXref());
    }

    //connection dropped corner cases
}
