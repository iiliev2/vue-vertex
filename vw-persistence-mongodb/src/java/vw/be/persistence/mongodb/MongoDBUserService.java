package vw.be.persistence.mongodb;

import vw.be.persistence.service.IManageUserService;

public class MongoDBUserService implements IManageUserService {
}
/*package vw.be.server.service;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import static vw.be.server.common.ApplicationErrorCodes.DB_ERROR;
import static vw.be.server.common.PersistenceResponseCodeEnum.*;


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
        mongoClient.findOne(COLLECTION,
                            new JsonObject().put(USER_ID, message.body().getString(ID)),
                            null,
                            findUserResultHandler -> {
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
                                                               .put(REGEX_OPERATOR,
                                                                    (LIKE_WILDCARD_OPERATOR +
                                                                     queryParam +
                                                                     LIKE_WILDCARD_OPERATOR))
                                                      ))
                             .add(new JsonObject().put(SURNAME_COLUMN,
                                                       new JsonObject()
                                                               .put(REGEX_OPERATOR,
                                                                    (LIKE_WILDCARD_OPERATOR +
                                                                     queryParam +
                                                                     LIKE_WILDCARD_OPERATOR))
                                                      ))
                             .add(new JsonObject().put(LAST_NAME_COLUMN,
                                                       new JsonObject()
                                                               .put(REGEX_OPERATOR,
                                                                    (LIKE_WILDCARD_OPERATOR +
                                                                     queryParam +
                                                                     LIKE_WILDCARD_OPERATOR))
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
        mongoClient.insert(COLLECTION, message.body(), res -> {
            if (res.succeeded()) {
                replyMessage(message, res.result(), createResponseHeaders(CREATED));
            } else {
                failMessage(message, DB_ERROR, res.cause().getMessage());
            }
        });
    }

    @Override
    public void updateUser(Message<JsonObject> message) {
        JsonObject query = new JsonObject().put(USER_ID, message.body().remove(ID));
        JsonObject forUpdate = new JsonObject().put(SET_PERSISTENCE_OPERATOR, message.body());
        mongoClient.findOneAndUpdate(COLLECTION, query, forUpdate, res -> {
            if (res.succeeded()) {
                replyMessage(message, null, createResponseHeaders(MERGED));
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
*/