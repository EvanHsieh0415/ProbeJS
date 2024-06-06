package moe.wolfgirl.next.java.type.impl;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.java.type.TypeAdapter;
import moe.wolfgirl.next.java.type.TypeDescriptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.GenericArrayType;
import java.util.Collection;
import java.util.stream.Stream;

public class ArrayType extends TypeDescriptor {
    public final TypeDescriptor component;

    public ArrayType(AnnotatedArrayType arrayType) {
        super(arrayType.getAnnotations());
        this.component = TypeAdapter.getTypeDescription(arrayType.getAnnotatedGenericComponentType());
    }

    public ArrayType(GenericArrayType arrayType) {
        super(new Annotation[]{});
        this.component = TypeAdapter.getTypeDescription(arrayType.getGenericComponentType());
    }

    @Override
    public Stream<TypeDescriptor> stream() {
        return component.stream();
    }

    @Override
    public Collection<ClassPath> getClassPaths() {
        return component.getClassPaths();
    }

    @Override
    public int hashCode() {
        return component.hashCode() * 31;
    }
}
