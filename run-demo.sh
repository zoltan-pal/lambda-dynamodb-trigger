#!/bin/bash

function main() {
  PS3="Select demo to run: "

  select opt in cloudformation_s3 dynamodb_lambda dynamodbstreams logs quit; do
    case $opt in
      cloudformation_s3)
        cloudformation_s3_demo;;
      dynamodb_lambda)
        dynamodb_lambda_demo;;
      dynamodbstreams)
        dynamodbstreams_demo;;
      logs)
        logs_demo;;
      quit)
        break;;
      *)
        echo "Invalid option $REPLY";;
    esac
  done
}

function cloudformation_s3_demo() {
  echo "Create bucket:"
  awslocal s3 mb s3://lambda-function-source-bucket

  echo "Upload lambda artifact to S3:"
  awslocal s3 cp target/dynamodb-sync-lambda.jar s3://lambda-function-source-bucket

  echo "Create lambda-dynamodb stack:"
  awslocal cloudformation create-stack \
    --template-body file://cloudformation/lambda-dynamodb-stack.yaml \
    --stack-name lambda-dynamo-stack
}

function dynamodb_lambda_demo() {
  awslocal dynamodb list-tables

  echo "Put user to userData"
  awslocal dynamodb put-item \
    --table-name userData \
    --item '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"},"name": {"S": "John Doe"}}'

  echo "userDataBackup:"
  awslocal dynamodb get-item \
    --table-name userDataBackup \
    --key '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"}}'

  echo "Update user in userData"
  awslocal dynamodb update-item \
    --table-name userData \
    --key '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"}}' \
    --attribute-updates '{"name": {"Action": "PUT","Value": {"S":"John Connor"}}}' \
    --return-values UPDATED_NEW

  echo "userDataBackup:"
  awslocal dynamodb get-item \
    --table-name userDataBackup \
    --key '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"}}'

  echo "Remove user from userData"
  awslocal dynamodb delete-item \
    --table-name userData \
    --key '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"}}'

  echo "userDataBackup:"
  awslocal dynamodb get-item \
    --table-name userDataBackup \
    --key '{"id": {"S": "4b9b1a75-cafe-438b-8526-80f0e094e229"}}'
}

function dynamodbstreams_demo() {
  echo "DynamoDB streams:"
  awslocal dynamodbstreams list-streams

  echo "Describe stream for userData:"
  streamArn=$(awslocal dynamodbstreams list-streams | jq -r '.Streams | .[].StreamArn')
  awslocal dynamodbstreams describe-stream --stream-arn "$streamArn"

  echo "ShardIterator for userData stream shards:"
  shardId=$(awslocal dynamodbstreams describe-stream --stream-arn "$streamArn" | jq -r '.StreamDescription.Shards | .[].ShardId')
  awslocal dynamodbstreams get-shard-iterator \
    --stream-arn "$streamArn" \
    --shard-id "$shardId" \
    --shard-iterator-type LATEST
}

function logs_demo() {
  echo "Log groups:"
  awslocal logs describe-log-groups

  echo "Sync lambda log streams:"
  awslocal logs describe-log-streams \
    --log-group-name "/aws/lambda/sync-lambda-function"
  logStreams=$(awslocal logs describe-log-streams \
    --log-group-name "/aws/lambda/sync-lambda-function" | jq -r '.[] | .[] | .logStreamName')

  echo "Sync lambda logs:"
  for logStreamName in $(printf "%s" "$logStreams"); do
    awslocal logs get-log-events \
      --log-group-name "/aws/lambda/sync-lambda-function" \
      --log-stream-name "$logStreamName"
  done
}

main "$@"
