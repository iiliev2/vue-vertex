package vw.be.restapi.eventbus;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.Map;

/**
 * Represents a HTTP request which can be transmitted over the event bus.
 */
public class HTTPRequestOverEB implements IHTTPTransmitOverEB {

    private final HttpMethod method;
    private final JsonObject headers;
    private final JsonObject query;
    private final String uri;
    private final String body;

    /**
     * Factory method to create an event bus http request. The new uri will exclude the rest apis' root, and any query
     * parameters. The uri will start with the target rest api root path.
     *
     * @param restApisRoot path were all rest apis are mounted
     * @param context      a request context, holding the original http request
     * @return the event bus request
     */
    public static HTTPRequestOverEB from(String restApisRoot, RoutingContext context) {
        HttpServerRequest serverRequest = context.request();
        String uri = serverRequest.uri();
        int queryStart = uri.indexOf('?');
        if (queryStart > -1)
            uri = uri.substring(0, queryStart);
        String subPath = uri.replaceFirst(restApisRoot, "/");
        String body = context.getBodyAsString();
        return new HTTPRequestOverEB(serverRequest.method(),
                                     fromMap(serverRequest.headers()),
                                     fromMap(serverRequest.params()),
                                     subPath,
                                     body);
    }

    private static JsonObject fromMap(MultiMap map) {
        JsonObject result = new JsonObject();
        for (Map.Entry<String, String> e : map)
            result.put(e.getKey(), e.getValue());
        return result;
    }

    /**
     * Instantiates a new Http request over eb.
     *
     * @param method  the method
     * @param headers the headers
     * @param query   the query(both query and path parameters)
     * @param uri     the uri
     * @param body    the body
     */
    private HTTPRequestOverEB(HttpMethod method, JsonObject headers, JsonObject query, String uri, String body) {
        this.method = method;
        this.headers = headers;
        this.query = query;
        this.uri = uri;
        this.body = body;
    }

    /**
     * Gets method.
     *
     * @return the method
     */
    public HttpMethod getMethod() {
        return method;
    }

    /**
     * Gets headers.
     *
     * @return the headers
     */
    public JsonObject getHeaders() {
        return headers;
    }

    /**
     * Gets query.
     *
     * @return the query
     */
    public JsonObject getQuery() {
        return query;
    }

    /**
     * The uri must start with the target api root path.
     *
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * Gets body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Computes the target api root path from the given uri.
     *
     * @return the target api
     */
    public String getTargetAPI() {
        String restApiName = uri;
        int subEndpointStart = uri.indexOf('/', 1);
        if (subEndpointStart > -1)
            restApiName = uri.substring(0, subEndpointStart);
        return restApiName;
    }

    /**
     * Convert to json.
     *
     * @return the json object
     */
    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.put("method", method.name());
        result.put("headers", headers);
        result.put("query", query);
        result.put("uri", uri);
        result.put("body", body);
        return result;
    }

    /**
     * Parse from json.
     *
     * @param json the json
     * @return the http request over eb
     */
    public static HTTPRequestOverEB fromJson(JsonObject json) {
        String httpMethod = json.getString("method");
        JsonObject headers = json.getJsonObject("headers");
        JsonObject query = json.getJsonObject("query");
        String uri = json.getString("uri");
        String body = json.getString("body");
        return new HTTPRequestOverEB(HttpMethod.valueOf(httpMethod), headers, query, uri, body);
    }
}
