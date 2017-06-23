package vw.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import vw.server.webapi.ManageUserVerticle;

import java.io.InputStream;
import java.util.Objects;
import java.util.Scanner;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    private static final String DEFAULT_CONFIGURATION = "my-app-config.json";

    public static void main(String[] args) {
        String verticleYoDeploy = ManageUserVerticle.class.getName();
        VERTX.deployVerticle(verticleYoDeploy, getDeploymentOptions(args), res -> {
            if (res.succeeded()) {
                System.out.printf("Verticle with id : %s is successfuly deployed!%n", res.result());
            } else {
                System.err.printf("Failed to deploy verticle %s!", verticleYoDeploy);
            }
        });

    }

    private static DeploymentOptions getDeploymentOptions(String[] args) {
        DeploymentOptions options = new DeploymentOptions();
        String appConfiguration;
        if(args != null && args.length == 2 && Objects.equals(args[0], "-conf")) {
            appConfiguration = args[1];
        } else {
            appConfiguration = DEFAULT_CONFIGURATION;
        }

        InputStream config = VertexLauncher.class.getClassLoader().getResourceAsStream(appConfiguration);
        if(config != null) {
            String text;
            try (Scanner scanner = new Scanner(config)) {
                text = scanner.useDelimiter("\\A").next();
            }
            options.setConfig(new JsonObject(text));
        }

        return options;
    }

}
