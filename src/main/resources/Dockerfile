FROM openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY ./target/scala-2.12/resources_2.12-0.1.0-SNAPSHOT.jar ./
COPY ./twitter/firstname.txt ./
COPY ./twitter/surname.txt ./
COPY ./twitter/words.txt ./
CMD ["java","-jar","./resources_2.12-0.1.0-SNAPSHOT.jar"]