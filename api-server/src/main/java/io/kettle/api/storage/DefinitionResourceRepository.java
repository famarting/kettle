package io.kettle.api.storage;

import java.util.Collection;
import java.util.List;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.Search;
import org.infinispan.query.dsl.QueryFactory;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.DefinitionResourceKey;

public class DefinitionResourceRepository {

	private RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> cache;
	private RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> coreResources;
	
	private QueryFactory resourcesQueryFactory;
	private QueryFactory coreQueryFactory;
	
	public DefinitionResourceRepository(RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> cache, RemoteCache<DefinitionResourceKey, DefinitionResourceSpec> coreResources) {
		this.cache = cache;
		this.coreResources = coreResources;
		
		this.resourcesQueryFactory = Search.getQueryFactory(this.cache);
		this.coreQueryFactory = Search.getQueryFactory(this.coreResources);
	}
	
	public void saveDefinitionResource(DefinitionResourceSpec definition) {
		cache.put(new DefinitionResourceKey(definition), definition);
	}
	
	public void saveCoreDefinitionResource(DefinitionResourceSpec definition) {
		coreResources.put(new DefinitionResourceKey(definition), definition);
	}
	
	public Collection<DefinitionResourceSpec> getAllDefinitions() {
		return cache.values();
	}
	
	public DefinitionResourceSpec getDefinition(String group, String version, String pluralName) {
		return lookUpDefinition(resourcesQueryFactory, group, version, pluralName).stream()
					.findFirst()
					.orElseGet(()->lookUpDefinition(coreQueryFactory, group, version, pluralName).stream()
									.findFirst()
									.orElse(null));
	}

	private List<DefinitionResourceSpec> lookUpDefinition(QueryFactory queryFactory, String group, String version, String pluralName) {
		return queryFactory.from(DefinitionResourceSpec.class)
			.having("group").equal(group)
			.and()
			.having("version").equal(version)
			.and()
			.having("names.plural").equal(pluralName)
			.build()
			.maxResults(1)
			.list();
	}
	
}
