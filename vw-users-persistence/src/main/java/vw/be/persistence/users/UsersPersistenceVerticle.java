package vw.be.persistence.users;

import vw.be.persistence.users.service.IManageUserService;
import vw.be.proxy.ProxyServiceVerticle;

public abstract class UsersPersistenceVerticle<T extends IManageUserService> extends ProxyServiceVerticle<T> {
    @Override public Class<IManageUserService> serviceType() {
        return IManageUserService.class;
    }

    @Override public String address() {
        return "manage.user.db.queue";
    }
}
