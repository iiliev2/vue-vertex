package vw.be.server.service;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.MultiMap;
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
import vw.be.server.verticle.ManageUserDatabaseVerticle;

import java.io.IOException;

import static vw.be.server.common.ITestConstants.*;
import static vw.be.server.common.PersistenceActionEnum.*;
import static vw.be.server.common.PersistenceResponseCodeEnum.*;
import static vw.be.server.service.IManageUserService.*;

/**
 * This is our JUnit test for our MONGO/Mock persistence service for user management.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUserDatabaseVerticleTest {

    private static final String VERSION_FIELD = "version";
    private static final int TEST_TIMEOUT_IN_SECONDS = 3;

    private Vertx vertx;
    private DeploymentOptions options;

    @Rule
    public Timeout rule = Timeout.seconds(TEST_TIMEOUT_IN_SECONDS);

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
    public void getAllUsers(TestContext context) {
        final Async async = context.async();

        DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_ALL));
        vertx.eventBus().send(IManageUserService.DB_QUEUE, new JsonObject(), options, reply -> {
            if (reply.succeeded()) {
                context.assertNotNull(reply.result());
                context.assertNotNull(reply.result().body());
                context.assertFalse(((JsonArray) reply.result().body()).isEmpty());
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
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE)), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                Object createResponseBody = replyOfCreate.result().body();
                MultiMap headers = replyOfCreate.result().headers();
                context.assertTrue(headers.contains(PERSISTENCE_RESPONSE_CODE));
                context.assertTrue(String.valueOf(CREATED).contentEquals(headers.get(PERSISTENCE_RESPONSE_CODE)));
                JsonObject getByIdRequest = new JsonObject().put(ID, createResponseBody);
                vertx.eventBus().send(IManageUserService.DB_QUEUE, getByIdRequest, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(GET_BY_ID)), replyOfGet -> {
                    if (replyOfGet.succeeded()) {
                        Object user = replyOfGet.result().body();
                        context.assertNotNull(user);
                        context.assertTrue(user.toString().contains(ID));
                        context.assertTrue(user.toString().contains(VERSION_FIELD));
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
        DeliveryOptions options = new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE));
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, options, reply -> {
            if (reply.succeeded()) {
                MultiMap headers = reply.result().headers();
                context.assertTrue(headers.contains(PERSISTENCE_RESPONSE_CODE));
                context.assertTrue(String.valueOf(CREATED).contentEquals(headers.get(PERSISTENCE_RESPONSE_CODE)));
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
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE)), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                Object createResponseBody = replyOfCreate.result().body();
                MultiMap headers = replyOfCreate.result().headers();
                context.assertTrue(headers.contains(PERSISTENCE_RESPONSE_CODE));
                context.assertTrue(String.valueOf(CREATED).contentEquals(headers.get(PERSISTENCE_RESPONSE_CODE)));
                JsonObject userToEdit = IOUtils.loadConfiguration(SIMPLE_USER_FOR_EDITION_JSON_FILE, this.getClass()).put(ID, createResponseBody);
                vertx.eventBus().send(IManageUserService.DB_QUEUE, userToEdit, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(MERGE)), replyOfMerge -> {
                    if (replyOfMerge.succeeded()) {
                        MultiMap mergeHeaders = replyOfMerge.result().headers();
                        context.assertTrue(mergeHeaders.contains(PERSISTENCE_RESPONSE_CODE));
                        context.assertTrue(String.valueOf(MERGED).contentEquals(mergeHeaders.get(PERSISTENCE_RESPONSE_CODE)));
                        async.complete();
                    } else {
                        context.fail(replyOfMerge.cause());
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
        vertx.eventBus().send(IManageUserService.DB_QUEUE, userToCreate, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(CREATE)), replyOfCreate -> {
            if (replyOfCreate.succeeded()) {
                Object createResponseBody = replyOfCreate.result().body();
                MultiMap headers = replyOfCreate.result().headers();
                context.assertTrue(headers.contains(PERSISTENCE_RESPONSE_CODE));
                context.assertTrue(String.valueOf(CREATED).contentEquals(headers.get(PERSISTENCE_RESPONSE_CODE)));
                JsonObject deleteByIdRequest = new JsonObject().put(ID, createResponseBody);
                vertx.eventBus().send(IManageUserService.DB_QUEUE, deleteByIdRequest, new DeliveryOptions().addHeader(PERSISTENCE_ACTION, String.valueOf(DELETE_BY_ID)), replyOfDelete -> {
                    if (replyOfDelete.succeeded()) {
                        MultiMap deleteHeaders = replyOfDelete.result().headers();
                        context.assertTrue(deleteHeaders.contains(PERSISTENCE_RESPONSE_CODE));
                        context.assertTrue(String.valueOf(DELETED).contentEquals(deleteHeaders.get(PERSISTENCE_RESPONSE_CODE)));
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
