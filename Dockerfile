FROM amazoncorretto:21-alpine-jdk
COPY target/*.jar registration-service.jar
ENTRYPOINT ["java","-jar","/registration-service.jar"]