#!bin/bash

mvn -f ./ChefTodo/pom.xml package
mvn -f ./Eureka/pom.xml package
mvn -f ./Menu/pom.xml package
mvn -f ./ReadOpenTab/pom.xml package
mvn -f ./Tab/pom.xml package
mvn -f ./UI/pom.xml package
mvn -f ./Zuul/pom.xml package