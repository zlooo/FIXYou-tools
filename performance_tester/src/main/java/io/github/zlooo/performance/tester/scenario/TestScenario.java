package io.github.zlooo.performance.tester.scenario;

import org.slf4j.Logger;

public interface TestScenario {

    void before();

    void after();

    void execute(int times);

    void logSumup(Logger logger);

    void reset();
}
