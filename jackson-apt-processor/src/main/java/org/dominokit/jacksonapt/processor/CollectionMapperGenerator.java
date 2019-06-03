package org.dominokit.jacksonapt.processor;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import org.dominokit.jacksonapt.AbstractObjectMapper;

import com.squareup.javapoet.MethodSpec;

@Deprecated
public class CollectionMapperGenerator extends AbstractMapperGenerator {
	@Override
	protected Class<?> getSuperClass() {
		return AbstractObjectMapper.class;
	}
	
	@Override
    protected Iterable<MethodSpec> getMapperMethods(Element element, TypeMirror beanType) {
        return Stream.of(makeNewDeserializerMethod(element, beanType), makeNewSerializerMethod(beanType))
                .collect(Collectors.toList());
    }

	@Override
	protected void generateSerializer(TypeMirror beanType, String packageName, SubTypesInfo subTypesInfo) {
		new SerializerGenerator().generate(beanType, packageName, subTypesInfo);
	}
	
	@Override
	protected void generateDeserializer(TypeMirror beanType, String packageName, SubTypesInfo subTypesInfo) {
		new DeserializerGenerator().generate(beanType, packageName, subTypesInfo);
	}
}
