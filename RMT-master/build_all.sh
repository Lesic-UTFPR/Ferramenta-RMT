#!/bin/sh

cd ./messages-api;
mvn install;
cd ../detection-agent;
mvn clean package -U;
cd ../intermediary-agent;
mvn clean package -U
cd ../metrics-agent
mvn clean package -U

