FROM frolvlad/alpine-oraclejdk8

ENV VERSION="0.0.1-SNAPSHOT"

ADD target/EurekaPub-${VERSION}.jar eureka.jar

CMD /usr/bin/java -Xmx128m -Xms64m -jar eureka.jar

EXPOSE 8761