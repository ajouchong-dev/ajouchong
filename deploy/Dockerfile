FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

CMD java $JAVA_OPTS -jar /app/app.jar
