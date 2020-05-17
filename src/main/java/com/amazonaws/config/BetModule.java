package com.amazonaws.config;

import com.amazonaws.dao.BetDao;

import com.fasterxml.jackson.databind.ObjectMapper;
import dagger.Module;
import dagger.Provides;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;
import java.util.Optional;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class BetModule {
    @Singleton
    @Provides
    @Named("tableName")
    String tableName() {
        return Optional.ofNullable(System.getenv("TABLE_NAME")).orElse("bet");
    }

    @Singleton
    @Provides
    DynamoDbClient dynamoDb() {
        final String endpoint = System.getenv("ENDPOINT_OVERRIDE");

        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        builder.httpClient(ApacheHttpClient.builder().build());
        if (endpoint != null && !endpoint.isEmpty()) {
            builder.endpointOverride(URI.create(endpoint));
        }

        return builder.build();
    }

    @Singleton
    @Provides
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Singleton
    @Provides
    public BetDao betDao(DynamoDbClient dynamoDb, @Named("tableName") String tableName) {
        return new BetDao(dynamoDb, tableName,10);
    }
}
