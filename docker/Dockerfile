FROM azul/zulu-openjdk-debian:17-jre-headless-latest

COPY ./build/libs/armeria-client-proxy-sidecar-1.0-SNAPSHOT.jar proxy.jar

EXPOSE 8888
ENV TZ="Asia/Seoul"

ENTRYPOINT ["java", "-jar", "proxy.jar"]