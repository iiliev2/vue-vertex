package vw.be.server.common;

import vw.be.common.IConfigurationConstants;

public interface IResourceBundleConstants extends IConfigurationConstants {

    String HTTP_SERVER_STARTED_OK_MESSAGE = "HTTP server is up and running on port %d!%n";
    String HTTP_SERVER_FAILED_MESSAGE = "HTTP server failed to run on port %d! Cause is %s";
}
