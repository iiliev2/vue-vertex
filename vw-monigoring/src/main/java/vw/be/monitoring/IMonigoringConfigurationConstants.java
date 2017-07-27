package vw.be.monitoring;

import vw.be.common.IConfigurationConstants;

public interface IMonigoringConfigurationConstants extends IConfigurationConstants {
    String MONITORING_VERTICLE_COUNT_KEY = "monitoring.verticle.count";
    int DEFAULT_MONITORING_VERTICLE_COUNT = 1;

    String START_MONITORING_KEY = "start.monitoring";
    boolean DEFAULT_START_MONITORING = false;

    String TIMER_LOG_PERIOD_KEY = "timer.log.period";
    int DEFAULT_TIMER_LOG_PERIOD = 60_000;//60 seconds
}
