package vw.be.server.sevice;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import io.vertx.core.Future;
import vw.be.common.dto.UserDTO;

/**
 * Repository interface for users
 */
public interface IManageUserService {

	/**
	 * Close persistence container connections
	 */
	default void destroy() {
	};

	/**
	 * Retrieves all users from persistence.
	 *
	 * @return all users as json
	 */
	Future<Collection<UserDTO>> getAllUsers();

	/**
	 * Retrieves a user by id from persistence
	 *
	 * @param userID
	 *            that we search
	 * @return found user or null
	 */
	Future<Optional<String>> getUserById(String userID);

	/**
	 * Creates a user in persistence
	 *
	 * @param userDTO
	 *            user to create
	 * @return is user created
	 */
	Future<Boolean> createUser(UserDTO userDTO);

	/**
	 * Updates a user in persistence
	 *
	 * @param userDTO
	 *            user to update
	 * @return is user updated
	 */
	Future<Boolean> updateUser(UserDTO userDTO);

	/**
	 * Deletes a user by id
	 *
	 * @param userID
	 *            user to be deleted
	 * @return is user deleted
	 */
	Future<Boolean> deleteUserById(String userID);

	/**
	 * Replace the entire collection of users with a new one.
	 *
	 * @param users
	 *            the new collection
	 * @return has the collection successfully been replaced
	 */
	Future<Boolean> replaceAllUsers(Collection<UserDTO> users);

	/**
	 * Delete the entire collection.
	 *
	 * @return has the collection successfully been deleted
	 */
	Future<Boolean> delete();

	/**
	 * Delete all users, whos ids are found in the input set.
	 *
	 * @param ids
	 *            set of ids to search for and delete
	 * @return has the collection successfully been deleted
	 */
	Future<Boolean> delete(Set<String> ids);
}
