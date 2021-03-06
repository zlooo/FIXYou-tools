package io.github.zlooo.performance.tester;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import io.github.zlooo.performance.tester.netty.NettyMessageExchange;
import io.github.zlooo.performance.tester.netty.NettyUtils;
import io.github.zlooo.performance.tester.scenario.AbstractFixScenario;
import io.github.zlooo.performance.tester.scenario.NewOrderSingleReceivingScenario;
import io.github.zlooo.performance.tester.scenario.NewOrderSingleSendingScenario;
import quickfix.SessionID;

import java.util.Map;

@Slf4j
@CommandLine.Command(name = "probe", mixinStandardHelpOptions = true, description = {
        "Launches in probe mode, meant to execute test scenario and log results. When run in probe mode no fix engine is started, just a simple TCP connection is established and predefined fix messages are sent"})
public class NettyProbe extends AbstractPerformanceTesterSubcommand {

    @CommandLine.Spec
    protected CommandLine.Model.CommandSpec commandSpec;

    @SneakyThrows
    @CommandLine.Command(name = "initiator")
    private int initiator(@CommandLine.Option(names = {"-s", "--scenario"}, description = "ID of scenario that should be run. Supported values are newOrderSingleSending and newOrderSingleReceiving", required = true) String scenarioId,
                          @CommandLine.Option(names = {"-t", "--times"}, description = "Number of times scenario should be executed", defaultValue = "1000") int times,
                          @CommandLine.Option(names = {"-w", "--warm-up"}, description = "Number of times scenario should be executed as a warm-up", defaultValue = "10") int warmUpTimes) {
        log.info("Probe - initiator - scenarioId {}", scenarioId);
        final Map<String, Object> config = getConfig("probe", "initiator");
        final int port = (int) config.get("port");
        final String host = (String) config.get("host");
        final Channel channel = new Bootstrap().group(new NioEventLoopGroup(1)).channel(NioSocketChannel.class).handler(NettyUtils.channelInitializer()).connect(host, port).sync().channel();
        log.info("Connected to {}:{}", host, port);
        final NettyMessageExchange nettyMessageExchange = new NettyMessageExchange(channel);
        final AbstractFixScenario testScenario = createTestScenario(scenarioId, nettyMessageExchange, createSessionID(config));
        log.info("Executing before");
        testScenario.before();
        log.info("Executing test scenario {} times as warm-up", warmUpTimes);
        testScenario.execute(warmUpTimes);
        testScenario.reset();
        log.info("Executing test scenario {} times", times);
        testScenario.execute(times);
        log.info("Executing after");
        testScenario.after();
        testScenario.logSumup();
        channel.close().sync();
        return 0;
    }

    @Override
    protected CommandLine.Model.CommandSpec getCommandSpec() {
        return commandSpec;
    }

    private SessionID createSessionID(Map<String, Object> config) {
        return new SessionID((String) config.get("beginString"), (String) config.get("senderCompId"), (String) config.get("targetCompId"));
    }

    private AbstractFixScenario createTestScenario(String scenarioId, MessageExchange<String> messageExchange, SessionID sessionID) {
        switch (scenarioId) {
            case "newOrderSingleSending":
                return new NewOrderSingleSendingScenario(sessionID, messageExchange);
            case "newOrderSingleReceiving":
                return new NewOrderSingleReceivingScenario(sessionID, messageExchange);
            default:
                throw new PerformanceTesterException("Unrecognized scenario id " + scenarioId);
        }
    }
}
