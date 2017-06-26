package vw.server.controller;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.sevice.MockManageUserService;

public class ManageMockUserRestController implements IManageUserRestController {

    private final Router restAPIRouter;

    private MockManageUserService manageUserService;

    public ManageMockUserRestController(Vertx vertx) {
        this.restAPIRouter = Router.router(vertx);
        this.manageUserService = new MockManageUserService();

        configure();
    }

    @Override
    public void destroy() {
    }

    private void configure() {
        // Restful api user method handlers
        restAPIRouter.get(GET_ALL_USERS_SUB_CONTEXT).handler(this::getAllUsers);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.post(ADD_USER_SUB_CONTEXT).handler(this::addUser);
        restAPIRouter.put(EDIT_USER_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    @Override
    public void getAllUsers(RoutingContext routingContext) {
        sendSuccess(HttpStatusCodeEnum.OK,
                                routingContext.response(),
                                Json.encodePrettily(manageUserService.getAllUsers(null)));
    }

    @Override
    public void getUserById(RoutingContext routingContext) {
        String userID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        if (userID == null || userID.isEmpty()) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            UserDTO userDTO = manageUserService.getUserById(userID);
            if (userDTO == null) {
                sendError(HttpStatusCodeEnum.NOT_FOUND, response);
            } else {
                sendSuccess(HttpStatusCodeEnum.OK,
                        response,
                        Json.encodePrettily(userDTO));
            }
        }
    }

    @Override
    public void addUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            final UserDTO userDTO = manageUserService.createUser(Json.decodeValue(requestBody, UserDTO.class));
            sendSuccess(HttpStatusCodeEnum.CREATED,
                    response,
                    Json.encodePrettily(userDTO));
        }
    }

    @Override
    public void editUser(RoutingContext routingContext) {
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
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            manageUserService.deleteUserById(userID);
            sendError(HttpStatusCodeEnum.NO_CONTENT, response);
        }
    }

    @Override
    public Router getRestAPIRouter() {
        return restAPIRouter;
    }
}
