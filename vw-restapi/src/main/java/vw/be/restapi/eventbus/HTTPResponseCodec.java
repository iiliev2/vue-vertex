package vw.be.restapi.eventbus;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.impl.codecs.JsonObjectMessageCodec;

/**
 * The type Http response codec. Internally it uses the json codec to convert the data.
 */
public class HTTPResponseCodec implements MessageCodec<HTTPResponseOverEB, HTTPResponseOverEB> {

    private final JsonObjectMessageCodec delegate;

    /**
     * Instantiates a new Http response codec.
     */
    public HTTPResponseCodec() {
        delegate = new JsonObjectMessageCodec();
    }

    @Override public void encodeToWire(Buffer buffer, HTTPResponseOverEB httpResponseOverEB) {
        delegate.encodeToWire(buffer, httpResponseOverEB.toJson());
    }

    @Override public HTTPResponseOverEB decodeFromWire(int pos, Buffer buffer) {
        return HTTPResponseOverEB.fromJson(delegate.decodeFromWire(pos, buffer));
    }

    @Override public HTTPResponseOverEB transform(HTTPResponseOverEB httpResponseOverEB) {
        return httpResponseOverEB;
    }

    @Override public String name() {
        return HTTPResponseCodec.class.getSimpleName();
    }

    @Override public byte systemCodecID() {
        return -1;
    }
}
