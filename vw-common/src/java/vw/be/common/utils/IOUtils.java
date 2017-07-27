package vw.be.common.utils;

import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.util.Scanner;

/**
 * Utility class for IO operations.
 */
public class IOUtils {

    /**
     * Load json configuration in new deployment options object.
     *
     * @param configuration file path in distribution
     * @param configClass   class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static JsonObject loadConfiguration(String configuration, Class configClass) {
        InputStream config = configClass.getResourceAsStream(configuration);

        return getDeploymentOptions(config);
    }

    /**
     * Load json configuration in new deployment options object.
     *
     * @param configuration file path in distribution
     * @param classLoader   class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static JsonObject loadConfiguration(String configuration, ClassLoader classLoader) {
        InputStream config = classLoader.getResourceAsStream(configuration);

        return getDeploymentOptions(config);
    }

    private static JsonObject getDeploymentOptions(InputStream config) {
        JsonObject configuration = null;
        if (config != null) {
            String text;
            try (Scanner scanner = new Scanner(config)) {
                text = scanner.useDelimiter("\\A").next();
            }

            configuration = new JsonObject(text);
        }

        return (configuration == null ? new JsonObject() : configuration);
    }

}
