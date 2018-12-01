package com.hunterwb.noconstructors.javac;

import com.hunterwb.noconstructors.NoConstructors;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import org.objectweb.asm.*;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class NoConstructorsPlugin implements Plugin, TaskListener {

    private final List<String> annotatedClassNames = new ArrayList<>();

    private JavaFileManager fileManager;

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public void init(JavacTask task, String... args) {
        try {
            Object ctx = task.getClass().getMethod("getContext").invoke(task);
            fileManager = (JavaFileManager) ctx.getClass().getMethod("get", Class.class).invoke(ctx, JavaFileManager.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        task.addTaskListener(this);
    }

    @Override
    public void finished(TaskEvent e) {
        switch (e.getKind()) {
            case GENERATE:
                if (e.getTypeElement().getAnnotation(NoConstructors.class) != null) {
                    annotatedClassNames.add(e.getTypeElement().getQualifiedName().toString());
                }
                break;
            case COMPILATION:
                for (String className : annotatedClassNames) {
                    try {
                        JavaFileObject readableFile = fileManager.getJavaFileForInput(StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS);
                        ClassWriter cw;
                        try (InputStream in = readableFile.openInputStream()) {
                            ClassReader cr = new ClassReader(in);
                            cw = new ClassWriter(cr, 0);
                            cr.accept(new ConstructorRemoval(cw), 0);
                        }

                        JavaFileObject writableFile = fileManager.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT, className, JavaFileObject.Kind.CLASS, null);
                        try (OutputStream out = writableFile.openOutputStream()) {
                            out.write(cw.toByteArray());
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                break;
        }
    }

    final static class ConstructorRemoval extends ClassVisitor {

        ConstructorRemoval(ClassVisitor cv) {
            super(Opcodes.ASM7, cv);
        }

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
    }
}
