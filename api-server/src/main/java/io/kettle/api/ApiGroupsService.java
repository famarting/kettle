package io.kettle.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import io.kettle.api.k8s.APIGroupList;
import io.kettle.api.k8s.ApiGroup;
import io.kettle.api.k8s.GroupVersionForDiscovery;

/**
 * ApiGroupsService
 */
@ApplicationScoped
public class ApiGroupsService {

    private Map<String, Map<String,GroupVersionForDiscovery>> groups = new ConcurrentHashMap<>();

	public void addApiGroup(String group, String version) {
        String groupVersion = group+"/"+version;
        GroupVersionForDiscovery forDiscovery = new GroupVersionForDiscovery();
        forDiscovery.setGroupVersion(groupVersion);
        forDiscovery.setVersion(version);
        groups.computeIfAbsent(group, k -> {
            Map<String,GroupVersionForDiscovery> groupVersions = new ConcurrentHashMap<>();
            return groupVersions;
        })
        .put(groupVersion, forDiscovery);
	}

    public APIGroupList getApiGroupList() {
        APIGroupList list = new APIGroupList();

        list.setApiVersion("v1");
        list.setKind("APIGroupList");

        list.setGroups(groups.entrySet().stream()
            .map(e -> {
                ApiGroup group = new ApiGroup();
                group.setName(e.getKey());
                group.setVersions(e.getValue().values().stream()
                    // .flatMap(List::stream)
                    .collect(Collectors.toList()));
                //TODO implement this properly
                group.setPreferredVersion(group.getVersions().get(0));
                return group;
            })
            .collect(Collectors.toList()));

        return list;
    }

    //TODO implement remove

}