package vw.server.controller;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import vw.common.dto.UserDTO;
import vw.server.common.HttpStatusCodeEnum;
import vw.server.common.IOUtils;
import vw.server.webapi.ManageUserVerticle;

import java.io.IOException;
import java.util.List;

import static vw.server.common.IConfigurationConstants.*;
import static vw.server.common.IWebApiConstants.APPLICATION_JSON_CHARSET_UTF_8;
import static vw.server.common.IWebApiConstants.HEADER_CONTENT_LENGTH;
import static vw.server.common.IWebApiConstants.HEADER_CONTENT_TYPE;

/**
 * This is our JUnit test for our rest api controller.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUserRestControllerTest {

    private static final String MY_APP_TEST_CONFIG_FILE = "/my-app-test-config.json";

    private static final String INDEX_PAGE_CONTEXT = "/";

    private static final String USER_ID_TO_FIND = "/1";

    private static final String SIMPLE_CREATE_USER_JSON_FILE = System.getProperty("user.dir") + "/src/test/resources/simple_user_for_creation.json";
    private static final String SIMPLE_UPDATE_USER_JSON_FILE = System.getProperty("user.dir") + "/src/test/resources/simple_user_for_edition.json";

    private static final String INDEX_PAGE_TITLE = "<title>App</title>";

    private Vertx vertx;
    private DeploymentOptions options;

    /**
     * Before executing our test, let's deploy our verticle.
     * <p/>
     * This method instantiates a new Vertx and deploy the verticle. Then, it waits in the verticle has successfully
     * completed its start sequence (thanks to `context.asyncAssertSuccess`).
     *
     * @param context the test context.
     */
    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        if (options == null) {
            options = IOUtils.loadConfiguration(MY_APP_TEST_CONFIG_FILE, this.getClass());
        }

        vertx.deployVerticle(ManageUserVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     * @param context the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Let's ensure that our application behaves correctly.
     * @param context the test context
     */
    @Test
    public void testApplicationRootContext(TestContext context) {
        // This test is asynchronous, so get an async handler to inform the test when we are done.
        final Async async = context.async();

        // We create a HTTP client and query our application. When we get the response we check it contains the some string
        // message. Then, we call the `complete` method on the async handler to declare this async (and here the test) done.
        // Notice that the assertions are made on the 'context' object and are not Junit assert. This ways it manage the
        // async aspect of the test the right way.
        vertx.createHttpClient().getNow(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                INDEX_PAGE_CONTEXT,
                response -> response.handler(body -> {
                    context.assertTrue(body.toString().contains(INDEX_PAGE_TITLE));
                    async.complete();
                }));
    }

    @Test
    public void addUser(TestContext context) {
        readJsonFile(context,
                context.async(),
                SIMPLE_CREATE_USER_JSON_FILE,
                true);
    }

    @Test
    public void editUser(TestContext context) {
        readJsonFile(context,
                context.async(),
                SIMPLE_UPDATE_USER_JSON_FILE,
                false);
    }

    @Test
    public void getAllUsers(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().get(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                (options.getConfig().getString(
                        USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)
                        + options.getConfig().getString(GET_ALL_USERS_SUB_CONTEXT_KEY, DEFAULT_GET_ALL_USERS_SUB_CONTEXT_VALUE)))
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatusCodeEnum.OK.getStatusCode());
                    context.assertTrue(response.headers().get(HEADER_CONTENT_TYPE).contains(APPLICATION_JSON_CHARSET_UTF_8));
                    response.bodyHandler(body -> {
                        //Just test decode
                        context.assertTrue(!Json.decodeValue(body.toString(), List.class).isEmpty());
                        async.complete();
                    }).exceptionHandler(defineThrowableHandler(context));
                })
                .end();
    }

    @Test
    public void getUserById(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().get(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                (options.getConfig().getString(USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE) + USER_ID_TO_FIND))
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .handler(response -> {
                    boolean isOKHttpStatus = ( response.statusCode() == HttpStatusCodeEnum.OK.getStatusCode() );
                    context.assertTrue( (response.statusCode() == HttpStatusCodeEnum.NOT_FOUND.getStatusCode() || isOKHttpStatus) );
                    response.bodyHandler(body -> {
                        if (isOKHttpStatus) {
                            final UserDTO user = Json.decodeValue(body.toString(), UserDTO.class);
                            context.assertNotNull(user.getId());
                            context.assertEquals(user.getId(), "1");
                            context.assertEquals(user.getVersion(), 1L);
                        }
                        async.complete();
                    }).exceptionHandler(defineThrowableHandler(context));
                })
                .end();
    }

    @Test
    public void deleteUserBySuccessfulyId(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().delete(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                (options.getConfig().getString(USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE) + USER_ID_TO_FIND))
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatusCodeEnum.NO_CONTENT.getStatusCode());
                    async.complete();
                })
                .end();
    }

    private void readJsonFile(TestContext context, Async async, String filename, boolean isCreate) {
        vertx.fileSystem().readFile(filename, result -> {
            if (result.succeeded()) {
                if (isCreate) {
                    manageUser(
                            result.result(),
                            (options.getConfig().getString(
                                    USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)
                                    + options.getConfig().getString(ADD_USER_SUB_CONTEXT_KEY, DEFAULT_ADD_USER_SUB_CONTEXT_VALUE)),
                            addUserHttpClientResponseHandler(
                                    context,
                                    HttpStatusCodeEnum.CREATED,
                                    addUserBodyHandler(
                                            context,
                                            async,
                                            result)));
                } else {
                    manageUser(
                            result.result(),
                            (options.getConfig().getString(
                                    USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)
                                    + options.getConfig().getString(EDIT_USER_SUB_CONTEXT_KEY, DEFAULT_EDIT_USER_SUB_CONTEXT_VALUE)),
                            editUserHttpClientResponseHandler(
                                    context,
                                    HttpStatusCodeEnum.NOT_FOUND,
                                    editUserBodyHandler(async)));
                }
            } else {
                context.fail(result.cause());
            }
        });
    }

    private void manageUser(Buffer fileContent, String operationURL, Handler<HttpClientResponse> clientResponseHandler) {
        vertx.createHttpClient().post(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                operationURL)
                .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
                .putHeader(HEADER_CONTENT_LENGTH, Integer.toString(fileContent.toString().length()))
                .handler(clientResponseHandler)
                .write(fileContent.toString())
                .end();
    }

    private Handler<HttpClientResponse> addUserHttpClientResponseHandler(TestContext context, HttpStatusCodeEnum statusCode, Handler<Buffer> bodyHandler) {
        return response -> {
            context.assertEquals(response.statusCode(), statusCode.getStatusCode());
            context.assertTrue(response.headers().get(HEADER_CONTENT_TYPE).contains(APPLICATION_JSON_CHARSET_UTF_8));
            response.
                    bodyHandler(bodyHandler).
                    exceptionHandler(defineThrowableHandler(context));
        };
    }

    private Handler<Buffer> addUserBodyHandler(TestContext context, Async async, AsyncResult<Buffer> result) {
        return body -> {
            UserDTO inputUser = Json.decodeValue(result.result().toString(), UserDTO.class);
            final UserDTO outputUser = Json.decodeValue(body.toString(), UserDTO.class);
            context.assertEquals(outputUser.getFirstName(), inputUser.getFirstName());
            context.assertEquals(outputUser.getSurname(), inputUser.getSurname());
            context.assertEquals(outputUser.getLastName(), inputUser.getLastName());
            context.assertEquals(outputUser.getVersion(), inputUser.getVersion());
            context.assertNotNull(outputUser.getId());
            async.complete();
        };
    }

    private Handler<HttpClientResponse> editUserHttpClientResponseHandler(TestContext context, HttpStatusCodeEnum statusCode, Handler<Buffer> bodyHandler) {
        return response -> {
            context.assertEquals(response.statusCode(), statusCode.getStatusCode());
            response.
                    bodyHandler(bodyHandler).
                    exceptionHandler(defineThrowableHandler(context));
        };
    }

    private Handler<Buffer> editUserBodyHandler(Async async) {
        return body -> async.complete();
    }

    private Handler<Throwable> defineThrowableHandler(TestContext context) {
        return exception -> {
            if(exception != null){
                context.fail();
            }
        };
    }

}
