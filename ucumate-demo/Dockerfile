# run here: docker build -f ucumate-demo/Dockerfile -t ucumate-demo .
# tarball: docker save -o ucumate-demo.tar ucumate-demo
# load elsewhere: docker load -i ucumate-demo.tar
# tag: docker tag ucumate-demo docker-registry.it.csiro.au/ucumate-demo/ucumate-demo
# push: docker push docker-registry.it.csiro.au/ucumate-demo/ucumate-demo
# run: docker run -p 8080:8080 docker-registry.it.csiro.au/ucumate-demo/ucumate-demo

FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean install -DskipTests
RUN mvn clean package spring-boot:repackage -DskipTests


FROM eclipse-temurin:21.0.7_6-jdk
WORKDIR /app
COPY --from=build /app/target/ucumate-demo-1.0.2-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
