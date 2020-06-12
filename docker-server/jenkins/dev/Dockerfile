FROM java:8-jre-alpine
RUN mkdir -p /opt/app/data && mkdir -p /opt/app/logs
ADD docker-server.jar /opt/app/docker-server.jar
ENV JAVA_OPTS_DEF="-server"
ENV JAVA_OPTS="-Xmx2g -Xms2g"

WORKDIR /opt/app

CMD java ${JAVA_OPTS_DEF} ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/app/docker-server.jar