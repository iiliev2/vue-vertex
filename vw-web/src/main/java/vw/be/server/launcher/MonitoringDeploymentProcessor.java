package vw.be.server.launcher;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.ext.dropwizard.DropwizardMetricsOptions;
import io.vertx.ext.dropwizard.Match;
import vw.be.server.verticle.MonitoringVerticle;

import static vw.be.server.common.IResourceBundleConstants.PROCESSOR_IS_STARTING;
import static vw.be.server.common.IWebConfigurationConstants.DEFAULT_MONITORING_VERTICLE_COUNT;
import static vw.be.server.common.IWebConfigurationConstants.MONITORING_VERTICLE_COUNT_KEY;
import static vw.be.server.service.IManageUserService.MANAGE_USER_DB_QUEUE;

public class MonitoringDeploymentProcessor implements IDeploymentProcessor {

    @Override
    public void deploy(DeploymentOptions deploymentOptions) {
        LOGGER.info(String.format(PROCESSOR_IS_STARTING, this.getClass().getSimpleName()));

        VertxOptions vertxOptions = getMonitoringVertxOptions();

        final Vertx VERTX = Vertx.vertx(vertxOptions);

        completeDeploymentHandler(deployMonitoringVerticlesComposition(VERTX, deploymentOptions));
    }

    private VertxOptions getMonitoringVertxOptions() {
        return getVertxOptions().setMetricsOptions(
                new DropwizardMetricsOptions().
                                                      setEnabled(true).
                                                      addMonitoredEventBusHandler(
                                                              new Match().setValue(MANAGE_USER_DB_QUEUE)));
    }

    private Future<String> deployMonitoringVerticlesComposition(Vertx VERTX, DeploymentOptions deploymentOptions) {
        return deployVerticlesComposition(VERTX, deploymentOptions).compose(monitoring -> {
            Future<String> monitoringVerticleDeployment = Future.future();
            VERTX.deployVerticle(
                    MonitoringVerticle.class.getName(),
                    deploymentOptions.setInstances(deploymentOptions.getConfig()
                                                                    .getInteger(MONITORING_VERTICLE_COUNT_KEY,
                                                                                DEFAULT_MONITORING_VERTICLE_COUNT)),
                    monitoringVerticleDeployment.completer());

            return monitoringVerticleDeployment;
        });
    }


}
