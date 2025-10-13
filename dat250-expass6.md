# DAT250 Exercise Assignment 6 Report

## Setup

- Message broker used: **RabbitMQ** (AMQP protocol)
- Exchange type: **TopicExchange**
- Each Poll creates its own topic: `poll.<id>`
- The PollApp subscribes to `poll.*` messages to receive all vote events.

## Implementation Summary

- Added `RabbitConfig.java` to configure the exchange and queue.
- Extended `PollService` to register topics on poll creation and publish vote events.
- Added `PollEventListener` to listen for incoming vote messages and update the database.
- Created a simple DTO (`VoteEvent`) for message serialization.
- Tested integration using a Python script and RabbitMQ Management UI.

## Technical Problems Encountered

1. **Message serialization errors**
    - Initially messages were not deserialized correctly.
    - Fixed by using the default `Jackson2JsonMessageConverter`.

2. **Queue binding issues**
    - Declared per-poll bindings dynamically when creating polls.
    - Verified with RabbitMQ Management UI.

3. **Anonymous votes**
    - Modified domain model to allow `Vote.voter` to be null.

## Pending Issues

- No unit tests for MQ listeners yet.
- Future improvement: Publish result updates back to clients via WebSocket or RabbitMQ.