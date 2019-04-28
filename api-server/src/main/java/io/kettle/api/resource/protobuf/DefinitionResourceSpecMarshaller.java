package io.kettle.api.resource.protobuf;

import java.io.IOException;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.extension.DefinitionResourceSpec;
import io.kettle.api.resource.extension.ResourceNames;
import io.kettle.api.resource.extension.ResourceScope;

public class DefinitionResourceSpecMarshaller implements MessageMarshaller<DefinitionResourceSpec>{

	@Override
	public Class<? extends DefinitionResourceSpec> getJavaClass() {
		return DefinitionResourceSpec.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.DefinitionResourceSpec";
	}

//	message DefinitionResourceSpec {
//		  required string group = 1;
//		  required string version = 2;
//		  enum ResourceScope {
//		    global = 0;
//		    namespace = 1;
//		  }
//		  required ResourceScope scope = 3;
//		  required ResourceNames names = 4;
//		}
//
//		message ResourceNames {
//		  required string kind = 1;
//		  required string listKind = 2;
//		  required string singular = 3;
//		  required string plural = 4;
//		}
	
	@Override
	public DefinitionResourceSpec readFrom(ProtoStreamReader reader) throws IOException {
		DefinitionResourceSpec spec = new DefinitionResourceSpec();
		spec.setGroup(reader.readString("group"));
		spec.setVersion(reader.readString("version"));
		spec.setScope(reader.readEnum("scope", ResourceScope.class));
		spec.setNames(reader.readObject("names", ResourceNames.class));
		return spec;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, DefinitionResourceSpec t) throws IOException {
		writer.writeString("group", t.getGroup());
		writer.writeString("version", t.getVersion());
		writer.writeEnum("scope", t.getScope());
		writer.writeObject("names", t.getNames(), ResourceNames.class);
	}

}
