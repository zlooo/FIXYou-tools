package io.github.zlooo.performance.tester.scenario;

import lombok.Value;

public interface TestScenario {

    void before();

    void after();

    void execute(int times);

    Sumup getSumup();

    void reset();

    @Value
    class Sumup {
        private String scenarioName;
        private int samples;
        private long timeTaken;
    }
}
