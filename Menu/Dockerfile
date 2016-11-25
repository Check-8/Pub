FROM frolvlad/alpine-oraclejdk8

ENTRYPOINT ["/usr/bin/java"]

CMD ["-Xmx64m", "-Xms64m", "-jar", "menu.jar"]

ADD target/Menu-0.0.1-SNAPSHOT.jar menu.jar