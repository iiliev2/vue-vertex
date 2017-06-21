package vw.server.webapi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ManageUserRestService extends AbstractVerticle {

    static final int HTTP_PORT = 23000;

    static final String CONTENT_TYPE = "content-type";
    static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json";
    static final String TEXT_HTML = "text/html";
    static final String CONTENT_LENGTH_HEADER = "content-length";

    static final String CONTEXT_ROOT = "/";
    static final String USER_ID = "userID";
    static final String URL_USER_BY_ID = "/api/user/:" + USER_ID;
    static final String URL_ADD_USER = "/api/user/create";
    static final String URL_GET_ALL_USERS = "/api/users";
    static final String USER_CONTEXT = "users";
    static final String STATIC_RESOURCES_CONTEXT = "/" + USER_CONTEXT + "/*";

    static final String ROOT_CONTEXT_WELCOME_MESSAGE = "<h1>Vert.x application is up and running!</h1>";

    private static final String INITIAL_USER_ID = "1";

    private static final String SERVER_STARTED_OK_MESSAGE = "%s is up and running on HTTP protocol and port 8080!%n";
    private static final String SERVER_FAILED_MESSAGE = "%s failed to run on HTTP protocol and port 8080! Cause is %s";

    private Map<String, UserDTO> users = new HashMap<>();

    @Override
    public void start(Future<Void> startFuture) {
        //Setup mock data
        setUpInitialData();

        //Define web api restful api handlers and start http server
        startWebApp((http) -> completeStartup(http, startFuture));
    }

    /**
     * Define WEB API restful methods handlers and management, define context root handler and start HTTP server
     * @param next HTTP server handler
     */
    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        // Create a router object.
        Router restAPIRouter = Router.router(vertx);

        //Create handler for static resources
        restAPIRouter.route().handler(BodyHandler.create());

        // Bind "/" to our hello message.
        bindRootContext(restAPIRouter);

        // Restful api method handlers
        restAPIRouter.get(URL_GET_ALL_USERS).handler(this::getAllUsers);
        restAPIRouter.get(URL_USER_BY_ID).handler(this::getUserById);
        restAPIRouter.post(URL_ADD_USER).handler(this::createUser);
        restAPIRouter.delete(URL_USER_BY_ID).handler(this::deleteUser);

        restAPIRouter.route(STATIC_RESOURCES_CONTEXT).handler(StaticHandler.create(USER_CONTEXT));

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer()
                .requestHandler(restAPIRouter::accept)
                .listen(
                        HTTP_PORT,
                        next
                );
    }

    /**
     * Start up HTTP server handler. Log is server up or failed!
     * @param http HTTP server handler
     * @param startFuture verticle start future
     */
    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> startFuture) {
        if (http.succeeded()) {
            startFuture.complete();
            System.out.printf(SERVER_STARTED_OK_MESSAGE, this.getClass().getSimpleName());
        } else {
            startFuture.fail(http.cause());
            System.err.printf(SERVER_FAILED_MESSAGE, this.getClass().getSimpleName(), http.cause());
        }
    }

    /**
     * Bind context ROOOt '/' to some welcome page
     * @param restAPIRouter vertx ROOT('/') routing context
     */
    private void bindRootContext(Router restAPIRouter) {
        restAPIRouter.route(CONTEXT_ROOT).handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                    .putHeader(CONTENT_TYPE, TEXT_HTML)
                    .end(ROOT_CONTEXT_WELCOME_MESSAGE);
        });
    }

    /**
     * Restful service, that retrieves all users
     * @param routingContext vertx get all users router routing context for restful web api
     */
    private void getAllUsers(RoutingContext routingContext) {
        routingContext.response()
                .putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .end(Json.encodePrettily(users.values()));
    }

    /**
     * Restful service, that retrieves a user by id
     * @param routingContext vertx get router routing context for restful web api
     */
    private void getUserById(RoutingContext routingContext) {
        String userID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        if (userID == null || userID.isEmpty()) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            UserDTO user = users.get(userID);
            if (user == null) {
                sendError(HttpStatusCodeEnum.NOT_FOUND, response);
            } else {
                response.putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8).end(Json.encodePrettily(user));
            }
        }
    }

    /**
     * Restful service, that creates a user
     * @param routingContext vertx create router routing context for restful web api
     */
    private void createUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            final UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
            Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
            userDTO.setId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(INITIAL_USER_ID));
            users.put(userDTO.getId(), userDTO);
            routingContext.response()
                    .setStatusCode(HttpStatusCodeEnum.CREATED.getStatusCode())
                    .putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                    .end(Json.encodePrettily(userDTO));
        }
    }

    /**
     * Restful service, that deletes a user by id
     * @param routingContext vertx delete router routing context for restful web api
     */
    private void deleteUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            users.remove(userID);
            sendError(HttpStatusCodeEnum.NO_CONTENT, response);
        }
    }

    private void sendError(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode.getStatusCode()).end();
    }

    /**
     * Mock some initial data
     * Used only for developement purposes
     */
    private void setUpInitialData() {
        addUser(new UserDTO("1", 1L, "Pesho", "Stupid", "Peshov"));
        addUser(new UserDTO("2", 1L, "Gosho", "Gargamel", "Goshev"));
        addUser(new UserDTO("3", 1L, "Kolio", "Paveto", "Kolev"));
        addUser(new UserDTO("4", 2L, "Kolio", "Mekereto", "Kolev"));
    }

    /**
     * Adds a user to user map
     * Used only for developement purposes.
     * @param user to be added to users map
     */
    private void addUser(UserDTO user) {
        users.put(user.getId(), user);
    }
}
