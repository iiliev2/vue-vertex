package vw.be.restapi;

import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import vw.be.restapi.eventbus.HTTPRequestOverEB;

/**
 * Represents a parameterised REST api - having a name(a root resource endpoint) and a set of (sub)endpoints.
 */
public interface RestAPI {

    /**
     * Get the root path for the api.
     *
     * @return the name
     */
    String getName();

    /**
     * Parameterise an endpoint at the root for the given method.
     *
     * @param method  the method
     * @param handler the handler
     * @return the rest api
     */
    RestAPI addEndpoint(HttpMethod method, Handler<Message<HTTPRequestOverEB>> handler);

    /**
     * Parameterise an endpoint at the given path under the root and for the given method.
     *
     * @param method  the method
     * @param path    the path
     * @param handler the handler
     * @return the rest api
     */
    RestAPI addEndpoint(HttpMethod method, String path, Handler<Message<HTTPRequestOverEB>> handler);

    /**
     * Try to match a request event against one of the parameterised endpoints and handle it.
     *
     * @param event the event
     */
    void serve(Message<HTTPRequestOverEB> event);
}
