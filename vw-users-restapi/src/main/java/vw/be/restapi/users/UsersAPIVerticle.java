package vw.be.restapi.users;


import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.types.EventBusService;
import vw.be.persistence.users.service.IManageUserService;
import vw.be.restapi.RestAPI;
import vw.be.restapi.RestAPIImpl;
import vw.be.restapi.RestAPIVerticle;
import vw.be.restapi.eventbus.HTTPRequestOverEB;
import vw.be.restapi.eventbus.HTTPResponseOverEB;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.vertx.core.http.HttpMethod.*;
import static vw.be.restapi.IHttpApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.restapi.IHttpApiConstants.HEADER_CONTENT_TYPE;

/**
 * The Users REST api verticle.
 */
public class UsersAPIVerticle extends RestAPIVerticle {
    private static final String USER_ID = "id";
    private static final String USER_BY_ID_SUB_CONTEXT = "/:" + USER_ID;

    @Override protected RestAPI initAPI() {
        return new RestAPIImpl("/users")
                .addEndpoint(GET, event -> process(event, this::getAllUsers))
                .addEndpoint(POST, event -> process(event, this::createUser))
                .addEndpoint(DELETE, event -> process(event, this::deleteUsers))
                .addEndpoint(PUT, event -> process(event, this::replaceUsers))
                .addEndpoint(GET, USER_BY_ID_SUB_CONTEXT, event -> process(event, this::getUserById))
                .addEndpoint(PUT, USER_BY_ID_SUB_CONTEXT, event -> process(event, this::editUser))
                .addEndpoint(DELETE, USER_BY_ID_SUB_CONTEXT, event -> process(event, this::deleteUserById));
    }


    /**
     * Wraps the real handler, as an instance of the {@link IManageUserService} will be needed for each separate
     * request. This is like the template pattern, but for async calls. The template method is passed as an argument via
     * the action parameter.
     *
     * @param event  request
     * @param action the real procedure, which will use the service to perform the actual work; it must release the
     *               service once no longer needed
     */
    private void process(Message<HTTPRequestOverEB> event,
                         BiConsumer<IManageUserService, Message<HTTPRequestOverEB>> action) {
        EventBusService.getProxy(discovery, IManageUserService.class, async -> {
            if (async.succeeded())
                action.accept(async.result(), event);
            else
                reply(event,
                      SERVICE_UNAVAILABLE,
                      encode(async.cause().getMessage()));
        });
    }

    private void getAllUsers(IManageUserService service, Message<HTTPRequestOverEB> event) {
        service.get(replyAndRelease(service, event, OK));
    }

    private void getUserById(IManageUserService service, Message<HTTPRequestOverEB> event) {
        String id = parseID(event);
        if (id == null) return;
        service.getById(id, replyAndRelease(service, event, OK));
    }

    private void createUser(IManageUserService service, Message<HTTPRequestOverEB> event) {
        HTTPRequestOverEB request = event.body();
        JsonObject newUser = checkBadRequest(() -> new JsonObject(request.getBody()), event);
        if (newUser == null) return;
        service.create(newUser, replyAndRelease(service, event, CREATED));
    }

    private void deleteUserById(IManageUserService service,
                                Message<HTTPRequestOverEB> event) {
        String id = parseID(event);
        if (id == null) return;
        service.deleteById(id, replyAndRelease(service, event, NO_CONTENT));
    }

    private void editUser(IManageUserService service, Message<HTTPRequestOverEB> event) {
        JsonObject newUser = parseUser(event);
        if (newUser == null) return;
        service.update(newUser, replyAndRelease(service, event, OK));
    }

    private void replaceUsers(IManageUserService service,
                              Message<HTTPRequestOverEB> event) {
        HTTPRequestOverEB request = event.body();
        JsonArray newUsers = checkBadRequest(() -> new JsonArray(request.getBody()), event);
        if (newUsers == null) return;
        service.updateByFilter(newUsers, replyAndRelease(service, event, OK));
    }

    private void deleteUsers(IManageUserService service,
                             Message<HTTPRequestOverEB> event) {
        HTTPRequestOverEB request = event.body();
        String listOfIDs = request.getQuery().getString("list");
        JsonArray idsToDelete = null;
        if (listOfIDs != null)
            idsToDelete = checkBadRequest(() -> new JsonArray(listOfIDs), event);
        if (idsToDelete != null) {
            if (!idsToDelete.isEmpty())
                service.deleteByFilter(idsToDelete, replyAndRelease(service, event, NO_CONTENT));
            else reply(event, BAD_REQUEST, encode("The list of ids is empty"));
        } else if (listOfIDs == null) service.delete(replyAndRelease(service, event, NO_CONTENT));
    }

    /**
     * Returns a template handler, which wraps the boilerplate code needed for the asynchronous api. This handler will
     * either serve the desired response status and body from the async result, or reply with {@link
     * HttpResponseStatus#SERVICE_UNAVAILABLE}. In addition it releases the service after it has been used for a single
     * request event.
     *
     * @param service         to be closed
     * @param event           request to reply to
     * @param statusOnSuccess the response status if the async has been successful; this is a function so that the
     *                        status can be decided based on the result content
     * @return the handler
     */
    private <T> Handler<AsyncResult<T>> replyAndRelease(IManageUserService service,
                                                        Message<HTTPRequestOverEB> event,
                                                        Function<T, HttpResponseStatus> statusOnSuccess) {
        return async -> {
            if (async.succeeded()) {
                reply(event, statusOnSuccess.apply(async.result()), encode(async.result()));
            } else {
                reply(event,
                      SERVICE_UNAVAILABLE,
                      encode(async.cause().getMessage()));
            }
            ServiceDiscovery.releaseServiceObject(discovery, service);
        };
    }

    /**
     * A shortcut to {@link #replyAndRelease(IManageUserService, Message, Function)} to ignore the result content and
     * directly set the desired status.
     */
    private <T> Handler<AsyncResult<T>> replyAndRelease(IManageUserService service,
                                                        Message<HTTPRequestOverEB> event,
                                                        HttpResponseStatus statusOnSuccess) {
        return replyAndRelease(service, event, asyncIgnored -> statusOnSuccess);
    }

    private <T> String encode(T pojo) {
        return Json.encodePrettily(pojo);
    }

    private String encode(String error) {
        return new JsonObject().put("error", error).encodePrettily();
    }

    /**
     * Build and send the response.
     *
     * @param event  request to reply to
     * @param status code of the response
     * @param body   as json
     */
    private void reply(Message<HTTPRequestOverEB> event, HttpResponseStatus status, String body) {
        event.reply(new HTTPResponseOverEB(status,
                                           new JsonObject().put(HEADER_CONTENT_TYPE,
                                                                APPLICATION_JSON_CHARSET_UTF_8),
                                           body));
    }

    /**
     * Helper method to parse some data from the request.
     *
     * @param supplier the parsing action
     * @param event    the request event which to reply to in case of failure.
     * @return T the result of the parsing action, null if failed
     */
    private <T> T checkBadRequest(Supplier<T> supplier, Message<HTTPRequestOverEB> event) {
        try {
            return supplier.get();
        } catch (Exception e) {
            reply(event, BAD_REQUEST, encode(e.getMessage()));
            return null;
        }
    }

    private JsonObject parseUser(Message<HTTPRequestOverEB> event) {
        HTTPRequestOverEB request = event.body();
        JsonObject newUser = checkBadRequest(() -> new JsonObject(request.getBody()), event);
        if (newUser == null) return null;
        newUser.put(USER_ID, request.getQuery().getValue(USER_ID));
        return newUser;
    }

    private String parseID(Message<HTTPRequestOverEB> event) {
        HTTPRequestOverEB request = event.body();
        String id = checkBadRequest(() -> request.getQuery().getString(USER_ID), event);
        if (id == null) return null;
        return id;
    }
}
