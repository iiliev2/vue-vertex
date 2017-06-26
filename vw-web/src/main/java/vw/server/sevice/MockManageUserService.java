package vw.server.sevice;

import io.vertx.core.Future;
import io.vertx.core.json.Json;
import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * This class is used for mocking purposes, only.
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
        users.put(user.getUserId(), user);
    }

    @Override
    public Future<Collection<String>> getAllUsers() {
        Future<Collection<String>> result = Future.future();
        result.complete(users.values().stream().map(Json::encodePrettily).collect(Collectors.toList()));

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
        users.put(userDTO.getUserId(), userDTO);
        result.complete(true);

        return result;
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        UserDTO oldUserVersion = users.get(userDTO.getUserId());
        if (oldUserVersion == null) {
            return null;
        } else {
            userDTO.setVersion(oldUserVersion.getVersion() + 1);
            boolean isUpdated = (users.replace(userDTO.getUserId(), userDTO) != null);

            return (isUpdated ? userDTO : null);
        }
    }

    @Override
    public void deleteUserById(String userID) {
        users.remove(userID);
    }
}
