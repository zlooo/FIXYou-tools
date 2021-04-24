#!/bin/bash

java=$1
perfTesterVersion=$2
engine=$3
scenario=$4

$java -Xms5g -Dio.netty.buffer.checkAccessible=false -Dio.netty.buffer.checkBounds=false -Dlogback.configurationFile=performance_tester/src/main/resources/logback.xml -jar "performance_tester/build/libs/performance_tester-$perfTesterVersion-all.jar" -c performance_tester/src/main/resources/defaults.yaml "$engine" acceptor -s "$scenario"