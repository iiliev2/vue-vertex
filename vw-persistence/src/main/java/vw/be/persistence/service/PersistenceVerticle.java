package vw.be.persistence.service;

import io.vertx.serviceproxy.ProxyHelper;
import vw.be.common.MicroServiceVerticle;

public abstract class PersistenceVerticle<T extends IManageUserService> extends MicroServiceVerticle {

    public static final String MANAGE_USER_DB_QUEUE = "manage.user.db.queue";

    @Override public void start() {
        super.start();
        T service = createService();
        ProxyHelper.registerService(IManageUserService.class, vertx, service, MANAGE_USER_DB_QUEUE);
        publishEventBusService(this.getClass().getName(), MANAGE_USER_DB_QUEUE, service.getClass(), ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println(this.getClass().getName() + " published : " + ar.succeeded());
            }
        });
    }

    public abstract T createService();
}
