package vw.server.webapi;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.sevice.ManageUserService;

public class ManageUserRestController {

    private static final String USER_ID = "userID";
    static final String USER_BY_ID_SUB_CONTEXT = "/:" + USER_ID;
    static final String ADD_USER_SUB_CONTEXT = "/add";
    static final String EDIT_USER_SUB_CONTEXT = "/edit";
    static final String GET_ALL_USERS_SUB_CONTEXT = "/getAll";

    private final Router restAPIRouter;

    private ManageUserService manageUserService;

    public ManageUserRestController(Vertx vertx, MongoClient mongoClient) {
        this.restAPIRouter = Router.router(vertx);
        this.manageUserService = new ManageUserService(mongoClient);

        configure();
    }

    private void configure() {
        // Restful api user method handlers
        restAPIRouter.get(GET_ALL_USERS_SUB_CONTEXT).handler(this::getAllUsers);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.post(ADD_USER_SUB_CONTEXT).handler(this::addUser);
        restAPIRouter.put(EDIT_USER_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    /**
     * Restful service, that retrieves all users
     * @param routingContext vertx get all users restAPIRouter routing context for restful web api
     */
    private void getAllUsers(RoutingContext routingContext) {
        manageUserService.getAllUsers(r -> {
            if(r.succeeded()){
                sendSuccess(HttpStatusCodeEnum.OK, routingContext.response(), Json.encodePrettily(r.result()));
            } else {
                sendError(HttpStatusCodeEnum.INTERNAL_SERVER_ERROR, routingContext.response());
                r.cause().printStackTrace();
            }
        });
    }

    /**
     * Restful service, that retrieves a user by id
     * @param routingContext vertx get restAPIRouter routing context for restful web api
     */
    private void getUserById(RoutingContext routingContext) {
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

    /**
     * Restful service, that creates a user
     * @param routingContext vertx create restAPIRouter routing context for restful web api
     */
    private void addUser(RoutingContext routingContext) {
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

    /**
     * Restful service, that updates a user
     * @param routingContext vertx update restAPIRouter routing context for restful web api
     */
    private void editUser(RoutingContext routingContext) {
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

    /**
     * Restful service, that deletes a user by id
     * @param routingContext vertx delete restAPIRouter routing context for restful web api
     */
    private void deleteUserById(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            manageUserService.deleteUserById(userID);
            sendError(HttpStatusCodeEnum.NO_CONTENT, response);
        }
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

    public Router getRestAPIRouter() {
        return restAPIRouter;
    }

}
