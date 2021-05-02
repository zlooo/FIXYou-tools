#!/bin/bash

java=$1
perfTesterVersion=$2

function testRun {
  echo "Run - $1/$2 $3 quotes"
  for i in {1..10}
  do
    $java -Dlogback.configurationFile=performance_tester/src/main/resources/logback_probe.xml -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" -c performance_tester/src/main/resources/defaults.yaml probe initiator -s quoteReceiving -w "$1" -t "$2" -q "$3"
    echo $i
    if [ $? != 0 ]; then
      echo "Something went wrong in run $i, check the logs"
      exit 1
    fi
  done
  mv perfTester.log "perfTester_$1_$2.log"
}
echo "Global warmup"
$java -Dlogback.configurationFile=performance_tester/src/main/resources/logback_probe.xml -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" -c performance_tester/src/main/resources/defaults.yaml probe initiator -s quoteReceiving -w 100000 -t 0
testRun 1 1 30000
testRun 2 5 30000
testRun 3 10 30000
testRun 5 20 30000
testRun 10 100 30000
testRun 20 200 30000
testRun 50 500 30000
$java -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" sumup `ls perfTester_*`