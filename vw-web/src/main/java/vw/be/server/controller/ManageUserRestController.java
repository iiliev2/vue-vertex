package vw.be.server.controller;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.be.server.common.HttpStatusCodeEnum;
import vw.be.server.service.IManageUserService;

import static vw.be.server.common.HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE;
import static vw.be.server.common.IConfigurationConstants.ROUTE_ROOT;
import static vw.be.server.common.IHttpApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.server.common.IHttpApiConstants.HEADER_CONTENT_TYPE;
import static vw.be.server.common.PersistenceActionEnum.*;
import static vw.be.server.service.IManageUserService.ID;
import static vw.be.server.service.IManageUserService.PERSISTENCE_ACTION;

/**
 * Users restful web api vertx asynchronous implementation.
 */
public class ManageUserRestController implements IManageUserRestController {

    private final Router restAPIRouter;

    private Vertx vertx;

    public ManageUserRestController(Vertx vertx, JsonObject config) {
        this.vertx = vertx;
        this.restAPIRouter = Router.router(vertx);

        configure();
    }

    /**
     * Restful api user method handlers
     */
    private void configure() {
        restAPIRouter.get(ROUTE_ROOT).handler(this::getAllUsers);
        restAPIRouter.post(ROUTE_ROOT).handler(this::addUser);
        restAPIRouter.put(ROUTE_ROOT).handler(this::replaceAllUsers);
        restAPIRouter.put(USER_BY_ID_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    @Override
    public void getAllUsers(RoutingContext routingContext) {
        DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_ALL));

        vertx.eventBus().send(IManageUserService.DB_QUEUE, new JsonObject(), options, reply -> {
            if (reply.succeeded()) {
                sendResponseSuccess(HttpStatusCodeEnum.OK,
                        routingContext.response(),
                        Json.encodePrettily(reply.result().body()));
            } else {
                sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
                        routingContext.response());
            }
        });
    }

    @Override
    public void getUserById(RoutingContext routingContext) {
        String userID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        if (userID == null || userID.isEmpty()) {
            sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_BY_ID));
            JsonObject request = new JsonObject().put(ID, userID);
            vertx.eventBus().send(IManageUserService.DB_QUEUE, request, options, reply -> {
                if (reply.succeeded()) {
                    sendResponseSuccess(HttpStatusCodeEnum.OK,
                            response,
                            Json.encodePrettily(reply.result().body()));
                } else {
                    sendResponse(HttpStatusCodeEnum.NOT_FOUND,
                            response);
                }
            });
        }
    }

    @Override
    public void addUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        JsonObject requestBody = routingContext.getBodyAsJson();
        if (requestBody == null) {
            sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE));
            vertx.eventBus().send(IManageUserService.DB_QUEUE, requestBody, options, reply -> {
                if (reply.succeeded()) {
                    sendResponseSuccess(HttpStatusCodeEnum.CREATED,
                            response, reply.result().body().toString());
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
                            routingContext.response());
                }
            });
        }
    }

    public void replaceAllUsers(RoutingContext routingContext) {
    }

    @Override
    public void editUser(RoutingContext routingContext) {
        String uriUserID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        JsonObject requestBody = routingContext.getBodyAsJson();
        //TODO user id can be in requset body. ADD logic!!!
        if (requestBody == null || !matchingUserID(requestBody.getString(ID), uriUserID)) {
            sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(MERGE));
            vertx.eventBus().send(IManageUserService.DB_QUEUE, requestBody.put(ID, uriUserID), options, reply -> {
                if (reply.succeeded()) {
                    sendResponse(HttpStatusCodeEnum.OK,
                            response);
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
                            routingContext.response());
                }
            });
        }
    }

    @Override
    public void deleteUserById(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(DELETE_BY_ID));
            JsonObject request = new JsonObject().put(ID, userID);
            vertx.eventBus().send(IManageUserService.DB_QUEUE, request, options, reply -> {
                if (reply.succeeded()) {
                    sendResponse(HttpStatusCodeEnum.NO_CONTENT, response);
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
                            routingContext.response());
                }
            });
        }
    }

    /**
     * Send response with HTTP code given as argument.
     */
    private void sendResponse(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .end();
    }

    /**
     * Send response with HTTP code and content given as arguments.
     */
    private void sendResponseSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response, String responseContent) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .end(responseContent);
    }

    /**
     * Router field getter method.
     *
     * @return Controller router object.
     */
    public Router getRestAPIRouter() {
        return restAPIRouter;
    }

    private boolean matchingUserID(String userId, String URI_ID) {
        return URI_ID != null && (userId == null || URI_ID.equals(userId));
    }

}
