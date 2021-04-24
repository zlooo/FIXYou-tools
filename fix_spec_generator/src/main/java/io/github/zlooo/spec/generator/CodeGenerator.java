package io.github.zlooo.spec.generator;

import com.squareup.javapoet.*;
import io.github.zlooo.fixyou.model.ApplicationVersionID;
import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;

import javax.lang.model.element.Modifier;
import java.util.*;

class CodeGenerator {

    public static final int PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR = 4;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";
    private static final String ARRAY_ENDING = "}";
    private static final char BACKSLASH = '\'';
    private static final String RETURN = "return ";

    JavaFile generateFixSpecSourceCode(DictionaryFileProcessor.Result procssingResult, String packageName) {
        final LinkedHashMap<Integer, FieldType> bodyFieldNumberToType = procssingResult.getBodyFieldsNumbersToTypes();
        final Integer[] bodyFieldNumbers = new Integer[bodyFieldNumberToType.size()];
        final FieldType[] bodyFieldTypes = new FieldType[bodyFieldNumberToType.size()];
        fillFieldNumbersAndTypes(bodyFieldNumberToType, bodyFieldNumbers, bodyFieldTypes);

        final LinkedHashMap<Integer, FieldType> headerFieldNumberToType = procssingResult.getHeaderFieldsNumberToTypes();
        final Integer[] headerFieldNumbers = new Integer[headerFieldNumberToType.size()];
        final FieldType[] headerFieldTypes = new FieldType[headerFieldNumberToType.size()];
        fillFieldNumbersAndTypes(headerFieldNumberToType, headerFieldNumbers, headerFieldTypes);

        final FieldSpec headerFieldsOrderStaticField = fieldsOrderField(headerFieldNumbers, "HEADER_FIELDS_ORDER");
        final FieldSpec headerFieldTypesStaticField = fieldsTypeField(headerFieldTypes, "HEADER_FIELD_TYPES");
        final FieldSpec bodyFieldsOrderStaticField = fieldsOrderField(bodyFieldNumbers, "BODY_FIELDS_ORDER");
        final FieldSpec bodyFieldTypesStaticField = fieldsTypeField(bodyFieldTypes, "BODY_FIELD_TYPES");
        return JavaFile.builder(packageName, TypeSpec.classBuilder("FixSpec")
                                                     .addField(emptyFieldNumberTypeArrayField())
                                                     .addField(repeatingGroupField())
                                                     .addField(headerFieldsOrderStaticField)
                                                     .addField(headerFieldTypesStaticField)
                                                     .addField(bodyFieldsOrderStaticField)
                                                     .addField(bodyFieldTypesStaticField)
                                                     .addField(messageTypesField(procssingResult.getMessageTypes()))
                                                     .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                                     .addSuperinterface(FixSpec.class)
                                                     .addMethod(createRepeatingGroupsMethod(procssingResult.getRepeatingGroups()))
                                                     .addMethod(getFieldsOrderMethod("getHeaderFieldsOrder", headerFieldsOrderStaticField.name))
                                                     .addMethod(getFieldsOrderMethod("getBodyFieldsOrder", bodyFieldsOrderStaticField.name))
                                                     .addMethod(getFieldTypesMethod("getHeaderFieldTypes", headerFieldTypesStaticField.name))
                                                     .addMethod(getFieldTypesMethod("getBodyFieldTypes", bodyFieldTypesStaticField.name))
                                                     .addMethod(getMessageTypesMethod())
                                                     .addMethod(getApplicationVersionIdMethod(procssingResult.getApplicationVersionID()))
                                                     .addMethod(getRepeatingGroupFieldNumbersMethod())
                                                     .build()).build();
    }

    private void fillFieldNumbersAndTypes(LinkedHashMap<Integer, FieldType> fieldNumberToType, Integer[] fieldNumbers, FieldType[] fieldTypes) {
        final Iterator<Map.Entry<Integer, FieldType>> fieldNumberToTypeIterator = fieldNumberToType.entrySet().iterator();
        for (int i = 0; fieldNumberToTypeIterator.hasNext(); i++) {
            final Map.Entry<Integer, FieldType> entry = fieldNumberToTypeIterator.next();
            fieldNumbers[i] = entry.getKey();
            fieldTypes[i] = entry.getValue();
        }
    }

    private FieldSpec emptyFieldNumberTypeArrayField() {
        return FieldSpec.builder(FixSpec.FieldNumberType[].class, "EMPTY_FIELD_NUMBER_TYPE_ARRAY", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("new $T[0]", FixSpec.FieldNumberType.class)
                        .build();
    }

    private FieldSpec messageTypesField(Set<String> messageTypes) {
        final StringBuilder initializer = new StringBuilder("new char[][]{");
        final Iterator<String> messageTypeIterator = messageTypes.iterator();
        while (messageTypeIterator.hasNext()) {
            initializer.append(toCharArray(messageTypeIterator.next()));
            if (messageTypeIterator.hasNext()) {
                initializer.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        initializer.append(ARRAY_ENDING);
        return FieldSpec.builder(char[][].class, "MESSAGE_TYPES", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.toString())
                        .build();
    }

    private FieldSpec fieldsOrderField(Integer[] fieldNumbers, String fieldName) {
        final StringBuilder initializer = new StringBuilder("new int[]{");
        for (int i = 0; i < fieldNumbers.length; i++) {
            initializer.append(fieldNumbers[i]);
            if (i < fieldNumbers.length - 1) {
                initializer.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        initializer.append(ARRAY_ENDING);
        return FieldSpec.builder(int[].class, fieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.toString())
                        .build();
    }

    private FieldSpec fieldsTypeField(FieldType[] fieldNumbers, String fieldName) {
        final StringBuilder initializer = new StringBuilder("new $T[]{");
        for (int i = 0; i < fieldNumbers.length; i++) {
            initializer.append("FieldType.").append(fieldNumbers[i]);
            if (i < fieldNumbers.length - 1) {
                initializer.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        initializer.append(ARRAY_ENDING);
        return FieldSpec.builder(FieldType[].class, fieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.toString(), FieldType.class)
                        .build();
    }

    private static MethodSpec createRepeatingGroupsMethod(Map<Integer, FixSpec.FieldNumberType[]> repeatingGroups) {
        final MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("createRepeatingGroups")
                                                               .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                                               .returns(ParameterizedTypeName.get(Map.class, Integer.class, FixSpec.FieldNumberType[].class))
                                                               .addStatement("final $T<$T, $T[]> result = new $T<>($L)", Map.class, Integer.class, FixSpec.FieldNumberType.class, HashMap.class, repeatingGroups.size());
        for (final Map.Entry<Integer, FixSpec.FieldNumberType[]> entry : repeatingGroups.entrySet()) {
            final FixSpec.FieldNumberType[] values = entry.getValue();
            final StringBuilder singleGroupStatementBuilder = new StringBuilder("result.put($L, new $T[]{");
            for (int i = 0; i < values.length; i++) {
                singleGroupStatementBuilder.append(CodeBlock.of("new $T($L, $T.$L)", FixSpec.FieldNumberType.class, values[i].getNumber(), FieldType.class, values[i].getType()).toString());
                if (i < values.length - 1) {
                    singleGroupStatementBuilder.append(ARRAY_ELEMENT_SEPARATOR);
                }
            }
            methodSpecBuilder.addStatement(singleGroupStatementBuilder.append("})").toString(), entry.getKey(), FixSpec.FieldNumberType.class);
        }
        return methodSpecBuilder.addStatement("return result").build();
    }

    private static FieldSpec repeatingGroupField() {
        return FieldSpec.builder(ParameterizedTypeName.get(Map.class, Integer.class, FixSpec.FieldNumberType[].class), "REPEATING_GROUPS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("createRepeatingGroups()")
                        .build();
    }

    private static MethodSpec getRepeatingGroupFieldNumbersMethod() {
        return MethodSpec.methodBuilder("getRepeatingGroupFieldNumbers")
                         .addModifiers(Modifier.PUBLIC)
                         .addAnnotation(Override.class)
                         .addParameter(int.class, "groupNumber")
                         .returns(FixSpec.FieldNumberType[].class)
                         .addStatement("return REPEATING_GROUPS.getOrDefault(groupNumber, EMPTY_FIELD_NUMBER_TYPE_ARRAY)")
                         .build();
    }

    private static MethodSpec getApplicationVersionIdMethod(ApplicationVersionID applicationVersionID) {
        return MethodSpec.methodBuilder("applicationVersionId")
                         .addModifiers(Modifier.PUBLIC)
                         .addAnnotation(Override.class)
                         .returns(ApplicationVersionID.class)
                         .addStatement("return $T.$L", ApplicationVersionID.class, applicationVersionID)
                         .build();
    }

    private static MethodSpec getMessageTypesMethod() {
        return MethodSpec.methodBuilder("getMessageTypes").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(char[][].class).addStatement("return MESSAGE_TYPES").build();
    }

    private static String toCharArray(String elementToCovnert) {
        final char[] chars = elementToCovnert.toCharArray();
        final StringBuilder charArray = new StringBuilder("{");
        for (int i = 0; i < chars.length; i++) {
            charArray.append(BACKSLASH).append(chars[i]).append(BACKSLASH);
            if (i < chars.length - 1) {
                charArray.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        return charArray.append('}').toString();
    }

    private static MethodSpec getFieldsOrderMethod(String methodName, String fieldName) {
        return MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(int[].class).addStatement(RETURN + fieldName).build();
    }

    private static MethodSpec getFieldTypesMethod(String methodName, String fieldName) {
        return MethodSpec.methodBuilder(methodName).addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(FieldType[].class).addStatement(RETURN + fieldName).build();
    }
}
