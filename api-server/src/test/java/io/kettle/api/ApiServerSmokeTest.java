package io.kettle.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

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
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.AsyncResult;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.ext.web.codec.BodyCodec;

@QuarkusTest
public class ApiServerSmokeTest {

	static Logger log = LoggerFactory.getLogger(ApiServerSmokeTest.class);
	
	int port = 7658;
	String host = "localhost";

	Vertx vertx = Vertx.vertx();
	WebClient client = WebClient.create(vertx, new WebClientOptions());

    private static MongodExecutable MONGO;

    @BeforeAll
    public static void startMongoDatabase() throws IOException {
        Version.Main version = Version.Main.V4_0;
        int port = 27017;
        log.info("Starting Mongo {} on port {}", version, port);
        IMongodConfig config = new MongodConfigBuilder()
                .version(version)
                .net(new Net(port, Network.localhostIsIPv6()))
                .build();
        MONGO = MongodStarter.getDefaultInstance().prepare(config);
        MONGO.start();
    }

    @AfterAll
    public static void stopMongoDatabase() {
        if (MONGO != null) {
            MONGO.stop();
        }
    }

	@Test
	void testApiServer() throws InterruptedException, ExecutionException, TimeoutException {

		int minDefintions = 0;
		
		JsonArray definitions = listDefinitions();
		log.info(definitions.encodePrettily());
		assertEquals(minDefintions, definitions.size());
		
		createBookDefinition();
		
		definitions = listDefinitions();
		assertEquals(minDefintions+1, definitions.size());
		
		String namespaceName = "test-namespace";
		createNamespace(namespaceName);
		
		String book1Name = "book1";
		JsonObject created = createBookResource(namespaceName, book1Name);
		
		JsonObject found = getBookResource(namespaceName, book1Name);
		
		assertEquals(created, found);
		
		String book2Name = "book2";
		JsonObject created2 = createBookResource(namespaceName, book2Name);
		
		JsonArray list = listBooksResources(namespaceName);
		
		assertEquals(2, list.size());
		
		JsonObject deleted = deleteBookResource(namespaceName, book1Name);
		
		assertEquals(created, deleted);
		
		list = listBooksResources(namespaceName);
		
		assertEquals(1, list.size());
		
		JsonObject deleted2 = deleteBookResource(namespaceName, book2Name);
		assertEquals(created2, deleted2);

		list = listBooksResources(namespaceName);

		assertEquals(0, list.size());
		
		deleteBookDefinition();
		
		definitions = listDefinitions();
		assertEquals(minDefintions, definitions.size());
		
		deleteNamespace(namespaceName);
		
		JsonArray namespaces = listNamespaces();
		
		assertEquals(0, namespaces.size());
	}

	private JsonObject deleteBookResource(String namespaceName, String book1Name)
			throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonObject> deleteBookFuture = new CompletableFuture<>();
		client.delete(port, host, "/apis/library.io/v1alpha1/namespaces/"+namespaceName+"/books/"+book1Name)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, deleteBookFuture, this::httpOk, "200", "Error deleting resource", true));
		JsonObject deleted = deleteBookFuture.get(10, TimeUnit.SECONDS);
		return deleted;
	}

	private JsonArray listBooksResources(String namespaceName)
			throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonArray> getBooksFuture = new CompletableFuture<>();
		client.get(port, host, "/apis/library.io/v1alpha1/namespaces/"+namespaceName+"/books")
			.as(BodyCodec.jsonArray())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, getBooksFuture, this::httpOk, "200", "Error listing resources", true));
		JsonArray list = getBooksFuture.get(10, TimeUnit.SECONDS);
		return list;
	}

	private JsonObject getBookResource(String namespaceName, String book1Name)
			throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonObject> getBookFuture = new CompletableFuture<>();
		client.get(port, host, "/apis/library.io/v1alpha1/namespaces/"+namespaceName+"/books/"+book1Name)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, getBookFuture, this::httpOk, "200", "Error reading resource", true));
		JsonObject found = getBookFuture.get(10, TimeUnit.SECONDS);
		return found;
	}

	private JsonObject createBookResource(String namespaceName, String book1Name)
			throws InterruptedException, ExecutionException, TimeoutException {
		JsonObject book1 = createResource("library.io/v1alpha1", "Book", book1Name);
		book1.put("spec", new JsonObject(Collections.singletonMap("text", UUID.randomUUID().toString())));
		CompletableFuture<JsonObject> book1Future = new CompletableFuture<>();
		client.post(port, host, "/apis/library.io/v1alpha1/namespaces/"+namespaceName+"/books/"+book1Name)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
			.sendJsonObject(book1, ar -> responseHandler(ar, book1Future, this::httpOk, "200", "Error creating resource", true));
		return book1Future.get(10, TimeUnit.SECONDS);
	}

	private JsonArray listNamespaces() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonArray> getBooksFuture = new CompletableFuture<>();
		client.get(port, host, "/apis/core/v1beta1/namespaces/")
			.as(BodyCodec.jsonArray())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, getBooksFuture, this::httpOk, "200", "Error listing resources", true));
		JsonArray list = getBooksFuture.get(10, TimeUnit.SECONDS);
		return list;
	}
	
	private void deleteNamespace(String namespaceName) throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonObject> deleteBookFuture = new CompletableFuture<>();
		client.delete(port, host, "/apis/core/v1beta1/namespaces/"+namespaceName)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, deleteBookFuture, this::httpOk, "200", "Error deleting resource", true));
		deleteBookFuture.get(10, TimeUnit.SECONDS);
	}
	
	private void createNamespace(String namespaceName)
			throws InterruptedException, ExecutionException, TimeoutException {
		JsonObject testNamespace = createResource("core/v1beta1", "Namespace", namespaceName);
		
		CompletableFuture<JsonObject> namespaceFuture = new CompletableFuture<>();
		client.post(port, host, "/apis/core/v1beta1/namespaces/"+namespaceName)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
			.sendJsonObject(testNamespace, ar -> responseHandler(ar, namespaceFuture, this::httpOk, "200", "Error creating test-namespace", true));
		namespaceFuture.get(10, TimeUnit.SECONDS);
	}

	private JsonArray listDefinitions() throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<JsonArray> getBooksFuture = new CompletableFuture<>();
		client.get(port, host, "/apis/core/v1beta1/resourcesdefinitions/")
			.as(BodyCodec.jsonArray())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, getBooksFuture, this::httpOk, "200", "Error listing resources", true));
		JsonArray list = getBooksFuture.get(10, TimeUnit.SECONDS);
		return list;
	}
	
	private void deleteBookDefinition() throws InterruptedException, ExecutionException, TimeoutException {
		String bookDefinitionName = "book-definition";
		CompletableFuture<JsonObject> deleteBookFuture = new CompletableFuture<>();
		client.delete(port, host, "/apis/core/v1beta1/resourcesdefinitions/"+bookDefinitionName)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.ACCEPT.toString(), "application/json")
			.send(ar -> responseHandler(ar, deleteBookFuture, this::httpOk, "200", "Error deleting resource", true));
		deleteBookFuture.get(10, TimeUnit.SECONDS);
	}
	
	private void createBookDefinition() throws InterruptedException, ExecutionException, TimeoutException {
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
		
		CompletableFuture<JsonObject> definitionFuture = new CompletableFuture<>();
		client.post(port, host, "/apis/core/v1beta1/resourcesdefinitions/"+bookDefinitionName)
			.as(BodyCodec.jsonObject())
			.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")
			.sendJsonObject(bookDefinition, ar -> responseHandler(ar, definitionFuture, this::httpOk, "200", "Error creating definition", true));
		definitionFuture.get(10, TimeUnit.SECONDS);
	}

	private JsonObject createResource(String apiVersion, String kind, String name) {
		JsonObject resource = new JsonObject();
		resource.put("apiVersion", apiVersion);
		resource.put("kind", kind);
		resource.put("metadata", new JsonObject(Collections.singletonMap("name", name)));
		return resource;
	}
	
	private boolean httpOk(int status) {
		return status == HttpURLConnection.HTTP_OK;
	}
	
	private <T> void responseHandler(AsyncResult<HttpResponse<T>> ar, CompletableFuture<T> promise, Predicate<Integer> expectedCodePredicate, 
			String expectedCodeOrCodes, String warnMessage, boolean throwException) {
		try {
			if (ar.succeeded()) {
				HttpResponse<T> response = ar.result();
				T body = response.body();
				if (expectedCodePredicate.negate().test(response.statusCode())) {
					log.error("expected-code: {}, response-code: {}, body: {}, op: {}", expectedCodeOrCodes,
							response.statusCode(), response.body(), warnMessage);
					promise.completeExceptionally(new RuntimeException(
							"Status " + response.statusCode() + " body: " + (body != null ? body.toString() : null)));
				} else if (response.statusCode() < HttpURLConnection.HTTP_OK
						|| response.statusCode() >= HttpURLConnection.HTTP_MULT_CHOICE) {
					if (throwException) {
						promise.completeExceptionally(new RuntimeException(body == null ? "null" : body.toString()));
					} else {
						promise.complete(ar.result().body());
					}
				} else {
					promise.complete(ar.result().body());
				}
			} else {
				log.warn(warnMessage);
				promise.completeExceptionally(ar.cause());
			}
		} catch (io.vertx.core.json.DecodeException decEx) {
			if (ar.result().bodyAsString().toLowerCase().contains("application is not available")) {
				log.warn("is not available.", ar.cause());
				throw new IllegalStateException("is not available.");
			} else {
				log.warn("Unexpected object received", ar.cause());
				throw new IllegalStateException(
						"JsonObject expected, but following object was received: " + ar.result().bodyAsString());
			}
		}
	}

}
