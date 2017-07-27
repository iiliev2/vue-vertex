package vw.be.server.common;

import vw.be.common.IConfigurationConstants;

public interface IWebConfigurationConstants extends IConfigurationConstants {

    String APP_HTTP_HOST_KEY = "app.http.host";
    String DEFAULT_HOST = "localhost";
    String HTTP_PORT_KEY = "http.port";
    int DEFAULT_HTTP_PORT_VALUE = 23000;

    String WEB_ROOT_CONTEXT = "/";

    String ROUTE_ROOT = "/";

    String REST_API_CONTEXT_PATTERN_KEY = "web.api.context";
    String DEFAULT_REST_API_CONTEXT_PATTERN = "/api/*";

    String USER_WEB_API_CONTEXT_KEY = "user.web.api.context";
    String DEFAULT_USER_WEB_API_CONTEXT_VALUE = "/api/users";

    String HTTP_VERTICLE_COUNT_KEY = "http.verticle.count";
    int DEFAULT_HTTP_VERTICLE_COUNT = 1;
    String MONITORING_VERTICLE_COUNT_KEY = "monitoring.verticle.count";
    int DEFAULT_MONITORING_VERTICLE_COUNT = 1;

    String START_MONITORING_KEY = "start.monitoring";
    boolean DEFAULT_START_MONITORING = false;

    String TIMER_LOG_PERIOD_KEY = "timer.log.period";
    int DEFAULT_TIMER_LOG_PERIOD = 60_000;//60 seconds
}
