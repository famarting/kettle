package io.kettle.api.storage;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.DefinitionResourceKey;

@Singleton
public class ResourcesStorageManager {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public static final String RESOURCES_CACHE = "resourcesstore";
	public static final String DEFINITIONS_CACHE = "definitionsstore";
	
	@Produces
	@Singleton
	ResourcesRepository resourcesRepository(RemoteCacheManager cacheManager) {
		log.info("Initializing resources cache");
		RemoteCache<ResourceKey, Resource> resourcesCache = cacheManager.administration().getOrCreateCache(RESOURCES_CACHE, "default");
		return new ResourcesRepository(resourcesCache);
	}
	
	@Produces
	@Singleton
	DefinitionResourceRepository definitionResourceRepository(RemoteCacheManager cacheManager) {
		log.info("Initializing definitions cache");
		RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> definitionsCache = cacheManager.administration().getOrCreateCache(DEFINITIONS_CACHE, "default");
		RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> coreDefinitionsCache = cacheManager.administration().getOrCreateCache("core_"+DEFINITIONS_CACHE, "default");
		return new DefinitionResourceRepository(definitionsCache, coreDefinitionsCache);
	}
	
}
