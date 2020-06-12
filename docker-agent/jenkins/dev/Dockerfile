FROM jre8-compose:1.0.0

RUN mkdir -p /opt/app/data && mkdir -p /opt/app/logs

ADD docker-agent.jar /opt/app/docker-agent.jar
ENV JAVA_OPTS_DEF="-server"
ENV JAVA_OPTS="-Xmx2g -Xms2g"

WORKDIR /opt/app

CMD java ${JAVA_OPTS_DEF} ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /opt/app/docker-agent.jar