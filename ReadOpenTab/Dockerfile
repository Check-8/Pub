FROM adoptopenjdk:11.0.6_10-jre-hotspot

ENTRYPOINT ["java"]

CMD ["-Xmx128m", "-Xms128m", "-jar", "opentab.jar"]

ADD target/ReadOpenTab-0.0.1-SNAPSHOT.jar opentab.jar