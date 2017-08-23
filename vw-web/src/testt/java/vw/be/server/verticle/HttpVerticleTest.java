package testt.java.vw.be.server.verticle;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.json.Json;
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
import testt.java.vw.be.server.common.ITestConstants;
import vw.be.common.dto.UserDTO;
import vw.be.server.verticle.HttpVerticle;

import java.io.IOException;

import static vw.be.server.common.IHttpApiConstants.*;
import static testt.java.vw.be.server.common.ITestConstants.*;
import static vw.be.server.common.IWebConfigurationConstants.*;
import static vw.be.server.service.MockManageUserService.FIRST_USER_ID;
import static vw.be.server.service.MockManageUserService.FIRST_USER_VERSION;

/**
 * This is our JUnit test for our rest api controller.
 * It will work with mocked persistence data, always.
 */
@RunWith(VertxUnitRunner.class)
public class HttpVerticleTest {

    private static final String URL_CONTEXT_SEPARATOR = "/";
    private static final String INDEX_PAGE_TITLE = "<title>App</title>";

    private Vertx vertx;
    private DeploymentOptions options;

    @Rule
    public Timeout rule = Timeout.seconds(3);

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
            JsonObject config = IOUtils.loadConfiguration(
                    ITestConstants.MY_APP_TEST_CONFIG_FILE,
                    this.getClass()
                                                         );
            removeDBProviderConfiguration(config);
            options = new DeploymentOptions()
                    .setConfig(
                            config
                              );
        }

        vertx.deployVerticle(
                ManageUserDatabaseVerticle.class.getName(),
                options,
                res -> vertx.deployVerticle(HttpVerticle.class.getName(), options, context.asyncAssertSuccess())
                            );
    }

    /**
     * DB provider is removed, because we want to mock persistence service.
     *
     * @param config test configuration
     */
    private void removeDBProviderConfiguration(JsonObject config) {
        config.remove(DB_TYPE_KEY);
        config.remove(DB_NAME_KEY);
        config.remove(CONNECTION_URL_KEY);
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     *
     * @param context the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Let's ensure that our application behaves correctly.
     *
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
                                        WEB_ROOT_CONTEXT,
                                        response -> response.handler(body -> {
                                            context.assertTrue(body.toString().contains(INDEX_PAGE_TITLE));
                                            async.complete();
                                        }));
    }

    @Test
    public void addUser(TestContext context) {
        final JsonObject userToCreate = IOUtils.loadConfiguration(ITestConstants.SIMPLE_USER_FOR_CREATION_JSON_FILE, this.getClass());
        manageUser(
                userToCreate.toString(),
                (options.getConfig().getString(
                        USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)),
                addUserHttpClientResponseHandler(
                        context,
                        HttpStatusCodeEnum.CREATED,
                        addUserBodyHandler(
                                context)));

    }

    @Test
    public void editUser(TestContext context) {
        final String userToEdit = IOUtils.loadConfiguration(ITestConstants.SIMPLE_USER_FOR_EDITION_JSON_FILE, this.getClass())
                                         .toString();
        manageUser(
                userToEdit,
                (options.getConfig().getString(
                        USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)),
                editUserHttpClientResponseHandler(
                        context,
                        HttpStatusCodeEnum.NOT_FOUND,
                        e -> {
                        }));

    }

    @Test
    public void getAllUsers(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().get(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                                     options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                                     (options.getConfig().getString(
                                             USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE)))
             .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
             .handler(response -> {
                 context.assertEquals(response.statusCode(), HttpStatusCodeEnum.OK.getStatusCode());
                 context.assertTrue(response.headers()
                                            .get(HEADER_CONTENT_TYPE)
                                            .contains(APPLICATION_JSON_CHARSET_UTF_8));
                 response.bodyHandler(body -> {
                     context.assertNotNull(body);
                     context.assertFalse(body.toJsonArray().isEmpty());
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
                                     (options.getConfig()
                                             .getString(USER_WEB_API_CONTEXT_KEY, DEFAULT_USER_WEB_API_CONTEXT_VALUE) +
                                      URL_CONTEXT_SEPARATOR +
                                      FIRST_USER_ID))
             .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
             .handler(response -> {
                 boolean isOKHttpStatus = (response.statusCode() == HttpStatusCodeEnum.OK.getStatusCode());
                 context.assertTrue((response.statusCode() == HttpStatusCodeEnum.NOT_FOUND.getStatusCode() ||
                                     isOKHttpStatus));
                 response.bodyHandler(body -> {
                     if (isOKHttpStatus) {
                         final UserDTO user = Json.decodeValue(body.toString(), UserDTO.class);
                         context.assertNotNull(user.getId());
                         context.assertEquals(user.getId(), FIRST_USER_ID);
                         context.assertEquals(user.getVersion(), FIRST_USER_VERSION);
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
                                        (options.getConfig()
                                                .getString(USER_WEB_API_CONTEXT_KEY,
                                                           DEFAULT_USER_WEB_API_CONTEXT_VALUE) +
                                         URL_CONTEXT_SEPARATOR +
                                         FIRST_USER_ID))
             .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
             .handler(response -> {
                 context.assertEquals(response.statusCode(), HttpStatusCodeEnum.NO_CONTENT.getStatusCode());
                 async.complete();
             })
             .end();
    }

    private void manageUser(String fileContent,
                            String operationURL,
                            Handler<HttpClientResponse> clientResponseHandler) {
        vertx.createHttpClient().post(options.getConfig().getInteger(HTTP_PORT_KEY, DEFAULT_HTTP_PORT_VALUE),
                                      options.getConfig().getString(APP_HTTP_HOST_KEY, DEFAULT_HOST),
                                      operationURL)
             .putHeader(HEADER_CONTENT_TYPE, APPLICATION_JSON_CHARSET_UTF_8)
             .putHeader(HEADER_CONTENT_LENGTH, Integer.toString(fileContent.length()))
             .handler(clientResponseHandler)
             .write(fileContent)
             .end();
    }

    private Handler<HttpClientResponse> addUserHttpClientResponseHandler(TestContext context,
                                                                         HttpStatusCodeEnum statusCode,
                                                                         Handler<Buffer> bodyHandler) {
        return response -> {
            context.assertEquals(response.statusCode(), statusCode.getStatusCode());
            context.assertTrue(response.headers().get(HEADER_CONTENT_TYPE).contains(APPLICATION_JSON_CHARSET_UTF_8));
            response.
                            bodyHandler(bodyHandler).
                            exceptionHandler(defineThrowableHandler(context));
        };
    }

    private Handler<Buffer> addUserBodyHandler(TestContext context) {
        Async async = context.async();
        return body -> {
            context.assertNotNull(body.toString());
            async.complete();
        };
    }

    private Handler<HttpClientResponse> editUserHttpClientResponseHandler(TestContext context,
                                                                          HttpStatusCodeEnum statusCode,
                                                                          Handler<Buffer> bodyHandler) {
        return response -> {
            context.assertEquals(response.statusCode(), statusCode.getStatusCode());
            response.
                            bodyHandler(bodyHandler).
                            exceptionHandler(defineThrowableHandler(context));
        };
    }

    private Handler<Throwable> defineThrowableHandler(TestContext context) {
        return exception -> {
            if (exception != null) {
                context.fail();
            }
        };
    }

}
