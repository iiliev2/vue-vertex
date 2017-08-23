package vw.be.persistence.users.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vw.be.persistence.users.dto.UserDTO;
import vw.be.persistence.users.dto.UserDTOConverter;
import vw.be.persistence.users.service.IManageUserService;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InMemoryUserService implements IManageUserService {
    private static final Map<String, UserDTO> users = new ConcurrentHashMap<>();

    public static List<UserDTO> getUsers() {
        return new ArrayList<>(users.values());
    }

    public static final String FIRST_USER_ID = "1";

    public InMemoryUserService(JsonObject config) {
        if (users.size() == 0) {
            JsonArray usersJson = config.getJsonArray("users");
            if (usersJson != null) {
                UserDTO userDTO;
                JsonObject userJson;
                for (int i = 0; i < usersJson.size(); i++) {
                    userJson = usersJson.getJsonObject(i);
                    if (userJson != null) {
                        userDTO = new UserDTO(userJson);
                        users.put(userDTO.getId(), userDTO);
                    }
                }
            }
        }
    }

    @Override
    public void get(Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.succeededFuture(new ArrayList<>(users.values())));
    }

    @Override
    public void getById(String id, Handler<AsyncResult<UserDTO>> handler) {
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
        userDTO.setId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(FIRST_USER_ID));
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
    public void deleteById(String id, Handler<AsyncResult<Void>> handler) {
        if (users.size() == 0) {
            handler.handle(Future.failedFuture("The database is already empty"));
            return;
        }
        UserDTO removedUser = users.remove(id);
        if (removedUser == null) {
            handler.handle(Future.failedFuture("No user with id " + id + " exists"));
        } else {
            handler.handle(Future.succeededFuture());
        }
    }

    @Override public void delete(Handler<AsyncResult<Void>> handler) {
        if (users.size() == 0) handler.handle(Future.failedFuture("The database is already empty"));
        else {
            users.clear();
            handler.handle(Future.succeededFuture());
        }
    }

    @Override public void deleteByFilter(JsonArray filter, Handler<AsyncResult<Void>> handler) {
        int sizeBefore = users.size();
        if (sizeBefore == 0) {
            handler.handle(Future.failedFuture("The database is already empty"));
            return;
        }
        try {
            filter.forEach(users::remove);
        } catch (ClassCastException cce) {
            handler.handle(Future.failedFuture("One or more of the ids could not be parsed"));
            return;
        }
        long sizeAfter = users.size();
        if (sizeBefore - sizeAfter == 0)
            handler.handle(Future.failedFuture("None of the ids were present in the database"));
        else handler.handle(Future.succeededFuture());

    }
}
