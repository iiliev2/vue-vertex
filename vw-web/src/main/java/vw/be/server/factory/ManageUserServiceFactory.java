package vw.be.server.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vw.be.server.service.IManageUserService;
import vw.be.server.service.MockManageUserService;
import vw.be.server.service.MongoManageUserService;

import static vw.be.server.common.IWebConfigurationConstants.DB_TYPE_KEY;
import static vw.be.server.common.IWebConfigurationConstants.MONGO_DB_PROVIDER;

public class ManageUserServiceFactory {

    public static IManageUserService getService(Vertx vertx, JsonObject config) {
        switch (config.getString(DB_TYPE_KEY, "").toUpperCase()) {
            case MONGO_DB_PROVIDER:
                return new MongoManageUserService(vertx, config);
            default:
                return new MockManageUserService();
        }
    }

}
