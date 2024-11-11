FROM eclipse-temurin:11-jdk-alpine
VOLUME /shared
WORKDIR /shared
COPY target/2bass-*/ /2bass
ENV PATH="/2bass/bin:${PATH}"
RUN chmod +x /2bass/bin/bass