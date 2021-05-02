package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.fix.FixMessages;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import quickfix.SessionID;

import java.util.UUID;

@Slf4j
public class QuoteReceivingScenario extends AbstractFixScenario {

    private final int numberOfQuotes;
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
    private long startTime;
    private long endTime;
    private int timesExecuted;

    public QuoteReceivingScenario(SessionID sessionID, MessageExchange<String> fixMessageExchange, int numberOfQuotes) {
        super(sessionID, fixMessageExchange);
        this.numberOfQuotes = numberOfQuotes;
    }

    @Override
    public void execute(int times) {
        final String quoteRequestId = UUID.randomUUID().toString();
        final String quoteRequest = FixMessages.quoteRequest(getSessionID(), sequenceNumber++, quoteRequestId);
        final MessageExchange<String> fixMessageExchange = getFixMessageExchange();
        fixMessageExchange.sendMessage(quoteRequest);
        fixMessageExchange.endOfBatch();
        startTime = System.nanoTime();
        for (int i = 1; i <= numberOfQuotes * times; ) {
            if (fixMessageExchange.getSingleMessage() != null) {
                i++;
                idleStrategy.idle(1);
            } else {
                idleStrategy.idle(0);
            }
        }
        endTime = System.nanoTime();
        fixMessageExchange.sendMessage(FixMessages.quoteCancel(getSessionID(), sequenceNumber++, quoteRequestId));
        fixMessageExchange.endOfBatch();
        timesExecuted = times;
    }

    @Override
    public Sumup getSumup() {
        return new Sumup("Quote Receiving", timesExecuted * numberOfQuotes, endTime - startTime);
    }

    @Override
    public void reset() {
        startTime = 0;
        endTime = 0;
        timesExecuted = 0;
    }
}
