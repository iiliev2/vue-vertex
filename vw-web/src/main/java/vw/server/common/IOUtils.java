package vw.server.common;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Utility class for IO operations.
 */
public class IOUtils {

    /**
     * Load json configuration in new deployment options object.
     * @param configuration file path in distribution
     * @param configClass class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static DeploymentOptions loadConfiguration(String configuration, Class configClass){
        InputStream config = configClass.getResourceAsStream(configuration);

        return getDeploymentOptions(config);
    }

    /**
     * Load json configuration in new deployment options object.
     * @param configuration file path in distribution
     * @param classLoader class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static DeploymentOptions loadConfiguration(String configuration, ClassLoader classLoader){
        InputStream config = classLoader.getResourceAsStream(configuration);

        return getDeploymentOptions(config);
    }

    private static DeploymentOptions getDeploymentOptions(InputStream config) {
        DeploymentOptions options = new DeploymentOptions();
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
