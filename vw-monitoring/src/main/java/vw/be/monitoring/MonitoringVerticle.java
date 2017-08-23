package vw.be.monitoring;

import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.dropwizard.MetricsService;
import vw.be.common.MicroServiceVerticle;

import static vw.be.monitoring.IMonigoringConfigurationConstants.DEFAULT_TIMER_LOG_PERIOD;
import static vw.be.monitoring.IMonigoringConfigurationConstants.TIMER_LOG_PERIOD_KEY;


public abstract class MonitoringVerticle extends MicroServiceVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringVerticle.class);

    @Override
    public void start() {
        MetricsService service = MetricsService.create(vertx);

        vertx.setPeriodic(
                config().getInteger(TIMER_LOG_PERIOD_KEY, DEFAULT_TIMER_LOG_PERIOD),
                h -> LOGGER.info(Json.encodePrettily(service.getMetricsSnapshot(getMonitoringTarget())))
                         );
    }

    abstract String getMonitoringTarget();
}
