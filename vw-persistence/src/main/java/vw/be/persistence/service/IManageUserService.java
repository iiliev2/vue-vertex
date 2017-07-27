package vw.be.persistence.service;

import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import vw.be.persistence.dto.UserDTO;

import java.util.List;

/**
 * Repository interface for users
 */
@ProxyGen
public interface IManageUserService {
    static String msg(IManageUserService instance) {
        return instance.getClass().getName() + " does not implement this method";
    }

    default void get(Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void getById(long id, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void getByName(String name, Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void create(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void update(JsonObject user, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void updateByFilter(JsonArray filter, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void delete(Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void deleteById(long id, Handler<AsyncResult<UserDTO>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }

    default void deleteByFilter(JsonArray filter, Handler<AsyncResult<List<UserDTO>>> handler) {
        handler.handle(Future.failedFuture(msg(this)));
    }
}
