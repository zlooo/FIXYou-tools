package io.github.zlooo.performance.tester;

import io.github.zlooo.fixyou.Engine;
import io.github.zlooo.fixyou.FIXYouConfiguration;
import io.github.zlooo.fixyou.fix.commons.FixMessageListener;
import io.github.zlooo.fixyou.netty.FIXYouNetty;
import io.github.zlooo.fixyou.session.SessionConfig;
import io.github.zlooo.fixyou.session.SessionID;
import io.github.zlooo.performance.tester.fixyou.FixSpec50SP2;
import io.github.zlooo.performance.tester.fixyou.FixSpec50SP2Min;
import io.github.zlooo.performance.tester.fixyou.NewOrderSingleReceivingMessageListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.util.Map;

@Slf4j
@CommandLine.Command(name = "fixyou", mixinStandardHelpOptions = true)
public class FixYouClient extends AbstractPerformanceTesterSubcommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec commandSpec;

    @SneakyThrows
    @CommandLine.Command(name = "acceptor")
    private int acceptor(@CommandLine.Option(names = {"-s", "--scenario"}, description = "ID of scenario that should be run. Supported values are newOrderSingleReceiving", required = true) String scenarioId,
                         @CommandLine.Option(names = {"-m", "--min-dic"}, description = "Use minimalistic dictionary containing only Logon, NewOrderSingle and ExecutionReport and even that with not all fields", defaultValue = "false")
                                 boolean minDictionary) {
        final Map<String, Object> config = getConfig("fixyou", "acceptor");
        final String bindInterface = (String) config.get("bindInterface");
        final int port = (int) config.get("port");
        final SessionID sessionID = createSessionID(config);
        log.info("About to start FIXYou for session {} listening on port {}", sessionID, port);
        final Engine engine = FIXYouNetty.create(FIXYouConfiguration.builder().acceptorBindInterface(bindInterface).acceptorListenPort(port).initiator(false).separateIoFromAppThread(true).build(), createFixMessageListener(scenarioId));
        engine.registerSessionAndDictionary(sessionID, "fix50sp2", minDictionary ? new FixSpec50SP2Min() : new FixSpec50SP2(), new SessionConfig().setPort(port).setConsolidateFlushes(true)).start().get();
        log.info("FIXYou started");
        System.out.println("Press enter when test is done");
        System.in.read();
        log.info("Stopping FIXYou");
        engine.stop().get();
        log.info("Stopped");
        return 0;
    }

    private SessionID createSessionID(Map<String, Object> config) {
        final String beginString = (String) config.get("beginString");
        final String senderCompId = (String) config.get("senderCompId");
        final String targetCompId = (String) config.get("targetCompId");
        return new SessionID(beginString.toCharArray(), beginString.length(), senderCompId.toCharArray(), senderCompId.length(), targetCompId.toCharArray(), targetCompId.length());
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
