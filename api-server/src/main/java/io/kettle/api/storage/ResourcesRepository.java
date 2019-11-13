package io.kettle.api.storage;

import java.util.List;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.type.ResourceType;

public interface ResourcesRepository {

	public Resource deleteResource(ResourceKey key);
	
	public void createResource(ResourceType resourceType, Resource resource);
	
	public void updateResource(ResourceType resourceType, Resource resource);
	
	public Resource getResource(ResourceKey key);

	public List<Resource> doNamespacedQuery(String apiVersion, String kind, String namespace);
	
	public List<Resource> doGlobalQuery(String apiVersion, String kind);

	//get by plural name
	
}
