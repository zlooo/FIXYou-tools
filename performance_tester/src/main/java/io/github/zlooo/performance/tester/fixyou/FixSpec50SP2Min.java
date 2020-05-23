package io.github.zlooo.performance.tester.fixyou;

import pl.zlooo.fixyou.model.ApplicationVersionID;
import pl.zlooo.fixyou.model.FieldType;
import pl.zlooo.fixyou.model.FixSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FixSpec50SP2Min implements FixSpec {
    @Override
    public int[] getFieldsOrder() {
        return new int[]{8, 9, 35, 34, 49, 52, 56, 11, 14, 17, 37, 39, 40, 54, 55, 60, 150, 151, 98, 108, 141, 1137, 10};
    }

    @Nonnull
    @Override
    public FieldType[] getTypes() {
        return new FieldType[]{FieldType.CHAR_ARRAY, FieldType.LONG, FieldType.CHAR_ARRAY, FieldType.LONG, FieldType.CHAR_ARRAY, FieldType.CHAR_ARRAY, FieldType.CHAR_ARRAY, FieldType.CHAR_ARRAY, FieldType.DOUBLE, FieldType.CHAR_ARRAY,
                FieldType.CHAR_ARRAY, FieldType.CHAR, FieldType.CHAR, FieldType.CHAR, FieldType.CHAR_ARRAY, FieldType.CHAR_ARRAY, FieldType.CHAR, FieldType.DOUBLE, FieldType.LONG, FieldType.LONG, FieldType.BOOLEAN, FieldType.CHAR_ARRAY,
                FieldType.CHAR_ARRAY};
    }

    @Nonnull
    @Override
    public char[][] getMessageTypes() {
        return new char[][]{{'0'}, {'8'}, {'D'}};
    }

    @Override
    public int highestFieldNumber() {
        return 1137;
    }

    @Nonnull
    @Override
    public ApplicationVersionID applicationVersionId() {
        return ApplicationVersionID.FIX50SP2;
    }

    @Nullable
    @Override
    public FieldNumberTypePair[] getChildPairSpec(int i) {
        return new FieldNumberTypePair[0];
    }
}
