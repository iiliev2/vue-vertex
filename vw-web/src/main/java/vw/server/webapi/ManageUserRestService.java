package vw.server.webapi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;

import java.util.*;

public class ManageUserRestService extends AbstractVerticle {

    static final int HTTP_PORT = 23000;

    static final String CONTENT_TYPE = "content-type";
    static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=UTF-8";
    static final String TEXT_HTML = "text/html";
    static final String CONTENT_LENGTH_HEADER = "content-length";

    static final String CONTEXT_ROOT = "/";
    static final String USER_ID = "userID";
    static final String URL_USER_BY_ID = "/api/user/:" + USER_ID;
    static final String URL_ADD_USER = "/api/user/create";
    static final String URL_EDIT_USER = "/api/user/update";
    static final String USER_CONTEXT = "users";
    static final String URL_GET_ALL_USERS = "/api/" + USER_CONTEXT;
    static final String STATIC_RESOURCES_CONTEXT = "/" + USER_CONTEXT + "/*";

    static final String ROOT_CONTEXT_WELCOME_MESSAGE = "<h1>Vert.x application is up and running!</h1>";

    private static final String INITIAL_USER_ID = "1";

    private static final String SERVER_STARTED_OK_MESSAGE = "%s is up and running on HTTP protocol and port %d!%n";
    private static final String SERVER_FAILED_MESSAGE = "%s failed to run on HTTP protocol and port %d! Cause is %s";

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

        // CORS support
        restAPIRouter.route().handler(CorsHandler.create("*")
                .allowedHeaders(defineAllowedHeaders())
                .allowedMethods(defineAllowedHttpMethods()));

        //Create handler for static resources
        restAPIRouter.route().handler(BodyHandler.create());

        // Bind "/" to our hello message.
        bindRootContext(restAPIRouter);

        // Restful api method handlers
        restAPIRouter.get(URL_GET_ALL_USERS).handler(this::getAllUsers);
        restAPIRouter.get(URL_USER_BY_ID).handler(this::getUserById);
        restAPIRouter.post(URL_ADD_USER).handler(this::addUser);
        restAPIRouter.put(URL_EDIT_USER).handler(this::editUser);
        restAPIRouter.delete(URL_USER_BY_ID).handler(this::deleteUserById);

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

    private Set<HttpMethod> defineAllowedHttpMethods() {
        Set<HttpMethod> allowMethods = new HashSet<>();
        allowMethods.add(HttpMethod.GET);
        allowMethods.add(HttpMethod.POST);
        allowMethods.add(HttpMethod.PUT);
        allowMethods.add(HttpMethod.DELETE);
        return allowMethods;
    }

    private Set<String> defineAllowedHeaders() {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.add("x-requested-with");
        allowHeaders.add("Access-Control-Allow-Origin");
        allowHeaders.add("origin");
        allowHeaders.add("Content-Type");
        allowHeaders.add("accept");
        return allowHeaders;
    }

    /**
     * Start up HTTP server handler. Log is server up or failed!
     * @param http HTTP server handler
     * @param startFuture verticle start future
     */
    private void completeStartup(AsyncResult<HttpServer> http, Future<Void> startFuture) {
        if (http.succeeded()) {
            startFuture.complete();
            System.out.printf(SERVER_STARTED_OK_MESSAGE, this.getClass().getSimpleName(), HTTP_PORT);
        } else {
            startFuture.fail(http.cause());
            System.err.printf(SERVER_FAILED_MESSAGE, this.getClass().getSimpleName(), HTTP_PORT, http.cause());
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
        sendSuccess(HttpStatusCodeEnum.OK,
                routingContext.response(),
                Json.encodePrettily(users.values()));
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
            UserDTO userDTO = users.get(userID);
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
     * @param routingContext vertx create router routing context for restful web api
     */
    private void addUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            final UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
            Optional<String> maxUserId = users.keySet().stream().max(Comparator.naturalOrder());
            userDTO.setId(maxUserId.map(value -> String.valueOf(Long.valueOf(value) + 1)).orElse(INITIAL_USER_ID));
            users.put(userDTO.getId(), userDTO);

            sendSuccess(HttpStatusCodeEnum.CREATED,
                    response,
                    Json.encodePrettily(userDTO));
        }
    }

    /**
     * Restful service, that updates a user
     * @param routingContext vertx update router routing context for restful web api
     */
    private void editUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String requestBody = routingContext.getBodyAsString();
        if (requestBody == null) {
            sendError(HttpStatusCodeEnum.BAD_REQUEST, response);
        } else {
            final UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
            UserDTO oldUserVersion = users.get(userDTO.getId());
            if(oldUserVersion == null){
                sendError(HttpStatusCodeEnum.NOT_FOUND, response);
            } else {
                userDTO.setVersion(oldUserVersion.getVersion() + 1);
                users.put(userDTO.getId(), userDTO);
                sendSuccess(HttpStatusCodeEnum.OK,
                        response,
                        Json.encodePrettily(userDTO));
            }
        }
    }

    /**
     * Restful service, that deletes a user by id
     * @param routingContext vertx delete router routing context for restful web api
     */
    private void deleteUserById(RoutingContext routingContext) {
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
        response
                .setStatusCode(statusCode.getStatusCode())
                .end();
    }

    private void sendSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response, String responseContent) {
        response
                .setStatusCode(statusCode.getStatusCode())
                .putHeader(CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .end(responseContent);
    }

    /**
     * Mock some initial data
     * Used only for developement purposes
     */
    private void setUpInitialData() {
        addUserToContentManagement(new UserDTO("1", 1L, "Pesho", "Stupid", "Peshov"));
        addUserToContentManagement(new UserDTO("2", 1L, "Gosho", "Gargamel", "Goshev"));
        addUserToContentManagement(new UserDTO("3", 1L, "Kolio", "The only one", "Kolev"));
        addUserToContentManagement(new UserDTO("4", 2L, "Macho", "Macho", "Kolev"));
    }

    /**
     * Adds a user to user map
     * Used only for developement purposes.
     * @param user to be added to users map
     */
    private void addUserToContentManagement(UserDTO user) {
        users.put(user.getId(), user);
    }
}
