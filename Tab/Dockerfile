FROM adoptopenjdk:11.0.6_10-jre-hotspot

ENTRYPOINT ["java"]

CMD ["-Xmx128m", "-Xms128m", "-jar", "tab.jar"]

ADD target/Tab-0.0.1-SNAPSHOT.jar tab.jar