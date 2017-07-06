package vw.be.server.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vw.be.server.factory.ManageUserServiceFactory;
import vw.be.server.service.IManageUserService;

import static vw.be.server.common.IConfigurationConstants.DB_TYPE_KEY;
import static vw.be.server.common.IConfigurationConstants.MOCK_DB_PROVIDER;
import static vw.be.server.common.IResourceBundleConstants.DB_VERTICLE_STARTED_OK_MESSAGE;

public class ManageUserDatabaseVerticle extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageUserDatabaseVerticle.class);

    private IManageUserService manageUserService;

    @Override
    public void start() throws Exception {
        this.manageUserService = ManageUserServiceFactory.getService(vertx, config());
        vertx.eventBus().consumer(IManageUserService.MANAGE_USER_DB_QUEUE, manageUserService::onMessage);
        LOGGER.info(String.format(DB_VERTICLE_STARTED_OK_MESSAGE, config().getString(DB_TYPE_KEY, MOCK_DB_PROVIDER)));
    }

    @Override
    public void stop() throws Exception {
        manageUserService.destroy();
    }
}
