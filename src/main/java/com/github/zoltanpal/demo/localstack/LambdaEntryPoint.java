package com.github.zoltanpal.demo.localstack;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.github.zoltanpal.demo.localstack.conversion.DynamoDBImageToUserConverter;
import com.github.zoltanpal.demo.localstack.event.EventType;
import com.github.zoltanpal.demo.localstack.event.UserDataEventHandler;
import com.github.zoltanpal.demo.localstack.event.UserDataEventHandlerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LambdaEntryPoint implements RequestHandler<DynamodbEvent, Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LambdaEntryPoint.class);
    private static final String DYNAMODB_ENDPOINT = "http://0.0.0.0:4569";
    private static final String EMPTY_STRING = "";

    private final UserDataEventHandlerProvider userDataEventHandlerProvider;

    public LambdaEntryPoint() {
        LOGGER.debug("Initializing SyncLambda...");
        var dynamoDBEndpointConfig = new AwsClientBuilder.EndpointConfiguration(DYNAMODB_ENDPOINT, EMPTY_STRING);
        var dynamoDBClient = AmazonDynamoDBClientBuilder.standard()
                .withEndpointConfiguration(dynamoDBEndpointConfig)
                .build();
        var dynamoDBMapper = new DynamoDBMapper(dynamoDBClient);
        var imageToUserConverter = new DynamoDBImageToUserConverter();
        var userDataEventHandler = new UserDataEventHandler(dynamoDBMapper, imageToUserConverter);

        this.userDataEventHandlerProvider = new UserDataEventHandlerProvider(userDataEventHandler);
    }

    @Override
    public Void handleRequest(final DynamodbEvent dynamodbEvent, final Context context) {
        LOGGER.debug("Processing DynamoDB events...");
        dynamodbEvent.getRecords().forEach(record -> {
            var eventType = EventType.valueOf(record.getEventName());
            LOGGER.info("Handling event={}", eventType);
            userDataEventHandlerProvider.getBy(eventType).accept(record);
        });
        return null;
    }
}
