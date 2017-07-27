package vw.be.server.controller;

import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.be.server.common.HttpStatusCodeEnum;
import vw.be.server.common.PersistenceResponseCodeEnum;
import vw.be.server.controller.parsers.IQueryParamParser;
import vw.be.server.exceptions.MalformedQueryException;
import vw.be.server.service.IManageUserService;

import java.util.Collection;
import java.util.Map.Entry;

import static vw.be.server.common.HttpStatusCodeEnum.*;
import static vw.be.server.common.IHttpApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.server.common.IHttpApiConstants.HEADER_CONTENT_TYPE;
import static vw.be.server.common.IWebConfigurationConstants.ROUTE_ROOT;
import static vw.be.server.common.PersistenceActionEnum.*;
import static vw.be.server.controller.parsers.IQueryParamParser.COMMA_SEPARATED_LIST_PARSER;
import static vw.be.server.controller.parsers.IQueryParamParser.COMMA_SEPARATED_RANGE_PARSER;
import static vw.be.server.service.IManageUserService.*;

/**
 * Users restful web api vertx asynchronous implementation.
 */
public class ManageUserRestController implements IManageUserRestController {

    private final Router restAPIRouter;

    private Vertx vertx;

    public ManageUserRestController(Vertx vertx) {
        this.vertx = vertx;
        this.restAPIRouter = Router.router(vertx);

        configure();
    }

    /**
     * Restful api user method handlers
     */
    private void configure() {
        restAPIRouter.route("");
        restAPIRouter.get(ROUTE_ROOT).handler(this::getAllUsers);
        restAPIRouter.post(ROUTE_ROOT).handler(this::addUser);
        restAPIRouter.delete(ROUTE_ROOT).handler(this::delete);
        restAPIRouter.put(ROUTE_ROOT).handler(this::replaceAllUsers);
        restAPIRouter.put(USER_BY_ID_SUB_CONTEXT).handler(this::editUser);
        restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
        restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
    }

    @Override
    public void getAllUsers(RoutingContext routingContext) {
        final String query = routingContext.request().params().get(SEARCH_BY_ALL_NAMES_PARTIAL_PARAMETER);
        DeliveryOptions options = new DeliveryOptions();
        JsonObject request = new JsonObject();

        if (query != null && !query.isEmpty()) {
            options.addHeader(PERSISTENCE_ACTION, String.valueOf(GET_BY_FILTER));
            request.put(SEARCH_BY_ALL_NAMES_PARTIAL_PARAMETER, query);
        } else {
            options.addHeader(PERSISTENCE_ACTION, String.valueOf(GET_ALL));
        }

        vertx.eventBus().send(MANAGE_USER_DB_QUEUE, request, options, reply -> {
            if (reply.succeeded()) {
                sendResponseSuccess(OK, routingContext.response(), Json.encodePrettily(reply.result().body()));
            } else {
                sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
            }
        });
    }

    @Override
    public void getUserById(RoutingContext routingContext) {
        String userID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        if (userID == null || userID.isEmpty()) {
            sendResponse(BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_BY_ID));
            JsonObject request = new JsonObject().put(ID, userID);
            vertx.eventBus().send(MANAGE_USER_DB_QUEUE, request, options, reply -> {
                if (reply.succeeded()) {
                    replySucceeded(response, reply.result(), OK);
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
                }
            });
        }
    }

    @Override
    public void addUser(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        JsonObject requestBody = routingContext.getBodyAsJson();
        if (requestBody == null) {
            sendResponse(BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE));
            vertx.eventBus().send(IManageUserService.MANAGE_USER_DB_QUEUE, requestBody, options, reply -> {
                if (reply.succeeded()) {
                    sendResponseSuccess(HttpStatusCodeEnum.CREATED, response, reply.result().body().toString());
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
                }
            });
        }
    }

    @Override
    public void replaceAllUsers(RoutingContext routingContext) {
    }

    @Override
    public void editUser(RoutingContext routingContext) {
        String uriUserID = routingContext.request().getParam(USER_ID);
        HttpServerResponse response = routingContext.response();
        JsonObject requestBody = routingContext.getBodyAsJson();
        // TODO user id can be in requset body. ADD logic!!!
        if (requestBody == null || !matchingUserID(requestBody.getString(ID), uriUserID)) {
            sendResponse(BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(MERGE));
            vertx.eventBus().send(MANAGE_USER_DB_QUEUE, requestBody.put(ID, uriUserID), options, reply -> {
                if (reply.succeeded()) {
                    replySucceeded(response, reply.result(), NO_CONTENT);
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
                }
            });
        }
    }

    @Override
    public void deleteUserById(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        String userID = routingContext.request().getParam(USER_ID);
        if (userID == null) {
            sendResponse(BAD_REQUEST, response);
        } else {
            DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(DELETE_BY_ID));
            JsonObject request = new JsonObject().put(ID, userID);
            vertx.eventBus().send(MANAGE_USER_DB_QUEUE, request, options, reply -> {
                if (reply.succeeded()) {
                    replySucceeded(response, reply.result(), NO_CONTENT);
                } else {
                    sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
                }
            });
        }
    }

    @Override
    public void delete(RoutingContext routingContext) {
        HttpServerResponse response = routingContext.response();
        HttpServerRequest request = routingContext.request();
        MultiMap query = request.params();
        String paramName;
        Collection<String> ids = null;
        IQueryParamParser<String, Collection<String>> parser;
        try {
            for (Entry<String, String> param : query.entries()) {
                paramName = param.getKey().trim().toLowerCase();
                switch (paramName) {
                    case "list":
                        parser = COMMA_SEPARATED_LIST_PARSER;
                        break;
                    case "range":
                        parser = COMMA_SEPARATED_RANGE_PARSER;
                        break;
                    default:
                        throw new MalformedQueryException("Unrecognized query parameter: " + paramName);
                }
                if (ids == null)
                    ids = parser.apply(param.getValue());
                else
                    ids.addAll(parser.apply(param.getValue()));
            }
        } catch (MalformedQueryException e) {
            sendResponse(BAD_REQUEST, response.setStatusMessage(e.getMessage()));
        }
    }

    private void replySucceeded(HttpServerResponse response, Message<Object> message,
                                HttpStatusCodeEnum onSuccessHttpResponceCode) {
        if (message == null) {
            sendResponse(SERVICE_TEMPORARY_UNAVAILABLE, response);
            return;
        }

        if (message.headers() != null && String.valueOf(PersistenceResponseCodeEnum.NOT_FOUND)
                                               .equals(message.headers().get(PERSISTENCE_RESPONSE_CODE))) {
            sendResponse(HttpStatusCodeEnum.NOT_FOUND, response);
        } else {
            final Object messageBody = message.body();
            if (messageBody == null) {
                sendResponse(onSuccessHttpResponceCode, response);
            } else {
                sendResponseSuccess(onSuccessHttpResponceCode, response, Json.encodePrettily(messageBody));

            }
        }
    }

    /**
     * Send response with HTTP code given as argument.
     */
    private void sendResponse(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
        response.setStatusCode(statusCode.getStatusCode()).end();
    }

    /**
     * Send response with HTTP code and content given as arguments.
     */
    private void sendResponseSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response,
                                     String responseContent) {
        response.setStatusCode(statusCode.getStatusCode())
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8).end(responseContent);
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
