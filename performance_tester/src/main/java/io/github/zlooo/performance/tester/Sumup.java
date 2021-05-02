package io.github.zlooo.performance.tester;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import io.github.zlooo.performance.tester.scenario.SumupOperations;
import picocli.CommandLine;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@CommandLine.Command(name = "sumup", mixinStandardHelpOptions = true, description = {"Parses logs of performance runs and prints results"})
public class Sumup implements Callable<Integer> {

    private static final int VALUE_LENGTH = 25;
    private static final char PADDING = ' ';
    private static final String VALUES_DELIMITER = "|";
    private static final int RUN_NUMBER_LENGTH = 3;
    private static final char WARMUP_EXECUTION_SEPARATOR = '/';
    @CommandLine.Parameters(paramLabel = "LOG_FILE", description = "Location of performance run log files", arity = "1..*")
    private List<File> logFiles;
    @CommandLine.Option(names = {"-r", "--runs"}, description = "Number of runs in a log", defaultValue = "10")
    private int runs;
    private final Table<Integer, String, Long> executionTimes = TreeBasedTable.create(Comparator.naturalOrder(), Comparator.comparing(key -> Integer.parseInt(key.substring(0, key.indexOf(WARMUP_EXECUTION_SEPARATOR)).trim())));

    @Override
    public Integer call() throws Exception {
        for (final File logFile : logFiles) {
            final String executionKey = executionKey(logFile.getName());
            final Queue<Long> singleLogExecutionTimes = new LinkedList<>();
            for (final String logLine : Files.readAllLines(logFile.toPath())) {
                final Matcher matcher = SumupOperations.SUMUP_PATTERN.matcher(logLine.split("-")[1].trim());
                if (matcher.matches()) {
                    singleLogExecutionTimes.add(Long.parseLong(matcher.group(SumupOperations.EXECUTION_TIME_CAP_GROUP_NAME)));
                }
            }
            for (int i = runs; i < singleLogExecutionTimes.size(); i++) {
                singleLogExecutionTimes.remove();
            }
            final int numberOfParsedExecutionTimes = Math.min(runs, singleLogExecutionTimes.size());
            for (int i = 0; i < numberOfParsedExecutionTimes; i++) {
                executionTimes.put(i, executionKey, singleLogExecutionTimes.remove());
            }
        }
        print(executionTimes);
        return 0;
    }

    private static void print(Table<Integer, String, Long> executionTimes) {
        System.out.println("=======================================================================================================================");
        System.out.println("Results:");
        System.out.println(padLeft("", RUN_NUMBER_LENGTH, PADDING) + String.join(VALUES_DELIMITER, executionTimes.columnKeySet()));
        executionTimes.rowMap().forEach((rowNumber, rowMap) -> {
            System.out.print(padLeft(rowNumber.toString(), RUN_NUMBER_LENGTH, PADDING));
            System.out.println(rowMap.values().stream().map(value -> padLeft(value.toString(), VALUE_LENGTH, PADDING)).collect(Collectors.joining(VALUES_DELIMITER)));
        });
    }

    private static String executionKey(String fileName) {
        final String base = fileName.substring(fileName.indexOf("_") + 1, fileName.length() - 4);
        return padLeft(base.replace('_', WARMUP_EXECUTION_SEPARATOR), VALUE_LENGTH, PADDING);
    }

    private static String padLeft(String stringToPad, int destinationLength, char padding) {
        final int requiredPaddingLength = destinationLength - stringToPad.length();
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < requiredPaddingLength; i++) {
            builder.append(padding);
        }
        builder.append(stringToPad);
        return builder.toString();
    }
}
