#Build jar file
FROM openjdk:13-jdk-alpine as build-jar
COPY . /
RUN ./gradlew build

#Run jar file
FROM openjdk:13-jdk-alpine
COPY --from=build-jar build/libs/*.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]