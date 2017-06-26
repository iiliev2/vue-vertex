package vw.server.common;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;

import java.io.InputStream;
import java.util.Scanner;

public class IOUtils {

    public static DeploymentOptions loadFileInJsonFormat(String configuration, Class configClass){
        DeploymentOptions options = new DeploymentOptions();
        InputStream config = configClass.getResourceAsStream(configuration);
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
