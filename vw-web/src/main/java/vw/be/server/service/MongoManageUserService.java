package vw.be.server.service;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.time.Instant;

import static vw.be.server.common.ApplicationErrorCodes.DB_ERROR;
import static vw.be.server.common.PersistenceResponseCodeEnum.*;

/**
 * Repository interface for users. MongoDB implementation.
 */
public class MongoManageUserService implements IManageUserService {

    private static final String COLLECTION = "user";
    private static final String USER_ID = "_id";
    private static final String SET_PERSISTENCE_OPERATOR = "$set";
    private static final String REGEX_OPERATOR = "$regex";
    private static final String OR_PERSISTENCE_OPERATOR = "$or";
    private static final String LIKE_WILDCARD_OPERATOR = ".*";
    private static final String FIRST_NAME_COLUMN = "firstName";
    private static final String SURNAME_COLUMN = "surname";
    private static final String LAST_NAME_COLUMN = "lastName";
    private static final String VERSION_NAME_COLUMN = "version";
    private static final String CREATED_BY_NAME_COLUMN = "createdBy";
    private static final String DEFAULT_USERNAME = "Admin";
    private static final String CREATION_DATETIME_NAME_COLUMN = "creationDatetime";
    private static final String EDITED_BY_NAME_COLUMN = "editedBy";
    private static final String EDITION_DATETIME_NAME_COLUMN = "editionDatetime";

    private MongoClient mongoClient;

    public MongoManageUserService(Vertx vertx, JsonObject config) {
        this.mongoClient = MongoClient.createShared(vertx, config);
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }

    @Override
    public void getAllUsers(Message<JsonObject> message) {
        mongoClient.find(COLLECTION, new JsonObject(), findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                JsonArray allUsers = new JsonArray(findAllResultHandler.result());
                replyMessage(message, allUsers, createResponseHeaders(FOUND));
            } else {
                failMessage(message, DB_ERROR, findAllResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void getUserById(Message<JsonObject> message) {
        mongoClient.findOne(COLLECTION, new JsonObject().put(USER_ID, message.body().getString(ID)), null, findUserResultHandler -> {
            if (findUserResultHandler.succeeded()) {
                JsonObject jsonUser = findUserResultHandler.result();
                if (jsonUser == null || jsonUser.isEmpty()) {
                    replyMessage(message, null, createResponseHeaders(NOT_FOUND));
                } else {
                    replyMessage(message, jsonUser, createResponseHeaders(FOUND));
                }
            } else {
                failMessage(message, DB_ERROR, findUserResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void getUserByFilter(Message<JsonObject> message) {
        final String queryParam = message.body().getString(SEARCH_BY_ALL_NAMES_PARTIAL_PARAMETER);
        JsonObject query = new JsonObject()
                .put(OR_PERSISTENCE_OPERATOR,
                        new JsonArray()
                                .add(new JsonObject().put(FIRST_NAME_COLUMN,
                                        new JsonObject()
                                                .put(REGEX_OPERATOR, (LIKE_WILDCARD_OPERATOR + queryParam + LIKE_WILDCARD_OPERATOR))
                                ))
                                .add(new JsonObject().put(SURNAME_COLUMN,
                                        new JsonObject()
                                                .put(REGEX_OPERATOR, (LIKE_WILDCARD_OPERATOR + queryParam + LIKE_WILDCARD_OPERATOR))
                                ))
                                .add(new JsonObject().put(LAST_NAME_COLUMN,
                                        new JsonObject()
                                                .put(REGEX_OPERATOR, (LIKE_WILDCARD_OPERATOR + queryParam + LIKE_WILDCARD_OPERATOR))
                                ))
                );
        mongoClient.find(COLLECTION, query, findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                JsonArray foundUsers = new JsonArray(findAllResultHandler.result());
                replyMessage(message, foundUsers, createResponseHeaders(FOUND));
            } else {
                failMessage(message, DB_ERROR, findAllResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void createUser(Message<JsonObject> message) {
        final JsonObject messageBody = message.body();
        setUserVersion(messageBody);
        messageBody.remove(USER_ID);
        messageBody.put(CREATED_BY_NAME_COLUMN, DEFAULT_USERNAME);
        messageBody.put(CREATION_DATETIME_NAME_COLUMN, Instant.now());
        messageBody.putNull(EDITED_BY_NAME_COLUMN);
        messageBody.putNull(EDITION_DATETIME_NAME_COLUMN);
        mongoClient.insert(COLLECTION, message.body(), res -> {
            if (res.succeeded()) {
                replyMessage(message, res.result(), createResponseHeaders(CREATED));
            } else {
                failMessage(message, DB_ERROR, res.cause().getMessage());
            }
        });
    }

    private void setUserVersion(JsonObject messageBody) {
        final Object version = messageBody.getValue(VERSION_NAME_COLUMN);
        messageBody.put(VERSION_NAME_COLUMN, (version == null || !version.getClass().isInstance(0) ? 1 : (int)version + 1));
    }

    @Override
    public void updateUser(Message<JsonObject> message) {
        final JsonObject messageBody = message.body();
        setUserVersion(messageBody);
        messageBody.put(EDITED_BY_NAME_COLUMN, DEFAULT_USERNAME);
        messageBody.put(EDITION_DATETIME_NAME_COLUMN, Instant.now());
        JsonObject query = new JsonObject().put(USER_ID, messageBody.remove(ID));
        JsonObject forUpdate = new JsonObject().put(SET_PERSISTENCE_OPERATOR, messageBody);
        mongoClient.findOneAndUpdate(COLLECTION, query, forUpdate, res -> {
            if (res.succeeded()) {
                replyMessage(message, forUpdate, createResponseHeaders(MERGED));
            } else {
                failMessage(message, DB_ERROR, res.cause().getMessage());
            }
        });
    }

    @Override
    public void deleteUserById(Message<JsonObject> message) {
        JsonObject query = new JsonObject().put(USER_ID, message.body().getString(ID));
        mongoClient.findOneAndDelete(COLLECTION, query, res -> {
            if (res.succeeded()) {
                replyMessage(message, null, createResponseHeaders(DELETED));
            } else {
                failMessage(message, DB_ERROR, res.cause().getMessage());
            }
        });
    }

}
