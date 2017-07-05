package vw.be.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vw.be.server.common.IOUtils;
import vw.be.server.verticle.HttpVerticle;
import vw.be.server.verticle.ManageUserDatabaseVerticle;

import java.util.Objects;

import static vw.be.server.common.IConfigurationConstants.*;
import static vw.be.server.common.IResourceBundleConstants.VERTICLE_DEPLOYED_SUCCESSFULY_MSG;
import static vw.be.server.common.IResourceBundleConstants.VERTICLE_FAILED_TO_DEPLOY;

public class VertexLauncher {

    private static final Vertx VERTX = Vertx.vertx();

    private static final String DEFAULT_CONFIGURATION = "my-app-config.json";
    private static final String CONF_ARG = "-conf";

    private static final Logger LOGGER = LoggerFactory.getLogger(VertexLauncher.class);

    public static void main(String[] args) {
        Future<String> dbVerticleDeployment = Future.future();
        DeploymentOptions deploymentOptions = getDeploymentOptions(args);

        VERTX.deployVerticle(ManageUserDatabaseVerticle.class.getName(), deploymentOptions.setInstances(deploymentOptions.getConfig().getInteger(DB_VERTICLE_COUNT_KEY, DEFAULT_DB_VERTICLE_COUNT)), dbVerticleDeployment.completer());

        dbVerticleDeployment.compose(id -> {
            Future<String> httpVerticleDeployment = Future.future();
            VERTX.deployVerticle(
                    HttpVerticle.class.getName(),
                    deploymentOptions.setInstances(deploymentOptions.getConfig().getInteger(HTTP_VERTICLE_COUNT_KEY, DEFAULT_HTTP_VERTICLE_COUNT)),
                    httpVerticleDeployment.completer());

            return httpVerticleDeployment;
        }).setHandler(ar -> {
            if (ar.succeeded()) {
                LOGGER.info(String.format(VERTICLE_DEPLOYED_SUCCESSFULY_MSG, ar.result()));
            } else {
                LOGGER.error(String.format(VERTICLE_FAILED_TO_DEPLOY, ar.cause()));
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

        return new DeploymentOptions()
                .setConfig(
                        IOUtils.loadConfiguration(
                                appConfiguration,
                                VertexLauncher.class.getClassLoader()
                        )
                );
    }

}
