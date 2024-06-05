package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class JSJoinedType extends BaseType {
    public final String delimiter;
    public final List<BaseType> types;

    protected JSJoinedType(String delimiter, List<BaseType> types) {
        this.delimiter = " %s ".formatted(delimiter);
        this.types = types;
    }


    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        Set<ClassPath> paths = new HashSet<>();
        for (BaseType type : types) {
            paths.addAll(type.getUsedClassPaths());
        }
        return paths;
    }

    @Override
    public List<String> format(Declaration declaration, boolean input) {
        return List.of(
                types.stream()
                        .map(type -> "(%s)".formatted(type.line(declaration, input)))
                        .collect(Collectors.joining(delimiter))
        );
    }

    public static class Union extends JSJoinedType {
        public Union(List<BaseType> types) {
            super("|", types);
        }
    }

    public static class Intersection extends JSJoinedType {

        public Intersection(List<BaseType> types) {
            super("&", types);
        }
    }
}
