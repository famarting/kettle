package io.kettle.api.storage;

import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.type.ResourceType;

public class ResourcesRepository {

	private RemoteCache<ResourceKey, Resource> cache;

	private QueryFactory queryFactory;
	
	public ResourcesRepository(RemoteCache<ResourceKey, Resource> cache) {
		this.cache = cache;
		this.queryFactory = Search.getQueryFactory(this.cache);

	}
	
	public void createResource(ResourceType resourceType, Resource resource) {
		this.cache.put(new ResourceKey(resource.getApiVersion(), resource.getKind(), resourceType, resource.getMetadata().getName()), resource);
	}
	
	public Resource getResource(ResourceKey key) {
		return this.cache.get(key);
	}

	public List<Resource> doNamespacedQuery(String apiVersion, String kind, String namespace) {
		return queryFactory.from(Resource.class)
				.having("apiVersion").equal(apiVersion)
				.and()
				.having("kind").equal(kind)
				.and()
				.having("metadata.namespace").equal(namespace)
				.build()
				.list();
	}
	
	public List<Resource> doGlobalQuery(String apiVersion, String kind) {
		return queryFactory.from(Resource.class)
				.having("apiVersion").equal(apiVersion)
				.and()
				.having("kind").equal(kind)
				.build()
				.list();
	}
	
}
