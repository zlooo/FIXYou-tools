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

@Slf4j
public class NewOrderSingleReceivingScenario extends AbstractFixScenario {

    public static final int EXECUTION_REPORTS_PER_NEW_ORDER_SINGLE = 3;
    private final IdleStrategy idleStrategy = new BackoffIdleStrategy();
    private int orderId = 1;
    private int executionId = 1;
    private int timesExecuted;

    public NewOrderSingleReceivingScenario(SessionID sessionID, MessageExchange<String> fixMessageExchange) {
        super(sessionID, fixMessageExchange);
    }

    @Override
    public void execute(int times) {
        timesExecuted = times;
        int timesRemaining = times;
        while (timesRemaining > 0) {
            final String message = getFixMessageExchange().getSingleMessage();
            if (message != null) {
                final String clordid = FixMessageUtils.getClordid(message);
                final String pendingNew = FixMessages.executionReport(getSessionID(), sequenceNumber++, clordid, executionId++, ExecType.PENDING_NEW, OrdStatus.PENDING_NEW, orderId++);
                final String newOrder = FixMessages.executionReport(getSessionID(), sequenceNumber++, clordid, executionId++, ExecType.NEW, OrdStatus.NEW, orderId);
                final String filled = FixMessages.executionReport(getSessionID(), sequenceNumber++, clordid, executionId++, ExecType.FILL, OrdStatus.FILLED, orderId);
                getFixMessageExchange().sendMessage(pendingNew);
                getFixMessageExchange().sendMessage(newOrder);
                getFixMessageExchange().sendMessage(filled);
                timesRemaining--;
                idleStrategy.idle(1);
            } else {
                idleStrategy.idle(0);
            }
        }
    }

    @Override
    public void reset() {
        timesExecuted = 0;
    }

    @Override
    public void logSumup() {
        log.info("Sent {} execution reports, ", timesExecuted * EXECUTION_REPORTS_PER_NEW_ORDER_SINGLE);
    }
}
