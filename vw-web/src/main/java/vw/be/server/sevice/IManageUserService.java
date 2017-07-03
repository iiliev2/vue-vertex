package vw.be.server.sevice;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Repository interface for users
 */
public interface IManageUserService {

    String DB_QUEUE = "manage.user.db.queue";

    default void onMessage(Message<JsonObject> message){
        if (!message.headers().contains("action")) {
            //message.fail(ErrorCodes.NO_ACTION_SPECIFIED.ordinal(), "No action header specified");
        }
        String action = message.headers().get("action");

        switch (action) {
            case "all-users":
                getAllUsers(message);
                break;
            case "get-user-by-id":
                getUserById(message);
                break;
            case "create-user":
                createUser(message);
                break;
            case "edit-user":
                updateUser(message);
                break;
            case "delete-user-by-id":
                deleteUserById(message);
                break;
            default:
                //message.fail(ErrorCodes.BAD_ACTION.ordinal(), "Bad action: " + action);
        }
    }

    /**
     * Close persistence container connections
     */
    default void destroy(){
    };

    /**
     * Retrieves all users from persistence.
     * @param message empty message
     */
    void getAllUsers(Message<JsonObject> message);

    /**
     * Retrieves a user by id from persistence
     *
     * @param message get-by-id action to be executed. Id of user that we search.
     */
    void getUserById(Message<JsonObject> message);

    /**
     * Creates a user in persistence
     *
     * @param message create action to be executed. User to create.
     */
    void createUser(Message<JsonObject> message);

    /**
     * Updates a user in persistence
     *
     * @param message user to update
     */
    void updateUser(Message<JsonObject> message);

    /**
     * Deletes a user by id
     *
     * @param message action to be executed. User to be deleted.
     */
    void deleteUserById(Message<JsonObject> message);
}
