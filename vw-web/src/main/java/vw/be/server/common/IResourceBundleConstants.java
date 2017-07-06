package vw.be.server.common;

public interface IResourceBundleConstants {

    String HTTP_SERVER_STARTED_OK_MESSAGE = "HTTP server is up and running on port %d!%n";
    String HTTP_SERVER_FAILED_MESSAGE = "HTTP server failed to run on port %d! Cause is %s";

    String VERTICLE_DEPLOYED_SUCCESSFULY_MSG = "Verticle with id : %s is successfuly deployed!%n";
    String VERTICLE_FAILED_TO_DEPLOY = "Failed to deploy : %s!";

    String BAD_PERSISTENCE_ACTION_MSG = "Bad persistence action: %s";
    String NO_ACTION_HEADER_SPECIFIED_MSG = "No action header specified";
    String USER_NOT_FOUND_MSG = "User does not exist!";

    String DB_VERTICLE_STARTED_OK_MESSAGE = "%s persistence provider is up and running!%n";

    String PROCESSOR_IS_STARTING = "%s is starting .....%n";
}
