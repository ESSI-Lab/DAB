FROM 672812193635.dkr.ecr.us-east-1.amazonaws.com/gi-project-build:run-base-4.0

LABEL description="This image runs the GS-service."

ARG WAR_FILE=gs-service.war
ADD ${WAR_FILE} /var/lib/tomcat9/webapps/gs-service.war

CMD ["catalina.sh", "run"]

EXPOSE 8080