package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.model.ApplicationVersionID;
import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;
import io.github.zlooo.fixyou.parser.FixFieldsTypes;
import io.github.zlooo.fixyou.utils.ArrayUtils;

import javax.annotation.Nonnull;

public class FixSpec50SP2Min implements FixSpec {

    private static final int[] HEADER_FIELDS_ORDER = {8, 9, 35, 34, 49, 52, 56};
    private static final int[] BODY_FIELDS_ORDER = {11, 14, 17, 37, 39, 40, 54, 55, 60, 150, 151, 98, 108, 141, 1137, 10};
    private static final FieldType[] HEADER_FIELDS_TYPES =
            {FixFieldsTypes.BEGIN_STRING, FixFieldsTypes.BODY_LENGTH, FixFieldsTypes.MESSAGE_TYPE, FixFieldsTypes.MESSAGE_SEQUENCE_NUMBER, FixFieldsTypes.SENDER_COMP_ID, FixFieldsTypes.SENDING_TIME, FixFieldsTypes.TARGET_COMP_ID};
    private static final FieldType[] BODY_FIELDS_TYPES =
            {FixFieldsTypes.CLORD_ID, FieldType.DOUBLE, FieldType.CHAR_ARRAY, FieldType.CHAR_ARRAY, FieldType.CHAR, FieldType.CHAR, FieldType.CHAR, FieldType.CHAR_ARRAY, FieldType.TIMESTAMP, FieldType.CHAR, FieldType.DOUBLE, FieldType.LONG,
                    FieldType.LONG, FieldType.BOOLEAN, FieldType.CHAR_ARRAY, FixFieldsTypes.CHECK_SUM};
    private static final char[][] MESSAGE_TYPES = {{'0'}, {'8'}, {'D'}};

    @Nonnull
    @Override
    public int[] getHeaderFieldsOrder() {
        return HEADER_FIELDS_ORDER;
    }

    @Nonnull
    @Override
    public FieldType[] getHeaderFieldTypes() {
        return HEADER_FIELDS_TYPES;
    }

    @Nonnull
    @Override
    public int[] getBodyFieldsOrder() {
        return BODY_FIELDS_ORDER;
    }

    @Nonnull
    @Override
    public FieldType[] getBodyFieldTypes() {
        return BODY_FIELDS_TYPES;
    }

    @Nonnull
    @Override
    public char[][] getMessageTypes() {
        return MESSAGE_TYPES;
    }

    @Nonnull
    @Override
    public ApplicationVersionID applicationVersionId() {
        return ApplicationVersionID.FIX50SP2;
    }

    @Nonnull
    @Override
    public FieldNumberType[] getRepeatingGroupFieldNumbers(int i) {
        return ArrayUtils.EMPTY_FIELD_NUMBER_TYPE;
    }
}
