package vw.be.server.common;

public interface IConfigurationConstants {

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

    String DB_TYPE_KEY = "db.type";
    String DB_NAME_KEY = "db_name";
    String CONNECTION_URL_KEY = "connection_string";

    String MONGO_DB_PROVIDER = "MONGO";
    String MOCK_DB_PROVIDER = "MOCK";

    String DB_VERTICLE_COUNT_KEY = "db.verticle.count";
    int DEFAULT_DB_VERTICLE_COUNT = 1;
    String HTTP_VERTICLE_COUNT_KEY = "http.verticle.count";
    int DEFAULT_HTTP_VERTICLE_COUNT = 1;
}
