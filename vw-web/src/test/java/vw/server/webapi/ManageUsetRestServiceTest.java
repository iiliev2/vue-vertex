package vw.server.webapi;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.DecodeException;
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

import java.io.IOException;

/**
 * This is our JUnit test for our verticle.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUsetRestServiceTest {

    private static final String LOCALHOST = "localhost";
    private static final String SIMPLE_CREATE_USER_JSON_FILE = "C:/Projects/vue-vertex/vw-web/src/test/resources/simple_user_for_creation.json";

    private Vertx vertx;

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
        vertx.deployVerticle(ManageUserRestService.class.getName(), context.asyncAssertSuccess());
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
        vertx.createHttpClient().getNow(ManageUserRestService.HTTP_PORT, LOCALHOST, ManageUserRestService.CONTEXT_ROOT, response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains(ManageUserRestService.ROOT_CONTEXT_WELCOME_MESSAGE));
                async.complete();
            });
        });
    }

    @Test
    public void checkThatWeCanAdd(TestContext context) {
        readJsonFile(context, context.async());
    }

    private void readJsonFile(TestContext context, Async async) {
        vertx.fileSystem().readFile(SIMPLE_CREATE_USER_JSON_FILE, result -> {
            if (result.succeeded()) {
                createUser(context, async, result.result());
            } else {
                context.fail(result.cause());
            }
        });
    }

    private void createUser(TestContext context, Async async, Buffer fileContent) {
        vertx.createHttpClient().post(ManageUserRestService.HTTP_PORT, LOCALHOST, ManageUserRestService.URL_ADD_USER)
                .putHeader(ManageUserRestService.CONTENT_TYPE, ManageUserRestService.APPLICATION_JSON_CHARSET_UTF_8)
                .putHeader(ManageUserRestService.CONTENT_LENGTH_HEADER, Integer.toString(fileContent.toString().length()))
                .handler(response -> {
                    context.assertEquals(response.statusCode(), HttpStatusCodeEnum.CREATED.getStatusCode());
                    context.assertTrue(response.headers().get(ManageUserRestService.CONTENT_TYPE).contains(ManageUserRestService.APPLICATION_JSON_CHARSET_UTF_8));
                    response.bodyHandler(body -> {
                        final UserDTO user = Json.decodeValue(body.toString(), UserDTO.class);
                        context.assertEquals(user.getFirstName(), "Pesho");
                        context.assertEquals(user.getSurname(), "Stupid");
                        context.assertEquals(user.getLastName(), "Peshov");
                        context.assertEquals(user.getVersion(), 1L);
                        context.assertNotNull(user.getId());
                        async.complete();
                    }).exceptionHandler( exception -> {
                        if(exception != null){
                            context.fail();
                        }
                    });
                })
                .write(fileContent.toString())
                .end();
    }

}
