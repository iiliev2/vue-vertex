package vw.server.sevice;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import vw.common.dto.UserDTO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * TODO All JDBC operation should work with DB. Now only getAllUsers works with DB!
 */
public class ManageUserService {

    private static final String INITIAL_USER_ID = "1";
    private static final String COLLECTION = "user";

    private Map<String, UserDTO> users = new ConcurrentHashMap<>();

    private MongoClient mongoClient;

    public ManageUserService(MongoClient mongoClient) {
        this.mongoClient = mongoClient;

        setMockupInitialData();
    }

    /**
     * Mock some initial data
     * Used only for developement purposes
     */
    private void setMockupInitialData() {
        addUserToPersistence(new UserDTO("1", 1L, "Pesho", "Stupid", "Peshov"));
        addUserToPersistence(new UserDTO("2", 1L, "Gosho", "Gargamel", "Goshev"));
        addUserToPersistence(new UserDTO("3", 1L, "Kolio", "The only one", "Kolev"));
        addUserToPersistence(new UserDTO("4", 2L, "Macho", "Macho", "Kolev"));
    }

    /**
     * Adds a user to user map
     * Used only for development purposes.
     *
     * @param user to be added to users map
     */
    private void addUserToPersistence(UserDTO user) {
        users.put(user.getId(), user);
    }

    /**
     * Retrieves all users from persistence
     * @param handler return all users or failure cause
     */
    public void getAllUsers(Handler<AsyncResult<List<UserDTO>>> handler) {
        mongoClient.find(COLLECTION, new JsonObject(), findAllResultHandler -> {
            if (findAllResultHandler.succeeded()) {
                handler.handle(
                        Future.succeededFuture(
                            findAllResultHandler.result().stream().map(
                                document -> new UserDTO(
                                    document.getString("id"),
                                    document.getLong("version"),
                                    document.getString("firstName"),
                                    document.getString("surname"),
                                    document.getString("lastName"))
                            ).collect(Collectors.toList())));
            } else {
                handler.handle(Future.failedFuture(findAllResultHandler.cause()));
            }
        });
    }

    /**
     * Retrieves a user by id from persistence
     *
     * @param userID that we search
     * @return found user or null
     */
    public UserDTO getUserById(String userID) {
        return users.getOrDefault(userID, null);
    }

    /**
     * Creates a user in persistence
     *
     * @param userDTO user to create
     * @return created user
     */
    public UserDTO createUser(UserDTO userDTO) {
        Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
        userDTO.setId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(INITIAL_USER_ID));
        users.put(userDTO.getId(), userDTO);

        return userDTO;
    }

    /**
     * Updates a user in persistence
     *
     * @param userDTO user to update
     * @return updated user or null if user does not exists
     */
    public UserDTO updateUser(UserDTO userDTO) {
        UserDTO oldUserVersion = users.get(userDTO.getId());
        if (oldUserVersion == null) {
            return null;
        } else {
            userDTO.setVersion(oldUserVersion.getVersion() + 1);
            boolean isUpdated = (users.replace(userDTO.getId(), userDTO) != null);

            return (isUpdated ? userDTO : null);
        }
    }

    /**
     * Deletes a user by id
     *
     * @param userID user to be deleted
     */
    public void deleteUserById(String userID) {
        users.remove(userID);
    }
}
