package vw.be.server.controller;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerRequest;
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

import static vw.be.server.common.HttpStatusCodeEnum.*;
import static vw.be.server.common.IConfigurationConstants.ROUTE_ROOT;
import static vw.be.server.common.IHttpApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.server.common.IHttpApiConstants.HEADER_CONTENT_TYPE;
import static vw.be.server.common.PersistenceActionEnum.*;
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
		restAPIRouter.get(ROUTE_ROOT).handler(this::getAllUsers);
		restAPIRouter.post(ROUTE_ROOT).handler(this::addUser);
		restAPIRouter.put(ROUTE_ROOT).handler(this::replaceAllUsers);
		restAPIRouter.put(USER_BY_ID_SUB_CONTEXT).handler(this::editUser);
		restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
		restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
	}

	/**
	 * Restful api user method handlers
	 */
	private void configure(JsonObject config) {
		restAPIRouter.get(ROUTE_ROOT).handler(this::getAllUsers);
		restAPIRouter.post(ROUTE_ROOT).handler(this::addUser);
		restAPIRouter.put(ROUTE_ROOT).handler(this::replaceAllUsers);
		restAPIRouter.put(USER_BY_ID_SUB_CONTEXT).handler(this::editUser);
		restAPIRouter.get(USER_BY_ID_SUB_CONTEXT).handler(this::getUserById);
		restAPIRouter.delete(ROUTE_ROOT).handler(this::delete);
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
	}manageUserService.createUser(userDTO).setHandler(

	resultHandler(routingContext.response(), res -> {
				if (res) {
					sendResponseSuccess(HttpStatusCodeEnum.CREATED, response, Json.encodePrettily(userDTO));
				} else {
					sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
				}
			}));
		}

	}

	@Override
	public void replaceAllUsers(RoutingContext routingContext) {
	}

	@Override
	public void editUser(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		String id = routingContext.request().getParam(USER_ID);
		String requestBody = routingContext.getBodyAsString();
		final UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
		if (requestBody == null || id.isEmpty() || notMatchingUserID(userDTO, id)) {
			sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
		} else {
			userDTO.setUserId(id);
			manageUserService.updateUser(userDTO).setHandler(resultHandler(routingContext.response(), res -> {
				if (res) {
					sendResponseSuccess(HttpStatusCodeEnum.OK, response, Json.encodePrettily(userDTO));
				} else {
					sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
				}
			}));
		}
	}

	@Override
	public void delete(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		HttpServerRequest request = routingContext.request();
		Set<String> ids = new HashSet<>();
		String parameterName;
		try {
			for (Entry<String, String> param : request.params().entries()) {
				parameterName = param.getKey().trim().toLowerCase();
				switch (parameterName) {
				case "list":
				case "range":
				default:
					throw new MalformedQueryException("Unknown query parameter:" + parameterName);
				}
			}
		} catch (MalformedQueryException e) {
			// TODO: handle exception
		} catch (Throwable e) {
		}

	}

	@Override
	public void deleteUserById(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		String userID = routingContext.request().getParam(USER_ID);
		if (userID == null) {
			sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
		} else {
			manageUserService.deleteUserById(userID).setHandler(resultHandler(routingContext.response(), res -> {
				if (res) {
					sendResponse(HttpStatusCodeEnum.NO_CONTENT, response);
				} else {
					sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
				}
			}));
		}
	}

	/**
	 * Wrap the result handler with failure handler (503 Service Unavailable)
	 */
	private <T> Handler<AsyncResult<T>> resultHandler(HttpServerResponse response, Consumer<T> consumer) {
		return res -> {
			if (res.succeeded()) {
				consumer.accept(res.result());
			} else {
				sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, response);
			}
		};
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

	private boolean notMatchingUserID(UserDTO userDTO, String URI_ID) {
		String id = userDTO.getId();
		if (id != null)
			return id.equals(URI_ID);
		return false;
	}

}
