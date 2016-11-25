FROM frolvlad/alpine-oraclejdk8

ENV VERSION="0.0.1-SNAPSHOT"

ADD target/ZuulPub-${VERSION}.jar zuul.jar

CMD /usr/bin/java -Xmx128m -Xms64m -jar zuul.jar

EXPOSE 8761