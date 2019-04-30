package io.kettle.api;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

@Singleton
public class ApiResourcesService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private Router router;

	@Inject
	public ApiResourcesService(Router router) {
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
		BodyHandler bodyHandler = BodyHandler.create();
		for(String expr : pathExpr) {
			router.get(expr)
			.produces("application/json")
			.produces("application/yaml")
			.handler(ctx -> {
				requestHandler.handle(ctx);
			});
		router.post(expr)
			.produces("application/json")
			.produces("application/yaml")
			.consumes("application/json")
			.consumes("application/yaml")
			.handler(bodyHandler::handle).handler(ctx -> {
				requestHandler.handle(ctx);
			});
		router.delete(expr)
			.produces("application/json")
			.produces("application/yaml")
			.handler(ctx -> {
				requestHandler.handle(ctx);
			});
		}

	}
	
}
