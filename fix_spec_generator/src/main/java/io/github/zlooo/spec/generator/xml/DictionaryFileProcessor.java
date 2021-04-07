package io.github.zlooo.spec.generator.xml;

import io.github.zlooo.fixyou.model.ApplicationVersionID;
import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import io.github.zlooo.spec.generator.xml.model.ComponentType;
import io.github.zlooo.spec.generator.xml.model.FixType;
import io.github.zlooo.spec.generator.xml.model.GroupType;
import io.github.zlooo.spec.generator.xml.model.MessageType;
import lombok.Data;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO you have to rewrite this class when you have more time, because of dictionary xml structure it's becoming a mess even though it's just been created
public class DictionaryFileProcessor {

    private static final Map<String, FieldType> FIELD_TYPES = createFieldTypeMapping();

    public Result process(FixType objectToProcess) {
        final Result result = new Result();
        result.setMessageTypes(objectToProcess.getMessages().getMessage().stream().map(MessageType::getMsgtype).collect(Collectors.toSet()));
        final List<io.github.zlooo.spec.generator.xml.model.FieldType> fields = objectToProcess.getFields().getField();
        final Map<Integer, FieldType> fieldNumbersToTypes = new LinkedHashMap<>(fields.size());
        final Map<String, GroupType> nameToGroup = new HashMap<>();
        final Map<String, ComponentType> nameToComponent = new HashMap<>(objectToProcess.getComponents().getComponent().size());
        final Map<String, io.github.zlooo.spec.generator.xml.model.FieldType> nameToField = new HashMap<>(fields.size());
        for (final ComponentType component : objectToProcess.getComponents().getComponent()) {
            nameToComponent.put(component.getName(), component);
            for (final GroupType group : component.getGroup()) {
                nameToGroup.merge(group.getName(), group, groupMergingFunction());
            }
        }
        for (final io.github.zlooo.spec.generator.xml.model.FieldType field : fields) {
            nameToField.put(field.getName(), field);
        }

        setFieldNumberToTypesMapEntries(fields, fieldNumbersToTypes, nameToField);

        resolveComponents(nameToComponent);
        final Map<Integer, FixSpec.FieldNumberType[]> repeatingGroups = new HashMap<>(nameToGroup.size());
        for (final Map.Entry<String, GroupType> entry : nameToGroup.entrySet()) {
            final GroupType group = entry.getValue();
            repeatingGroups.put(nameToField.get(group.getName()).getNumber(), buildFieldNumbers(group, nameToField, nameToComponent));
        }
        result.setFieldNumbersToTypes(fieldNumbersToTypes);
        result.setApplicationVersionID(ApplicationVersionID.FIX50SP2);
        result.setRepeatingGroups(repeatingGroups);
        return result;
    }

    //TODO use <header> and <trailer> tags instead to set up beginning and ending of list of fields
    private void setFieldNumberToTypesMapEntries(List<io.github.zlooo.spec.generator.xml.model.FieldType> fields, Map<Integer, FieldType> fieldNumbersToTypes, Map<String, io.github.zlooo.spec.generator.xml.model.FieldType> nameToField) {
        final io.github.zlooo.spec.generator.xml.model.FieldType beginString = nameToField.get("BeginString");
        fieldNumbersToTypes.put(beginString.getNumber(), FIELD_TYPES.get(beginString.getType()));
        final io.github.zlooo.spec.generator.xml.model.FieldType bodyLength = nameToField.get("BodyLength");
        fieldNumbersToTypes.put(bodyLength.getNumber(), FIELD_TYPES.get(bodyLength.getType()));
        final io.github.zlooo.spec.generator.xml.model.FieldType msgType = nameToField.get("MsgType");
        fieldNumbersToTypes.put(msgType.getNumber(), FIELD_TYPES.get(msgType.getType()));
        for (final io.github.zlooo.spec.generator.xml.model.FieldType field : fields) {
            if (!fieldNumbersToTypes.containsKey(field.getNumber())) {
                fieldNumbersToTypes.put(field.getNumber(), FIELD_TYPES.get(field.getType()));
            }
        }
    }

    private BiFunction<? super GroupType, ? super GroupType, ? extends GroupType> groupMergingFunction() {
        return (group1, group2) -> {
            addItemsThatAreNotPresent(group1.getComponent(), group2.getComponent(), ComponentType::getName);
            addItemsThatAreNotPresent(group1.getField(), group2.getField(), io.github.zlooo.spec.generator.xml.model.FieldType::getName);
            return group1;
        };
    }

    private static <T> void addItemsThatAreNotPresent(List<T> list1, List<T> list2, Function<T, String> nameExtractor) {
        final Set<String> namesInList1 = list1.stream().map(nameExtractor).collect(Collectors.toSet());
        for (final T itemFromList2 : list2) {
            if (!namesInList1.contains(nameExtractor.apply(itemFromList2))) {
                list1.add(itemFromList2);
            }
        }
    }

    private void resolveComponents(Map<String, ComponentType> nameToComponent) {
        final Set<String> resolvedComponentNames = new HashSet<>(nameToComponent.size());
        for (final ComponentType component : nameToComponent.values()) {
            resolveSingleComponent(component, nameToComponent, resolvedComponentNames);
        }
    }

    private void resolveSingleComponent(ComponentType component, Map<String, ComponentType> nameToComponent, Set<String> resolvedComponentNames) {
        for (final ComponentType nestedComponent : component.getComponent()) {
            if (resolvedComponentNames.contains(nestedComponent.getName())) {
                final ComponentType lookedUpComponent = nameToComponent.get(nestedComponent.getName());
                component.getField().addAll(lookedUpComponent.getField());
                component.getGroup().addAll(lookedUpComponent.getGroup());
            } else {
                resolveSingleComponent(nestedComponent, nameToComponent, resolvedComponentNames);
            }
        }
        resolvedComponentNames.add(component.getName());
    }

    private static FixSpec.FieldNumberType[] buildFieldNumbers(GroupType groupToProcess, Map<String, io.github.zlooo.spec.generator.xml.model.FieldType> nameToField,
                                                               Map<String, ComponentType> nameToComponent) {
        final List<FixSpec.FieldNumberType> result = new ArrayList<>();
        for (final ComponentType component : groupToProcess.getComponent()) {
            final ComponentType lookedUpComponent = nameToComponent.get(component.getName());
            lookedUpComponent.getField().stream().map(field -> nameToField.get(field.getName())).map(xmlModel -> new FixSpec.FieldNumberType(xmlModel.getNumber(), FIELD_TYPES.get(xmlModel.getType()))).forEach(result::add);
            lookedUpComponent.getGroup().stream().map(group -> nameToField.get(group.getName())).map(xmlModel -> new FixSpec.FieldNumberType(xmlModel.getNumber(), FIELD_TYPES.get(xmlModel.getType()))).forEach(result::add);
        }
        groupToProcess.getField().stream().map(field -> nameToField.get(field.getName())).map(xmlModel -> new FixSpec.FieldNumberType(xmlModel.getNumber(), FIELD_TYPES.get(xmlModel.getType()))).forEach(result::add);
        return result.toArray(new FixSpec.FieldNumberType[0]);
    }

    private static Map<String, FieldType> createFieldTypeMapping() {
        final Map<String, FieldType> result = new HashMap<String, FieldType>() {
            @Override
            public FieldType get(Object key) {
                final FieldType getResult = super.get(key);
                if (getResult == null) {
                    throw new NoSuchElementException("Could not find element " + key + " in map " + this);
                }
                return getResult;
            }
        };
        result.put("INT", FieldType.LONG);
        result.put("LENGTH", FieldType.LONG);
        result.put("NUMINGROUP", FieldType.GROUP);
        result.put("SEQNUM", FieldType.LONG);
        result.put("TAGNUM", FieldType.LONG);
        result.put("DAYOFMONTH", FieldType.LONG);
        result.put("FLOAT", FieldType.DOUBLE);
        result.put("QTY", FieldType.DOUBLE);
        result.put("PRICE", FieldType.DOUBLE);
        result.put("PRICEOFFSET", FieldType.DOUBLE);
        result.put("AMT", FieldType.DOUBLE);
        result.put("PERCENTAGE", FieldType.DOUBLE);
        result.put("CHAR", FieldType.CHAR);
        result.put("BOOLEAN", FieldType.BOOLEAN);
        result.put("STRING", FieldType.CHAR_ARRAY);
        result.put("MULTIPLECHARVALUE", FieldType.CHAR_ARRAY);
        result.put("MULTIPLEVALUESTRING", FieldType.CHAR_ARRAY);
        result.put("MULTIPLESTRINGVALUE", FieldType.CHAR_ARRAY);
        result.put("COUNTRY", FieldType.CHAR_ARRAY);
        result.put("LANGUAGE", FieldType.CHAR_ARRAY);
        result.put("CURRENCY", FieldType.CHAR_ARRAY);
        result.put("EXCHANGE", FieldType.CHAR_ARRAY);
        result.put("MONTHYEAR", FieldType.CHAR_ARRAY);
        result.put("UTCTIMESTAMP", FieldType.TIMESTAMP);
        result.put("UTCTIMEONLY", FieldType.CHAR_ARRAY);
        result.put("UTCDATEONLY", FieldType.CHAR_ARRAY);
        result.put("LOCALMKTDATE", FieldType.CHAR_ARRAY);
        result.put("TZTIMEONLY", FieldType.CHAR_ARRAY);
        result.put("TZTIMESTAMP", FieldType.CHAR_ARRAY);
        result.put("DATA", FieldType.CHAR_ARRAY);
        result.put("XMLDATA", FieldType.CHAR_ARRAY);
        return result;
    }

    @Data
    public static final class Result {
        private Set<String> messageTypes;
        private Map<Integer, FieldType> fieldNumbersToTypes;
        private ApplicationVersionID applicationVersionID;
        private Map<Integer, FixSpec.FieldNumberType[]> repeatingGroups;
    }
}
