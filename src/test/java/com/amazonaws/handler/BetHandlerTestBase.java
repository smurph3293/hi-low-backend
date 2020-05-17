package com.amazonaws.handler;

import com.amazonaws.config.DaggerBetTestComponent;
import com.amazonaws.config.BetTestComponent;
import org.junit.After;
import org.junit.Before;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import javax.inject.Inject;

/**
 * This class serves as the base class for Integration tests. do not include I T in
 * the class name so that it does not get picked up by failsafe.
 */
public abstract class BetHandlerTestBase {
    private static final String TABLE_NAME = "bet";

    private final BetTestComponent betComponent;

    @Inject
    DynamoDbClient dynamoDb;

    public BetHandlerTestBase() {
        betComponent = DaggerBetTestComponent.builder().build();
        betComponent.inject(this);
    }

    @Before
    public void setup() {
        dynamoDb.createTable(CreateTableRequest.builder()
                .tableName(TABLE_NAME)
                .keySchema(KeySchemaElement.builder()
                        .keyType(KeyType.HASH)
                        .attributeName("betXref")
                        .build())
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("betXref")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .provisionedThroughput(
                        ProvisionedThroughput.builder()
                                .readCapacityUnits(1L)
                                .writeCapacityUnits(1L)
                                .build())
                .build());

    }

    @After
    public void teardown() {
        dynamoDb.deleteTable(DeleteTableRequest.builder().tableName(TABLE_NAME).build());
    }
}
