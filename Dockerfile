FROM openjdk:19-alpine

RUN apk update && apk add ffmpeg

EXPOSE 5005

# Create a directory inside the container
RUN mkdir -p /bewired/jar

# Copy the JAR file to the container
COPY build/libs/demo-0.0.1-SNAPSHOT.jar /bewired/jar/demo-0.0.1-SNAPSHOT.jar

# Set the working directory
WORKDIR /bewired/jar

# Run the JAR file
CMD ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-jar", "demo-0.0.1-SNAPSHOT.jar"]