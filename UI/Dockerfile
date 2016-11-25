FROM frolvlad/alpine-oraclejdk8

ENTRYPOINT ["/usr/bin/java"]

CMD ["-Xmx128m", "-Xms128m", "-jar", "ui.jar"]

ADD target/UI-0.0.1-SNAPSHOT.jar ui.jar