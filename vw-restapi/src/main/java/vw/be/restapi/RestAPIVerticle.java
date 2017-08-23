package vw.be.restapi;

import io.vertx.core.eventbus.EventBus;
import vw.be.common.MicroServiceVerticle;
import vw.be.restapi.eventbus.HTTPRequestCodec;
import vw.be.restapi.eventbus.HTTPResponseCodec;
import vw.be.restapi.eventbus.HTTPResponseOverEB;
import vw.be.restapi.eventbus.Util;

/**
 * This verticle will register a message consumer at the root path of a rest api endpoint. It will use {@link
 * HTTPResponseOverEB} and {@link HTTPResponseOverEB} for communication with the http server over the event bus. The
 * idea is to be able to dynamically define and deploy different rest apis as separate microservices.
 */
public abstract class RestAPIVerticle extends MicroServiceVerticle {
    private RestAPI api;

    @Override
    public void start() {
        super.start();
        EventBus eventBus = vertx.eventBus();
        Util.initEventBusCodecs(eventBus, HTTPResponseOverEB.class, new HTTPResponseCodec(), new HTTPRequestCodec());
        getAPI();
        eventBus.consumer(api.getName(), api::serve);
    }

    /**
     * Initialise the api configuration(e.g. its root name and endpoints). This should not be called by sub classes. Use
     * {@link #getAPI()} to retrieve the api configuration.
     *
     * @return the rest api
     */
    protected abstract RestAPI initAPI();

    /**
     * Loads the api if not initialised and retrieves it.
     *
     * @return the api
     */
    public final RestAPI getAPI() {
        if (api == null) api = initAPI();
        return api;
    }
}
