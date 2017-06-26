package vw.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vw.server.common.IOUtils;
import vw.server.webapi.ManageUserVerticle;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    private static final String DEFAULT_CONFIGURATION = "my-app-config.json";
    private static final String VERTICLE_DEPLOYED_SUCCESSFULY_MSG = "Verticle with id : %s is successfuly deployed!%n";
    private static final String VERTICLE_FAILED_TO_DEPLOY = "Failed to deploy verticle %s!";
    private static final String CONF_ARG = "-conf";

    public static void main(String[] args) {
        String verticleYoDeploy = ManageUserVerticle.class.getName();
        VERTX.deployVerticle(verticleYoDeploy, getDeploymentOptions(args), res -> {
            if (res.succeeded()) {
                System.out.printf(VERTICLE_DEPLOYED_SUCCESSFULY_MSG, res.result());
            } else {
                System.err.printf(VERTICLE_FAILED_TO_DEPLOY, verticleYoDeploy);
            }
        });

    }

    private static DeploymentOptions getDeploymentOptions(String[] args) {
        String appConfiguration;
        if(args != null && args.length == 2 && Objects.equals(args[0], CONF_ARG)) {
            appConfiguration = args[1];
        } else {
            appConfiguration = DEFAULT_CONFIGURATION;
        }

        return IOUtils.loadFileInJsonFormat(appConfiguration, VertexLauncher.class);
    }

}
