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
import com.yy.lqw.pvm.annotations.PVM;
import com.yy.lqw.pvm.annotations.PVMSink;

import java.io.IOException;
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
import javax.lang.model.type.MirroredTypeException;
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

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        note("PVMProcessor begin, %s, %s", annotations, roundEnv);
        for (Element classElement : roundEnv.getElementsAnnotatedWith(PVM.class)) {
            PVM pvmAnnotation = classElement.getAnnotation(PVM.class);
            // ClassName of view
            ClassName vClassName = (ClassName) ClassName.get(classElement.asType());

            // ClassName of presenter
            ClassName pClassName;
            try {
                pClassName = ClassName.get(pvmAnnotation.presenter());
            } catch (MirroredTypeException e) {
                pClassName = (ClassName) ClassName.get(e.getTypeMirror());
            }

            // ClassName of interface
            final String iName = pClassName.simpleName() + "Proxy";
            ClassName iClassName = ClassName.get(pClassName.packageName(), iName);

            // mHandler field
            FieldSpec fieldSpec = FieldSpec.builder(mHandlerClassName, "mHandler", Modifier.PRIVATE)
                    .initializer("new $T($T.getMainLooper())", mHandlerClassName, mLooperClassName)
                    .build();

            // constructor
            MethodSpec constructMethod = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(vClassName, "view")
                    .addStatement("mView = view")
                    .build();
            final String cName = vClassName.simpleName() + iName + "Impl";
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(cName);
            classBuilder.addModifiers(Modifier.PUBLIC)
                    .addSuperinterface(iClassName)
                    .addMethod(constructMethod)
                    .addField(fieldSpec)
                    .addField(vClassName, "mView", Modifier.PRIVATE);

            TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(iClassName);
            interfaceBuilder.addModifiers(Modifier.PUBLIC);
            for (Element methodElement : roundEnv.getElementsAnnotatedWith(PVMSink.class)) {
                if (methodElement.getEnclosingElement().equals(classElement)) {
                    ExecutableElement ee = (ExecutableElement) methodElement;
                    addMethod(interfaceBuilder, ee, TypeSpec.Kind.INTERFACE);
                    addMethod(classBuilder, ee, TypeSpec.Kind.CLASS);
                }
            }

            String packageName = pClassName.packageName();
            TypeSpec typeSpec = interfaceBuilder.build();
            JavaFile interfaceFile = JavaFile.builder(packageName, typeSpec).build();
            packageName = vClassName.packageName();
            typeSpec = classBuilder.build();
            JavaFile classFile = JavaFile.builder(packageName, typeSpec).build();
            try {
                classFile.writeTo(processingEnv.getFiler());
                interfaceFile.writeTo(processingEnv.getFiler());
            } catch (FilerException e) {
                note("FilerException: " + e.getMessage());
            } catch (IOException e) {
                error("Write file error: " + e.getMessage());
            }
        }
        note("PVMProcessor end");
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return mSupportedAnnotationTypes;
    }

    private void addMethod(TypeSpec.Builder builder,
                           ExecutableElement element,
                           TypeSpec.Kind kind) {
        final String methodName = element.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodName);
        for (TypeParameterElement typeParameterElement : element.getTypeParameters()) {
            TypeVariable var = (TypeVariable) typeParameterElement.asType();
            methodBuilder.addTypeVariable(TypeVariableName.get(var));
        }

        // parameters
        List<? extends VariableElement> parameters = element.getParameters();
        for (VariableElement parameter : parameters) {
            TypeName type = TypeName.get(parameter.asType());
            String name = parameter.getSimpleName().toString();
            Set<Modifier> parameterModifiers = parameter.getModifiers();
            Modifier[] modifiers = new Modifier[parameterModifiers.size()];
            ParameterSpec.Builder parameterBuilder = ParameterSpec.builder(type, name)
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
                    .beginControlFlow("if (mHandler.getLooper().isCurrentThread())")
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
