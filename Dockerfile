ARG BASE_IMAGE=961225121166.dkr.ecr.eu-central-1.amazonaws.com/base/openjdk:11-jre-slim

FROM ${BASE_IMAGE}

RUN apt-get update \
    && apt-get install -y curl jq wget \
    && rm -rf /var/lib/apt/lists/*

ADD target/*.jar app.jar
ADD entrypoint.sh entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080
ENTRYPOINT ["/entrypoint.sh"]
CMD ["server"]
