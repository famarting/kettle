package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.extension.ResourceNames;

public class ResourceNamesMarshaller implements MessageMarshaller<ResourceNames>{

	@Override
	public Class<? extends ResourceNames> getJavaClass() {
		return ResourceNames.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.ResourceNames";
	}

//	message ResourceNames {
//	  required string kind = 1;
//	  required string listKind = 2;
//	  required string singular = 3;
//	  required string plural = 4;
//	}
	
	@Override
	public ResourceNames readFrom(ProtoStreamReader reader) throws IOException {
		ResourceNames names = new ResourceNames();
		names.setKind(reader.readString("kind"));
		names.setListKind(reader.readString("listKind"));
		names.setSingular(reader.readString("singular"));
		names.setPlural(reader.readString("plural"));
		return names;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ResourceNames t) throws IOException {
		writer.writeString("kind", t.getKind());
		writer.writeString("listKind", t.getListKind());
		writer.writeString("singular", t.getSingular());
		writer.writeString("plural", t.getPlural());
	}

}
