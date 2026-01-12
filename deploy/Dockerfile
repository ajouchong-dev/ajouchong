FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

COPY . .
RUN apt update -y
RUN apt install -y git
RUN ./gradlew build -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -jar /app/app.jar