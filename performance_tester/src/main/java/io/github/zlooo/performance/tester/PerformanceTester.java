package io.github.zlooo.performance.tester;

import io.github.gcmonitor.GcMonitor;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
@CommandLine.Command(name = "performance_tester", mixinStandardHelpOptions = true, version = "1.0.0", subcommands = {NettyProbe.class, FixYouClient.class, QuickfixClient.class})
public class PerformanceTester implements Callable<Integer> {

    private static final String DEFAULT_CONFIG_FILE_NAME = "/defaults.yaml";
    private static final int GC_MONITOR_INTERVAL_SECONDS = 10;
    @CommandLine.Option(names = {"-c", "--config"}, description = "Configuration file that should be used")
    private Path configFile;
    @CommandLine.Option(names = {"-g", "--gc-monitor"}, description = "Turn on gc-monitor, defaults to ${DEFAULT-VALUE}", defaultValue = "false")
    private boolean gcMonitorOn;
    @CommandLine.Spec
    private CommandLine.Model.CommandSpec commandSpec;
    @Getter
    private Map<String, Object> config;

    public static void main(String[] args) {
        final PerformanceTester performanceTester = new PerformanceTester();
        System.exit(new CommandLine(performanceTester).setExecutionStrategy(performanceTester::executionStrategy).execute(args));
    }

    @Override
    public Integer call() throws Exception {
        throw new CommandLine.ParameterException(commandSpec.commandLine(), "Missing required subcommand");
    }

    private int executionStrategy(CommandLine.ParseResult parseResult) {
        init();
        return new CommandLine.RunLast().execute(parseResult);
    }

    @SneakyThrows
    private void init() {
        final Yaml yaml = new Yaml();
        if (configFile != null) {
            try (final BufferedReader reader = Files.newBufferedReader(configFile)) {
                config = yaml.load(reader);
            }
        } else {
            config = yaml.load(getClass().getResourceAsStream(DEFAULT_CONFIG_FILE_NAME));
        }
        log.info("Loaded config {}", config);
        if (gcMonitorOn) {
            final Thread gcLogCollector = new Thread(PerformanceTester::gcMonitorRun, "gcLogCollector");
            gcLogCollector.setDaemon(true);
            gcLogCollector.start();
        }
    }

    @SneakyThrows
    private static void gcMonitorRun() {
        final GcMonitor gcMonitor = GcMonitor.builder().build();
        gcMonitor.start();
        while (true) {
            log.info("GC stats snapshot - {}", gcMonitor.getSnapshot());
            Thread.sleep(TimeUnit.SECONDS.toMillis(GC_MONITOR_INTERVAL_SECONDS));
        }
    }
}
