package vw.be.persistence.users.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vw.be.persistence.users.dto.UserDTO;

import java.util.List;

/**
 * Repository interface for users. All methods have a default implementation which will tell the caller that the service
 * does not support the corresponding operation.
 */
@ProxyGen
public interface IManageUserService {
    /**
     * Helper method to construct the message returned when the caller invokes an unsupported method.
     *
     * @param instance the instance from which to get the name of the implementing class
     * @return the string
     */
    static String msg(IManageUserService instance) {
        return instance.getClass().getName() + " does not implement this method";
    }

    /**
     * Get all users.
     *
     * @param handler to pass the result to
     */
    default void get(Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Gets a user by id.
     *
     * @param id      of the user
     * @param handler to pass the result to
     */
    default void getById(String id, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Gets a user by name.
     *
     * @param name
     * @param handler to pass the result to
     */
    default void getByName(String name, Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Create a new user.
     *
     * @param user    is a json with the data;an id will be ignored as the service decides what the new user's ide will
     *                be
     * @param handler to pass the result to
     */
    default void create(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Update a user.
     *
     * @param user    the user json - must contain a valid id
     * @param handler to pass the result to
     */
    default void update(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Update a list of users.
     *
     * @param filter  an array of user json objects; all must contain valid user ids; will fail if at least one contains
     *                an invalid id;
     * @param handler to pass the result to
     */
    default void updateByFilter(JsonArray filter, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Delete all users. Fails if the database is already empty.
     *
     * @param handler to pass the result to
     */
    default void delete(Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Delete by id. Fails if there is no such user or the database is already empty.
     *
     * @param id      the id
     * @param handler to pass the result to
     */
    default void deleteById(String id, Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    /**
     * Delete a list of users. The array must contain strings only. Will fail if the database is already empty, or none
     * of the ids were present, or there was a problem parsing the ids.
     *
     * @param filter  with ids
     * @param handler to pass the result to
     */
    default void deleteByFilter(JsonArray filter, Handler<AsyncResult<Void>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }
}
