package vw.be.persistence.config;

import vw.be.common.IConfigurationConstants;

public interface IPersistenceConfigurationConstants extends IConfigurationConstants {
    String DB_TYPE_KEY = "db.type";
    String DB_NAME_KEY = "db_name";
    String CONNECTION_URL_KEY = "connection_string";

    String MONGO_DB_PROVIDER = "MONGO";
    String MOCK_DB_PROVIDER = "MOCK";

    String DB_VERTICLE_COUNT_KEY = "db.verticle.count";
    int DEFAULT_DB_VERTICLE_COUNT = 1;
}
