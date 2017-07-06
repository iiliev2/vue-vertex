package vw.be.server.service;

import io.vertx.core.MultiMap;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import vw.be.server.common.ApplicationErrorCodes;
import vw.be.server.common.PersistenceActionEnum;
import vw.be.server.common.PersistenceResponseCodeEnum;

import static vw.be.server.common.ApplicationErrorCodes.BAD_ACTION;
import static vw.be.server.common.ApplicationErrorCodes.NO_ACTION_SPECIFIED;
import static vw.be.server.common.IResourceBundleConstants.BAD_PERSISTENCE_ACTION_MSG;
import static vw.be.server.common.IResourceBundleConstants.NO_ACTION_HEADER_SPECIFIED_MSG;

/**
 * Repository interface for users
 */
public interface IManageUserService {

    String MANAGE_USER_DB_QUEUE = "manage.user.db.queue";
    String PERSISTENCE_ACTION = "action";
    String PERSISTENCE_RESPONSE_CODE = "responseCode";

    String ID = "id";

    /**
     * This method receives all events incoming events for 'manage.user.db.queue'
     *
     * @param message received message
     */
    default void onMessage(Message<JsonObject> message) {
        MultiMap headers = message.headers();
        if (!headers.contains(PERSISTENCE_ACTION)) {
            message.fail(NO_ACTION_SPECIFIED.ordinal(), NO_ACTION_HEADER_SPECIFIED_MSG);
        }

        PersistenceActionEnum action = PersistenceActionEnum.valueOf(headers.get(PERSISTENCE_ACTION));
        switch (action) {
            case GET_ALL:
                getAllUsers(message);
                break;
            case GET_BY_ID:
                getUserById(message);
                break;
            case CREATE:
                createUser(message);
                break;
            case MERGE:
                updateUser(message);
                break;
            case DELETE_BY_ID:
                deleteUserById(message);
                break;
            default:
                failMessage(BAD_ACTION, message, String.format(BAD_PERSISTENCE_ACTION_MSG, action));
        }
    }

    /**
     * It creates message reply response response code header, see {@link PersistenceResponseCodeEnum}.
     *
     * @param responseCode response code
     * @return reply delivery options with response header set.
     */
    default DeliveryOptions createResponseHeaders(PersistenceResponseCodeEnum responseCode) {
        return new DeliveryOptions().addHeader(PERSISTENCE_RESPONSE_CODE, String.valueOf(responseCode));
    }

    /**
     * It creates message failures on error or missing data.
     *
     * @param errorCode        application error code {@link ApplicationErrorCodes}
     * @param message          reply message
     * @param errorDescription application error description
     */
    default void failMessage(ApplicationErrorCodes errorCode, Message<JsonObject> message, String errorDescription) {
        message.fail(errorCode.ordinal(), errorDescription);
    }

    /**
     * It creates message reply on success.
     *
     * @param message         reply message
     * @param body            reply message body
     * @param deliveryOptions reply delivery options with response header.
     */
    default void replyMessage(Message<JsonObject> message, Object body, DeliveryOptions deliveryOptions) {
        message.reply(body, deliveryOptions);
    }

    /**
     * Close persistence container connections
     */
    default void destroy() {
    }

    /**
     * Retrieves all users from persistence.
     *
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
