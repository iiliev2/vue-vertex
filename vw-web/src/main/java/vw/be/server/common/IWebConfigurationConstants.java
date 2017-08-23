package vw.be.server.common;

import vw.be.common.IConfigurationConstants;

public interface IWebConfigurationConstants extends IConfigurationConstants {

    String HTTP_PORT_KEY = "http.port";
    int DEFAULT_HTTP_PORT_VALUE = 23000;

    String REST_API_CONTEXT_PATTERN_KEY = "web.api.context";
    String DEFAULT_REST_API_CONTEXT_PATTERN = "/api/*";
}
