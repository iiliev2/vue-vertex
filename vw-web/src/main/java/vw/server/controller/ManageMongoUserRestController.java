package vw.server.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.sevice.MongoManageUserService;

public class ManageMongoUserRestController implements IManageUserRestController{

    private MongoClient mongoClient;

    private final Router restAPIRouter;

    private MongoManageUserService manageUserService;

    public ManageMongoUserRestController(Vertx vertx, JsonObject config) {
        this.restAPIRouter = Router.router(vertx);

        configure(vertx, config);

        // Create a Mongo client
        mongoClient = MongoClient.createShared(vertx, config);

        this.manageUserService = new MongoManageUserService(mongoClient);
    }

    @Override
    public void destroy() {
        mongoClient.close();
    }

    private void configure(Vertx vertx, JsonObject config) {
        // Restful api user method handlers
        restAPIRouter.get(GET_ALL_USERS_SUB_CONTEXT).handler(this::getAllUsers);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.post(ADD_USER_SUB_CONTEXT).handler(this::addUser);
        restAPIRouter.put(EDIT_USER_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    @Override
    public void getAllUsers(RoutingContext routingContext) {
        manageUserService.getAllUsers(r -> {
            if(r.succeeded()){
                sendSuccess(HttpStatusCodeEnum.OK, routingContext.response(), Json.encodePrettily(r.result()));
            } else {
                sendError(HttpStatusCodeEnum.INTERNAL_SERVER_ERROR, routingContext.response());
                r.cause().printStackTrace();
            }
        });
    }

    @Override
    public void getUserById(RoutingContext routingContext) {

    }

    @Override
    public void addUser(RoutingContext routingContext) {

    }

    @Override
    public void editUser(RoutingContext routingContext) {

    }

    @Override
    public void deleteUserById(RoutingContext routingContext) {
    }

    @Override
    public Router getRestAPIRouter() {
        return restAPIRouter;
    }

}
