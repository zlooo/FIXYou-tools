package io.github.zlooo.spec.generator;

import com.squareup.javapoet.*;
import io.github.zlooo.fixyou.model.ApplicationVersionID;
import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import io.github.zlooo.fixyou.utils.ArrayUtils;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;

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

    JavaFile generateFixSpecSourceCode(DictionaryFileProcessor.Result procssingResult, String packageName) {
        final Map<Integer, FieldType> fieldNumberToType = procssingResult.getFieldNumbersToTypes();
        final Integer[] fieldNumbers = new Integer[fieldNumberToType.size()];
        final Iterator<Map.Entry<Integer, FieldType>> fieldNumberToTypeIterator = fieldNumberToType.entrySet().iterator();
        for (int i = 0; fieldNumberToTypeIterator.hasNext(); i++) {
            final Map.Entry<Integer, FieldType> entry = fieldNumberToTypeIterator.next();
            fieldNumbers[i] = entry.getKey();
        }
        return JavaFile.builder(packageName, TypeSpec.classBuilder("FixSpec").addField(repeatingGroupField())
                                                     .addField(fieldsOrderField(fieldNumbers))
                                                     .addField(messageTypesField(procssingResult.getMessageTypes()))
                                                     .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                                                     .addSuperinterface(FixSpec.class)
                                                     .addMethod(createRepeatingGroupsMethod(procssingResult.getRepeatingGroups()))
                                                     .addMethod(getFieldsOrderMethod())
                                                     .addMethod(getMessageTypesMethod())
                                                     .addMethod(getHighestFieldNumberMethod(fieldNumbers))
                                                     .addMethod(getApplicationVersionIdMethod(procssingResult.getApplicationVersionID()))
                                                     .addMethod(getRepeatingGroupFieldNumbersMethod())
                                                     .build()).build();
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

    private FieldSpec fieldsOrderField(Integer[] fieldNumbers) {
        final StringBuilder initializer = new StringBuilder("new int[]{");
        for (int i = 0; i < fieldNumbers.length; i++) {
            initializer.append(fieldNumbers[i]);
            if (i < fieldNumbers.length - 1) {
                initializer.append(ARRAY_ELEMENT_SEPARATOR);
            }
        }
        initializer.append(ARRAY_ENDING);
        return FieldSpec.builder(int[].class, "FIELDS_ORDER", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer(initializer.toString())
                        .build();
    }

    private static MethodSpec createRepeatingGroupsMethod(Map<Integer, int[]> repeatingGroups) {
        final MethodSpec.Builder methodSpecBuilder = MethodSpec.methodBuilder("createRepeatingGroups")
                                                               .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                                               .returns(ParameterizedTypeName.get(Map.class, Integer.class, int[].class))
                                                               .addStatement("final $T<$T, int[]> result = new $T<>($L)", Map.class, Integer.class, HashMap.class, repeatingGroups.size());
        for (final Map.Entry<Integer, int[]> entry : repeatingGroups.entrySet()) {
            final int[] values = entry.getValue();
            final StringBuilder singleGroupStatementBuilder = new StringBuilder("result.put($L, new int[]{");
            for (int i = 0; i < values.length; i++) {
                singleGroupStatementBuilder.append(values[i]);
                if (i < values.length - 1) {
                    singleGroupStatementBuilder.append(ARRAY_ELEMENT_SEPARATOR);
                }
            }
            methodSpecBuilder.addStatement(singleGroupStatementBuilder.append("})").toString(), entry.getKey());
        }
        return methodSpecBuilder.addStatement("return result").build();
    }

    private static FieldSpec repeatingGroupField() {
        return FieldSpec.builder(ParameterizedTypeName.get(Map.class, Integer.class, int[].class), "REPEATING_GROUPS", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                        .initializer("createRepeatingGroups()")
                        .build();
    }

    private static MethodSpec getRepeatingGroupFieldNumbersMethod() {
        return MethodSpec.methodBuilder("getRepeatingGroupFieldNumbers")
                         .addModifiers(Modifier.PUBLIC)
                         .addAnnotation(Override.class)
                         .addParameter(int.class, "groupNumber")
                         .returns(int[].class)
                         .addStatement("return REPEATING_GROUPS.getOrDefault(groupNumber, $T.EMPTY_INT_ARRAY)", ArrayUtils.class)
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

    private static MethodSpec getFieldsOrderMethod() {
        return MethodSpec.methodBuilder("getFieldsOrder").addModifiers(Modifier.PUBLIC).addAnnotation(Override.class).returns(int[].class).addStatement("return FIELDS_ORDER").build();
    }
}
