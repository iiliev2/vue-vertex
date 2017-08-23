package vw.be.common.utils;

import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Utility class for IO operations.
 */
public class IOUtils {
    private static final Logger LOG = LoggerFactory.getLogger(IOUtils.class);

    /**
     * Load json configuration in new deployment options object.
     *
     * @param configuration file path in distribution
     * @param configClass   class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static JsonObject loadConfiguration(String configuration, Class configClass) {
        return loadConfiguration(configClass.getResourceAsStream(configuration));
    }

    /**
     * Load json configuration in new deployment options object.
     *
     * @param configuration file path in distribution
     * @param classLoader   class loader of this class is used
     * @return loaded configuration or default if passed as argument does not exist.
     */
    public static JsonObject loadConfiguration(String configuration, ClassLoader classLoader) {
        return loadConfiguration(classLoader.getResourceAsStream(configuration));
    }

    public static JsonObject loadConfiguration(InputStream config) {
        return loadConfiguration(() -> new Scanner(config));
    }

    public static JsonObject loadConfiguration(File conf) {
        return loadConfiguration(() -> new Scanner(conf));
    }

    private static JsonObject loadConfiguration(ScannerCreator loader) {
        JsonObject conf = new JsonObject();
        try (Scanner scanner = loader.create().useDelimiter("\\A")) {
            String sconf = scanner.next();
            try {
                conf = new JsonObject(sconf);
            } catch (DecodeException e) {
                LOG.info("Invalid JSON object");
            }
        } catch (Throwable e) {
            LOG.info(e.getMessage());
        }
        return conf;
    }

    private interface ScannerCreator {
        Scanner create() throws Exception;
    }
}
