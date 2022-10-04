FROM maven:3.6.0-jdk-11-slim AS build
RUN apt-get update \
 && apt-get install openssl
WORKDIR /usr/app
COPY pom.xml ./
## mvn package à vide pour cache docker des dependances
RUN mvn package
COPY src ./src
RUN openssl genpkey -algorithm rsa -outform PEM -out ./src/main/resources/JWTPrivateKey.pem \
 && openssl rsa -in ./src/main/resources/JWTPrivateKey.pem -outform PEM -pubout -out ./src/main/resources/JWTPublicKey.pem
RUN mvn package
CMD mvn spring-boot:run
