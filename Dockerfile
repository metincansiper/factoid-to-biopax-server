# copy the source and build it as a maven project
FROM maven:3.5-jdk-8-alpine as build
WORKDIR /app
COPY . $PWD
RUN mvn clean install

# deploy the war file created in build stage to tomcat
FROM tomcat:7.0.86-jre8-alpine
ENV TARGET_WAR_NAME=FactoidToBiopaxServer
COPY --from=build /app/target/*.war /usr/local/tomcat/webapps/${TARGET_WAR_NAME}.war
