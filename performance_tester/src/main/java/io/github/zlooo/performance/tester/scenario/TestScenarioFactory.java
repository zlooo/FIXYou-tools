package io.github.zlooo.performance.tester.scenario;

import io.github.zlooo.performance.tester.MessageExchange;
import io.github.zlooo.performance.tester.PerformanceTesterException;
import lombok.experimental.UtilityClass;
import quickfix.SessionID;

@UtilityClass
public class TestScenarioFactory {

    public static AbstractFixScenario createTestScenario(String scenarioId, MessageExchange<String> messageExchange, SessionID sessionID, int numberOfQuotes) {
        switch (scenarioId) {
            case "newOrderSingleSending":
                return new NewOrderSingleSendingScenario(sessionID, messageExchange);
            case "newOrderSingleReceiving":
                return new NewOrderSingleReceivingScenario(sessionID, messageExchange);
            case "quoteReceiving":
                return new QuoteReceivingScenario(sessionID, messageExchange, numberOfQuotes);
            default:
                throw new PerformanceTesterException("Unrecognized scenario id " + scenarioId);
        }
    }
}
