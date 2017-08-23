package vw.be.proxy;

import io.vertx.serviceproxy.ProxyHelper;
import vw.be.common.MicroServiceVerticle;

/**
 * This verticle will register an event bus proxy service. The service must be generated using the proxy api(annotated
 * with {@link io.vertx.codegen.annotations.ProxyGen}).
 *
 * @param <T> service type
 */
public abstract class ProxyServiceVerticle<T> extends MicroServiceVerticle {

    @Override public void start() {
        super.start();
        T service = createService();
        Class<? super T> tClass = serviceType();
        String address = address();
        ProxyHelper.registerService(tClass, vertx, service, address);
        publishEventBusService(this.getClass().getName(), address, tClass, ar -> {
            if (ar.failed()) {
                ar.cause().printStackTrace();
            } else {
                System.out.println(this.getClass().getName() + " published : " + ar.succeeded());
            }
        });
    }

    /**
     * Create an instance of the service.
     *
     * @return the instance
     */
    public abstract T createService();

    /**
     * The type that will be registered in the service discovery. This should be the service interface and not any
     * concrete implementation of it.
     *
     * @return the type
     */
    public abstract Class<? super T> serviceType();

    /**
     * Address of the service on the event bus.
     *
     * @return the address
     */
    public abstract String address();
}
