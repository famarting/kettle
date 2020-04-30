package io.kettle.core.storage.filesystem;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.kettle.core.KettleConstants;
import io.kettle.core.KettleUtils;
import io.kettle.core.resource.Resource;
import io.kettle.core.resource.ResourceKey;
import io.kettle.core.resource.extension.DefinitionResourceSpec;
import io.kettle.core.resource.extension.ResourceScope;
import io.kettle.core.resource.type.ResourceType;
import io.kettle.core.storage.ResourcesRepository;

@ApplicationScoped
public class FilesystemResourcesRepository implements ResourcesRepository {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    String configStoragePath;

    private ObjectMapper jsonMapper = new ObjectMapper();

    @Override
    public DefinitionResourceSpec findDefinitionResourceByNames(String name) {
        return list(KettleUtils.formatApiVersion(KettleConstants.CORE_API_GROUP, KettleConstants.CORE_API_VERSION), KettleConstants.DEFINITION_RESOURCE_KIND, null)
            .stream()
            .map(r -> new DefinitionResourceSpec(r.getSpec()))
            .filter(d ->
                d.getNames().getKind().equals(name) || d.getNames().getPlural().equals(name)
                    || d.getNames().getSingular().equals(name) || d.getShortNames().contains(name)
            )
            .findFirst()
            .orElse(null);
    }

    /**
     *
     *
     /apis/core/v1beta1/resourcesdefinitions/
     /api/v1/namespaces/
     /apis/library.io/v1alpha1/namespaces/"+namespaceName+"/books/"+book1Name
     */

    @Override
    public void createResource(ResourceType resourceType, Resource resource) {

        var path = Paths.get(getResourcePath(resourceType, resource));
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, jsonMapper.writeValueAsBytes(resource), StandardOpenOption.CREATE_NEW);
        } catch ( IOException e ) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public void updateResource(ResourceType resourceType, Resource resource) {

        var path = Paths.get(getResourcePath(resourceType, resource));
        try {
            Files.copy(new ByteArrayInputStream(jsonMapper.writeValueAsBytes(resource)), path, StandardCopyOption.REPLACE_EXISTING);
        } catch ( IOException e ) {
            throw new UncheckedIOException(e);
        }

    }

    @Override
    public Resource deleteResource(ResourceKey key) {
        Resource r = getResource(key);
        if (r != null) {
            try {
                Files.delete(Paths.get(getResourcePath(key)));
            } catch ( IOException e ) {
                throw new UncheckedIOException(e);
            }
        }
        return r;
    }

    @Override
    public Resource getResource(ResourceKey key) {
        try {
            InputStream in = null;
            try {
                in = new FileInputStream(Paths.get(getResourcePath(key)).toFile());
            } catch(FileNotFoundException fnfe) {
                return null;
            }
            return jsonMapper.readValue(in, Resource.class);
        } catch ( IOException e ) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public List<Resource> list(String apiVersion, String kind, String namespace) {
        var builder = new StringBuilder(configStoragePath)
                .append("apis")
                .append("/")
                .append(apiVersion)
                .append("/");

        if (namespace != null) {
            builder.append("namespaces").append("/").append(namespace).append("/");
        }

        builder.append(kind).append("/");

        Path path = Paths.get(builder.toString());
        if (Files.notExists(path)) {
            return new ArrayList<>();
        }

        try {
            return Files.list(path)
                    .map(p -> {
                        try {
                            return jsonMapper.readValue(new FileInputStream(p.toFile()), Resource.class);
                        } catch ( IOException e ) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .collect(Collectors.toList());
        } catch ( IOException e ) {
            throw new UncheckedIOException(e);
        }
    }

    private String getResourcePath(ResourceType resourceType, Resource resource) {
        var builder = new StringBuilder(configStoragePath)
                .append("apis")
                .append("/")
                .append(resource.getApiVersion())
                .append("/");

        if (resourceType.scope() == ResourceScope.Namespaced) {
            builder.append("namespaces").append("/").append(resourceType.namespace()).append("/");
        }

        builder.append(resource.getKind()).append("/").append(resource.getMetadata().getName());

        return builder.toString();
    }

    private String getResourcePath(ResourceKey key) {
        var builder = new StringBuilder(configStoragePath)
                .append("apis")
                .append("/")
                .append(key.apiVersion)
                .append("/");

        if (key.type.scope() == ResourceScope.Namespaced) {
            builder.append("namespaces").append("/").append(key.type.namespace()).append("/");
        }

        builder.append(key.kind).append("/").append(key.name);

        return builder.toString();
    }

    @Override
    public boolean isCompatible(String connectionString) {
        if(connectionString.startsWith("file://")) {
            log.info("Initializing filesystem based persistence");
            configStoragePath = connectionString.replace("file://", "");
            if (!configStoragePath.endsWith("/")) {
                configStoragePath += "/";
            }
            Path storagePath = Paths.get(configStoragePath);
            if (Files.exists(storagePath)) {
                if (Files.isDirectory(storagePath)) {
                    //TODO validate structure?
                } else {
                    throw new IllegalStateException("Storage path "+configStoragePath+" is not a directory");
                }
            } else {
                throw new IllegalStateException("Storage path "+configStoragePath+" does not exist");
            }
            return true;
        }
        return false;
    }

}
