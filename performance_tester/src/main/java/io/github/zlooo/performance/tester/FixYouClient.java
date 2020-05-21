package io.github.zlooo.performance.tester;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import pl.zlooo.fixyou.Engine;
import pl.zlooo.fixyou.FIXYouConfiguration;
import pl.zlooo.fixyou.fix.commons.FixMessageListener;
import pl.zlooo.fixyou.netty.FIXYouNetty;
import io.github.zlooo.performance.tester.fixyou.FixSpec50SP2;
import io.github.zlooo.performance.tester.fixyou.NewOrderSingleReceivingMessageListener;
import pl.zlooo.fixyou.session.SessionConfig;
import pl.zlooo.fixyou.session.SessionID;

import java.util.Map;

@Slf4j
@CommandLine.Command(name = "fixyou", mixinStandardHelpOptions = true)
public class FixYouClient extends AbstractPerformanceTesterSubcommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec commandSpec;

    @SneakyThrows
    @CommandLine.Command(name = "acceptor")
    private int acceptor(@CommandLine.Option(names = {"-s", "--scenario"}, description = "ID of scenario that should be run. Supported values are newOrderSingleReceiving", required = true) String scenarioId) {
        final Map<String, Object> config = getConfig("fixyou", "acceptor");
        final String bindInterface = (String) config.get("bindInterface");
        final int port = (int) config.get("port");
        final SessionID sessionID = createSessionID(config);
        log.info("About to start FIXYou for session {} listening on port {}", sessionID, port);
        final Engine engine = FIXYouNetty.create(FIXYouConfiguration.builder().acceptorBindInterface(bindInterface).acceptorListenPort(port).initiator(false).build(), createFixMessageListener(scenarioId));
        engine.registerSessionAndDictionary(sessionID, "fix50sp2", new FixSpec50SP2(), new SessionConfig().setPort(port)).start().get();
        log.info("FIXYou started");
        System.out.println("Press any key when test is done");
        System.in.read();
        log.info("Stopping FIXYou");
        engine.stop().get();
        log.info("Stopped");
        return 0;
    }

    private SessionID createSessionID(Map<String, Object> config) {
        return new SessionID(((String) config.get("beginString")).toCharArray(), ((String) config.get("senderCompId")).toCharArray(), ((String) config.get("targetCompId")).toCharArray());
    }

    @Override
    protected CommandLine.Model.CommandSpec getCommandSpec() {
        return commandSpec;
    }

    private FixMessageListener createFixMessageListener(String scenarioId) {
        switch (scenarioId) {
            case "newOrderSingleReceiving":
                return new NewOrderSingleReceivingMessageListener();
            default:
                throw new PerformanceTesterException("Unrecognized scenario id " + scenarioId);
        }
    }
}
