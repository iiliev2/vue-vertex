package vw.be.persistence.inmem;


import vw.be.persistence.service.PersistenceVerticle;

public class PersistenceInMemVerticle extends PersistenceVerticle<InMemoryUserService> {

    @Override
    public InMemoryUserService createService() {
        return new InMemoryUserService();
    }
}
