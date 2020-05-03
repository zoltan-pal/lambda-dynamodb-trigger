# Lambda with DynamoDB trigger (LocalStack demo)

This demo presents some capabilities of [LocalStack](https://github.com/localstack/localstack) related to the following 
AWS services:
* CloudFormation
* CloudWatch Logs
* DynamoDB
* DynamoDB Streams
* Lambda Functions
* S3

## Prerequisites

* Java 11
* Maven
* Docker
* [LocalStack](https://github.com/localstack/localstack)
* [awslocal](https://github.com/localstack/awscli-local)
* jq

## Usage

1. Build lambda with Maven: `mvn clean install`
1. Spin up LocalStack: `docker-compose up localstack`
1. Run demo script: `./run-demo.sh`

## Note

I have run into issues with running Java 11 lambda functions in LocalStack, so for this demo I'm using a somewhat hacky 
custom-built image:

```dockerfile
FROM localstack/localstack:0.11.0
RUN apk -u --no-cache add openjdk11 --repository=http://dl-cdn.alpinelinux.org/alpine/edge/community
COPY ./localstack-utils-fat.jar /opt/code/localstack/localstack/infra/localstack-utils-fat.jar
```

> :warning: I have also rebuilt [localstack-java-utils](https://github.com/localstack/localstack-java-utils) with Java 11,
> which I use here to overwrite the original Java 8 one.
