#docker build -t oauth-server:v0.0.1 .
#docker tag oauth-server:v0.0.1 uvolodia/pets-projects:oauth-server_v0.0.1
#docker push uvolodia/pets-projects:oauth-server_v0.0.1
FROM adoptopenjdk/openjdk11:alpine-jre
ADD docker/oauth-server.jar oauth-server.jar

# Set default timezone
ENV TZ=Europe/Moscow

ENTRYPOINT java -jar oauth-server.jar
