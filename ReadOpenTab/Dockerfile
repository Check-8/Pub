FROM frolvlad/alpine-oraclejdk8

ENTRYPOINT ["/usr/bin/java"]

CMD ["-Xmx128m", "-Xms128m", "-jar", "opentab.jar"]

ADD target/ReadOpenTab-0.0.1-SNAPSHOT.jar opentab.jar