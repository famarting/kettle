package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.extension.ResourceScope;
import io.kettle.api.resource.type.ResourceType;

public class ResourceTypeMarshaller implements MessageMarshaller<ResourceType>{

	@Override
	public Class<? extends ResourceType> getJavaClass() {
		return ResourceType.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.ResourceType";
	}

	@Override
	public ResourceType readFrom(ProtoStreamReader reader) throws IOException {
		return new ResourceType(reader.readEnum("scope", ResourceScope.class), reader.readString("namespace"));
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ResourceType t) throws IOException {
		writer.writeEnum("scope", t.scope());
		writer.writeString("namespace", t.namespace());
	}

}
