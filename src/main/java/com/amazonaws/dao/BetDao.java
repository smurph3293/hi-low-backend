package com.amazonaws.dao;

import com.amazonaws.exception.BetDoesNotExistException;
import com.amazonaws.exception.CouldNotCreateBetException;
import com.amazonaws.exception.TableDoesNotExistException;
import com.amazonaws.exception.UnableToDeleteException;
import com.amazonaws.exception.UnableToUpdateException;
import com.amazonaws.model.Bet;
import com.amazonaws.model.request.BetRequest;

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
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemResponse;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class BetDao {

    private static final String UPDATE_EXPRESSION = "SET commissionerXref = :comxref, " +
            "title = :t, " +
            "description = :des, " +
            "conditions = :con, " +
            "punishment = :pun, " +
            "conditionsDeadline = :cond, " +
            "punishmentDeadline = :pund, " +
            "resultXref = :rxref, " +
            "isComplete = :com";
    private static final String XREF = "xref";

    private final String tableName;
    private final DynamoDbClient dynamoDb;

    /**
     * Constructs an BetDao.
     * 
     * @param dynamoDb  dynamodb client
     * @param tableName name of table to use for bets
     * @param pageSize  size of pages for getBets
     */
    public BetDao(final DynamoDbClient dynamoDb, final String tableName, final int pageSize) {
        this.dynamoDb = dynamoDb;
        this.tableName = tableName;
    }

    private Bet convert(final Map<String, AttributeValue> item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        Bet.BetBuilder builder = Bet.builder();
        builder.xref(item.get(XREF).s())
                .creatorXref(item.get("creatorXref").s())
                .participants(item.get("participants").ss())
                .commissionerXref(item.get("commissionerXref").s())
                .createdAt(new Date(item.get("createdAt").s()))
                .title(item.get("title").s())
                .description(item.get("description").s())
                .conditions(item.get("conditions").s())
                .punishment(item.get("punishment").s())
                .conditionsDeadline(new Date(item.get("conditionsDeadline").s()))
                .punishmentDeadline(new Date(item.get("punishmentDeadline").s()))
                .version(Long.valueOf(item.get("version").n()));
        try {
            builder.resultXref(item.get("resultXref").s());
        } catch (NullPointerException e) { }
        try {
            builder.comments(item.get("comments").ss());
        } catch (NullPointerException e) { }
        try {
            builder.isComplete(item.get("isComplete").bool());
        } catch (NullPointerException e) { }
        return builder.build();
    }

    private Map<String, AttributeValue> updateBetItem(final BetRequest bet) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(":comxref", AttributeValue.builder().s(bet.getCommissionerXref()).build());
        try {
            item.put(":t", AttributeValue.builder().s(bet.getTitle().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("title must be defined");
        }
        item.put(":des", AttributeValue.builder().s(bet.getDescription()).build());
        try {
            item.put(":con", AttributeValue.builder().s(bet.getConditions().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("conditions must be defined");
        }
        try {
            item.put(":pun", AttributeValue.builder().s(bet.getPunishment().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("punishment must be defined");
        }
        try {
            item.put(":cond", AttributeValue.builder().s(bet.getConditionsDeadline().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("conditionsDeadline must be defined");
        }
        try {
            item.put(":pund", AttributeValue.builder().s(bet.getPunishmentDeadline().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("punishmentDeadline must be defined");
        }
        item.put(":rxref", AttributeValue.builder().s(bet.getResultXref()).build());
        item.put(":com", AttributeValue.builder().bool(bet.getIsComplete()).build());
        return item;
    }

    private Map<String, AttributeValue> createBetItem(final BetRequest bet) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("betId", AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put(XREF, AttributeValue.builder().s(UUID.randomUUID().toString()).build());
        item.put("version", AttributeValue.builder().n("1").build());
        item.put("creatorXref", AttributeValue.builder().s(validateId(bet.getCreatorXref())).build());
        item.put("participants", AttributeValue.builder().ss(bet.getParticipants()).build());
        try {
            item.put("commissionerXref", AttributeValue.builder().s(bet.getCommissionerXref().toString()).build());
        } catch (NullPointerException e) {
            item.put("commissionerXref", AttributeValue.builder().s(bet.getCreatorXref()).build());
        }
        item.put("createdAt", AttributeValue.builder().s(new Date().toString()).build());
        try {
            item.put("title", AttributeValue.builder().s(bet.getTitle().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("title must be defined");
        }
        item.put("description", AttributeValue.builder().s(bet.getDescription()).build());
        try {
            item.put("conditions", AttributeValue.builder().s(bet.getConditions().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("conditions must be defined");
        }
        try {
            item.put("punishment", AttributeValue.builder().s(bet.getPunishment().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("punishment must be defined");
        }
        try {
            item.put("conditionsDeadline", AttributeValue.builder().s(bet.getConditionsDeadline().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("conditionsDeadline must be defined");
        }
        try {
            item.put("punishmentDeadline", AttributeValue.builder().s(bet.getPunishmentDeadline().toString()).build());
        } catch (NullPointerException e) {
            throw new IllegalArgumentException("punishmentDeadline must be defined");
        }
        item.put("version", AttributeValue.builder().n("1").build());
        return item;
    }

    private String validateId(final String id) {
        if (isNullOrEmpty(id)) {
            throw new IllegalArgumentException("id was null or empty");
        }
        return id;
    }

    /**
     * Returns an bet or throws if the bet does not exist.
     * 
     * @param xref id of bet to get
     * @return the bet if it exists
     * @throws BetDoesNotExistException if the bet does not exist
     */
    public Bet getBet(final String xref) {
        try {
            return Optional
                    .ofNullable(dynamoDb.getItem(GetItemRequest.builder().tableName(tableName)
                            .key(Collections.singletonMap(XREF, AttributeValue.builder().s(xref).build())).build()))
                    .map(GetItemResponse::item).map(this::convert)
                    .orElseThrow(() -> new BetDoesNotExistException("Bet " + xref + " does not exist"));
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException("Bet table " + tableName + " does not exist");
        }
    }

    /**
     * Updates an bet object.
     * 
     * @param betRequest bet to update
     * @return updated bet
     */
    public Bet updateBet(final BetRequest betRequest) {
        if (betRequest == null) {
            throw new IllegalArgumentException("Bet to update was null");
        }
        String xref = betRequest.getXref();
        if (isNullOrEmpty(xref)) {
            throw new IllegalArgumentException("xref was null or empty");
        }
        Map<String, AttributeValue> expressionAttributeValues = updateBetItem(betRequest);
        final UpdateItemResponse result;
        try {
            result = dynamoDb.updateItem(UpdateItemRequest.builder().tableName(tableName)
                    .key(Collections.singletonMap(XREF, AttributeValue.builder().s(betRequest.getXref()).build()))
                    .returnValues(ReturnValue.ALL_NEW).updateExpression(UPDATE_EXPRESSION)
                    .conditionExpression("attribute_exists(xref) AND version = :v")
                    .expressionAttributeValues(expressionAttributeValues).build());
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToUpdateException("Either the bet did not exist or the provided version was not current");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException(
                    "Bet table " + tableName + " does not exist and was deleted after reading the bet");
        }
        return convert(result.attributes());
    }

    /**
     * Deletes an bet.
     * 
     * @param xref bet id of bet to delete
     * @return the deleted bet
     */
    public Bet deleteBet(final String xref) {
        try {
            return Optional
                    .ofNullable(dynamoDb.deleteItem(DeleteItemRequest.builder().tableName(tableName)
                            .key(Collections.singletonMap(XREF, AttributeValue.builder().s(xref).build()))
                            .conditionExpression("attribute_exists(xref)").returnValues(ReturnValue.ALL_OLD).build()))
                    .map(DeleteItemResponse::attributes).map(this::convert)
                    .orElseThrow(() -> new IllegalStateException("Condition passed but deleted item was null"));
        } catch (ConditionalCheckFailedException e) {
            throw new UnableToDeleteException("A competing request changed the bet while processing this request");
        } catch (ResourceNotFoundException e) {
            throw new TableDoesNotExistException(
                    "Bet table " + tableName + " does not exist and was deleted after reading the bet");
        }
    }

    /**
     * Creates an bet.
     * 
     * @param betRequest details of bet to create
     * @return created bet
     */
    public Bet createBet(final BetRequest betRequest) {
        if (betRequest == null) {
            throw new IllegalArgumentException("BetRequest was null");
        }
        int tries = 0;
        while (tries < 3) {
            try {
                Map<String, AttributeValue> item = createBetItem(betRequest);
                dynamoDb.putItem(PutItemRequest.builder().tableName(tableName).item(item)
                        .conditionExpression("attribute_not_exists(xref)").build());
                return convert(item);
            } catch (ConditionalCheckFailedException e) {
                tries++;
            } catch (ResourceNotFoundException e) {
                throw new TableDoesNotExistException("Bet table " + tableName + " does not exist");
            }
        }
        throw new CouldNotCreateBetException("Unable to generate unique bet id after " + tries + " tries");
    }

    private static boolean isNullOrEmpty(final String string) {
        return string == null || string.isEmpty();
    }
}
