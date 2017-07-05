package vw.be.server.service;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vw.be.common.dto.UserDTO;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static vw.be.server.common.ApplicationErrorCodes.UNEXISTING_OBJECT;
import static vw.be.server.common.IResourceBundleConstants.USER_NOT_FOUND_MSG;
import static vw.be.server.common.PersistenceResponseCodeEnum.*;

/**
 * Repository interface for users. This class is used for mocking purposes, only.
 */
public class MockManageUserService implements IManageUserService{

    static final String FIRST_USER_ID = "1";
    static final long FIRST_USER_VERSION = 1L;
    private static final String FIRST_USER_FIRST_NAME = "Pesho";
    private static final String FIRST_USER_SURNAME = "Stupid";
    private static final String FIRST_USER_LAST_NAME = "Peshov";

    private static final String SECOND_USER_ID = "2";
    private static final long SECOND_USER_VERSION = 1L;
    private static final String SECOND_USER_FIRST_NAME = "Gosho";
    private static final String SECOND_USER_SURNAME = "Gargamel";
    private static final String SECOND_USER_LAST_NAME = "Goshev";

    private static final String THIRD_USER_ID = "3";
    private static final long THIRD_USER_VERSION = 1L;
    private static final String THIRD_USER_FIRST_NAME = "Kolio";
    private static final String THIRD_USER_SURNAME = "The only one";
    private static final String THIRD_USER_LAST_NAME = "Kolev";

    private static final String FORTH_USER_ID = "4";
    private static final long FORTH_USER_VERSION = 2L;
    private static final String FORTH_USER_FIRST_NAME = "Macho";
    private static final String FORTH_USER_SURNAME = "Macho";
    private static final String FORTH_USER_LAST_NAME = "Kolev";

    private Map<String, UserDTO> users = new ConcurrentHashMap<>();

    public MockManageUserService() {
        setMockupInitialData();
    }

    /**
     * Mock some initial data
     * Used only for developement purposes
     */
    private void setMockupInitialData() {
        addUserToPersistence(new UserDTO(FIRST_USER_ID, FIRST_USER_VERSION, FIRST_USER_FIRST_NAME, FIRST_USER_SURNAME, FIRST_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(SECOND_USER_ID, SECOND_USER_VERSION, SECOND_USER_FIRST_NAME, SECOND_USER_SURNAME, SECOND_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(THIRD_USER_ID, THIRD_USER_VERSION, THIRD_USER_FIRST_NAME, THIRD_USER_SURNAME, THIRD_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(FORTH_USER_ID, FORTH_USER_VERSION, FORTH_USER_FIRST_NAME, FORTH_USER_SURNAME, FORTH_USER_LAST_NAME));
    }

    /**
     * Adds a user to user map
     * Used only for development purposes.
     * @param user to be added to users map
     */
    private void addUserToPersistence(UserDTO user) {
        users.put(user.getId(), user);
    }

    @Override
    public void getAllUsers(Message<JsonObject> message) {
        JsonArray allUsers = new JsonArray(users.values().stream().map(UserDTO::toJsonObject).collect(Collectors.toList()));
        message.reply(allUsers, createResponseHeaders(FOUND));
    }

    @Override
    public void getUserById(Message<JsonObject> message) {
        String userId = message.body().getString(ID);
        UserDTO userDTO = users.getOrDefault(userId, null);
        if(userDTO == null){
            failMessage(UNEXISTING_OBJECT, message, USER_NOT_FOUND_MSG);
        } else {
            replyMessage(message, userDTO.toJsonObject(), createResponseHeaders(FOUND));
        }
    }

    @Override
    public void createUser(Message<JsonObject> message) {
        Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
        UserDTO userDTO = Json.decodeValue(message.body().toString(), UserDTO.class);
        userDTO.setUserId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(FIRST_USER_ID));
        users.put(userDTO.getId(), userDTO);
        replyMessage(message, userDTO.getId(), createResponseHeaders(CREATED));
    }

    @Override
    public void updateUser(Message<JsonObject> message) {
        String userId = message.body().getString(ID);
        UserDTO oldUserVersion = users.get(userId);
        if (oldUserVersion == null) {
            failMessage(UNEXISTING_OBJECT, message, USER_NOT_FOUND_MSG);
        } else {
            UserDTO userDTO = Json.decodeValue(message.body().toString(), UserDTO.class);
            userDTO.setVersion(oldUserVersion.getVersion() + 1);
            users.replace(userDTO.getId(), userDTO);
            replyMessage(message, null, createResponseHeaders(MERGED));
        }
    }

    @Override
    public void deleteUserById(Message<JsonObject> message) {
        UserDTO removedUser = users.remove(message.body().getString(ID));
        if(removedUser != null){
            replyMessage(message, null, createResponseHeaders(DELETED));
        } else {
            failMessage(UNEXISTING_OBJECT, message, USER_NOT_FOUND_MSG);
        }
    }
}
