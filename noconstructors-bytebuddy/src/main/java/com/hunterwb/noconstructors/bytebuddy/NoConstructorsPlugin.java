package com.hunterwb.noconstructors.bytebuddy;

import com.hunterwb.noconstructors.NoConstructors;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberRemoval;
import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.OpenedClassReader;

public final class NoConstructorsPlugin implements Plugin {

    @Override
    public boolean matches(TypeDescription target) {
        return target.getDeclaredAnnotations().isAnnotationPresent(NoConstructors.class);
    }

    @Override
    public DynamicType.Builder<?> apply(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            ClassFileLocator classFileLocator
    ) {
        return builder.visit(new AsmVisitorWrapper.AbstractBase() {

            @Override
            public ClassVisitor wrap(
                    TypeDescription instrumentedType,
                    ClassVisitor classVisitor,
                    Implementation.Context implementationContext,
                    TypePool typePool,
                    FieldList<FieldDescription.InDefinedShape> fields,
                    MethodList<?> methods,
                    int writerFlags,
                    int readerFlags
            ) {
                return new ClassVisitor(OpenedClassReader.ASM_API, classVisitor) {

                    @Override
                    public AnnotationVisitor visitAnnotation(
                            String descriptor,
                            boolean visible
                    ) {
                        return "Lcom/hunterwb/noconstructors/NoConstructors;".equals(descriptor)
                                ? null
                                : super.visitAnnotation(descriptor, visible);
                    }

                    @Override
                    public MethodVisitor visitMethod(
                            int access,
                            String name,
                            String descriptor,
                            String signature,
                            String[] exceptions
                    ) {
                        return "<init>".equals(name)
                                ? null
                                : super.visitMethod(access, name, descriptor, signature, exceptions);
                    }
                };
            }
        });
    }

    @Override
    public void close() {}
}
