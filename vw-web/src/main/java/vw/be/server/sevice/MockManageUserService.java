package vw.be.server.sevice;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import vw.be.common.dto.UserDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository interface for users. This class is used for mocking purposes, only.
 */
public class MockManageUserService implements IManageUserService{

    public static final String FIRST_USER_ID = "1";
    public static final long FIRST_USER_VERSION = 1L;
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
    public Future<Collection<UserDTO>> getAllUsers() {
        Future<Collection<UserDTO>> result = Future.future();
        result.complete(users.values());

        return result;
    }

    @Override
    public Future<Optional<String>> getUserById(String userID) {
        Future<Optional<String>> result = Future.future();
        UserDTO user = users.getOrDefault(userID, null);
        if(user == null){
            result.complete(Optional.empty());
        } else {
            result.complete(Optional.of(Json.encodePrettily(user)));
        }

        return result;
    }

    @Override
    public Future<Boolean> createUser(UserDTO userDTO) {
        Future<Boolean> result = Future.future();
        Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
        userDTO.setUserId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(FIRST_USER_ID));
        users.put(userDTO.getId(), userDTO);
        result.complete(true);

        return result;
    }

    @Override
    public Future<Boolean> updateUser(UserDTO userDTO) {
        Future<Boolean> result = Future.future();
        UserDTO oldUserVersion = users.get(userDTO.getId());
        if (oldUserVersion == null) {
            result.complete(false);
        } else {
            userDTO.setVersion(oldUserVersion.getVersion() + 1);
            boolean isUpdated = (users.replace(userDTO.getId(), userDTO) != null);
            result.complete(isUpdated);
        }

        return result;
    }

    @Override
    public Future<Boolean> deleteUserById(String userID) {
        Future<Boolean> result = Future.future();
        UserDTO removedUser = users.remove(userID);
        result.complete(removedUser != null);

        return result;
    }
}
