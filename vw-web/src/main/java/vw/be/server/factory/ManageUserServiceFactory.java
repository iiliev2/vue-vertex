package vw.be.server.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vw.be.server.sevice.IManageUserService;
import vw.be.server.sevice.MockManageUserService;
import vw.be.server.sevice.MongoManageUserService;

import static vw.be.server.common.IConfigurationConstants.DB_TYPE_KEY;
import static vw.be.server.common.IConfigurationConstants.MONGO_DB_PROVIDER;

public class ManageUserServiceFactory {

    public static IManageUserService getService(Vertx vertx, JsonObject config){
        switch (config.getString(DB_TYPE_KEY, "")) {
            case MONGO_DB_PROVIDER:
                return new MongoManageUserService(vertx, config);
            default:
                return new MockManageUserService();
        }
    }

}
