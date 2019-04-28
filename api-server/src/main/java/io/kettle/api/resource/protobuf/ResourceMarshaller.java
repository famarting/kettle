package io.kettle.api.resource.protobuf;

import java.io.IOException;
import java.util.Map;

import org.infinispan.protostream.MessageMarshaller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.kettle.api.resource.Resource;
import io.kettle.api.resource.ResourceMetadata;

public class ResourceMarshaller implements MessageMarshaller<Resource>{

	private ObjectMapper mapper = new ObjectMapper();
	
	@Override
	public Class<? extends Resource> getJavaClass() {
		return Resource.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.Resource";
	}

	@Override
	public Resource readFrom(ProtoStreamReader reader) throws IOException {
		Resource resource = new Resource();
		resource.setApiVersion(reader.readString("apiVersion"));
		resource.setKind(reader.readString("kind"));
		resource.setMetadata(reader.readObject("metadata", ResourceMetadata.class));
		byte[] spec = reader.readBytes("spec");
		if(spec!=null) {
			resource.setSpec(mapper.readValue(spec, new TypeReference<Map<String, Object>>() {}));			
		}
		byte[] status = reader.readBytes("status");
		if(status!=null) {
			resource.setStatus(mapper.readValue(spec, new TypeReference<Map<String, Object>>() {}));			
		}
		return resource;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, Resource resource) throws IOException {
		writer.writeString("apiVersion", resource.getApiVersion());
		writer.writeString("kind", resource.getKind());
		writer.writeObject("metadata", resource.getMetadata(), ResourceMetadata.class);
		if(resource.getSpec()!=null) {
			writer.writeBytes("spec", mapper.writeValueAsBytes(resource.getSpec()));			
		}
		if(resource.getStatus() != null) {
			writer.writeBytes("status", mapper.writeValueAsBytes(resource.getStatus()));
		}
	}

}
