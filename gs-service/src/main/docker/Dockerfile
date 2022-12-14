FROM ubuntu:jammy

LABEL description="This image runs the DAB service."

ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get update && apt-get install -yq \
	libnetcdf-dev \
	gdal-bin \
	cdo \
	openjdk-8-jdk \
	&& rm -rf /var/lib/apt/lists/*

ENV TERM xterm-mono

RUN apt-get update && apt-get install -yq \
	tomcat9 \
	&& rm -rf /var/lib/apt/lists/*

RUN sed -i -e 's/<Connector/<Connector maxHttpHeaderSize="81920"/g' /etc/tomcat9/server.xml
RUN sed -i -e 's/<Connector/<Connector relaxedQueryChars="[,]"/g' /etc/tomcat9/server.xml

ENV PATH="/usr/share/tomcat9/bin:${PATH}"
	
ENV CATALINA_HOME="/usr/share/tomcat9"
ENV CATALINA_BASE="/var/lib/tomcat9"

ARG WAR_FILE=target/gs-service.war
ADD ${WAR_FILE} /var/lib/tomcat9/webapps/gs-service.war

CMD ["catalina.sh", "run"]

EXPOSE 8080