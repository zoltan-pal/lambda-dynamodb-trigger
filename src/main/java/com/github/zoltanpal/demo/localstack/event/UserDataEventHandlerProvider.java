package com.github.zoltanpal.demo.localstack.event;

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent;

import java.util.Map;
import java.util.function.Consumer;

public class UserDataEventHandlerProvider {

    private final Map<EventType, Consumer<DynamodbEvent.DynamodbStreamRecord>> eventHandlerMapping;

    public UserDataEventHandlerProvider(UserDataEventHandler userDataEventHandler) {
        this.eventHandlerMapping = Map.of(
                EventType.INSERT, userDataEventHandler::handleUserInsertion,
                EventType.MODIFY, userDataEventHandler::handleUserUpdate,
                EventType.REMOVE, userDataEventHandler::handleUserDeletion
        );
    }

    public Consumer<DynamodbEvent.DynamodbStreamRecord> getBy(EventType eventType) {
        return this.eventHandlerMapping.get(eventType);
    }
}
