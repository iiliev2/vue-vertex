package vw.be.server.sevice;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Repository interface for users. MongoDB implementation.
 */
public class MongoManageUserService implements IManageUserService{

    private static final String COLLECTION = "user";
    private static final String USER_ID = "_id";

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
                JsonArray users = new JsonArray(findAllResultHandler.result());
                message.reply(users);
            } else {
                message.fail(1, findAllResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void getUserById(Message<JsonObject> message) {
        mongoClient.findOne(COLLECTION, new JsonObject().put(USER_ID, message.body().getString("id")), null, findUserResultHandler -> {
            if (findUserResultHandler.succeeded()) {
                JsonObject jsonUser = findUserResultHandler.result();
                if(jsonUser == null || jsonUser.isEmpty()){
                    message.fail(2, findUserResultHandler.cause().getMessage());
                } else {
                    message.reply(jsonUser);
                }
            } else {
                message.fail(1, findUserResultHandler.cause().getMessage());
            }
        });
    }

    @Override
    public void createUser(Message<JsonObject> message) {
        mongoClient.insert(COLLECTION, message.body(), res -> {
            if (res.succeeded()) {
                JsonObject response = new JsonObject();
                response.put("taskStatus", "Created");
                response.put("id", res.result());
                message.reply(response);
            } else {
                message.fail(1, res.cause().getMessage());
            }
        });
    }

    @Override
    public void updateUser(Message<JsonObject> message) {
        JsonObject query = new JsonObject().put(USER_ID, message.body().remove("id"));
        JsonObject forUpdate = new JsonObject().put("$set", message.body());
        mongoClient.findOneAndUpdate(COLLECTION, query, forUpdate, res -> {
            if (res.succeeded()) {
                message.reply("Edited");
            } else {
                message.fail(1, res.cause().getMessage());
            }
        });
    }

    @Override
    public void deleteUserById(Message<JsonObject> message) {
        JsonObject query = new JsonObject().put(USER_ID, message.body().getString("id"));
        mongoClient.findOneAndDelete(COLLECTION, query, res -> {
            if (res.succeeded()) {
                message.reply("Deleted");
            } else {
                message.fail(1, res.cause().getMessage());
            }
        });

    }
}
