[![version](https://img.shields.io/badge/version-0.0.1-green.svg)](https://semver.org) [![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

# Leveraging AWS, Terraform and K8s to deploy a messaging pipeline
The messaging pipeline generates messages and allows the user to visualize them using the following components:

 1. **Kafka and Zookeeper** - Used for managing the incoming messages.
 2. **ELK** - The full ELK (Elasticsearch, Logstash and Kibana) stack will be utilized in order to visualize logs in Kibana.
 3. **Cassandra** - The data base for this use case.
 4. **Grafana** - With the Grafana web UI we will be able to visualize metrics.

The idea of the repo is to be able to collect messages. In this case auto generated tweets. Part of this architecture includes a message generator that allows you to explore several scenarios. You can increase or decrease the volume of messages based on your use case. 

The main goal is to receive tweets in a JSON format. Then convert the data to Avro (for more about Avro [Click Here](https://avro.apache.org/docs/current/)). The end solution produced by this deployment will allow the user to visualize, analyze and enrich the messages.

With that being said, we must be cost efficient and reliable. Thats where Cloud, CI/CD, serverless and automation come into play. Through out this article you will learn about all the tools we used in order to acheive a reproducible one click deployment of this whole stack.

This diagram is a good representation of the message flow in this app:
### Messages flow
<img src="/src/main/resources/diagram/messages_flow_new.png" width=100%>

Hopefully you will find value in the knowledge that we've gained through trial and error.
 
## Why Terraform?
While creating a CI/CD pipeline it is important to use IaC for automation purposes. Automating the infrastructure deployment is crucial for a healthy environemnt. Having an immutable deployment will allow you to focus on architecting a great app rather than patching up and monitoring monolithic solutions. Through Terraform we can deploy all the needed resources while utilizing the module structure. 

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
    - Kubernetes
    - Helm
 5. AWS IAM
    - IAM policy for codepipeline to communicate with S3
    - IAM policy for ECS
 6. AWS KMS
    - Key for S3 encryption at rest

## Version control and source
As far as source control we have decided to use Github. AWS will authenticate to Github using a token. After the authentication takes place the code will be copied to S3. This deployment runs immidietly on commit to the repo. It is possible to change this setting by applying a tag to the commit or administrating the user access to the CI/CD pipeline. To read more about how to customize triggers in AWS codepipeline please refer to this doc: [Adding custom logic to AWS codepipeline](https://aws.amazon.com/blogs/devops/adding-custom-logic-to-aws-codepipeline-with-aws-lambda-and-amazon-cloudwatch-events/)

## Buildspec.yml configuration
The build spec file compiles and produces build artifacts. These artifacts end up in the S3 bucket for this deployment. The build file will execute pre-build, build and post build actions. As you can see in this yaml file. During the `build` stage a docker image is being created and tagged. The image then is pushed to ECR. This image will later be utilized to created the Proc containers.

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
      - java -classpath messaging_pipeline.jar me.messaging.producer.KafkaProducer
      - java -classpath messaging_pipeline.jar me.messaging.LTProc.LTProc
      - java -classpath messaging_pipeline.jar me.messaging.esProc.ElasticSearchProc
      - java -classpath messaging_pipeline.jar me.messaging.enrichment.EnrichmentProc
      - java -classpath messaging_pipeline.jar me.messaging.cassandraProc.CassandraProc
    artifacts:
      type: zip
      files:
        - deploy/lib/messaging_pipeline-1.0.jar
        - NewSamTemplate.yaml
```

## How to deploy

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

## What happens during the build process?

See this table to get a better understanding of this deployment:

|          Docker        |                Cassandra                 |         Elasticsearch    |                    Grafana         |                    Jupyter                            |
|          :---:         |                  :---:                   |              :---:       |                     :---:          |                     :---:                             |              
| Copy integration dir   | docker exec -it cassandra cqlsh          |     Add index: logstash* |  Add Elasticsearch as a datasource |  Run: `docker exec -it jupyter jupyter notebook list` |
| Run KafkaProducer      | CREATE KEYSPACE kafka WITH REPLICATION   |     Add index: kafka*    |  Use elasticsearch:9200 as the URL |  Copy the token provided to the clipboard to login    |
| Run EnrichmentProc     | CREATE TABLE kafka.tweets                |                          |  Use \*kafka\* as the Index Name   |                                                       |
| Run ElasticsearchProc  | CREATE TABLE kafka.metrics               |                          |  Use version 7.0+                  |                                                       |
| Run CassandraProc      |                                          |                          |  Add Cassandra as a datasource     |                                                       | 
| Run MetricProc         |                                          |                          |  Use cassandra:9042 as the Host    |                                                       |
|                        |                                          |                          |  Use `kafka` as the keyspace       |                                                       |

<img src="/src/main/resources/diagram/metrics_flow.png" width=75%>

<img src="/src/main/resources/diagram/logs_flow.png" width=75%>


## Installation Steps for Jupyter:
Sample Python playbook:
```
from pyspark import SparkContext
from pyspark import SparkConf
from re import Match

# Currently not able to get files directly from S3 mock, therefore 
# this requires downloading the file first
url = "untitled.txt"

conf = SparkConf()
sc = SparkContext.getOrCreate(conf=conf)
text_file = sc.textFile(url)

res = text_file.flatMap(lambda line: line.split(":")) \
                .filter(lambda w: len(w.split(" ")) == 1) \
                .map(lambda w: (w, 1)) \
                .reduceByKey(lambda a, b: a + b)

print(res.collect())
```
