package vw.be.server.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
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

import static vw.be.server.common.ITestConstants.*;

/**
 * This is our JUnit test for our MONGO/Mock persistence service for user management.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUserDatabaseVerticleTest {

    private Vertx vertx;
    private DeploymentOptions options;

    @Rule
    public Timeout rule = Timeout.seconds(3);

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
                context.assertNotNull(reply.result());
                context.assertNotNull(reply.result().body());
                context.assertFalse(((JsonArray)reply.result().body()).isEmpty());
                async.complete();
            } else {
                context.fail(reply.cause());
            }
        });
    }

    @Test
    public void getUserById(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, this.getClass());
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                JsonObject createResponseBody = (JsonObject) replyOfCreate.result().body();
                context.assertEquals("Created", createResponseBody.getString("taskStatus"));
                JsonObject getByIdRequest = new JsonObject().put("id", createResponseBody.getString("id"));
                vertx.eventBus().send(IManageUserService.DB_QUEUE, getByIdRequest, new DeliveryOptions().addHeader("action", "get-user-by-id"), replyOfGet -> {
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

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, this.getClass());
        DeliveryOptions options = new DeliveryOptions().addHeader("action", "create-user");
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, options, reply -> {
            if (reply.succeeded()) {
                JsonObject createResponseBody = (JsonObject) reply.result().body();
                context.assertEquals("Created", createResponseBody.getString("taskStatus"));
                async.complete();
            } else {
                context.fail(reply.cause());
            }
        });
    }

    @Test
    public void editUser(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, this.getClass());
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                JsonObject createResponseBody = (JsonObject) replyOfCreate.result().body();
                context.assertEquals("Created", createResponseBody.getString("taskStatus"));
                JsonObject userToEdit = IOUtils.loadConfiguration(SIMPLE_USER_FOR_EDITION_JSON_FILE, this.getClass())
                        .put("id", createResponseBody.getString("id"));
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

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, this.getClass());
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader("action", "create-user"), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                JsonObject createResponseBody = (JsonObject) replyOfCreate.result().body();
                context.assertEquals("Created", createResponseBody.getString("taskStatus"));
                JsonObject deleteByIdRequest = new JsonObject().put("id", createResponseBody.getString("id"));
                vertx.eventBus().send(IManageUserService.DB_QUEUE, deleteByIdRequest, new DeliveryOptions().addHeader("action", "delete-user-by-id"), replyOfDelete -> {
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
