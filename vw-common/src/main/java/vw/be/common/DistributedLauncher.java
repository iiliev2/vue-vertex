package vw.be.common;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import static vw.be.common.utils.IOUtils.loadConfiguration;

public class DistributedLauncher extends io.vertx.core.Launcher {
    private static final String CLUSTER_HOST_PROPERTY = "cluster.host";
    private static final String CONFIG_FILE = "config.json";
    private static final String DEFAULT_CONFIG_FILE_PATH = "src/conf/" + CONFIG_FILE;
    private static JsonObject jsonConfig;
    private static DistributedLauncher INSTANCE;
    private static String clusterHost;

    public static DistributedLauncher get() {
        if (INSTANCE == null) INSTANCE = new DistributedLauncher();
        return INSTANCE;
    }

    static {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.SLF4JLogDelegateFactory");
        System.setProperty("hazelcast.logging.type", "slf4j");
    }

    private static final Logger LOG = LoggerFactory.getLogger(DistributedLauncher.class);
    private LinkedHashSet<Runnable> listeners = new LinkedHashSet<>();

    public static void main(String[] args) {
        jsonConfig = loadConfig();
        if (jsonConfig != null) clusterHost = jsonConfig.getString(CLUSTER_HOST_PROPERTY);
        clusterHost = clusterHost == null ? "127.0.0.1" : clusterHost;

        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        argsList.add("-cluster");
        argsList.add("-cluster-host=" + clusterHost);
        args = argsList.toArray(args);
        LOG.debug("Main args: " + Arrays.deepToString(args));
        get().dispatch(args);
    }

    public boolean addShutdownCallback(Runnable callback) {
        return listeners.add(callback);
    }

    public boolean cancelShutdownCallback(Runnable callback) {
        return listeners.remove(callback);
    }

    private DistributedLauncher() {
    }

    public static JsonObject loadConfig() {
        JsonObject jsonConfig;
        File conf = new File(DEFAULT_CONFIG_FILE_PATH);
        //from project
        jsonConfig = loadConfiguration(conf);
        //from classpath
        jsonConfig.mergeIn(loadConfiguration(CONFIG_FILE, DistributedLauncher.class.getClassLoader()));
        //from execution dir
        jsonConfig.mergeIn(loadConfiguration(Paths.get(CONFIG_FILE).toAbsolutePath().toFile()));
        return jsonConfig;
    }

    @Override
    public void beforeStartingVertx(VertxOptions options) {
        LOG.debug("The cluster host from config.json is " + clusterHost);
        options.setClusterHost(clusterHost).setClustered(true);
    }

    @Override public void afterStoppingVertx() {
        super.afterStoppingVertx();
        LOG.debug("Executing shutdown hooks");
        for (Runnable listener : listeners)
            try {
                listener.run();
            } catch (Throwable e) {
                LOG.error("Could not run an undeployment shutdown hook.", e);
            }
    }

    @Override
    public void beforeDeployingVerticle(DeploymentOptions deploymentOptions) {
        super.beforeDeployingVerticle(deploymentOptions);

        if (deploymentOptions.getConfig() == null) {
            deploymentOptions.setConfig(new JsonObject());
        }

        deploymentOptions.getConfig().mergeIn(jsonConfig);
    }
}
