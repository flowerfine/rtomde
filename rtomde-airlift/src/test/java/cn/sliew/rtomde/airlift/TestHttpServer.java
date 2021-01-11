package cn.sliew.rtomde.airlift;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.airlift.bootstrap.Bootstrap;
import io.airlift.bootstrap.LifeCycleManager;
import io.airlift.http.client.HttpClient;
import io.airlift.http.client.jetty.JettyHttpClient;
import io.airlift.http.server.HttpServerModule;
import io.airlift.http.server.testing.TestingHttpServer;
import io.airlift.jaxrs.JaxrsModule;
import io.airlift.json.JsonCodec;
import io.airlift.json.JsonModule;
import io.airlift.log.Logger;
import io.airlift.node.NodeModule;
import io.airlift.testing.Closeables;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static io.airlift.http.client.JsonResponseHandler.createJsonResponseHandler;
import static io.airlift.http.client.Request.Builder.prepareGet;
import static io.airlift.json.JsonCodec.listJsonCodec;
import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestHttpServer {

    private HttpClient client;
    private TestingHttpServer server;

    private final JsonCodec<List<Object>> listCodec = listJsonCodec(Object.class);
    private LifeCycleManager lifeCycleManager;

    @BeforeEach
    public void setup() {
        Bootstrap app = new Bootstrap(
                new HttpServerModule(),
//                new TestingHttpServerModule(),
                new NodeModule(),
//                new TestingNodeModule(),
                new JsonModule(),
                new JaxrsModule()
        );
        Injector injector = app.strictConfig().doNotInitializeLogging().initialize();

        lifeCycleManager = injector.getInstance(LifeCycleManager.class);
        server = injector.getInstance(TestingHttpServer.class);
        client = new JettyHttpClient();
    }

    @AfterEach
    public void teardown() {
        try {
            if (lifeCycleManager != null) {
                lifeCycleManager.stop();
            }
        } finally {
            Closeables.closeQuietly(client);
        }
    }

    @Test
    public void testEmpty() {
        List<Object> response = client.execute(
                prepareGet().setUri(uriFor("/v1/person")).build(),
                createJsonResponseHandler(listCodec));
        assertEquals(response, ImmutableList.of());
    }

    private URI uriFor(String path) {
        return server.getBaseUrl().resolve(path);
    }

    private static void logLocation(Logger log, String name, Path path) {
        if (!Files.exists(path, NOFOLLOW_LINKS)) {
            log.info("%s: [does not exist]", name);
            return;
        }
        try {
            path = path.toAbsolutePath().toRealPath();
        } catch (IOException e) {
            log.info("%s: [not accessible]", name);
            return;
        }
        log.info("%s: %s", name, path);
    }
}