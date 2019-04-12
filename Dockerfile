FROM openjdk:11.0.3-jre-stretch
COPY target/cash-flow.jar /home/cash-flow.jar
COPY ./wait-for-it.sh /home/wait-for-it.sh
EXPOSE 8080
CMD java -jar /home/cash-flow.jar
