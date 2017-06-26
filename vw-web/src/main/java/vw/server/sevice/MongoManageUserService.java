package vw.server.sevice;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * TODO All JDBC operation should work with DB. Now only getAllUsers works with DB!
 */
public class MongoManageUserService implements IManageUserService{

    private static final String COLLECTION = "user";

    private MongoClient mongoClient;

    public MongoManageUserService(Vertx vertx, JsonObject config) {
        this.mongoClient = MongoClient.createShared(vertx, config);
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }

    @Override
    public Future<Collection<String>> getAllUsers() {
        Future<Collection<String>> result = Future.future();
        mongoClient.find(COLLECTION, new JsonObject(), findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                Collection<JsonObject> users = findAllResultHandler.result();
                result.complete(users.stream().map(JsonObject::encodePrettily).collect(Collectors.toList()));
            } else {
                result.fail(findAllResultHandler.cause());
            }
        });

        return result;
    }

    @Override
    public Future<Optional<String>> getUserById(String userID) {
        Future<Optional<String>> result = Future.future();
        mongoClient.findOne(COLLECTION, new JsonObject().put("id", userID), null, findUserResultHandler -> {
            if (findUserResultHandler.succeeded()) {
                JsonObject jsonUser = findUserResultHandler.result();
                if(jsonUser == null || jsonUser.isEmpty()){
                    result.complete(Optional.empty());
                } else {
                    result.complete(Optional.of(jsonUser.encodePrettily()));
                }
            } else {
                result.fail(findUserResultHandler.cause());
            }
        });

        return result;
    }

    @Override
    public Future<Boolean> createUser(UserDTO userDTO) {
        Future<Boolean> result = Future.future();
        JsonObject jsonUser = userDTO.toJsonObject();
        mongoClient.insert(COLLECTION, jsonUser, res -> {
            if (res.succeeded()) {
                userDTO.setUserId(res.result());
                result.complete(true);
            } else {
                result.fail(res.cause());
            }
        });

        return result;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        return null;
    }

    @Override
    public void deleteUserById(String userID) {

    }
}
