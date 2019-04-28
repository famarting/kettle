package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.ResourceKey;
import io.kettle.api.resource.type.ResourceType;

public class ResourceKeyMarshaller implements MessageMarshaller<ResourceKey>{

	@Override
	public Class<? extends ResourceKey> getJavaClass() {
		return ResourceKey.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.ResourceKey";
	}

	@Override
	public ResourceKey readFrom(ProtoStreamReader reader) throws IOException {
		return new ResourceKey(reader.readString("apiVersion"), 
				reader.readString("kind"), 
				reader.readObject("type", ResourceType.class), 
				reader.readString("name"));
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ResourceKey t) throws IOException {
		writer.writeString("apiVersion", t.apiVersion);
		writer.writeString("kind", t.kind);
		writer.writeObject("type", t.type, ResourceType.class);
		writer.writeString("name", t.name);
	}

}
