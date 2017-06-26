package vw.server.sevice;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import vw.common.dto.UserDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO All JDBC operation should work with DB. Now only getAllUsers works with DB!
 */
public class MongoManageUserService implements IManageUserService<Handler<AsyncResult<List<UserDTO>>>,Void>{

    private static final String COLLECTION = "user";

    private MongoClient mongoClient;

    public MongoManageUserService(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public Void getAllUsers(Handler<AsyncResult<List<UserDTO>>> handler) {
        mongoClient.find(COLLECTION, new JsonObject(), findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                handler.handle(
                        Future.succeededFuture(
                            findAllResultHandler.result().stream().map(
                                document -> new UserDTO(
                                    null,
                                    document.getLong("version"),
                                    document.getString("firstName"),
                                    document.getString("surname"),
                                    document.getString("lastName"))
                            ).collect(Collectors.toList())));
            } else {
                handler.handle(Future.failedFuture(findAllResultHandler.cause()));
            }
        });

        return null;
    }

    public UserDTO getUserById(String userID) {
        return null;
    }

    public UserDTO createUser(UserDTO userDTO) {

        return null;
    }

    public UserDTO updateUser(UserDTO userDTO) {
        return null;
    }

    public void deleteUserById(String userID) {

    }
}
