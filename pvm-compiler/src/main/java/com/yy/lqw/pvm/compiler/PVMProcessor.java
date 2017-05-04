package com.yy.lqw.pvm.compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.yy.lqw.pvm.Delegate;
import com.yy.lqw.pvm.Presenter;
import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class PVMProcessor extends AbstractProcessor {
    private final Set<String> mSupportedAnnotationTypes = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    PVM.class.getCanonicalName(),
                    PVMSink.class.getCanonicalName()
            ))
    );
    private final ClassName mHandlerClassName = ClassName.get("android.os", "Handler");
    private final ClassName mLooperClassName = ClassName.get("android.os", "Looper");

    // handler field
    private final FieldSpec mHandlerField = FieldSpec.builder(mHandlerClassName, "mHandler", Modifier.PRIVATE)
            .initializer("new $T($T.getMainLooper())", mHandlerClassName, mLooperClassName)
            .build();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("PVM: begin, %s, %s", annotations, roundEnv);
        for (Element classElement : roundEnv.getElementsAnnotatedWith(PVM.class)) {
            // PVM annotation class
            final PVM pvmAnnotation = classElement.getAnnotation(PVM.class);

            // class name of view
            final ClassName viewClassName = (ClassName) ClassName.get(classElement.asType());

            // class name of presenters
            final List<ClassName> presenterClassNames = new ArrayList<>();

            try {
                for (Class<? extends Presenter> clazz : pvmAnnotation.presenters()) {
                    presenterClassNames.add(ClassName.get(clazz));
                }
            } catch (MirroredTypesException e) {
                for (TypeMirror typeMirror : e.getTypeMirrors()) {
                    presenterClassNames.add((ClassName) ClassName.get(typeMirror));
                }
            }

            if (presenterClassNames.size() > 0) {
                note("PVM: processing, view: %s, presenter(s): %s", viewClassName, presenterClassNames);
                processView(viewClassName, presenterClassNames, roundEnv, classElement);
            } else {
                error("PVM: error, no presenter found in view: %s", viewClassName);
            }
        }
        note("PVM: done");
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return mSupportedAnnotationTypes;
    }

    /**
     * Process a view which contains PVM annotation {@link PVM},
     * generate delegate interface and implementation class
     * <p>
     * Delegate name rule：Presenter's name + "Delegate"
     * Delegate name rule：View's name + Presenter's name + "DelegateImpl"
     *
     * @param viewClassName       View clas
     * @param presenterClassNames list of presenter classes
     * @param roundEnv
     * @param classElement
     */
    private void processView(ClassName viewClassName,
                             List<ClassName> presenterClassNames,
                             RoundEnvironment roundEnv,
                             Element classElement) {
        // constructor field
        final MethodSpec constructMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(viewClassName, "view")
                .addStatement("mView = view")
                .build();

        for (int i = 0; i < presenterClassNames.size(); i++) {
            // class name of presenter
            final ClassName presenterClassName = presenterClassNames.get(i);

            // name of delegate interface
            final String delegateInterfaceName = presenterClassName.simpleName() + "Delegate";

            // class name of delegate interface
            final ClassName delegateInterfaceClassName = ClassName.get(
                    presenterClassName.packageName(), delegateInterfaceName);

            // name of delegate impl
            final String delegateImplName = viewClassName.simpleName()
                    + delegateInterfaceName
                    + "Impl";

            // delegate impl class builder
            final TypeSpec.Builder delegateImplBuilder = TypeSpec.classBuilder(delegateImplName);
            delegateImplBuilder.addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(delegateInterfaceClassName)
                    .addMethod(constructMethod)
                    .addField(mHandlerField)
                    .addField(viewClassName, "mView", Modifier.PRIVATE);

            // delegate interface builder
            final TypeSpec.Builder delegateBuilder = TypeSpec.interfaceBuilder(delegateInterfaceClassName);
            delegateBuilder.addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(Delegate.class);

            for (Element methodElement : roundEnv.getElementsAnnotatedWith(PVMSink.class)) {
                final PVMSink sinkAnnotation = methodElement.getAnnotation(PVMSink.class);
                if (i == sinkAnnotation.ordinal()
                        && methodElement.getEnclosingElement().equals(classElement)) {
                    final ExecutableElement ee = (ExecutableElement) methodElement;
                    addMethod(delegateBuilder, ee, TypeSpec.Kind.INTERFACE);
                    addMethod(delegateImplBuilder, ee, TypeSpec.Kind.CLASS);
                }
            }

            // write delegate interface class
            String packageName = presenterClassName.packageName();
            TypeSpec typeSpec = delegateBuilder.build();
            JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
            writeToFile(javaFile);

            // write delegate impl class
            packageName = viewClassName.packageName();
            typeSpec = delegateImplBuilder.build();
            javaFile = JavaFile.builder(packageName, typeSpec).build();
            writeToFile(javaFile);
        }
    }

    private void addMethod(TypeSpec.Builder builder,
                           ExecutableElement element,
                           TypeSpec.Kind kind) {
        final String methodName = element.getSimpleName().toString();
        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
        for (TypeParameterElement typeParameterElement : element.getTypeParameters()) {
            final TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        // parameters
        final List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement parameter : parameters) {
            final TypeName type = TypeName.get(parameter.asType());
            final String name = parameter.getSimpleName().toString();
            final Set<Modifier> parameterModifiers = parameter.getModifiers();
            final Modifier[] modifiers = new Modifier[parameterModifiers.size()];
            final ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name)
                    .addModifiers(parameterModifiers.toArray(modifiers))
                    .addModifiers(Modifier.FINAL);
            for (AnnotationMirror mirror : parameter.getAnnotationMirrors()) {
                parameterBuilder.addAnnotation(AnnotationSpec.get(mirror));
            }
            methodBuilder.addParameter(parameterBuilder.build());
        }

        // return
        methodBuilder.returns(TypeName.get(element.getReturnType()))
                .addModifiers(element.getModifiers())
                .addModifiers(Modifier.PUBLIC)
                .varargs(element.isVarArgs());

        // interface add abstract modifier while class add implementation
        if (kind == TypeSpec.Kind.INTERFACE) {
            methodBuilder.addModifiers(Modifier.ABSTRACT);
        } else if (kind == TypeSpec.Kind.CLASS) {
            methodBuilder.addAnnotation(Override.class)
                    .beginControlFlow("if (Looper.myLooper() == Looper.getMainLooper())")
                    .addStatement("mView.$L($L)", methodName, parameters.toString())
                    .nextControlFlow("else")
                    .beginControlFlow("mHandler.post(new $T()", Runnable.class)
                    .beginControlFlow("@Override\npublic void run()")
                    .addStatement("mView.$L($L)", methodName, parameters.toString())
                    .endControlFlow()
                    .endControlFlow()
                    .addCode(");")
                    .endControlFlow();
        }

        builder.addMethod(methodBuilder.build());
    }

    private void writeToFile(JavaFile file) {
        try {
            file.writeTo(processingEnv.getFiler());
        } catch (FilerException e) {
            note("FilerException: " + e.getMessage());
        } catch (IOException e) {
            error("Write file error: " + e.getMessage());
        }
    }

    private void error(String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, message, args);
    }

    private void warn(String message, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, message, args);
    }

    private void note(String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }
        processingEnv.getMessager().printMessage(kind, message);
    }
}
