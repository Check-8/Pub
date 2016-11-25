FROM frolvlad/alpine-oraclejdk8

ENTRYPOINT ["/usr/bin/java"]

CMD ["-Xmx128m", "-Xms128m", "-jar", "tab.jar"]

ADD target/Tab-0.0.1-SNAPSHOT.jar tab.jar