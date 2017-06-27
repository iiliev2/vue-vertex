package vw.server.common;

/**
 * HTTP status codes.
 */
public enum HttpStatusCodeEnum {
    OK(200), CREATED(201), NO_CONTENT(204), NOT_FOUND(404), BAD_REQUEST(400), INTERNAL_SERVER_ERROR(500), SERVICE_TEMPORARY_UNAVAILABLE(503);

    private int statusCode;

    HttpStatusCodeEnum(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
