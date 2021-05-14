package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.PerformanceTesterException;
import lombok.experimental.UtilityClass;
import quickfix.SessionID;

import java.util.concurrent.atomic.AtomicInteger;

@UtilityClass
public class TestScenarioFactory {

    public static AbstractFixScenario createTestScenario(String scenarioId, MessageExchange<String> messageExchange, SessionID sessionID, int numberOfQuotes, AtomicInteger sequencer) {
        switch (scenarioId) {
            case "newOrderSingleSending":
                return new NewOrderSingleSendingScenario(sessionID, messageExchange, sequencer);
            case "newOrderSingleReceiving":
                return new NewOrderSingleReceivingScenario(sessionID, messageExchange, sequencer);
            case "quoteReceiving":
                return new QuoteReceivingScenario(sessionID, messageExchange, numberOfQuotes, sequencer);
            default:
                throw new PerformanceTesterException("Unrecognized scenario id " + scenarioId);
        }
    }
}
