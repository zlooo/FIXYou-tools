package io.github.zlooo.performance.tester;

import io.github.zlooo.performance.tester.quickfix.NewOrderSindleReceivingApplication;
import io.github.zlooo.performance.tester.quickfix.QuoteStreamingApplication;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import quickfix.*;
import quickfix.fixt11.MessageFactory;

import java.util.Map;

@Slf4j
@CommandLine.Command(name = "quickfix", mixinStandardHelpOptions = true)
public class QuickfixClient extends AbstractPerformanceTesterSubcommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec commandSpec;

    @SneakyThrows
    @CommandLine.Command(name = "acceptor")
    private int acceptor(@CommandLine.Option(names = {"-s", "--scenario"}, description = "ID of scenario that should be run", required = true) String scenarioId) {
        final Map<String, Object> config = getConfig("quickfix", "acceptor");
        final SessionSettings sessionSettings = new SessionSettings((String) config.get("confFile"));
        log.info("About to start quickfix with settings {}", sessionSettings);
        final SocketAcceptor socketAcceptor =
                SocketAcceptor.newBuilder()
                              .withApplication(createFixApplication(scenarioId))
                              .withMessageFactory(new MessageFactory())
                              .withSettings(sessionSettings)
                              .withMessageStoreFactory(new NoopStoreFactory())
                              .withLogFactory(new SLF4JLogFactory(sessionSettings))
                              .build();
        socketAcceptor.start();
        log.info("Quickfix started");
        System.out.println("Press any key when test is done");
        System.in.read();
        log.info("Stopping quickfix");
        socketAcceptor.stop();
        log.info("Stopped");
        return 0;
    }

    private Application createFixApplication(String scenarioId) {
        switch (scenarioId) {
            case "newOrderSingleReceiving":
                return new NewOrderSindleReceivingApplication();
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
