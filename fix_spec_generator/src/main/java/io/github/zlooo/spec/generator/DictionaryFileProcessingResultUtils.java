package io.github.zlooo.spec.generator;

import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import io.github.zlooo.spec.generator.xml.DictionaryFileProcessor;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BinaryOperator;

@Slf4j
@UtilityClass
class DictionaryFileProcessingResultUtils {

    static void validate(DictionaryFileProcessor.Result processingResult) {
        if (processingResult.getMessageTypes().contains(null)) {
            throw new FixSpecGeneratorException("Message types cannot contain null values");
        }
        validateFieldNumberTypes("Header", processingResult.getHeaderFieldsNumberToTypes());
        validateFieldNumberTypes("Body", processingResult.getBodyFieldsNumbersToTypes());
        Objects.requireNonNull(processingResult.getApplicationVersionID(), "Application version id must be set");
        validateRepeatingGroups(processingResult);
    }

    private static void validateFieldNumberTypes(String fieldNumbersType, LinkedHashMap<Integer, FieldType> fieldsNumbersToTypes) {
        for (final Map.Entry<Integer, FieldType> entry : fieldsNumbersToTypes.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new FixSpecGeneratorException(fieldNumbersType + " field number to types map cannot contain null key or value, entry inspected " + entry);
            }
        }
    }

    private static void validateRepeatingGroups(DictionaryFileProcessor.Result processingResult) {
        for (final Map.Entry<Integer, FixSpec.FieldNumberType[]> entry : processingResult.getRepeatingGroups().entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue().length < 1) {
                throw new FixSpecGeneratorException("Repeating group definitions cannot contain null key or value. Also value's length must be greater than 0, entry inspected " + entry.getKey() + "=" + Arrays.toString(entry.getValue()));
            }
        }
    }

    static BinaryOperator<DictionaryFileProcessor.Result> resultAccumulator() {
        return (result1, result2) -> {
            result1.getMessageTypes().addAll(result2.getMessageTypes());
            mergeFieldNumberToTypeMap(result1.getHeaderFieldsNumberToTypes(), result2.getHeaderFieldsNumberToTypes());
            mergeFieldNumberToTypeMap(result1.getBodyFieldsNumbersToTypes(), result2.getBodyFieldsNumbersToTypes());
            result1.getHeaderFieldsNumberToTypes().forEach(result1.getBodyFieldsNumbersToTypes()::remove);
            if (result1.getApplicationVersionID() != result2.getApplicationVersionID()) {
                throw new FixSpecGeneratorException("Different application version ids defined in multiple files! Versions received " + result1.getApplicationVersionID() + " and " + result2.getApplicationVersionID());
            }
            final Map<Integer, FixSpec.FieldNumberType[]> repeatingGroups = result1.getRepeatingGroups();
            for (final Map.Entry<Integer, FixSpec.FieldNumberType[]> entry : result2.getRepeatingGroups().entrySet()) {
                final FixSpec.FieldNumberType[] repeatingGroupFieldNumberTypes = repeatingGroups.get(entry.getKey());
                if (repeatingGroupFieldNumberTypes == null) {
                    repeatingGroups.put(entry.getKey(), entry.getValue());
                } else if (!Arrays.equals(repeatingGroupFieldNumberTypes, entry.getValue())) {
                    log.warn("The same repeating group defined in multiple files but they contain different fields, they will be merged. Group number {}, definition 1 {}, definition 2 {}", entry.getKey(),
                             Arrays.toString(repeatingGroupFieldNumberTypes),
                             Arrays.toString(entry.getValue()));
                    final List<FixSpec.FieldNumberType> mergeResult = new ArrayList<>(); //List not set because we want to preserve order
                    mergeResult.addAll(Arrays.asList(entry.getValue()));
                    mergeResult.addAll(Arrays.asList(repeatingGroupFieldNumberTypes));
                    repeatingGroups.put(entry.getKey(), mergeResult.stream().distinct().toArray(FixSpec.FieldNumberType[]::new));
                }
            }
            return result1;
        };
    }

    private static void mergeFieldNumberToTypeMap(Map<Integer, FieldType> destinationMap, Map<Integer, FieldType> sourceMap) {
        for (final Map.Entry<Integer, FieldType> entry : sourceMap.entrySet()) {
            final FieldType fieldType = destinationMap.get(entry.getKey());
            if (fieldType != null && fieldType != entry.getValue()) {
                throw new FixSpecGeneratorException("Same field in multiple files defined but with different types? Field number " + entry.getKey() + ", types " + fieldType + ", " + entry.getValue());
            } else {
                destinationMap.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
