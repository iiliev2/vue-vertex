package vw.be.persistence.mongodb;

import vw.be.persistence.service.PersistenceVerticle;

public class PersistenceMongoDBVerticle extends PersistenceVerticle<MongoDBUserService> {

    @Override
    public MongoDBUserService createService() {
        return new MongoDBUserService();
    }
}
