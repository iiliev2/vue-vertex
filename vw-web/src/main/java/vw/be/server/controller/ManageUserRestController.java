package vw.be.server.controller;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import vw.be.common.dto.UserDTO;
import vw.be.server.common.HttpStatusCodeEnum;
import vw.be.server.factory.ManageUserServiceFactory;
import vw.be.server.sevice.IManageUserService;

import java.util.function.Consumer;

import static vw.be.server.common.IConfigurationConstants.*;
import static vw.be.server.common.IWebApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.be.server.common.IWebApiConstants.HEADER_CONTENT_TYPE;

/**
 * Users restful web api vertx asynchronous implementation.
 */
public class ManageUserRestController implements IManageUserRestController {

	private final Router restAPIRouter;

	private final IManageUserService manageUserService;

	public ManageUserRestController(Vertx vertx, JsonObject config) {
		this.restAPIRouter = Router.router(vertx);
		this.manageUserService = ManageUserServiceFactory.getService(vertx, config);

		configure(config);
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
		restAPIRouter.delete(USER_BY_ID_SUB_CONTEXT).handler(this::deleteUserById);
	}

	/**
	 * Close persistence container connections.
	 */
	public void destroy() {
		manageUserService.destroy();
	}

	@Override
	public void getAllUsers(RoutingContext routingContext) {
		manageUserService.getAllUsers().setHandler(resultHandler(routingContext.response(), res -> {
			if (res == null) {
				sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
			} else {
				sendResponseSuccess(HttpStatusCodeEnum.OK, routingContext.response(), Json.encodePrettily(res));
			}
		}));
	}

	@Override
	public void getUserById(RoutingContext routingContext) {
		String userID = routingContext.request().getParam(USER_ID);
		HttpServerResponse response = routingContext.response();
		if (userID == null || userID.isEmpty()) {
			sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
		} else {
			manageUserService.getUserById(userID).setHandler(resultHandler(routingContext.response(), res -> {
				if (!res.isPresent()) {
					sendResponse(HttpStatusCodeEnum.NOT_FOUND, response);
				} else {
					sendResponseSuccess(HttpStatusCodeEnum.OK, response, res.get());
				}
			}));
		}
	}

	@Override
	public void addUser(RoutingContext routingContext) {
		HttpServerResponse response = routingContext.response();
		String requestBody = routingContext.getBodyAsString();
		UserDTO userDTO = Json.decodeValue(requestBody, UserDTO.class);
		if (requestBody == null || userDTO.getId() != null) {
			sendResponse(HttpStatusCodeEnum.BAD_REQUEST, response);
		} else {

			manageUserService.createUser(userDTO).setHandler(resultHandler(routingContext.response(), res -> {
				if (res) {
					sendResponseSuccess(HttpStatusCodeEnum.CREATED, response, Json.encodePrettily(userDTO));
				} else {
					sendResponse(HttpStatusCodeEnum.SERVICE_TEMPORARY_UNAVAILABLE, routingContext.response());
				}
			}));
		}
	}

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
