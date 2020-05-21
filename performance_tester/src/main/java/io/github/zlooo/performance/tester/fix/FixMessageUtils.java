package io.github.zlooo.performance.tester.fix;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FixMessageUtils {

    private static final char[] CLORDID_PREFIX = new char[]{'1', '1', '='};
    private static final char SOH = '\u0001';

    public static String getClordid(String fixMessage) {
        int index = -1;
        boolean sequenceFound = false;
        int indexInSequence = 0;
        int startingIndex = 0;
        int endingIndex = 0;
        for (final char singleChar : fixMessage.toCharArray()) {
            index++;
            if (!sequenceFound && singleChar == CLORDID_PREFIX[indexInSequence]) {
                indexInSequence++;
                if (indexInSequence == CLORDID_PREFIX.length) {
                    sequenceFound = true;
                    startingIndex = index + 1;
                }
            } else {
                indexInSequence = 0;
            }
            if (sequenceFound && singleChar == SOH) {
                endingIndex = index;
                break;
            }
        }
        return fixMessage.substring(startingIndex, endingIndex);
    }
}
