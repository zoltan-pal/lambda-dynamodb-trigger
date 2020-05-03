package com.github.zoltanpal.demo.localstack.event;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;
import com.github.zoltanpal.demo.localstack.conversion.DynamoDBImageToUserConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDataEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserDataEventHandler.class);
    private final DynamoDBMapper dynamoDBMapper;
    private final DynamoDBImageToUserConverter imageToUserConverter;

    public UserDataEventHandler(DynamoDBMapper dynamoDBMapper, DynamoDBImageToUserConverter imageToUserConverter) {
        this.dynamoDBMapper = dynamoDBMapper;
        this.imageToUserConverter = imageToUserConverter;
    }

    void handleUserInsertion(DynamodbEvent.DynamodbStreamRecord record) {
        var newImage = record.getDynamodb().getNewImage();
        var user = imageToUserConverter.convert(newImage);
        LOGGER.info("Syncing for inserted user={}", user);
        dynamoDBMapper.save(user);
    }

    void handleUserUpdate(DynamodbEvent.DynamodbStreamRecord record) {
        var oldImage = record.getDynamodb().getOldImage();
        var originalUser = imageToUserConverter.convert(oldImage);
        var newImage = record.getDynamodb().getNewImage();
        var updatedUser = imageToUserConverter.convert(newImage);
        LOGGER.info("Syncing for update on user={}, new data={}", originalUser, updatedUser);
        dynamoDBMapper.save(updatedUser);
    }

    void handleUserDeletion(DynamodbEvent.DynamodbStreamRecord record) {
        var oldImage = record.getDynamodb().getOldImage();
        var user = imageToUserConverter.convert(oldImage);
        LOGGER.info("Syncing for removed user={}", user);
        dynamoDBMapper.delete(user);
    }
}
