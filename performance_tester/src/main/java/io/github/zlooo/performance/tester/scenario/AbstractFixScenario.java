package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.PerformanceTesterException;
import io.github.zlooo.performance.tester.fix.FixMessages;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import quickfix.SessionID;

@Getter
@RequiredArgsConstructor
public abstract class AbstractFixScenario implements TestScenario {

    private static final int HEARTBEAT_INTERVAL = 30;
    protected int sequenceNumber = 1;
    private final SessionID sessionID;
    private final MessageExchange<String> fixMessageExchange;

    @Override
    public void before() {
        fixMessageExchange.sendMessage(FixMessages.logon(sessionID, sequenceNumber++, HEARTBEAT_INTERVAL, true));
        fixMessageExchange.endOfBatch();
        String logonResponse;
        while ((logonResponse = fixMessageExchange.getSingleMessage()) == null) {
            //nothing to do but wait
        }
        if (!logonResponse.contains("35=A")) {
            throw new PerformanceTesterException("Expected to receive logon message but got following one instead " + logonResponse);
        }
    }

    @Override
    public void after() {
        fixMessageExchange.sendMessage(FixMessages.logout(sessionID, sequenceNumber++, null));
        fixMessageExchange.endOfBatch();
        while (true) {
            final String logoutResponse = fixMessageExchange.getSingleMessage();
            if (logoutResponse != null && logoutResponse.contains("35=5")) {
                break;
            }
        }
    }
}
