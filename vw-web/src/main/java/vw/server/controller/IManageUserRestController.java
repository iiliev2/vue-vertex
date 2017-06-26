package vw.server.controller;

import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.webapi.ManageUserVerticle;

import javax.annotation.PreDestroy;

public interface IManageUserRestController {

    String USER_ID = "userID";
    String USER_BY_ID_SUB_CONTEXT = "/:" + USER_ID;
    String ADD_USER_SUB_CONTEXT = "/add";
    String EDIT_USER_SUB_CONTEXT = "/edit";
    String GET_ALL_USERS_SUB_CONTEXT = "/getAll";

    @PreDestroy
    void destroy();

    /**
     * Restful service, that retrieves all users
     * @param routingContext vertx get all users restAPIRouter routing context for restful web api
     */
    void getAllUsers(RoutingContext routingContext);

    /**
     * Restful service, that retrieves a user by id
     * @param routingContext vertx get restAPIRouter routing context for restful web api
     */
    void getUserById(RoutingContext routingContext);

    /**
     * Restful service, that creates a user
     * @param routingContext vertx create restAPIRouter routing context for restful web api
     */
    void addUser(RoutingContext routingContext);

    /**
     * Restful service, that updates a user
     * @param routingContext vertx update restAPIRouter routing context for restful web api
     */
    void editUser(RoutingContext routingContext);

    /**
     * Restful service, that deletes a user by id
     * @param routingContext vertx delete restAPIRouter routing context for restful web api
     */
    void deleteUserById(RoutingContext routingContext);

    Router getRestAPIRouter();

    default void sendError(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .end();
    }

    default void sendSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response, String responseContent) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .putHeader(ManageUserVerticle.HEADER_CONTENT_TYPE, ManageUserVerticle.APPLICATION_JSON_CHARSET_UTF_8)
                .end(responseContent);
    }


}
