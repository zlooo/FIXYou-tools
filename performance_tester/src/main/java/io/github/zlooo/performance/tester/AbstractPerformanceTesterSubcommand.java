package io.github.zlooo.performance.tester;

import picocli.CommandLine;

import java.util.Map;
import java.util.concurrent.Callable;

abstract class AbstractPerformanceTesterSubcommand implements Callable<Integer> {

    protected abstract CommandLine.Model.CommandSpec getCommandSpec();

    @Override
    public Integer call() throws Exception {
        throw new CommandLine.ParameterException(getCommandSpec().commandLine(), "Missing required subcommand");
    }

    protected Map<String, Object> getConfig(String... keys) {
        CommandLine commandLine = getCommandSpec().commandLine();
        while (commandLine.getParent() != null) {
            commandLine = commandLine.getParent();
        }
        Map<String, Object> configMap = commandLine.<PerformanceTester>getCommand().getConfig();
        if (keys != null) {
            final StringBuilder configKeyBuilder = new StringBuilder();
            for (final String key : keys) {
                configKeyBuilder.append(key).append('/');
                final Object configEntry = configMap.get(key);
                if (configEntry == null) {
                    throw new PerformanceTesterException("No configuration for key " + configKeyBuilder.deleteCharAt(configKeyBuilder.length() - 1));
                } else if (!(configEntry instanceof Map)) {
                    throw new PerformanceTesterException("Expected configuration for key " + configKeyBuilder.deleteCharAt(configKeyBuilder.length() - 1) + " to be of complex type(Map<String, Object>), not concrete entry " + configEntry);
                } else {
                    configMap = (Map<String, Object>) configEntry;
                }
            }
        }
        return configMap;
    }
}
