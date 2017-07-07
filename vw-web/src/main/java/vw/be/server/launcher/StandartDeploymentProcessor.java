package vw.be.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

import static vw.be.server.common.IResourceBundleConstants.PROCESSOR_IS_STARTING;

public class StandartDeploymentProcessor implements IDeploymentProcessor {

    @Override
    public void deploy(DeploymentOptions deploymentOptions) {
        LOGGER.info(String.format(PROCESSOR_IS_STARTING, this.getClass().getSimpleName()));

        VertxOptions vertxOptions = getVertxOptions();

        final Vertx VERTX = Vertx.vertx(vertxOptions);

        completeDeploymentHandler(deployVerticlesComposition(VERTX, deploymentOptions));
    }
}
