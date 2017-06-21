package vw.server.common;

public enum HttpStatusCodeEnum {
    OK(200), CREATED(201), NO_CONTENT(204), NOT_FOUND(404), BAD_REQUEST(400);

    private int statusCode;

    HttpStatusCodeEnum(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
