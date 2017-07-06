package vw.be.server.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.StaticHandler;
import vw.be.server.controller.ManageUserRestController;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static vw.be.server.common.IConfigurationConstants.*;
import static vw.be.server.common.IHttpApiConstants.*;
import static vw.be.server.common.IResourceBundleConstants.HTTP_SERVER_FAILED_MESSAGE;
import static vw.be.server.common.IResourceBundleConstants.HTTP_SERVER_STARTED_OK_MESSAGE;

public class HttpVerticle extends AbstractVerticle {

    private static final String WEB_ROOT_FOLDER = "WEB-INF";
    private static final String STATIC_RESOURCES_CONTEXT = "/*";

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpVerticle.class);

    @Override
    public void start(Future<Void> startFuture) {
        startWebApp((http) -> completeStartupHandler(http, startFuture));
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
            LOGGER.info(String.format(HTTP_SERVER_STARTED_OK_MESSAGE, config().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE)));
        } else {
            startFuture.fail(http.cause());
            LOGGER.error(String.format(HTTP_SERVER_FAILED_MESSAGE, config().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE), http.cause()));
        }
    }

    private void configure(Router applicationRouter) {
        // CORS support
        applicationRouter.route().handler(CorsHandler.create(ALLOWED_ORIGIN_PATTERN)
                .allowedHeaders(defineAllowedCORSHeaders())
                .allowedMethods(defineAllowedCORSHttpMethods()));

        //Store post bodies in rooting context for all api calls
        applicationRouter.route(HttpMethod.POST, config().getString(REST_API_CONTEXT_PATTERN_KEY, DEFAULT_REST_API_CONTEXT_PATTERN)).handler(BodyHandler.create());
        applicationRouter.route(HttpMethod.PUT, config().getString(REST_API_CONTEXT_PATTERN_KEY, DEFAULT_REST_API_CONTEXT_PATTERN)).handler(BodyHandler.create());

        // mount sub router for manage users web restful api
        ManageUserRestController manageUserRestController = new ManageUserRestController(vertx, config());
        applicationRouter.mountSubRouter(config().getString(USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE), manageUserRestController.getRestAPIRouter());

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
