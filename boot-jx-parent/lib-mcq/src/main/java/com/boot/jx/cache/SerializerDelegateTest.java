package com.boot.jx.cache;

import java.io.IOException;
import java.io.Serializable;

import org.nustaq.serialization.FSTClazzInfo;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;
import org.nustaq.serialization.FSTObjectSerializer;
import org.nustaq.serialization.FSTSerializerRegistryDelegate;
import org.nustaq.serialization.serializers.FSTClassSerializer;

public class SerializerDelegateTest {

	static interface WrapperInterface {
		Object getWrapped();
	}

	static class WrappedObject implements WrapperInterface, Serializable {
		private static final long serialVersionUID = 4184362564631897069L;
		Object wrapped;

		public WrappedObject(Object wrapped) {
			this.wrapped = wrapped;
		}

		@Override
		public Object getWrapped() {
			return wrapped;
		}
	}

	static class MySkippingSerializer extends FSTClassSerializer {

		@Override
		public void writeObject(FSTObjectOutput out, Object toWrite, FSTClazzInfo clzInfo,
				FSTClazzInfo.FSTFieldInfo referencedBy, int streamPosition) throws IOException {
			super.writeObject(out, toWrite, clzInfo, referencedBy, streamPosition);
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Object instantiate(Class objectClass, FSTObjectInput in, FSTClazzInfo serializationInfo,
				FSTClazzInfo.FSTFieldInfo referencee, int streamPosition) throws Exception {
			//in.readObject(); // ensure everything read and registered same order as on write time
			try {
				return super.instantiate(objectClass, in, serializationInfo, referencee, streamPosition);	
			} catch (Exception e) {
				System.out.println("ERRRRRRRRRRRRRRRRRRRR"+e.getMessage());
				return REALLY_NULL;
			}
			//return null; // but just don't use/return it.
			// Note: returning 'null' will result in fst attempting to construct an instance
			// and call readObject on the serializer
		}

	}

	static class MyFSTSerializerRegistryDelegate implements FSTSerializerRegistryDelegate {
		final MySkippingSerializer skippingSer = new MySkippingSerializer();

		@SuppressWarnings("rawtypes")
		@Override
		public FSTObjectSerializer getSerializer(Class cl) {
			//if (WrapperInterface.class.isAssignableFrom(cl)) {
				return skippingSer;
			//}
			//return null;
		}
	}

}
