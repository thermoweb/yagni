package fr.yagni.core.dto;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

public class BuilderGenerator {

    private BuilderGenerator() {

    }

    public static TypeSpec generateBuilder(TypeElement typeElement, Name builderPackager) {
        ClassName className = ClassName.get(typeElement);
        MethodSpec.Builder buildMethod = MethodSpec
                .methodBuilder("build")
                .returns(className);
        String builderName = typeElement.getSimpleName() + "Builder";
        TypeSpec.Builder builderClass = TypeSpec
                .classBuilder(builderName)
                .addTypeVariables(typeElement.getTypeParameters().stream().map(t -> TypeVariableName.get(t.getSimpleName().toString())).toList())
                .addModifiers(Modifier.PUBLIC);

        List<BuilderField> fields = getFields(typeElement);
        for (BuilderField field : fields) {
            builderClass.addMethods(getFieldMethods(builderPackager, field, builderName));
            buildMethod.addStatement("$T.requireNonNull($N, \"$N should be set\")", Objects.class, field.fieldSpec(), field.fieldSpec());
            builderClass.addField(field.fieldSpec());
        }

        return builderClass
                .addMethod(buildMethod
                        .addStatement("return new $T($L)", className, CodeBlock.join(fields.stream().map(f -> CodeBlock.of("$N.orElse(null)", f.fieldSpec().name)).toList(), ", "))
                        .build())
                .build();
    }

    private static List<MethodSpec> getFieldMethods(Name builderPackager, BuilderField field, String builderName) {
        if (field.isNullable()) {
            return List.of(createFieldSetter(builderPackager, field, builderName),
                    createFieldWithout(builderPackager, field, builderName));
        }
        return List.of(createFieldSetter(builderPackager, field, builderName));
    }

    private static MethodSpec createFieldWithout(Name builderPackager, BuilderField field, String builderName) {
        return MethodSpec
                .methodBuilder("without" + capitalize(field.fieldSpec().name))
                .addStatement("this.$N = Optional.empty()", field.fieldSpec())
                .addStatement("return this")
                .returns(ClassName.get(builderPackager.toString(), builderName))
                .addModifiers(Modifier.PUBLIC).build();
    }

    private static MethodSpec createFieldSetter(Name builderPackager, BuilderField field, String builderName) {
        return MethodSpec
                .methodBuilder(field.fieldSpec().name)
                .addParameter(field.parameter())
                .addStatement("this.$N = Optional.of($N)", field.fieldSpec(), field.parameter())
                .addStatement("return this")
                .returns(ClassName.get(builderPackager.toString(), builderName))
                .addModifiers(Modifier.PUBLIC).build();
    }

    private static List<BuilderField> getFields(TypeElement typeElement) {
        if (typeElement.getKind() == ElementKind.RECORD) {
            return typeElement.getRecordComponents()
                    .stream()
                    .map(BuilderGenerator::getField)
                    .toList();
        } else if (typeElement.getKind() == ElementKind.CLASS) {
            return typeElement.getEnclosedElements()
                    .stream()
                    .filter(f -> f.getKind() == ElementKind.FIELD)
                    .map(BuilderGenerator::getField)
                    .toList();
        }
        throw new IllegalStateException();
    }

    private static BuilderField getField(Element element) {
        return new BuilderField(
                FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(Optional.class), TypeName.get(element.asType())), element.getSimpleName().toString(), Modifier.PRIVATE).build(),
                ParameterSpec.builder(TypeName.get(element.asType()), "value").build(),
                Optional.ofNullable(element.getAnnotation(Builder.Nullable.class)).isPresent() );
    }

    private static String capitalize(final String name) {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private record BuilderField(FieldSpec fieldSpec, ParameterSpec parameter, boolean isNullable) {
    }
}
