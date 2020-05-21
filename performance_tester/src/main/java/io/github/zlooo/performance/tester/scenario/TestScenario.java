package io.github.zlooo.performance.tester.scenario;

public interface TestScenario {

    void before();

    void after();

    void execute(int times);

    void logSumup();

    void reset();
}
