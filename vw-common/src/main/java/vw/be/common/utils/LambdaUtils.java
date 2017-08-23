package vw.be.common.utils;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.function.Supplier;

public class LambdaUtils {
    private LambdaUtils() {
    }

    public static <T> Handler<T> noop() {
        return t -> {
        };
    }

    public static <T> Handler<AsyncResult<T>> asyncHandler(Handler<T> onSuccess, Runnable onFailure) {
        return async -> successOrFailureHandler(async::succeeded, async::result, onSuccess, onFailure);

    }

    public static <T> Handler<T> doIf(Supplier<Boolean> condition,
                                      Runnable onSuccess,
                                      Runnable onFailure) {
        return successOrFailureHandler(condition, () -> null, ignoredVal -> onSuccess.run(), onFailure);
    }

    public static <T> Handler<T> successOrFailureHandler(Supplier<Boolean> condition,
                                                         Supplier<T> ofT,
                                                         Handler<T> onSuccess,
                                                         Runnable onFailure) {
        return q -> {
            if (condition.get()) {
                onSuccess.handle(ofT.get());
            } else onFailure.run();
        };
    }
}
