#!/bin/bash

java=$1
perfTesterVersion=$2

function testRun {
  echo "Run - $1k/$2k"
  for i in {1..10}
  do
    $java -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" probe initiator -s newOrderSingleSending -w "$1000" -t "$2000"
    echo $i
    if [ $? != 0 ]; then
      echo "Something went wrong in run $i, check the logs"
      exit 1
    fi
  done
  mv perfTester.log "perfTester_$1_$2.log"
}

testRun 1 1
testRun 2 5
testRun 3 10
testRun 5 20
testRun 10 100
testRun 20 200
testRun 50 500