package vw.be.server.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import vw.be.common.dto.UserDTO;
import vw.be.server.common.IOUtils;
import vw.be.server.factory.ManageUserServiceFactory;
import vw.be.server.sevice.IManageUserService;

import java.io.IOException;

import static vw.be.server.controller.WebVerticleTest.MY_APP_TEST_CONFIG_FILE;
import static vw.be.server.factory.ManageUserServiceFactory.DB_TYPE_KEY;

/**
 * This is our JUnit test for our MONGO persistence service for user management.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUserServiceTest {

    private static final String SIMPLE_USER_FOR_CREATION_JSON_FILE = "/simple_user_for_creation.json";
    private static final String SIMPLE_USER_FOR_EDITION_JSON_FILE = "/simple_user_for_edition.json";
    private static final String UPDATED_SURNAME = "The Master";

    private static MongoClient mongoClient;
    private static IManageUserService mongoManageUserService;

    @BeforeClass
    public static void setUp() throws IOException {
        JsonObject config = IOUtils.loadConfiguration(MY_APP_TEST_CONFIG_FILE, ManageUserServiceTest.class);
        if (!config.getString(DB_TYPE_KEY, "").isEmpty()) {
            mongoClient = MongoClient.createNonShared(Vertx.vertx(), config);
        }
        mongoManageUserService = ManageUserServiceFactory.getService(Vertx.vertx(), config);
    }

    @AfterClass
    public static void tearDown() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Test
    public void getAllUsers(TestContext context){
        final Async async = context.async();

        mongoManageUserService
                .getAllUsers()
                .setHandler(res -> {
                                if (res.succeeded()) {
                                    if(res.result() == null){
                                        context.fail(res.cause());
                                    } else {
                                        context.assertTrue(res.result() != null && !res.result().isEmpty());
                                        async.complete();
                                    }
                                } else {
                                    context.fail(res.cause());
                                }
                            }
                );

    }

    @Test
    public void getUserById(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserServiceTest.class);
        UserDTO userDTO = Json.decodeValue(userToCreate.toString(), UserDTO.class);
        mongoManageUserService
                .createUser(userDTO)
                .setHandler(creationResult -> {
                    if (creationResult.succeeded()) {
                        if(creationResult.result()){
                            mongoManageUserService
                                    .getUserById(userDTO.getId())
                                    .setHandler(foundResult -> {
                                        if (foundResult.succeeded()) {
                                            if(foundResult.result().isPresent()){
                                                String user = foundResult.result().get();
                                                context.assertTrue(!user.isEmpty());
                                                context.assertTrue(user.contains("id"));
                                                async.complete();
                                            } else {
                                                context.fail(foundResult.cause());
                                            }
                                        } else {
                                            context.fail(foundResult.cause());
                                        }
                                    });
                        } else{
                            context.fail(creationResult.cause());
                        }
                    } else {
                        context.fail(creationResult.cause());
                    }
                });
    }

    @Test
    public void addUser(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserServiceTest.class);
        UserDTO userDTO = Json.decodeValue(userToCreate.toString(), UserDTO.class);
        mongoManageUserService
                .createUser(userDTO)
                .setHandler(res -> {
                    if (res.succeeded()) {
                        if(res.result()){
                            context.assertNotNull(userDTO.getId());
                            async.complete();
                        } else{
                            context.fail(res.cause());
                        }
                    } else {
                        context.fail(res.cause());
                    }
                });
    }

    @Test
    public void editUser(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserServiceTest.class);
        UserDTO userToCreateDTO = Json.decodeValue(userToCreate.toString(), UserDTO.class);
        mongoManageUserService
                .createUser(userToCreateDTO)
                .setHandler(creationResult -> {
                    if (creationResult.succeeded()) {
                        if(creationResult.result()){
                            JsonObject userToEdit = IOUtils.loadConfiguration(SIMPLE_USER_FOR_EDITION_JSON_FILE, ManageUserServiceTest.class);
                            UserDTO userToEditDTO = Json.decodeValue(userToEdit.toString(), UserDTO.class);
                            userToEditDTO.setUserId(userToCreateDTO.getId());
                            mongoManageUserService
                                    .updateUser(userToEditDTO)
                                    .setHandler(res -> {
                                        if (res.succeeded()) {
                                            if(res.result()){
                                                context.assertNotNull(userToEditDTO.getId());
                                                context.assertEquals(UPDATED_SURNAME, userToEditDTO.getSurname());
                                                async.complete();
                                            } else{
                                                context.fail(res.cause());
                                            }
                                        } else {
                                            context.fail(res.cause());
                                        }
                                    });
                        } else{
                            context.fail(creationResult.cause());
                        }
                    } else {
                        context.fail(creationResult.cause());
                    }
                });
    }

    @Test
    public void deleteUserById(TestContext context) {
        final Async async = context.async();

        JsonObject userToCreate = IOUtils.loadConfiguration(SIMPLE_USER_FOR_CREATION_JSON_FILE, ManageUserServiceTest.class);
        UserDTO userDTO = Json.decodeValue(userToCreate.toString(), UserDTO.class);
        mongoManageUserService
                .createUser(userDTO)
                .setHandler(creationResult -> {
                    if (creationResult.succeeded()) {
                        if(creationResult.result()){
                            mongoManageUserService
                                    .deleteUserById(userDTO.getId())
                                    .setHandler(res -> {
                                        if (res.succeeded()) {
                                            if(res.result()){
                                                async.complete();
                                            } else {
                                                context.fail(res.cause());
                                            }
                                        } else {
                                            context.fail(res.cause());
                                        }
                                    });
                        } else{
                            context.fail(creationResult.cause());
                        }
                    } else {
                        context.fail(creationResult.cause());
                    }
                });
    }

}
