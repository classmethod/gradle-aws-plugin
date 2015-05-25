FROM java:openjdk-8-jdk
EXPOSE 8080
WORKDIR /opt/${artifactId}/
ADD ${jarFilename} /opt/${artifactId}/

CMD /usr/bin/java \
  -Xms512m \
  -Xmx1g \
  -Dfile.encoding=UTF-8 \
  -Dserver.port=8080 \
  -jar ${jarFilename}
