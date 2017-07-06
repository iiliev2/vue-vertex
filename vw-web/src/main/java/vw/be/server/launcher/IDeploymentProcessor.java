package vw.be.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vw.be.server.verticle.HttpVerticle;
import vw.be.server.verticle.ManageUserDatabaseVerticle;

import static vw.be.server.common.IConfigurationConstants.*;
import static vw.be.server.common.IResourceBundleConstants.VERTICLE_DEPLOYED_SUCCESSFULY_MSG;
import static vw.be.server.common.IResourceBundleConstants.VERTICLE_FAILED_TO_DEPLOY;

public interface IDeploymentProcessor {

    Logger LOGGER = LoggerFactory.getLogger(IDeploymentProcessor.class);

    void deploy(DeploymentOptions deploymentOptions);

    default void completeDeploymentHandler(Future<String> verticleDeploymentFuture) {
        verticleDeploymentFuture.setHandler(ar -> {
            if (ar.succeeded()) {
                LOGGER.info(String.format(VERTICLE_DEPLOYED_SUCCESSFULY_MSG, ar.result()));
            } else {
                LOGGER.error(String.format(VERTICLE_FAILED_TO_DEPLOY, ar.cause()));
            }
        });
    }

    default VertxOptions getVertxOptions() {
        return new VertxOptions();
    }

    default Future<String> deployVerticlesComposition(Vertx VERTX, DeploymentOptions deploymentOptions) {
        Future<String> dbVerticleDeployment = Future.future();
        VERTX.deployVerticle(ManageUserDatabaseVerticle.class.getName(),
                deploymentOptions.setInstances(deploymentOptions.getConfig().getInteger(DB_VERTICLE_COUNT_KEY, DEFAULT_DB_VERTICLE_COUNT)),
                dbVerticleDeployment.completer());

        return dbVerticleDeployment.compose(id -> {
            Future<String> httpVerticleDeployment = Future.future();
            VERTX.deployVerticle(
                    HttpVerticle.class.getName(),
                    deploymentOptions.setInstances(deploymentOptions.getConfig().getInteger(HTTP_VERTICLE_COUNT_KEY, DEFAULT_HTTP_VERTICLE_COUNT)),
                    httpVerticleDeployment.completer());

            return httpVerticleDeployment;
        });
    }

}
