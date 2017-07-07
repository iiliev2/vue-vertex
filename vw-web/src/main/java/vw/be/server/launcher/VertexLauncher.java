package vw.be.server.launcher;

import io.vertx.core.DeploymentOptions;
import vw.be.server.common.IOUtils;

import java.util.Objects;

import static vw.be.server.common.IConfigurationConstants.DEFAULT_START_MONITORING;
import static vw.be.server.common.IConfigurationConstants.START_MONITORING_KEY;

public class VertexLauncher {

    private static final String DEFAULT_CONFIGURATION = "my-app-config.json";
    private static final String CONF_ARG = "-conf";

    public static void main(String[] args) {
        DeploymentOptions deploymentOptions = getDeploymentOptions(args);

        final boolean toStartMonitoring = deploymentOptions.getConfig().getBoolean(START_MONITORING_KEY, DEFAULT_START_MONITORING);

        initDeploymentProcessor(toStartMonitoring).deploy(deploymentOptions);
    }

    private static IDeploymentProcessor initDeploymentProcessor(boolean toStartMonitoring) {
        IDeploymentProcessor deploymentProcessor;
        if (toStartMonitoring) {
            deploymentProcessor = new MonitoringDeploymentProcessor();
        } else {
            deploymentProcessor = new StandartDeploymentProcessor();
        }

        return deploymentProcessor;
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
