package io.github.zlooo.performance.tester.scenario;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

@UtilityClass
public class SumupOperations {

    public static final String EXECUTION_TIME_CAP_GROUP_NAME = "time";
    public static final Pattern SUMUP_PATTERN = Pattern.compile("^\\D+(?<samples>\\d+)\\D+(?<" + EXECUTION_TIME_CAP_GROUP_NAME + ">\\d+)\\D+");
    private static final String SUMUP_TEMPLATE = "Sumup: scenario \"{}\" executed with {} samples has taken {} ns";
    private static final Logger SUMUP_LOGGER = LoggerFactory.getLogger("sumup");

    public static void log(TestScenario.Sumup sumup) {
        SUMUP_LOGGER.info(SUMUP_TEMPLATE, sumup.getScenarioName(), sumup.getSamples(), sumup.getTimeTaken());
    }
}
