package io.github.zlooo.performance.tester;

import fixio.FixServer;
import fixio.handlers.FixApplication;
import fixio.netty.pipeline.InMemorySessionRepository;
import fixio.netty.pipeline.server.AcceptAllAuthenticator;
import io.github.zlooo.performance.tester.fixio.NewOrderSingleReceivingApplication;
import io.github.zlooo.performance.tester.fixio.QuoteStreamingApplication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Map;

@Slf4j
@CommandLine.Command(name = "fixio", mixinStandardHelpOptions = true)
public class FixioClient extends AbstractPerformanceTesterSubcommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec commandSpec;

    @SneakyThrows
    @CommandLine.Command(name = "acceptor")
    private int acceptor(@CommandLine.Option(names = {"-s", "--scenario"}, description = "ID of scenario that should be run", required = true) String scenarioId) {
        final Map<String, Object> config = getConfig("fixio");
        log.info("About to start fixio with settings {}", config);
        final FixServer fixServer = new FixServer((Integer) config.get("port"), chooseApplication(scenarioId), new AcceptAllAuthenticator(), new InMemorySessionRepository());
        fixServer.start();
        log.info("Fixio started");
        System.out.println("Press any key when test is done");
        System.in.read();
        log.info("Stopping fixio");
        fixServer.stop();
        log.info("Stopped");
        return 0;
    }

    private FixApplication chooseApplication(String scenarioId) {
        switch (scenarioId) {
            case "newOrderSingleReceiving":
                return new NewOrderSingleReceivingApplication();
            case "quoteStreaming":
                return new QuoteStreamingApplication();
            default:
                throw new PerformanceTesterException("Unrecognized scenario id " + scenarioId);
        }
    }

    @Override
    protected CommandLine.Model.CommandSpec getCommandSpec() {
        return commandSpec;
    }
}
