package vw.be.server.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.MetricsService;

import static vw.be.server.common.IConfigurationConstants.DEFAULT_TIMER_LOG_PERIOD;
import static vw.be.server.common.IConfigurationConstants.TIMER_LOG_PERIOD_KEY;

public class MonitoringVerticle extends AbstractVerticle {

    private static final String MANAGE_USER_EVENTBUS_QUEUE = "vertx.eventbus.handlers.manage.user.db.queue";

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringVerticle.class);

    @Override
    public void start() throws Exception {
        MetricsService service = MetricsService.create(vertx);

        vertx.setPeriodic(config().getInteger(TIMER_LOG_PERIOD_KEY, DEFAULT_TIMER_LOG_PERIOD), h -> {
            LOGGER.info(Json.encodePrettily(service.getMetricsSnapshot(MANAGE_USER_EVENTBUS_QUEUE)));
        });

    }
}
