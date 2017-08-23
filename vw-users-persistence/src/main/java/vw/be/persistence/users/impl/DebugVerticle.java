package vw.be.persistence.users.impl;

import io.vertx.core.AbstractVerticle;

public class DebugVerticle extends AbstractVerticle {

    @Override public void start() throws Exception {
        vertx.deployVerticle(new UsersPersistenceInMemVerticle(), id -> {
            if (id.succeeded())
                vertx.undeploy(id.result());
        });
    }
}
