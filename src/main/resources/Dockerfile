FROM openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY ./target/scala-2.13/kafka-example_2.13-0.1.jar ./
CMD ["java","-jar","./kafka-example_2.13-0.1.jar"]