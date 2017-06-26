package vw.server.sevice;

import io.vertx.core.Future;
import vw.common.dto.UserDTO;

import java.util.Collection;
import java.util.Optional;

public interface IManageUserService {

    /**
     * Close persistence container connections
     */
    default void destroy(){
    };

    /**
     * Retrieves all users from persistence
     */
    Future<Collection<String>> getAllUsers();

    /**
     * Retrieves a user by id from persistence
     *
     * @param userID that we search
     * @return found user or null
     */
    Future<Optional<String>> getUserById(String userID);

    /**
     * Creates a user in persistence
     *
     * @param userDTO user to create
     * @return created user
     */
    Future<Boolean> createUser(UserDTO userDTO);

    /**
     * Updates a user in persistence
     * @param userDTO user to update
     * @return updated user or null if user does not exists
     */
    UserDTO updateUser(UserDTO userDTO);

    /**
     * Deletes a user by id
     * @param userID user to be deleted
     */
    void deleteUserById(String userID);
}
