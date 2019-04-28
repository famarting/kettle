package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.MetadataProperty;

public class MetadataPropertyMarshaller implements MessageMarshaller<MetadataProperty>{

	@Override
	public Class<? extends MetadataProperty> getJavaClass() {
		return MetadataProperty.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.MetadataProperty";
	}

	@Override
	public MetadataProperty readFrom(ProtoStreamReader reader) throws IOException {
		return new MetadataProperty(reader.readString("key"), reader.readString("value")); 
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, MetadataProperty t) throws IOException {
		writer.writeString("key", t.getKey());
		writer.writeString("value", t.getValue());
	}

}
