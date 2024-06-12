package moe.wolfgirl.probejs.jdoc.document;

import com.google.gson.JsonObject;
import moe.wolfgirl.probejs.jdoc.JsAnnotations;
import moe.wolfgirl.probejs.jdoc.Serde;
import moe.wolfgirl.probejs.jdoc.java.ClassInfo;
import moe.wolfgirl.probejs.jdoc.property.PropertyType;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.*;

/**
 * The Document of a class.
 */
public class DocumentClass extends AbstractDocument<DocumentClass> {
    protected String name;
    protected List<PropertyType<?>> generics = new ArrayList<>();
    protected PropertyType<?> parent;
    public Set<PropertyType<?>> interfaces = new HashSet<>();
    public Set<DocumentField> fields = new HashSet<>();
    public Set<DocumentMethod> methods = new HashSet<>();
    public Set<DocumentConstructor> constructors = new HashSet<>();

    protected boolean isAbstract = false;
    protected boolean isInterface = false;
    protected boolean isFunctionalInterface = false;

    @Override
    public JsonObject serialize() {
        JsonObject object = super.serialize();
        object.addProperty("className", name);
        object.addProperty("abstract", isAbstract);
        object.addProperty("interface", isInterface);
        if (parent != null) object.add("parent", parent.serialize());
        Serde.serializeCollection(object, "fields", fields);
        Serde.serializeCollection(object, "methods", methods);
        Serde.serializeCollection(object, "variables", generics, true);
        Serde.serializeCollection(object, "interfaces", interfaces, true);
        Serde.serializeCollection(object, "constructors", constructors);
        return object;
    }

    @Override
    public void deserialize(JsonObject object) {
        super.deserialize(object);
        name = object.get("className").getAsString();
        if (object.has("parent"))
            parent = (PropertyType<?>) Serde.deserializeProperty(object.get("parent").getAsJsonObject());
        if (object.has("abstract"))
            isAbstract = object.get("abstract").getAsBoolean();
        if (object.has("interface"))
            isInterface = object.get("interface").getAsBoolean();
        Serde.deserializeDocuments(this.fields, object.get("fields"));
        Serde.deserializeDocuments(this.methods, object.get("methods"));
        Serde.deserializeDocuments(this.constructors, object.get("constructors"));
        Serde.deserializeDocuments(this.generics, object.get("variables"));
        Serde.deserializeDocuments(this.interfaces, object.get("interfaces"));

    }

    public static DocumentClass fromJava(ClassInfo info) {
        DocumentClass document = new DocumentClass();
        document.name = info.getName();
        document.isAbstract = info.isAbstract();
        document.isInterface = info.isInterface();
        document.parent = info.getSuperClass() != null ? Serde.deserializeFromJavaType(info.getSuperClassType()) : null;
        document.interfaces.addAll(info.getInterfaceTypes().stream().map(Serde::deserializeFromJavaType).toList());
        document.generics.addAll(info.getParameters().stream().map(Serde::deserializeFromJavaType).toList());
        info.getFieldInfo().stream().map(DocumentField::fromJava).forEach(document.fields::add);
        info.getMethodInfo().stream().map(DocumentMethod::fromJava).forEach(document.methods::add);
        info.getConstructorInfo().stream().map(DocumentConstructor::fromJava).forEach(document.constructors::add);
        info.getAnnotations().stream().filter(annotation -> annotation instanceof Deprecated).findFirst().ifPresent(annotation -> {
            document.builtinComments.add("@deprecated");
            if (((Deprecated) annotation).forRemoval()) {
                document.builtinComments.add("This class is marked to be removed in future!");
            }
        });
        info.getAnnotations().stream().filter(annotation -> annotation instanceof Info).findFirst().ifPresent(annotation -> {
            document.builtinComments = document.builtinComments.merge(JsAnnotations.fromAnnotation((Info) annotation, true));
        });
        document.properties.addAll(JsAnnotations.getClassAssignments(info));
        return document;
    }

    @Override
    public DocumentClass applyProperties() {
        return this;
    }

    @Override
    public DocumentClass merge(DocumentClass other) {
        //Overwrites everything basing on document.
        //Generics are not overwritten tho, just added
        if (this == other) return this;
        DocumentClass document = other.copy();
        document.parent = parent;
        document.interfaces.addAll(interfaces);
        document.methods.addAll(methods);
        document.fields.addAll(fields);
        document.properties.addAll(properties);
        document.constructors = constructors;
        document.generics.addAll(generics);
        document.isInterface = isInterface;
        document.isAbstract = isAbstract;
        document.builtinComments = document.builtinComments.merge(builtinComments);
        return document;
    }

    @Override
    public DocumentClass copy() {
        DocumentClass document = new DocumentClass();
        document.name = name;
        document.parent = parent;
        document.isInterface = isInterface;
        document.isAbstract = isAbstract;
        document.interfaces.addAll(interfaces);
        document.properties.addAll(properties);
        document.methods.addAll(methods);
        document.fields.addAll(fields);
        document.builtinComments.merge(builtinComments);
        return document;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentClass that = (DocumentClass) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public String getName() {
        return name;
    }

    public Set<DocumentConstructor> getConstructors() {
        return constructors;
    }

    public Set<DocumentMethod> getMethods() {
        return methods;
    }

    public Set<DocumentField> getFields() {
        return fields;
    }

    public Set<PropertyType<?>> getInterfaces() {
        return interfaces;
    }

    public List<PropertyType<?>> getGenerics() {
        return generics;
    }

    public PropertyType<?> getParent() {
        return parent;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public boolean isInterface() {
        return isInterface;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void setInterface(boolean anInterface) {
        isInterface = anInterface;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParent(PropertyType<?> parent) {
        this.parent = parent;
    }
}
