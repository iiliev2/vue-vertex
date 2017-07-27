package vw.be.persistence.inmem;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vw.be.persistence.dto.UserDTO;
import vw.be.persistence.dto.UserDTOConverter;
import vw.be.persistence.service.IManageUserService;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

//TODO make distributable via haselcast
public class InMemoryUserService implements IManageUserService {
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

    private static Map<String, UserDTO> users = new ConcurrentHashMap<>();

    static {
        setMockupInitialData();
    }

    /**
     * Mock some initial data Used only for developement purposes
     */
    private static void setMockupInitialData() {
        addUserToPersistence(new UserDTO(FIRST_USER_ID, FIRST_USER_VERSION, FIRST_USER_FIRST_NAME, FIRST_USER_SURNAME,
                                         FIRST_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(SECOND_USER_ID, SECOND_USER_VERSION, SECOND_USER_FIRST_NAME,
                                         SECOND_USER_SURNAME, SECOND_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(THIRD_USER_ID, THIRD_USER_VERSION, THIRD_USER_FIRST_NAME, THIRD_USER_SURNAME,
                                         THIRD_USER_LAST_NAME));
        addUserToPersistence(new UserDTO(FORTH_USER_ID, FORTH_USER_VERSION, FORTH_USER_FIRST_NAME, FORTH_USER_SURNAME,
                                         FORTH_USER_LAST_NAME));
    }

    /**
     * Adds a user to user map Used only for development purposes.
     *
     * @param user to be added to users map
     */
    private static void addUserToPersistence(UserDTO user) {
        users.put(user.getId(), user);
    }

    @Override
    public void get(Handler<AsyncResult<List<UserDTO>>> handler) {
        JsonArray allUsers = new JsonArray(
                users.values().stream().map(UserDTO::toJson).collect(Collectors.toList()));
        handler.handle(Future.succeededFuture(allUsers.getList()));
    }

    @Override
    public void getById(long id, Handler<AsyncResult<UserDTO>> handler) {
        UserDTO userDTO = users.getOrDefault(id, null);
        if (userDTO == null) {
            handler.handle(Future.failedFuture("No user with id " + id + " exists"));
        } else {
            handler.handle(Future.succeededFuture(userDTO));
        }
    }

    @Override
    public void getByName(String name, Handler<AsyncResult<List<UserDTO>>> handler) {
        List allUsers = users.values().stream().filter(user -> user.getFirstName().contains(name) ||
                                                               user.getSurname().contains(name) ||
                                                               user.getLastName().contains(name)
                                                      ).collect(Collectors.toList());
        if (allUsers.isEmpty()) handler.handle(Future.failedFuture("No users exist with the name " + name));
        else
            handler.handle(Future.succeededFuture(allUsers));
    }

    @Override
    public void create(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
        UserDTO userDTO = new UserDTO();
        UserDTOConverter.fromJson(user, userDTO);
        userDTO.setUserId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(FIRST_USER_ID));
        users.put(userDTO.getId(), userDTO);
        handler.handle(Future.succeededFuture(userDTO));
    }

    @Override
    public void update(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        UserDTO userDTO = new UserDTO();
        UserDTOConverter.fromJson(user, userDTO);
        String userId = userDTO.getId();
        UserDTO oldUserVersion = users.get(userId);
        if (oldUserVersion == null) {
            handler.handle(Future.failedFuture("No user with id " + userId + " exists"));
        } else {
            userDTO.setVersion(oldUserVersion.getVersion() + 1);
            users.replace(userDTO.getId(), userDTO);
            handler.handle(Future.succeededFuture(userDTO));
        }
    }

    @Override
    public void deleteById(long id, Handler<AsyncResult<UserDTO>> handler) {
        UserDTO removedUser = users.remove(id);
        if (removedUser != null) {
            handler.handle(Future.failedFuture("No user with id " + id + " exists"));
        } else {
            handler.handle(Future.succeededFuture(removedUser));
        }
    }
}
