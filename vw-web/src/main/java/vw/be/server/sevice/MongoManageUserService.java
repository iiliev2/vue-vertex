package vw.be.server.sevice;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public Future<Collection<UserDTO>> getAllUsers() {
        Future<Collection<UserDTO>> result = Future.future();
        mongoClient.find(COLLECTION, new JsonObject(), findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                Collection<UserDTO> users = findAllResultHandler.result().stream().map(
                        user -> new UserDTO(
                                user.getValue("_id").toString(),
                                user.getLong("version"),
                                user.getString("firstName"),
                                user.getString("surname"),
                                user.getString("lastName"))
                ).collect(Collectors.toList());

                result.complete(users);
            } else {
                result.fail(findAllResultHandler.cause());
            }
        });

        return result;
    }

    @Override
    public Future<Optional<String>> getUserById(String userID) {
        Future<Optional<String>> result = Future.future();
        mongoClient.findOne(COLLECTION, new JsonObject().put(USER_ID, userID), null, findUserResultHandler -> {
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
    public Future<Boolean> updateUser(UserDTO userDTO) {
        Future<Boolean> result = Future.future();
        JsonObject query = new JsonObject().put(USER_ID, userDTO.getId());
        mongoClient.findOneAndReplace(COLLECTION, query, userDTO.toJsonObject(), res -> {
            if (res.succeeded()) {
                result.complete(true);
            } else {
                result.fail(res.cause());
            }
        });

        return result;
    }

    @Override
    public Future<Boolean> deleteUserById(String userID) {
        Future<Boolean> result = Future.future();
        JsonObject query = new JsonObject().put(USER_ID, userID);
        mongoClient.findOneAndDelete(COLLECTION, query, res -> {
            if (res.succeeded()) {
                result.complete(true);
            } else {
                result.fail(res.cause());
            }
        });

        return result;
    }
}
