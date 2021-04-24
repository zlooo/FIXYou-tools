# FIXYou Tools

Repository containing additional tools that help to use FIXYou engine.

I do appreciate those are not the prettiest programs the world have seen, especially fix spec generator. They are not unit or integration tested, they definitely have some bugs lurking in the shadows. However, with limited resources I had, I'm doing this after hours as a side, pet project, I preferred to pay more attention to the engine itself.

## Fix Spec Generator
Simple program that takes Quickfix xml data dictionary(ies) and generates implementations of `pl.zlooo.fixyou.model.FixSpec`. Can either just generate source or compile it and package to jar as well so that it can be just placed on classpath. Run with -h, --help or no option at all to see usage information.

<br/>Example usage, provided you have shadow jar created by this build, either built yourself or got from maven central:

`java -jar fix_spec_generator.jar -p pl.zlooo.fixyou.example -f FIXT11.xml -f FIX50SP2.xml` will read 2 xml files, FIXT11.xml and FIX50SP2.xml, and produce pl/zlooo/fixyou/example/FixSpec.java in working directory

`java -jar fix_spec_generator.jar -p pl.zlooo.fixyou.example -f FIXT11.xml -f FIX50SP2.xml -j fix50sp2Spec.jar` will read 2 xml files, FIXT11.xml and FIX50SP2.xml, produce pl/zlooo/fixyou/example/FixSpec.java in working directory and create usable jar, fix50sp2Spec.jar, that contains compiled FixSpec class

## Performance Tester
Small program that can be used to execute simple test scenarios. It's primary purpose is to do performance tests of FIXYou and Quickfix using the same scenario so that they can be compared. Run with -h, --help or no option at all to see usage information.
### Probe test scenarios
There are 3 test scenarios currently supported by probe subcommand
1. `newOrderSingleSending` is meant to simulate exchange client. This scenario executes following steps:
    - Send login message with no credentials
    - Wait for login response
    - Send burst of NewOrderSingle messages, burst size is configured by -t option
    - For every NewOrderSingle message 3 ExecutionReport messages are expected, clordId field is used to match ExecutionReport to NewOrderSinge
    - Once all NewOrderSingle are sent, wait for all ExecutionReports to arrive
2. `newOrderSingleReceiving` opposite side of `newOrderSingleSending`. This scenario executes following steps:
    - Send login message with no credentials
    - Wait for login response
    - For every NewOrderSingle message that is received, 3 ExecutionReport messages are sent by probe
    - Number of NewOrderSingle messages that's expected is configured by -t option
3. `quoteReceiving`
    - Send login message with no credentials
    - Wait for login response
    - Send `Quote Request` message, type `R`
    - Wait for specified number of `Quote`, type `S`, messages
    - Send `Quote Cancel` message, type `Z`

Example usage:
`java -jar performance_tester.jar probe initiator -s newOrderSingleSending -t 10000 -w 1000` will execute newOrderSingleSending scenario, first warming up by sending 1000 NewOrderSingle and then sending 10000 NewOrderSingle and waiting for ExecutionReport responses with matching clordid. Time is measured in nano seconds by means of `System.nanoTime()`