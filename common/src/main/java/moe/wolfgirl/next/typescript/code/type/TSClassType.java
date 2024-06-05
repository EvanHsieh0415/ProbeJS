package moe.wolfgirl.next.typescript.code.type;

import moe.wolfgirl.next.java.clazz.ClassPath;
import moe.wolfgirl.next.typescript.Declaration;
import moe.wolfgirl.next.typescript.Reference;

import java.util.Collection;
import java.util.List;

public class TSClassType extends BaseType {
    public ClassPath classPath;

    public TSClassType(ClassPath classPath) {
        this.classPath = classPath;
    }

    @Override
    public Collection<ClassPath> getUsedClassPaths() {
        return List.of(classPath);
    }

    @Override
    public List<String> format(Declaration declaration) {
        return List.of(declaration.getSymbol(classPath));
    }
}
