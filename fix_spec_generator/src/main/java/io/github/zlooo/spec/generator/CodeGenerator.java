package io.github.zlooo.spec.generator;

import com.squareup.javapoet.*;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;
import pl.zlooo.fixyou.model.ApplicationVersionID;
import pl.zlooo.fixyou.model.FieldType;
import pl.zlooo.fixyou.model.FixSpec;

import javax.lang.model.element.Modifier;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

class CodeGenerator {

    public static final int PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR = 4;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";
    private static final String ARRAY_ENDING = "}";
    private static final char BACKSLASH = '\'';
    private static final int OFFSET_ONE = 1;
    private static final int OFFSET_TWO = 2;
    private static final int OFFSET_THREE = 3;

    JavaFile generateFixSpecSourceCode(DictionaryFileProcessor.Result procssingResult, String packageName) {
        final Map<Integer, FieldType> fieldNumberToType = procssingResult.getFieldNumbersToTypes();
        final Integer[] fieldNumbers = new Integer[fieldNumberToType.size()];
        final FieldType[] fieldTypes = new FieldType[fieldNumberToType.size()];
        final Iterator<Map.Entry<Integer, FieldType>> fieldNumberToTypeIterator = fieldNumberToType.entrySet().iterator();
        for (int i = 0; fieldNumberToTypeIterator.hasNext(); i++) {
            final Map.Entry<Integer, FieldType> entry = fieldNumberToTypeIterator.next();
            fieldNumbers[i] = entry.getKey();
            fieldTypes[i] = entry.getValue();
        }
        return JavaFile.builder(packageName, TypeSpec.classBuilder("FixSpec").addField(repeatingGroupField(procssingResult.getRepeatingGroups()))
                                                     .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                                     .addSuperinterface(FixSpec.class)
                                                     .addMethod(createRepeatingGroupsMethod(procssingResult.getRepeatingGroups()))
                                                     .addMethod(getFieldsOrderMethod(fieldNumbers))
                                                     .addMethod(getTypesMethod(fieldTypes))
                                                     .addMethod(getMessageTypesMethod(procssingResult.getMessageTypes()))
                                                     .addMethod(getHighestFieldNumberMethod(fieldNumbers))
                                                     .addMethod(getApplicationVersionIdMethod(procssingResult.getApplicationVersionID()))
                                                     .addMethod(getChildPairSpecMethod())
                                                     .build()).build();
    }

    private static MethodSpec createRepeatingGroupsMethod(Map<Integer, FixSpec.FieldNumberTypePair[]> repeatingGroups) {
        final MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("createRepeatingGroups")
                                                               .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                                               .returns(ParameterizedTypeName.get(Map.class, Integer.class, FixSpec.FieldNumberTypePair[].class))
                                                               .addStatement("final $T<Integer, FixSpec.FieldNumberTypePair[]> result = new $T<>($L)", Map.class, HashMap.class, repeatingGroups.size());
        for (final Map.Entry<Integer, FixSpec.FieldNumberTypePair[]> entry : repeatingGroups.entrySet()) {
            final FixSpec.FieldNumberTypePair[] values = entry.getValue();
            final Object[] args = new Object[2 + PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR * values.length];
            args[0] = entry.getKey();
            args[1] = FixSpec.FieldNumberTypePair.class;
            final StringBuilder singleGroupStatementBuilder = new StringBuilder("result.put($L, new $T[]{");
            for (int i = 0; i < values.length; i++) {
                singleGroupStatementBuilder.append("new $T($T.$L, $L)");
                args[2 + PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR * i] = FixSpec.FieldNumberTypePair.class;
                args[2 + PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR * i + OFFSET_ONE] = FieldType.class;
                final FixSpec.FieldNumberTypePair value = values[i];
                args[2 + PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR * i + OFFSET_TWO] = value.getFieldType();
                args[2 + PARAM_COUNT_PER_FIELD_NUMBER_TYPE_PAIR * i + OFFSET_THREE] = value.getFieldNumber();
                if (i < values.length - 1) {
                    singleGroupStatementBuilder.append(ARRAY_ELEMENT_SEPARATOR);
                }
            }
            methodSpecBuilder.addStatement(singleGroupStatementBuilder.append("})").toString(), args);
        }
        return methodSpecBuilder.addStatement("return result").build();
    }

    private static FieldSpec repeatingGroupField(Map<Integer, FixSpec.FieldNumberTypePair[]> repeatingGroups) {
        return FieldSpec.builder(ParameterizedTypeName.get(Map.class, Integer.class, FixSpec.FieldNumberTypePair[].class), "REPEATING_GROUPS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("createRepeatingGroups()")
                        .build();
    }

    private static MethodSpec getChildPairSpecMethod() {
        return MethodSpec.methodBuilder("getChildPairSpec")
                         .addModifiers(Modifier.PUBLIC)
                         .addAnnotation(Override.class)
                         .addParameter(int.class, "groupNumber")
                         .returns(FixSpec.FieldNumberTypePair[].class)
                         .addStatement("return REPEATING_GROUPS.get(groupNumber)")
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

    private static MethodSpec getHighestFieldNumberMethod(Integer[] fieldNumbers) {
        final int highestFieldNumber = Stream.of(fieldNumbers).mapToInt(Integer::intValue).max().orElseThrow(() -> new FixSpecGeneratorException("Empty stream, seriously? Get your shit together!!!!"));
        return MethodSpec.methodBuilder("highestFieldNumber").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(int.class).addStatement("return " + highestFieldNumber).build();
    }

    private static MethodSpec getMessageTypesMethod(Set<String> messageTypes) {
        final StringBuilder returnStatement = new StringBuilder("return new char[][]{");
        final Iterator<String> messageTypeIterator = messageTypes.iterator();
        while (messageTypeIterator.hasNext()) {
            returnStatement.append(toCharArray(messageTypeIterator.next()));
            if (messageTypeIterator.hasNext()) {
                returnStatement.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        returnStatement.append(ARRAY_ENDING);
        return MethodSpec.methodBuilder("getMessageTypes").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(char[][].class).addStatement(returnStatement.toString()).build();
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

    private static MethodSpec getTypesMethod(FieldType[] fieldTypes) {
        final StringBuilder returnStatement = new StringBuilder("return new $T[]{");
        final Object[] args = new Object[2 * fieldTypes.length + 1];
        args[0] = FieldType.class;
        for (int i = 0; i < fieldTypes.length; i++) {
            returnStatement.append("$T.$L");
            if (i < fieldTypes.length - 1) {
                returnStatement.append(ARRAY_ELEMENT_SEPARATOR);
            }
            args[1 + 2 * i] = FieldType.class;
            args[1 + 2 * i + 1] = fieldTypes[i];
        }
        returnStatement.append(ARRAY_ENDING);
        return MethodSpec.methodBuilder("getTypes").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(FieldType[].class).addStatement(returnStatement.toString(), args).build();
    }

    private static MethodSpec getFieldsOrderMethod(Integer[] fieldNumbers) {
        final StringBuilder returnStatement = new StringBuilder("return new int[]{");
        for (int i = 0; i < fieldNumbers.length; i++) {
            returnStatement.append(fieldNumbers[i]);
            if (i < fieldNumbers.length - 1) {
                returnStatement.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        returnStatement.append(ARRAY_ENDING);
        return MethodSpec.methodBuilder("getFieldsOrder").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(int[].class).addStatement(returnStatement.toString()).build();
    }
}
