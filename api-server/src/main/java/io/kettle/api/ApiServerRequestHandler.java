package io.kettle.api;


import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.k8s.table.PartialObjectMetadata;
import io.kettle.api.k8s.table.Table;
import io.kettle.api.k8s.table.TableColumnDefinition;
import io.kettle.api.k8s.table.TableRow;
import io.kettle.api.resource.ApiType;
import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.ResourceMetadata;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;
import io.kettle.api.storage.ResourcesRepository;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.RoutingContext;

public class ApiServerRequestHandler implements RequestHandler{

	private Logger log = LoggerFactory.getLogger(this.getClass());

	ResourcesRepository resourcesRepository;

	protected ObjectMapper yamlMapper = new YAMLMapper(new YAMLFactory()
														.enable(Feature.MINIMIZE_QUOTES))
											.enable(SerializationFeature.INDENT_OUTPUT);

	protected ObjectMapper jsonMapper = new ObjectMapper()
											.enable(SerializationFeature.INDENT_OUTPUT);

	public ApiServerRequestHandler(ResourcesRepository resourcesRepository) {
		this.resourcesRepository = resourcesRepository;
	}

	public void handle (RoutingContext ctx) {

        log.info("New request {} {}", ctx.request().method(), ctx.request().path());
		try {
			ApiServerRequestContext requestContext = validateRequest(ctx);
			switch (ctx.request().method()) {
			case GET:
				handleGet(requestContext);
				break;
			case POST:
				handlePost(requestContext);
				break;
			case PUT:
				handlePut(requestContext);
				break;
			case PATCH:
				handlePatch(requestContext);
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
		} catch (Throwable e) {
			log.error("Unknow error(throwable) ", e);
			ctx.response()
				.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code())
				.end(e.getMessage());
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
				.filter(s->s!=null && !s.trim().isEmpty())
				.toArray(String[]::new);

		ApiType apiType = ApiType.API_SERVICE;
		if (pathItems[0].equals("apis")) {
			apiType = ApiType.API_GROUP;
		}

		String group;
		String version;
		String pluralName;
		if (apiType == ApiType.API_SERVICE) {
			group = null;
			version = pathItems[1];
			pluralName = pathItems[2];
		} else {
			group = pathItems[1];
			version = pathItems[2];
			pluralName = pathItems[3];
		}

		ResourceType type = ResourceType.global();
		Optional<String> name = Optional.empty();
		if (apiType == ApiType.API_SERVICE) {
		    if (pathItems.length >= 4) {
		        name = Optional.of(pathItems[3]);
		    }
		} else {
		    if(pluralName.equals("namespaces") && pathItems.length >= 6) {
		        String namespace = pathItems[4];
	            type = ResourceType.namespaced(namespace);
	            pluralName = pathItems[5];
	            if(pathItems.length == 7) {
	                name = Optional.of(pathItems[6]);
	            }
	        } else if(pathItems.length == 5) {
	            name = Optional.of(pathItems[4]);
	        }
		}

		DefinitionResourceSpec definition = resourcesRepository.getDefinitionResource(pluralName);
		if(definition == null) {
			throw new RequestValidationException("Resource type not found");
		}
		if ( ( (apiType == ApiType.API_SERVICE && group == null && definition.getGroup() == null) || (apiType == ApiType.API_GROUP && group.equals(definition.getGroup())) )
		        && version.equals(definition.getVersion())
		        && type.scope() == definition.getScope()) {
			return new ApiServerRequestContext(definition.getGroup(), definition.getVersion(), definition.getNames().getKind(),
				ctx, type, name);
		} else {
			throw new RequestValidationException("Api not found");
		}
	}

	protected void handleDelete(ApiServerRequestContext requestContext) throws RequestValidationException, JsonProcessingException {
		if(requestContext.resourceName().isPresent()) {

			Resource deletedResource = delete(requestContext);

			sendResponse(requestContext, deletedResource);

		}else {
			throw new RequestValidationException("Resource name in path is mandatory for resource deletion");
		}
	}

	protected Resource delete(ApiServerRequestContext requestContext) {
		return resourcesRepository.deleteResource(
				resourceKey(requestContext));
	}

	protected void handlePut(ApiServerRequestContext requestContext) throws JsonParseException, JsonMappingException, IOException, RequestValidationException {
		if(requestContext.resourceName().isPresent()) {
			Resource resource = validateRequestBody(requestContext);

			Resource existingResource = resourcesRepository.getResource(new ResourceKey(resource.getApiVersion(), resource.getKind(), requestContext.resourceType(), resource.getMetadata().getName()));
			if(existingResource == null) {
				requestContext.httpContext().response()
					.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
					.end();
			}else {
				resource.getMetadata().setSelfLink(requestContext.httpContext().request().path());
				resource.getMetadata().setUid(existingResource.getMetadata().getUid());
				resource.getMetadata().setCreationTimestamp(existingResource.getMetadata().getCreationTimestamp());
				resource.setId(existingResource.getId());

				update(requestContext, resource);

				sendResponse(requestContext, resource);
			}

		}else {
			throw new RequestValidationException("Resource name in path is mandatory for resource creation");
		}
	}

	protected void handlePatch(ApiServerRequestContext requestContext) throws JsonParseException, JsonMappingException, IOException, RequestValidationException {
		if(requestContext.resourceName().isPresent()) {
			// Resource resource = validateRequestBody(requestContext);
			Buffer body = requestContext.httpContext().getBody();
			Resource resource = getRequestObjectMapper(requestContext).readValue(body.getBytes(), Resource.class);

			Resource existingResource = resourcesRepository.getResource(
				new ResourceKey(ApiServerUtils.formatApiVersion(requestContext.group(), requestContext.version()), requestContext.kind(), requestContext.resourceType(), requestContext.resourceName().get()));
			if(existingResource == null) {
				requestContext.httpContext().response()
					.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
					.end();
			}else {
				resource.setId(existingResource.getId());
				resource.setMetadata(existingResource.getMetadata());
				resource.setApiVersion(existingResource.getApiVersion());
				resource.setKind(existingResource.getKind());
				//TODO implement merge properly
				if (existingResource.getSpec()!=null) {
					resource.setSpec(Optional.ofNullable(resource.getSpec()).orElseGet(HashMap::new));
					Map<String, Object> specToAdd = existingResource.getSpec().entrySet().stream()
						.filter(e -> !resource.getSpec().containsKey(e.getKey()))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
					resource.getSpec().putAll(specToAdd);
				}
				if (existingResource.getStatus()!=null) {
					resource.setStatus(Optional.ofNullable(resource.getStatus()).orElseGet(HashMap::new));
					Map<String, Object> statusToAdd = existingResource.getStatus().entrySet().stream()
						.filter(e -> !resource.getStatus().containsKey(e.getKey()))
						.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
					resource.getStatus().putAll(statusToAdd);
				}
				update(requestContext, resource);

				sendResponse(requestContext, resource);
			}

		}else {
			throw new RequestValidationException("Resource name in path is mandatory for resource creation");
		}
	}

	protected void handlePost(ApiServerRequestContext requestContext) throws JsonParseException, JsonMappingException, IOException, RequestValidationException {
	    Resource resource = validateRequestBody(requestContext);
		Resource dbResult = resourcesRepository.getResource(resourceKey(requestContext));
		if (dbResult != null) {
			throw new RequestValidationException("Resource already exists");
		}
		resource.getMetadata().setSelfLink(requestContext.httpContext().request().path());
		resource.getMetadata().setUid(UUID.randomUUID().toString());
		resource.getMetadata().setCreationTimestamp(Instant.now().toString());
		create(requestContext, resource);
		sendResponse(requestContext, resource);
	}

	private Resource validateRequestBody(ApiServerRequestContext requestContext) throws IOException, JsonParseException, JsonMappingException, RequestValidationException {
		Buffer body = requestContext.httpContext().getBody();
		Resource resource = getRequestObjectMapper(requestContext).readValue(body.getBytes(), Resource.class);
		if(!ApiServerUtils.formatApiVersion(requestContext.group(), requestContext.version()).equals(resource.getApiVersion())) {
			throw new RequestValidationException("apiVersion doesn't match");
		}
		if(!requestContext.kind().equals(resource.getKind())) {
			throw new RequestValidationException("Kind doesn't match");
		}
		if(resource.getMetadata() == null) {
			resource.setMetadata(new ResourceMetadata());
		}
		if(resource.getMetadata().getName() == null || resource.getMetadata().getName().trim().isEmpty()) {
			throw new RequestValidationException("Resource name is missing");
		}else if(!requestContext.resourceName().isPresent()) {
			requestContext.setResourceName(resource.getMetadata().getName());
		}else if(!resource.getMetadata().getName().equals(requestContext.resourceName().get())) {
			throw new RequestValidationException("Name doesn't match");
		}
		if(requestContext.resourceType().scope() == ResourceScope.Namespaced) {
			if(resource.getMetadata().getNamespace() == null || resource.getMetadata().getNamespace().trim().isEmpty()) {
				resource.getMetadata().setNamespace(requestContext.resourceType().namespace());
			}else if(resource.getMetadata().getNamespace() != null && !resource.getMetadata().getNamespace().equals(requestContext.resourceType().namespace())) {
				throw new RequestValidationException("Namespace doesn't match");
			}
		}else if(resource.getMetadata().getNamespace() != null && !resource.getMetadata().getNamespace().isEmpty()){
			throw new RequestValidationException("Namespace is not used in global resources");
		}
		return resource;
	}

	protected void update(ApiServerRequestContext requestContext, Resource resource) {
		resourcesRepository.updateResource(requestContext.resourceType(), resource);
	}

	protected void create(ApiServerRequestContext requestContext, Resource resource) {
		resourcesRepository.createResource(requestContext.resourceType(), resource);
	}

	private void sendResponse(ApiServerRequestContext requestContext, Object resource) throws JsonProcessingException {
		if (Optional.ofNullable(requestContext.httpContext().request().getHeader(HttpHeaders.ACCEPT)).orElse("").contains("application/json;as=Table;v=v1beta1;g=meta.k8s.io")) {
			List<TableColumnDefinition> tableColumnDefinitions = Arrays.asList(
				new TableColumnDefinition("Name must be unique within a namespace.",
						"name",
						"Name",
						0,
						"string"),
				new TableColumnDefinition("Creation timestamp of the resource.", "", "Created At", 0, "date"));

			List<Resource> items;
			if (resource instanceof Map) {
				items = (List<Resource>) ((Map<String,Object>)resource).get("items");
			} else {
				items = Arrays.asList(((Resource) resource));
			}
			List<TableRow> rows = items.stream()
				.map(item -> {
					return new TableRow(Arrays.asList(item.getMetadata().getName(), item.getMetadata().getCreationTimestamp()), new PartialObjectMetadata(item.getMetadata()));
				})
				.collect(Collectors.toList());
			Table table = new Table(tableColumnDefinitions, rows);
			requestContext.httpContext().response()
				.putHeader(HttpHeaders.CONTENT_TYPE, "application/json;as=Table;v=v1beta1;g=meta.k8s.io")
				.end(jsonMapper.writeValueAsString(table));
			return;
		}

		String responseContentType = Optional.ofNullable(
				Optional.ofNullable(requestContext.httpContext().request().getHeader(HttpHeaders.ACCEPT))
						.map(acceptHeader -> acceptHeader != null && acceptHeader.equals("*/*") ? null : acceptHeader)
						.map(acceptHeader -> {
							return acceptHeader.contains("*/*") ? acceptHeader.replace("*/*", "").trim().replace(",", "") : acceptHeader;
						})
						.orElseGet(() -> requestContext.httpContext().request().getHeader(HttpHeaders.CONTENT_TYPE)))
				.orElseGet(() -> "application/yaml");

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
		if(requestContext.resourceName().isPresent()) {
			Resource resource = resourcesRepository.getResource(resourceKey(requestContext));
			if(resource == null) {
				requestContext.httpContext().response()
					.setStatusCode(HttpResponseStatus.NOT_FOUND.code())
					.end();
			}else {
				sendResponse(requestContext, resource);
			}
		}else {
			List<Resource> resources = list(requestContext);
			Map<String, Object> listObject = new HashMap<>();
			listObject.put("apiVersion", "v1");
			listObject.put("kind", "List");
			listObject.put("items", resources);
			sendResponse(requestContext, listObject);
		}
	}

	private ResourceKey resourceKey(ApiServerRequestContext requestContext) {
		return new ResourceKey(ApiServerUtils.formatApiVersion(requestContext.group(), requestContext.version()), requestContext.kind(), requestContext.resourceType(), requestContext.resourceName().get());
	}

	private List<Resource> list(ApiServerRequestContext requestContext) {
		if (requestContext.resourceType().scope() == ResourceScope.Global) {
			return resourcesRepository.doGlobalQuery(ApiServerUtils.formatApiVersion(requestContext.group(), requestContext.version()), requestContext.kind());
		} else {
			return resourcesRepository.doNamespacedQuery(ApiServerUtils.formatApiVersion(requestContext.group(), requestContext.version()), requestContext.kind(), requestContext.resourceType().namespace());
		}
	}

	protected ObjectMapper getRequestObjectMapper(ApiServerRequestContext requestContext) {
		if(requestContext.httpContext().parsedHeaders().contentType().value().equals("application/yaml")) {
			return yamlMapper;
		}else {
			return jsonMapper;
		}
	}

}
