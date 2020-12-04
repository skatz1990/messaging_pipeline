[![GitHub version](https://badge.fury.io/gh/conventional-changelog%2Fstandard-version.svg)](https://badge.fury.io/gh/conventional-changelog%2Fstandard-version)

# Infrastructure deployment using Terraform

## Terraform
In every good CI/CD pipeline it is important to use IaC. Automating the infrastructure deployment is crucial for a healthy environemnt. Having an immutable deployment will allow you to focus on architecting a great app rather than patching up and monitoring monolithic solutions. Throuhg Terraform we can deploy all the needed resources while utilizing the module structure. 

View this high level infra diagram in order to have a better understanding of whats to come in this article:
<img src="/src/main/resources/diagram/msg_pipeline_tf_v6.png" width=100%>

## AWS resources
This deployment started as a local non-saclable POC application. In efforts to automate all things we have decided to use a few cloud native tools.
First we determied that AWS would be the best cloud platform to host our solution. In this deployment model we are using the following AWS services:

 1. S3
 2. AWS Codepipeline
    - Codebuild
    - Codebuild project
    - Codedeploy
 3. ECS
    - ECR
    - Task definitions
    - Service
 4. Fargate/EKS/EC2
 5. AWS IAM
    - IAM policy for codepipeline to communicate with S3
    - IAM policy for ECS
 6. AWS KMS
    - Key for S3 encryption at rest

## Version control and source
As far as source control we have decided to use Github. AWS will authenticate to Github using a token. After the authentication takes place the code will be copied to S3.

## Buildspec.yml configuration
The build spec file compiles and produces build artifacts. These artifacts end up in the S3 bucket for htis deployment.
```
spec.yml
version: 0.1
phases:
  build:
    commands:
      - echo Build started on `date`
      - echo Run the test and package the code...
      - sbt compile
      - sbt assembly
      - docker build -t $IMAGE_REPO_NAME:$IMAGE_TAG .
      - docker tag $IMAGE_REPO_NAME:$IMAGE_TAG $AWS_ACCOUNT_ID.dkr.ecr.$AWS_DEFAULT_REGION.amazonaws.com/$IMAGE_REPO_NAME:$IMAGE_TAG
  post_build:
    commands:
      - echo Build completed on `date`
      - mkdir deploy
      - mkdir deploy/lib
      - cp target/scala-2.13/messaging_pipeline.jar deploy/lib
      - docker push aws_account_id.dkr.ecr.region.amazonaws.com/my-web-app
      - java -classpath messaging_pipeline.jar me.skatz.producer.KafkaProducer
      - java -classpath messaging_pipeline.jar me.skatz.LTProc.LTProc
      - java -classpath messaging_pipeline.jar me.skatz.esProc.ElasticSearchProc
      - java -classpath messaging_pipeline.jar me.skatz.enrichment.EnrichmentProc
      - java -classpath messaging_pipeline.jar me.skatz.cassandraProc.CassandraProc
    artifacts:
      type: zip
      files:
        - deploy/lib/messaging_pipeline-1.0.jar
        - NewSamTemplate.yaml
```

## How to deploy

### Pre-reqs

- Install git
- Install awscli
- Install terraform

### Deploy

1. Clone the repo
```
git clone https://github.com/skatz1990/messaging_pipeline.git
```
2. Run Terraform code
```
terraform init
```
```
terraform plan
```
```
terraform apply -var github_token="*******" -var region="AWS region" -var access_key="*******" -var secret_key="*********" -var cluster_name="cluster name" -var app_service_name="service name"
```

# What happens during the build process? 
The messaging pipeline generates messages and allows the user to visualize them using the following components:

- Kafka and Zookeeper
- ELK
- Cassandra
- Grafana

<img src="/src/main/resources/diagram/messages_flow.png" width=75%>

<img src="/src/main/resources/diagram/logs_flow.png" width=75%>

<img src="/src/main/resources/diagram/metrics_flow.png" width=75%>

See this table to get a better understanding of this deployment:

|          Docker        |                Cassandra                 |         Elasticsearch    |                    Grafana         | 
|          :---:         |                  :---:                   |              :---:       |                     :---:          |
| Copy integration dir   | docker exec -it cassandra cqlsh          |     Add index: logstash* |  Add Elasticsearch as a datasource |
| Run KafkaProducer      | CREATE KEYSPACE kafka WITH REPLICATION   |     Add index: kafka*    |  Use elasticsearch:9200 as the URL |
| Run EnrichmentProc     | REATE TABLE kafka.tweets                 |                          |  Use \*kafka\* as the Index Name   |
| Run ElasticsearchProc  | CREATE TABLE kafka.metrics               |                          |  Use version 7.0+                  |
| Run CassandraProc      |                                          |                          |  Add Cassandra as a datasource     | 
| Run MetricProc         |                                          |                          |  Use cassandra:9042 as the Host    |
| Run MetricProc         |                                          |                          |  Use `kafka` as the keyspace       |