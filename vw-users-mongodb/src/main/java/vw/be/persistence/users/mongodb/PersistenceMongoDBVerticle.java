package vw.be.persistence.users.mongodb;

import vw.be.persistence.users.UsersPersistenceVerticle;

public class PersistenceMongoDBVerticle extends UsersPersistenceVerticle<MongoDBUserService> {

    @Override
    public MongoDBUserService createService() {
        return new MongoDBUserService();
    }
}
