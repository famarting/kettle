package io.kettle.api.resource.protobuf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.infinispan.protostream.MessageMarshaller;

import io.kettle.api.resource.MetadataProperty;
import io.kettle.api.resource.ResourceMetadata;

public class ResourceMetadataMarshaller implements MessageMarshaller<ResourceMetadata>{

	@Override
	public Class<? extends ResourceMetadata> getJavaClass() {
		return ResourceMetadata.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.ResourceMetadata";
	}

	@Override
	public ResourceMetadata readFrom(ProtoStreamReader reader) throws IOException {
//		  required string uid = 1;
//		  required string name = 2;
//		  required string namespace = 3;
//		  required string selfLink = 4;
//		  required string creationTimestamp = 5;
//		  optional map<string, string> labels = 6;
//		  optional map<string, string> annotations = 7;
		
		ResourceMetadata metadata = new ResourceMetadata();
		metadata.setUid(reader.readString("uid"));
		metadata.setName(reader.readString("name"));
		String ns = reader.readString("namespace");
		if(ns != null) {
			metadata.setNamespace(ns);			
		}
		metadata.setSelfLink(reader.readString("selfLink"));
		metadata.setCreationTimestamp(reader.readString("creationTimestamp"));
		List<MetadataProperty> labels = new ArrayList<>();
		reader.readCollection("labels", labels, MetadataProperty.class);
		metadata.setLabels(labels);	
		List<MetadataProperty> annotations = new ArrayList<>();
		reader.readCollection("annotations", annotations, MetadataProperty.class);
		metadata.setAnnotations(annotations);	
		return metadata;
	}

	@Override
	public void writeTo(ProtoStreamWriter writer, ResourceMetadata meta) throws IOException {
//		  required string uid = 1;
//		  required string name = 2;
//		  required string namespace = 3;
//		  required string selfLink = 4;
//		  required string creationTimestamp = 5;
//		  optional map<string, string> labels = 6;
//		  optional map<string, string> annotations = 7;
		writer.writeString("uid", meta.getUid());
		writer.writeString("name", meta.getName());
		if(meta.getNamespace()!=null) {
			writer.writeString("namespace", meta.getNamespace());			
		}
		writer.writeString("selfLink", meta.getSelfLink());
		writer.writeString("creationTimestamp", meta.getCreationTimestamp());
		writer.writeCollection("labels", meta.getLabels(), MetadataProperty.class);
		writer.writeCollection("annotations", meta.getAnnotations(), MetadataProperty.class);
	}

}
