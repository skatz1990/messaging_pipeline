FROM openjdk:8-jre-alpine
RUN mkdir -p /opt/app
WORKDIR /opt/app
COPY ./kafka-example_2.13-0.1.jar ./
COPY ./twitter/firstname.txt ./
COPY ./twitter/surname.txt ./
COPY ./twitter/words.txt ./
CMD ["java","-jar","./kafka-example_2.13-0.1.jar"]