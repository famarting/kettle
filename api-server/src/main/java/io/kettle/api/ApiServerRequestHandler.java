package io.kettle.api;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator.Feature;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.ResourceMetadata;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;
import io.kettle.api.storage.DefinitionResourceRepository;
import io.kettle.api.storage.ResourcesRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class ApiServerRequestHandler implements RequestHandler{

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	DefinitionResourceRepository definitionsRepository;
	ResourcesRepository resourcesRepository;
	
	protected ObjectMapper yamlMapper = new YAMLMapper(new YAMLFactory()
														.enable(Feature.MINIMIZE_QUOTES))
											.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
											.enable(SerializationFeature.INDENT_OUTPUT);
	
	protected ObjectMapper jsonMapper = new ObjectMapper()
											.setSerializationInclusion(Include.NON_EMPTY)
											.enable(SerializationFeature.INDENT_OUTPUT);
	
	public ApiServerRequestHandler(DefinitionResourceRepository definitionsRepository, ResourcesRepository resourcesRepository) {
		this.definitionsRepository = definitionsRepository;
		this.resourcesRepository = resourcesRepository;
	}
	
	public void handle (RoutingContext ctx) {

        log.info("Request {} {}", ctx.request().method(), ctx.request().path());
		try {
			ApiServerRequestContext requestContext = validateRequest(ctx);
			switch (ctx.request().method()) {
			case GET:
				handleGet(requestContext);
				break;
			case POST:
				handlePost(requestContext);
				break;
			case DELETE:
				handleDelete(requestContext);
				break;
			default:
				break;
			}
		} catch (RequestValidationException e) {
			log.info("Error validating request ", e.getMessage());
			ctx.response()
				.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
				.end(e.getMessage());
		} catch (Exception e) {
			log.error("Unknow error ", e);
			ctx.response()
				.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
				.end(e.getMessage());
		} finally {
			log.info("Request processed {} {}", ctx.request().method(), ctx.request().path());			
		}
	}

	/**
	  Cluster-scoped resources:
		GET /apis/GROUP/VERSION/RESOURCETYPE - return the collection of resources of the resource type
		GET /apis/GROUP/VERSION/RESOURCETYPE/NAME - return the resource with NAME under the resource type
	  Namespace-scoped resources:
		GET /apis/GROUP/VERSION/RESOURCETYPE - return the collection of all instances of the resource type across all namespaces
		GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE - return collection of all instances of the resource type in NAMESPACE
		GET /apis/GROUP/VERSION/namespaces/NAMESPACE/RESOURCETYPE/NAME - return the instance of the resource type with NAME in NAMESPACE
	 */
	
	protected ApiServerRequestContext validateRequest(RoutingContext ctx) throws RequestValidationException {
		String path = ctx.request().path();
		
		String[] pathItems = Stream.of(path.split("/"))
				.filter(Predicate.not(String::isBlank))
				.toArray(String[]::new);
		
		String group = pathItems[1];
		String version = pathItems[2];
		String pluralName = pathItems[3];

		ResourceType type = new ResourceType(ResourceScope.Global);
		Optional<String> name = Optional.empty();
		if(pluralName.equals("namespaces") && pathItems.length >= 6) {
			String namespace = pathItems[4];
			type = new ResourceType(namespace);
			pluralName = pathItems[5];
			if(pathItems.length == 7) {
				name = Optional.of(pathItems[6]);				
			}
		} else if(pathItems.length == 5) {
			name = Optional.of(pathItems[4]);
		}
		
		DefinitionResourceSpec definition = definitionsRepository.getDefinition(group, version, pluralName);
		
		if(definition == null) {
			throw new RequestValidationException("Resource type not found");
		}

		return new ApiServerRequestContext(definition, ctx, type, name);
	}

	protected void handleDelete(ApiServerRequestContext requestContext) {
		// TODO Auto-generated method stub
		
	}

	protected void handlePost(ApiServerRequestContext requestContext) throws JsonParseException, JsonMappingException, IOException, RequestValidationException {

		if(requestContext.resourceName().isPresent()) {
			Buffer body = requestContext.httpContext().getBody();
			Resource resource = getRequestObjectMapper(requestContext).readValue(body.getBytes(), Resource.class);
			if(!formatApiVersion(requestContext.definition().getGroup(), requestContext.definition().getVersion()).equals(resource.getApiVersion())) {
				throw new RequestValidationException("apiVersion doesn't match");
			}
			if(!requestContext.definition().getNames().getKind().equals(resource.getKind())) {
				throw new RequestValidationException("Kind doesn't match");
			}
			if(resource.getMetadata() == null) {
				resource.setMetadata(new ResourceMetadata());
			}
			if(resource.getMetadata().getName() == null || resource.getMetadata().getName().isBlank()) {
				resource.getMetadata().setName(requestContext.resourceName().get());
			}else if(!resource.getMetadata().getName().equals(requestContext.resourceName().get())) {
				throw new RequestValidationException("Name doesn't match");
			}
			if(requestContext.resourceType().scope() == ResourceScope.Namespaced) {
				if(resource.getMetadata().getNamespace() == null || resource.getMetadata().getNamespace().isBlank()) {
					resource.getMetadata().setNamespace(requestContext.resourceType().namespace());
				}else if(resource.getMetadata().getNamespace() != null && !resource.getMetadata().getNamespace().equals(requestContext.resourceType().namespace())) {
					throw new RequestValidationException("Namespace doesn't match");
				}
			}else if(resource.getMetadata().getNamespace() != null){
				throw new RequestValidationException("Namespace is not used in global resources");
			}
			
			resource.getMetadata().setSelfLink(requestContext.httpContext().currentRoute().getPath());
			resource.getMetadata().setUid(UUID.randomUUID().toString());
			resource.getMetadata().setCreationTimestamp(Instant.now().toString());
			
			create(requestContext, resource);

			sendResponse(requestContext, resource);
			
		}else {
			throw new RequestValidationException("Resource name in path is mandatory for resource creation");
		}
		
	}

	protected void create(ApiServerRequestContext requestContext, Resource resource) {
		resourcesRepository.createResource(requestContext.resourceType(), resource);
	}

	private void sendResponse(ApiServerRequestContext requestContext, Object resource) throws JsonProcessingException {
		String responseContentType = Optional.ofNullable(requestContext.httpContext().request().getHeader("Accept"))
				.orElseGet(()->requestContext.httpContext().parsedHeaders().contentType().value());
		
		String responseBody;
		if(responseContentType.equals("application/yaml")) {
			responseBody = yamlMapper.writeValueAsString(resource);
		}else {
			responseBody = jsonMapper.writeValueAsString(resource);
		}
		
		requestContext.httpContext().response()
			.putHeader(HttpHeaders.CONTENT_TYPE, responseContentType)
			.end(responseBody);
	}

	protected void handleGet(ApiServerRequestContext requestContext) throws JsonProcessingException {
		DefinitionResourceSpec definition = requestContext.definition();
		if(requestContext.resourceName().isPresent()) {
			Resource resource = resourcesRepository.getResource(new ResourceKey(formatApiVersion(definition.getGroup(), definition.getVersion()), definition.getNames().getKind(), requestContext.resourceType(), requestContext.resourceName().get()));
			if(resource == null) {
				requestContext.httpContext().response()
					.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
					.end();
			}else {
				sendResponse(requestContext, resource);
			}
		}else {
			List<Resource> resources = list(requestContext, definition);
			sendResponse(requestContext, resources);
		}
	}

	private List<Resource> list(ApiServerRequestContext requestContext, DefinitionResourceSpec definition) {
		if (requestContext.resourceType().scope() == ResourceScope.Global) {
			return resourcesRepository.doGlobalQuery(formatApiVersion(definition.getGroup(), definition.getVersion()), definition.getNames().getKind());
		} else {
			return resourcesRepository.doNamespacedQuery(formatApiVersion(definition.getGroup(), definition.getVersion()), definition.getNames().getKind(), requestContext.resourceType().namespace());
		}
	}
	
	private String formatApiVersion(String group, String version) {
		return String.format("%s/%s", group, version);
	}
	
	protected ObjectMapper getRequestObjectMapper(ApiServerRequestContext requestContext) {
		if(requestContext.httpContext().parsedHeaders().contentType().value().equals("application/yaml")) {
			return yamlMapper;
		}else {
			return jsonMapper;
		}
	}
	
	
}
