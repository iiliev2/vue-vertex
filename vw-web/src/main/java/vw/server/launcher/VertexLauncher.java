package vw.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions();
        String verticleYoDeploy = "vw.server.webapi.ManageUserRestService";
        VERTX.deployVerticle(verticleYoDeploy, options, res -> {
            if (res.succeeded()) {
                System.out.printf("Verticle with id : %s is successfuly deployed!%n", res.result());
            } else {
                System.err.printf("Failed to deploy verticle %s!", verticleYoDeploy);
            }
        });

    }

}
