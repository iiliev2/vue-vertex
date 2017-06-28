package vw.be.server.sevice;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository interface for users. This class is used for mocking purposes, only.
 */
public class MockManageUserService implements IManageUserService{

    private static final String INITIAL_USER_ID = "1";

    private Map<String, UserDTO> users = new ConcurrentHashMap<>();

    public MockManageUserService() {

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
        userDTO.setUserId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(INITIAL_USER_ID));
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
