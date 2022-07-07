FROM openjdk:11.0.15-slim-buster

WORKDIR /app

COPY ./target/api-rest-accounts-0.0.1-SNAPSHOT.jar .

EXPOSE 8082

ENTRYPOINT ["java","-jar","api-rest-accounts-0.0.1-SNAPSHOT.jar"]

