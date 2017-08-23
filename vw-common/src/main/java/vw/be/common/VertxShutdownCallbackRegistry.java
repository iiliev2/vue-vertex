package vw.be.common;

import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;

public class VertxShutdownCallbackRegistry implements ShutdownCallbackRegistry {

    @Override public Cancellable addShutdownCallback(Runnable callback) {
        DistributedLauncher.get().addShutdownCallback(callback);
        return new Cancellable() {
            @Override public void cancel() {
                DistributedLauncher.get().cancelShutdownCallback(callback);
            }

            @Override public void run() {
                //will be called by the launcher
            }
        };
    }
}
