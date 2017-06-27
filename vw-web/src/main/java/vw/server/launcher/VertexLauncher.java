package vw.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vw.server.common.IOUtils;
import vw.server.webapi.ManageUserVerticle;

import java.util.Objects;

import static vw.server.common.IResourceBundleConstants.VERTICLE_DEPLOYED_SUCCESSFULY_MSG;
import static vw.server.common.IResourceBundleConstants.VERTICLE_FAILED_TO_DEPLOY;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    private static final String DEFAULT_CONFIGURATION = "my-app-config.json";
    private static final String CONF_ARG = "-conf";

    private static final Logger LOGGER = LoggerFactory.getLogger(ManageUserVerticle.class);

    public static void main(String[] args) {
        String verticleYoDeploy = ManageUserVerticle.class.getName();
        VERTX.deployVerticle(verticleYoDeploy, getDeploymentOptions(args), res -> {
            if (res.succeeded()) {
                LOGGER.info(String.format(VERTICLE_DEPLOYED_SUCCESSFULY_MSG, res.result()));
            } else {
                LOGGER.error(String.format(VERTICLE_FAILED_TO_DEPLOY, verticleYoDeploy));
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

        return IOUtils.loadConfiguration(appConfiguration, VertexLauncher.class.getClassLoader());
    }

}
