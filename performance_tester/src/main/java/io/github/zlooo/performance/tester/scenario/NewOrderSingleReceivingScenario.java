package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.fix.FixMessageUtils;
import io.github.zlooo.performance.tester.fix.FixMessages;
import lombok.extern.slf4j.Slf4j;
import org.agrona.concurrent.BackoffIdleStrategy;
import org.agrona.concurrent.IdleStrategy;
import quickfix.SessionID;
import quickfix.field.ExecType;
import quickfix.field.OrdStatus;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class NewOrderSingleReceivingScenario extends AbstractFixScenario {

    public static final int EXECUTION_REPORTS_PER_NEW_ORDER_SINGLE = 3;
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
    private int orderId = 1;
    private int executionId = 1;
    private int timesExecuted;
    private long startTime;
    private long endTime;

    public NewOrderSingleReceivingScenario(SessionID sessionID, MessageExchange<String> fixMessageExchange, AtomicInteger sequencer) {
        super(sequencer, sessionID, fixMessageExchange);
    }

    @Override
    public void execute(int times) {
        startTime = System.nanoTime();
        timesExecuted = times;
        int timesRemaining = times;
        while (timesRemaining > 0) {
            final String message = getFixMessageExchange().getSingleMessage();
            if (message != null) {
                final String clordid = FixMessageUtils.getClordid(message);
                final String pendingNew = FixMessages.executionReport(getSessionID(), sequencer.incrementAndGet(), clordid, executionId++, ExecType.PENDING_NEW, OrdStatus.PENDING_NEW, orderId++);
                final String newOrder = FixMessages.executionReport(getSessionID(), sequencer.incrementAndGet(), clordid, executionId++, ExecType.NEW, OrdStatus.NEW, orderId);
                final String filled = FixMessages.executionReport(getSessionID(), sequencer.incrementAndGet(), clordid, executionId++, ExecType.FILL, OrdStatus.FILLED, orderId);
                getFixMessageExchange().sendMessage(pendingNew);
                getFixMessageExchange().sendMessage(newOrder);
                getFixMessageExchange().sendMessage(filled);
                timesRemaining--;
                idleStrategy.idle(1);
            } else {
                idleStrategy.idle(0);
            }
        }
        endTime = System.nanoTime();
    }

    @Override
    public void reset() {
        timesExecuted = 0;
        startTime = 0;
        endTime = 0;
    }

    @Override
    public Sumup getSumup() {
        return new Sumup("New Order Single Receiging", timesExecuted * EXECUTION_REPORTS_PER_NEW_ORDER_SINGLE, endTime - startTime);
    }
}
