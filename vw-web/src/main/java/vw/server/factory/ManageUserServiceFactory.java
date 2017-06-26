package vw.server.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vw.server.sevice.IManageUserService;
import vw.server.sevice.MockManageUserService;
import vw.server.sevice.MongoManageUserService;

public class ManageUserServiceFactory {

    private static final String DB_TYPE_KEY = "db.type";

    public static IManageUserService getService(Vertx vertx, JsonObject config){
        switch (config.getString(DB_TYPE_KEY, "")) {
            case "mongo":
                return new MongoManageUserService(vertx, config);
            default:
                return new MockManageUserService();
        }
    }

}
