FROM public.ecr.aws/docker/library/openjdk:17-slim

RUN mkdir /app
WORKDIR /app

COPY target/*.jar /app/
EXPOSE 8080

RUN printf "#!/bin/sh \n\
exec /usr/local/openjdk-17/bin/java -jar %s\n" "$(ls /app/*.jar)"\
 > /app/entrypoint.sh \
 && chmod u+x /app/entrypoint.sh
ENTRYPOINT ["/bin/sh","/app/entrypoint.sh"]
