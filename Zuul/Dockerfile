FROM adoptopenjdk:11.0.6_10-jre-hotspot

ENV VERSION="0.0.1-SNAPSHOT"

ADD target/ZuulPub-${VERSION}.jar zuul.jar

CMD java -Xmx128m -Xms64m -jar zuul.jar

EXPOSE 8761