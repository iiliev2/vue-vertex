package vw.be.server.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Timeout;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import vw.be.server.common.IOUtils;
import vw.be.server.sevice.IManageUserService;
import vw.be.server.verticle.ManageUserDatabaseVerticle;

import java.io.IOException;
import java.util.List;

import static vw.be.server.common.ITestConstants.*;

/**
 * This is our JUnit test for our MONGO persistence service for user management.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUserDatabaseVerticleTest {

    private Vertx vertx;
    private DeploymentOptions options;

    @Rule
    public Timeout rule = Timeout.seconds(10);

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        if (options == null) {
            JsonObject config = IOUtils.loadConfiguration(
                    MY_APP_TEST_CONFIG_FILE,
                    this.getClass()
            );
            options = new DeploymentOptions()
                    .setConfig(
                        config
                    );
        }

        vertx.deployVerticle(ManageUserDatabaseVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void getAllUsers(TestContext context){
        final Async async = context.async();

        DeliveryOptions options = new DeliveryOptions().addHeader("action", "all-users");
        vertx.eventBus().send(IManageUserService.DB_QUEUE, new JsonObject(), options, reply -> {
            if (reply.succeeded()) {
                context.assertTrue(reply.result() != null && !((List)reply.result()).isEmpty());
                async.complete();
            } else {
                context.fail(reply.cause());
            }
        });
    }

    @Test
    public void getUserById(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserDatabaseVerticleTest.class);
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate.toString(), new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                JsonObject request = new JsonObject().put("id", userToCreate.getString("id"));
                vertx.eventBus().send(IManageUserService.DB_QUEUE, request, new DeliveryOptions().addHeader("action", "get-user-by-id"), replyOfGet -> {
                    if (replyOfGet.succeeded()) {
                        Object user = replyOfGet.result().body();
                        context.assertNotNull(user);
                        context.assertTrue(user.toString().contains("id"));
                        async.complete();
                    } else {
                        context.fail(replyOfGet.cause());
                    }
                });
            } else {
                context.fail(replyOfCreate.cause());
            }
        });
    }

    @Test
    public void addUser(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserDatabaseVerticleTest.class);
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "create-user");
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, options, reply -> {
            if (reply.succeeded()) {
                context.assertEquals("Created", reply.result().body());
                async.complete();
            } else {
                context.fail(reply.cause());
            }
        });
    }

    @Test
    public void editUser(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserDatabaseVerticleTest.class);
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                context.assertEquals("Created", replyOfCreate.result().body());
                JsonObject userToEdit = IOUtils.loadConfiguration(SIMPLE_USER_FOR_EDITION_JSON_FILE, ManageUserDatabaseVerticleTest.class)
                        .put("id", userToCreate.getString("id"));
                vertx.eventBus().send(IManageUserService.DB_QUEUE, userToEdit, new DeliveryOptions().addHeader("action", "edit-user"), replyOfUpdate -> {
                    if (replyOfUpdate.succeeded()) {
                        context.assertEquals("Edited", replyOfUpdate.result().body());
                        async.complete();
                    } else {
                        context.fail(replyOfUpdate.cause());
                    }
                });
            } else {
                context.fail(replyOfCreate.cause());
            }
        });
    }

    @Test
    public void deleteUserById(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserDatabaseVerticleTest.class);
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate.toString(), new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                context.assertEquals("Created", replyOfCreate.result().body());
                JsonObject userToEdit = IOUtils.loadConfiguration(SIMPLE_USER_FOR_EDITION_JSON_FILE, ManageUserDatabaseVerticleTest.class);
                vertx.eventBus().send(IManageUserService.DB_QUEUE, userToEdit.toString(), new DeliveryOptions().addHeader("action", "delete-user-by-id"), replyOfDelete -> {
                    if (replyOfDelete.succeeded()) {
                        context.assertEquals("Deleted", replyOfDelete.result().body());
                        async.complete();
                    } else {
                        context.fail(replyOfDelete.cause());
                    }
                });
            } else {
                context.fail(replyOfCreate.cause());
            }
        });
    }

}
