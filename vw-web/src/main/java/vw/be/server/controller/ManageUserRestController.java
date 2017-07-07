package vw.be.server.controller;

import static vw.be.server.common.HttpStatusCodeEnum.BAD_REQUEST;
import static vw.be.server.common.HttpStatusCodeEnum.NO_CONTENT;
import static vw.be.server.common.HttpStatusCodeEnum.OK;
import static vw.be.server.common.HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE;
import static vw.be.server.common.IConfigurationConstants.ROUTE_ROOT;
import static vw.be.server.common.IHttpApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.server.common.IHttpApiConstants.HEADER_CONTENT_TYPE;
import static vw.be.server.common.PersistenceActionEnum.CREATE;
import static vw.be.server.common.PersistenceActionEnum.DELETE_BY_ID;
import static vw.be.server.common.PersistenceActionEnum.GET_ALL;
import static vw.be.server.common.PersistenceActionEnum.GET_BY_ID;
import static vw.be.server.common.PersistenceActionEnum.MERGE;
import static vw.be.server.service.IManageUserService.ID;
import static vw.be.server.service.IManageUserService.MANAGE_USER_DB_QUEUE;
import static vw.be.server.service.IManageUserService.PERSISTENCE_ACTION;
import static vw.be.server.service.IManageUserService.PERSISTENCE_RESPONSE_CODE;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.be.server.common.HttpStatusCodeEnum;
import vw.be.server.common.PersistenceResponseCodeEnum;
import vw.be.server.service.IManageUserService;

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
		restAPIRouter.get(ROUTE_ROOT).handler(this::getAllUsers);
		restAPIRouter.post(ROUTE_ROOT).handler(this::addUser);
		restAPIRouter.put(ROUTE_ROOT).handler(this::replaceAllUsers);
		restAPIRouter.put(USER_BY_ID_SUB_CONTEXT).handler(this::editUser);
		restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
		restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
	}

	@Override
	public void getAllUsers(RoutingContext routingContext) {
		DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_ALL));

		vertx.eventBus().send(MANAGE_USER_DB_QUEUE, new JsonObject(), options, reply -> {
			if (reply.succeeded()) {
				sendResponseSuccess(OK,
						routingContext.response(),
						Json.encodePrettily(reply.result().body()));
			} else {
				sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
						routingContext.response());
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
					sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
							routingContext.response());
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
					sendResponseSuccess(HttpStatusCodeEnum.CREATED,
							response, reply.result().body().toString());
				} else {
					sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
							routingContext.response());
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
					sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
							routingContext.response());
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
					sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
							routingContext.response());
				}
			});
		}
	}

	private void replySucceeded(HttpServerResponse response, Message<Object> message,
			HttpStatusCodeEnum onSuccessHttpResponceCode) {
		if (message == null) {
			sendResponse(SERVICE_TEMPORARY_UNAVAILABLE,
					response);
			return;
		}

		if (message.headers() != null
				&& String.valueOf(PersistenceResponseCodeEnum.NOT_FOUND)
						.equals(message.headers().get(PERSISTENCE_RESPONSE_CODE))) {
			sendResponse(HttpStatusCodeEnum.NOT_FOUND,
					response);
		} else {
			final Object messageBody = message.body();
			if (messageBody == null) {
				sendResponse(onSuccessHttpResponceCode,
						response);
			} else {
				sendResponseSuccess(onSuccessHttpResponceCode,
						response,
						Json.encodePrettily(messageBody));

			}
		}
	}

	/**
	 * Send response with HTTP code given as argument.
	 */
	private void sendResponse(HttpStatusCodeEnum statusCode, HttpServerResponse response) {
		response
				.setStatusCode(statusCode.getStatusCode())
				.end();
	}

	/**
	 * Send response with HTTP code and content given as arguments.
	 */
	private void sendResponseSuccess(HttpStatusCodeEnum statusCode, HttpServerResponse response,
			String responseContent) {
		response
				.setStatusCode(statusCode.getStatusCode())
				.putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
				.end(responseContent);
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

	@Override
	public void delete(RoutingContext routingContext) {
		// TODO Auto-generated method stub

	}

}
