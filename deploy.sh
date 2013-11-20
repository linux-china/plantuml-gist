#!/bin/bash

git pull
mvn -DskipTests clean package
rm -rf /home/virtual_hosts/plantuml.mvnsearch.org/ROOT
cp -r target/plantuml-gist /home/virtual_hosts/plantuml.mvnsearch.org/ROOT