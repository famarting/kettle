package io.kettle.ctl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.UUID;

import javax.inject.Inject;

import org.jboss.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.kettle.core.resource.Resource;
import io.kettle.core.storage.ResourcesRepository;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

@QuarkusTest
@QuarkusTestResource(MongoTestResource.class)
public class KettleCtlSmokeTest {

    private static final Logger log = Logger.getLogger(KettleCtlSmokeTest.class);

    @Inject
    ResourcesRepository repo;

    @Test
    void testKettle() throws Exception{
        createNamespace("test-namespace-1");

        testKettleCtl("test-namespace-1");

        deleteNamespace("test-namespace-1");
        JsonArray namespaces = listNamespaces();
        assertEquals(1, namespaces.size());
    }

    @Test
    void testContextNoExists() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("config", "use-context", "dummy");
        assertTrue(out.size()>0, "Unexpected, no error");
    }

    @Test
    void testUseContext() throws Exception {

        Path tempconfig = Files.createTempFile("kettleconfig", ".yaml");
        Files.copy(Paths.get("src/test/resources/kettle-config.yaml"), tempconfig, StandardCopyOption.REPLACE_EXISTING);

        String kettleconfigArg = "--kettleconfig="+tempconfig.toString();

        success("config", kettleconfigArg, "set-cluster", "my-cluster-1");

        success("config", kettleconfigArg, "set-context", "context-cluster-1", "--namespace=default", "--cluster=my-cluster-1");

        success("config", kettleconfigArg, "use-context", "context-cluster-1");

        testKettleCtl("default");

    }

    void success(String... args) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        boolean res = new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle(args);
        assertTrue(out.size()==0, "Unexpected: "+out.toString());
        assertTrue(res);
    }

    void testKettleCtl(String namespaceName) throws Exception {

        int minDefintions = 0;

        JsonArray definitions = listDefinitions();
        log.info(definitions.encodePrettily());
        assertEquals(minDefintions, definitions.size());

        createBookDefinition();

        definitions = listDefinitions();
        assertEquals(minDefintions+1, definitions.size());

        String book1Name = "book1";
        JsonObject created = createBookResource(namespaceName, book1Name);

        JsonObject found = getBookResource(namespaceName, book1Name);

        compareResources(created, found);
        assertEquals(namespaceName, found.getJsonObject("metadata").getString("namespace"));

        String book2Name = "book2";
        JsonObject created2 = createBookResource(namespaceName, book2Name);

        JsonArray list = listBooksResources(namespaceName);

        assertEquals(2, list.size());

        deleteBookResource(namespaceName, book1Name);

        list = listBooksResources(namespaceName);

        assertEquals(1, list.size());
        compareResources(list.getJsonObject(0), created2);

        deleteBookResource(namespaceName, book2Name);

        list = listBooksResources(namespaceName);

        assertEquals(0, list.size());

        deleteBookDefinition();

        definitions = listDefinitions();
        assertEquals(minDefintions, definitions.size());

    }

    private void deleteBookResource(String namespaceName, String book1Name) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("delete", "book", book1Name, "-n", namespaceName);
        assertTrue(out.size() <= 0, "Error: " + out.toString());
    }

    private JsonArray listBooksResources(String namespaceName) throws Exception {
        return list("get", "books", "-n", namespaceName, "-o", "json");
    }

    private JsonObject getBookResource(String namespaceName, String book1Name) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, new PrintStream(out), new PrintStream(err), System.in).handle("get", "books", book1Name, "-n", namespaceName, "-o", "json");
        if (err.size() > 0) {
            Assertions.fail(err.toString());
        }
        return new JsonObject(out.toString());
    }

    private JsonObject createBookResource(String namespaceName, String book1Name) throws Exception {
        JsonObject book1 = createResource("library.io/v1alpha1", "Book", book1Name);
        book1.put("spec", new JsonObject(Collections.singletonMap("text", UUID.randomUUID().toString())));

        Path path = File.createTempFile("book", ".json").toPath();
        Files.write(path, book1.encode().getBytes());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("apply", "-f", path.toString(), "-n", namespaceName);
        assertTrue(out.size() <= 0, "Error: " + out.toString());

        return book1;
    }

    private JsonArray listNamespaces() throws Exception {
        return list("get", "namespaces", "-o", "json");
    }

    private void deleteNamespace(String namespaceName) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("delete", "namespace", namespaceName);
        assertTrue(out.size() <= 0, "Error: " + out.toString());
    }

    private void createNamespace(String namespaceName) throws Exception {
        JsonObject testNamespace = createResource("v1", "Namespace", namespaceName);

        Path path = File.createTempFile("namespace", ".json").toPath();
        Files.write(path, testNamespace.encode().getBytes());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("apply", "-f", path.toString());
        assertTrue(out.size() <= 0, "Error: " + out.toString());
    }

    private JsonArray listDefinitions() throws Exception {
        return list("get", "resourcesdefinitions", "-o", "json");
    }

    private JsonArray list(String... args) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, new PrintStream(out), System.err, System.in).handle(args);
        JsonObject list = new JsonObject(out.toString());
        return list.getJsonArray("items");
    }

    private void deleteBookDefinition() throws Exception {
        String bookDefinitionName = "book-definition";
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("delete", "resourcesdefinitions", bookDefinitionName);
        assertTrue(out.size() <= 0, "Error: " + out.toString());
    }

    private void createBookDefinition() throws Exception {
        String bookDefinitionName = "book-definition";
        JsonObject bookDefinition = createResource("core/v1beta1", "ResourceDefinition", bookDefinitionName);
        JsonObject spec = new JsonObject();
        spec.put("group", "library.io");
        spec.put("version", "v1alpha1");
        spec.put("scope", "Namespaced");
        JsonObject names = new JsonObject();
        names.put("kind", "Book");
        names.put("listKind", "Books");
        names.put("plural", "books");
        names.put("singular", "book");
        spec.put("names", names);
        bookDefinition.put("spec", spec);

        Path path = File.createTempFile("bookdefinition", ".json").toPath();
        Files.write(path, bookDefinition.encode().getBytes());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new KettleRequestHandler(repo, System.out, new PrintStream(out), System.in).handle("apply", "-f", path.toString());
        assertTrue(out.size() <= 0, "Error: " + out.toString());
    }

    private JsonObject createResource(String apiVersion, String kind, String name) {
        JsonObject resource = new JsonObject();
        resource.put("apiVersion", apiVersion);
        resource.put("kind", kind);
        resource.put("metadata", new JsonObject(Collections.singletonMap("name", name)));
        return resource;
    }

    private void compareResources(JsonObject one, JsonObject two) {
        var a = one.mapTo(Resource.class);
        var b = two.mapTo(Resource.class);
        assertEquals(a.getApiVersion(), b.getApiVersion());
        assertEquals(a.getKind(), b.getKind());
        assertEquals(a.getMetadata().getName(), b.getMetadata().getName());
        assertEquals(a.getSpec(), b.getSpec());
    }

}