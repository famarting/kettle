package io.kettle.api;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.k8s.ApiResource;
import io.kettle.api.resource.extension.DefinitionResourceKey;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@Singleton
public class ApiResourcesService {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Router router;

    private Map<DefinitionResourceKey, List<Route>> routesByResource;
    private Map<String, List<ApiResource>> resourcesByGroupVersion;
    private Map<String, List<ApiResource>> resourcesByApiServiceVersion;

    private ApiGroupsService apiGroupsService;

    @Inject
    public ApiResourcesService(ApiGroupsService apiGroupsService) {
        this.apiGroupsService = apiGroupsService;
        this.routesByResource = new ConcurrentHashMap<>();
        this.resourcesByGroupVersion = new ConcurrentHashMap<>();
        this.resourcesByApiServiceVersion = new ConcurrentHashMap<>();
    }

    public void init(@Observes Router router) {
        this.router = router;
        // this.router.route("/*").order(Integer.MIN_VALUE).handler(ctx -> {
        // log.info("Request logging {} {}", ctx.request().method(), ctx.request().path());
        // ctx.next();
        // });
        // this.router.route("/*").order(Integer.MAX_VALUE).handler(ctx -> {
        // ctx.response().setStatusCode(200).end();;
        // });
    }

    public void registerCoreRoute(Consumer<Route> routeSetter) {
        routeSetter.accept(router.route());
    }

    public void registerApiServiceRoute(DefinitionResourceSpec resource, RequestHandlerFactory requestHandlerFactory) {

        registerApiServiceResource(resource);

        registerApiServiceRoute(resource);

        String[] pathExpr = new String[] {
                        String.format("/api/%s/%s", resource.getVersion(), resource.getNames().getPlural()),
                        String.format("/api/%s/%s/*", resource.getVersion(), resource.getNames().getPlural())
        };
        log.info("registering api service route " + pathExpr[0]);
        internalRegisterApiRoute(resource, requestHandlerFactory, pathExpr);
    }

    public void registerApiGroupRoute(DefinitionResourceSpec resource, RequestHandlerFactory requestHandlerFactory) {

        // TODO validate resource

        registerApiGroupResource(resource);

        registerApiGroupRoute(resource);

        String[] pathExpr;
        if ( resource.getScope() == ResourceScope.Global ) {
            pathExpr = new String[] {String.format("/apis/%s/%s/%s/*", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural()),
                            String.format("/apis/%s/%s/%s", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural())};
        } else {
            pathExpr = new String[] {String.format("/apis/%s/%s/namespaces/:namespace/%s/*", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural()),
                            String.format("/apis/%s/%s/namespaces/:namespace/%s", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural())};
        }
        log.info("registering api group route " + pathExpr[0]);
        internalRegisterApiRoute(resource, requestHandlerFactory, pathExpr);
    }

    private void internalRegisterApiRoute(DefinitionResourceSpec resource, RequestHandlerFactory requestHandlerFactory, String... pathExpr) {
        RequestHandler requestHandler = requestHandlerFactory.createRequestHandler();
        routesByResource.put(new DefinitionResourceKey(resource),
                Stream.of(pathExpr)
                        .flatMap(expr -> Stream.of(
                                router.get(expr)
                                        .produces("application/json")
                                        .produces("application/json;as=Table;v=v1beta1;g=meta.k8s.io")
                                        .produces("application/yaml")
                                        .handler(ctx -> {
                                            requestHandler.handle(ctx);
                                        }),
                                router.post(expr)
                                        .produces("application/json")
                                        .produces("application/yaml")
                                        .consumes("application/json")
                                        .consumes("application/yaml")
                                        .consumes("*")
                                        .handler(BodyHandler.create())
                                        .handler(ctx -> {
                                            requestHandler.handle(ctx);
                                        }),
                                router.put(expr)
                                        .produces("application/json")
                                        .produces("application/yaml")
                                        .consumes("application/json")
                                        .consumes("application/yaml")
                                        .handler(BodyHandler.create())
                                        .handler(ctx -> {
                                            requestHandler.handle(ctx);
                                        }),
                                router.patch(expr)
                                        .produces("application/json")
                                        .consumes("application/merge-patch+json")
                                        .handler(BodyHandler.create())
                                        .handler(ctx -> {
                                            requestHandler.handle(ctx);
                                        }),
                                router.delete(expr)
                                        .produces("application/json")
                                        .produces("application/yaml")
                                        .handler(ctx -> {
                                            requestHandler.handle(ctx);
                                        })))
                        .collect(Collectors.toList()));
    }

    public void removeResourceRoute(DefinitionResourceSpec resource) {
        Optional.ofNullable(routesByResource.remove(new DefinitionResourceKey(resource)))
                .ifPresent(routes -> routes.forEach(Route::remove));
    }

    private void registerApiGroupResource(DefinitionResourceSpec resource) {
        registerApiResource(resource, resourcesByGroupVersion.computeIfAbsent(resource.getGroup() + "/" + resource.getVersion(), k -> new CopyOnWriteArrayList<>()));
    }

    private void registerApiServiceResource(DefinitionResourceSpec resource) {
        registerApiResource(resource, resourcesByApiServiceVersion.computeIfAbsent(resource.getVersion(), k -> new CopyOnWriteArrayList<>()));
    }

    private void registerApiResource(DefinitionResourceSpec resource, List<ApiResource> resources) {
        ApiResource apiResource = new ApiResource();
        apiResource.setKind(resource.getNames().getKind());
        apiResource.setName(resource.getNames().getPlural());
        apiResource.setNamespaced(resource.getScope() == ResourceScope.Namespaced);
        apiResource.setSingularName(resource.getNames().getSingular());
        // TODO short names
        apiResource.setShortNames(resource.getShortNames());
        // TODO implement verbs properly
        apiResource.setVerbs(Arrays.asList("delete",
                "get",
                "list",
                "create",
                "update",
                "patch"));
        resources.add(apiResource);
    }

    private void registerApiGroupRoute(DefinitionResourceSpec resource) {
        apiGroupsService.addApiGroup(resource.getGroup(), resource.getVersion());

        String groupVersion = resource.getGroup() + "/" + resource.getVersion();
        router.get("/apis/" + groupVersion)
                .produces("application/json")
                .handler(ctx -> {
                    JsonObject result = new JsonObject();
                    result.put("apiVersion", resource.getVersion());
                    result.put("groupVersion", groupVersion);
                    result.put("kind", "APIResourceList");
                    result.put("resources", resourcesByGroupVersion.get(groupVersion));
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(result.encode());
                });
    }

    private void registerApiServiceRoute(DefinitionResourceSpec resource) {
        router.get("/api/" + resource.getVersion())
                .produces("application/json")
                .handler(ctx -> {
                    JsonObject result = new JsonObject();
                    result.put("groupVersion", resource.getVersion());
                    result.put("kind", "APIResourceList");
                    result.put("resources", resourcesByApiServiceVersion.get(resource.getVersion()));
                    ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "application/json").end(result.encode());
                });
    }

}
