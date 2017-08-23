package vw.be.restapi.eventbus;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;

/**
 * The event bus codec for a http request. Internally it uses the json codec to convert the data.
 */
public class HTTPRequestCodec implements MessageCodec<HTTPRequestOverEB, HTTPRequestOverEB> {
    private final JsonObjectMessageCodec delegate;

    /**
     * Instantiates a new Http request codec.
     */
    public HTTPRequestCodec() {
        delegate = new JsonObjectMessageCodec();
    }

    @Override public void encodeToWire(Buffer buffer, HTTPRequestOverEB httpRequestOverEB) {
        delegate.encodeToWire(buffer, httpRequestOverEB.toJson());
    }

    @Override public HTTPRequestOverEB decodeFromWire(int pos, Buffer buffer) {
        return HTTPRequestOverEB.fromJson(delegate.decodeFromWire(pos, buffer));
    }

    @Override public HTTPRequestOverEB transform(HTTPRequestOverEB httpRequestOverEB) {
        return httpRequestOverEB;
    }

    @Override public String name() {
        return HTTPRequestCodec.class.getSimpleName();
    }

    @Override public byte systemCodecID() {
        return -1;
    }
}
