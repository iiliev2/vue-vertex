package vw.server.sevice;

import vw.common.dto.UserDTO;

public interface IManageUserService<T, E> {

    /**
     * Retrieves all users from persistence
     * @param handler return all users or failure cause
     */
    E getAllUsers(T handler);

    /**
     * Retrieves a user by id from persistence
     *
     * @param userID that we search
     * @return found user or null
     */
    UserDTO getUserById(String userID);

    /**
     * Creates a user in persistence
     *
     * @param userDTO user to create
     * @return created user
     */
    UserDTO createUser(UserDTO userDTO);

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
