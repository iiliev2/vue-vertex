package vw.be.restapi;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import vw.be.restapi.eventbus.HTTPRequestOverEB;
import vw.be.restapi.eventbus.HTTPResponseOverEB;

import java.util.HashSet;
import java.util.Set;

/**
 * This implementation mimics the router api behaviour, but simplified. An endpoint is roughly equivalent to a {@link
 * io.vertx.ext.web.Route}. It will match and serve up to a single endpoint per request event.
 *
 * @see Endpoint
 */
public class RestAPIImpl implements RestAPI {
    private final String name;
    private final Set<Endpoint> endpoints;

    /**
     * Instantiates a new Rest api that will be served at the given name.
     *
     * @param name api root
     */
    public RestAPIImpl(String name) {
        this.name = name;
        endpoints = new HashSet<>();
    }

    @Override public String getName() {
        return name;
    }

    @Override public RestAPI addEndpoint(HttpMethod method, Handler<Message<HTTPRequestOverEB>> handler) {
        addEndpoint(method, "/", handler);
        return this;
    }

    @Override
    public RestAPI addEndpoint(HttpMethod method, String path, Handler<Message<HTTPRequestOverEB>> handler) {
        endpoints.add(new Endpoint(method, path, handler));
        return this;
    }

    @Override
    public void serve(Message<HTTPRequestOverEB> event) {
        HttpMethod method = event.body().getMethod();
        String uri = event.body().getUri();
        for (Endpoint endpoint : endpoints) {
            if (endpoint.matches(event, name)) {
                endpoint.handle(event);
                return;
            }
        }
        event.reply(new HTTPResponseOverEB(HttpResponseStatus.BAD_REQUEST,
                                           new JsonObject(),
                                           String.format("Unsupported api endpoint/method [%s: %s]",
                                                         method.name(),
                                                         uri)));
    }
}
