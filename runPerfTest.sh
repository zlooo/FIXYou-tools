#!/bin/bash
for i in {1..10}
do
  $1 -jar performance_tester/build/libs/performance_tester-0.0.0-11-all.jar probe initiator -s newOrderSingleSending -w 1 -t 1
  if [ $? != 0 ]; then
    echo "Something went wrong in run $i, check the logs"
    exit 1
  fi
done
