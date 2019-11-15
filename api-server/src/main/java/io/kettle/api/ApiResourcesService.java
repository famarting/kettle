package io.kettle.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.event.Observes;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.extension.DefinitionResourceKey;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@Singleton
public class ApiResourcesService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Router router;

	private Map<DefinitionResourceKey, List<Route>> routesByResource;
	
	//FIXME injecting router is buggy
//	@Inject
//	public ApiResourcesService(Router router) {
	public ApiResourcesService() {
//		this.router = router;
		this.routesByResource = new ConcurrentHashMap<>();
	}

     public void init(@Observes Router router) {
     	this.router = router;
     }
	
	public void registerResourceRoute(DefinitionResourceSpec resource, RequestHandlerFactory requestHandlerFactory) {
		
		//TODO validate resource
		
		String[] pathExpr;
			
		if(resource.getScope() == ResourceScope.Global) {
			pathExpr = new String[] { String.format("/apis/%s/%s/%s/*", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural()), 
					String.format("/apis/%s/%s/%s", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural()) };
		}else {
			pathExpr = new String[] { String.format("/apis/%s/%s/namespaces/:namespace/%s/*", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural()), 
					String.format("/apis/%s/%s/namespaces/:namespace/%s", resource.getGroup(), resource.getVersion(), resource.getNames().getPlural())};
		}
		
		log.info("registering route "+pathExpr[0]);
		
		RequestHandler requestHandler = requestHandlerFactory.createRequestHandler();
		routesByResource.put(new DefinitionResourceKey(resource),
			Stream.of(pathExpr)
				.flatMap(expr->
					Stream.of(
							router.get(expr)
							.produces("application/json")
							.produces("application/yaml")
							.handler(ctx -> {
								requestHandler.handle(ctx);
							}),
						router.post(expr)
							.produces("application/json")
							.produces("application/yaml")
							.consumes("application/json")
							.consumes("application/yaml")
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
	
}
