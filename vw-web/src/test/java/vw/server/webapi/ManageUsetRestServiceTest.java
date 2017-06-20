package vw.server.webapi;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

/**
 * This is our JUnit test for our verticle.
 * The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class ManageUsetRestServiceTest {

    private static final String LOCALHOST = "localhost";

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

}
