FROM openjdk:8-jdk-alpine
EXPOSE 8080
WORKDIR /opt/${artifactId}/
ADD ${jarFilename} /opt/${artifactId}/

CMD /usr/bin/java \
  -Xms512m \
  -Xmx1g \
  -Dfile.encoding=UTF-8 \
  -Dserver.port=8080 \
  -jar ${jarFilename}
