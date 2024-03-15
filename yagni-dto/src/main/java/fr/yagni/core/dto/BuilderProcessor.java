package fr.yagni.core.dto;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("fr.yagni.core.dto.Builder")
@AutoService(Processor.class)
public class BuilderProcessor extends AbstractProcessor {

    List<ElementKind> availableKinds = List.of(ElementKind.RECORD, ElementKind.CLASS);

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        roundEnvironment.getElementsAnnotatedWith(Builder.class).stream()
                .filter(e -> availableKinds.contains(e.getKind()))
                .map(TypeElement.class::cast)
                .forEach(this::processElement);
        return false;
    }

    private void processElement(TypeElement typeElement) {
        processingEnv.getMessager().printMessage(NOTE, "generating builder for " + typeElement);
        Name builderPackager = processingEnv.getElementUtils().getPackageOf(typeElement).getQualifiedName();

        try {
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(builderPackager + "." + typeElement.getSimpleName() + "Builder");
            TypeSpec builderClass = BuilderGenerator.generateBuilder(typeElement, builderPackager);
            JavaFile builderFile = JavaFile.builder(builderPackager.toString(), builderClass).build();
            try (Writer out = sourceFile.openWriter()) {
                builderFile.writeTo(out);
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(ERROR, "error during builder generation: " + e.getMessage());
        }
    }
}
