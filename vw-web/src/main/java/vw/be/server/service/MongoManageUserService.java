package vw.be.server.service;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import static vw.be.server.common.ApplicationErrorCodes.DB_ERROR;
import static vw.be.server.common.ApplicationErrorCodes.UNEXISTING_OBJECT;
import static vw.be.server.common.IResourceBundleConstants.USER_NOT_FOUND_MSG;
import static vw.be.server.common.PersistenceResponseCodeEnum.*;

/**
 * Repository interface for users. MongoDB implementation.
 */
public class MongoManageUserService implements IManageUserService {

    private static final String COLLECTION = "user";
    private static final String USER_ID = "_id";
    private static final String SET_PERSISTENCE_OPERATOR = "$set";

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
                failMessage(DB_ERROR, message, findAllResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void getUserById(Message<JsonObject> message) {
        mongoClient.findOne(COLLECTION, new JsonObject().put(USER_ID, message.body().getString(ID)), null, findUserResultHandler -> {
            if (findUserResultHandler.succeeded()) {
                JsonObject jsonUser = findUserResultHandler.result();
                if (jsonUser == null || jsonUser.isEmpty()) {
                    failMessage(UNEXISTING_OBJECT, message, USER_NOT_FOUND_MSG);
                } else {
                    replyMessage(message, jsonUser, createResponseHeaders(FOUND));
                }
            } else {
                failMessage(DB_ERROR, message, findUserResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void createUser(Message<JsonObject> message) {
        mongoClient.insert(COLLECTION, message.body(), res -> {
            if (res.succeeded()) {
                replyMessage(message, res.result(), createResponseHeaders(CREATED));
            } else {
                failMessage(DB_ERROR, message, res.cause().getMessage());
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
                failMessage(DB_ERROR, message, res.cause().getMessage());
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
                failMessage(DB_ERROR, message, res.cause().getMessage());
            }
        });
    }

}
