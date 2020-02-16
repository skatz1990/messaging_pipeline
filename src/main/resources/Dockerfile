FROM openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY ./target/scala-2.13/kafka-example-assembly-0.1.jar ./
CMD ["java","-jar","./kafka-example-assembly-0.1.jar"]