package io.github.zlooo.spec.generator;

import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        for (final Map.Entry<Integer, int[]> entry : processingResult.getRepeatingGroups().entrySet()) {
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
            final Map<Integer, int[]> repeatingGroups = result1.getRepeatingGroups();
            for (final Map.Entry<Integer, int[]> entry : result2.getRepeatingGroups().entrySet()) {
                final int[] repeatingGroupFieldNumbers = repeatingGroups.get(entry.getKey());
                if (repeatingGroupFieldNumbers == null) {
                    repeatingGroups.put(entry.getKey(), entry.getValue());
                } else if (!Arrays.equals(repeatingGroupFieldNumbers, entry.getValue())) {
                    log.warn("The same repeating group defined in multiple files but they contain different fields, they will be merged. Group number {}, definition 1 {}, definition 2 {}", entry.getKey(),
                             Arrays.toString(repeatingGroupFieldNumbers),
                             Arrays.toString(entry.getValue()));
                    final List<Integer> mergeResult = new ArrayList<>(); //List not set because we want to preserve order
                    Arrays.stream(entry.getValue()).forEach(mergeResult::add);
                    Arrays.stream(repeatingGroupFieldNumbers).forEach(mergeResult::add);
                    repeatingGroups.put(entry.getKey(), mergeResult.stream().distinct().mapToInt(Integer::intValue).toArray());
                }
            }
            return result1;
        };
    }
}
