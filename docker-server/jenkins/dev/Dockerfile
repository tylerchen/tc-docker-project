FROM java:8-jre-alpine
ADD docker-server.jar /opt/docker-server.jar
ENV JAVA_OPTS_DEF="-server"
ENV JAVA_OPTS="-Xmx2g -Xms2g"
CMD java ${JAVA_OPTS_DEF} ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/docker-server.jar