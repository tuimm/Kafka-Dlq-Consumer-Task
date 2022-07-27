# Kafka Dlq Consumer Task Sample


## Getting started

This sample illustrates how to run a task that consumes a dead letter queue from a Kafka topic.
The main app, as shown in the next code snippet, runs the task and when lag of consumer group is equals to zero terminates the 
service:

 ```
public void run(String... args) {
    dlqConsumer.runConsumer();
    dlqConsumer.shutdown();
    log.info("task finished");
    shutdownApp();

}
```

## Include your business logic

This sample does not implement the part of the business logic related to the event process. The main purpose of consuming an event from a dead letter queue is to be able to 
re-insert it into the source origin once the problem that caused the error has been fixed. The idea is then to replace this code snipped by your own business logic:

 ```
  for (ConsumerRecord<byte[], byte[]> record : partitionRecords) {
    processEvent(record);
  }
 ```

## How to launch the task

Fist step is configure your service and the Gitlab project to allow it to be deployed.

### Configure the project

The next environment variables can be configured:

* BOOTSTRAP_SERVERS
* GROUP_ID
* TOPIC

If they are not defined, the service will use the ones configured by default.

### Configure Gitlab Project

Because the pipeline allows execution for multiple environments, the configuration parameters also must be by each 
environment you need to deploy.
The Gitlab CI/CD pipeline require the following parameters to be fully executed according the environment:

| Environment Variable                  | Description |
| :---                                  | :---- |
| **\<ENVIRONMENT>_TARGET_ROLE**        | This value requires the role ARN deployed by IaC which allows create new task definitions and run Fargate jobs |  
| **\<ENVIRONMENT>_AWS_SUBNET**         | To run Fargate job you must specify a subnet list | 
| **\<ENVIRONMENT>_AWS_SECURITY_GROUP** | A security group list is required To run Fargate tasks | 

Where the <ENVIRONMENT> typically can take this values: TEST, PRE, PROD.

### Pipelines

Has been configured a `gitlab-ci` with two pipeline flows: 

* One of them to package your Java code and build the docker image, update the AWS task definition  deployed by 
  [IaC project](https://source.tui/dx/fulfillment/ermes/iac-kafka-dlq-consumer-task) with the docker image generated 
  by the previous job and run optionally the AWS Fargate task. This pipeline is enabled for each push to the repository.
  ![Full pipeline](assets/pipeline_1.png)
* The other one runs automatically the AWS Fargate task. This pipeline is only activated using the web interface. 
  ![Full pipeline](assets/pipeline_2.png)



