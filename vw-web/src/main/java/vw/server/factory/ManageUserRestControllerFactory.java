package vw.server.factory;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import vw.server.controller.IManageUserRestController;
import vw.server.controller.ManageMockUserRestController;
import vw.server.controller.ManageMongoUserRestController;

public class ManageUserRestControllerFactory {

    public static IManageUserRestController getController(String provider, Vertx vertx, JsonObject config){
        if("mongo".equals(provider)){
            return new ManageMongoUserRestController(vertx, config);
        } else {
            return new ManageMockUserRestController(vertx);
        }
    }

}
