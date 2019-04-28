package io.kettle.api.resource.protobuf;

import org.infinispan.protostream.EnumMarshaller;

import io.kettle.api.resource.extension.ResourceScope;

public class ResourceScopeMarshaller implements EnumMarshaller<ResourceScope> {

	@Override
	public Class<? extends ResourceScope> getJavaClass() {
		return ResourceScope.class;
	}

	@Override
	public String getTypeName() {
		return "kettle.ResourceScope";
	}

	@Override
	public ResourceScope decode(int enumValue) {
		switch (enumValue) {
		case 0:
			return ResourceScope.Global;
		case 1:
			return ResourceScope.Namespaced;
		default:
			return null;
		}
	}

	@Override
	public int encode(ResourceScope e) throws IllegalArgumentException {
		switch (e) {
		case Global:
			return 0;
		case Namespaced:
			return 1;
		default:
			throw new IllegalArgumentException("Unexpected ResultType value : " + e);
		}
	}

}
