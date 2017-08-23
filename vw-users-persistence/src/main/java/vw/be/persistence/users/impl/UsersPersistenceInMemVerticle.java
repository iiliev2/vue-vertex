package vw.be.persistence.users.impl;


import io.vertx.core.Future;
import io.vertx.core.file.FileSystem;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vw.be.persistence.users.UsersPersistenceVerticle;
import vw.be.persistence.users.dto.UserDTO;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static vw.be.common.utils.IOUtils.loadConfiguration;
import static vw.be.persistence.users.impl.InMemoryUserService.getUsers;

/**
 * Default users persisting verticle using the in memory implementation of the service.
 */
public final class UsersPersistenceInMemVerticle extends UsersPersistenceVerticle<InMemoryUserService> {
    private final Logger LOG = LoggerFactory.getLogger(UsersPersistenceInMemVerticle.class);
    private final String USERS_JSON = "users.json";
    private InMemoryUserService service;


    @Override
    public InMemoryUserService createService() {
        JsonObject users = loadConfiguration(Paths.get(USERS_JSON).toAbsolutePath().toFile());
        LOG.debug(Paths.get(USERS_JSON).toAbsolutePath().toString());
        if (users == null) users = loadConfiguration(USERS_JSON, getClass().getClassLoader());
        if (users == null) users = new JsonObject().put("users", new JsonArray());
        service = new InMemoryUserService(users);
        return service;
    }

    @Override public void stop(Future<Void> future) throws Exception {
        List<UserDTO> users = getUsers();
        if (users.size() > 0) {
            FileSystem fs = vertx.fileSystem();
            LocalDateTime now = LocalDateTime.now();
            String baseFile = Paths.get(USERS_JSON).toAbsolutePath().toString();
            String output = baseFile + "." + now.format(DateTimeFormatter.ofPattern("uuuuMMddkkmmss"));
            LOG.debug("Backup old users to:" + output);
            fs.copy(baseFile,
                    output,
                    v -> {
                        if (v.succeeded()) {
                            LOG.warn("Overwriting the users");
                            fs.writeFile(baseFile,
                                         new JsonObject().put("users", new JsonArray(users)).toBuffer(),
                                         v2 -> callSuperClose(future));
                        } else {
                            LOG.warn("Backup failed, will not overwrite the users");
                            callSuperClose(future);
                        }
                    });
        } else
            super.stop(future);
    }

    private void callSuperClose(Future<Void> future) {
        try {
            super.stop(future);
        } catch (Exception e) {
            if (!future.isComplete()) future.fail(e);
        }
    }
}
