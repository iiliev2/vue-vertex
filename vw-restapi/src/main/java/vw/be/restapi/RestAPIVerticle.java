package vw.be.restapi;

import io.vertx.core.json.JsonObject;
import vw.be.common.MicroServiceVerticle;

public abstract class RestAPIVerticle extends MicroServiceVerticle {

    @Override
    public void start() {
        super.start();

        vertx.eventBus().consumer(getAddress(), event -> new JsonObject());
        publishMessageSource(getAddress(), getAddress(), JsonObject.class, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println("REST service published : " + ar.succeeded());
            }
        });
    }

    void sendMessage(JsonObject responce) {
        vertx.eventBus().send(getAddress(), responce);

    }

    abstract String getAddress();
}
