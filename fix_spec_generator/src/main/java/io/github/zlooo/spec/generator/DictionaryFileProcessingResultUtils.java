package io.github.zlooo.spec.generator;

import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BinaryOperator;

@Slf4j
@UtilityClass
class DictionaryFileProcessingResultUtils {

    static void validate(DictionaryFileProcessor.Result processingResult) {
        if (processingResult.getMessageTypes().contains(null)) {
            throw new FixSpecGeneratorException("Message types cannot contain null values");
        }
        for (final Map.Entry<Integer, FieldType> entry : processingResult.getFieldNumbersToTypes().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new FixSpecGeneratorException("Field number to types map cannot contain null key or value, entry inspected " + entry);
            }
        }

        if (processingResult.getApplicationVersionID() == null) {
            throw new FixSpecGeneratorException("Application version id must be set");
        }
        for (final Map.Entry<Integer, FixSpec.FieldNumberTypePair[]> entry : processingResult.getRepeatingGroups().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().length < 1) {
                throw new FixSpecGeneratorException("Repeating group definitions cannot contain null key or value. Also value's length must be greater than 0, entry inspected " + entry.getKey() + "=" + Arrays.toString(entry.getValue()));
            }
        }
    }

    static BinaryOperator<DictionaryFileProcessor.Result> resultAccumulator() {
        return (result1, result2) -> {
            result1.getMessageTypes().addAll(result2.getMessageTypes());
            final Map<Integer, FieldType> fieldNumberToType = result1.getFieldNumbersToTypes();
            for (final Map.Entry<Integer, FieldType> entry : result2.getFieldNumbersToTypes().entrySet()) {
                final FieldType fieldType = fieldNumberToType.get(entry.getKey());
                if (fieldType != null && fieldType != entry.getValue()) {
                    throw new FixSpecGeneratorException("Same field in multiple files defined but with different types? Field number " + entry.getKey() + ", types " + fieldType + ", " + entry.getValue());
                } else {
                    fieldNumberToType.put(entry.getKey(), entry.getValue());
                }
            }
            if (result1.getApplicationVersionID() != result2.getApplicationVersionID()) {
                throw new FixSpecGeneratorException("Different application version ids defined in multiple files! Versions received " + result1.getApplicationVersionID() + " and " + result2.getApplicationVersionID());
            }
            final Map<Integer, FixSpec.FieldNumberTypePair[]> repeatingGroups = result1.getRepeatingGroups();
            for (final Map.Entry<Integer, FixSpec.FieldNumberTypePair[]> entry : result2.getRepeatingGroups().entrySet()) {
                final FixSpec.FieldNumberTypePair[] repeatingGroupDefinition = repeatingGroups.get(entry.getKey());
                if (repeatingGroupDefinition == null) {
                    repeatingGroups.put(entry.getKey(), entry.getValue());
                } else if (!Arrays.equals(repeatingGroupDefinition, entry.getValue())) {
                    log.warn("The same repeating group defined in multiple files but they contain different fields. They will be merged if possible. Group number {}, definition 1 {}, definition 2 {}", entry.getKey(),
                             Arrays.toString(repeatingGroupDefinition),
                             Arrays.toString(entry.getValue()));
                    final Map<Integer, FixSpec.FieldNumberTypePair> mergeResult = mergeRepeatingGroupsIfPossible(entry, repeatingGroupDefinition);
                    repeatingGroups.put(entry.getKey(), mergeResult.values().toArray(new FixSpec.FieldNumberTypePair[]{}));
                }
            }
            return result1;
        };
    }

    private static Map<Integer, FixSpec.FieldNumberTypePair> mergeRepeatingGroupsIfPossible(Map.Entry<Integer, FixSpec.FieldNumberTypePair[]> entry,
                                                                                            FixSpec.FieldNumberTypePair[] repeatingGroupDefinition) {
        final Map<Integer, FixSpec.FieldNumberTypePair> mergeResult = new HashMap<>();
        for (final FixSpec.FieldNumberTypePair definition : repeatingGroupDefinition) {
            mergeResult.put(definition.getFieldNumber(), definition);
        }
        for (final FixSpec.FieldNumberTypePair definition2 : entry.getValue()) {
            final FixSpec.FieldNumberTypePair definitionFromMergingProcess = mergeResult.get(definition2.getFieldNumber());
            if (definitionFromMergingProcess == null) {
                mergeResult.put(definition2.getFieldNumber(), definition2);
            } else if (definitionFromMergingProcess.getFieldType() != definition2.getFieldType()) {
                throw new FixSpecGeneratorException(
                        "Could not merge group with number " + entry.getKey() + ", two sources define the same field but with different type, field " + definitionFromMergingProcess.getFieldNumber() + ", type 1 " +
                        definitionFromMergingProcess.getFieldType() + ", type 2 " + definition2.getFieldType());
            }
        }
        return mergeResult;
    }
}
