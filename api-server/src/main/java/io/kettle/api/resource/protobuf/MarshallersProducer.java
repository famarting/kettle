package io.kettle.api.resource.protobuf;

import javax.enterprise.inject.Produces;

import org.infinispan.protostream.BaseMarshaller;
import org.infinispan.protostream.MessageMarshaller;

public class MarshallersProducer {

	@Produces
	MessageMarshaller resourceMarshaller() {
		return new ResourceMarshaller();
	}
	
	@Produces
	MessageMarshaller resourceMetadataMarshaller() {
		return new ResourceMetadataMarshaller();
	}
	
	@Produces
	MessageMarshaller definitionResourceSpecMarshaller() {
		return new DefinitionResourceSpecMarshaller();
	}
	
	@Produces 
	MessageMarshaller metadataPropertyMarshaller() {
		return new MetadataPropertyMarshaller();
	}
	
	@Produces
	MessageMarshaller resourceNamesMarshaller() {
		return new ResourceNamesMarshaller();
	}
	
	@Produces
	MessageMarshaller definitionResourceKeyMarshaller() {
		return new DefinitionResourceKeyMarshaller();
	}
	
	@Produces
	MessageMarshaller resourceKeyMarshaller() {
		return new ResourceKeyMarshaller();
	}
	
	@Produces
	MessageMarshaller resourceTypeMarshaller() {
		return new ResourceTypeMarshaller();
	}
	
	@Produces
	BaseMarshaller resourceScopeMarshaller() {
		return new ResourceScopeMarshaller();
	}
	
	
}
