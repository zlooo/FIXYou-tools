package io.github.zlooo.performance.tester.fixyou;

import io.github.zlooo.fixyou.model.ApplicationVersionID;
import io.github.zlooo.fixyou.model.FieldType;
import io.github.zlooo.fixyou.model.FixSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FixSpec50SP2Min implements FixSpec {

    private static final int[] FIELDS_ORDER = {8, 9, 35, 34, 49, 52, 56, 11, 14, 17, 37, 39, 40, 54, 55, 60, 150, 151, 98, 108, 141, 1137, 10};
    private static final char[][] MESSAGE_TYPES = {{'0'}, {'8'}, {'D'}};

    @Override
    public int[] getFieldsOrder() {
        return FIELDS_ORDER;
    }

    @Nonnull
    @Override
    public char[][] getMessageTypes() {
        return MESSAGE_TYPES;
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

    @Nonnull
    @Override
    public int[] getRepeatingGroupFieldNumbers(int i) {
        return new int[0];
    }
}
