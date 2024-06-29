package moe.wolfgirl.probejs.lang.transpiler.transformation;

import moe.wolfgirl.probejs.lang.java.clazz.ClassPath;
import moe.wolfgirl.probejs.lang.java.clazz.Clazz;
import moe.wolfgirl.probejs.lang.java.clazz.members.ConstructorInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.MethodInfo;
import moe.wolfgirl.probejs.lang.java.clazz.members.ParamInfo;
import moe.wolfgirl.probejs.lang.java.type.impl.ClassType;
import moe.wolfgirl.probejs.lang.java.type.impl.ParamType;
import moe.wolfgirl.probejs.lang.typescript.code.member.ConstructorDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.MethodDecl;
import moe.wolfgirl.probejs.lang.typescript.code.member.ParamDecl;
import moe.wolfgirl.probejs.lang.typescript.code.type.BaseType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSClassType;
import moe.wolfgirl.probejs.lang.typescript.code.type.TSParamType;
import moe.wolfgirl.probejs.lang.typescript.code.type.Types;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

import java.util.Set;

public class InjectSpecialType implements ClassTransformer {
    public static final Set<ClassPath> NO_WRAPPING = Set.of(
            new ClassPath(ResourceKey.class),
            new ClassPath(TagKey.class),
            new ClassPath(HolderSet.class),
            new ClassPath(Holder.class)
    );

    public static void modifyWrapping(ParamDecl param) {
        if (param.type instanceof TSParamType paramType &&
                paramType.baseType instanceof TSClassType baseClass &&
                NO_WRAPPING.contains(baseClass.classPath)) {
            param.type = new TSParamType(
                    paramType.baseType,
                    paramType.params.stream()
                            .map(c -> Types.ignoreContext(c, BaseType.FormatType.RETURN))
                            .toList()
            );
        }
    }

    public static void modifyLambda(ParamDecl param, ParamInfo info) {
        if (info.type instanceof ParamType paramType &&
                paramType.base instanceof ClassType classType &&
                classType.clazz.isAnnotationPresent(FunctionalInterface.class) &&
                param.type instanceof TSParamType tsParamType) {
            param.type = new TSParamType(
                    tsParamType.baseType,
                    tsParamType.params.stream()
                            .map(c -> Types.ignoreContext(c, BaseType.FormatType.RETURN))
                            .toList()
            );
        }
    }

    @Override
    public void transformConstructor(ConstructorInfo constructorInfo, ConstructorDecl constructorDecl) {
        for (int i = 0; i < constructorDecl.params.size(); i++) {
            var param = constructorDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, constructorInfo.params.get(i));
        }
    }

    @Override
    public void transformMethod(Clazz clazz, MethodInfo methodInfo, MethodDecl methodDecl) {
        for (int i = 0; i < methodDecl.params.size(); i++) {
            var param = methodDecl.params.get(i);
            modifyWrapping(param);
            modifyLambda(param, methodInfo.params.get(i));
        }
    }
}
