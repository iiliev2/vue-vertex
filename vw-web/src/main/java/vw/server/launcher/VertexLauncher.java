package vw.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import vw.server.webapi.ManageUserRestService;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    public static void main(String[] args) {
        DeploymentOptions options = new DeploymentOptions();
        String verticleYoDeploy = ManageUserRestService.class.getName();
        VERTX.deployVerticle(verticleYoDeploy, options, res -> {
            if (res.succeeded()) {
                System.out.printf("Verticle with id : %s is successfuly deployed!%n", res.result());
            } else {
                System.err.printf("Failed to deploy verticle %s!", verticleYoDeploy);
            }
        });

    }

}
