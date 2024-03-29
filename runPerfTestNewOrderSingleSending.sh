#!/bin/bash

java=$1
perfTesterVersion=$2

function testRun {
  echo "Run - $1k/$2k"
  for i in {1..10}
  do
    $java -Dlogback.configurationFile=performance_tester/src/main/resources/logback_probe.xml -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" -c performance_tester/src/main/resources/defaults.yaml probe initiator -s newOrderSingleSending -w "$1000" -t "$2000"
    echo $i
    if [ $? != 0 ]; then
      echo "Something went wrong in run $i, check the logs"
      exit 1
    fi
  done
  mv perfTester.log "perfTester_$1_$2.log"
}
echo "Global warmup"
$java -Dlogback.configurationFile=performance_tester/src/main/resources/logback_probe.xml -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" -c performance_tester/src/main/resources/defaults.yaml probe initiator -s newOrderSingleSending -w 100000 -t 0
testRun 1 1
testRun 2 5
testRun 3 10
testRun 5 20
testRun 10 100
testRun 20 200
testRun 50 500
$java -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" sumup `ls perfTester_*`