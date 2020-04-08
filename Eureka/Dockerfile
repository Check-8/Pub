FROM adoptopenjdk:11.0.6_10-jre-hotspot

ENV VERSION="0.0.1-SNAPSHOT"

ADD target/EurekaPub-${VERSION}.jar eureka.jar

CMD java -Xmx128m -Xms64m -jar eureka.jar

EXPOSE 8761