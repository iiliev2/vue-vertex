package vw.server.controller;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.factory.ManageUserServiceFactory;
import vw.server.sevice.IManageUserService;
import vw.server.webapi.ManageUserVerticle;

import java.util.function.Consumer;

public class ManageUserRestController implements IManageUserRestController {

    private final Router restAPIRouter;

    private IManageUserService manageUserService;

    public ManageUserRestController(Vertx vertx, JsonObject config) {
        this.restAPIRouter = Router.router(vertx);
        this.manageUserService = ManageUserServiceFactory.getService(vertx, config);

        configure();
    }

    private void configure() {
        //TODO put urls in config file
        // Restful api user method handlers
        restAPIRouter.get(GET_ALL_USERS_SUB_CONTEXT).handler(this::getAllUsers);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.post(ADD_USER_SUB_CONTEXT).handler(this::addUser);
        restAPIRouter.put(EDIT_USER_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    public void destroy() {
        manageUserService.destroy();
    }

    @Override
    public void getAllUsers(RoutingContext routingContext) {
        manageUserService
                .getAllUsers()
                .setHandler(
                        resultHandler(routingContext.response(), res -> {
                            if (res == null) {
                                sendError(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE,
                                        routingContext.response());
                            } else {
                                sendSuccess(HttpStatusCodeEnum.OK,
                                        routingContext.response(),
                                        Json.encodePrettily(res));
                            }
                        })
                );
    }

    @Override
    public void getUserById(RoutingContext routingContext) {
        String userID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        if (userID == null || userID.isEmpty()) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            manageUserService.getUserById(userID).setHandler(
                    resultHandler(routingContext.response(), res -> {
                        if (!res.isPresent()) {
                            sendError(HttpStatusCodeEnum.NOT_FOUND,
                                    response);
                        } else {
                            sendSuccess(HttpStatusCodeEnum.OK,
                                    response,
                                    res.get());
                        }
                    })
            );
        }
    }

    @Override
    public void addUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
            manageUserService.createUser(userDTO).setHandler(
                    resultHandler(routingContext.response(), res -> {
                        if (res) {
                            sendSuccess(HttpStatusCodeEnum.CREATED,
                                    response,
                                    Json.encodePrettily(userDTO));
                        } else {
                            sendError(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE,
                                    routingContext.response());

                        }
                    })
                );
        }
    }

    @Override
    public void editUser(RoutingContext routingContext) {
        //TODO add implementation for mongo, these work for mocks, only
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {

            final UserDTO userDTO = manageUserService.updateUser(Json.decodeValue(requestBody, UserDTO.class));
            if(userDTO == null){
                sendError(HttpStatusCodeEnum.NOT_FOUND, response);
            } else {
                sendSuccess(HttpStatusCodeEnum.OK,
                        response,
                        Json.encodePrettily(userDTO));
            }
        }
    }

    @Override
    public void deleteUserById(RoutingContext routingContext) {
        //TODO add implementation for mongo, these work for mocks, only
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            manageUserService.deleteUserById(userID);
            sendError(HttpStatusCodeEnum.NO_CONTENT, response);
        }
    }

    public Router getRestAPIRouter() {
        return restAPIRouter;
    }

    /**
     * Wrap the result handler with failure handler (503 Service Unavailable)
     */
    private <T> Handler<AsyncResult<T>> resultHandler(HttpServerResponse response, Consumer<T> consumer) {
        return res -> {
            if (res.succeeded()) {
                consumer.accept(res.result());
            } else {
                sendError(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, response);
            }
        };
    }

    private void sendError(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .end();
    }

    private void sendSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response, String responseContent) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .putHeader(ManageUserVerticle.HEADER_CONTENT_TYPE, ManageUserVerticle.APPLICATION_JSON_CHARSET_UTF_8)
                .end(responseContent);
    }
}
