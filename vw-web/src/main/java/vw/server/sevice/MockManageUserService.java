package vw.server.sevice;

import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is used for mocking purposes, only.
 */
public class MockManageUserService implements IManageUserService<Void, Collection<UserDTO>>{

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
    public Collection<UserDTO> getAllUsers(Void handler) {
        return users.values();
    }

    @Override
    public UserDTO getUserById(String userID) {
        return users.getOrDefault(userID, null);
    }

    @Override
    public UserDTO createUser(UserDTO userDTO) {
        Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
        userDTO.setId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(INITIAL_USER_ID));
        users.put(userDTO.getId(), userDTO);

        return userDTO;
    }

    @Override
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

    @Override
    public void deleteUserById(String userID) {
        users.remove(userID);
    }
}
