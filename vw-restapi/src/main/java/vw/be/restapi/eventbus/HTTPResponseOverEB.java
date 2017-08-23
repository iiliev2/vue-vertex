package vw.be.restapi.eventbus;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.json.JsonObject;

/**
 * Represents a HTTP response which can be transmitted over the event bus.
 */
public class HTTPResponseOverEB implements IHTTPTransmitOverEB {

    private final HttpResponseStatus status;
    private final JsonObject headers;
    private final String body;

    /**
     * Instantiates a new Http response over eb.
     *
     * @param status  the status
     * @param headers the headers
     * @param body    the body
     */
    public HTTPResponseOverEB(HttpResponseStatus status, JsonObject headers, String body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public HttpResponseStatus getStatus() {
        return status;
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
     * Gets body.
     *
     * @return the body
     */
    public String getBody() {
        return body;
    }

    /**
     * Convert to json.
     *
     * @return the json object
     */
    public JsonObject toJson() {
        JsonObject result = new JsonObject();
        result.put("status", status.code());
        result.put("headers", headers);
        result.put("body", body);
        return result;
    }

    /**
     * Parse from json.
     *
     * @param json the json
     * @return the http response over eb
     */
    public static HTTPResponseOverEB fromJson(JsonObject json) {
        int statusCode = json.getInteger("status");
        JsonObject headers = json.getJsonObject("headers");
        String body = json.getString("body");
        return new HTTPResponseOverEB(HttpResponseStatus.valueOf(statusCode), headers, body);
    }
}
