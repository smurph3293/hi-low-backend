package com.amazonaws.dao;

import com.amazonaws.exception.BetDoesNotExistException;
import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.BetPage;
import com.amazonaws.model.request.CreateBetRequest;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemResponse;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ReturnValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class BetDao {

    private static final String UPDATE_EXPRESSION
            = "SET customerId = :cid, preTaxAmount = :pre, postTaxAmount = :post ADD version :o";
    private static final String BET_ID = "betId";
    private static final String PRE_TAX_AMOUNT_WAS_NULL = "preTaxAmount was null";
    private static final String POST_TAX_AMOUNT_WAS_NULL = "postTaxAmount was null";
    private static final String VERSION_WAS_NULL = "version was null";

    private final String tableName;
    private final DynamoDbClient dynamoDb;
    private final int pageSize;

    /**
     * Constructs an BetDao.
     * @param dynamoDb dynamodb client
     * @param tableName name of table to use for bets
     * @param pageSize size of pages for getBets
     */
    public BetDao(final DynamoDbClient dynamoDb, final String tableName,
                    final int pageSize) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
        this.pageSize = pageSize;
    }

    /**
     * Returns an bet or throws if the bet does not exist.
     * @param betId id of bet to get
     * @return the bet if it exists
     * @throws BetDoesNotExistException if the bet does not exist
     */
    public Bet getBet(final String betId) {
        try {
            return Optional.ofNullable(
                    dynamoDb.getItem(GetItemRequest.builder()
                            .tableName(tableName)
                            .key(Collections.singletonMap(BET_ID,
                                    AttributeValue.builder().s(betId).build()))
                            .build()))
                    .map(GetItemResponse::item)
                    .map(this::convert)
                    .orElseThrow(() -> new BetDoesNotExistException("Bet "
                            + betId + " does not exist"));
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Bet table " + tableName + " does not exist");
        }
    }

    /**
     * Gets a page of bets, at most pageSize long.
     * @param exclusiveStartBetId the exclusive start id for the next page.
     * @return a page of bets.
     * @throws TableDoesNotExistException if the bet table does not exist
     */
    public BetPage getBets(final String exclusiveStartBetId) {
        final ScanResponse result;

        try {
            ScanRequest.Builder scanBuilder = ScanRequest.builder()
                    .tableName(tableName)
                    .limit(pageSize);
            if (!isNullOrEmpty(exclusiveStartBetId)) {
                scanBuilder.exclusiveStartKey(Collections.singletonMap(BET_ID,
                        AttributeValue.builder().s(exclusiveStartBetId).build()));
            }
            result = dynamoDb.scan(scanBuilder.build());
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Bet table " + tableName
                    + " does not exist");
        }

        final List<Bet> bets = result.items().stream()
                .map(this::convert)
                .collect(Collectors.toList());

        BetPage.BetPageBuilder builder = BetPage.builder().bets(bets);
        if (result.lastEvaluatedKey() != null && !result.lastEvaluatedKey().isEmpty()) {
            if ((!result.lastEvaluatedKey().containsKey(BET_ID)
                    || isNullOrEmpty(result.lastEvaluatedKey().get(BET_ID).s()))) {
                throw new IllegalStateException(
                    "betId did not exist or was not a non-empty string in the lastEvaluatedKey");
            } else {
                builder.lastEvaluatedKey(result.lastEvaluatedKey().get(BET_ID).s());
            }
        }

        return builder.build();
    }

    /**
     * Updates an bet object.
     * @param bet bet to update
     * @return updated bet
     */
    public Bet updateBet(final Bet bet) {
        if (bet == null) {
            throw new IllegalArgumentException("Bet to update was null");
        }
        String betId = bet.getBetId();
        if (isNullOrEmpty(betId)) {
            throw new IllegalArgumentException("betId was null or empty");
        }
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":cid",
                AttributeValue.builder().s(validateCustomerId(bet.getCustomerId())).build());

        try {
            expressionAttributeValues.put(":pre",
                    AttributeValue.builder().n(bet.getPreTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(PRE_TAX_AMOUNT_WAS_NULL);
        }
        try {
            expressionAttributeValues.put(":post",
                    AttributeValue.builder().n(bet.getPostTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(POST_TAX_AMOUNT_WAS_NULL);
        }
        expressionAttributeValues.put(":o", AttributeValue.builder().n("1").build());
        try {
            expressionAttributeValues.put(":v",
                    AttributeValue.builder().n(bet.getVersion().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(VERSION_WAS_NULL);
        }
        final UpdateItemResponse result;
        try {
            result = dynamoDb.updateItem(UpdateItemRequest.builder()
                    .tableName(tableName)
                    .key(Collections.singletonMap(BET_ID,
                            AttributeValue.builder().s(bet.getBetId()).build()))
                    .returnValues(ReturnValue.ALL_NEW)
                    .updateExpression(UPDATE_EXPRESSION)
                    .conditionExpression("attribute_exists(betId) AND version = :v")
                    .expressionAttributeValues(expressionAttributeValues)
                    .build());
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToUpdateException(
                    "Either the bet did not exist or the provided version was not current");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Bet table " + tableName
                    + " does not exist and was deleted after reading the bet");
        }
        return convert(result.attributes());
    }

    /**
     * Deletes an bet.
     * @param betId bet id of bet to delete
     * @return the deleted bet
     */
    public Bet deleteBet(final String betId) {
        final DeleteItemResponse result;
        try {
            return Optional.ofNullable(dynamoDb.deleteItem(DeleteItemRequest.builder()
                            .tableName(tableName)
                            .key(Collections.singletonMap(BET_ID,
                                    AttributeValue.builder().s(betId).build()))
                            .conditionExpression("attribute_exists(betId)")
                            .returnValues(ReturnValue.ALL_OLD)
                            .build()))
                    .map(DeleteItemResponse::attributes)
                    .map(this::convert)
                    .orElseThrow(() -> new IllegalStateException(
                            "Condition passed but deleted item was null"));
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToDeleteException(
                    "A competing request changed the bet while processing this request");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Bet table " + tableName
                    + " does not exist and was deleted after reading the bet");
        }
    }

    private Bet convert(final Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        Bet.BetBuilder builder = Bet.builder();

        try {
            builder.betId(item.get(BET_ID).s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "item did not have an betId attribute or it was not a String");
        }

        try {
            builder.customerId(item.get("customerId").s());
        } catch (NullPointerException e) {
            throw new IllegalStateException(
                    "item did not have an customerId attribute or it was not a String");
        }

        try {
            builder.preTaxAmount(new BigDecimal(item.get("preTaxAmount").n()));
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalStateException(
                    "item did not have an preTaxAmount attribute or it was not a Number");
        }

        try {
            builder.postTaxAmount(new BigDecimal(item.get("postTaxAmount").n()));
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalStateException(
                    "item did not have an postTaxAmount attribute or it was not a Number");
        }

        try {
            builder.version(Long.valueOf(item.get("version").n()));
        } catch (NullPointerException | NumberFormatException e) {
            throw new IllegalStateException(
                    "item did not have an version attribute or it was not a Number");
        }

        return builder.build();
    }

    private Map<String, AttributeValue> createBetItem(final CreateBetRequest bet) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(BET_ID, AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("version", AttributeValue.builder().n("1").build());
        item.put("customerId",
                AttributeValue.builder().s(validateCustomerId(bet.getCustomerId())).build());
        try {
            item.put("preTaxAmount",
                    AttributeValue.builder().n(bet.getPreTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(PRE_TAX_AMOUNT_WAS_NULL);
        }
        try {
            item.put("postTaxAmount",
                    AttributeValue.builder().n(bet.getPostTaxAmount().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(POST_TAX_AMOUNT_WAS_NULL);
        }

        return item;
    }

    private String validateCustomerId(final String customerId) {
        if (isNullOrEmpty(customerId)) {
            throw new IllegalArgumentException("customerId was null or empty");
        }
        return customerId;
    }

    /**
     * Creates an bet.
     * @param createBetRequest details of bet to create
     * @return created bet
     */
    public Bet createBet(final CreateBetRequest createBetRequest) {
        if (createBetRequest == null) {
            throw new IllegalArgumentException("CreateBetRequest was null");
        }
        int tries = 0;
        while (tries < 10) {
            try {
                Map<String, AttributeValue> item = createBetItem(createBetRequest);
                dynamoDb.putItem(PutItemRequest.builder()
                        .tableName(tableName)
                        .item(item)
                        .conditionExpression("attribute_not_exists(betId)")
                        .build());
                return Bet.builder()
                        .betId(item.get(BET_ID).s())
                        .customerId(item.get("customerId").s())
                        .preTaxAmount(new BigDecimal(item.get("preTaxAmount").n()))
                        .postTaxAmount(new BigDecimal(item.get("postTaxAmount").n()))
                        .version(Long.valueOf(item.get("version").n()))
                        .build();
            } catch (ConditionalCheckFailedException e) {
                tries++;
            } catch (ResourceNotFoundException e) {
                throw new TableDoesNotExistException(
                        "Bet table " + tableName + " does not exist");
            }
        }
        throw new CouldNotCreateBetException(
                "Unable to generate unique bet id after 10 tries");
    }

    private static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }
}

