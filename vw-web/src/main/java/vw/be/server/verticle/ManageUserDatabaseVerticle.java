package vw.be.server.verticle;

import io.vertx.core.AbstractVerticle;
import vw.be.server.factory.ManageUserServiceFactory;
import vw.be.server.sevice.IManageUserService;

public class ManageUserDatabaseVerticle extends AbstractVerticle{

    private IManageUserService manageUserService;

    @Override
    public void start() throws Exception {
        this.manageUserService = ManageUserServiceFactory.getService(vertx, config());
        vertx.eventBus().consumer(IManageUserService.DB_QUEUE, manageUserService::onMessage);
    }

    @Override
    public void stop() throws Exception {
        manageUserService.destroy();
    }
}
