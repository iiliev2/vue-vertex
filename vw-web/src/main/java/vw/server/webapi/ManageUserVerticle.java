package vw.server.webapi;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import vw.server.controller.ManageUserRestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ManageUserVerticle extends AbstractVerticle {

    static final int DEFAULT_HTTP_PORT_VALUE = 1;
    static final String HTTP_PORT_KEY = "http.port";

    private static final String WEB_ROOT_FOLDER = "WEB-INF";

    private static final String STATIC_RESOURCES_CONTEXT = "/*";
    private static final String REST_API_CONTEXT_PATTERN = "/api/*";
    static final String USER_WEB_API_CONTEXT = "/api/user";

    public static final String HEADER_CONTENT_TYPE = "content-type";
    public static final String APPLICATION_JSON_CHARSET_UTF_8 = "application/json; charset=UTF-8";
    static final String HEADER_CONTENT_LENGTH = "content-length";
    private static final String HEADER_X_REQUESTED_WITH = "x-requested-with";
    private static final String HEADER_ACCESS_CONTROL_ALLOW_ORIGIN = "Access-Control-Allow-Origin";
    private static final String HEADER_ORIGIN = "origin";
    private static final String HEADER_ACCEPT = "accept";
    private static final String ALLOWED_ORIGIN_PATTERN = "*";

    private static final String SERVER_STARTED_OK_MESSAGE = "%s is up and running on HTTP protocol and port %d!%n";
    private static final String SERVER_FAILED_MESSAGE = "%s failed to run on HTTP protocol and port %d! Cause is %s";


    private ManageUserRestController manageUserRestController;

    @Override
    public void start(Future<Void> startFuture) {
        //Define web api restful api handlers and start http server
        startWebApp((http) -> completeStartupHandler(http, startFuture));
    }

    @Override
    public void stop() throws Exception {
        manageUserRestController.destroy();
    }

    /**
     * Define WEB API restful methods handlers and management, define context root handler and start HTTP server
     * @param next HTTP server handler
     */
    private void startWebApp(Handler<AsyncResult<HttpServer>> next) {
        // Create a router object.
        Router applicationRouter = Router.router(vertx);

        configure(applicationRouter);

        // enable http compression (e.g. gzip js)
        final HttpServerOptions options = new HttpServerOptions().setCompressionSupported(true);

        // Create the HTTP server and pass the "accept" method to the request handler.
        vertx
                .createHttpServer(options)
                .requestHandler(applicationRouter::accept)
                .listen(
                        config().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                        next
                );
    }

    /**
     * Start up HTTP server handler. Log is server up or failed!
     * @param http        HTTP server handler
     * @param startFuture verticle start future
     */
    private void completeStartupHandler(AsyncResult<HttpServer> http, Future<Void> startFuture) {
        if (http.succeeded()) {
            startFuture.complete();
            System.out.printf(SERVER_STARTED_OK_MESSAGE, this.getClass().getSimpleName(), config().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE));
        } else {
            startFuture.fail(http.cause());
            System.err.printf(SERVER_FAILED_MESSAGE, this.getClass().getSimpleName(), config().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE), http.cause());
        }
    }

    private void configure(Router applicationRouter) {
        // CORS support
        applicationRouter.route().handler(CorsHandler.create(ALLOWED_ORIGIN_PATTERN)
                .allowedHeaders(defineAllowedCORSHeaders())
                .allowedMethods(defineAllowedCORSHttpMethods()));

        //Store post bodies in rooting context for all api calls
        applicationRouter.route(HttpMethod.POST, REST_API_CONTEXT_PATTERN).handler(BodyHandler.create());
        applicationRouter.route(HttpMethod.PUT, REST_API_CONTEXT_PATTERN).handler(BodyHandler.create());

        // mount sub router for manage users web restful api
        manageUserRestController = new ManageUserRestController(vertx, config());
        applicationRouter.mountSubRouter(USER_WEB_API_CONTEXT, manageUserRestController.getRestAPIRouter());

        //Create handler for static resources
        //Map application root context to webroot folder
        applicationRouter.route(STATIC_RESOURCES_CONTEXT).handler(StaticHandler.create(WEB_ROOT_FOLDER).setCachingEnabled(false));
    }

    private Set<HttpMethod> defineAllowedCORSHttpMethods() {
        return new HashSet<>(Collections.singletonList(HttpMethod.GET));
    }

    private Set<String> defineAllowedCORSHeaders() {
        return new HashSet<>(
                Arrays.asList(
                        HEADER_X_REQUESTED_WITH,
                        HEADER_ACCESS_CONTROL_ALLOW_ORIGIN,
                        HEADER_ORIGIN,
                        HEADER_CONTENT_TYPE,
                        HEADER_ACCEPT));
    }

}
