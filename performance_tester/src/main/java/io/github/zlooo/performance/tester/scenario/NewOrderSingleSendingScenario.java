package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.fix.FixMessageUtils;
import io.github.zlooo.performance.tester.fix.FixMessages;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import quickfix.SessionID;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class NewOrderSingleSendingScenario extends AbstractFixScenario {

    private static final int EXPECTED_NUMBER_OF_RESPONSES = 3;
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
    private long startTime;
    private long endTime;
    private int timesExecuted;

    public NewOrderSingleSendingScenario(SessionID sessionID, MessageExchange<String> fixMessageExchange) {
        super(sessionID, fixMessageExchange);
    }

    @Override
    public void execute(int times) {
        log.info("Preparing {} new order singles to send", times);
        final Map<String, Counter> clordIDsToExpectedMessageCount = new HashMap<>(times);
        final String[] messages = new String[times];
        for (int i = 0; i < times; i++) {
            final String clordid = UUID.randomUUID().toString();
            messages[i] = FixMessages.newOrderSingle(getSessionID(), sequenceNumber++, clordid);
            clordIDsToExpectedMessageCount.put(clordid, new Counter(EXPECTED_NUMBER_OF_RESPONSES));
        }
        log.info("Done, spamming session {}", getSessionID());
        startTime = System.nanoTime();
        for (final String message : messages) {
            getFixMessageExchange().sendMessage(message);
        }
        getFixMessageExchange().endOfBatch();
        while (!clordIDsToExpectedMessageCount.isEmpty()) {
            final String message = getFixMessageExchange().getSingleMessage();
            if (message != null) {
                final String clordid = FixMessageUtils.getClordid(message);
                final Counter messagesRemaining = clordIDsToExpectedMessageCount.get(clordid);
                if (messagesRemaining == null) {
                    log.warn("Unexpected message received {}", message);
                } else {
                    messagesRemaining.counter--;
                    if (messagesRemaining.counter == 0) {
                        clordIDsToExpectedMessageCount.remove(clordid);
                    }
                }
                idleStrategy.idle(1);
            } else {
                idleStrategy.idle(0);
            }
        }
        endTime = System.nanoTime();
        timesExecuted = times;
    }

    @Override
    public void reset() {
        timesExecuted = 0;
        startTime = 0;
        endTime = 0;
    }

    @Override
    public Sumup getSumup() {
        return new Sumup("New Order Single Sending", timesExecuted * EXPECTED_NUMBER_OF_RESPONSES, endTime - startTime);
    }

    @AllArgsConstructor
    private static final class Counter {
        private int counter;
    }
}
