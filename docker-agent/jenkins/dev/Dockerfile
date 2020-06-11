FROM jre8-compose:1.0.0
ADD docker-agent.jar /opt/docker-agent.jar
ENV JAVA_OPTS_DEF="-server"
ENV JAVA_OPTS="-Xmx2g -Xms2g"
CMD java ${JAVA_OPTS_DEF} ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/docker-agent.jar