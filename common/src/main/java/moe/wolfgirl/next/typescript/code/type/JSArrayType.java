package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JSArrayType extends BaseType {
    public final List<BaseType> components;

    public JSArrayType(List<BaseType> components) {
        this.components = components;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (BaseType component : components) {
            paths.addAll(component.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration) {
        return List.of(
                "[%s]".formatted(components.stream()
                        .map(type -> "(%s)".formatted(type.line(declaration)))
                        .collect(Collectors.joining(", ")))
        );
    }
}
