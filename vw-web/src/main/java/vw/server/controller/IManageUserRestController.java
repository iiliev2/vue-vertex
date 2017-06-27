package vw.server.controller;

import io.vertx.ext.web.RoutingContext;

/**
 * User restful web api controller interface. It exposes CRUD operations.
 */
public interface IManageUserRestController {

    String USER_ID = "userID";
    String USER_BY_ID_SUB_CONTEXT = "/:" + USER_ID;

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

}
