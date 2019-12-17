package io.kettle.api;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class K8sCompatibilityTest {

    static Logger log = LoggerFactory.getLogger(ApiServerSmokeTest.class);

    int port = 7658;
    String host = "localhost";

    private static MongodExecutable MONGO;

    private static KubernetesClient client;

    @BeforeAll
    static void startMongoDatabase() throws IOException {
        Version.Main version = Version.Main.V4_0;
        int port = 27017;
        log.info("Starting Mongo {} on port {}", version, port);
        IMongodConfig config = new MongodConfigBuilder()
                .version(version)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        MONGO = MongodStarter.getDefaultInstance().prepare(config);
        MONGO.start();


        client = new DefaultKubernetesClient("http://localhost:7658");
    }

    @AfterAll
    static void stopMongoDatabase() {
        if (MONGO != null) {
            MONGO.stop();
        }
    }

    @Test
    void testNamespacesCompatibility() {
        client.namespaces().createNew().withNewMetadata().withName("dummy").endMetadata().done();
        Namespace dummyNamespace = client.namespaces().withName("dummy").get();
        assertNotNull(dummyNamespace);
        assertTrue(dummyNamespace.getMetadata().getName().equals("dummy"));
        client.namespaces().createNew().withNewMetadata().withName("foo").addToLabels("test", "true").endMetadata().done();
        Namespace fooNamespace = client.namespaces().withName("foo").get();
        assertTrue(fooNamespace.getMetadata().getName().equals("foo"));
        assertNotNull(fooNamespace.getMetadata().getLabels());
        assertTrue(fooNamespace.getMetadata().getLabels().get("test").equals("true"));
        assertTrue(client.namespaces().list().getItems().size()==2);
        assertTrue(client.namespaces().withName("dummy").delete());
        assertTrue(client.namespaces().list().getItems().size()==1);
        assertTrue(client.namespaces().withName("foo").delete());
        assertTrue(client.namespaces().list().getItems().isEmpty());
    }

//    @Test
//    void testCustomResourceDefinition() {
//        client.customResourceDefinitions().create(new CustomResourceDefinitionBuilder()
//                .withNewMetadata()
//                .withName("book-definition")
//                .endMetadata()
//                .withNewSpec()
//                .withGroup(""));
//    }

}
