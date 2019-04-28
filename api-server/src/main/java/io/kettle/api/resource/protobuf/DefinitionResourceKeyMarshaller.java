package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.extension.DefinitionResourceKey;

public class DefinitionResourceKeyMarshaller implements MessageMarshaller<DefinitionResourceKey>{

	@Override
	public Class<? extends DefinitionResourceKey> getJavaClass() {
		return DefinitionResourceKey.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.DefinitionResourceKey";
	}

	@Override
	public DefinitionResourceKey readFrom(ProtoStreamReader reader) throws IOException {
		return new DefinitionResourceKey(reader.readString("group"), reader.readString("version"), reader.readString("kind"));
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, DefinitionResourceKey t) throws IOException {
		writer.writeString("group", t.group);
		writer.writeString("version", t.version);
		writer.writeString("kind", t.kind);
	}

}
