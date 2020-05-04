package com.github.zoltanpal.demo.localstack.conversion;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.github.zoltanpal.demo.localstack.domain.User;

import java.util.Map;

public class DynamoDBImageToUserConverter {

    public User convert(Map<String, AttributeValue> dynamoDBImage) {
        var id = dynamoDBImage.get("userId").getS();
        var name = dynamoDBImage.get("userName").getS();
        return new User(id, name);
    }
}
