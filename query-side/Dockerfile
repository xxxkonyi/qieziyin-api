FROM java:8-jre

VOLUME /tmp

EXPOSE 8000

# Copy your fat jar to the container
ADD build/libs/*.jar app.jar

RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
