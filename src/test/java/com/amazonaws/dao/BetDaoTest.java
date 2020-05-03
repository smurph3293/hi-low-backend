package com.amazonaws.dao;

import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.exception.BetDoesNotExistException;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.BetPage;
import com.amazonaws.model.request.CreateBetRequest;
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
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class BetDaoTest {
    private static final String BET_ID = "some bet id";
    private DynamoDbClient dynamoDb = mock(DynamoDbClient.class);
    private BetDao sut = new BetDao(dynamoDb, "table_name", 10);

    @Test(expected = IllegalArgumentException.class)
    public void createBet_whenRequestNull_throwsIllegalArgumentException() {
        sut.createBet(null);
    }

    //test CRUD when table does not exist
    @Test(expected = TableDoesNotExistException.class)
    public void createBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).putItem(any(PutItemRequest.class));
        sut.createBet(CreateBetRequest.builder()
                .preTaxAmount(100L).postTaxAmount(109L).creatorId("me").build());
    }

    @Test(expected = TableDoesNotExistException.class)
    public void getBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = TableDoesNotExistException.class)
    public void getBets_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).scan(any(ScanRequest.class));
        sut.getBets(any());
    }

    @Test
    public void getBets_whenTableEmpty_returnsEmptyPage() {
        doReturn(ScanResponse.builder()
                .items(new ArrayList<>())
                .lastEvaluatedKey(null)
                .build()).when(dynamoDb).scan(any(ScanRequest.class));
        BetPage page = sut.getBets(any());
        assertNotNull(page);
        assertNotNull(page.getBets());
        assertTrue(page.getBets().isEmpty());
        assertNull(page.getLastEvaluatedKey());
    }

    @Test(expected = IllegalStateException.class)
    public void getBets_whenTableNotEmpty_butLastEvaluatedKeyHasBetIdSetToWrongType_throwsIllegalStateException() {
        doReturn(ScanResponse.builder()
                .items(Collections.singletonList(new HashMap<>()))
                .lastEvaluatedKey(Collections.singletonMap("betId", AttributeValue.builder().nul(true).build()))
                .build()
        ).when(dynamoDb).scan(any(ScanRequest.class));
        sut.getBets(any());
    }

    @Test(expected = IllegalStateException.class)
    public void getBets_whenTableNotEmpty_butLastEvaluatedKeyHasBetIdSetToUnsetAv_throwsIllegalStateException() {
        doReturn(ScanResponse.builder()
                .items(Collections.singletonList(new HashMap<>()))
                .lastEvaluatedKey(Collections.singletonMap("betId", AttributeValue.builder().build()))
                .build()
        ).when(dynamoDb).scan(any(ScanRequest.class));
        sut.getBets(any());
    }

    @Test(expected = IllegalStateException.class)
    public void getBets_whenTableNotEmpty_butLastEvaluatedKeyHasBetIdSetToEmptyString_throwsIllegalStateException() {
        doReturn(ScanResponse.builder()
                .items(Collections.singletonList(new HashMap<>()))
                .lastEvaluatedKey(Collections.singletonMap("betId", AttributeValue.builder().s("").build()))
                .build()
        ).when(dynamoDb).scan(any(ScanRequest.class));
        sut.getBets(any());
    }

    @Test
    public void getBets_whenTableNotEmpty_returnsPage() {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("betId", AttributeValue.builder().s("d").build());
        item.put("creatorId", AttributeValue.builder().s("d").build());
        item.put("preTaxAmount", AttributeValue.builder().n("1").build());
        item.put("postTaxAmount", AttributeValue.builder().n("10").build());
        item.put("version", AttributeValue.builder().n("1").build());
        doReturn(ScanResponse.builder()
                .items(Collections.singletonList(item))
                .lastEvaluatedKey(Collections.singletonMap("betId", AttributeValue.builder().s("d").build()))
                .build()).when(dynamoDb).scan(any(ScanRequest.class));
        sut.getBets(any());
    }

    @Test(expected = TableDoesNotExistException.class)
    public void updateBet_whenTableDoesNotExistOnLoadItem_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).updateItem(any(UpdateItemRequest.class));
        sut.updateBet(Bet.builder()
                .betId(BET_ID)
                .creatorId("customer")
                .preTaxAmount(BigDecimal.ONE)
                .postTaxAmount(BigDecimal.TEN)
                .version(0L)
                .build());
    }

    @Test(expected = TableDoesNotExistException.class)
    public void updateBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).updateItem(any(UpdateItemRequest.class));
        sut.updateBet(Bet.builder()
                .betId(BET_ID)
                .creatorId("customer")
                .preTaxAmount(BigDecimal.ONE)
                .postTaxAmount(BigDecimal.TEN)
                .version(1L)
                .build());
    }

    @Test(expected = TableDoesNotExistException.class)
    public void deleteBet_whenTableDoesNotExist_throwsTableDoesNotExistException() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betId", AttributeValue.builder().s(BET_ID).build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(GetItemResponse.builder().item(betItem).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        doThrow(ResourceNotFoundException.builder().build()).when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_ID);
    }

    //conditional failure tests
    @Test(expected = CouldNotCreateBetException.class)
    public void createBet_whenAlreadyExists_throwsCouldNotCreateBetException() {
        doThrow(ConditionalCheckFailedException.builder().build()).when(dynamoDb).putItem(any(PutItemRequest.class));
        sut.createBet(CreateBetRequest.builder()
                .preTaxAmount(100L).postTaxAmount(109L).creatorId("me").build());
    }

    @Test(expected = UnableToDeleteException.class)
    public void deleteBet_whenVersionMismatch_throwsUnableToDeleteException() {
        doThrow(ConditionalCheckFailedException.builder().build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void deleteBet_whenDeleteItemReturnsNull_throwsIllegalStateException() {
        doReturn(null).when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void deleteBet_whenDeleteItemReturnsNoAttributes_throwsIllegalStateException() {
        doReturn(DeleteItemResponse.builder().build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void deleteBet_whenDeleteItemReturnsEmptyAttributes_throwsIllegalStateException() {
        doReturn(DeleteItemResponse.builder().attributes(new HashMap<>()).build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        sut.deleteBet(BET_ID);
    }

    @Test
    public void deleteBet_whenDeleteItemReturnsOkBetItem_returnsDeletedBet() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betId", AttributeValue.builder().s(BET_ID).build());
        betItem.put("creatorId", AttributeValue.builder().s("customer").build());
        betItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        betItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(DeleteItemResponse.builder().attributes(betItem).build())
                .when(dynamoDb).deleteItem(any(DeleteItemRequest.class));
        Bet deleted = sut.deleteBet(BET_ID);
        assertNotNull(deleted);
    }

    @Test(expected = UnableToUpdateException.class)
    public void updateBet_whenVersionMismatch_throwsUnableToUpdateException() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betId", AttributeValue.builder().s(BET_ID).build());
        betItem.put("version", AttributeValue.builder().n("0").build());
        doReturn(GetItemResponse.builder().item(betItem).build())
                .when(dynamoDb).getItem(any(GetItemRequest.class));
        Bet postBet = new Bet();
        postBet.setBetId(BET_ID);
        postBet.setVersion(0L);
        postBet.setCreatorId("customer");
        postBet.setPreTaxAmount(BigDecimal.ONE);
        postBet.setPostTaxAmount(BigDecimal.TEN);
        doThrow(ConditionalCheckFailedException.builder().build())
                .when(dynamoDb).updateItem(any(UpdateItemRequest.class));
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenBetIsNull_throwsIllegalArgumentException() {
        sut.updateBet(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenAllNotSet_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenBetIdSetButEmpty_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        postBet.setBetId("");
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenCreatorIdSetButEmpty_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        postBet.setBetId("s");
        postBet.setCreatorId("");
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenPreTaxAmountNull_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        postBet.setBetId("s");
        postBet.setCreatorId("c");
        postBet.setPreTaxAmount(null);
        postBet.setPostTaxAmount(BigDecimal.TEN);
        postBet.setVersion(1L);
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenPostTaxAmountNull_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        postBet.setBetId("s");
        postBet.setCreatorId("c");
        postBet.setPreTaxAmount(BigDecimal.ONE);
        postBet.setPostTaxAmount(null);
        postBet.setVersion(1L);
        sut.updateBet(postBet);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBet_whenVersionNull_throwsIllegalArgumentException() {
        Bet postBet = new Bet();
        postBet.setBetId("s");
        postBet.setCreatorId("c");
        postBet.setPreTaxAmount(BigDecimal.ONE);
        postBet.setPostTaxAmount(BigDecimal.TEN);
        postBet.setVersion(null);
        sut.updateBet(postBet);
    }

    @Test
    public void updateBet_whenAllSet_returnsUpdate() {
        Map<String, AttributeValue> createdItem = new HashMap<>();
        createdItem.put("betId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        createdItem.put("creatorId", AttributeValue.builder().s("customer").build());
        createdItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        createdItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        createdItem.put("version", AttributeValue.builder().n("1").build());

        doReturn(UpdateItemResponse.builder().attributes(createdItem).build())
                .when(dynamoDb).updateItem(any(UpdateItemRequest.class));

        Bet postBet = new Bet();
        postBet.setBetId(createdItem.get("betId").s());
        postBet.setCreatorId("customer");
        postBet.setPreTaxAmount(BigDecimal.ONE);
        postBet.setPostTaxAmount(BigDecimal.TEN);
        postBet.setVersion(1L);
        Bet bet = sut.updateBet(postBet);
        assertEquals(createdItem.get("betId").s(), bet.getBetId());
    }

    //positive functional tests
    @Test
    public void createBet_whenBetDoesNotExist_createsBetWithPopulatedBetId() {
        Map<String, AttributeValue> createdItem = new HashMap<>();
        createdItem.put("betId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        createdItem.put("creatorId", AttributeValue.builder().s("customer").build());
        createdItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        createdItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        createdItem.put("version", AttributeValue.builder().n("1").build());
        doReturn(PutItemResponse.builder().attributes(createdItem).build()).when(dynamoDb).putItem(any(PutItemRequest.class));

        Bet bet = sut.createBet(CreateBetRequest.builder()
                .creatorId("customer")
                .preTaxAmount(1L)
                .postTaxAmount(10L).build());
        assertNotNull(bet.getVersion());
        //for a new item, object mapper sets version to 1
        assertEquals(1L, bet.getVersion().longValue());
        assertEquals("customer", bet.getCreatorId());
        assertEquals(BigDecimal.ONE, bet.getPreTaxAmount());
        assertEquals(BigDecimal.TEN, bet.getPostTaxAmount());
        assertNotNull(bet.getBetId());
        assertNotNull(UUID.fromString(bet.getBetId()));
    }

    @Test(expected = BetDoesNotExistException.class)
    public void getBet_whenBetDoesNotExist_throwsBetDoesNotExist() {
        doReturn(GetItemResponse.builder().item(null).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = BetDoesNotExistException.class)
    public void getBet_whenGetItemReturnsEmptyHashMap_throwsIllegalStateException() {
        doReturn(GetItemResponse.builder().item(new HashMap<>()).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithBetIdWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithUnsetBetIdAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithEmptyBetId_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithCreatorIdWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithUnsetCreatorIdAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithEmptyCreatorId_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithPreTaxWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithUnsetPreTaxAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithInvalidPreTax_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithPostTaxWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().nul(true).build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithUnsetPostTaxAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithInvalidPostTax_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithVersionOfWrongType_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().ss("").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithUnsetVersionAV_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test(expected = IllegalStateException.class)
    public void getBet_whenGetItemReturnsHashMapWithInvalidVersion_throwsIllegalStateException() {
        Map<String, AttributeValue> map = new HashMap<>();
        map.put("betId", AttributeValue.builder().s("a").build());
        map.put("creatorId", AttributeValue.builder().s("a").build());
        map.put("preTaxAmount", AttributeValue.builder().n("1").build());
        map.put("postTaxAmount", AttributeValue.builder().n("10").build());
        map.put("version", AttributeValue.builder().n("a").build());
        doReturn(GetItemResponse.builder().item(map).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        sut.getBet(BET_ID);
    }

    @Test
    public void getBet_whenBetExists_returnsBet() {
        Map<String, AttributeValue> betItem = new HashMap<>();
        betItem.put("betId", AttributeValue.builder().s(BET_ID).build());
        betItem.put("version", AttributeValue.builder().n("1").build());
        betItem.put("preTaxAmount", AttributeValue.builder().n("1").build());
        betItem.put("postTaxAmount", AttributeValue.builder().n("10").build());
        betItem.put("creatorId", AttributeValue.builder().s("customer").build());
        doReturn(GetItemResponse.builder().item(betItem).build()).when(dynamoDb).getItem(any(GetItemRequest.class));
        Bet bet = sut.getBet(BET_ID);
        assertEquals(BET_ID, bet.getBetId());
        assertEquals(1L, bet.getVersion().longValue());
        assertEquals(1L, bet.getPreTaxAmount().longValue());
        assertEquals(10L, bet.getPostTaxAmount().longValue());
        assertEquals("customer", bet.getCreatorId());
    }

    //connection dropped corner cases
}
