FROM java:8-alpine

MAINTAINER Ed Sweeney <ed@onextent.com>

EXPOSE 8080

RUN mkdir -p /app

COPY target/scala-2.12/*.jar /app/

WORKDIR /app

ENTRYPOINT ["java","-jar", "SupersixHttp.jar", "-Xms256m", "-Xmx256m"]

