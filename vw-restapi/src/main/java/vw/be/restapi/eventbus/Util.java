package vw.be.restapi.eventbus;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.MessageCodec;

public class Util {

    private Util() {
    }

    public static <D extends IHTTPTransmitOverEB, O extends IHTTPTransmitOverEB> void initEventBusCodecs(EventBus eventBus,
                                                                                                         Class<D> defaultType,
                                                                                                         MessageCodec<D, ?> defaultCodec,
                                                                                                         MessageCodec<O, ?> onDemandCodec) {

        eventBus.registerDefaultCodec(defaultType, defaultCodec);
        eventBus.registerCodec(onDemandCodec);
    }
}
